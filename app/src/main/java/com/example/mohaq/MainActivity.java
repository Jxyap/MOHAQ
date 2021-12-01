package com.example.mohaq;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class MainActivity extends AppCompatActivity {

    FirebaseAuth mAuth;
    FirebaseUser user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();

        Button register = findViewById(R.id.btn_reg);
        Button homepage_btn = findViewById(R.id.btn_home);

        if(user != null){
            register.setVisibility(View.INVISIBLE);
            homepage_btn.setVisibility(View.VISIBLE);
        }
        else{
            register.setVisibility(View.VISIBLE);
            homepage_btn.setVisibility(View.INVISIBLE);
        }

        register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this,registerInterface.class));
            }
        });

        homepage_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, monitorInterface.class));
            }
        });
    }

    @Override
    public void onBackPressed() {
        finish();
    }
}