package com.johnnymcfly.gps2sms;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;


public class MyReceiver extends BroadcastReceiver {

    public static final String EXTRA_MESSAGE = "com.johnnymcfly.gps2sms.MESSAGE";

    @Override
    public void onReceive(Context context, Intent intent) {

        Log.d("MonLog", "MyReceiver Triggered");
        String action = intent.getAction();
        Log.d("MonLog", "MyReceiver action: " + action);

        switch (action) {
            case MyService.NEW_LOCATION_RECEIVED:
                Log.d("MonLog","NEW_LOCATION_RECEIVED");
                Bundle extras = intent.getExtras();
                if (extras != null) {
                    Location loc = (Location) extras.get(LocationManager.KEY_LOCATION_CHANGED);
                    if (loc == null) {
                        Log.d("MonLog","NULL LOCATION - EXTRAS : " + extras);
                        // while GPS is ON : "Bundle[{satellites=0, status=1}]"
                        // while gps is disabled :"Bundle[{providerEnabled=false}]"
                    } else {
                        Log.d("MonLog","NON NULL LOCATION - EXTRAS : " + extras);

                        String currentTime = new SimpleDateFormat("HH:mm", Locale.getDefault()).format(new Date());
                        String smsString = currentTime +
                                 " ; http://maps.google.com/maps?q=" + String.valueOf(loc.getLatitude())
                                + "," + String.valueOf(loc.getLongitude());
                        Log.d("MonLog", smsString);

                        Intent intentSendSMS = new Intent(context, MyService.class);
                        intentSendSMS.setAction(MyService.ACTION_SEND_SMS);
                        intentSendSMS.putExtra(EXTRA_MESSAGE, smsString);
                        context.startService(intentSendSMS);

                        Intent intentSetAlarm = new Intent(context, MyService.class);
                        intentSetAlarm.setAction(MyService.ACTION_SET_ALARM);
                        context.startService(intentSetAlarm);
                    }
                }
                break;
            case MyService.SMS_SENT:
                Log.d("MonLog","SMS_SENT");
                break;

            case MyService.ALARM_RINGING:
                Log.d("MonLog","ALARM_RINGING");
                Intent intentStartLocationAcq = new Intent(context, MyService.class);
                intentStartLocationAcq.setAction(MyService.ACTION_START_LOCATION_ACQUISITION);
                context.startService(intentStartLocationAcq);
                break;

            case MyService.QUIT_CMD_RECEIVED:
                Intent intentQuit = new Intent(context, MyService.class);
                intentQuit.setAction(MyService.ACTION_QUIT);
                context.startService(intentQuit);
                break;
        }
    }
}
