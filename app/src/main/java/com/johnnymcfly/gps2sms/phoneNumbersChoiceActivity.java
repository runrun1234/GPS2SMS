package com.johnnymcfly.gps2sms;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;


import java.util.ArrayList;

public class phoneNumbersChoiceActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_phone_numbers_choice);
        Intent intent = getIntent();

        ArrayList<String> phoneNumbersList = intent.getStringArrayListExtra(MainActivity.PHONE_NBRS_ARRAY_EXTRA);

        final ListView phoneNumbersListView = (ListView) findViewById(R.id.phoneNumbersListView);
        final ArrayAdapter<String> adapter = new ArrayAdapter<String>(phoneNumbersChoiceActivity.this, android.R.layout.simple_list_item_1, phoneNumbersList);
        phoneNumbersListView.setAdapter(adapter);

        // When the user clicks on the ListItem
        phoneNumbersListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> a, View v, int position, long id) {
                Object o = phoneNumbersListView.getItemAtPosition(position);

                MyApplication application=(MyApplication)getApplication();
                application.SMSphoneNumber = (String) o;
                Log.d("MonLog", "SMSphoneNumber: " + application.SMSphoneNumber);

                Intent intent = new Intent(phoneNumbersChoiceActivity.this, MyService.class);
                intent.setAction(MyService.ACTION_START_FOREGROUND_SERVICE);
                startForegroundService(intent);

                finish();
            }
        });
    }
}