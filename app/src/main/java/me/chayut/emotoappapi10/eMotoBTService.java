package me.chayut.emotoappapi10;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Handler;

import static android.support.v4.app.ActivityCompat.startActivityForResult;

/**
 * Created by chayut on 15/01/15.
 */
public class eMotoBTService {

    private final static int REQUEST_ENABLE_BT = 1;

    public final static int[] PREAMBLE = {0xEC,0xDF};
    public final static int GET_STATUS = 0xA5;
    public final static int RTS_IMAGE = 0x4B;
    public final static int ACK_IMAGE_INFO = 0x6B;
    public final static int ACK_IMAGE_DATA = 0x4A;
    public final static int NACK_RTS = 0x9E;



    private final BluetoothAdapter mAdapter;
    //private final Handler mHandler;
    private ConnectThread mConnectThread;
    private ConnectedThread mConnectedThread;
    private int BTServiceState;
    private Context mContext;

    public eMotoBTService(Context context) { //, Handler handler
        BTServiceState = 0;
        mAdapter = BluetoothAdapter.getDefaultAdapter();
        //mHandler = handler;
        mContext = context;
        mConnectThread = new ConnectThread(getBTDevice());
        mConnectThread.start();
    }

    public static boolean initiateBT (){

        BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBluetoothAdapter == null) {
            // Device does not support Bluetooth
            Log.d("eMotoBT", "Device does not support Bluetooth");
            return false;
        }

        if (!mBluetoothAdapter.isEnabled()) {
            Log.d("eMotoBT", "Bluetooth is not enabled");
            return false;
        }
        return true;
    }

    public static BluetoothDevice getBTDevice (){

        BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();
        // If there are paired devices

        BluetoothDevice mDevice = null;
        if (pairedDevices.size() > 0) {
            // Loop through paired devices
            for (BluetoothDevice device : pairedDevices) {
                // Add the name and address to an array adapter to show in a ListView
                Log.d("eMotoBT","Paired: " + device.getName() + " : " + device.getAddress());
                mDevice = device;
            }
        }
        return mDevice;
    }


    public void sendBytes (byte[] bytes){
        mConnectedThread.write(bytes);
    }


    public int getServiceState (){
        return BTServiceState;
    }


    private class ConnectThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final BluetoothDevice mmDevice;

        public ConnectThread(BluetoothDevice device) {
            // Use a temporary object that is later assigned to mmSocket,
            // because mmSocket is final
            BluetoothSocket tmp = null;
            mmDevice = device;

            // Get a BluetoothSocket to connect with the given BluetoothDevice
            try {
                // MY_UUID is the app's UUID string, also used by the server code
                tmp = device.createRfcommSocketToServiceRecord(UUID.fromString("00001101-0000-1000-8000-00805F9B34FB"));
            } catch (IOException e) {
                Log.d("BT","EX:" +e.getMessage());
            }
            mmSocket = tmp;
        }

        public void run() {
            // Cancel discovery because it will slow down the connection
            //mBluetoothAdapter.cancelDiscovery();
            Log.d("BT","ConnectThread Run");
            try {
                // Connect the device through the socket. This will block
                // until it succeeds or throws an exception
                mmSocket.connect();
            } catch (IOException connectException) {
                // Unable to connect; close the socket and get out
                Log.d("BT","EX:" +connectException.getMessage());

                try {
                    mmSocket.close();
                } catch (IOException closeException) {
                    Log.d("BT","EX:" +closeException.getMessage());
                }
                return;
            }

            mConnectedThread = new ConnectedThread(mmSocket);
            mConnectedThread.run();
            // Do work to manage the connection (in a separate thread)
            //manageConnectedSocket(mmSocket);
        }

        /** Will cancel an in-progress connection, and close the socket */
        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) { }
        }
    }

    private class ConnectedThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final InputStream mmInStream;
        private final OutputStream mmOutStream;

        public ConnectedThread(BluetoothSocket socket) {
            mmSocket = socket;
            InputStream tmpIn = null;
            OutputStream tmpOut = null;

            // Get the input and output streams, using temp objects because
            // member streams are final
            try {
                tmpIn = socket.getInputStream();
                tmpOut = socket.getOutputStream();
            } catch (IOException e) { }

            mmInStream = tmpIn;
            mmOutStream = tmpOut;
        }

        public void run() {

            Log.d("BT","ConnectedThread Run");
            BTServiceState = 1;

            //notify UI thread.
            sendToasttoUI("Coonnection Successful");


            //
            byte[] buffer = new byte[1024];  // buffer store for the stream
            int bytes; // bytes returned from read()

            // Keep listening to the InputStream until an exception occurs
            while (true) {
                try {
                    int availableBytes = mmInStream.available();
                    if(availableBytes > 8) {
                        // Read from the InputStream
                        bytes = mmInStream.read(buffer);
                        // Send the obtained bytes to the UI activity
                        Log.d("BT", "Data:" + new String(buffer, 0, bytes));

                        mmOutStream.write((byte) 0x66);
                    }
                } catch (IOException e) {
                    break;
                }
            }
        }

        /* Call this from the main activity to send data to the remote device */
        public void write(byte[] bytes) {
            try {
                mmOutStream.write(bytes);
            } catch (IOException e) { }
        }

        /* Call this from the main activity to shutdown the connection */
        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) { }
        }
    }

    void sendToasttoUI (String message)
    {
        final Activity activity = (Activity) mContext;
        activity.runOnUiThread(new Runnable() {
            public void run() {
                Toast.makeText(activity, "Connected!", Toast.LENGTH_SHORT).show();
            }
        });
    }

}
