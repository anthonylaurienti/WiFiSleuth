package com.example.wifisleuth;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.text.Editable;
import android.text.Html;
import android.text.TextWatcher;
import android.text.method.KeyListener;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.util.List;

import static com.example.wifisleuth.R.*;

public class MainActivity extends AppCompatActivity {
    String TAG = "WiFi_SLEUTH";
    WifiManager wifiManager;
    TextView textView;
    TextView textView2;
    EditText editText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(layout.activity_main);
        //permissions
        checkPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_BACKGROUND_LOCATION},1);
        //UI init
        Button b = findViewById(id.button);
        b.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                scanWiFi();
            }
        });
        b.setVisibility(View.GONE);
        textView = findViewById(id.textView);
        textView2 = findViewById(id.textView2);
        editText = findViewById(id.editText);
        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                scanFailure();
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });


        wifiManager = (WifiManager)getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        /*(new Thread() {
            public void run() {
                scanWiFi();
                try{
                    Thread.sleep(1000);
                }catch(Error | InterruptedException e){
                    //do nothing
                    Log.e(TAG,"",e);
                }
            }
        }).start();*/
        setupBroadcastReceiver();

    }

    public void scanWiFi(){
        Log.d(TAG,"Scanning Wifi!");
        boolean success = wifiManager.startScan();
        if(success){
            Log.d(TAG,"scan successful");
            scanSuccess();
        }else{
            Log.d(TAG,"scan failure");
            scanFailure();
        }
    }

    public void setupBroadcastReceiver(){
        BroadcastReceiver wifiScanReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                boolean success = intent.getBooleanExtra(WifiManager.EXTRA_RESULTS_UPDATED,false);

                if(success){
                    scanSuccess();
                }else{
                    scanFailure();
                }
            }
        };

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
        getApplicationContext().registerReceiver(wifiScanReceiver, intentFilter);
        Log.d(TAG,"scan starting");
        boolean success = wifiManager.startScan();
        if(!success){
            scanFailure();
        }
    }

    public void scanSuccess(){
        List<ScanResult> results = wifiManager.getScanResults();
        populateText(results);
        wifiManager.startScan();
    }

    private void scanFailure() {
        // handle failure: new scan did NOT succeed
        // consider using old scan results: these are the OLD results!
        List<ScanResult> results = wifiManager.getScanResults();
        populateText(results);
    }

    private void populateText(List<ScanResult> results){
        textView.setText("");
        textView2.setText("");
        for(ScanResult result:results){
            if(!result.SSID.equals("")) {

                Log.d(TAG, "Old SSID " + result.SSID + " rssi " + result.level);
                String search = editText.getText().toString();
                if(search.equals("")|!result.SSID.toLowerCase().contains(search.toLowerCase())) {
                    textView.setText(new StringBuilder().append(textView.getText()).append("SSID ").append(result.SSID).append(" RSSI ").append(result.level).append("\n").toString());
                } else {
                    textView2.setText(new StringBuilder().append(textView2.getText()).append("SSID ").append(result.SSID).append(" RSSI ").append(result.level).append("\n").toString()); //populate this textview if ssid matches the one we're searching for.
                }
            }
        }
    }

    private void checkPermissions(String[] permission, int requestCode){
        if(ContextCompat.checkSelfPermission(this, permission[0])//doesn't really work if we're checking for more than one permission.
                != PackageManager.PERMISSION_GRANTED)
        {
            // Permission is not granted
            ActivityCompat.requestPermissions(MainActivity.this, permission, requestCode);
        }
    }
}