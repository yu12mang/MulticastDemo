package com.example.receiver;

import android.content.Context;
import android.net.wifi.WifiManager;
import android.os.StrictMode;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;

public class MainActivity extends AppCompatActivity {
    private final String TAG = "logan";
    private final String IP = "224.0.0.1";
    private final int PORT = 12345;

    private MulticastSocket socket;
    private InetAddress address;

    private TextView tvResult = null;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder()
                .detectDiskReads().detectDiskWrites().detectNetwork()
                .penaltyLog().build());
        StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder()
                .detectLeakedSqlLiteObjects().penaltyLog().penaltyDeath()
                .build());


        WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        WifiManager.MulticastLock multicastLock = wifiManager.createMulticastLock("multicast.test");
        multicastLock.acquire();


        tvResult = findViewById(R.id.tv_receive);

        joinMulticast();
        receiveMsg();
    }

    private void joinMulticast() {
        try{
            socket = new MulticastSocket(PORT);
            address = InetAddress.getByName(IP);
            socket.joinGroup(address);
        }catch(Exception e){
            Log.e(TAG, "joinMulticast: 加入失败"+e.getMessage());
        }

    }

    private void receiveMsg(){
        new Thread(new Runnable() {
            @Override
            public void run() {
                try{

                    byte[] buf  = new byte[1024];
                    while (true){
                        DatagramPacket packet = new DatagramPacket(buf,buf.length);
                        socket.receive(packet);
                        Log.i(TAG, "run: "+packet.getAddress());
                        final String result = new String (packet.getData()).trim();
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                tvResult.setText(result);
                            }
                        });
                    }

                }catch(Exception e){
                    Log.e(TAG, "run: "+e.getMessage() );
                }
            }
        }).start();
    }
}
