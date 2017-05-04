package com.example.rebecca.canes;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.ParcelUuid;
import android.util.Log;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;

import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.ArrayList;
import java.util.Locale;
import java.net.HttpURLConnection;
import java.net.URL;

import org.json.JSONObject;
import org.json.JSONException;


public class MainActivity extends AppCompatActivity {

    BluetoothDevice mDevice = null;
    BluetoothAdapter mBluetoothAdapter;
    TextToSpeech t1;
    TextToSpeech t2;
    final byte delimiter = 33;
    int readBufferPosition = 0;
    private TextView txtSpeechInput;
    //private ImageButton btnSpeak;
    private final int REQ_CODE_SPEECH_INPUT = 100;
    int greetFlag = 0;
    ArrayList<String> deviceAddresses = new ArrayList<String>();
    ArrayList<String> deviceNames = new ArrayList<String>();


    Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            byte[] writeBuf = (byte[]) msg.obj;
            int begin = (int)msg.arg1;
            int end = (int)msg.arg2;

            switch(msg.what) {
                case 1:
                    String writeMessage = new String(writeBuf);
                    writeMessage = writeMessage.substring(begin, end);
                    break;
            }
        }
    };

    public static JSONObject getJSONObjectFromURL(String urlString) throws IOException, JSONException {

        HttpURLConnection urlConnection = null;

        URL url = new URL(urlString);

        urlConnection = (HttpURLConnection) url.openConnection();

        urlConnection.setRequestMethod("GET");
        urlConnection.setReadTimeout(10000 /* milliseconds */);
        urlConnection.setConnectTimeout(15000 /* milliseconds */);

        urlConnection.setDoOutput(true);

        urlConnection.connect();

        BufferedReader br=new BufferedReader(new InputStreamReader(url.openStream()));

        char[] buffer = new char[1024];

        String jsonString = new String();

        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = br.readLine()) != null) {
            sb.append(line+"\n");
        }
        br.close();

        jsonString = sb.toString();

        System.out.println("JSON: " + jsonString);

        return new JSONObject(jsonString);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //check if device supports bluetooth
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBluetoothAdapter == null) {
            Log.d("bluetooth", "bluetooth is not supported");
        }

        //check if bluetooth is enabled
        if (!mBluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, 1);
        }

        //retrieve paired bluetooth devices
        Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();
        if (pairedDevices.size() > 0) {
            for (BluetoothDevice device : pairedDevices) {
                deviceAddresses.add(device.getAddress());
                deviceNames.add(device.getName());
                if (device.getName().equals("raspberrypi"))
                {
                    mDevice = device;
                    ParcelUuid[] supportedUuids = mDevice.getUuids();
                    Log.e("bluetooth", "found device");
                }
            }
        }

        for (String object : deviceAddresses) {
            Log.v("bluetooth", "Address= " + object);
        }

        for (String nobject : deviceNames){
            Log.v("bluetooth", "DeviceName= " + nobject);
        }

        //use thread class to connect the raspberrypi bluetooth module to android
        ConnectThread mConnectThread = new ConnectThread(mDevice);
        mConnectThread.run();

        txtSpeechInput = (TextView) findViewById(R.id.txtSpeechInput);
//        btnSpeak = (ImageButton) findViewById(R.id.btnSpeak);


