package com.deathalurer.chat.Adapter;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.deathalurer.chat.AllChats;
import com.deathalurer.chat.ChatActivity;
import com.deathalurer.chat.CircleTransform;
import com.deathalurer.chat.Profile;
import com.deathalurer.chat.R;
import com.quickblox.chat.QBRestChatService;
import com.quickblox.chat.model.QBChatDialog;
import com.quickblox.content.QBContent;
import com.quickblox.content.model.QBFile;
import com.quickblox.core.QBEntityCallback;
import com.quickblox.core.exception.QBResponseException;
import com.quickblox.users.QBUsers;
import com.quickblox.users.model.QBUser;
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
    public void onBindViewHolder(@NonNull final ChatListViewHolder holder, final int position) {
        holder.textView.setText(mList.get(position).getName());
        holder.lastMessage.setText(mList.get(position).getLastMessage());
        int count = mList.get(position).getUnreadMessageCount();
        if(count>0)
        {
            holder.unreadCount.setVisibility(View.VISIBLE);
            holder.unreadCount.setText(count+"");
        }
        else
            holder.unreadCount.setVisibility(View.GONE);
        //to load friend image
        int friendId = mList.get(position).getRecipientId();
        QBUsers.getUser(friendId).performAsync(new QBEntityCallback<QBUser>() {
            @Override
            public void onSuccess(QBUser qbUser, Bundle bundle) {
                if(qbUser.getFileId()!=null){
                    int profileID = qbUser.getFileId();
                    QBContent.getFile(profileID).performAsync(new QBEntityCallback<QBFile>() {
                        @Override
                        public void onSuccess(QBFile qbFile, Bundle bundle) {
                            String url = qbFile.getPublicUrl();
                            Glide.with(mcontext).load(url).apply(RequestOptions.circleCropTransform()).into(holder.userImage);
                        }

                        @Override
                        public void onError(QBResponseException e) {
                            holder.userImage.setImageResource(R.drawable.ic_person_black_24dp);
                        }
                    });
                }
                else {
                    holder.userImage.setImageResource(R.drawable.ic_person_black_24dp);
                }
            }

            @Override
            public void onError(QBResponseException e) {
                holder.userImage.setImageResource(R.drawable.ic_person_black_24dp);
            }
        });

        holder.layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(mcontext, ChatActivity.class);
                intent.putExtra("QBDialog",mList.get(position));
                Log.d("_______", "onClick: "+ mList.get(position).getUserId()+
                        mList.get(position).getName() + mList.get(position).getRecipientId()
                +mList.get(position).getName());
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                mcontext.startActivity(intent);
            }
        });

        holder.layout.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {

                PopupMenu popupMenu = new PopupMenu(mcontext,holder.layout);
                MenuInflater menuInflater = popupMenu.getMenuInflater();
                menuInflater.inflate(R.menu.delete_dialog,popupMenu.getMenu());
                popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem menuItem) {
                        switch (menuItem.getItemId()){
                            case R.id.deleteDialog:
                                QBRestChatService.deleteDialog(mList.get(position).getDialogId(),false)
                                        .performAsync(new QBEntityCallback<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid, Bundle bundle) {
                                                mList.remove(position);
                                                notifyDataSetChanged();
                                                Toast.makeText(mcontext,"Deleted",Toast.LENGTH_SHORT).show();
                                            }
                                            @Override
                                            public void onError(QBResponseException e) {

                                            }
                                        });
                                break;
                        }
                        return false;
                    }
                });
                popupMenu.show();
                return true;
            }
        });


        //Picasso.get().load(R.drawable.ic_person_black_24dp).transform(new CircleTransform()).into(holder.imageView);
    }

    @Override
    public int getItemCount() {
        return mList.size();
    }

    public class ChatListViewHolder extends RecyclerView.ViewHolder{
        private TextView textView,lastMessage,unreadCount;
        private ImageView userImage;
        private RelativeLayout layout;

        public ChatListViewHolder(@NonNull View itemView) {
            super(itemView);
            textView = itemView.findViewById(R.id.chatUserName);
            userImage = itemView.findViewById(R.id.chatUserImage);
            layout = itemView.findViewById(R.id.layout_all_chat);
            unreadCount = itemView.findViewById(R.id.unreadCount);
            lastMessage = itemView.findViewById(R.id.lastMessage);
        }
    }
}
