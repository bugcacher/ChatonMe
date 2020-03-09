package com.deathalurer.chat;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.deathalurer.chat.Adapter.ChatListAdapter;
import com.deathalurer.chat.Adapter.QBChatListAdapter;
import com.quickblox.chat.QBChatService;
import com.quickblox.chat.QBIncomingMessagesManager;
import com.quickblox.chat.QBRestChatService;
import com.quickblox.chat.exception.QBChatException;
import com.quickblox.chat.listeners.QBChatDialogMessageListener;
import com.quickblox.chat.model.QBChatDialog;
import com.quickblox.chat.model.QBChatMessage;
import com.quickblox.chat.model.QBDialogType;
import com.quickblox.chat.request.QBMessageGetBuilder;
import com.quickblox.core.QBEntityCallback;
import com.quickblox.core.exception.QBResponseException;

import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smackx.muc.DiscussionHistory;

import java.util.ArrayList;

public class ChatActivity extends AppCompatActivity implements QBChatDialogMessageListener {
    RecyclerView recyclerView;
    ImageView sendButton;
    ImageView attachment;
    EditText content;
    QBChatDialog chatDialog;
    QBChatMessage message;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_activty);
        sendButton = findViewById(R.id.sendButton);
        attachment = findViewById(R.id.attachmentButton);
        content = findViewById(R.id.messageTypeTextView);
        recyclerView = findViewById(R.id.recyclerViewMessageList);


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

    private void retrieveMessage() {
        QBMessageGetBuilder builder = new QBMessageGetBuilder();
        builder.setLimit(300);

        if(chatDialog!=null){

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
