package com.deathalurer.chat;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.deathalurer.chat.Adapter.ChatListAdapter;
import com.deathalurer.chat.Adapter.QBChatListAdapter;
import com.quickblox.chat.QBChatService;
import com.quickblox.chat.QBIncomingMessagesManager;
import com.quickblox.chat.QBRestChatService;
import com.quickblox.chat.QBRoster;
import com.quickblox.chat.exception.QBChatException;
import com.quickblox.chat.listeners.QBChatDialogMessageListener;
import com.quickblox.chat.listeners.QBChatDialogParticipantListener;
import com.quickblox.chat.listeners.QBRosterListener;
import com.quickblox.chat.listeners.QBSubscriptionListener;
import com.quickblox.chat.model.QBChatDialog;
import com.quickblox.chat.model.QBChatMessage;
import com.quickblox.chat.model.QBDialogType;
import com.quickblox.chat.model.QBPresence;
import com.quickblox.chat.model.QBRosterEntry;
import com.quickblox.chat.request.QBMessageGetBuilder;
import com.quickblox.content.QBContent;
import com.quickblox.content.model.QBFile;
import com.quickblox.core.QBEntityCallback;
import com.quickblox.core.exception.QBResponseException;
import com.quickblox.users.QBUsers;
import com.quickblox.users.model.QBUser;

import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.roster.Roster;
import org.jivesoftware.smackx.muc.DiscussionHistory;

import java.util.ArrayList;
import java.util.Collection;

public class ChatActivity extends AppCompatActivity implements QBChatDialogMessageListener {
    RecyclerView recyclerView;
    ImageView sendButton;
    ImageView attachment;
    EditText content;
    QBChatDialog chatDialog;
    QBChatMessage message;
    TextView friendName;
    ImageView back,isOnline,friendImage;
    QBRoster chatRoster;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_activty);
        sendButton = findViewById(R.id.sendButton);
        attachment = findViewById(R.id.attachmentButton);
        content = findViewById(R.id.messageTypeTextView);
        recyclerView = findViewById(R.id.recyclerViewMessageList);

        Toolbar toolbar = findViewById(R.id.toolBar);
        setSupportActionBar(toolbar);

        friendName = findViewById(R.id.toolbarName);
        isOnline = findViewById(R.id.toolBarOnline);
        back = findViewById(R.id.toolbarBack);
        friendImage = findViewById(R.id.toolbarFriend);

        //getFriendImage();
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });

        initChatDialog();

        retrieveMessage();

        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                message = new QBChatMessage();
                message.setBody(content.getText().toString());
                message.setSenderId(QBChatService.getInstance().getUser().getId());
                message.setSaveToHistory(true);

                try {
                    chatDialog.sendMessage(message);
                } catch (SmackException.NotConnectedException e) {
                    e.printStackTrace();
                }
                if (chatDialog.getType()==QBDialogType.PRIVATE)
                {
                    QBChatHolder.getInstance().putMessage(chatDialog.getDialogId(),message);
                    ArrayList<QBChatMessage> messages = QBChatHolder.getInstance()
                            .getChatMessageByDialogId(chatDialog.getDialogId());
                    recyclerView.setLayoutManager(new LinearLayoutManager(getBaseContext()));
                    QBChatListAdapter adapter = new QBChatListAdapter(getBaseContext(),messages);
                    recyclerView.setAdapter(adapter);
                    adapter.notifyDataSetChanged();
                    recyclerView.scrollToPosition(adapter.getItemCount()-1);
                }

                /////////
