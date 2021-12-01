package com.example.mohaq;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;

public class registerInterface extends AppCompatActivity {

    FirebaseAuth mAuth;
    EditText username_ed, device_ed;
    Button submit_btn;
    private String username, device;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_interface);

        mAuth = FirebaseAuth.getInstance();
        username_ed = findViewById(R.id.ed_username);
        device_ed = findViewById(R.id.ed_deviceName);
        submit_btn = findViewById(R.id.btn_submit);
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Please wait...");
        progressDialog.setCanceledOnTouchOutside(false);

        submit_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                inputData();
            }
        });
    }

    private void inputData() {
        username = username_ed.getText().toString().trim();
        device = device_ed.getText().toString().trim();

        if(username_ed.getText().toString().matches("")){
            Toast.makeText(getApplicationContext(),"Username is empty!",Toast.LENGTH_SHORT).show();
            return;
        }

        if(device_ed.getText().toString().matches("")){
            Toast.makeText(getApplicationContext(),"Device is empty!",Toast.LENGTH_SHORT).show();
            return;
        }

        createAccount();
    }

    public void createAccount() {
        progressDialog.setMessage("Creating Account...");
        progressDialog.show();

        //create account and save into firebase
        mAuth.signInAnonymously().addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    saveAccountData();
                }else {
                    // If sign in fails, display a message to the user.
                    Toast.makeText(getApplicationContext(), "Authentication failed.", Toast.LENGTH_SHORT).show();
                }
            }
        });

    }

    public void saveAccountData(){
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("uid",""+mAuth.getUid());
        hashMap.put("username",""+username);
        hashMap.put("device",""+device);

        DatabaseReference dRef = FirebaseDatabase.getInstance().getReference("User");
        dRef.child(mAuth.getUid()).setValue(hashMap).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                startActivity(new Intent(registerInterface.this, monitorInterface.class));
            }
        });
    }
}