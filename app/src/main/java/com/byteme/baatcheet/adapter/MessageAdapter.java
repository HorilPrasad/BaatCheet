package com.byteme.baatcheet.adapter;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.byteme.baatcheet.R;
import com.byteme.baatcheet.activity.Login;
import com.byteme.baatcheet.activity.MainActivity;
import com.byteme.baatcheet.modal.Message;
import com.google.firebase.auth.FirebaseAuth;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class MessageAdapter extends RecyclerView.Adapter{

    FirebaseAuth mAuth = FirebaseAuth.getInstance();
    String Uid = mAuth.getUid();

    Context context;
    ArrayList<Message> messages;

    final int ITEM_SENT = 1;
    final int ITEM_RECEIVE = 2;

    public MessageAdapter(Context context,ArrayList<Message> messages){
        this.context = context;
        this.messages = messages;
    }


    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        if(viewType == ITEM_SENT){
            View view = LayoutInflater.from(context).inflate(R.layout.item_sent,parent,false);
            return new SentViewHolder(view);
        }else{
            View view = LayoutInflater.from(context).inflate(R.layout.item_recieve,parent,false);
            return new ReceiveViewHolder(view);
        }

    }

    @Override
    public int getItemViewType(int position) {
        {
            Message message = messages.get(position);
            if(Uid.equals(message.getSenderId()))
            {
                return ITEM_SENT;
            }else
            {
                return ITEM_RECEIVE;
            }
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {

        Message message = messages.get(position);

        if (holder.getClass() == SentViewHolder.class) {

            SentViewHolder viewHolder = (SentViewHolder) holder;
            if (message.getMessage().equals("photo"))
            {
                viewHolder.sentMessage.setVisibility(View.GONE);
                Picasso.get().load(message.getImage()).placeholder(R.drawable.image_placeholder).into(viewHolder.sentImage);
                viewHolder.sentImage.setVisibility(View.VISIBLE);
            }else{
                viewHolder.sentImage.setVisibility(View.GONE);
                viewHolder.sentMessage.setVisibility(View.VISIBLE);
                viewHolder.sentMessage.setText(message.getMessage());
            }

            viewHolder.sentMessageTime.setText(message.getTimeStamp());
            if (position == messages.size()-1)
            {
                if (message.isIsseen())
                {
                    ((SentViewHolder) holder).isSeen.setVisibility(View.VISIBLE);
                    ((SentViewHolder) holder).isSeen.setText("seen");
                }else
                {
                    ((SentViewHolder) holder).isSeen.setVisibility(View.VISIBLE);
                    ((SentViewHolder) holder).isSeen.setText("Delivered");
                }
            }else
            {
                ((SentViewHolder) holder).isSeen.setVisibility(View.GONE);
            }
        }else {
            ReceiveViewHolder viewHolder = (ReceiveViewHolder) holder;

            if (message.getMessage().equals("photo"))
            {
                viewHolder.receiveImage.setVisibility(View.GONE);
                Picasso.get().load(message.getImage()).placeholder(R.drawable.image_placeholder).into(viewHolder.receiveImage);
                viewHolder.receiveImage.setVisibility(View.VISIBLE);
            }else{
                viewHolder.receiveImage.setVisibility(View.GONE);
                viewHolder.receiveMessage.setVisibility(View.VISIBLE);
                viewHolder.receiveMessage.setText(message.getMessage());
            }
            viewHolder.receiveMessageTime.setText(message.getTimeStamp());

            if (position == messages.size()-1)
            {
                if (message.isIsseen())
                {
                    ((ReceiveViewHolder) holder).isSeen.setText("seen");
                }else
                {
                    ((ReceiveViewHolder) holder).isSeen.setText("Delivered");
                }
            }else
            {
                ((ReceiveViewHolder) holder).isSeen.setVisibility(View.GONE);
            }
        }

        holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {

                Toast.makeText(context, "Long Clicked..", Toast.LENGTH_SHORT).show();
                return false;
            }
        });


    }

    @Override
    public int getItemCount() {
        return messages.size();
    }

    public static class SentViewHolder extends RecyclerView.ViewHolder{

        TextView sentMessage,sentMessageTime,isSeen;
        ImageView sentImage;
        public SentViewHolder(@NonNull View itemView) {
            super(itemView);

            sentMessage = itemView.findViewById(R.id.sent_message);
            sentMessageTime = itemView.findViewById(R.id.sent_message_time);
            isSeen = itemView.findViewById(R.id.message_seen);
            sentImage = itemView.findViewById(R.id.sent_image);

        }
    }

    public void dialog()
    {
        new AlertDialog.Builder(context)
                .setTitle("Do you to remove message")
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int whichButton) {
                    }})
                .setNegativeButton(android.R.string.no, null).show();
    }

    public static class ReceiveViewHolder extends RecyclerView.ViewHolder{

        TextView receiveMessage,receiveMessageTime,isSeen;
        ImageView receiveImage;
        public ReceiveViewHolder(@NonNull View itemView) {
            super(itemView);

            receiveMessage = itemView.findViewById(R.id.receive_message);
            isSeen = itemView.findViewById(R.id.message_seen);
            receiveMessageTime = itemView.findViewById(R.id.receive_message_time);
            receiveImage = itemView.findViewById(R.id.receive_image);
        }
    }
}
