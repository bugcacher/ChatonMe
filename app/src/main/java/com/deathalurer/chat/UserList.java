package com.deathalurer.chat;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.ContactsContract;
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
import com.quickblox.core.request.QBPagedRequestBuilder;
import com.quickblox.users.QBUsers;
import com.quickblox.users.model.QBUser;


import java.util.ArrayList;
import java.util.HashMap;

public class UserList extends AppCompatActivity implements ListUserAdapter.AddUser, CreateGroupDialog.GroupNameInterface {

    private RecyclerView recyclerView;
    private Button addUsers;
    private ArrayList<FriendList> userSelectedList = new ArrayList<>();
    ArrayList<String> phoneNumbers;
    private String groupName="";
    private static final String TAG ="UserList";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_list);
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.READ_CONTACTS)
                == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.READ_CONTACTS)
                == PackageManager.PERMISSION_GRANTED) {
            getUsers();
        } else {
            requestPermission();
        }
        recyclerView = findViewById(R.id.listUserRecyclerView);
        addUsers = findViewById(R.id.createChat);

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
                    openCreateGroupDialog();
                }
                else
                {
                    Toast.makeText(getBaseContext(),"Select atleast one",Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void openCreateGroupDialog() {
        Log.d(TAG,"on create group dialog");
        CreateGroupDialog groupDialog = new CreateGroupDialog();
        groupDialog.show(getSupportFragmentManager(),"Dialog");
    }

    private void requestPermission() {
        if ((ContextCompat.checkSelfPermission(UserList.this,
                Manifest.permission.READ_CONTACTS)
                != PackageManager.PERMISSION_GRANTED ) &&( ContextCompat.checkSelfPermission(UserList.this,
                Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED ) &&( ContextCompat.checkSelfPermission(UserList.this,
                Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED )){

            // Permission is not granted
            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(UserList.this,
                    Manifest.permission.READ_CONTACTS)) {
                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.
            } else {
                // No explanation needed; request the permission
                ActivityCompat.requestPermissions(UserList.this,
                        new String[]{Manifest.permission.READ_CONTACTS,Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        1);

                // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
                // app-defined int constant. The callback method gets the
                // result of the request.
            }
        } else {
            // Permission has already been granted
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case 1: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.
                    getUsers();
                } else {
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                }
                return;
            }

            // other 'case' lines to check for other
            // permissions this app might request.
        }
    }

    private void getAllContacts() {
        phoneNumbers = new ArrayList<>();
        ContentResolver cr = getContentResolver();
        Cursor cur = cr.query(ContactsContract.Contacts.CONTENT_URI,
                null, null, null, null);
        if ((cur != null ? cur.getCount() : 0) > 0) {
            while (cur != null && cur.moveToNext()) {
                String id = cur.getString(
                        cur.getColumnIndex(ContactsContract.Contacts._ID));
//                String name = cur.getString(cur.getColumnIndex(
//                        ContactsContract.Contacts.DISPLAY_NAME));
//                //nameList.add(name);
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

    }


    private void createOneToOneChat() {
        final ProgressDialog dialog = new ProgressDialog(UserList.this);
        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(false);
        dialog.show();

        QBUser user = userSelectedList.get(0).getUser();
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
        Log.d(TAG,"groupName : " + groupName);
        //openCreateGroupDialog();
        final CustomDialog dialog = new CustomDialog(UserList.this);
        dialog.showDialog();
        ArrayList<Integer> usersIdList = new ArrayList<>();
        for(FriendList user : userSelectedList)
        {
            usersIdList.add(user.getUser().getId());
        }
        QBChatDialog chatDialog = new QBChatDialog();
        //chatDialog.setName("Group: "+userSelectedList.get(0).getUser().getFullName()+","
         //     +userSelectedList.get(1).getUser().getFullName());
        chatDialog.setOccupantsIds(usersIdList);
        chatDialog.setName(groupName);
        chatDialog.setType(QBDialogType.GROUP);

        QBRestChatService.createChatDialog(chatDialog).performAsync(new QBEntityCallback<QBChatDialog>() {
            @Override
            public void onSuccess(QBChatDialog qbChatDialog, Bundle bundle) {
                dialog.hideDialog();
                Toast.makeText(getBaseContext(),"Group created successfully",Toast.LENGTH_SHORT).show();
                finish();
            }
            @Override
            public void onError(QBResponseException e) {
                Log.d(TAG, "onError: "+e.getLocalizedMessage());
            }
        });
    }

    private void getUsers() {
//        getAllContacts();
        QBPagedRequestBuilder pagedRequestBuilder = new QBPagedRequestBuilder();
        pagedRequestBuilder.setPage(1);
        pagedRequestBuilder.setPerPage(50);

        QBUsers.getUsers(pagedRequestBuilder).performAsync(new QBEntityCallback<ArrayList<QBUser>>() {
            @Override
            public void onSuccess(ArrayList<QBUser> qbUsers, Bundle bundle) {
                recyclerView.setLayoutManager(new LinearLayoutManager(UserList.this));
                recyclerView.setHasFixedSize(true);
                ArrayList<FriendList> list = new ArrayList<>();
                for (QBUser user: qbUsers){
                    list.add(new FriendList(user,false));
                }
                ListUserAdapter adapter = new ListUserAdapter(getBaseContext(), list, UserList.this);
                recyclerView.setAdapter(adapter);
            }

            @Override
            public void onError(QBResponseException e) {

            }
        });
//        for (int i = 0; i <phoneNumbers.size() ; i++) {
//            Log.e("UserList","Number: "+ phoneNumbers.get(i)+"\n");
//        }
//        QBUsers.getUsersByPhoneNumbers(phoneNumbers, pagedRequestBuilder).performAsync(new QBEntityCallback<ArrayList<QBUser>>() {
//            @Override
//            public void onSuccess(ArrayList<QBUser> qbUsers, Bundle bundle) {
//                recyclerView.setLayoutManager(new LinearLayoutManager(UserList.this));
//                recyclerView.setHasFixedSize(true);
//                ListUserAdapter adapter = new ListUserAdapter(getBaseContext(), qbUsers, UserList.this);
//                recyclerView.setAdapter(adapter);
//                Toast.makeText(getBaseContext(),"size " + qbUsers.size(),Toast.LENGTH_SHORT).show();
//                for (int i = 0; i < qbUsers.size(); i++) {
//                    Log.e("____","Name " + qbUsers.get(i).getFullName()+"\n");
//                }
//            }
//            @Override
//            public void onError(QBResponseException e) {
//                Toast.makeText(getBaseContext(), "Error: " + e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
//            }
//        });


    }


    @Override
    public void UserSelected(FriendList friendUser) {
        if(userSelectedList.contains(friendUser))
        {
            userSelectedList.remove(friendUser);
        }
        else
            userSelectedList.add(friendUser);

        if (userSelectedList.size()==1||userSelectedList.size()==0)
            addUsers.setText("Message");
        else
            addUsers.setText("Create Group");

    }

    @Override
    public void sendGroupName(String name) {
        groupName = name;
        Log.d(TAG, "sendGroupName: "+name);
        createGroupChat();
    }
}
