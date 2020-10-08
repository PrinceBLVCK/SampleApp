package com.example.sampleapp;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Credentials;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.provider.Settings;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.theartofdev.edmodo.cropper.CropImage;

import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

public class register_page extends AppCompatActivity {

    CircleImageView imgView;
    TextView choosePic;
    EditText username, password, phoneNum, email;
    CheckBox termAndConditions;
    Button submit;

    private Uri imageUri;
    private StorageReference mStorage;
    private StorageTask uploadTask;
    private String myUrl;
    private boolean User_Available;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_page);

        mStorage = FirebaseStorage.getInstance().getReference().child("Profile_Picture");

        imgView = (CircleImageView)findViewById(R.id.imgView);
        choosePic = (TextView)findViewById(R.id.choosePic);
        username = (EditText)findViewById(R.id.username);
        password = (EditText)findViewById(R.id.password);
        phoneNum = (EditText)findViewById(R.id.phone);
        email = (EditText)findViewById(R.id.email);
        termAndConditions = (CheckBox)findViewById(R.id.termsAndConditions);
        submit = (Button)findViewById(R.id.submit);


        choosePic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CropImage.activity(imageUri)
                        .setAspectRatio(1,1)
                        .start(register_page.this);
            }
        });

        termAndConditions.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (termAndConditions.isChecked()){
                    submit.setVisibility(View.VISIBLE);
                }
                else {
                    submit.setVisibility(View.GONE);
                }
            }
        });

        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (TextUtils.isEmpty(username.getText().toString())){
                    Snackbar.make(view,"Username cannot be empty",Snackbar.LENGTH_LONG)
                            .setAction("Action",null).show();
                }
                else if (TextUtils.isEmpty(password.getText().toString())){
                    Snackbar.make(view,"Password cannot be empty",Snackbar.LENGTH_LONG)
                            .setAction("Action",null).show();
                }
                else if (TextUtils.isEmpty(phoneNum.getText().toString())){
                    Snackbar.make(view,"Phone Number cannot be empty",Snackbar.LENGTH_LONG)
                            .setAction("Action",null).show();
                }
                else if (TextUtils.isEmpty(email.getText().toString())){
                    Snackbar.make(view,"Email Address cannot be empty",Snackbar.LENGTH_LONG)
                            .setAction("Action",null).show();
                }
                else {
                    boolean isAvailable = available(phoneNum.getText().toString());
                    if (isAvailable == true){
                        Toast.makeText(register_page.this, "Account with " + phoneNum.getText().toString()+ "phone number already exists", Toast.LENGTH_SHORT).show();
                    }
                    else {
                        createAccount(view);
                    }
                }


            }
        });

    }

    private boolean available(final String user){


        DatabaseReference RootRef = FirebaseDatabase.getInstance().getReference().child("Users");

        RootRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.child(user).exists()){
                    User_Available = true;
                }
                else {
                    User_Available = false;
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(register_page.this, ""+ error, Toast.LENGTH_SHORT).show();
            }
        });
        return User_Available;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE && resultCode == RESULT_OK && data != null){
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            imageUri = result.getUri();
            imgView.setImageURI(imageUri);
        }
    }

    private void createAccount(final View view) {
        if (imageUri != null){
            Snackbar.make(view,"Creating user Account", Snackbar.LENGTH_LONG)
                    .setAction("Action",null).show();

            final StorageReference imgRef = mStorage.child(phoneNum.getText().toString() + "jpg");
            uploadTask = imgRef.putFile(imageUri);
            uploadTask.continueWithTask(new Continuation() {
                @Override
                public Object then(@NonNull Task task) throws Exception {
                    if (!task.isSuccessful()){
                        throw task.getException();
                    }
                    return imgRef.getDownloadUrl();
                }
            }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                @Override
                public void onComplete(@NonNull Task<Uri> task) {
                    Uri downloadUrl = task.getResult();
                    myUrl = downloadUrl.toString();

                    DatabaseReference RootRef = FirebaseDatabase.getInstance().getReference().child("Users");

                    HashMap<String,Object>userData = new HashMap<>();
                    userData.put("Username", username.getText().toString());
                    userData.put("Password", password.getText().toString());
                    userData.put("Phone", phoneNum.getText().toString());
                    userData.put("Email_Address", email.getText().toString());
                    userData.put("Image", myUrl);

                    RootRef.child(phoneNum.getText().toString()).updateChildren(userData)
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()){
                                Snackbar.make(view,"Account Successfully Created",Snackbar.LENGTH_LONG)
                                        .setAction("Action",null).show();
                                startActivity(new Intent(register_page.this,MainActivity.class));
                            }
                        }
                    });


                }
            });


        }
        else {
            Snackbar.make(view,"Choose a Profile Picture", Snackbar.LENGTH_LONG)
                    .setAction("Action",null).show();
        }
    }
}