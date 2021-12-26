package com.byteme.baatcheet.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.byteme.baatcheet.R;
import com.byteme.baatcheet.activity.ChatActivity;
import com.byteme.baatcheet.modal.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.UserViewHolder>{

    Context context;
    ArrayList<User> users;

    public UserAdapter(Context context,ArrayList<User> users)
    {
        this.context = context;
        this.users = users;
    }

    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.user_list,parent,false);

        return new UserViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {
        User user = users.get(position);
        holder.userName.setText(user.getName());

        String senderId = FirebaseAuth.getInstance().getUid();
        String senderRoom = senderId + user.getUid();

        FirebaseDatabase.getInstance().getReference()
                .child("Chats")
                .child(senderRoom)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists() && snapshot.hasChild("lastMessage")) {
                            String lastMsg = snapshot.child("lastMessage").getValue(String.class);
                            String lastMsgTime = snapshot.child("lastMessageTime").getValue(String.class);
                            holder.lastMessage.setText(lastMsg);
                            holder.lastMessageTime.setText(lastMsgTime);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });


        Picasso.get().load(user.getProfileImage()).placeholder(R.drawable.avtar).into(holder.userImage);
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, ChatActivity.class);
            intent.putExtra("uid",user.getUid());
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return (users == null)?0:users.size();
    }

    public static class UserViewHolder extends RecyclerView.ViewHolder {
        TextView userName,lastMessage,lastMessageTime;
        CircleImageView userImage;
        RelativeLayout relativeLayout;
        public UserViewHolder(@NonNull View itemView) {
            super(itemView);

            userName = itemView.findViewById(R.id.list_user_name);
            userImage = itemView.findViewById(R.id.list_profile_img);
            relativeLayout = itemView.findViewById(R.id.list_layout);
            lastMessage = itemView.findViewById(R.id.user_last_message);
            lastMessageTime = itemView.findViewById(R.id.last_message_time);
        }
    }
}
