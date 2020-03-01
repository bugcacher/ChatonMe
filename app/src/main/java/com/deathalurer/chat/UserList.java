package com.deathalurer.chat;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.deathalurer.chat.Adapter.ListUserAdapter;
import com.quickblox.chat.QBChatService;
import com.quickblox.chat.QBRestChatService;
import com.quickblox.chat.model.QBChatDialog;
import com.quickblox.chat.model.QBDialogType;
import com.quickblox.chat.utils.DialogUtils;
import com.quickblox.core.QBEntityCallback;
import com.quickblox.core.exception.QBResponseException;
import com.quickblox.users.QBUsers;
import com.quickblox.users.model.QBUser;


import java.util.ArrayList;

public class UserList extends AppCompatActivity implements ListUserAdapter.AddUser {

    private RecyclerView recyclerView;
    private Button addUsers;
    private ArrayList<QBUser> userSelectedList = new ArrayList<>();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_list);

        recyclerView = findViewById(R.id.listUserRecyclerView);
        addUsers = findViewById(R.id.createChat);

        getUsers();

        addUsers.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int count = userSelectedList.size();
                if(count==1)
                {
                    createOneToOneChat();
                }
                else if(count>1)
                {
                    createGroupChat();
                }
                else
                {
                    Toast.makeText(getBaseContext(),"Select atlest one",Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void createOneToOneChat() {
        final ProgressDialog dialog = new ProgressDialog(UserList.this);
        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(false);
        dialog.show();

        QBUser user = userSelectedList.get(0);
        Log.d("_________","0 th position name :"+ user.getFullName());
        QBChatDialog userDialog = DialogUtils.buildPrivateDialog(user.getId());

        QBRestChatService.createChatDialog(userDialog).performAsync(new QBEntityCallback<QBChatDialog>() {
            @Override
            public void onSuccess(QBChatDialog qbChatDialog, Bundle bundle) {
                dialog.dismiss();
                Toast.makeText(getBaseContext(),"Successfully created",Toast.LENGTH_LONG).show();
                finish();
            }

            @Override
            public void onError(QBResponseException e) {

            }
        });


    }

    private void createGroupChat() {
        final ProgressDialog dialog = new ProgressDialog(UserList.this);
        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(false);
        dialog.show();
        ArrayList<Integer> usersIdList = new ArrayList<>();
        for(QBUser user : userSelectedList)
        {
            usersIdList.add(user.getId());
        }
        QBChatDialog chatDialog = new QBChatDialog();
        chatDialog.setName("Group: "+userSelectedList.get(0).getFullName()+","+userSelectedList.get(1).getFullName());
        chatDialog.setOccupantsIds(usersIdList);
        chatDialog.setType(QBDialogType.GROUP);

        QBRestChatService.createChatDialog(chatDialog).performAsync(new QBEntityCallback<QBChatDialog>() {
            @Override
            public void onSuccess(QBChatDialog qbChatDialog, Bundle bundle) {
                dialog.dismiss();
                Toast.makeText(getBaseContext(),"Group created successfully",Toast.LENGTH_SHORT).show();
                finish();
            }

            @Override
            public void onError(QBResponseException e) {

            }
        });
    }

    private void getUsers() {
        QBUsers.getUsers(null).performAsync(new QBEntityCallback<ArrayList<QBUser>>() {
            @Override
            public void onSuccess(ArrayList<QBUser> qbUsers, Bundle bundle) {
                ArrayList<QBUser> userList = new ArrayList<>();
                for(QBUser user : qbUsers)
                {
                    if(!user.getLogin().equals(QBChatService.getInstance().getUser().getLogin()))
                    {
                        userList.add(user);
                    }
                }
                recyclerView.setLayoutManager(new LinearLayoutManager(UserList.this));
                recyclerView.setHasFixedSize(true);
                ListUserAdapter adapter = new ListUserAdapter(getBaseContext(),userList,UserList.this);
                recyclerView.setAdapter(adapter);
            }

            @Override
            public void onError(QBResponseException e) {

            }
        });


    }

    @Override
    public void UserSelected(QBUser user1) {
        Log.d("______","In Activity implementing interface");
        if(userSelectedList.contains(user1))
        {
            userSelectedList.remove(user1);
        }
        else
            userSelectedList.add(user1);

        Log.d("______","added user name :" + user1.getFullName());
    }
}
