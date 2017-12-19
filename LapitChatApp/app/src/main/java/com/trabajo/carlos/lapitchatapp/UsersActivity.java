package com.trabajo.carlos.lapitchatapp;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
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

    private RecyclerView rvList;

    private DatabaseReference mUsersDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_users);

        //Creamos el toolbar
        mToolbar = (Toolbar) findViewById(R.id.users_appBar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("All Users");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        mUsersDatabase = FirebaseDatabase.getInstance().getReference().child("Users");

        rvList = (RecyclerView) findViewById(R.id.users_rvList);

        rvList.setHasFixedSize(true);
        rvList.setLayoutManager(new LinearLayoutManager(this));



    }

    @Override
    protected void onStart() {
        super.onStart();

        FirebaseRecyclerAdapter<Users, UsersViewHolder> firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<Users, UsersViewHolder>(Users.class, R.layout.users_single_layout, UsersViewHolder.class, mUsersDatabase) {
            @Override
            protected void populateViewHolder(UsersViewHolder usersViewHolder, Users users, int position) {

                //Cogemos el nombre
                usersViewHolder.setUserName(users.getName());
                //Cogemos el estado
                usersViewHolder.setUserStatus(users.getStatus());
                //Cogemos la imagen
                usersViewHolder.setUserImage(users.getThumb_image(), getApplicationContext());
                //Cogemos el id del usuario seleccionado
                final String user_id = getRef(position).getKey();

                //Cuando se clickea en la imagen
                usersViewHolder.view.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                        Intent intent = new Intent(UsersActivity.this, ProfileActivity.class);
                        //Le enviamos el id del elemento seleccionado
                        intent.putExtra("user_id", user_id);
                        startActivity(intent);

                    }
                });

            }
        };

        rvList.setAdapter(firebaseRecyclerAdapter);

    }

    public static class UsersViewHolder extends RecyclerView.ViewHolder{

        View view;

        public UsersViewHolder(View itemView) {
            super(itemView);

            view = itemView;

        }

        public void setUserName(String name){

            TextView txvUsername = (TextView) view.findViewById(R.id.users_txvSingleName);
            txvUsername.setText(name);

        }

        public void setUserStatus(String status){

            TextView txvStatus = (TextView) view.findViewById(R.id.users_txvSingleStatus);
            txvStatus.setText(status);

        }

        /**
         * Metodo para cargar la imagen en el imageview de la lista
         * @param thumb_image
         * @param context
         */
        public void setUserImage(String thumb_image, Context context){

            CircleImageView cimUser = (CircleImageView) view.findViewById(R.id.users_civSingleUser);

            Picasso.with(context).load(thumb_image).placeholder(R.drawable.avatar).into(cimUser);

        }

    }

}
