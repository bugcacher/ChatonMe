package com.deathalurer.chat;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.deathalurer.chat.Adapter.ListUserAdapter;
import com.facebook.shimmer.ShimmerFrameLayout;
import com.quickblox.chat.QBChatService;
import com.quickblox.chat.QBRestChatService;
import com.quickblox.chat.model.QBChatDialog;
import com.quickblox.chat.request.QBDialogRequestBuilder;
import com.quickblox.core.QBEntityCallback;
import com.quickblox.core.exception.QBResponseException;
import com.quickblox.core.request.QBPagedRequestBuilder;
import com.quickblox.core.request.QBRequestBuilder;
import com.quickblox.users.QBUsers;
import com.quickblox.users.model.QBUser;

import java.util.ArrayList;

import de.measite.minidns.record.A;

public class Edit_Group_Member extends AppCompatActivity implements ListUserAdapter.AddUser {

    RecyclerView recyclerView;
    ArrayList<FriendList> currentUsers = new ArrayList<>();
    ArrayList<QBUser> toBeAddedUsers = new ArrayList<>();;
    ArrayList<QBUser> toBeRemovedUsers = new ArrayList<>();;
    Button editMembers ;
    QBChatDialog dialog;
    ListUserAdapter adapter;
    ArrayList<Integer> currentOccupants = new ArrayList<>();
    ArrayList<String> phoneNumbers;
    String mode = "";
    ShimmerFrameLayout shimmerFrameLayout;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_list);

        editMembers = findViewById(R.id.createChat);
        recyclerView = findViewById(R.id.listUserRecyclerView);
        shimmerFrameLayout = findViewById(R.id.shimmerLayoutUserList);
        shimmerFrameLayout.startShimmer();

        dialog =(QBChatDialog) getIntent().getSerializableExtra("chatDialog");
        mode = getIntent().getStringExtra(ChatActivity.MODE);

        recyclerView.setLayoutManager(new LinearLayoutManager(Edit_Group_Member.this));
        adapter = new ListUserAdapter(getBaseContext(),currentUsers,Edit_Group_Member.this);
        recyclerView.setAdapter(adapter);

        if(mode.equals("remove")){
            editMembers.setText("Remove");
            getCurrentGroupUsers();
        }
        else{
            editMembers.setText("Add");
            getOtherContacts();
        }



        editMembers.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mode.equals("remove"))
                {
                    if(toBeRemovedUsers.size()<1)
                        Toast.makeText(getBaseContext(),"Select atleast one",Toast.LENGTH_SHORT).show();
                    else
                        removeUsersFromGroup();
                }
                else{
                    if(toBeAddedUsers.size()<1)
                        Toast.makeText(getBaseContext(),"Select atleast one",Toast.LENGTH_SHORT).show();
                    else
                        addUsersToGroup();
                }
            }
       });
    }

    private void getOtherContacts() {

        QBRestChatService.getChatDialogById(dialog.getDialogId()).performAsync(new QBEntityCallback<QBChatDialog>() {
            @Override
            public void onSuccess(QBChatDialog qbChatDialog, Bundle bundle) {

                currentOccupants = (ArrayList<Integer>) qbChatDialog.getOccupants();
                Log.e("_____", "onSuccess: " + currentOccupants.size() );

                QBPagedRequestBuilder builder = new QBPagedRequestBuilder();
                builder.setPage(1);
                builder.setPerPage(50);

                QBUsers.getUsersByIDs(currentOccupants,builder).performAsync(new QBEntityCallback<ArrayList<QBUser>>() {
                    @Override
                    public void onSuccess(ArrayList<QBUser> qbUsers, Bundle bundle) {
                        getAllContacts(qbUsers);
                    }

                    @Override
                    public void onError(QBResponseException e) {

                    }
                });
            }
            @Override
            public void onError(QBResponseException e) {
            }
        });

    }

    private void getCurrentGroupUsers() {
        QBRestChatService.getChatDialogById(dialog.getDialogId()).performAsync(new QBEntityCallback<QBChatDialog>() {
            @Override
            public void onSuccess(QBChatDialog qbChatDialog, Bundle bundle) {

                currentOccupants = (ArrayList<Integer>) qbChatDialog.getOccupants();
                Log.e("_____", "onSuccess: " + currentOccupants.size() );

                QBPagedRequestBuilder builder = new QBPagedRequestBuilder();
                builder.setPage(1);
                builder.setPerPage(50);

                QBUsers.getUsersByIDs(currentOccupants,builder).performAsync(new QBEntityCallback<ArrayList<QBUser>>() {
                    @Override
                    public void onSuccess(ArrayList<QBUser> qbUsers, Bundle bundle) {
                        ArrayList<FriendList> myList = new ArrayList<>();
                        for(QBUser user:qbUsers){
                            myList.add(new FriendList(user,false));
                        }

                        adapter = new ListUserAdapter(getBaseContext(),myList,Edit_Group_Member.this);
                        recyclerView.setAdapter(adapter);
                        adapter.notifyDataSetChanged();
                        shimmerFrameLayout.stopShimmer();
                        shimmerFrameLayout.setVisibility(View.GONE);
                    }

                    @Override
                    public void onError(QBResponseException e) {

                    }
                });
            }
            @Override
            public void onError(QBResponseException e) {
            }
        });

    }

    @Override
    public void UserSelected(FriendList friendUser) {
        if (mode.equals("remove")){
            if(toBeRemovedUsers.contains(friendUser.getUser()))
            {
                 toBeRemovedUsers.remove(friendUser.getUser());
            }
            else
                toBeRemovedUsers.add(friendUser.getUser());
        }
        else {
            if(toBeAddedUsers.contains(friendUser.getUser()))
                {
                   toBeAddedUsers.remove(friendUser.getUser());
                }
            else
                toBeAddedUsers.add(friendUser.getUser());
        }
    }

    private void getAllContacts(ArrayList<QBUser> currentUserList) {
        phoneNumbers = new ArrayList<>();
        ContentResolver cr = getContentResolver();
        Cursor cur = cr.query(ContactsContract.Contacts.CONTENT_URI,
                null, null, null, null);
        if ((cur != null ? cur.getCount() : 0) > 0) {
            while (cur != null && cur.moveToNext()) {
                String id = cur.getString(
                        cur.getColumnIndex(ContactsContract.Contacts._ID));
                if (cur.getInt(cur.getColumnIndex( ContactsContract.Contacts.HAS_PHONE_NUMBER)) > 0) {
                    Cursor pCur = cr.query(
                            ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                            null,
                            ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?",
                            new String[]{id}, null);
                    while (pCur.moveToNext()) {
                        String phoneNo = pCur.getString(pCur.getColumnIndex(
                                ContactsContract.CommonDataKinds.Phone.NUMBER));
                        phoneNumbers.add(phoneNo);
                        Log.e("UserList0","phone number:"+phoneNo+"\n");
                    }
                    pCur.close();
                }
            }
        }
        if (cur != null) {
            cur.close();
        }

        getUsers(currentUserList);

    }

    private void getUsers(final ArrayList<QBUser> currentUserList) {
        QBPagedRequestBuilder pagedRequestBuilder = new QBPagedRequestBuilder();
        pagedRequestBuilder.setPage(1);
        pagedRequestBuilder.setPerPage(50);

        QBUsers.getUsers(pagedRequestBuilder).performAsync(new QBEntityCallback<ArrayList<QBUser>>() {
            @Override
            public void onSuccess(ArrayList<QBUser> qbUsers, Bundle bundle) {
                ArrayList<QBUser> list = new ArrayList<>();
                for(QBUser user : qbUsers){
                    if (phoneNumbers.contains(user.getPhone())){
                        list.add(user);
                    }
                }

                for (int i = 0; i <list.size() ; i++) {
                    Log.e("UserList","Users in phone contacts "+ list.get(i).getFullName()+"\n");
                }

                for (int i = 0;i<currentUserList.size();i++){
                    if (list.contains(currentUserList.get(i))){
                        list.remove(currentUserList.get(i));
                    }
                }

                for (int i = 0; i <list.size() ; i++) {
                    Log.e("UserList","User other than current group: "+ list.get(i).getFullName()+"\n");
                }

                ArrayList<FriendList> newList = new ArrayList<>();
                for(QBUser user : list){
                    newList.add(new FriendList(user,false));
                }

                adapter = new ListUserAdapter(getBaseContext(), newList, Edit_Group_Member.this);
                recyclerView.setAdapter(adapter);
                adapter.notifyDataSetChanged();
                shimmerFrameLayout.stopShimmer();
                shimmerFrameLayout.setVisibility(View.GONE);
            }

            @Override
            public void onError(QBResponseException e) {

            }
        });
    }

    void addUsersToGroup(){
        Log.e("________", "onClick: size"+toBeAddedUsers.size());
        QBDialogRequestBuilder builder = new QBDialogRequestBuilder();

        for (int i = 0; i < toBeAddedUsers.size(); i++) {
            builder.addUsers(toBeAddedUsers.get(i));
        }

        QBRestChatService.updateGroupChatDialog(dialog, builder).performAsync(new QBEntityCallback<QBChatDialog>() {
            @Override
            public void onSuccess(QBChatDialog qbChatDialog, Bundle bundle) {
                Log.e("", "onSuccess: " + "Added Successfully");
                finish();
            }

            @Override
            public void onError(QBResponseException e) {
                Log.e("______", "onError: " + e.getMessage());
            }
        });

    }
    void removeUsersFromGroup(){
        Log.e("________", "onClick: size"+toBeRemovedUsers.size());
        QBDialogRequestBuilder builder = new QBDialogRequestBuilder();

        for (int i = 0; i < toBeRemovedUsers.size(); i++) {
            builder.removeUsers(toBeRemovedUsers.get(i));
        }
        QBRestChatService.updateGroupChatDialog(dialog, builder).performAsync(new QBEntityCallback<QBChatDialog>() {
            @Override
            public void onSuccess(QBChatDialog qbChatDialog, Bundle bundle) {
                Log.e("", "onSuccess: " + "Removed Successfully");
                finish();
            }

            @Override
            public void onError(QBResponseException e) {
                Log.e("______", "onError: " + e.getMessage());
            }
        });
    }

}
