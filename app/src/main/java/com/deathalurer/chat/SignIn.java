package com.deathalurer.chat;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.quickblox.auth.session.QBSettings;
import com.quickblox.core.QBEntityCallback;
import com.quickblox.core.exception.QBResponseException;
import com.quickblox.users.QBUsers;
import com.quickblox.users.model.QBUser;

public class SignIn extends AppCompatActivity {
    static final String APP_ID = "80513";
    static final String AUTH_KEY = "gkhkXPVG5Ub38tD";
    static final String AUTH_SECRET = "xxUnexrgv6fuKhS";
    static final String ACCOUNT_KEY = "wKJwHTiUaxDNjAAcj-jb";
    private EditText username,password;
    private Button signUp,signIn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initializeQBlox();

        username = findViewById(R.id.loginEmail);
        password = findViewById(R.id.loginPassword);
        signIn = findViewById(R.id.btnlogin);
        signUp = findViewById(R.id.btnsignup);

        signIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final String email,pass;
                email = username.getText().toString();
                pass = password.getText().toString();

                QBUser user = new QBUser(email,pass);
                QBUsers.signIn(user).performAsync(new QBEntityCallback<QBUser>() {
                    @Override
                    public void onSuccess(QBUser qbUser, Bundle bundle) {
                        Toast.makeText(getBaseContext(),"Successfully login",Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(getBaseContext(),AllChats.class);
                        intent.putExtra("email",email);
                        intent.putExtra("pass",pass);
                        startActivity(intent);
                    }

                    @Override
                    public void onError(QBResponseException e) {
                        Toast.makeText(getBaseContext(),"Wrong credentials",Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });

        signUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getBaseContext(),SignUp.class);
                startActivity(intent);
            }
        });



    }

    private void initializeQBlox() {
        QBSettings.getInstance().init(getApplicationContext(),APP_ID,AUTH_KEY,AUTH_SECRET);
        QBSettings.getInstance().setAccountKey(ACCOUNT_KEY);
    }
}
