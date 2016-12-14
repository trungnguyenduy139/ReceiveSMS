package com.example.trungnguyen.receivesms;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.SmsMessage;
import android.util.Log;

import java.util.ArrayList;

/**
 * Created by Trung Nguyen on 12/14/2016.
 */
public class SmsBroadcastReceiver extends BroadcastReceiver {
    public static final String queryString = "Are You OK".toLowerCase();
    public static final String SMS_FORWARD_BROADCAST_RECEIVER = "sms_forward";
    public static final String LIST_ADDRESS = "list_address";
    ArrayList<String> listAddress;

    @Override
    public void onReceive(Context context, Intent intent) {
        Bundle bundle = intent.getExtras();
        SmsMessage[] msgs;
        if (bundle != null) {
            listAddress = new ArrayList<String>();
            try {
                Object[] pdus = (Object[]) bundle.get("pdus");
                msgs = new SmsMessage[pdus.length];
                for (int i = 0; i < msgs.length; i++) {
                    msgs[i] = SmsMessage.createFromPdu((byte[]) pdus[i]);
                    if (msgs[i].getMessageBody().contains(queryString)) {
                        listAddress.add(msgs[i].getOriginatingAddress());
                    }
                }
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
                    }
                }
            } catch (Exception e) {
                Log.d("Exception caught", e.getMessage());
            }
        }
    }
}
