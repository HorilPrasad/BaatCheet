package com.byteme.baatcheet.activity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.byteme.baatcheet.R;
import com.byteme.baatcheet.modal.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;

public class ProfileActivity extends AppCompatActivity {

    private Button nextButton;
    private EditText userName;
    private CircleImageView userImage;
    private Uri selectedImage;
    private String name;
    private FirebaseAuth mAuth;
    private FirebaseDatabase database;
    private FirebaseStorage storage;
    private String userId;

    private ProgressDialog progressDialog;

    private static final int imagePic = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);


        //initializing and binding variables

        nextButton = findViewById(R.id.next_btn);
        userName = findViewById(R.id.user_name);
        userImage = findViewById(R.id.user_profile_image);

        mAuth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();
        storage = FirebaseStorage.getInstance();
        userId = mAuth.getUid();


        //get user data if available
        getDataFromFirebase();


        //click listener for user profile image
        userImage.setOnClickListener(v -> {
            Intent intent = new Intent();
            intent.setAction(Intent.ACTION_GET_CONTENT);
            intent.setType("image/*");
            startActivityForResult(intent,imagePic);
        });

        //click event on next button
        nextButton.setOnClickListener(v ->{
            name = userName.getText().toString();
            progressDialog = new ProgressDialog(this);
             progressDialog.setTitle("Uploading data...");
             progressDialog.setCancelable(false);


            if (name.isEmpty())
            {
                userName.setError("Enter Name!");
                userName.requestFocus();
            }else if (selectedImage != null)
            {
                progressDialog.show();
                //set user name and profile image
                StorageReference storageReference = storage.getReference().child("ProfileImage").child(userId);
                storageReference.putFile(selectedImage).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                        if (task.isSuccessful()){
                            storageReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {
                                    String imageUri = uri.toString();
                                    String phone = Objects.requireNonNull(mAuth.getCurrentUser()).getPhoneNumber();
                                    String name = userName.getText().toString();

                                    User user = new User(name,userId,phone,imageUri);

                                    database.getReference()
                                            .child("Users")
                                            .child(userId)
                                            .setValue(user)
                                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @Override
                                                public void onSuccess(Void unused) {
                                                    startActivity(new Intent(ProfileActivity.this,MainActivity.class));
                                                    progressDialog.dismiss();
                                                    finish();
                                                }
                                            });
                                }
                            });
                        }
                    }
                });
            }else
            {
                progressDialog.show();
                String phone = Objects.requireNonNull(mAuth.getCurrentUser()).getPhoneNumber();
                String name = userName.getText().toString();
                //User user = new User(name,userId,phone,"NO IMAGE");
                HashMap<String,Object> hashMap = new HashMap<>();
                hashMap.put("name",name);
                hashMap.put("uid",userId);
                hashMap.put("phoneNumber",phone);
                database.getReference()
                        .child("Users")
                        .child(userId)
                        .updateChildren(hashMap)
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void unused) {
                                startActivity(new Intent(ProfileActivity.this,MainActivity.class));
                                progressDialog.dismiss();
                                finish();
                            }
                        });
            }

        });
    }

    private void getDataFromFirebase() {

        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Getting Data...");
        progressDialog.setCancelable(false);
        progressDialog.show();
        FirebaseDatabase database = FirebaseDatabase.getInstance();

        database.getReference().child("Users")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if(snapshot.exists() && snapshot.hasChild(userId))
                        {

                            Picasso.get().load(snapshot.child(userId).child("profileImage").getValue().toString()).placeholder(R.drawable.avtar).into(userImage);

                            userName.setText(snapshot.child(userId).child("name").getValue().toString());

                        }
                        progressDialog.dismiss();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        progressDialog.dismiss();
                    }
                });
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

       if (data != null && requestCode == imagePic)
       {

           selectedImage = data.getData();

           CropImage.activity()
                   .setGuidelines(CropImageView.Guidelines.ON)
                   .setAspectRatio(1,1)
                   .start(this);


       }

       if(requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE)
       {
           CropImage.ActivityResult result = CropImage.getActivityResult(data);

           if(resultCode == RESULT_OK)
           {
               assert result != null;
               userImage.setImageURI(result.getUri());
               selectedImage = result.getUri();
           }
       }

    }

    @Override
    protected void onResume() {
        super.onResume();
        String uid = FirebaseAuth.getInstance().getUid();
        FirebaseDatabase.getInstance().getReference().child("Presence").child(uid).setValue("Online");

    }

    @Override
    protected void onPause() {
        String uid = FirebaseAuth.getInstance().getUid();
        FirebaseDatabase.getInstance().getReference().child("Presence").child(uid).setValue("Offline");
        super.onPause();
    }

}