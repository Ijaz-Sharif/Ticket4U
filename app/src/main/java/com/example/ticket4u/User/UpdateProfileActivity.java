package com.example.ticket4u.User;

import static com.example.ticket4u.Utils.Constant.getUserId;
import static com.example.ticket4u.Utils.Constant.setUserEmail;
import static com.example.ticket4u.Utils.Constant.setUserId;
import static com.example.ticket4u.Utils.Constant.setUserLoginStatus;
import static com.example.ticket4u.Utils.Constant.setUsername;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import android.app.Dialog;
import android.content.ContentResolver;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.MimeTypeMap;
import android.widget.EditText;
import android.widget.Toast;

import com.example.ticket4u.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

public class UpdateProfileActivity extends AppCompatActivity {
    private EditText et_register_address,et_user_number, et_latitude,et_longitude,et_user_name;
    DatabaseReference myRef;
    private Dialog loadingDialog;
    CircleImageView imageView;
    StorageReference mRef;
    private Uri imgUri =null;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_profile);
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        mRef= FirebaseStorage.getInstance().getReference("profile_images");

        et_user_name=findViewById(R.id.et_user_name);
        imageView=findViewById(R.id.updateUserPic);
        et_user_number=findViewById(R.id.et_user_number);
        et_register_address=findViewById(R.id.et_register_address);
        et_latitude=findViewById(R.id.et_latitude);
        et_longitude=findViewById(R.id.et_longitude);
        /////loading dialog
        loadingDialog=new Dialog(this);
        loadingDialog.setContentView(R.layout.loading_progress_dialog);
        loadingDialog.setCancelable(false);
        loadingDialog.getWindow().setBackgroundDrawable(getResources().getDrawable(R.drawable.slider_background));
        loadingDialog.getWindow().setLayout(ViewGroup.LayoutParams.WRAP_CONTENT,ViewGroup.LayoutParams.WRAP_CONTENT);


        loadProfile();
    }

public void loadProfile(){

          loadingDialog.show();
   DatabaseReference myRef=  FirebaseDatabase.getInstance().getReference().child("User").child(getUserId(this));
    myRef.addValueEventListener(new ValueEventListener() {
        @Override
        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
           et_user_name.setText(dataSnapshot.child("Name").getValue(String.class));
            et_register_address.setText(dataSnapshot.child("Address").getValue(String.class));
            et_latitude.setText(dataSnapshot.child("Latitude").getValue(String.class));
            et_longitude.setText(dataSnapshot.child("Longitude").getValue(String.class));
            et_user_number.setText(dataSnapshot.child("PhoneNumber").getValue(String.class));

            Picasso.with(UpdateProfileActivity.this)
                    .load(dataSnapshot.child("UserImage").getValue(String.class))
                    .placeholder(R.drawable.progress_animation)
                    .fit()
                    .centerCrop()
                    .into(imageView);
            loadingDialog.dismiss();
        }

        @Override
        public void onCancelled(@NonNull DatabaseError databaseError) {

        }
    });
}

    // get the extension of file
    private String getFileEx(Uri uri){
        ContentResolver cr=UpdateProfileActivity.this.getContentResolver();
        MimeTypeMap mime=MimeTypeMap.getSingleton();
        return mime.getExtensionFromMimeType(cr.getType(uri));
    }
    @RequiresApi(api = Build.VERSION_CODES.GINGERBREAD)
    public void updateProfile(View view) {
        loadingDialog.show();
        myRef=  FirebaseDatabase.getInstance().getReference().child("User").child(getUserId(this));
        if(imgUri!=null){
            StorageReference storageReference = mRef.child(System.currentTimeMillis() + "." + getFileEx(imgUri));
            storageReference.putFile(imgUri)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                            Task<Uri> urlTask = taskSnapshot.getStorage().getDownloadUrl();
                            while (!urlTask.isSuccessful()) ;
                            Uri downloadUrl = urlTask.getResult();
                            myRef.child("Name").setValue(et_user_name.getText().toString());
                            myRef.child("Address").setValue(et_register_address.getText().toString());
                            myRef.child("PhoneNumber").setValue(et_user_number.getText().toString());
                            myRef.child("Latitude").setValue(et_latitude.getText().toString());
                            myRef.child("Longitude").setValue(et_longitude.getText().toString());
                            myRef.child("UserImage").setValue(downloadUrl.toString());
                            loadingDialog.dismiss();
                            Toast.makeText(UpdateProfileActivity.this,"profile updated", Toast.LENGTH_LONG).show();
                            finish();

                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            loadingDialog.dismiss();
                            Toast.makeText(UpdateProfileActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
                        }
                    })
                    .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {

                        }
                    });

        }
        else {
            myRef.child("Name").setValue(et_user_name.getText().toString());
            myRef.child("Address").setValue(et_register_address.getText().toString());
            myRef.child("PhoneNumber").setValue(et_user_number.getText().toString());
            myRef.child("Latitude").setValue(et_latitude.getText().toString());
            myRef.child("Longitude").setValue(et_longitude.getText().toString());
            setUsername(UpdateProfileActivity.this,et_user_name.getText().toString());
            Toast.makeText(UpdateProfileActivity.this,"profile updated", Toast.LENGTH_LONG).show();
             finish();
            loadingDialog.dismiss();
        }
    }

    public void updatePicture(View view) {
        Intent intent=new Intent(Intent.ACTION_PICK,android.provider. MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent,1);
    }
    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 && resultCode == RESULT_OK && null != data) {
            imgUri  = data.getData();
            imageView.setImageURI(imgUri);
        }

    }
}