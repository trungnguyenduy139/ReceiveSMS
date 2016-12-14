package com.example.trungnguyen.receivesms;

import android.app.Activity;
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

    @Override
    public void onReceive(Context context, Intent intent) {
        ArrayList<String> listAddress;
        Bundle bundle = intent.getExtras();
        SmsMessage[] msgs;
        if (bundle != null) {
            listAddress = new ArrayList<>();
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

                    } else {
                        Intent mForward = new Intent(SMS_FORWARD_BROADCAST_RECEIVER);
                        mForward.putStringArrayListExtra(this.LIST_ADDRESS, listAddress);
                        context.sendBroadcast(mForward);
                    }
                }
            } catch (Exception e) {
                Log.d("Exception caught", e.getMessage());
            }
        }
    }
}
