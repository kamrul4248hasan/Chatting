package com.kabir.chatting;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class ProfileActivity extends AppCompatActivity {


    private DatabaseReference mUserDatabase;

    private ImageView mProfileImage;
    private TextView mProfileName, mProfileStatus, mProfileFriendsCount;
    private Button mProfileSendRegbtn, mDeclineBtn;

    private ProgressDialog mProgressDialog;
    private String mCurrent_state;

    private FirebaseUser mCurrent_user;

    private DatabaseReference mFriendReqDatabase;
    private DatabaseReference mRootRef;

    private DatabaseReference mFriendDatabase;
    private  DatabaseReference mNotificationDatabase;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);



        final String user_id;
        String data = getIntent().getStringExtra("user_id");
        if(data == null){
            user_id= getIntent().getStringExtra("from_user_id");
        }else{
            user_id = getIntent().getStringExtra("user_id");
        }

        mRootRef = FirebaseDatabase.getInstance().getReference();
        mUserDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child(user_id);
        mFriendReqDatabase=FirebaseDatabase.getInstance().getReference().child("Friend_req");
        mFriendDatabase=FirebaseDatabase.getInstance().getReference().child("Friends");
        mNotificationDatabase=FirebaseDatabase.getInstance().getReference().child("notifications");
        mCurrent_user=FirebaseAuth.getInstance().getCurrentUser();


        mProfileImage= findViewById(R.id.profile_image);
        mProfileName=findViewById(R.id.profile_displayName);
        mProfileStatus=findViewById(R.id.profile_status);
        mProfileFriendsCount=findViewById(R.id.profile_totalFriends);
        mProfileSendRegbtn=findViewById(R.id.profile_send_reg_btn);
        mDeclineBtn= findViewById(R.id.profile_decline_btn);
        mCurrent_state="not_friends";


        mDeclineBtn.setVisibility(View.INVISIBLE);
        mDeclineBtn.setEnabled(false);


        mProgressDialog=new ProgressDialog(this);
        mProgressDialog.setTitle("Loading User Data");
        mProgressDialog.setMessage("Please wait while we load the user data");
        mProgressDialog.setCanceledOnTouchOutside(false);
        mProgressDialog.show();


        mUserDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                String display_name = dataSnapshot.child("name").getValue().toString();
                String status = dataSnapshot.child("status").getValue().toString();
                String image = dataSnapshot.child("image").getValue().toString();

                mProfileName.setText(display_name);
                mProfileStatus.setText(status);

                Picasso.get().load(image).placeholder(R.drawable.default_avator).into(mProfileImage);

                if(mCurrent_user.getUid().equals(user_id)){

                    mDeclineBtn.setEnabled(false);
                    mDeclineBtn.setVisibility(View.INVISIBLE);

                    mProfileSendRegbtn.setEnabled(false);
                    mProfileSendRegbtn.setVisibility(View.INVISIBLE);

                }


                //--------------- FRIENDS LIST / REQUEST FEATURE -----

                mFriendReqDatabase.child(mCurrent_user.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {

                        if(dataSnapshot.hasChild(user_id)){

                            String req_type = dataSnapshot.child(user_id).child("request_type").getValue().toString();

                            if(req_type.equals("received")){

                                mCurrent_state = "req_received";
                                mProfileSendRegbtn.setText("Accept Friend Request");

                                mDeclineBtn.setVisibility(View.VISIBLE);
                                mDeclineBtn.setEnabled(true);


                            } else if(req_type.equals("sent")) {

                                mCurrent_state = "req_sent";
                                mProfileSendRegbtn.setText("Cancel Friend Request");

                                mDeclineBtn.setVisibility(View.INVISIBLE);
                                mDeclineBtn.setEnabled(false);

                            }

                            mProgressDialog.dismiss();


                        } else {


                            mFriendDatabase.child(mCurrent_user.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {

                                    if(dataSnapshot.hasChild(user_id)){

                                        mCurrent_state = "friends";
                                        mProfileSendRegbtn.setText("Unfriend this Person");

                                        mDeclineBtn.setVisibility(View.INVISIBLE);
                                        mDeclineBtn.setEnabled(false);

                                    }

                                    mProgressDialog.dismiss();

                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {

                                    mProgressDialog.dismiss();

                                }
                            });

                        }



                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });


            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        mProfileSendRegbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mProfileSendRegbtn.setEnabled(false);

                //.................NOT FRIEND STATE.............

                if (mCurrent_state.equals("not_friends"))
                {
                    DatabaseReference newNotificationref =mRootRef.child("notifications").child(user_id).push();
                    String newNotificationId =newNotificationref.getKey();

                    HashMap<String, String> notificationsData = new HashMap<>();
                    notificationsData.put("from", mCurrent_user.getUid());
                    notificationsData.put("type","request");


                    Map requestMap = new HashMap<>();
                   requestMap.put("Friend_req/" +mCurrent_user.getUid()+ "/"  +user_id +  "/request_type", "sent");
                   requestMap.put("Friend_req/" +user_id + "/" + mCurrent_user.getUid() + "/request_type", "received");
                   requestMap.put("notifications/"+user_id+"/"+newNotificationId, notificationsData);
                   mRootRef.updateChildren(requestMap, new DatabaseReference.CompletionListener() {
                       @Override
                       public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {


                           if(databaseError !=null)
                           {
                               Toast.makeText(ProfileActivity.this, "There was some error in sending request", Toast.LENGTH_SHORT).show();
                           }
                           mProfileSendRegbtn.setEnabled(true);
                           mCurrent_state="req_sent";
                           mProfileSendRegbtn.setText("Cancel Friend Reques");



                       }
                   });
                }

                //.................CANCEL REQUEST STATE.............

                if(mCurrent_state.equals("req_sent"))
                {
                    mFriendReqDatabase.child(mCurrent_user.getUid()).child(user_id).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            mFriendReqDatabase.child(user_id).child(mCurrent_user.getUid()).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {

                                    mProfileSendRegbtn.setEnabled(true);
                                    mCurrent_state="not_friends";
                                    mProfileSendRegbtn.setText("Send Friend Request");
                                    mDeclineBtn.setVisibility(View.INVISIBLE);
                                    mDeclineBtn.setEnabled(false);



                                }
                            });

                        }
                    });
                }

                //...............Request received state............
                if (mCurrent_state.equals("req_received"))
                {

                    //final  String currentDate = DateFormat.getDateTimeInstance().format(new Date());

                    final String currentDate = java.text.DateFormat.getDateTimeInstance().format(Calendar.getInstance().getTime());

                    Map friendsMap =new HashMap();
                    friendsMap.put("Friends/" + mCurrent_user.getUid() + "/" +user_id + "/data" ,currentDate);
                    friendsMap.put("Friends/" + user_id + "/" +mCurrent_user.getUid()+ "/data" ,currentDate);

                    friendsMap.put("Friend_req/" + mCurrent_user.getUid() + "/" +user_id , null);
                    friendsMap.put("Friend_req/" + user_id + "/" + mCurrent_user.getUid(), null);


                    mRootRef.updateChildren(friendsMap, new DatabaseReference.CompletionListener() {
                       @Override
                       public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {

                           if(databaseError==null)
                           {
                               mProfileSendRegbtn.setEnabled(true);
                               mCurrent_state="friends";
                               mProfileSendRegbtn.setText("Unfriend this Person");
                               mDeclineBtn.setVisibility(View.INVISIBLE);
                               mDeclineBtn.setEnabled(false);
                           }
                           else
                           {
                               String error = databaseError.getMessage();
                               Toast.makeText(ProfileActivity.this, error, Toast.LENGTH_SHORT).show();
                           }
                       }
                   });
                }


                //...................UNFRIEND......
                if(mCurrent_state.equals("friends"))
                {
                    Map unfriendMap = new HashMap();
                    unfriendMap.put("Friends/" + mCurrent_user.getUid() + "/" + user_id, null);
                    unfriendMap.put("Friends/" + user_id + "/" + mCurrent_user.getUid(), null);


                    mRootRef.updateChildren(unfriendMap, new DatabaseReference.CompletionListener() {
                        @Override
                        public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {

                            if(databaseError==null)
                            {

                                mCurrent_state="not_friends";
                                mProfileSendRegbtn.setText("Send Friend Request");

                                mDeclineBtn.setVisibility(View.INVISIBLE);
                                mDeclineBtn.setEnabled(false);
                            }
                            else
                            {
                                String error = databaseError.getMessage();
                                Toast.makeText(ProfileActivity.this, error, Toast.LENGTH_SHORT).show();
                            }
                            mProfileSendRegbtn.setEnabled(true);
                        }
                    });

                }
            }
        });
    }
}
