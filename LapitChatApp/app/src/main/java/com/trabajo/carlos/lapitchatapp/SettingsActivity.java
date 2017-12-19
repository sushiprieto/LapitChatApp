package com.trabajo.carlos.lapitchatapp;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import de.hdodenhof.circleimageview.CircleImageView;
import id.zelory.compressor.Compressor;

public class SettingsActivity extends AppCompatActivity implements View.OnClickListener{

    private DatabaseReference mUserDatabase;
    private FirebaseUser mCurrentUser;
    private StorageReference mImageStorage;

    private CircleImageView civUserImage;
    private TextView txvName, txvStatus;
    private Button btnCambiarStatus, btnCambiarImagen;

    private ProgressDialog mProgressDialog;

    private static final int GALLERY_PICK = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        txvName = (TextView) findViewById(R.id.settings_txvName);
        txvStatus= (TextView) findViewById(R.id.settings_txvStatus);
        civUserImage = (CircleImageView) findViewById(R.id.settings_civAvatar);
        btnCambiarStatus = (Button) findViewById(R.id.settings_btnCambiarStatus);
        btnCambiarImagen = (Button) findViewById(R.id.settings_btnCambiarImage);

        btnCambiarStatus.setOnClickListener(this);
        btnCambiarImagen.setOnClickListener(this);

        //Instanciamos el almacenamiento
        mImageStorage = FirebaseStorage.getInstance().getReference();

        mCurrentUser = FirebaseAuth.getInstance().getCurrentUser();
        String currentUid = mCurrentUser.getUid();

        mUserDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child(currentUid);
        //Para poder usar firebase offline
        mUserDatabase.keepSynced(true);

        mUserDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                //Toast.makeText(SettingsActivity.this, dataSnapshot.toString(), Toast.LENGTH_SHORT).show();

                //Recogemos los datos de la BBDD
                String name = dataSnapshot.child("name").getValue().toString();
                final String image = dataSnapshot.child("image").getValue().toString();
                String status = dataSnapshot.child("status").getValue().toString();
                String thumbImage = dataSnapshot.child("thumb_image").getValue().toString();

                txvName.setText(name);
                txvStatus.setText(status);

                if (!image.equals("default")){

                    //Establecemos la imagen usando una libreria externa
                    //Picasso.with(SettingsActivity.this).load(image).placeholder(R.drawable.avatar).into(civUserImage);

                    //Para cargar la imagen cuando estemos offline
                    Picasso.with(SettingsActivity.this).load(image).networkPolicy(NetworkPolicy.OFFLINE).placeholder(R.drawable.avatar).into(civUserImage, new Callback() {
                        @Override
                        public void onSuccess() {

                        }

                        @Override
                        public void onError() {

                            //Si hay algun error pues que intente cargar la imagen de modo online
                            Picasso.with(SettingsActivity.this).load(image).placeholder(R.drawable.avatar).into(civUserImage);

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
    public void onClick(View view) {

        switch (view.getId()){

            case R.id.settings_btnCambiarStatus:
                //Mandamos el estado para recogerlo luego
                String statusValue = txvStatus.getText().toString();

                Intent intent = new Intent(SettingsActivity.this, StatusActivity.class);
                intent.putExtra("status_value", statusValue);
                startActivity(intent);
                break;

            case R.id.settings_btnCambiarImage:
                /*Intent galeriaIntent = new Intent();
                galeriaIntent.setType("image*//**//*");
                galeriaIntent.setAction(Intent.ACTION_GET_CONTENT);

                startActivityForResult(Intent.createChooser(galeriaIntent, "SELECCIONAR IMAGEN"), GALLERY_PICK);*/

                // start picker to get image for cropping and then use the image in cropping activity
                CropImage.activity()
                        .setGuidelines(CropImageView.Guidelines.ON)
                        .start(SettingsActivity.this);

                break;

        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == GALLERY_PICK && resultCode == RESULT_OK){

            Uri imageUri = data.getData();

            CropImage.activity(imageUri)
                    .setAspectRatio(1, 1)
                    .setMinCropWindowSize(500, 500)
                    .start(this);

            //Toast.makeText(SettingsActivity.this, imageUri, Toast.LENGTH_LONG).show();

        }

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {

            CropImage.ActivityResult result = CropImage.getActivityResult(data);

            if (resultCode == RESULT_OK) {

                mProgressDialog = new ProgressDialog(SettingsActivity.this);
                mProgressDialog.setTitle("Subiendo imagen...");
                mProgressDialog.setMessage("Espere mientras cargamos tu imagen");
                mProgressDialog.setCanceledOnTouchOutside(false);
                mProgressDialog.show();

                Uri resultUri = result.getUri();

                File filePath = new File(resultUri.getPath());

                String currentUserId = mCurrentUser.getUid();

                try {

                    Bitmap thumb_bitmap = new Compressor(this)
                            .setMaxWidth(200)
                            .setMaxHeight(200)
                            .setQuality(75)
                            .compressToBitmap(filePath);

                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    thumb_bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                    final byte[] thumb_byte = baos.toByteArray();



                StorageReference filepath = mImageStorage.child("profile_images").child(currentUserId + ".jpg");
                final StorageReference thumb_filePath = mImageStorage.child("profile_images").child("thumbs").child(currentUserId + ".jpg");

                filepath.putFile(resultUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onComplete(@NonNull final Task<UploadTask.TaskSnapshot> task) {

                        if (task.isSuccessful()){

                            //He tenido que hacer esto para que no de fallos
                            @SuppressWarnings("VisibleForTests") final String downloadUrl = task.getResult().getDownloadUrl().toString();

                            UploadTask uploadTask = thumb_filePath.putBytes(thumb_byte);
                            uploadTask.addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                                @Override
                                public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> thumb_task) {

                                    @SuppressWarnings("VisibleForTests")String thumb_downloadUrl = thumb_task.getResult().getDownloadUrl().toString();

                                    if (thumb_task.isSuccessful()){

                                        Map update_hashMap = new HashMap();
                                        update_hashMap.put("image", downloadUrl);
                                        update_hashMap.put("thumb_image", thumb_downloadUrl);

                                        //Apuntamos a la columna image de la bbdd para guardar la imagen
                                        mUserDatabase.updateChildren(update_hashMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {

                                                if (task.isSuccessful()){

                                                    mProgressDialog.dismiss();
                                                    Toast.makeText(SettingsActivity.this, "Exito subiendola", Toast.LENGTH_LONG).show();

                                                }

                                            }
                                        });

                                    }else {

                                        Toast.makeText(SettingsActivity.this, "error", Toast.LENGTH_SHORT).show();

                                        mProgressDialog.dismiss();

                                    }

                                }
                            });

                        }else {

                            Toast.makeText(SettingsActivity.this, "error", Toast.LENGTH_SHORT).show();

                            mProgressDialog.dismiss();

                        }

                    }
                });

                /*CropImage.activity(resultUri)
                        .setAspectRatio(1, 1)
                        .start(this);*/

                //Toast.makeText(SettingsActivity.this, resultUri.toString(), Toast.LENGTH_SHORT).show();

                } catch (IOException e) {
                    e.printStackTrace();
                }

            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {

                Exception error = result.getError();

            }

        }

    }

    public static String random(){

        Random generator = new Random();
        StringBuilder randomStringBuilder = new StringBuilder();
        int randomLength = generator.nextInt(10);
        char tempChar;

        for (int i = 0; i < randomLength; i++) {

            tempChar = (char) (generator.nextInt(96) + 32);
            randomStringBuilder.append(tempChar);

        }

        return randomStringBuilder.toString();

    }

}
