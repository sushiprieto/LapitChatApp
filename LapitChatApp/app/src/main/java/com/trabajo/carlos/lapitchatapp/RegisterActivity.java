package com.trabajo.carlos.lapitchatapp;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
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

    private static final String TAG = "";
    private TextInputLayout tilUsername, tilEmail, tilPassword;
    private Button btnCreateAccount;

    private Toolbar mToolbar;

    private ProgressDialog mRegProgress;

    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        //Firebase autentificacion
        mAuth = FirebaseAuth.getInstance();

        tilUsername = (TextInputLayout) findViewById(R.id.tilUsername);
        tilEmail = (TextInputLayout) findViewById(R.id.tilEmail);
        tilPassword = (TextInputLayout) findViewById(R.id.tilPassword);
        btnCreateAccount = (Button) findViewById(R.id.btnCreateAccount);

        //Establecer el toolbar
        mToolbar = (Toolbar) findViewById(R.id.register_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("Create Account");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        //Creamos ProgressRing
        mRegProgress = new ProgressDialog(this);

        btnCreateAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                //Recogemos los datos
                String username = tilUsername.getEditText().getText().toString();
                String email = tilEmail.getEditText().getText().toString();
                String password = tilPassword.getEditText().getText().toString();

                if (!TextUtils.isEmpty(username) || !TextUtils.isEmpty(email) || !TextUtils.isEmpty(password)) {

                    //Establecemos el progressring
                    mRegProgress.setTitle("Registrando usuario");
                    mRegProgress.setMessage("Por favor espere mientras creamos su cuenta");
                    mRegProgress.setCanceledOnTouchOutside(false);
                    mRegProgress.show();

                    RegisterUser(username, email, password);

                }

            }
        });

    }

    private void RegisterUser(final String username, String email, String password) {

        mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {

                Log.d(TAG, "createUserWithEmail:onComplete:" + task.isSuccessful());

                // If sign in fails, display a message to the user. If sign in succeeds
                // the auth state listener will be notified and logic to handle the
                // signed in user can be handled in the listener.
                if (!task.isSuccessful()) {

                    //Si tenemos algun error lo ocultamos
                    mRegProgress.hide();

                    Toast.makeText(RegisterActivity.this, "No se puede registrar. Por favor revise el formulario.", Toast.LENGTH_SHORT).show();

                } else {

                    //Creamos y cogemos el id del usuario actual
                    FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
                    String uid = currentUser.getUid();

                    //Buscamos la columna usuarios de la BBDD y sacamos el uid
                    mDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child(uid);

                    String deviceToken = FirebaseInstanceId.getInstance().getToken();

                    //Metemos los datos
                    HashMap<String, String> userMap = new HashMap<>();
                    userMap.put("name", username);
                    userMap.put("status", "Hola, estoy usando el chat");
                    userMap.put("image", "default");
                    userMap.put("thumb_image", "default");
                    userMap.put("device_token", deviceToken);

                    //Le asignamos el valor a la BBDD
                    mDatabase.setValue(userMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {

                            if (task.isSuccessful()) {

                                //Lo cancelamos ya
                                mRegProgress.dismiss();

                                Intent mainIntent = new Intent(RegisterActivity.this, MainActivity.class);
                                mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                startActivity(mainIntent);
                                finish();

                            }

                        }
                    });

                }

            }
        });

    }

}
