package com.byteme.baatcheet.activity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.RecyclerView;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.byteme.baatcheet.R;
import com.byteme.baatcheet.adapter.MessageAdapter;
import com.byteme.baatcheet.modal.Message;
import com.byteme.baatcheet.modal.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
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

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;

public class ChatActivity extends AppCompatActivity {

    private EditText typeMessage;

    private FirebaseAuth mAuth;
    private FirebaseDatabase database;
    private DatabaseReference databaseReference;
    private FirebaseStorage storage;

    private MessageAdapter messageAdapter;
    private ArrayList<Message> messages;
    private RecyclerView recyclerView;


    private String senderUid,receiverUid;
    private String senderRoom, receiverRoom;

    private ValueEventListener seenListener;

    private String userName,userImage,phone;
    private TextView receiverName;
    private TextView userStatus;
    private CircleImageView receiverImage;

    private final int GET_IMAGE =1;
    private ProgressDialog dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);


        // Initializing and binding XML layout

        Toolbar toolbar = findViewById(R.id.chat_activity_bar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        recyclerView = findViewById(R.id.chat_recyclerview);
        typeMessage = findViewById(R.id.type_message);
        ImageView sentMessagebtn = findViewById(R.id.sent_message_btn);
        receiverName = findViewById(R.id.receiver_name);
        receiverImage = findViewById(R.id.receiver_image);
        userStatus = findViewById(R.id.user_last_seen);
        ImageView attachment = findViewById(R.id.attachment);
        ImageView camera = findViewById(R.id.camera);
        dialog = new ProgressDialog(this);
        dialog.setTitle("Image uploading...");
        dialog.setCancelable(false);

        receiverUid = getIntent().getStringExtra("uid");

        //back arrow
        ImageView backArrow = findViewById(R.id.back_arrow_chat_activity);
        backArrow.setOnClickListener(v -> {
            onBackPressed();
        });


        //Firebase instance
        mAuth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();
        storage = FirebaseStorage.getInstance();
        databaseReference = FirebaseDatabase.getInstance().getReference();
        senderUid = mAuth.getUid();

        senderRoom = senderUid + receiverUid;
        receiverRoom = receiverUid + senderUid;


        messages = new ArrayList<>();

        //set Adapter for retrieving Messages
        messageAdapter = new MessageAdapter(this,messages);
        recyclerView.setAdapter(messageAdapter);


        database.getReference().child("Users").child(receiverUid)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists())
                        {

                            userName = snapshot.child("name").getValue(String.class);
                            userImage = snapshot.child("profileImage").getValue(String.class);
                            receiverName.setText(userName);
                            Picasso.get().load(userImage).placeholder(R.drawable.avtar).into(receiverImage);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });


        database.getReference().child("Presence").child(receiverUid).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists())
                {
                    String status = snapshot.getValue(String.class);
                    if (!status.isEmpty())
                    {
                        if (status.equals("Offline"))
                        {
                            userStatus.setVisibility(View.GONE);

                        }else
                        {
                            userStatus.setText(status);
                            userStatus.setVisibility(View.VISIBLE);
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });


        final Handler handler = new Handler();
        typeMessage.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {

                database.getReference().child("Presence").child(senderUid).setValue("typing...");
                handler.removeCallbacksAndMessages(null);
                handler.postDelayed(userStopTyping,1000);
            }
            final Runnable userStopTyping = new Runnable() {
                @Override
                public void run() {
                    database.getReference().child("Presence").child(senderUid).setValue("Online");
                }
            };
        });



        // Firebase database reference

        database.getReference().child("Chats").child(senderRoom).child("Messages")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        messages.clear();

                        for (DataSnapshot snapshot1 : snapshot.getChildren()){
                            Message message = snapshot1.getValue(Message.class);
                            messages.add(message);
                        }
                        messageAdapter.notifyDataSetChanged();
                        recyclerView.scrollToPosition(messages.size()-1);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }


                });

        //message deliver or seen
        seenMessage(receiverUid);


        //click event on sent message button
        sentMessagebtn.setOnClickListener(v -> {
            sentMessages();
        });


        attachment.setOnClickListener(v -> {
            getImage();
        });

        camera.setOnClickListener(v -> {
            getImage();
        });

        LinearLayout layout = findViewById(R.id.profile);
        layout.setOnClickListener(v -> {

           database.getReference().child("Users").child(receiverUid)
                   .addValueEventListener(new ValueEventListener() {
                       @Override
                       public void onDataChange(@NonNull DataSnapshot snapshot) {
                           if (snapshot.exists())
                           {
                               phone = snapshot.child("phoneNumber").getValue(String.class);
                           }

                       }

                       @Override
                       public void onCancelled(@NonNull DatabaseError error) {

                       }
                   });
            Intent intent = new Intent(ChatActivity.this,UserProfile.class);
            intent.putExtra("name",userName);
            intent.putExtra("image",userImage);
            intent.putExtra("phone",phone);
            startActivity(intent);
        });

    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == GET_IMAGE)
        {
            if (data != null)
            {
                Uri selectedImage = data.getData();

                Calendar calendar = Calendar.getInstance();
                dialog.show();
                StorageReference reference = storage.getReference().child(senderUid).child(calendar.getTimeInMillis()+"");
                reference.putFile(selectedImage).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                        dialog.dismiss();
                        if (task.isSuccessful())
                        {
                            reference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(@NonNull Uri uri) {
                                    String filePath = uri.toString();
                                    //Check message is empty or not

                                        Calendar calendar = Calendar.getInstance();
                                        SimpleDateFormat dateFormat = new SimpleDateFormat("hh:mm a", Locale.ENGLISH);
                                        String time = dateFormat.format(calendar.getTime());
                                        String userTypeMessage = "photo";

                                        Message message = new Message(userTypeMessage,senderUid,time,false,filePath);

                                        String key = database.getReference().push().getKey();

                                        HashMap<String, Object> lastMessage = new HashMap<>();
                                        lastMessage.put("lastMessage",message.getMessage());
                                        lastMessage.put("lastMessageTime",message.getTimeStamp());

                                        database.getReference().child("Chats").child(senderRoom).updateChildren(lastMessage);
                                        database.getReference().child("Chats").child(receiverRoom).updateChildren(lastMessage);

                                        database.getReference().child("Chats")
                                                .child(senderRoom)
                                                .child("Messages")
                                                .child(key)
                                                .setValue(message)
                                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                    @Override
                                                    public void onSuccess(Void unused) {

                                                    }
                                                });
                                        database.getReference().child("Chats")
                                                .child(receiverRoom)
                                                .child("Messages")
                                                .child(key)
                                                .setValue(message)
                                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                    @Override
                                                    public void onSuccess(Void unused) {

                                                    }
                                                });
                                    }


                            });
                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        dialog.dismiss();
                    }
                });
            }
        }

    }




    private void getImage() {

        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        startActivityForResult(intent,GET_IMAGE);
    }

    private void seenMessage(String uid)
    {
        databaseReference = databaseReference.child("Chats").child(receiverRoom).child("Messages");
        seenListener = databaseReference
                .addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot snapshot1 : snapshot.getChildren())
                {
                    Message message = snapshot1.getValue(Message.class);

                    if (message.getSenderId().equals(uid))
                    {
                        HashMap<String, Object> hashMap = new HashMap<>();
                        hashMap.put("isseen",true);
                        snapshot1.getRef().updateChildren(hashMap);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    //function for sending message
    private void sentMessages() {

        String userTypeMessage = typeMessage.getText().toString();
        //Check message is empty or not
        if (userTypeMessage.isEmpty())
        {
            typeMessage.setError("Enter Message!");
            typeMessage.requestFocus();
        }else{
            Calendar calendar = Calendar.getInstance();
            SimpleDateFormat dateFormat = new SimpleDateFormat("hh:mm a", Locale.ENGLISH);
            String time = dateFormat.format(calendar.getTime());
            typeMessage.setText("");

            Message message = new Message(userTypeMessage,senderUid,time,false,"no image");

            String key = database.getReference().push().getKey();

            HashMap<String, Object> lastMessage = new HashMap<>();
            lastMessage.put("lastMessage",message.getMessage());
            lastMessage.put("lastMessageTime",message.getTimeStamp());

            database.getReference().child("Chats").child(senderRoom).updateChildren(lastMessage);
            database.getReference().child("Chats").child(receiverRoom).updateChildren(lastMessage);

            database.getReference().child("Chats")
                    .child(senderRoom)
                    .child("Messages")
                    .child(key)
                    .setValue(message)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void unused) {

                        }
                    });
            database.getReference().child("Chats")
                    .child(receiverRoom)
                    .child("Messages")
                    .child(key)
                    .setValue(message)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void unused) {

                        }
                    });
        }
    }

    //overriding the function for back navigation button

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home)
        {
            onBackPressed();
        }

        if (item.getItemId() == R.id.clear_chat)
        {
            clearAllChat();
        }

        return super.onOptionsItemSelected(item);
    }

    private void clearAllChat() {
        database.getReference().child("Chats").child(senderRoom).removeValue()
        .addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(@NonNull Void unused) {
                Toast.makeText(ChatActivity.this, "Chat Cleared", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.chat_menu,menu);
        return super.onCreateOptionsMenu(menu);
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
        databaseReference.removeEventListener(seenListener);
        super.onPause();

    }
}