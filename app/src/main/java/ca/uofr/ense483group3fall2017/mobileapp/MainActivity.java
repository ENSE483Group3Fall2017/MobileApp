package ca.uofr.ense483group3fall2017.mobileapp;

import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.location.Location;
import android.location.LocationManager;
import android.os.RemoteException;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;

import org.altbeacon.beacon.Beacon;
import org.altbeacon.beacon.BeaconConsumer;
import org.altbeacon.beacon.BeaconManager;
import org.altbeacon.beacon.MonitorNotifier;
import org.altbeacon.beacon.RangeNotifier;
import org.altbeacon.beacon.Region;

import java.util.ArrayList;
import java.util.Collection;
import java.util.UUID;

import ca.uofr.ense483group3fall2017.mobileapp.data.TrackingDbHelper;
import ca.uofr.ense483group3fall2017.mobileapp.data.TrackingInfoContract;
import ca.uofr.ense483group3fall2017.mobileapp.dto.BeaconInfo;

public class MainActivity extends AppCompatActivity implements BeaconConsumer {

    private static final int REQUEST_ENABLE_BT = 100;
    private static final int REQUEST_ENABLE_GPS = 101;

    private static final String TAG = "MonitoringActivity";
    private final Region BEACON_REGION = new Region("ca.uofr.ense483group3fall2017.region", null,null, null);

    private FusedLocationProviderClient mFusedLocationClient;

    private boolean isBeaconRangingStarted = false;
    private BeaconManager mBeaconManager;
    private TextView mMonitoringLog;

    private SQLiteDatabase mDb;

    private static string serverURL = "http://messagebrokerwebapiense483group3fall2017.azurewebsites.net/api/PetTracking";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mMonitoringLog = findViewById(R.id.tv_monitoring_log);

        enableBluetoothOnStart();
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        mBeaconManager = BeaconManager.getInstanceForApplication(this);

        mDb = new TrackingDbHelper(this).getWritableDatabase();

        mBeaconManager.bind(this);