//                QBChatHolder.getInstance().putMessage(chatDialog.getDialogId(),message);
//                ArrayList<QBChatMessage> messages = QBChatHolder.getInstance()
//                        .getChatMessageByDialogId(chatDialog.getDialogId());
//                recyclerView.setLayoutManager(new LinearLayoutManager(getBaseContext()));
//                QBChatListAdapter adapter = new QBChatListAdapter(getBaseContext(),messages);
//                recyclerView.setAdapter(adapter);
//                adapter.notifyDataSetChanged();
//                recyclerView.scrollToPosition(adapter.getItemCount()-1);
                content.setText("");
                content.setFocusable(true);

            }
        });
    }

    private void getFriendImage() {
        QBUsers.getUser(chatDialog.getRecipientId()).performAsync(new QBEntityCallback<QBUser>() {
            @Override
            public void onSuccess(QBUser qbUser, Bundle bundle) {
                if(qbUser.getFileId()!=null){
                    int profileID = qbUser.getFileId();
                    QBContent.getFile(profileID).performAsync(new QBEntityCallback<QBFile>() {
                        @Override
                        public void onSuccess(QBFile qbFile, Bundle bundle) {
                            String url = qbFile.getPublicUrl();
                            Glide.with(ChatActivity.this).load(url).apply(RequestOptions.circleCropTransform()).into(friendImage);
                        }
                        @Override
                        public void onError(QBResponseException e) {
                            friendImage.setImageResource(R.drawable.ic_person_black_24dp);
                        }
                    });
                }
                else {
                    friendImage.setImageResource(R.drawable.ic_person_black_24dp);
                }
            }

            @Override
            public void onError(QBResponseException e) {
                friendImage.setImageResource(R.drawable.ic_person_black_24dp);
            }
        });
    }

    private void retrieveMessage() {
        QBMessageGetBuilder builder = new QBMessageGetBuilder();
        builder.setLimit(300);

        if(chatDialog!=null){
            friendName.setText(chatDialog.getName());

            QBRestChatService.getDialogMessages(chatDialog,builder)
                    .performAsync(new QBEntityCallback<ArrayList<QBChatMessage>>() {
                        @Override
                        public void onSuccess(ArrayList<QBChatMessage> qbChatMessages, Bundle bundle) {

                            QBChatHolder.getInstance().putMessages(chatDialog.getDialogId(),qbChatMessages);
                            QBChatListAdapter adapter = new QBChatListAdapter(getBaseContext(),qbChatMessages);
                            recyclerView.setLayoutManager(new LinearLayoutManager(getBaseContext()));
                            recyclerView.setAdapter(adapter);
                            adapter.notifyDataSetChanged();
                            recyclerView.scrollToPosition(adapter.getItemCount()-1);
                        }

                        @Override
                        public void onError(QBResponseException e) {

                        }
                    });
        }
    }

    private void initChatDialog() {
        chatDialog = (QBChatDialog) getIntent().getSerializableExtra("QBDialog");
        chatDialog.initForChat(QBChatService.getInstance());

        //registering listener

        QBIncomingMessagesManager messagesManager = QBChatService.getInstance().getIncomingMessagesManager();
        messagesManager.addDialogMessageListener(new QBChatDialogMessageListener() {
            @Override
            public void processMessage(String s, QBChatMessage qbChatMessage, Integer integer) {

            }

            @Override
            public void processError(String s, QBChatException e, QBChatMessage qbChatMessage, Integer integer) {
                Log.e("Error",""+e.getMessage());
            }
        });



        //group

        if (chatDialog.getType()== QBDialogType.GROUP||chatDialog.getType() == QBDialogType.PUBLIC_GROUP)
        {
            DiscussionHistory discussionHistory = new DiscussionHistory();
            discussionHistory.setMaxStanzas(0);
            chatDialog.join(discussionHistory, new QBEntityCallback() {
                @Override
                public void onSuccess(Object o, Bundle bundle) {

                }

                @Override
                public void onError(QBResponseException e) {

                }
            });
        }
        //private

        chatDialog.addMessageListener(this);
        Log.d("_________", "initChatDialog: " );

        //online feature
        getOnlineStatus();

    }

    private void getOnlineStatus() {
        QBSubscriptionListener subscriptionListener = new QBSubscriptionListener() {
            @Override
            public void subscriptionRequested(int userId) {
                try {
                    if (chatRoster != null)
                        chatRoster.confirmSubscription(userId);
                } catch (SmackException.NotConnectedException e) {

                } catch (SmackException.NotLoggedInException e) {

                } catch (XMPPException e) {

                } catch (SmackException.NoResponseException e) {

                }
            }
        };

        chatRoster = QBChatService.getInstance().getRoster(QBRoster.SubscriptionMode.mutual, subscriptionListener);


        try {
            chatRoster.subscribe(chatDialog.getRecipientId()); //getRecipientId is Opponent UserID
        } catch (SmackException.NotConnectedException e) {
            e.printStackTrace();
        }

        QBPresence presence = new QBPresence(QBPresence.Type.online, "I am now available", 1, QBPresence.Mode.available);
        try {
            chatRoster.sendPresence(presence);
        } catch (SmackException.NotConnectedException e) {

        } catch (Exception e) {
            e.printStackTrace();
        }

        QBPresence presence1 = chatRoster.getPresence(chatDialog.getRecipientId());
        if (presence1.getType() == QBPresence.Type.online) {
            //online
            isOnline.setImageResource(R.drawable.user_online);
        }else{
            //offline
            isOnline.setImageResource(R.drawable.user_ofline);
        }

        QBRosterListener rosterListener = new QBRosterListener() {
            @Override
            public void entriesDeleted(Collection<Integer> userIds) {

            }

            @Override
            public void entriesAdded(Collection<Integer> userIds) {

            }

            @Override
            public void entriesUpdated(Collection<Integer> userIds) {

            }

            @Override
            public void presenceChanged(QBPresence presence1) {
                try {
                    int userIdd = presence1.getUserId();
                    int receiverId = chatDialog.getRecipientId();
                    if (userIdd == receiverId) {

                        if (presence1.getType() == QBPresence.Type.online)
                        {
                            isOnline.setImageResource(R.drawable.user_online);
                        }
                        else {
                            isOnline.setImageResource(R.drawable.user_ofline);
                        }
                    } else {
                    }
                } catch (Exception e) {

                }

            }
        };

        chatRoster.addRosterListener(rosterListener);
    }

    @Override
    public void processMessage(String s, QBChatMessage qbChatMessage, Integer integer) {
        QBChatHolder.getInstance().putMessage(qbChatMessage.getDialogId(),qbChatMessage);
        ArrayList<QBChatMessage> qbChatMessageArrayList = QBChatHolder.getInstance()
                .getChatMessageByDialogId(qbChatMessage.getDialogId());
        recyclerView.setLayoutManager(new LinearLayoutManager(getBaseContext()));
        QBChatListAdapter adapter = new QBChatListAdapter(getBaseContext(),qbChatMessageArrayList);
        recyclerView.setAdapter(adapter);
        adapter.notifyDataSetChanged();
    }

    @Override
    public void processError(String s, QBChatException e, QBChatMessage qbChatMessage, Integer integer) {

    }

    @Override
    protected void onStop() {
        super.onStop();
        chatDialog.removeMessageListrener(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        chatDialog.removeMessageListrener(this);
    }
}
