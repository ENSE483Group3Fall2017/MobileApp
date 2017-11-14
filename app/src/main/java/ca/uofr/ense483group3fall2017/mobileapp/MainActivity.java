package ca.uofr.ense483group3fall2017.mobileapp;

import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.RemoteException;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import org.altbeacon.beacon.BeaconConsumer;
import org.altbeacon.beacon.BeaconManager;
import org.altbeacon.beacon.MonitorNotifier;
import org.altbeacon.beacon.Region;

public class MainActivity extends AppCompatActivity implements BeaconConsumer {

    public static final int REQUEST_ENABLE_BT = 100;

    protected static final String TAG = "MonitoringActivity";

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
        mBeaconManager.unbind(this);
    }

    @Override
    public void onBeaconServiceConnect() {
        writeLog("onBeaconServiceConnect called...");
        mBeaconManager.addMonitorNotifier(new MonitorNotifier() {
            @Override
            public void didEnterRegion(Region region) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        writeLog( "I just saw an beacon for the first time!");
                    }
                });
            }

            @Override
            public void didExitRegion(Region region) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        writeLog("I no longer see an beacon");
                    }
                });
            }

            @Override
            public void didDetermineStateForRegion(final int state, Region region) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        writeLog("I have just switched from seeing/not seeing beacons: "+ state);
                    }
                });
            }
        });

        try {
            mBeaconManager.startMonitoringBeaconsInRegion(new Region("myMonitoringUniqueId", null, null, null));
        } catch (RemoteException e) {
            writeLog(e.getMessage());
        }
    }

    private void writeLog(String msg) {
        mMonitoringLog.append(msg + "\n \n");
    }
}
