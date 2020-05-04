package com.deathalurer.chat;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.quickblox.chat.QBChatService;
import com.quickblox.chat.QBRestChatService;
import com.quickblox.chat.model.QBChatMessage;
import com.quickblox.content.QBContent;
import com.quickblox.content.model.QBFile;
import com.quickblox.core.QBEntityCallback;
import com.quickblox.core.exception.QBResponseException;
import com.quickblox.users.QBUsers;
import com.quickblox.users.model.QBUser;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class Profile extends AppCompatActivity implements ChooseAvatarDialog.SendAvatarInterface {
    private static final String TAG ="ProfileActivity" ;
    ImageView profile_image,edit,logout;
    TextView profile_name,profile_phone,profile_email,profile_pass;
    EditText profile_name_et,profile_phone_et,profile_email_et,profile_pass_old,profile_pass_new;
    Button saveCHanges;
    CustomDialog dialog;
    boolean allowUpload = false;
    ChooseAvatarDialog chooseAvatarDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        profile_image = findViewById(R.id.profileImage);
        edit = findViewById(R.id.editProfile);
        profile_name = findViewById(R.id.profile_name);
        profile_email = findViewById(R.id.profile_email);
        profile_phone  =findViewById(R.id.profile_phone);
        profile_pass = findViewById(R.id.profile_pass);
        logout = findViewById(R.id.logoutProfile);

        logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                QBChatService chatService = QBChatService.getInstance();
                if (!chatService.isLoggedIn())
                {
                    return;
                }
                else
                {
                    chatService.logout(new QBEntityCallback<Void>() {
                        @Override
                        public void onSuccess(Void aVoid, Bundle bundle) {
                            finish();
                            Toast.makeText(getBaseContext(),"Bye Bye!",Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(getBaseContext(),SignIn.class);
                            startActivity(intent);
                        }

                        @Override
                        public void onError(QBResponseException e) {

                        }
                    });
                }
            }
        });

        profile_name_et = findViewById(R.id.profile_name_et);
        profile_email_et = findViewById(R.id.profile_email_et);
        profile_phone_et = findViewById(R.id.profile_phone_et);
        profile_pass_old = findViewById(R.id.profile_pass_old__et);
        profile_pass_new = findViewById(R.id.profile_pass_new_et);

        saveCHanges = findViewById(R.id.submit_changes);

        fillUserData();
        allowUpload = false;
        profile_image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (allowUpload){
                    chooseAvatarDialog = new ChooseAvatarDialog();
                    chooseAvatarDialog.show(getSupportFragmentManager(),"Avatar dialog");
                }
            }
        });

        edit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                profile_name.setVisibility(View.INVISIBLE);
                profile_email.setVisibility(View.INVISIBLE);
                profile_phone.setVisibility(View.INVISIBLE);
                edit.setVisibility(View.INVISIBLE);
                profile_pass.setVisibility(View.INVISIBLE);

                profile_name_et.setVisibility(View.VISIBLE);
                profile_email_et.setVisibility(View.VISIBLE);
                profile_phone_et.setVisibility(View.VISIBLE);
                profile_pass_old.setVisibility(View.VISIBLE);
                profile_pass_new.setVisibility(View.VISIBLE);
                saveCHanges.setVisibility(View.VISIBLE);
                allowUpload = true;
            }
        });

        saveCHanges.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                QBUser user = new QBUser();
                user.setId(QBChatService.getInstance().getUser().getId());
                if (profile_email_et.getText().toString().isEmpty() || profile_name_et.getText().toString().isEmpty()
                        || profile_phone_et.getText().toString().isEmpty()) {
                    Toast.makeText(getBaseContext(), "Enter Details", Toast.LENGTH_SHORT).show();
                }
                else{
                    if(profile_pass_old.getText().toString().isEmpty() &&profile_pass_new.getText().toString().isEmpty() ){
                        user.setFullName(profile_name_et.getText().toString());
                        user.setPhone(profile_phone_et.getText().toString());
                        user.setEmail(profile_email_et.getText().toString());
                        dialog = new CustomDialog(Profile.this);
                        dialog.showDialog();
                        QBUsers.updateUser(user).performAsync(new QBEntityCallback<QBUser>() {
                            @Override
                            public void onSuccess(QBUser user, Bundle bundle) {
                                Toast.makeText(getBaseContext(),"Updated",Toast.LENGTH_SHORT).show();
                                dialog.hideDialog();
                                showViews();
                            }

                            @Override
                            public void onError(QBResponseException e) {
                                dialog.hideDialog();
                                Toast.makeText(getBaseContext(),""+e.getMessage(),Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                    else if(profile_pass_old.getText().toString().isEmpty()||
                            profile_pass_new.getText().toString().isEmpty()){
                        Toast.makeText(getBaseContext(), "Both old and new Password required!", Toast.LENGTH_SHORT).show();
                    }
                    else
                    {
                        user.setFullName(profile_name_et.getText().toString());
                        user.setPhone(profile_phone_et.getText().toString());
                        user.setEmail(profile_email_et.getText().toString());
                        user.setOldPassword(profile_pass_old.getText().toString());
                        user.setPassword(profile_pass_new.getText().toString());
                        dialog = new CustomDialog(Profile.this);
                        dialog.showDialog();
                        QBUsers.updateUser(user).performAsync(new QBEntityCallback<QBUser>() {
                            @Override
                            public void onSuccess(QBUser user, Bundle bundle) {
                                Toast.makeText(getBaseContext(),"Updated",Toast.LENGTH_SHORT).show();
                                dialog.hideDialog();
                                showViews();
                                allowUpload = false;
                            }

                            @Override
                            public void onError(QBResponseException e) {
                                dialog.hideDialog();
                                Toast.makeText(getBaseContext(),"Error"+e.getMessage(),Toast.LENGTH_SHORT).show();
                            }
                        });

                    }
                }
                allowUpload = false;
            }
        });
    }

    private void showViews() {
        profile_name.setVisibility(View.VISIBLE);
        profile_email.setVisibility(View.VISIBLE);
        profile_phone.setVisibility(View.VISIBLE);
        edit.setVisibility(View.VISIBLE);
        profile_pass.setVisibility(View.VISIBLE);

        profile_name_et.setVisibility(View.GONE);
        profile_email_et.setVisibility(View.GONE);
        profile_phone_et.setVisibility(View.GONE);
        profile_pass_old.setVisibility(View.GONE);
        profile_pass_new.setVisibility(View.GONE);
        saveCHanges.setVisibility(View.GONE);
        fillUserData();
        allowUpload = false;

    }

    private void fillUserData() {
        int id = QBChatService.getInstance().getUser().getId();
        Log.d(TAG, "fillUserData: " + id);
        QBUsers.getUser(id).performAsync(new QBEntityCallback<QBUser>() {
            @Override
            public void onSuccess(QBUser qbUser, Bundle bundle) {

                 profile_name.setText(qbUser.getFullName());
                 profile_email.setText(qbUser.getEmail());
                 profile_phone.setText(qbUser.getPhone());

                profile_name_et.setText(qbUser.getFullName());
                profile_email_et.setText(qbUser.getEmail());
                profile_phone_et.setText(qbUser.getPhone());


                    if(qbUser.getFileId()!=null){
                           int profileID = qbUser.getFileId();
                         QBContent.getFile(profileID).performAsync(new QBEntityCallback<QBFile>() {
                          @Override
                         public void onSuccess(QBFile qbFile, Bundle bundle) {
                             String url = qbFile.getPublicUrl();
                            Glide.with(Profile.this).load(url).apply(RequestOptions.circleCropTransform()).into(profile_image);
                          }

                       @Override
                         public void onError(QBResponseException e) {

                          }
                    });
                }
            }

            @Override
            public void onError(QBResponseException e) {
                Toast.makeText(getBaseContext(),"Error",Toast.LENGTH_SHORT).show();
            }
        });
//        Log.e("Profile","Name : "+currentUser.getFullName()+","+currentUser.getEmail());

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == 1) {
                Uri imageSelected = data.getData();
                //todo progress bar
                final CustomDialogUpload dialog = new CustomDialogUpload(Profile.this);
                dialog.showDialog();
                try {
                    InputStream in = getContentResolver().openInputStream(imageSelected);
                    final Bitmap bitmap = BitmapFactory.decodeStream(in);
                    ByteArrayOutputStream bos = new ByteArrayOutputStream();
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, bos);
                    File file = new File(Environment.getExternalStorageDirectory() + "/myyyProfilePhoto.png");
                    FileOutputStream fos = new FileOutputStream(file);
                    fos.write(bos.toByteArray());
                    fos.flush();
                    fos.close();

                    final int fileSize = (int) (file.length() / 1024);
                    if (fileSize > 1024 * 100) {
                        Toast.makeText(getBaseContext(), "Size too large", Toast.LENGTH_SHORT).show();
                    }
                    QBContent.uploadFileTask(file, true, null)
                            .performAsync(new QBEntityCallback<QBFile>() {
                                @Override
                                public void onSuccess(QBFile qbFile, Bundle bundle) {
                                    Toast.makeText(getBaseContext(), "Done Uploading ", Toast.LENGTH_SHORT).show();

                                    QBUser user = new QBUser();
                                    user.setId(QBChatService.getInstance().getUser().getId());
                                    user.setFileId(qbFile.getId());

                                    QBUsers.updateUser(user).performAsync(new QBEntityCallback<QBUser>() {
                                        @Override
                                        public void onSuccess(QBUser qbUser, Bundle bundle) {
                                            Toast.makeText(getBaseContext(), "Done Updating ", Toast.LENGTH_SHORT).show();
                                            dialog.hideDialog();
                                            Glide.with(getBaseContext()).load(bitmap).
                                                    apply(RequestOptions.circleCropTransform())
                                                    .into(profile_image);
                                        }

                                        @Override
                                        public void onError(QBResponseException e) {
                                            Toast.makeText(getBaseContext(), "Error in updating user", Toast.LENGTH_SHORT).show();
                                        }
                                    });
                                }

                                @Override
                                public void onError(QBResponseException e) {
                                    Toast.makeText(getBaseContext(), "Error in uploading file" + e.getMessage(), Toast.LENGTH_SHORT).show();

                                }
                            });

                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                    Toast.makeText(getBaseContext(), "Er:" + e.getMessage(), Toast.LENGTH_SHORT).show();

                } catch (IOException e) {
                    Toast.makeText(getBaseContext(), "Error :" + e.getMessage(), Toast.LENGTH_SHORT).show();
                    e.printStackTrace();
                }

            }
        }
    }

    @Override
    public void sendAvatarResponse(boolean avatar, int id) {
        Log.d(TAG, "sendAvatarResponse: " + avatar + " "+ id + "");
        if (!avatar)
        {   chooseAvatarDialog.dismiss();
            Intent intent = new Intent();
            intent.setType("image/*");
            intent.setAction(Intent.ACTION_GET_CONTENT);
            startActivityForResult(Intent.createChooser(intent,"Select Image"),1);
        }
        else{
            chooseAvatarDialog.dismiss();
            uploadImage(id);
        }
    }

    void uploadImage(Integer avatarId){
        final CustomDialogUpload dialogUpload = new CustomDialogUpload(Profile.this);
        dialogUpload.showDialog();
        final Bitmap bitmap = BitmapFactory.decodeResource(Profile.this.getResources(),avatarId);
        File file = new File(Environment.getExternalStorageDirectory() + "/myyProfilePhoto.png");
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
            fos.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        final int fileSize = (int) (file.length() / 1024);
        if (fileSize > 1024 * 100) {
            Toast.makeText(getBaseContext(), "Size too large", Toast.LENGTH_SHORT).show();
        }
        QBContent.uploadFileTask(file, true, null)
                .performAsync(new QBEntityCallback<QBFile>() {
                    @Override
                    public void onSuccess(QBFile qbFile, Bundle bundle) {
                        //Toast.makeText(getBaseContext(), "Done Uploading ", Toast.LENGTH_SHORT).show();

                        QBUser user = new QBUser();
                        user.setId(QBChatService.getInstance().getUser().getId());
                        user.setFileId(qbFile.getId());

                        QBUsers.updateUser(user).performAsync(new QBEntityCallback<QBUser>() {
                            @Override
                            public void onSuccess(QBUser qbUser, Bundle bundle) {
                                Toast.makeText(getBaseContext(), "Image Uploaded! ", Toast.LENGTH_SHORT).show();
                                Glide.with(getBaseContext()).load(bitmap).
                                        apply(RequestOptions.circleCropTransform())
                                        .into(profile_image);
                                dialogUpload.hideDialog();
                            }

                            @Override
                            public void onError(QBResponseException e) {
                                Toast.makeText(getBaseContext(), "Error in updating user", Toast.LENGTH_SHORT).show();
                                dialogUpload.hideDialog();
                            }
                        });
                    }

                    @Override
                    public void onError(QBResponseException e) {
                        Toast.makeText(getBaseContext(), "Error in uploading file" + e.getMessage(), Toast.LENGTH_SHORT).show();
                        dialogUpload.hideDialog();
                    }
                });
    }
}
