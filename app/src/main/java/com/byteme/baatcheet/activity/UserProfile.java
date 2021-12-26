package com.byteme.baatcheet.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.os.Bundle;
import android.view.MenuItem;
import android.widget.TextView;

import com.byteme.baatcheet.R;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

public class UserProfile extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);

        CircleImageView userImage = findViewById(R.id.profile_user_image);
        TextView userName = findViewById(R.id.profile_user_name);
        TextView userPhone = findViewById(R.id.profile_user_phone);

        Toolbar toolbar = findViewById(R.id.user_profile_app_bar);
        setSupportActionBar(toolbar);

        String name = getIntent().getStringExtra("name");
        String image = getIntent().getStringExtra("image");
        String phone = getIntent().getStringExtra("phone");

        getSupportActionBar().setTitle(name);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        userName.setText(name);
        userPhone.setText(phone);
        Picasso.get().load(image).placeholder(R.drawable.avtar).into(userImage);

    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home)
        {
            onBackPressed();
        }
        return super.onOptionsItemSelected(item);
    }
}