package com.example.myproject;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.bluetooth.BluetoothAdapter;



import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class MainActivity extends AppCompatActivity {



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //admin@admin.com
        //admin0000

    }


    public void homePage(View view) {

        FirebaseAuth fAuth = FirebaseAuth.getInstance();
        TextView e2 = findViewById(R.id.et_password);
        TextView e3 = findViewById(R.id.signin);
        TextView e1 = findViewById(R.id.et_username);

        String email = e1.getText().toString().trim();
        String password = e2.getText().toString().trim();

        if (TextUtils.isEmpty(email)) {
            e1.setError("Email is required");
        }
        if (TextUtils.isEmpty(password)) {
            e2.setError("password is required");

        }
        if (password.length() < 6) {
            e2.setError("password Must be 6 characters");
        }else {
            fAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(
                    new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                Toast.makeText(MainActivity.this, "logged in Successfully", Toast.LENGTH_LONG).show();
                                startActivity(new Intent(getApplicationContext(), HomePage.class));
                            } else {
                                Toast.makeText(MainActivity.this, "Error !", Toast.LENGTH_LONG).show();
                            }
                        }
                    });
        }


    }



}