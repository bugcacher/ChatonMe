package com.deathalurer.chat.Adapter;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.deathalurer.chat.R;
import com.quickblox.chat.QBChatService;
import com.quickblox.chat.QBRestChatService;
import com.quickblox.chat.model.QBChatMessage;
import com.quickblox.chat.model.QBDialogType;
import com.quickblox.content.QBContent;
import com.quickblox.content.model.QBFile;
import com.quickblox.core.QBEntityCallback;
import com.quickblox.core.exception.QBResponseException;
import com.quickblox.users.QBUsers;
import com.quickblox.users.model.QBUser;

import java.util.ArrayList;

/**
 * Created by Abhinav Singh on 05,March,2020
 */
public class QBChatListAdapter extends RecyclerView.Adapter {

    private Context mContext;
    private ArrayList<QBChatMessage> qbChatMessageArrayList;
    private static final int Sent = 1;
    private static  final int Received = 2;
    private QBDialogType dialogType;

    public QBChatListAdapter(Context mContext, ArrayList<QBChatMessage> qbChatMessageArrayList,QBDialogType dialogType) {
        this.mContext = mContext;
        this.qbChatMessageArrayList = qbChatMessageArrayList;
        this.dialogType = dialogType;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view;
        LayoutInflater layoutInflater = LayoutInflater.from(mContext);
        if (viewType == 1)
        {
            view =  layoutInflater.inflate(R.layout.sender_layout,parent,false);
            return new SenderViewHolder(view);
        }
        else
        {
            view =  layoutInflater.inflate(R.layout.reciever_layout,parent,false);
            return new ReceiverViewHolder(view);
        }

    }

    @Override
    public void onBindViewHolder(@NonNull final RecyclerView.ViewHolder holder, final int position) {

        QBChatMessage message = qbChatMessageArrayList.get(position);
        switch (holder.getItemViewType()){
            case Sent :
                ((SenderViewHolder)holder).bind(message);
                ((SenderViewHolder)holder).senderMessage.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View view) {
                        PopupMenu popupMenu = new PopupMenu(mContext,((SenderViewHolder)holder).senderMessage);
                        MenuInflater menuInflater = popupMenu.getMenuInflater();
                        menuInflater.inflate(R.menu.message_options,popupMenu.getMenu());
                        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                            @Override
                            public boolean onMenuItemClick(MenuItem menuItem) {
                                switch (menuItem.getItemId()){
                                    case R.id.delete:
                                        String s = qbChatMessageArrayList.get(position).getId();
                                        Log.d("_____","message id : " +
                                                qbChatMessageArrayList.get(position).getId());
                                        QBRestChatService.deleteMessage(s,false)
                                                .performAsync(new QBEntityCallback<Void>() {
                                                    @Override
                                                    public void onSuccess(Void aVoid, Bundle bundle) {
                                                        Toast.makeText(mContext,"Deleted",Toast.LENGTH_SHORT).show();
                                                        qbChatMessageArrayList.remove(position);
                                                        notifyDataSetChanged();
                                                    }

                                                    @Override
                                                    public void onError(QBResponseException e) {

                                                    }
                                                });
                                        break;
                                    case R.id.deleteForAll :
                                        String sid = qbChatMessageArrayList.get(position).getId();
                                        Log.d("_____","message id : " +
                                                qbChatMessageArrayList.get(position).getId());
                                        QBRestChatService.deleteMessage(sid,true)
                                                .performAsync(new QBEntityCallback<Void>() {
                                                    @Override
                                                    public void onSuccess(Void aVoid, Bundle bundle) {
                                                        Toast.makeText(mContext,"Deleted",Toast.LENGTH_SHORT).show();
                                                        qbChatMessageArrayList.remove(position);
                                                        notifyDataSetChanged();
                                                    }

                                                    @Override
                                                    public void onError(QBResponseException e) {

                                                    }
                                                });
                                        break;
                                    default:
                                        return false;
                                }
                                return false;
                            }
                        });
                        popupMenu.show();
                        return true;
                    }
                });
                break;
            case Received :
                ((ReceiverViewHolder)holder).bind(message);

                break;
        }
    }

    @Override
    public int getItemCount() {
        return qbChatMessageArrayList.size();
    }

    @Override
    public int getItemViewType(int position) {
        int id = qbChatMessageArrayList.get(position).getSenderId();
        if(id == QBChatService.getInstance().getUser().getId()){
         return Sent;
        }
        else{
            return Received;
        }
    }

    class SenderViewHolder extends RecyclerView.ViewHolder {
        private TextView senderMessage;
        public SenderViewHolder(@NonNull View itemView) {
            super(itemView);
            senderMessage = itemView.findViewById(R.id.my_message);
        }
        void bind(QBChatMessage message){
            senderMessage.setText(message.getBody());
//            senderMessage.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View view) {
//                    Toast.makeText(mContext,"YEs",Toast.LENGTH_SHORT).show();
//                }
//            });
        }
    }

    class ReceiverViewHolder extends RecyclerView.ViewHolder {
        private TextView receiverMessage;
           private TextView name;
//        private ImageView userImage;
        public ReceiverViewHolder(@NonNull View itemView) {
            super(itemView);
            receiverMessage = itemView.findViewById(R.id.receiver_message_body);
            name = itemView.findViewById(R.id.ReceiverName);
//            userImage = itemView.findViewById(R.id.receiverAvatar);
        }
        void bind(QBChatMessage message){
            if(dialogType==QBDialogType.GROUP || dialogType == QBDialogType.PUBLIC_GROUP){
                name.setVisibility(View.VISIBLE);
                QBUsers.getUser(message.getSenderId()).performAsync(new QBEntityCallback<QBUser>() {
                    @Override
                    public void onSuccess(QBUser qbUser, Bundle bundle) {
                        name.setText(qbUser.getFullName());
                    }

                    @Override
                    public void onError(QBResponseException e) {
                        name.setVisibility(View.GONE);
                    }
                });
            }
            receiverMessage.setText(message.getBody());
        }
    }


}
