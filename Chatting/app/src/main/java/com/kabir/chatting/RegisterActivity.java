package com.kabir.chatting;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;

import java.util.HashMap;

public class RegisterActivity extends AppCompatActivity {

    private DatabaseReference mDatabase;



    private Button mCreateBtn;
    private Toolbar mToolbar;


    //dialog progress
    private ProgressDialog mRegProgress;


    private TextInputLayout mDisplayName;
    private TextInputLayout mEmail;
    private TextInputLayout mPassword;

    //firebase auth
    private FirebaseAuth mAuth;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        //register toolbar set
        mToolbar = findViewById(R.id.register_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("Create Account");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mRegProgress = new ProgressDialog(this);


        mAuth = FirebaseAuth.getInstance();
        mDisplayName =  findViewById(R.id.reg_display_name);
        mEmail = findViewById(R.id.reg_email);
        mPassword= findViewById(R.id.reg_password);
        mCreateBtn=findViewById(R.id.reg_create_btn);

        mCreateBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String display_name = mDisplayName.getEditText().getText().toString();
                String  email = mEmail.getEditText().getText().toString();
                String password = mPassword.getEditText().getText().toString();


                if(!TextUtils.isEmpty(display_name) || !TextUtils.isEmpty(email) || !TextUtils.isEmpty(password))
                {
                    mRegProgress.setTitle("Registering user");
                    mRegProgress.setMessage("Please wait while we create your account !");
                    mRegProgress.show();
                    mRegProgress.setCanceledOnTouchOutside(false);
                    register_user(display_name, email, password);

                }


            }
        });


    }

    private void register_user(final String display_name, String email, String password) {

        mAuth.createUserWithEmailAndPassword(email,password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {

                        if (task.isSuccessful()) {

                            FirebaseUser current_user = FirebaseAuth.getInstance().getCurrentUser();
                            String uid = current_user.getUid();

                            //storing data into database and retrieve
                            mDatabase=FirebaseDatabase.getInstance().getReference().child("Users").child(uid);
                            String device_token =FirebaseInstanceId.getInstance().getToken();

                            HashMap<String, String> userMap = new HashMap<>();
                            userMap.put("name", display_name);
                            userMap.put("status", "Hi there, I'm using Chat App");
                            userMap.put("image", "default");
                            userMap.put("thumb_image", "default");
                            userMap.put("device_token", device_token);

                            mDatabase.setValue(userMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {

                                    if(task.isSuccessful()) {
                                        mRegProgress.dismiss();
                                        Intent mainIntent = new Intent(RegisterActivity.this, MainActivity.class);
                                        mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                        startActivity(mainIntent);
                                        finish();
                                    }


                                }
                            });



                        } else {

                            mRegProgress.hide();
                            Toast.makeText(RegisterActivity.this, "Cannot sign in. please check the form and try again", Toast.LENGTH_LONG).show();

                        }

                    }
                });
    }
}
