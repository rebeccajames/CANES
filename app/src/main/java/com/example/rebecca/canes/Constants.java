package com.example.rebecca.canes;

/**
 * Created by Rebecca on 5/2/2017.
 */

import java.util.UUID;

public interface Constants {

    String TAG = "Bluetooth";
    int REQUEST_ENABLE_BT = 1;

    // message types sent from the BluetoothChatService Handler
    int MESSAGE_STATE_CHANGE = 1;
    int MESSAGE_READ = 2;
    int MESSAGE_WRITE = 3;
    int MESSAGE_SNACKBAR = 4;

    // Constants that indicate the current connection state
    int STATE_NONE = 0;       // we're doing nothing
    int STATE_ERROR = 1;
    int STATE_CONNECTING = 2; // now initiating an outgoing connection
    int STATE_CONNECTED = 3;  // now connected to a remote device


    UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805f9b34fb");
    String urlString = "www.rebeccajamesbeebe.com";

    // Key names received from the BluetoothChatService Handler
    String EXTRA_DEVICE  = "EXTRA_DEVICE";
    String SNACKBAR = "toast";


}