        // starting SERVER Connection
        final RequestQueue requestQueue = Volley.newRequestQueue(MainActivity.this);
        StringRequest stringRequest = new stringRequest (Request.Method.POST,serverURL,
                new Response.Listener<String>(){
                    @Override
                    public void onResponse (String response){
                        mMonitoringLog.setText(response);
                        requestQueue.stop();
                    }
                }, new Response.ErrorListener(){
            @Override
            public void onErrorResponse(VolleyError error){
                mMonitoringLog.setText("Error occured while sending data");
                error.printStackTrace();
                requestQueue.stop();
            }
        });
        requestQueue.add(stringRequest);
    }

    private void enableGpsOnStart() {
        if(!isLocationServiceEnabled()) {
            Intent enableGpsIntent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
            startActivityForResult(enableGpsIntent, REQUEST_ENABLE_GPS);
        }
    }

    private void handleEnableGpsResult() {
        if(isLocationServiceEnabled()) return;

        AlertDialog.Builder noGpsDialogBuilder = new AlertDialog.Builder(this);
        noGpsDialogBuilder
                .setTitle(R.string.app_will_be_closed_title)
                .setMessage(R.string.dialog_no_gps_message)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        MainActivity.this.finish();
                    }
                });

        AlertDialog noBtDialog = noGpsDialogBuilder.create();
        noBtDialog.show();
    }

    private boolean isLocationServiceEnabled() {
        LocationManager manager = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
        return  manager.isProviderEnabled(LocationManager.GPS_PROVIDER);
    }

    private void enableBluetoothOnStart() {
        if (!BluetoothAdapter.getDefaultAdapter().isEnabled()) {
            Intent enableBluetoothIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBluetoothIntent, REQUEST_ENABLE_BT);
        }
        else {
            enableGpsOnStart();
        }
    }

    private void handleEnableBluetoothResult(int resultCode) {
        if (Activity.RESULT_OK == resultCode)
            return;

        AlertDialog.Builder noBtDialogBuilder = new AlertDialog.Builder(this);
        noBtDialogBuilder
                .setTitle(R.string.app_will_be_closed_title)
                .setMessage(R.string.dialog_no_bt_message)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        MainActivity.this.finish();
                    }
                });

        AlertDialog noBtDialog = noBtDialogBuilder.create();
        noBtDialog.show();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_ENABLE_BT:
                handleEnableBluetoothResult(resultCode);
                enableGpsOnStart();
                break;
            case REQUEST_ENABLE_GPS:
                handleEnableGpsResult();
                break;
            default:
                super.onActivityResult(requestCode, resultCode, data);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mBeaconManager.removeAllRangeNotifiers();
        mBeaconManager.removeAllMonitorNotifiers();
        try {
            mBeaconManager.stopRangingBeaconsInRegion(BEACON_REGION);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        try {
            mBeaconManager.stopMonitoringBeaconsInRegion(BEACON_REGION);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        mBeaconManager.unbind(this);
    }

    @Override
    public void onBeaconServiceConnect() {
        writeLog("onBeaconServiceConnect called...");
        registerBeaconMonitorNotifier();
        startBeaconMonitoringForRegion(BEACON_REGION);
    }

    private void startBeaconMonitoringForRegion(Region region) {
        try {
            mBeaconManager.startMonitoringBeaconsInRegion(region);
        } catch (RemoteException e) {
            writeLog(e.getMessage());
        }
    }

    private void registerBeaconMonitorNotifier() {
        mBeaconManager.addMonitorNotifier(new MonitorNotifier() {
            @Override
            public void didEnterRegion(Region region) {
            }

            @Override
            public void didExitRegion(Region region) {
            }

            @Override
            public void didDetermineStateForRegion(final int state, Region region) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        writeLog("I have just switched from seeing/not seeing beacons: "+ state);

                        switch (state) {
                            case 1:
                                startBeaconRanging();
                                break;
                            case 0:
                                stopBeaconRanging();
                                break;
                        }
                    }
                });
            }
        });
    }

    public void startBeaconRanging() {
        if (isBeaconRangingStarted) return;
        isBeaconRangingStarted = true;

        mBeaconManager.addRangeNotifier(
            new RangeNotifier() {
                @Override
                public void didRangeBeaconsInRegion(Collection<Beacon> collection, Region region) {
                    final BeaconInfo[] beacons = mapBeaconInfos(collection);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() { logBeacons(beacons); }
                    });
                }
            }
        );

        try {
            mBeaconManager.startRangingBeaconsInRegion(BEACON_REGION);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    private BeaconInfo[] mapBeaconInfos(Collection<Beacon> collection) {
        ArrayList<BeaconInfo> foundBeacons = new ArrayList<>();
        for (Beacon b : collection) {
            int major = Integer.parseInt(b.getId2().toString());
            int minor = Integer.parseInt(b.getId3().toString());
            String beaconId = String.format("%1$05d-%2$05d", major, minor);
            foundBeacons.add(new BeaconInfo(
              b.getId1().toString(),
              beaconId,
              b.getDistance()
            ));
        }
        BeaconInfo[] beacons = new BeaconInfo[foundBeacons.size()];
        foundBeacons.toArray(beacons);
        return  beacons;
    }

    public void stopBeaconRanging() {
        if (!isBeaconRangingStarted);
        isBeaconRangingStarted = false;

        mBeaconManager.removeAllRangeNotifiers();
    }

    private void writeLog(String msg) {
        mMonitoringLog.append(msg + "\n");
    }

    private void logBeacons(BeaconInfo[] beacons) {
        for (BeaconInfo b : beacons) {
            writeLog("Region: {"+ b.getRegionId() + "}");
            writeLog("Beacon: {"+ b.getBeaonId() + "}");
            writeLog("Proximity: {"+ b.getProximityAsString() + "}");
            writeLog("");
            saveBeaconInfo(b);
        }
    }

    private void saveBeaconInfo(BeaconInfo beaconInfo) {
        ContentValues beaconValues = new ContentValues();
        beaconValues.put(TrackingInfoContract._ID, UUID.randomUUID().toString());
        beaconValues.put(TrackingInfoContract.COLUMN_BEACON_ID, beaconInfo.getBeaonId());
        beaconValues.put(TrackingInfoContract.COLUMN_PROXIMITY, beaconInfo.getProximity());
        mDb.insert(TrackingInfoContract.TABLE_NAME, null, beaconValues);
    }
}
