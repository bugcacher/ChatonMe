package com.deathalurer.chat;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.quickblox.auth.QBAuth;
import com.quickblox.auth.session.QBSession;
import com.quickblox.core.QBEntityCallback;
import com.quickblox.core.exception.QBResponseException;
import com.quickblox.users.QBUsers;
import com.quickblox.users.model.QBUser;

public class SignUp extends AppCompatActivity {
    private EditText username,password,fullName,phoneNumber;
    private Button signUp,signIn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);
        registerSession();

        username = findViewById(R.id.signupEmail);
        password = findViewById(R.id.signupPassword);
        signIn = findViewById(R.id.signInButton);
        signUp = findViewById(R.id.signUpButton);
        fullName  = findViewById(R.id.signupName);
        phoneNumber  =findViewById(R.id.signupPhone);



        signIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intent = new Intent(getBaseContext(),SignIn.class);
                startActivity(intent);
            }
        });

        signUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String email,pass,name,phone;
                email = username.getText().toString();
                pass = password.getText().toString();
                name = fullName.getText().toString();
                phone = phoneNumber.getText().toString();

                QBUser user = new QBUser(email,pass);
                user.setFullName(name);
                user.setPhone(phone);
                QBUsers.signUp(user).performAsync(new QBEntityCallback<QBUser>() {
                    @Override
                    public void onSuccess(QBUser qbUser, Bundle bundle) {
                        Toast.makeText(getBaseContext(),"Successfully Registered",Toast.LENGTH_SHORT).show();
                        finish();
                    }
                    @Override
                    public void onError(QBResponseException e) {
                        Toast.makeText(getBaseContext(),"Some error occur : "+e.getMessage(),Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }

    private void registerSession() {
        QBAuth.createSession().performAsync(new QBEntityCallback<QBSession>() {
            @Override
            public void onSuccess(QBSession qbSession, Bundle bundle) {

            }

            @Override
            public void onError(QBResponseException e) {
                Toast.makeText(getBaseContext(),""+ e.getMessage(),Toast.LENGTH_SHORT).show();
            }
        });
    }
}
