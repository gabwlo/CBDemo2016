package com.caseweek.comarchbeacon.cbdemo;

import android.app.Activity;
import android.app.Dialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanRecord;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Looper;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;

import java.util.ArrayList;

public class MainActivityFragment extends Fragment {

    private static final String TAG = MainActivityFragment.class.getSimpleName();

    public static final int REQUEST_CODE_ENABLE_BLE = 1001;

    private static final long SCAN_TIME_MILLIS = 2000;

    // Receives the runnable that stops scanning after SCAN_TIME_MILLIS.
    private static final Handler handler = new Handler(Looper.getMainLooper());

    // An aggressive scan for nearby devices that reports immediately.
    private static final ScanSettings SCAN_SETTINGS =
            new ScanSettings.Builder().
                    setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
                    .setReportDelay(0)
                    .build();

    // private static final ParcelUuid CBEACON_UUID = ParcelUuid.fromString("4d100990-0f3e-444f-8184-a840bbd1aa8c");
    // A filter that scans only for devices with the UUID.
    // private static final ScanFilter CBEACON_SCAN_FILTER = new ScanFilter.Builder()
    //          .setServiceUuid(CBEACON_UUID)
    //          .build();

    // private static final List<ScanFilter> SCAN_FILTERS = buildScanFilters();

    // private static List<ScanFilter> buildScanFilters() {
    //     List<ScanFilter> scanFilters = new ArrayList<>();
    //     scanFilters.add(CBEACON_SCAN_FILTER);
    //     return scanFilters;
    // }

    private SharedPreferences sharedPreferences;
    private ArrayList<CBeacon> arrayList;
    private BeaconArrayAdapter arrayAdapter;
    private ScanCallback scanCallback;
    private BluetoothLeScanner scanner;
    private FloatingActionButton scanButton;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        arrayList = new ArrayList<>();
        arrayAdapter = new BeaconArrayAdapter(getActivity(), R.layout.cbeacon_list_item, arrayList);

        scanCallback = new ScanCallback() {
            @Override
            public void onScanResult(int callbackType, ScanResult result) {
                ScanRecord scanRecord = result.getScanRecord();
                if (scanRecord == null) {
                    Log.w(TAG, "Null ScanRecord for device " + result.getDevice().getAddress());
                    return;
                }
                byte[] scanRecordByte = scanRecord.getBytes();
                int startByte = 2;
                boolean patternFound = false;
                while (startByte <= 5) {
                    if (    ((int) scanRecordByte[startByte + 2] & 0xff) == 0x02 && //Identifies an iBeacon
                            ((int) scanRecordByte[startByte + 3] & 0xff) == 0x15) { //Identifies correct data length
                        patternFound = true;
                        break;
                    }
                    startByte++;
                }
                if (patternFound) {
                    //Convert to hex String
                    byte[] uuidBytes = new byte[16];
                    System.arraycopy(scanRecordByte, startByte+4, uuidBytes, 0, 16);
                    String hexString = MainActivity.bytesToHex(uuidBytes);
                    //Here is your UUID
                    String uuid =  hexString.substring(0,8) + "-" +
                            hexString.substring(8,12) + "-" +
                            hexString.substring(12,16) + "-" +
                            hexString.substring(16,20) + "-" +
                            hexString.substring(20,32);
                    //Here is your Major value
                    int major = (scanRecordByte[startByte+20] & 0xff) * 0x100 + (scanRecordByte[startByte+21] & 0xff);
                    //Here is your Minor value
                    int minor = (scanRecordByte[startByte+22] & 0xff) * 0x100 + (scanRecordByte[startByte+23] & 0xff);
                    CBeacon beacon = new CBeacon(uuid, major, minor,scanRecord.getTxPowerLevel(), result.getRssi(), scanRecord.toString());
                    insertIntoList(beacon);
                }
                // Extract the beacon ID from the service data. Offset 0 is the frame type, 1 is the
                // Tx power, and the next 16 are the ID.
            }

            @Override
            public void onScanFailed(int errorCode) {
                Log.e(TAG, "onScanFailed errorCode " + errorCode);
            }
        };

        createScanner();
    }

    private void insertIntoList(final CBeacon beacon) {
        arrayAdapter.add(beacon);
        arrayAdapter.sort(CBeacon.RSSI_COMPARATOR);
        updateArrayAdapter();
    }

    private void updateArrayAdapter() {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                arrayAdapter.notifyDataSetChanged();
            }
        });
    }

    private void createScanner() {
        BluetoothManager btManager = (BluetoothManager)getActivity().getSystemService(Context.BLUETOOTH_SERVICE);
        BluetoothAdapter btAdapter = btManager.getAdapter();
        if (btAdapter == null || !btAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_CODE_ENABLE_BLE);
        }
        if (btAdapter == null || !btAdapter.isEnabled()) {
            Log.e(TAG, "Can't enable Bluetooth");
            Toast.makeText(getActivity(), "Can't enable Bluetooth", Toast.LENGTH_SHORT).show();
            return;
        }
        scanner = btAdapter.getBluetoothLeScanner();
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_ENABLE_BLE) {
            if (resultCode == Activity.RESULT_OK) {
                createScanner();
            }
            else if (resultCode == Activity.RESULT_CANCELED) {
                Toast.makeText(getActivity(), "Please enable Bluetooth", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_main, container, false);

        final ProgressBar progressBar = (ProgressBar)rootView.findViewById(R.id.progressBar);
        progressBar.setProgress(0);
        progressBar.getProgressDrawable().setColorFilter(Color.GREEN, PorterDuff.Mode.MULTIPLY);
        progressBar.setVisibility(View.GONE);
        scanButton = (FloatingActionButton) getActivity().findViewById(R.id.fab);
        scanButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                arrayAdapter.clear();
                progressBar.setVisibility(View.VISIBLE);
                //scanner.startScan(SCAN_FILTERS, SCAN_SETTINGS, scanCallback);
                scanner.startScan(scanCallback);
                Log.i(TAG, "starting scan");
                //client = new ProximityBeaconImpl(getActivity(), accountNameView.getText().toString());
                CountDownTimer countDownTimer = new CountDownTimer(SCAN_TIME_MILLIS, 100) {
                    @Override
                    public void onTick(long millisUntilFinished) {
                        double i = (1 - millisUntilFinished / (double) SCAN_TIME_MILLIS) * 100;
                        progressBar.setProgress((int) i);
                    }

                    @Override
                    public void onFinish() {
                        progressBar.setProgress(100);
                        progressBar.setVisibility(View.GONE);
                        ((MainActivity) getActivity()).increaseBeaconsCounter(arrayAdapter.getCount());
                    }
                };
                countDownTimer.start();

                Runnable stopScanning = new Runnable() {
                    @Override
                    public void run() {
                        scanner.stopScan(scanCallback);
                        Log.i(TAG, "stopped scan");
                    }
                };
                handler.postDelayed(stopScanning, SCAN_TIME_MILLIS);
            }
        });

        ListView listView = (ListView)rootView.findViewById(R.id.listView);
        listView.setAdapter(arrayAdapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                CBeacon beacon = arrayAdapter.getItem(position);
                saveBeaconColor(beacon);
                //DatabaseHelper.getInstance(getActivity()).getBeacons();
            }
        });
        return rootView;
    }

    private void saveBeaconColor(final CBeacon beacon) {
        // TODO
    }
}
