package com.example.sampleapp;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;

import java.util.HashMap;

import Prevelant.Prevelant;
import de.hdodenhof.circleimageview.CircleImageView;

public class setting_page extends AppCompatActivity {

    CircleImageView imgPreview;
    EditText username, password, phone, email;
    Button update_btn;

    private Uri imageUri;
    private String checker = "";
    private String myUrl = "";
    private StorageTask uploadTask;
    private StorageReference mStorage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting_page);

        mStorage = FirebaseStorage.getInstance().getReference().child("Profile_Picture");

        imgPreview = (CircleImageView)findViewById(R.id.imgView);
        username = (EditText)findViewById(R.id.username);
        password = (EditText)findViewById(R.id.password);
        phone = (EditText)findViewById(R.id.phone);
        email = (EditText)findViewById(R.id.email);
        update_btn = (Button)findViewById(R.id.update_btn);

        userInfoDisplay(username, password,phone,email,imgPreview);




        imgPreview.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                checker = "checked";
                CropImage.activity(imageUri)
                        .setAspectRatio(1,1)
                        .start(setting_page.this);
            }
        });

        update_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (checker.equals("checked")){
                    updateSavedInfo();
                }
                else{
                    updateOnlyInfo();
                }
            }
        });
    }

    private void updateOnlyInfo() {
        final ProgressDialog loading = new ProgressDialog(this);
        loading.setTitle("Updating Profile");
        loading.setMessage("Please wait while updating your Profile");
        loading.setCanceledOnTouchOutside(false);
        loading.show();

        DatabaseReference RootRef = FirebaseDatabase.getInstance().getReference().child("Users");

        HashMap<String,Object>userData = new HashMap<>();
        userData.put("Username", username.getText().toString());
        userData.put("Password", password.getText().toString());
        userData.put("Phone", phone.getText().toString());
        userData.put("Email_Address", email.getText().toString());

        RootRef.child(Prevelant.currentUserOnline.getPhone()).updateChildren(userData);
        loading.dismiss();
        startActivity(new Intent(setting_page.this, MainActivity2.class));
        finish();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE && resultCode == RESULT_OK && data != null){
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            imageUri = result.getUri();
            imgPreview.setImageURI(imageUri);
        }
        else {
            startActivity( new Intent(setting_page.this, setting_page.class));
            Toast.makeText(this, "Error has occurred, please try again.../n report the issue if it continue to occur.", Toast.LENGTH_SHORT).show();
            finish();
        }

    }

    private void updateSavedInfo() {
        if (TextUtils.isEmpty(username.getText().toString())){
            Toast.makeText(this, "Username cannot be empty", Toast.LENGTH_SHORT).show();
        } else if (TextUtils.isEmpty(password.getText().toString())) {
            Toast.makeText(this, "Password cannot be empty", Toast.LENGTH_SHORT).show();
        }else if (TextUtils.isEmpty(phone.getText().toString())) {
            Toast.makeText(this, "Phone Number cannot be empty", Toast.LENGTH_SHORT).show();
        }else if (TextUtils.isEmpty(email.getText().toString())) {
            Toast.makeText(this, "Email Address cannot be empty", Toast.LENGTH_SHORT).show();
        }
        else{
            uploadImgAndInfo();
        }
    }

    private void uploadImgAndInfo() {
        final ProgressDialog loading = new ProgressDialog(this);
        loading.setTitle("Updating Profile");
        loading.setMessage("Please wait while updating your Profile");
        loading.setCanceledOnTouchOutside(false);
        loading.show();

        if (imageUri != null){
            final StorageReference fileRef = mStorage.child(Prevelant.currentUserOnline.getPhone() + ".jpg");
            uploadTask = fileRef.putFile(imageUri);
            uploadTask.continueWithTask(new Continuation() {
                @Override
                public Object then(@NonNull Task task) throws Exception {

                    if (!task.isSuccessful()){
                        throw task.getException();
                    }
                    return fileRef.getDownloadUrl();
                }
            }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                @Override
                public void onComplete(@NonNull Task<Uri> task) {
                    if (task.isSuccessful()){
                        Uri downloadUri = task.getResult();
                        myUrl = downloadUri.toString();

                        DatabaseReference RootRef = FirebaseDatabase.getInstance().getReference().child("Users");

                        HashMap<String,Object>userData = new HashMap<>();
                        userData.put("Username", username.getText().toString());
                        userData.put("Password", password.getText().toString());
                        userData.put("Phone", phone.getText().toString());
                        userData.put("Email_Address", email.getText().toString());
                        userData.put("Image", myUrl);

                        RootRef.child(Prevelant.currentUserOnline.getPhone()).updateChildren(userData);
                        loading.dismiss();
                        startActivity(new Intent(setting_page.this, MainActivity2.class));
                        finish();
                    }
                    else {
                        Toast.makeText(setting_page.this, "Choose a Profile picture", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
    }

    private void userInfoDisplay(EditText username, EditText password, EditText phone, EditText email, CircleImageView imgPreview) {
        username.setText(Prevelant.currentUserOnline.getUsername());
        password.setText(Prevelant.currentUserOnline.getPassword());
        phone.setText(Prevelant.currentUserOnline.getPhone());
        email.setText(Prevelant.currentUserOnline.getEmail_Address());

        Picasso.get().load(Prevelant.currentUserOnline.getImage()).into(imgPreview);
    }
}