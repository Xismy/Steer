package com.example.iye19.steer;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Parcel;
import android.os.ParcelUuid;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Set;


public class Setup extends AppCompatActivity {

    private ArrayAdapter<String> devices_name;
    private ArrayList<BluetoothDevice> devices = new ArrayList<>();
    private final BroadcastReceiver  mReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                for(BluetoothDevice device_reg:devices)
                    if(device.getAddress().equals(device_reg.getAddress()))
                        return;
                devices_name.add(device.getName());
                devices.add(device);
            }
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setup);
        BluetoothAdapter bta = BluetoothAdapter.getDefaultAdapter();

        if (!bta.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, 1);
        }
        if (!bta.isEnabled())
            finish();
        //Bluetooh activado

        devices_name= new ArrayAdapter<>(this, R.layout.device);
        ListView list = ((ListView) findViewById(R.id.listView));
        list.setAdapter(devices_name);
        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent steer = new Intent(Setup.this, Steer.class);
                steer.putExtra("device", devices.get(position));
                startActivity(steer);
            }
        });

        Set<BluetoothDevice> pairedDevices = bta.getBondedDevices();
        for (BluetoothDevice device : pairedDevices) {
                devices_name.add(device.getName());
                devices.add(device);
        }

        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        registerReceiver(mReceiver, filter);

        bta.startDiscovery();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mReceiver);
    }
}

