package ca.uofr.ense483group3fall2017.mobileapp;

import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.RemoteException;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import org.altbeacon.beacon.Beacon;
import org.altbeacon.beacon.BeaconConsumer;
import org.altbeacon.beacon.BeaconManager;
import org.altbeacon.beacon.MonitorNotifier;
import org.altbeacon.beacon.RangeNotifier;
import org.altbeacon.beacon.Region;

import java.util.ArrayList;
import java.util.Collection;

import ca.uofr.ense483group3fall2017.mobileapp.dto.BeaconInfo;

public class MainActivity extends AppCompatActivity implements BeaconConsumer {

    public static final int REQUEST_ENABLE_BT = 100;

    protected static final String TAG = "MonitoringActivity";

    private final Region BECAON_REGION = new Region("ca.uofr.ense483group3fall2017.region", null,null, null);

    private boolean isBeaconRangingStarted = false;
    private BeaconManager mBeaconManager;
    private TextView mMonitoringLog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mMonitoringLog = findViewById(R.id.tv_monitoring_log);

        enableBluetoothOnStart();

        mBeaconManager = BeaconManager.getInstanceForApplication(this);
        mBeaconManager.bind(this);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_ENABLE_BT:
                handleEnableBluetoothResult(resultCode);
                break;
            default:
                super.onActivityResult(requestCode, resultCode, data);
        }
    }

    private void enableBluetoothOnStart() {
        if (!BluetoothAdapter.getDefaultAdapter().isEnabled()) {
            Intent enableBluetoothIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBluetoothIntent, REQUEST_ENABLE_BT);
        }
    }

    private void handleEnableBluetoothResult(int resultCode) {
        if (Activity.RESULT_OK == resultCode)
            return;

        AlertDialog.Builder noBtDialogBuilder = new AlertDialog.Builder(this);
        noBtDialogBuilder
                .setTitle(R.string.dalog_no_bt_title)
                .setMessage(R.string.dalog_no_bt_message)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        MainActivity.this.finish();
                    }
                });

        AlertDialog noBtDialog = noBtDialogBuilder.create();
        noBtDialog.show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mBeaconManager.removeAllRangeNotifiers();
        mBeaconManager.removeAllMonitorNotifiers();
        try {
            mBeaconManager.stopRangingBeaconsInRegion(BECAON_REGION);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        try {
            mBeaconManager.stopMonitoringBeaconsInRegion(BECAON_REGION);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        mBeaconManager.unbind(this);
    }

    @Override
    public void onBeaconServiceConnect() {
        writeLog("onBeaconServiceConnect called...");
        registerBeaconMonitorNotifier();
        startBeaconMonitoringForRegion(BECAON_REGION);
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
            mBeaconManager.startRangingBeaconsInRegion(BECAON_REGION);
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
        }
    }




    //Three Listener Update & Insert & Delete & Select need to be fixed
    //The red part need to change to the button name, myDb is the to call the database function
    public void UpdateDate(){
        update.setOnClickListener(
                new View.OnClickListener(){
                    public void onClick(View v){
                        boolean isUpdate = myDb.updateData(editStatus.getText(), editTextId.getText());
                        if(isUpdate == true){
                            Toast.makeText(MainActivity.this, "Data Updated", Toast.LENGTH_LONG).show();
                        }else{
                            Toast.makeText(MainActivity.this, "Data Not Updated", Toast.LENGTH_LONG).show();
                        }
                    }
                }
        );
    }

    public void AddData(){
        btnAddData.setOnClickListener(
                new View.OnClickListener(){
                    public void onClick(View v){
                        boolean isInserted = myDb.insertData(editStatus.getText().toString());
                        if(isInserted == true)
                            Toast.makeText(MainActivity.this, "Data Inserted", Toast.LENGTH_LONG).show();
                        else
                            Toast.makeText(MainActivity.this, "Data Not Inserted", Toast.LENGTH_LONG).show();
                    }
                }
        );
    }

    public void DeleteData(){
        delete.setOnClickListener(
                new View.OnClickListener(){
                    public void onClick(View v){
                        Integer deletedRows = myDb.deleteData(editTextId.getText().toString());
                        if(deletedRows > 0)
                            Toast.makeText(MainActivity.this, "Data Deleted", Toast.LENGTH_LONG).show();
                        else
                            Toast.makeText(MainActivity.this, "Data Not Deleted", Toast.LENGTH_LONG).show();
                    }
                }
        );
    }

    public void ViewAll(){
        viewAll.setOnClickListener(
                new View.OnClickListener(){
                    public void onClick(View v){
                        Cursor res = myDb.getAllData();
                        if(res.getColumnCount() == 0){
                            //show message
                            showMessage("Error", "Northing");
                            return;
                        }
                        StringBuffer buffer = new StringBuffer();
                        while(res.moveToNext()){
                            buffer.append("Id: " + res.getString(0)+"\n");
                            buffer.append("Status: " + res.getString(1)+"\n\n");
                        }
                        //show all data
                        showMessage("Data", buffer.toString());
                    }
                }
        );
    }

    public void showMessage(String title, String message){
        android.support.v7.app.AlertDialog.Builder builder = new android.support.v7.app.AlertDialog.Builder(this);
        builder.setCancelable(true);
        builder.setTitle(title);
        builder.setMessage(message);
        builder.show();
    }

    //End of Three Listener Update & Insert & Delete & Select need to be fixed



}
