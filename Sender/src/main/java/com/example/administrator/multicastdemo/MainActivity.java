package com.example.administrator.multicastdemo;

import android.content.Context;
import android.net.wifi.WifiManager;
import android.os.StrictMode;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.text.SimpleDateFormat;

public class MainActivity extends AppCompatActivity {

    private final String TAG = "logan";
    private EditText etContent = null;

    private final String IP = "224.0.0.1";
    private final int PORT = 12345;

    private MulticastSocket socket;
    private InetAddress address;

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

        WifiManager wifiManager = (WifiManager)  getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        WifiManager.MulticastLock multicastLock = wifiManager.createMulticastLock("multicast.test");
        multicastLock.acquire();

        initMulticast();

        Button btnSend = findViewById(R.id.btn_send);
        etContent = findViewById(R.id.et_content);
        btnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendMulticast();
            }
        });
    }

    private void initMulticast() {
        try {
            socket = new MulticastSocket(PORT);
            address = InetAddress.getByName(IP);
            socket.setTimeToLive(4);
            socket.joinGroup(address);

        } catch (Exception e) {
            Log.i(TAG, "发送--连接失败");
        }

    }

    private void sendMulticast() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    while (true){
                        String content = etContent.getText().toString()+System.currentTimeMillis();
                        byte[] buf = content.getBytes();
                        DatagramPacket packet = new DatagramPacket(buf, buf.length);
                        packet.setAddress(address);
                        packet.setPort(PORT);
                        socket.send(packet);
                        Log.e(TAG, "run: 发送成功");
                        Thread.sleep(1000);
                    }

                } catch (Exception e) {
                    Log.e(TAG, "run: 发送失败");
                    e.printStackTrace();
                }
            }
        }).start();

    }
}
