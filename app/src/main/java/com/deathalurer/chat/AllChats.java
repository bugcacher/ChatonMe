package com.deathalurer.chat;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.deathalurer.chat.Adapter.ChatListAdapter;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.quickblox.auth.QBAuth;
import com.quickblox.auth.session.BaseService;
import com.quickblox.auth.session.QBSession;
import com.quickblox.chat.QBChatService;
import com.quickblox.chat.QBRestChatService;
import com.quickblox.chat.model.QBChatDialog;
import com.quickblox.core.QBEntityCallback;
import com.quickblox.core.exception.BaseServiceException;
import com.quickblox.core.exception.QBResponseException;
import com.quickblox.core.request.QBRequestBuilder;
import com.quickblox.core.request.QBRequestGetBuilder;
import com.quickblox.users.QBUsers;
import com.quickblox.users.model.QBUser;

import java.util.ArrayList;

public class AllChats extends AppCompatActivity {
    private RecyclerView recyclerView;
    private ChatListAdapter adapter;
    private FloatingActionButton floatingActionButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);
        recyclerView = findViewById(R.id.friendListRecyclerView);
        floatingActionButton = findViewById(R.id.floatingAddChatUser);

        createSession();
        loadChats();

        floatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getBaseContext(),UserList.class);
                startActivity(intent);
            }
        });
    }

    private void loadChats() {
        QBRequestGetBuilder requestBuilder = new QBRequestGetBuilder();
        requestBuilder.setLimit(50);
        QBRestChatService.getChatDialogs(null,requestBuilder).performAsync(new QBEntityCallback<ArrayList<QBChatDialog>>() {
            @Override
            public void onSuccess(ArrayList<QBChatDialog> qbChatDialogs, Bundle bundle) {
                for(int i =0 ; i<qbChatDialogs.size();i++){
                    Log.d("______","name"+qbChatDialogs.get(i).getName()+"\n");

                }
                recyclerView.setLayoutManager(new LinearLayoutManager(getBaseContext()));
                adapter = new ChatListAdapter(qbChatDialogs,getBaseContext());
                recyclerView.setAdapter(adapter);
                recyclerView.setHasFixedSize(true);
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onError(QBResponseException e) {

            }
        });
    }

    private void createSession() {
        final ProgressDialog dialog = new ProgressDialog(AllChats.this);
        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(false);
        dialog.show();

        String email = getIntent().getStringExtra("email");
        String pass = getIntent().getStringExtra("pass");

        final QBUser user = new QBUser(email,pass);
        QBAuth.createSession(user).performAsync(new QBEntityCallback<QBSession>() {
            @Override
            public void onSuccess(QBSession qbSession, Bundle bundle) {
                user.setId(qbSession.getUserId());
                try {
                    user.setPassword(BaseService.getBaseService().getToken());
                } catch (BaseServiceException e) {
                    e.printStackTrace();
                }
                QBChatService.getInstance().login(user, new QBEntityCallback() {
                    @Override
                    public void onSuccess(Object o, Bundle bundle) {
                        dialog.dismiss();
                    }
                    @Override
                    public void onError(QBResponseException e) {
                        Toast.makeText(getBaseContext(),"Error :"+e.getMessage(),Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onError(QBResponseException e) {

            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadChats();
    }
}
