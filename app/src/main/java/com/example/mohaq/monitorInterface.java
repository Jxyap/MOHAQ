package com.example.mohaq;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.messaging.FirebaseMessaging;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Formatter;

public class monitorInterface extends AppCompatActivity {

    DatabaseReference dRef;
    String currentDate, uid, device;
    FirebaseAuth mAuth;
    FirebaseUser user;
    TextView temperatureView, nh3View, coView, humidityView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_monitor_interface);

        currentDate=getDate();
        RelativeLayout tempLayout = findViewById(R.id.tempLayout);
        RelativeLayout humidityLayout = findViewById(R.id.humiLayout);
        RelativeLayout coLayout = findViewById(R.id.coLayout);
        RelativeLayout nh3Layout = findViewById(R.id.nh3Layout);
        temperatureView = findViewById(R.id.TempRead);
        nh3View = findViewById(R.id.nh3Read);
        coView = findViewById(R.id.coRead);
        humidityView = findViewById(R.id.humidityRead);

        mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();
        uid = user.getUid();

        FirebaseMessaging.getInstance().getToken().addOnCompleteListener(new OnCompleteListener<String>() {
            @Override
            public void onComplete(@NonNull Task<String> task) {
                Log.d("token", task.getResult());
            }
        });
        searchDevice();

        tempLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent analysisIntent = new Intent(monitorInterface.this, analysisGraph.class);
                analysisIntent.putExtra("Type", "Temperature Reading");
                startActivity(analysisIntent);
            }
        });

        humidityLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent analysisIntent = new Intent(monitorInterface.this, analysisGraph.class);
                analysisIntent.putExtra("Type", "Humidity Reading");
                startActivity(analysisIntent);
            }
        });

        coLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent analysisIntent = new Intent(monitorInterface.this, analysisGraph.class);
                analysisIntent.putExtra("Type", "CO Reading");
                startActivity(analysisIntent);
            }
        });

        nh3Layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent analysisIntent = new Intent(monitorInterface.this, analysisGraph.class);
                analysisIntent.putExtra("Type", "NH3 Reading");
                startActivity(analysisIntent);
            }
        });

        Notification();
    }

    private void searchDevice(){
        dRef = FirebaseDatabase.getInstance().getReference();
        dRef.child("User").child(uid).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                device = snapshot.child("device").getValue(String.class);
                displayReading();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    public void displayReading(){
        dRef = FirebaseDatabase.getInstance().getReference("Device/"+device+"/SensorReading/");
        Query lastQuery = dRef.child(currentDate).limitToLast(1);

        lastQuery.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for(DataSnapshot child : snapshot.getChildren()){
                    temperatureView.setText(child.child("Temperature").getValue(Float.class)+" \u2103");
                    humidityView.setText(child.child("Humidity").getValue(Float.class)+" %");
                    Formatter coValue = new Formatter();
                    Formatter nh3Value = new Formatter();
                    coValue.format("%.4f",child.child("coValue").getValue(Float.class));
                    coView.setText(String.valueOf(coValue));
                    nh3Value.format("%.4f", child.child("nh3Value").getValue(Float.class));
                    nh3View.setText(String.valueOf(nh3Value));
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    public String getDate(){
        SimpleDateFormat date  = new SimpleDateFormat("yyyyMMdd");
        Date todayDate = new Date();
        return date.format(todayDate);
    }

    @Override
    protected void onPause() {
        Notification();
        super.onPause();
    }

    @Override
    protected void onResume() {
        Notification();
        super.onResume();
    }

    public void Notification(){
        dRef = FirebaseDatabase.getInstance().getReference();
        dRef.child("User").child(uid).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String device = snapshot.child("device").getValue(String.class);
                dRef.child("Device").child(device).child("Notification").addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for(DataSnapshot ds:snapshot.getChildren()){
                            if(ds.getValue(String.class).matches("true")){
                                createNotification(ds.getKey());
                                dRef.child("Device").child(device).child("Notification").child(ds.getKey()).setValue("false");
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    public void createNotification(String Factor){
        String title = "MOHAQ Alert";
        String text = "Factor: "+Factor+"is exceed threshold! Please take action!";
        final String CHANNEL_ID = "HEADS_UP_NOTIFICATION";
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "Heads Up Notification",
                    NotificationManager.IMPORTANCE_HIGH
            );
        }
        NotificationCompat.Builder notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.logo)
                .setContentTitle(title)
                .setContentText(text)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);
        NotificationManagerCompat.from(this).notify(1, notification.build());
    }
}