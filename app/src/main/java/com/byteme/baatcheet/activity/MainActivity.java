package com.byteme.baatcheet.activity;

import static java.lang.System.exit;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.RecyclerView;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;

import com.byteme.baatcheet.R;
import com.byteme.baatcheet.adapter.UserAdapter;
import com.byteme.baatcheet.modal.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    Toolbar toolbar;
    private FirebaseAuth mAuth;
    private FirebaseDatabase database;
    private RecyclerView recyclerView;
    private UserAdapter userAdapter;
    private ArrayList<User> users;
    private String currentUid;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //initializing and binding variables

        toolbar =  findViewById(R.id.main_activity_actionbar);

        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("ChatWithMe");
        progressBar = findViewById(R.id.main_progressbar);

        recyclerView = findViewById(R.id.user_recycler_view);

        mAuth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();
        users = new ArrayList<>();

        //set adapter for retrieve users
        userAdapter = new UserAdapter(this,users);
        recyclerView.setAdapter(userAdapter);
        recyclerView.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));


    }

    private void retrieveUsers() {
        currentUid = mAuth.getUid();
        database.getReference().child("Users")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        users.clear();
                        for (DataSnapshot snapshot1 : snapshot.getChildren())
                        {
                            User user = snapshot1.getValue(User.class);
                            if(!currentUid.equals(user.getUid()))
                            users.add(user);
                        }
                        userAdapter.notifyDataSetChanged();
                        progressBar.setVisibility(View.GONE);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser user = mAuth.getCurrentUser();

        //check user already login or not
        if (user==null)
        {
            //not login then sent login activity
            sentToLoginActivity();
        }else {
            //user login then retrieve data of users
            progressBar.setVisibility(View.VISIBLE);
            retrieveUsers();
        }

    }

    //function for sent login activity
    private void sentToLoginActivity() {
        Intent intent = new Intent(MainActivity.this,Login.class);
        startActivity(intent);
        finishAffinity();
    }

    //override function for showing menu items
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu,menu);
        return super.onCreateOptionsMenu(menu);
    }

    //override function for set click listener on menu items
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        if (item.getItemId()==R.id.menu_setting)
        {
            //setting

            startActivity(new Intent(this,ProfileActivity.class));
        }else if (item.getItemId()==R.id.menu_quit)
        {
            alertDialog();
        }else if (item.getItemId() == R.id.about)
        {
            startActivity(new Intent(MainActivity.this,AboutUs.class));
        }

        return super.onOptionsItemSelected(item);
    }

    //create alert dialog for exit form app

    public void alertDialog()
    {
        new AlertDialog.Builder(this)
                .setTitle("Alert")
                .setMessage("Do you really want to Exit?")
                .setIcon(R.drawable.ic_warning)
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int whichButton) {
                        FirebaseAuth.getInstance().signOut();
                        startActivity(new Intent(MainActivity.this,Login.class));
                        finishAffinity();
                    }})
                .setNegativeButton(android.R.string.no, null).show();
    }

    @Override
    protected void onResume() {
        super.onResume();
        String uid = FirebaseAuth.getInstance().getUid();
        database.getReference().child("Presence").child(uid).setValue("Online");

    }

    @Override
    protected void onPause() {
        String uid = FirebaseAuth.getInstance().getUid();
        database.getReference().child("Presence").child(uid).setValue("Offline");
        super.onPause();
    }
}