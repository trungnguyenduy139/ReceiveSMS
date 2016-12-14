package com.example.trungnguyen.receivesms;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Switch;

import java.util.ArrayList;
import java.util.concurrent.locks.ReentrantLock;

public class MainActivity extends AppCompatActivity {
    private ReentrantLock reentrantLock;
    private Switch swAutoResponse;
    private LinearLayout llButtons;
    private Button btnSafe, btnMayday;
    private ArrayList<String> requesters;
    private ArrayAdapter<String> adapter;
    private ListView lvMessages;
    private BroadcastReceiver broadcastReceiver;
    public static boolean isRunning;
    private final String AUTO_RESPONSE = "auto_response";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        addControls();
        HandleEvents();
        addVariables();
    }

    private void addVariables() {
        requesters = new ArrayList<>();
        reentrantLock = new ReentrantLock();
        adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, requesters);
        lvMessages.setAdapter(adapter);
    }

    @Override
    protected void onResume() {
        super.onResume();
        isRunning = true;
        if (broadcastReceiver == null) initBroadcastReceiver();
        IntentFilter intentFilter = new IntentFilter(SmsBroadcastReceiver.SMS_FORWARD_BROADCAST_RECEIVER);
        registerReceiver(broadcastReceiver, intentFilter);
        SharedPreferences preferences = getSharedPreferences(AUTO_RESPONSE, MODE_PRIVATE);
        boolean isChecked = preferences.getBoolean("SWITCH_IS_CHECKED", false);
        swAutoResponse.setChecked(isChecked);
        if (isChecked)
            llButtons.setVisibility(View.GONE);

        initBroadcastReceiver();
    }

    private void addControls() {
        swAutoResponse = (Switch) findViewById(R.id.sw_auto_response);
        llButtons = (LinearLayout) findViewById(R.id.ll_buttons);
        lvMessages = (ListView) findViewById(R.id.lv_messages);
        btnSafe = (Button) findViewById(R.id.btn_safe);
        btnMayday = (Button) findViewById(R.id.btn_mayday);
    }

    private void respond(String toContact, String smsContent) {
        reentrantLock.lock();
        requesters.remove(toContact);
        adapter.notifyDataSetChanged();
        reentrantLock.unlock();
        // TODO: Send SMS
        SmsManager smsManager = SmsManager.getDefault();
        smsManager.sendTextMessage(toContact, null, smsContent, null, null);
    }

    private void respondAll(boolean isOK) {
        String okString = getResources().getString(R.string.i_am_safe_and_well_worry_not);
        String notOkString = getResources().getString(R.string.tell_my_mother_i_love_her);
        String outputString = isOK ? okString : notOkString;
        ArrayList<String> requesterCopy = (ArrayList<String>) requesters.clone();
        for (String to : requesterCopy) {
            respond(to, outputString);
        }
    }

    public void processReceiveListAddress(ArrayList<String> listAddress) {
        for (int i = 0; i < listAddress.size(); i++) {
            if (!requesters.contains(listAddress.get(i))) {
                reentrantLock.lock();
                requesters.add(listAddress.get(i));
                adapter.notifyDataSetChanged();
                reentrantLock.unlock();
            }
        }
        if (swAutoResponse.isChecked())
            respondAll(true); // true if you SAFE and false if not
    }

    public void HandleEvents() {
        btnSafe.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                respondAll(true);
            }
        });

        btnMayday.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                respondAll(false);
            }
        });

        swAutoResponse.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                if (isChecked) {
                    llButtons.setVisibility(View.GONE);
                } else {
                    llButtons.setVisibility(View.VISIBLE);
                }
            }
        });
    }

    private void initBroadcastReceiver() {
        broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                ArrayList<String> list =
                        intent.getStringArrayListExtra(SmsBroadcastReceiver.LIST_ADDRESS);

                processReceiveListAddress(list);
            }
        };
    }

    @Override
    protected void onStop() {
        super.onStop();
        super.onStop();
        isRunning = false;

        // UnregisterReceiver
        unregisterReceiver(broadcastReceiver);
        SharedPreferences preferences = getSharedPreferences(AUTO_RESPONSE, MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean("SWITCH_IS_CHECKED", swAutoResponse.isChecked());
        editor.commit();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
