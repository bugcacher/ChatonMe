package com.deathalurer.chat.Adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.deathalurer.chat.ChatActivity;
import com.deathalurer.chat.CircleTransform;
import com.deathalurer.chat.R;
import com.quickblox.chat.model.QBChatDialog;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

/**
 * Created by Abhinav Singh on 29,February,2020
 */
public class ChatListAdapter extends RecyclerView.Adapter<ChatListAdapter.ChatListViewHolder> {

    private ArrayList<QBChatDialog> mList;
    private Context mcontext;

    public ChatListAdapter(ArrayList<QBChatDialog> mList, Context mcontext) {
        this.mList = mList;
        this.mcontext = mcontext;
    }


    @NonNull
    @Override
    public ChatListViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view;
        LayoutInflater layoutInflater = LayoutInflater.from(mcontext);
        view = layoutInflater.inflate(R.layout.chat_list_item,parent,false);
        return new ChatListViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ChatListViewHolder holder, final int position) {
        holder.textView.setText(mList.get(position).getName());
        holder.layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(mcontext, ChatActivity.class);
                intent.putExtra("QBDialog",mList.get(position));
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                mcontext.startActivity(intent);
            }
        });
        //Picasso.get().load(R.drawable.ic_person_black_24dp).transform(new CircleTransform()).into(holder.imageView);
    }

    @Override
    public int getItemCount() {
        return mList.size();
    }

    public class ChatListViewHolder extends RecyclerView.ViewHolder{
        private TextView textView;
        private ImageView imageView;
        private RelativeLayout layout;

        public ChatListViewHolder(@NonNull View itemView) {
            super(itemView);
            textView = itemView.findViewById(R.id.chatUserName);
            imageView = itemView.findViewById(R.id.chatUserImage);
            layout = itemView.findViewById(R.id.layout_all_chat);
        }
    }
}
