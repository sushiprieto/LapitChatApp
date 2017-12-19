package com.trabajo.carlos.lapitchatapp;

import android.app.ProgressDialog;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class StatusActivity extends AppCompatActivity {

    private Toolbar mToolbar;

    private TextInputLayout tilStatus;
    private Button btnStatus;

    private DatabaseReference mStatusDatabase;
    private FirebaseUser mCurrentUser;

    private ProgressDialog mProgress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_status);

        mCurrentUser = FirebaseAuth.getInstance().getCurrentUser();
        String currentUid = mCurrentUser.getUid();

        mStatusDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child(currentUid);

        //Creamos el toolbar
        mToolbar = (Toolbar) findViewById(R.id.status_appBar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("Account Status");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        //Recogemos el estado
        String statusValue = getIntent().getStringExtra("status_value");

        tilStatus = (TextInputLayout) findViewById(R.id.status_tilStatus);
        btnStatus = (Button) findViewById(R.id.status_btnStatus);

        tilStatus.getEditText().setText(statusValue);

        btnStatus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                mProgress = new ProgressDialog(StatusActivity.this);
                mProgress.setTitle("Guardando Cambios");
                mProgress.setMessage("Espere mientras establecemos el estado");
                mProgress.show();

                //Cogemos el estado del eddittext
                String status = tilStatus.getEditText().getText().toString();

                //Apuntamos a la columna status de la bbdd y le asignamos el estado
                mStatusDatabase.child("status").setValue(status).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {

                        if (task.isSuccessful()){

                            mProgress.dismiss();

                        }else {

                            Toast.makeText(StatusActivity.this, "Hubo un error cambiando el estado", Toast.LENGTH_SHORT).show();

                        }

                    }
                });

            }
        });

    }
}
