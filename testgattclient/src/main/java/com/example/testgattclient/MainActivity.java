package com.example.testgattclient;

import android.Manifest;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.library.utils.BluetoothUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainActivity extends Activity implements View.OnClickListener {

    private static final int REQUEST_OPEN_BLUETOOTH = 1;
    private static final String TAG = "bush";
    private static final int PERMISSION_REQUEST_LOCATION = 2;
    private ListView mListView;
    private Button mButton;
    private boolean mScanning;
    private Map<String, ScanResult> mScanResultMap;
    private ScanAdapter mAdapter;
    private BluetoothAdapter.LeScanCallback mScanCallback = new BluetoothAdapter.LeScanCallback() {
        @Override
        public void onLeScan(BluetoothDevice device, int rssi, byte[] scanRecord) {
            ScanResult result = new ScanResult(device, rssi, scanRecord);

            if (!mScanResultMap.containsKey(device.getAddress())) {
                mScanResultMap.put(device.getAddress(), result);
                Log.v(TAG, String.format("onLeScan: mac=%s, name=%s, record=(%s)%d",
                        device.getAddress(), device.getName(), byteToString(scanRecord), scanRecord.length));
                mAdapter.refresh(mScanResultMap);
            }
        }
    };
    private String[] permissions = {Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION};

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mButton = findViewById(R.id.btn);
        mButton.setOnClickListener(this);

        mListView = findViewById(R.id.listview);
        mAdapter = new ScanAdapter(this);
        mListView.setAdapter(mAdapter);

        mScanResultMap = new HashMap<>();

        // need location permissions
        if (!checkPermissions()) {
            return;
        }

        checkBluetooth();
    }

    private boolean checkPermissions() {
        boolean result = true;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            for (String permission : permissions) {
                if (checkSelfPermission(permission) != PackageManager.PERMISSION_GRANTED) {
                    result = false;
                    break;
                }
            }
            if (!result) {
               requestPermissions(permissions, PERMISSION_REQUEST_LOCATION);
            }
        }
        return result;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        boolean grantedLocation = true;
        if (requestCode == PERMISSION_REQUEST_LOCATION) {
            for (int i : grantResults) {
                if (i != PackageManager.PERMISSION_GRANTED) {
                    grantedLocation = false;
                }
            }
        }

        if (!grantedLocation) {
            Toast.makeText(this, "Permission error !!!", Toast.LENGTH_SHORT).show();
            finish();
        } else {
            checkBluetooth();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_OPEN_BLUETOOTH && resultCode != RESULT_OK) {
            finish();
        }
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.btn) {
            processClicked();
        }
    }

    private void processClicked() {
        BluetoothAdapter adapter = BluetoothUtils.getBluetoothAdapter();
        if (adapter == null) {
            Toast.makeText(this, "Bluetooth not supported", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!adapter.isEnabled()) {
            checkBluetooth();
            return;
        }

        if (!mScanning) {
            if (!adapter.startLeScan(mScanCallback)) {
                Toast.makeText(this, "Scan failed", Toast.LENGTH_SHORT).show();
            } else {
                mScanResultMap.clear();
                // add connected devices
                for (BluetoothDevice device : BluetoothUtils.getConnectedDevices()) {
                    mScanResultMap.put(device.getAddress(), new ScanResult(device, 0, null));
                }
                onScanStarted();
            }
        } else {
            adapter.stopLeScan(mScanCallback);
            onScanFinished();
        }
    }

    private void onScanStarted() {
        mScanning = true;
        mButton.setText(R.string.stop_scan);
    }

    private void onScanFinished() {
        mScanning = false;
        mButton.setText(R.string.start_scan);
    }

    @Override
    protected void onPause() {
        super.onPause();
        processClicked();
    }

    private void checkBluetooth() {
        if (!BluetoothUtils.isBluetoothOpen()) {
            BluetoothUtils.openBluetooth(this, REQUEST_OPEN_BLUETOOTH);
        }
    }

    private static class ScanResult {
        BluetoothDevice device;
        int rssi;
        byte[] record;

        public ScanResult(BluetoothDevice device, int rssi, byte[] record) {
            this.device = device;
            this.rssi = rssi;

            if (record != null) {
                this.record = new byte[record.length];
                System.arraycopy(record, 0, this.record, 0, record.length);
            } else {
                this.record = new byte[0];
            }
        }
    }

    private static String byteToString(byte[] bytes) {
        StringBuilder stringBuilder = new StringBuilder();
        if (bytes != null) {
            for (byte aByte : bytes) {
                stringBuilder.append(String.format("%02X", aByte));
            }
        }
        return stringBuilder.toString();
    }

    private static class ScanAdapter extends BaseAdapter {

        private List<ScanResult> mDatas = new ArrayList<>();
        private Context mContext;

        public ScanAdapter(Context context) {
            mContext = context;
        }

        public void refresh(Map<String, ScanResult> results) {
            mDatas.clear();
            mDatas.addAll(results.values());
            Collections.sort(mDatas, new Comparator<ScanResult>() {
                @Override
                public int compare(ScanResult o1, ScanResult o2) {
                    return o2.rssi - o1.rssi;
                }
            });
            notifyDataSetChanged();
        }

        @Override
        public int getCount() {
            return mDatas.size();
        }

        @Override
        public ScanResult getItem(int position) {
            return mDatas.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View view = convertView;
            ViewHolder viewHolder;
            if (view != null) {
                viewHolder = (ViewHolder) view.getTag();
            } else {
                view = LayoutInflater.from(mContext).inflate(R.layout.scan_result_item, null);
                viewHolder = new ViewHolder(view);
                view.setTag(viewHolder);
            }
            bindViewHolder(viewHolder, mDatas.get(position));
            return view;
        }

        private void bindViewHolder(ViewHolder viewHolder, final ScanResult scanResult) {
            String name = scanResult.device.getName();
            if (TextUtils.isEmpty(name)) {
                name = "UnKnown";
            }
            viewHolder.name.setText(name);
            viewHolder.mac.setText(scanResult.device.getAddress());
            viewHolder.data.setText(parseBytes(scanResult.record));
            viewHolder.rssi.setText(String.valueOf(scanResult.rssi));

            viewHolder.itemView.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(mContext, DeviceActivity.class);
                    intent.putExtra("device", scanResult.device);
                    mContext.startActivity(intent);
                }
            });
        }

        private static String parseBytes(byte[] bytes) {
            StringBuilder stringBuilder = new StringBuilder();
            for (Parser.Pdu pdu : Parser.parse(bytes)) {
                int type = pdu.type;
                byte[] data = pdu.data;
                stringBuilder.append(String.format("Type: 0x%02x, Data: %s\n", type, byteToString(data)));
            }
            return stringBuilder.toString();
        }


        private static class ViewHolder {
            View itemView;
            TextView name;
            TextView mac;
            TextView data;
            TextView rssi;

            ViewHolder(View view) {
                itemView = view;
                name = view.findViewById(R.id.name);
                mac = view.findViewById(R.id.mac);
                data = view.findViewById(R.id.data);
                rssi = view.findViewById(R.id.rssi);
            }
        }
    }
}
