package com.johnnymcfly.gps2sms;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.location.LocationManager;
import android.os.IBinder;
import android.telephony.SmsManager;
import android.util.Log;
import android.widget.Toast;

import androidx.core.app.NotificationCompat;

public class MyService extends Service {

    public static final String ACTION_START_FOREGROUND_SERVICE = "ACTION_START_FOREGROUND_SERVICE";
    public static final String ACTION_SEND_SMS = "ACTION_SEND_SMS";
    public static final String ACTION_SET_ALARM = "ACTION_SET_ALARM";
    public static final String ACTION_START_LOCATION_ACQUISITION = "ACTION_START_LOCATION_ACQUISITION";
    public static final String ACTION_QUIT = "ACTION_QUIT";

    public static final String NEW_LOCATION_RECEIVED = "NEW_LOCATION_RECEIVED";
    public static final String SMS_SENT = "SMS_SENT";
    public static final String ALARM_RINGING = "ALARM_RINGING";
    public static final String QUIT_CMD_RECEIVED = "QUIT_CMD_RECEIVED";

    public static final int ALRM_P_INTENT_ID = 101;
    public static final int LOC_P_INTENT_ID = 102;
    public static final int NOTIF_P_INTENT_ID = 103;

    public MyService() {
    }


    @Override
    public IBinder onBind(Intent intent) {
            throw new UnsupportedOperationException("Not yet implemented");
    }


    @Override
    public void onCreate() {
        super.onCreate();
        Log.d("MonLog", "Service onCreate()");
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
        MyApplication application=(MyApplication)getApplication();

        if(intent != null)
        {
            String action = intent.getAction();
            Log.d("MonLog", "Service onStartCommand()");
            switch (action)
            {
                case ACTION_START_FOREGROUND_SERVICE:
                    Log.d("MonLog", "Case ACTION_START_FOREGROUND_SERVICE");
                    startForegroundService();
                    Toast.makeText(getApplicationContext(), "Tracking started", Toast.LENGTH_LONG).show();
                    application.trackingStarted = true;
                    launchLocationAcquisition();
                    break;

                case ACTION_SEND_SMS:
                    Log.d("MonLog", "Case ACTION_SEND_SMS");
                    String message = intent.getStringExtra(MyReceiver.EXTRA_MESSAGE);
                    Log.d("MonLog", "Envoi à " + application.SMSphoneNumber +" du SMS " + message);
                    SmsManager smsManager = SmsManager.getDefault();
                    Intent intentSMSSent = new Intent(this, MyReceiver.class);
                    intentSMSSent.setAction(MyService.SMS_SENT);
                    PendingIntent pendingIntentSMSSent = PendingIntent.getBroadcast(this, 3, intentSMSSent, 0);
                    smsManager.sendTextMessage(application.SMSphoneNumber, null, message, pendingIntentSMSSent, null);
                    break;

                case ACTION_SET_ALARM:
                    Log.d("MonLog", "Case ACTION_SET_ALARM");
                    Intent alarmMngIntent = new Intent(this, MyReceiver.class);
                    alarmMngIntent.setAction(MyService.ALARM_RINGING);
                    PendingIntent pendingIntentAlarmMng = PendingIntent.getBroadcast(this, ALRM_P_INTENT_ID, alarmMngIntent, 0);
                    Log.d("MonLog", "Alarme réglée pour " + String.valueOf(System.currentTimeMillis() + 900000));
                    alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + 900000, pendingIntentAlarmMng);
                    break;

                case ACTION_START_LOCATION_ACQUISITION:
                    Log.d("MonLog", "Case ACTION_START_LOCATION_ACQUISITION");
                    launchLocationAcquisition();
                    break;

                case ACTION_QUIT:
                    Log.d("MonLog", "Case ACTION_QUIT");

                    stopForegroundService();
                    break;
            }
        }
        return super.onStartCommand(intent, flags, startId);
    }


    @Override
    public void onDestroy() {
        super.onDestroy();

        AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
        MyApplication application=(MyApplication)getApplication();
        Log.d("MonLog", "Service onDestroy()");

        Intent alarmMngCncelIntent = new Intent(this, MyReceiver.class);
        alarmMngCncelIntent.setAction(MyService.ALARM_RINGING);
        PendingIntent pendingIntentAlarmMngCncel = PendingIntent.getBroadcast(this,ALRM_P_INTENT_ID,alarmMngCncelIntent,PendingIntent.FLAG_CANCEL_CURRENT);
        alarmManager.cancel(pendingIntentAlarmMngCncel);

        LocationManager locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        Intent intentLoc = new Intent(this, MyReceiver.class);
        intentLoc.setAction(MyService.NEW_LOCATION_RECEIVED);
        PendingIntent pendingIntentLoc = PendingIntent.getBroadcast(this, LOC_P_INTENT_ID, intentLoc, 0);
        locationManager.removeUpdates(pendingIntentLoc);

        sendBroadcast(new Intent(MainActivity.FINISH_MAIN_ACT_MSG));

        Toast.makeText(getApplicationContext(), "Tracking stopped", Toast.LENGTH_LONG).show();
        application.trackingStarted = false;
    }


    private void startForegroundService() {
        Log.d("MonLog", "Appel méthode Start foreground service");


        String NOTIFICATION_CHANNEL_ID = "com.johnnymcfly.gps2sms";
        String channelName = "GPS2SMS Background Service";
        NotificationChannel chan = new NotificationChannel(NOTIFICATION_CHANNEL_ID, channelName, NotificationManager.IMPORTANCE_NONE);
        chan.setLightColor(Color.BLUE);
        chan.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);
        NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        assert manager != null;
        manager.createNotificationChannel(chan);

        Intent quitIntent = new Intent(this, MyReceiver.class);
        quitIntent.setAction(QUIT_CMD_RECEIVED);
        PendingIntent pendingQuitIntent = PendingIntent.getBroadcast(this, NOTIF_P_INTENT_ID, quitIntent, 0);

        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID);
        Notification notification = notificationBuilder.setOngoing(true)
                .setSmallIcon(R.drawable.ic_icon)
                .setContentTitle("Tracking active")
                .addAction(R.drawable.ic_launcher_foreground, "Stop", pendingQuitIntent)
                .setPriority(NotificationManager.IMPORTANCE_MAX)
                .setCategory(Notification.CATEGORY_SERVICE)
                .build();
        startForeground(2, notification);
    }


    private void launchLocationAcquisition() {
        Log.d("MonLog", "launchLocationAcquisition");



        LocationManager locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        Log.d("MonLog", "LocationManager créé");
        Intent intentLoc = new Intent(this, MyReceiver.class);
        intentLoc.setAction(MyService.NEW_LOCATION_RECEIVED);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, LOC_P_INTENT_ID, intentLoc, 0);
        locationManager.requestSingleUpdate (LocationManager.GPS_PROVIDER, pendingIntent);
        Log.d("MonLog", "requestSingleUpdate lancé");
    }


    private void stopForegroundService() {
        Log.d("MonLog", "Stop foreground service.");

        // Stop foreground service and remove the notification.
        stopForeground(true);

        // Stop the foreground service.
        stopSelf();
    }
}