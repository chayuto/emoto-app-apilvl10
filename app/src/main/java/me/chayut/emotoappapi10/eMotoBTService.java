package me.chayut.emotoappapi10;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Set;
import java.util.UUID;


/**
 * Created by chayut on 15/01/15.
 */
public class eMotoBTService {

    private final static int REQUEST_ENABLE_BT = 1;

    public final static byte[] PREAMBLE = {(byte)0xEC,(byte)0xDF};
    public final static byte PREAMBLE0 = (byte)0xEC;
    public final static byte PREAMBLE1 = (byte)0xDF;
    public final static byte GET_STATUS = (byte)0xA5;
    public final static byte RTS_IMAGE = (byte)0x4B;
    public final static byte ACK_IMAGE_INFO = (byte)0x6B;
    public final static byte ACK_IMAGE_DATA = (byte)0x4A;
    public final static byte NACK_RTS = (byte)0x9E;



    private final BluetoothAdapter mAdapter;

    private ConnectThread mConnectThread;
    private ConnectedThread mConnectedThread;
    private int BTServiceState = 0;
    private Context mContext;

    private byte[] mainIncomingBuffer = {0};

    public eMotoBTService(Context context) { //, Handler handler
        BTServiceState = 0;
        mAdapter = BluetoothAdapter.getDefaultAdapter();
        //mHandler = handler;
        mContext = context;
        if (getBTDevice()!= null) {
            mConnectThread = new ConnectThread(getBTDevice());
            mConnectThread.start();
        }
        else
        {
            Log.d("eMotoBT", "Device is not Paired");
        }
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
                if (device.getName().equalsIgnoreCase("HC-06")) {
                    mDevice = device; //if the device is the BT dongle, TODO: selection
                }
            }
        }
        return mDevice;
    }

    public int getServiceState (){
        return BTServiceState;
    }

    public void sendBytes (byte[] bytes){
        if(BTServiceState ==1) {
            mConnectedThread.write(bytes);
        }
        else
        {
            Log.d("eMotoBT","BT is not in ready state");
        }
    }

    public boolean sendEMotoPacket(byte command,int transaction, byte[] payload)
    {
        boolean success = false;

        if (transaction > 255){ return success;}

        byte[] header = new byte[8 + payload.length];
        header[0] = PREAMBLE0;
        header[1] = PREAMBLE1;
        header[2] = (byte) transaction;
        header[3] = command;
        byte[] bytes = ByteBuffer.allocate(2).putInt(payload.length).array();
        Log.d("Debug",String.format("%x %x", bytes[0] , bytes[1]));
        header[4] = bytes[0];
        header[5] = bytes[1];
        header[6] = (byte) 0x55; //TODO: make CRC function
        header[7] = (byte) 0xcc; //TODO: make CRC function

        System.arraycopy(payload,0,header,8,payload.length); //copy payload into out buffer

        sendBytes(header);

        success = true; //TODO: change response of the function

        return success;
    }



    private void processIncomingBytes (byte[] incomingBytes){
        byte[] newMainBuffer = new byte[mainIncomingBuffer.length + incomingBytes.length];
        System.arraycopy(mainIncomingBuffer, 0, newMainBuffer, 0, mainIncomingBuffer.length);
        System.arraycopy(incomingBytes, 0, newMainBuffer, mainIncomingBuffer.length, incomingBytes.length);

        mainIncomingBuffer = newMainBuffer;

        Log.d("BT Service","MainBuffer:" + new String(mainIncomingBuffer, 0, mainIncomingBuffer.length));

        for (int i =0 ; i<= (mainIncomingBuffer.length - 8); i++) {
            byte test = mainIncomingBuffer[i];

            if (test == PREAMBLE0) {
                if (mainIncomingBuffer[i + 1] == PREAMBLE1) {
                    Log.d("BT Service", "Detect incoming PreAmble");

                    byte transactionID = mainIncomingBuffer[i + 2];
                    byte command = mainIncomingBuffer[i + 3];
                    byte contentSize0 = mainIncomingBuffer[i + 4];
                    byte contentSize1  = mainIncomingBuffer[i + 5];
                    byte contentCRC  = mainIncomingBuffer[i + 6];
                    byte headerCRC  = mainIncomingBuffer[i + 7];
                    byte[] contentSizeArray = {contentSize0,contentSize1};
                    int iContentSize = (int)ByteBuffer.wrap(contentSizeArray).getShort();

                    int iMessageLength = 8 ;
                    int iNewRemainingMainBufferLength =  mainIncomingBuffer.length - iMessageLength - i ;
                    byte[] newRemainingMainBuffer = new byte[iNewRemainingMainBufferLength];
                    byte[] messageBytes = new byte[iMessageLength];

                    //Extract message from main Buffer
                    System.arraycopy(mainIncomingBuffer,iMessageLength + i,newRemainingMainBuffer,0,iNewRemainingMainBufferLength);
                    System.arraycopy(mainIncomingBuffer,i,messageBytes,0,iMessageLength);

                    Log.d("BT Service","messageBytes:" + new String(messageBytes, 0, messageBytes.length));


                    mainIncomingBuffer = newRemainingMainBuffer; //update buffer after dectect Preamble
                    i = 0; //reset counter

                    // break; //break loop
                }
            }
        }


    }


   //region Threads

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
                    if(availableBytes > 7) {
                        // Read from the InputStream
                        bytes = mmInStream.read(buffer);
                        // Send the obtained bytes to the UI activity
                        Log.d("BT Service", "IncomingData:" + new String(buffer, 0, bytes));

                        mmOutStream.write(PREAMBLE);
                        processIncomingBytes(Arrays.copyOfRange(buffer,0,bytes));
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

    //endregion

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
