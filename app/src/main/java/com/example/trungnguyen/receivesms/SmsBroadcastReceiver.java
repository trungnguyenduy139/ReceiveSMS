package com.example.trungnguyen.receivesms;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Message;
import android.telephony.SmsMessage;
import android.util.Log;

import java.util.ArrayList;

/**
 * Created by Trung Nguyen on 12/14/2016.
 */
public class SmsBroadcastReceiver extends BroadcastReceiver {
    //    public static final String queryString = "Are You OK".toLowerCase();
    public static final String SMS_FORWARD_BROADCAST_RECEIVER = "sms_forward";
    public static final String LIST_ADDRESS = "list_address";
    private static final String TAG = SmsBroadcastReceiver.class.getSimpleName();
    ArrayList<String> listAddress;

    @Override
    public void onReceive(Context context, Intent intent) {
        String queryString = "Are You OK".toLowerCase();
        Log.d(TAG, "Receive SmsBroadcast");
        Bundle bundle = intent.getExtras();
        if (bundle != null) {
            listAddress = new ArrayList<String>();
            try {
                Object[] pdus = (Object[]) bundle.get("pdus");
                SmsMessage[] msgs = new SmsMessage[pdus.length];
                for (int i = 0; i < msgs.length; i++) {
                    if (Build.VERSION.SDK_INT >= 23)
                        msgs[i] = SmsMessage.createFromPdu((byte[]) pdus[i], "");
                    else msgs[i] = SmsMessage.createFromPdu((byte[]) pdus[i]);
                }
                for (SmsMessage message : msgs) {
                    if (message.getMessageBody().toLowerCase().contains(queryString))
                        listAddress.add(message.getOriginatingAddress());
                }
                Log.d(TAG, listAddress.size() + "");
                if (listAddress.size() > 0) {
                    if (!MainActivity.isRunning) {
                        Intent iStart = new Intent(context, MainActivity.class);
                        iStart.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        iStart.putStringArrayListExtra(LIST_ADDRESS, listAddress);
                        iStart.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        context.startActivity(iStart);
                    } else {
                        Intent iForward = new Intent(SMS_FORWARD_BROADCAST_RECEIVER);
                        iForward.putStringArrayListExtra(LIST_ADDRESS, listAddress);
                        context.sendBroadcast(iForward);
                        Log.d(TAG, "Send Broadcast");
                    }
                }
            } catch (Exception e) {
                Log.d("Exception caught", e.getMessage());
            }
        }
    }
}
