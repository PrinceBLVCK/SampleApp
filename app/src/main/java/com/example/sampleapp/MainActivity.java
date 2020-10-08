package com.example.sampleapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.FileObserver;
import android.provider.DocumentsContract;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import Model.User;
import Prevelant.Prevelant;

public class MainActivity extends AppCompatActivity {

    EditText username, password;
    TextView CreateAccount;
    Button Continue_btn;

    ProgressDialog loading;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        loading = new ProgressDialog(this);

        username = (EditText)findViewById(R.id.username);
        password = (EditText)findViewById(R.id.password);
        Continue_btn = (Button)findViewById(R.id.continue_btn);
        CreateAccount = (TextView)findViewById(R.id.createAccount);

        CreateAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(MainActivity.this, register_page.class));
            }
        });

        Continue_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String Username = username.getText().toString();
                String Password = password.getText().toString();
                if (TextUtils.isEmpty(Username)){
                    Snackbar.make(view,"Username cannot be empty",Snackbar.LENGTH_LONG)
                            .setAction("Action",null).show();
                }
                else if (TextUtils.isEmpty(Password)){
                    Snackbar.make(view,"Password cannot be empty",Snackbar.LENGTH_LONG)
                            .setAction("Action",null).show();
                }
                else {
                    loading.setCanceledOnTouchOutside(false);
                    loading.setTitle("Logging In");
                    loading.setMessage("Please wait...");
                    loading.show();
                    login(Username,Password);
                }
            }
        });
    }

    private void login(final String username, final String password) {
        final DatabaseReference RootRef = FirebaseDatabase.getInstance().getReference();

        RootRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.child("Users").child(username).exists()){

                    User userData = snapshot.child("Users").child(username).getValue(User.class);

                    if (username.equals(userData.getUsername()) || password.equals(userData.getPassword())) {
                        Prevelant.currentUserOnline = userData;
                        loading.dismiss();
                        startActivity(new Intent(MainActivity.this, MainActivity2.class));

                    }
                    else {
                        Toast.makeText(MainActivity.this, "Wrong Password", Toast.LENGTH_SHORT).show();
                        loading.dismiss();
                    }
                }
                else {
                    Toast.makeText(MainActivity.this,  "Account with username " + username + " do not exist", Toast.LENGTH_SHORT).show();
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }


}