//BEFORE PROMPT SPEECH INPUT GIVE A GREETING TO USER
        t1=new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if(status != TextToSpeech.ERROR) {
                    t1.setLanguage(Locale.US);
                }
                String toSpeak = "Hello.  How can I help?";
               // Toast.makeText(getApplicationContext(), toSpeak,Toast.LENGTH_SHORT).show();
                t1.speak(toSpeak, TextToSpeech.QUEUE_FLUSH, null);
            }
        });
    }

    @Override
    protected void onStart(){
        super.onStart();
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                // this code will be executed after 3 seconds
                promptSpeechInput();
            }
        }, 3000);

        //promptSpeechInput();
    }

    //Showing google speech input dialog
    private void promptSpeechInput() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT,
                getString(R.string.speech_prompt));
        try {
            startActivityForResult(intent, REQ_CODE_SPEECH_INPUT);
        } catch (ActivityNotFoundException a) {
            Toast.makeText(getApplicationContext(),
                    getString(R.string.speech_not_supported),
                    Toast.LENGTH_SHORT).show();
        }
    }

    // Get speech input
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        ArrayList<String> result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                    txtSpeechInput.setText(result.get(0));
                    for (String info : result) {
                        Log.d("user voice command ", info);
                        if (info == "navigate")
                        {
                            Log.d("navigate", "inside navigate if statemnt ");
                            //this block should listen on Bluetooth channel for
                            //incoming strings from rasberrypi that contain the
                            //RFID value for tag read - then we convert the string
                            //from text to speech to output the
                        }
                        if (info == "map")
                        {
                            Log.d("map", "inside map if statemnt ");
                            //this block will be to start communication about which map
                            //t2=new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
                            //    @Override
                            //public void onInit(int status) {
                            //        if(status != TextToSpeech.ERROR) {
                            //            t2.setLanguage(Locale.US);
                            //        }
                            //        String toSpeak = "Which map would you like?";
                                    // Toast.makeText(getApplicationContext(), toSpeak,Toast.LENGTH_SHORT).show();
                            //        t2.speak(toSpeak, TextToSpeech.QUEUE_FLUSH, null);
                            //  }

                            //this block makes request to webserver for tagids based on locale
                            try{
                                JSONObject jsonObject = getJSONObjectFromURL(Constants.urlString);

                                // Parse json here

                            } catch (IOException e) {
                                e.printStackTrace();
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }

    //Thread class to connect bluetooth devices (rasberrypi and Android)
    private class ConnectThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final BluetoothDevice mmDevice;

        public ConnectThread(BluetoothDevice device) {
            BluetoothSocket tmp = null;
            mmDevice = device;
            try {
                tmp = device.createRfcommSocketToServiceRecord(Constants.MY_UUID);
            } catch (IOException e) {
                Log.e(Constants.TAG, "socket's listen() method failed", e);
            }
            mmSocket = tmp;
        }
        public void run() {
           //mBluetoothAdapter.cancelDiscovery();
           //line above says I am missing Permission BLUETOOTH_ADMIN but it is in manifest.xml
            try {
                mmSocket.connect();
            } catch (IOException e) {
                Log.e(Constants.TAG, "socket's connect() method failed", e);
                try {
                    mmSocket.close();
                } catch (IOException closeException) {}
                return;
            }
            ConnectedThread mConnectedThread = new ConnectedThread(mmSocket);
            mConnectedThread.run();
            Log.d(Constants.TAG, "started thread for input/output stream");
        }


        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) { }
        }
    }

    //Additional Thread Class for handling input/output stream
    private class ConnectedThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final InputStream mmInStream;
        private final OutputStream mmOutStream;
        public String rfid = null;
        public ConnectedThread(BluetoothSocket socket) {
            mmSocket = socket;
            InputStream tmpIn = null;
            OutputStream tmpOut = null;
            try {
                tmpIn = socket.getInputStream();
                tmpOut = socket.getOutputStream();
            } catch (IOException e) { }
            mmInStream = tmpIn;
            mmOutStream = tmpOut;
        }
        public void run() {
            byte[] buffer = new byte[1024];

            int begin = 0;
            int bytes = 0;
            while (true) {
                try {
                    bytes += mmInStream.read(buffer, bytes, buffer.length - bytes);
                    for(int i = begin; i < bytes; i++) {
                        //if(buffer[i] == "#".getBytes()[0]) {
                        if(buffer[i] == "&".getBytes()[0]) {
                           // mHandler.obtainMessage(1, begin, i, buffer).sendToTarget();

                            begin = i + 1;
                            if(i == bytes - 1) {
                                bytes = 0;
                                begin = 0;
                            }
                            rfid = new String(buffer, "UTF-8");
                            //rfid = "After read tag.";
                            Log.d(Constants.TAG, rfid);
                        }
                    }
                    //attempt to output the received string value as text to speech
                    //None of the TextToSpeech works until diconnect raspberry pi
                    t2=new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
                        @Override
                        public void onInit(int status) {
                            if(status != TextToSpeech.ERROR) {
                                t2.setLanguage(Locale.US);
                            }
                            String toSpeak = rfid;
                            // Toast.makeText(getApplicationContext(), toSpeak,Toast.LENGTH_SHORT).show();
                            t2.speak(toSpeak, TextToSpeech.QUEUE_FLUSH, null);
                        }
                    });
                } catch (IOException e) {
                    break;
                }
            }
        }
        public void write(byte[] bytes) {
            try {
                mmOutStream.write(bytes);
            } catch (IOException e) { }
        }
        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) { }
        }
    }
} //End of Main Activity
