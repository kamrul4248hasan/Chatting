package com.kabir.chatting;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;


public class UsersActivity extends AppCompatActivity {
    private Toolbar mToolbar;
    private RecyclerView mUserList;
    private DatabaseReference mUsersDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_users);

        mToolbar= findViewById(R.id.users_appBar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("All Users");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mUsersDatabase = FirebaseDatabase.getInstance().getReference().child("Users");


        mUserList= findViewById(R.id.users_list);
        mUserList.setHasFixedSize(true);
        mUserList.setLayoutManager(new LinearLayoutManager(this));

    }

    @Override
    protected void onStart() {
        super.onStart();

        FirebaseRecyclerAdapter<Users, UsersViewHolder> firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<Users, UsersViewHolder>(
                Users.class,
                R.layout.users_single_layout,
                UsersViewHolder.class,
                mUsersDatabase
        ) {
            @Override
            protected void populateViewHolder(UsersViewHolder usersviewHolder, Users users, int position) {

                usersviewHolder.setDisplayName(users.getName());
                usersviewHolder.setUserStatus(users.getStatus());
                usersviewHolder.setUserImage(users.getImage());
                final String user_id = getRef(position).getKey();

                usersviewHolder.mview.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        Intent profileIntent = new Intent(UsersActivity.this, ProfileActivity.class);
                        profileIntent.putExtra("user_id", user_id);
                        startActivity(profileIntent);

                    }
                });


            }
        };
        mUserList.setAdapter(firebaseRecyclerAdapter);



    }
    public static class UsersViewHolder extends RecyclerView.ViewHolder {

        View mview;

        public UsersViewHolder(@NonNull View itemView) {
            super(itemView);

            mview = itemView;

        }

        public void setDisplayName(String name)
        {
            TextView userNameView = mview.findViewById(R.id.user_single_name);
            userNameView.setText(name);
        }
        public void setUserStatus(String status)
        {
            TextView userStatusView = mview.findViewById(R.id.user_single_status);
            userStatusView.setText(status);

        }
        public void setUserImage(String image)
        {
            CircleImageView userImageView=(CircleImageView)mview.findViewById(R.id.user_single_image);
            Picasso.get().load(image).placeholder(R.drawable.default_avator).into(userImageView);
        }
    }


}
