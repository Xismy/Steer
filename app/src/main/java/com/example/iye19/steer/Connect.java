package com.example.iye19.steer;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.Application;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import java.io.IOException;
import java.io.Serializable;
import java.util.UUID;

/**
 * Created by iye19 on 27/04/2016.
 */
public class Connect extends AsyncTask<BluetoothDevice, Object, BluetoothSocket>{

    public static final UUID STEER_UID = new UUID(0xA16F2B14C236BB07L, 0x702A904C278FB892L);

    Steer ui;

    Connect(Steer ui){
        this.ui=ui;
    }

    @Override
    protected BluetoothSocket doInBackground(BluetoothDevice... params) {
        /**
         * TODO:
         * Parar descubrimiento antes de ejecutar y reanudarlo si hay algun error
         */
        try {
            BluetoothSocket sock= params[0].createInsecureRfcommSocketToServiceRecord(STEER_UID);
            sock.connect();
            return sock;
        }
        catch(IOException ioex){
            return null;
        }
    }

    @Override
    protected void onPostExecute(BluetoothSocket bluetoothSocket) {
        super.onPostExecute(bluetoothSocket);
        ui.setSocket(bluetoothSocket);
    }
}
