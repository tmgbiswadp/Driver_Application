package com.dune.monitorme;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.*;

import java.io.FileNotFoundException;
import java.io.IOException;

public class UpdatePhoto extends AppCompatActivity {

    ImageView profileimage;
    Button button;
    private static final int PICK_FROM_GALLERY = 1;
    Uri uriProfileImage;
    String profileimageUrl;
    Bitmap bitmap;
    StorageTask muploadTask;
    FirebaseAuth mauth;


    ProgressDialog mProgress;
    DatabaseReference mdatabaseref;
    StorageReference mstorageref;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_photo);

        profileimage=findViewById(R.id.profileimage);
        button=findViewById(R.id.saveimagetofirebase);
        mdatabaseref=FirebaseDatabase.getInstance().getReference();

        mauth=FirebaseAuth.getInstance();
        profileimage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showimagechooser();
            }
        });

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(muploadTask != null && muploadTask.isInProgress()){
                    Toast.makeText(UpdatePhoto.this, "Image upload is in progress", Toast.LENGTH_SHORT).show();
                    return;
                }
                uploadimagetoFirebaseStorage();
            }
        });
    }

    public void showimagechooser(){
        Intent intent=new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent,"Select Profile Image"),PICK_FROM_GALLERY);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==PICK_FROM_GALLERY && resultCode ==RESULT_OK && data != null && data.getData() != null){
            uriProfileImage=data.getData();
            try{
                bitmap= MediaStore.Images.Media.getBitmap(getContentResolver(),uriProfileImage);
                profileimage.setImageBitmap(bitmap);
            }
            catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    private void uploadimagetoFirebaseStorage(){
        if(bitmap==null){
            Toast.makeText(this, "Please select an image", Toast.LENGTH_SHORT).show();
            return;
        }
        if(!isNetworkConnected()){
            Toast.makeText(this, "Internet not Connected", Toast.LENGTH_SHORT).show();
            return;
        }
        mProgress = new ProgressDialog(UpdatePhoto.this);
        mProgress.setTitle("Updating...");
        mProgress.setMessage("Please wait...");
        mProgress.setCancelable(true);
        mProgress.setIndeterminate(true);
        mProgress.show();

        final String user_id=mauth.getUid();
        mstorageref= FirebaseStorage.getInstance().getReference("Profilepics/"+System.currentTimeMillis()+".jpg");
        if(uriProfileImage != null){
            muploadTask= mstorageref.putFile(uriProfileImage).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    mstorageref.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            profileimageUrl=uri.toString();
                            //gets the download url for the uploaded image
                            assert user_id != null;//assuming userid is not null at this point
                            mdatabaseref.child("Users").child("Drivers").child(user_id).child("imageurl").setValue(profileimageUrl);
                            startActivity(new Intent(UpdatePhoto.this,DriverProfile.class));
                            finish();
                        }
                    });
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(UpdatePhoto.this, "Some Error Occurred", Toast.LENGTH_SHORT).show();
                }
            }).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {

                }
            });
        }
    }

    public void backtoprofile(View view) {
        startActivity(new Intent(this,DriverProfile.class));
    }

    private boolean isNetworkConnected() {//This method checks whether the device is connected
        //to the internet or not
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo mNetworkInfo = cm.getActiveNetworkInfo();//
        if (mNetworkInfo != null) {
            return mNetworkInfo.isAvailable() && mNetworkInfo.isConnected();//This returns true
        }
        return false;
    }
}
