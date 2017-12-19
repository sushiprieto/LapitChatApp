package com.trabajo.carlos.lapitchatapp;

import android.app.ProgressDialog;
import android.icu.text.DateFormat;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class ProfileActivity extends AppCompatActivity implements View.OnClickListener {

    private TextView txvUsername, txvStatus, txvTotalFriends;
    private Button btnSendReq, btnDeclineReq;
    private ImageView imvAvatar;

    private ProgressDialog progressDialog;

    private DatabaseReference mUsersDatabase;
    private DatabaseReference mFriendReqDatabase;
    private DatabaseReference mFriendDatabase;
    private DatabaseReference mNotificationDatabase;
    private DatabaseReference mRootRef;
    private FirebaseUser currentUser;

    private String currentState;
    private String user_id;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        //Recogemos el id del elemento seleccionado de la actividad UsersActivity
        user_id = getIntent().getStringExtra("user_id");

        mUsersDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child(user_id);
        mFriendReqDatabase = FirebaseDatabase.getInstance().getReference().child("Friend_req");
        mFriendDatabase = FirebaseDatabase.getInstance().getReference().child("Friends");
        mNotificationDatabase = FirebaseDatabase.getInstance().getReference().child("notifications");
        mRootRef = FirebaseDatabase.getInstance().getReference();
        currentUser = FirebaseAuth.getInstance().getCurrentUser();

        txvUsername = (TextView) findViewById(R.id.profile_txvUsername);
        txvStatus = (TextView) findViewById(R.id.profile_txvStatus);
        txvTotalFriends = (TextView) findViewById(R.id.profile_txvTotalAmigos);
        imvAvatar = (ImageView) findViewById(R.id.profile_imvAvatar);
        btnSendReq = (Button) findViewById(R.id.profile_btnSendReq);
        btnDeclineReq = (Button) findViewById(R.id.profile_btnDeclineReq);

        currentState = "not_friends";

        btnDeclineReq.setVisibility(View.INVISIBLE);
        btnDeclineReq.setEnabled(false);

        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Cargando datos del usuario");
        progressDialog.setMessage("Espere mientras cargamos los datos");
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.show();

        btnSendReq.setOnClickListener(this);

        mUsersDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                //Recogemos los datos
                String userName = dataSnapshot.child("name").getValue().toString();
                String status = dataSnapshot.child("status").getValue().toString();
                String image = dataSnapshot.child("image").getValue().toString();

                txvUsername.setText(userName);
                txvStatus.setText(status);

                Picasso.with(ProfileActivity.this).load(image).placeholder(R.drawable.avatar).into(imvAvatar);

                // ---------------------- FRIENDS LIST STATE ----------------------
                mFriendReqDatabase.child(currentUser.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {

                        if (dataSnapshot.hasChild(user_id)) {

                            String req_type = dataSnapshot.child(user_id).child("request_type").getValue().toString();

                            if (req_type.equals("received")) {

                                currentState = "req_received";
                                //Cambiamos el texto del boton
                                btnSendReq.setText("Aceptar peticion");

                                //Mostramos el otro boton
                                btnDeclineReq.setVisibility(View.VISIBLE);
                                btnDeclineReq.setEnabled(true);

                            } else if (req_type.equals("sent")) {

                                currentState = "req_sent";
                                btnSendReq.setText("Cancelar peticion");

                                //Ocultamos el otro boton
                                btnDeclineReq.setVisibility(View.INVISIBLE);
                                btnDeclineReq.setEnabled(false);

                            }

                            progressDialog.dismiss();

                        } else {

                            mFriendDatabase.child(currentUser.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {

                                    if (dataSnapshot.hasChild(user_id)) {

                                        currentState = "friends";
                                        //Cambiamos el texto del boton
                                        btnSendReq.setText("Eliminar amigo");

                                        //Ocultamos el otro boton
                                        btnDeclineReq.setVisibility(View.INVISIBLE);
                                        btnDeclineReq.setEnabled(false);

                                    }

                                    progressDialog.dismiss();

                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {

                                    progressDialog.dismiss();

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

    }

    @Override
    public void onClick(View view) {

        switch (view.getId()) {

            case R.id.profile_btnSendReq:

                //Desabilitamos el boton
                btnSendReq.setEnabled(false);

                // ---------------------- NOT FRIENDS STATE ----------------------

                if (currentState.equals("not_friends")) {

                    DatabaseReference newNotificationref = mRootRef.child("notifications").child(user_id).push();
                    String newNotificationId = newNotificationref.getKey();

                    HashMap<String, String> notificationData = new HashMap<>();
                    notificationData.put("from", currentUser.getUid());
                    notificationData.put("type", "request");

                    Map requestMap = new HashMap();
                    requestMap.put("Friend_req/" + currentUser.getUid() + "/" + user_id + "/request_type", "sent");
                    requestMap.put("Friend_req/" + user_id + "/" + currentUser.getUid() + "/request_type", "received");
                    requestMap.put("notifications/" + user_id + "/" + newNotificationId, notificationData);

                    mRootRef.updateChildren(requestMap, new DatabaseReference.CompletionListener() {
                        @Override
                        public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {

                            if (databaseError != null) {

                                Toast.makeText(ProfileActivity.this, "There was some error in sending request", Toast.LENGTH_SHORT).show();

                            } else {

                                currentState = "req_sent";
                                btnSendReq.setText("Cancelar Peticion");

                            }

                            btnSendReq.setEnabled(true);
                            currentState = "req_sent";
                            //Cambiamos el texto del boton
                            btnSendReq.setText("Cancelar peticion");

                        }
                    });

                }

                // ---------------------- CANCEL REQUEST STATE ----------------------

                if (currentState.equals("req_sent")) {

                    mFriendReqDatabase.child(currentUser.getUid()).child(user_id).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {

                            mFriendReqDatabase.child(user_id).child(currentUser.getUid()).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {

                                    //Activamos el boton
                                    btnSendReq.setEnabled(true);
                                    currentState = "not_friends";
                                    //Cambiamos el texto del boton
                                    btnSendReq.setText("Enviar peticion");

                                    //Ocultamos el otro boton
                                    btnDeclineReq.setVisibility(View.INVISIBLE);
                                    btnDeclineReq.setEnabled(false);

                                }
                            });

                        }
                    });

                }

                // ---------------------- REQUEST RECEIVED STATE ----------------------

                if (currentState.equals("req_received")) {

                    final String currentDate = DateFormat.getDateTimeInstance().format(new Date());

                    Map friendsMap = new HashMap();
                    friendsMap.put("Friends/" + currentUser.getUid() + "/" + user_id + "/date", currentDate);
                    friendsMap.put("Friends/" + user_id + "/" + currentUser.getUid() + "/date", currentDate);

                    friendsMap.put("Friend_req/" + currentUser.getUid() + "/" + user_id, null);
                    friendsMap.put("Friend_req/" + user_id + "/" + currentUser.getUid(), null);

                    mRootRef.updateChildren(friendsMap, new DatabaseReference.CompletionListener() {
                        @Override
                        public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {

                            if (databaseError == null) {

                                btnSendReq.setEnabled(true);
                                currentState = "friends";
                                btnSendReq.setText("Eliminar amigo");

                                btnDeclineReq.setVisibility(View.INVISIBLE);
                                btnDeclineReq.setEnabled(false);

                            } else {

                                String error = databaseError.getMessage();

                                Toast.makeText(ProfileActivity.this, error, Toast.LENGTH_SHORT).show();

                            }

                        }
                    });

                }

                // ---------------------- UNFRIEND ----------------------

                if (currentState.equals("friends")) {

                    Map unfriendMap = new HashMap();
                    unfriendMap.put("Friends/" + currentUser.getUid() + "/" + user_id, null);
                    unfriendMap.put("Friends/" + user_id + "/" + currentUser.getUid(), null);

                    mRootRef.updateChildren(unfriendMap, new DatabaseReference.CompletionListener() {
                        @Override
                        public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {


                            if (databaseError == null) {

                                currentState = "not_friends";
                                btnSendReq.setText("Enviar peticion");

                                btnDeclineReq.setVisibility(View.INVISIBLE);
                                btnDeclineReq.setEnabled(false);

                            } else {

                                String error = databaseError.getMessage();

                                Toast.makeText(ProfileActivity.this, error, Toast.LENGTH_SHORT).show();

                            }

                            btnSendReq.setEnabled(true);

                        }
                    });

                }


                break;

            case R.id.profile_btnDeclineReq:

                break;

        }

    }
}
