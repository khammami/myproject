package com.example.myproject;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.os.Bundle;


import android.os.Handler;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Set;
import java.util.UUID;

public class HomePage extends AppCompatActivity {

    Button connect, on, off, disconnect;
    TextView text,text1;
    BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    BluetoothDevice mDevice;
    BluetoothSocket mmSocket;
    InputStream mmInStream;
    OutputStream mmOutStream;
    boolean status = false;
    Thread workerThread;
    byte[] readBuffer;
    int readBufferPosition;

    volatile boolean stopWorker;


    String deviceHardwareAddress, TAG;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        setUp();


        connect.setOnClickListener(view -> {
            Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();


            for (BluetoothDevice device : pairedDevices) {
                text.setText(device.toString());

                deviceHardwareAddress = "F4:B7:E2:64:5B:D6";

            }

            mDevice = mBluetoothAdapter.getRemoteDevice(deviceHardwareAddress);


            if (!status) {
                ConnectThread ct = new ConnectThread(mDevice);
                ct.start();

                ConnectedThread cet = new ConnectedThread(mmSocket);
            } else {
                Toast.makeText(HomePage.this, "You are already connected click disconnected if you wish to connect on another device", Toast.LENGTH_SHORT).show();
            }


        });


        on.setOnClickListener(view -> {

            if (status) {
                on_led(1);
                text.setText("LED TURNED ON");
            } else {
                Toast.makeText(HomePage.this, "first connect to arduino", Toast.LENGTH_SHORT).show();
            }

        });

        off.setOnClickListener(view -> {

            if (status) {
                off_led(0);
                text.setText("LED TURNED OFF");
            } else {
                Toast.makeText(HomePage.this, "first connect to arduino", Toast.LENGTH_SHORT).show();
            }

        });
        disconnect.setOnClickListener(view -> {

            if (status) {
                status = false;
                Toast.makeText(HomePage.this, "You've been disconnected", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(HomePage.this, "You are not connected", Toast.LENGTH_SHORT).show();
            }

        });


    }

    private void setUp() {
        connect = (Button) findViewById(R.id.button);
        on = (Button) findViewById(R.id.button2);
        off = (Button) findViewById(R.id.button3);
        //disconnect = (Button) findViewById(R.id.button4);
        text = (TextView) findViewById(R.id.textView2);
        //text1 = (TextView) findViewById(R.id.textView3);


    }


    private class ConnectThread extends Thread {

        public final BluetoothDevice mmDevice;

        public ConnectThread(BluetoothDevice device) {
            BluetoothSocket tmp = null;
            mmDevice = device;

            try {
                UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
                tmp = device.createRfcommSocketToServiceRecord(MY_UUID);
                Log.d(TAG, "Socket's create() success");
            } catch (IOException e) {
                Log.e(TAG, "Socket's create() method failed", e);
            }
            mmSocket = tmp;
        }


        public void run() {
            mBluetoothAdapter.cancelDiscovery();

            try {
                mmSocket.connect();
                status = true;
            } catch (IOException connectException) {
                try {
                    mmSocket.close();
                } catch (IOException closeException) {
                    Log.e(TAG, "Could not close the client socket", closeException);
                }
                return;
            }

            runOnUiThread(() -> {
                if (status) {
                    Toast.makeText(HomePage.this, "Successfully connected", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(HomePage.this, "connection failed", Toast.LENGTH_SHORT).show();

                }

            });

        }


    }

    private class ConnectedThread extends Thread {
        public final BluetoothSocket mmSocket;

        public ConnectedThread(BluetoothSocket socket){
            mmSocket = socket;
            InputStream tmpIn = null;
            OutputStream tmpOut = null;
            try {
                tmpIn = socket.getInputStream();
                tmpOut = socket.getOutputStream();
                beginListenForData();

                Log.d(TAG, "Input Output stream create successfully");

            } catch (IOException e) {

                Toast.makeText(HomePage.this, e.getMessage().toString(), Toast.LENGTH_SHORT).show();
            }

            mmInStream = tmpIn;
            mmOutStream = tmpOut;



        }

    }

    public void on_led(int a) {
        try {
            mmOutStream.write(a);
            Log.d(TAG, "on signal send successfully");

        } catch (IOException e) {
            Log.e(TAG, "Error occurred when sending data", e);

        }
    }

    public void off_led(int b) {
        try {
            mmOutStream.write(b);

            Log.d(TAG, "off signal send successfully");

        } catch (IOException e) {
            Log.e(TAG, "Error occurred when sending data", e);

        }
    }

    void beginListenForData() throws IOException {
        final Handler handler = new Handler();
        final byte delimiter = 10; //This is the ASCII code for a newline character

        stopWorker = false;
        readBufferPosition = 0;
        readBuffer = new byte[1024];

        workerThread = new Thread(new Runnable() {
            public void run() {
                try {
                    mmSocket.connect();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                while (!Thread.currentThread().isInterrupted() && !stopWorker) {
                    try {

                        int bytesAvailable = mmInStream.available();
                        if (bytesAvailable > 0) {
                            byte[] packetBytes = new byte[bytesAvailable];
                            mmInStream.read(packetBytes);
                            for (int i = 0; i < bytesAvailable; i++) {
                                byte b = packetBytes[i];
                                if (b == delimiter) {
                                    byte[] encodedBytes = new byte[readBufferPosition];
                                    System.arraycopy(readBuffer, 0, encodedBytes, 0, encodedBytes.length);
                                    final String data = new String(encodedBytes, "US-ASCII");
                                    readBufferPosition = 0;

                                    handler.post(new Runnable() {
                                        public void run() {
                                            text1.setText(data);
                                        }
                                    });
                                } else {
                                    readBuffer[readBufferPosition++] = b;
                                }
                            }
                        }

                    } catch (IOException ex) {
                        stopWorker = true;
                    }
                }
            }
        });

        workerThread.start();
    }
}

