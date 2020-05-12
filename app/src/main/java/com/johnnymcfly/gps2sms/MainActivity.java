package com.johnnymcfly.gps2sms;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import java.util.ArrayList;


public class MainActivity extends AppCompatActivity {

    public static final int PICK_CONTACT = 102;
    public static final int REQ_SEND_SMS_PERMISSION = 103;
    public static final int REQ_ACCESS_FINE_LOCATION_PERMISSION = 104;
    public static final int REQ_READ_CONTACTS_PERMISSION = 105;
    public static final String PHONE_NBRS_ARRAY_EXTRA = "com.johnnymcfly.gps2sms.MESSAGE";
    public static final String FINISH_MAIN_ACT_MSG = "com.johnnymcfly.gps2sms.STOP";


    private final BroadcastReceiver mainActBrReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d("MonLog", "mainActBrReceiver received STOP command");
            finish();
        }
    };

    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);

        Log.d("MonLog", "mainActivity.onCreate");

        registerReceiver(mainActBrReceiver, new IntentFilter(FINISH_MAIN_ACT_MSG));

        if (    (checkSelfPermission(Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED
                 || checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                 || checkSelfPermission(Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED)

        ) {
            Log.d("MonLog", "pas toutes les permissions");
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.SEND_SMS}, REQ_SEND_SMS_PERMISSION);
        }
        else  {
            Log.d("MonLog", "toutes les permissions ok");
        }
      /*  else  {
            Log.d("MonLog", "permission SEND_SMS ok");
        }

        if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Log.d("MonLog", "pas de permission FINE_LOCATION");
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQ_ACCESS_FINE_LOCATION_PERMISSION);
        }
        else  {
            Log.d("MonLog", "permission ACCESS_FINE_LOCATION ok");
        }

        if (checkSelfPermission(Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
            Log.d("MonLog", "pas de permission READ_CONTACTS");
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_CONTACTS}, REQ_READ_CONTACTS_PERMISSION);
        }
        else  {
            Log.d("MonLog", "permission READ_CONTACTS ok");
        }*/


        setContentView(R.layout.activity_main);

        MyApplication application=(MyApplication)getApplication();

        if (application.trackingStarted){
            View myObject = findViewById(R.id.startTrackingButton);
            myObject.setVisibility(View.INVISIBLE);
            myObject = findViewById(R.id.helpText);
            myObject.setVisibility(View.INVISIBLE);

            myObject = findViewById(R.id.trackingActiveText);
            myObject.setVisibility(View.VISIBLE);
        }
        else{
            View myObject = findViewById(R.id.trackingActiveText);
            myObject.setVisibility(View.INVISIBLE);
        }


        final Button startTrackingButton = findViewById(R.id.startTrackingButton);
        startTrackingButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent pickContactIntent = new Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI);
                startActivityForResult(pickContactIntent, PICK_CONTACT);
                Log.d("MonLog","startActivityForResult lancé");
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case REQ_SEND_SMS_PERMISSION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.d("MonLog", "Permission SMS donnée");

                    ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQ_ACCESS_FINE_LOCATION_PERMISSION);
                }
               else {
                   finish();
               }
               return;
            }

            case REQ_ACCESS_FINE_LOCATION_PERMISSION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.d("MonLog", "Permission Fine Loc donnée");

                    ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_CONTACTS}, REQ_READ_CONTACTS_PERMISSION);
                }
                else {
                    finish();
                }
                return;
            }

            case REQ_READ_CONTACTS_PERMISSION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.d("MonLog", "Permission Read Contacts donnée");
                }
                else {
                    finish();
                }
                return;
            }
        }
    }


    @Override
    public void onActivityResult(int reqCode, int resultCode, Intent data) {
        super.onActivityResult(reqCode, resultCode, data);

        ArrayList<String> phoneNumbersList = new ArrayList<String>();

        switch (reqCode) {
        case (PICK_CONTACT) :
            if (resultCode == Activity.RESULT_OK) {
            Uri contactData = data.getData();
            Cursor cur = getContentResolver().query(contactData, null, null, null, null);
            if (cur.getCount() > 0) {// that means that some result has been found
                Log.d("MonLog", "cur.getCount()" );
                if (cur.moveToNext()) {
                    String id = cur.getString(cur.getColumnIndex(ContactsContract.Contacts._ID));
                    Log.d("MonLog", id);
                    String name = cur.getString(cur.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
                    Log.d("MonLog", name);

                    if (Integer.parseInt(cur.getString(cur.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER))) > 0) {
                        Cursor phones = getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = " + id, null, null);

                        while (phones.moveToNext()) {
                            String phoneNumber = phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                            Log.d("MonLog", phoneNumber);
                            phoneNumbersList.add(phoneNumber);
                        }
                        phones.close();
                        Intent intent = new Intent(this, phoneNumbersChoiceActivity.class);
                        intent.putExtra(PHONE_NBRS_ARRAY_EXTRA, phoneNumbersList);
                        startActivity(intent);
                    }
                }
            }
            cur.close();
            }
            finish();

            break;
        }
    }
}