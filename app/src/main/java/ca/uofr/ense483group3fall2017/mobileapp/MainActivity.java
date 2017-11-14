package ca.uofr.ense483group3fall2017.mobileapp;

import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import org.altbeacon.beacon.BeaconManager;

public class MainActivity extends AppCompatActivity {

    public static final int REQUEST_ENABLE_BT = 100;

    protected static final String TAG = "MonitoringActivity";

    private BeaconManager mBeaconManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        enableBluetoothOnStart();

        mBeaconManager = BeaconManager.getInstanceForApplication(this);
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
}
