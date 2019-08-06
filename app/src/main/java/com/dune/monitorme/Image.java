package com.dune.monitorme;

import android.Manifest;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.*;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.*;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class Image extends AppCompatActivity {
    private StorageTask muploadTask;
    private static final int PICK_FROM_GALLERY = 1;
    private ImageView profileimage;
    Uri uriProfileImage;
    String profileimageUrl;
    private FirebaseAuth mauth;

    private StorageReference mstorageref;
    private DatabaseReference mdatabaseref;

    Bitmap bitmap;

    private String name,contact,email,password,address,licenseno,route;
    private String EXTERNAL_STORAGE=Manifest.permission.READ_EXTERNAL_STORAGE;
    private static final int MY_PERMISSIONS_REQUEST_EXTERNAL_STORAGE= 99;
    String user_id;

    boolean mstoragepermission;


    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image);

        profileimage=findViewById(R.id.profileimage);

        mdatabaseref= FirebaseDatabase.getInstance().getReference();

        Button save = findViewById(R.id.savetofirebase);

        mauth=FirebaseAuth.getInstance();


        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(isNetworkConnected()){
                    if(muploadTask != null && muploadTask.isInProgress()){
                        Toast.makeText(Image.this, "Image upload is in progress", Toast.LENGTH_SHORT).show();
                    }
                    else {
                        createuser();
                    }
                }
                else {
                    new AlertDialog.Builder(Image.this)
                            .setMessage("Please chech your internet connection")
                            .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                                public void onClick(final DialogInterface dialog, final int id) {
                                    dialog.cancel();
                                }
                            })
                            .create()
                            .show();
                }

            }
        });
        profileimage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showimagechooser();
            }
        });
    }

    private void getdriverdetails(){
        email= getIntent().getStringExtra("email");
        contact= getIntent().getStringExtra("contact");
        password= getIntent().getStringExtra("password");
        name= getIntent().getStringExtra("name");
        address= getIntent().getStringExtra("address");
        licenseno= getIntent().getStringExtra("licenseno");
        route= getIntent().getStringExtra("route");
        //gets data from Driver Register activity
        Log.d("Image", "getdriverdetails: "+email+contact+password+name+address+licenseno+route);
    }

    @Override
    protected void onStart() {
        super.onStart();
        checkstoragepermission();
        getdriverdetails();
    }

    public void checkstoragepermission() {
        if (ContextCompat.checkSelfPermission(Image.this,
                EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {

            // Permission is not granted for the first time
            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(Image.this,
                    EXTERNAL_STORAGE)) {
                new AlertDialog.Builder(this)
                        .setTitle("Storage Permission")
                        .setMessage("Let this app access the permission")
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                //Prompt the user once explanation has been shown
                                ActivityCompat.requestPermissions(Image.this,
                                        new String[]{EXTERNAL_STORAGE},
                                        MY_PERMISSIONS_REQUEST_EXTERNAL_STORAGE);
                            }
                        })
                        .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        })
                        .create()
                        .show();
                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.
            } else {
                ActivityCompat.requestPermissions(Image.this,
                        new String[]{EXTERNAL_STORAGE},
                        MY_PERMISSIONS_REQUEST_EXTERNAL_STORAGE);

            }
        } else {
           // statusCheck();
            mstoragepermission=true;
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        // If request is cancelled, the result arrays are empty.
        if (requestCode == MY_PERMISSIONS_REQUEST_EXTERNAL_STORAGE) {
            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Storage Permission granted", Toast.LENGTH_SHORT).show();
                // permission was granted, yay! Do the
                // contacts-related task you need to do.
                mstoragepermission=true;
            } else {
                // permission denied, boo! Disable the
                // functionality that depends on this permission.
                Toast.makeText(this, "Not granted", Toast.LENGTH_SHORT).show();
            }
            // other 'case' lines to check for other
            // permissions this app might request.
        }
    }

    public void showimagechooser(){
        if(mstoragepermission){
            Intent intent=new Intent();
            intent.setType("image/*");
            intent.setAction(Intent.ACTION_GET_CONTENT);
            startActivityForResult(Intent.createChooser(intent,"Select Profile Image"),PICK_FROM_GALLERY);
            return;
        }
        checkstoragepermission();
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
        mstorageref=FirebaseStorage.getInstance().getReference("Profilepics/"+System.currentTimeMillis()+".jpg");
        if(bitmap != null){
            //checking whether the user selected a picture or not
            muploadTask=mstorageref.putFile(uriProfileImage).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    mstorageref.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            profileimageUrl=uri.toString();
                            //gets the download url for the uploaded image
                            Driver driver=new Driver(address,licenseno,contact,email,profileimageUrl,name,md5(password),route);//creating driver object
                            mdatabaseref.child("Users").child("Drivers").child(user_id).setValue(driver);//uploading driver object to database
                            Toast.makeText(Image.this, "Uploaded", Toast.LENGTH_SHORT).show();
                            startActivity(new Intent(Image.this,DriverLogin.class));
                            finish();
                        }
                    });
                }
            });
        }
    }

    private void createuser(){
            if(bitmap==null){
                Toast.makeText(this, "Please select a image", Toast.LENGTH_SHORT).show();
            }
            else {
                final ProgressDialog mProgress = new ProgressDialog(Image.this);
                mProgress.setTitle("Creating account...");
                mProgress.setMessage("Please wait...");
                mProgress.setCancelable(true);
                mProgress.setIndeterminate(true);
                mProgress.show();//shows the progress dialog

                mauth.createUserWithEmailAndPassword(email, password)//create user
                        .addOnCompleteListener(Image.this, new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if (!task.isSuccessful()) {
                                    mProgress.dismiss();
                                    AlertDialog.Builder builder = new AlertDialog.Builder(Image.this);
                                    builder.setMessage("Registration Failed")
                                            .setNegativeButton("Retry", null)
                                            .create()
                                            .show();
                                } else {
                                    assert  mauth.getCurrentUser() != null;//assuming mauth.getCurrentUser is never null
                                    user_id = mauth.getCurrentUser().getUid();
                                    uploadimagetoFirebaseStorage();
                                }
                            }
                        });
            }
    }
    private boolean isNetworkConnected() {//This method checks whether the deveice is connected
        //to the internet or not
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo mNetworkInfo = cm.getActiveNetworkInfo();//
        if (mNetworkInfo != null) {
            return mNetworkInfo.isAvailable() && mNetworkInfo.isConnected();//This returns true
        }
        return false;
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    public void backtoregister(View view) {
        startActivity(new Intent(this,DriverRegister.class));
    }

    public String md5(String s) {
        try {
            // Create MD5 Hash
            MessageDigest digest = java.security.MessageDigest.getInstance("MD5");
            digest.update(s.getBytes());
            byte[] messageDigest = digest.digest();

            // Create Hex String
            StringBuilder hexString = new StringBuilder();
            for (byte b : messageDigest) hexString.append(Integer.toHexString(0xFF & b));

            return hexString.toString();
        }catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return "";
    }
}
