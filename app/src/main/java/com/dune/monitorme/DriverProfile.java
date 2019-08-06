package com.dune.monitorme;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.*;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.*;

public class DriverProfile extends AppCompatActivity {

    FirebaseAuth mauth;

    private TextView name,email,contact,address,clicenseno,route;

    ImageView driverimage,background;
    //A Database reference represents a particular location in your Database and can be used for reading or writing data to that Database location.
    String image;
    Intent updateintent;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_driver_profile);

        mauth=FirebaseAuth.getInstance();

        FloatingActionButton floatingActionButton = findViewById(R.id.fab);
        floatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getBaseContext(),Navigation.class));
            }
        });
        name=findViewById(R.id.user_profile_name);
        email=findViewById(R.id.email);
        contact=findViewById(R.id.contact);
        address=findViewById(R.id.address);
        clicenseno=findViewById(R.id.licenseno);
        route=findViewById(R.id.route);


        driverimage=findViewById(R.id.user_profile_photo);

        background=findViewById(R.id.header_cover_image);


    }

    private void GetDriverInfo() {
        assert  mauth.getUid() != null;//assuming user is always present at this point
        DatabaseReference dref = FirebaseDatabase.getInstance().getReference("Users").child("Drivers").child(mauth.getUid());
            //database location of the desired driver/user
            dref.addListenerForSingleValueEvent(new ValueEventListener() {
                //This is useful for data that only needs to be loaded once and isn't expected to change frequently or require active listening.
                //Reading Data Once
                //In some cases it may be useful for a callback to be called once and then immediately removed.
                @SuppressLint("SetTextI18n")
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                   // This method will be called with a snapshot of the data at this location.
                    if(dataSnapshot.exists()){
                        Driver driver=dataSnapshot.getValue(Driver.class);
                        //Toast.makeText(DriverProfile.this, ""+driver.getName(), Toast.LENGTH_SHORT).show();
                        assert driver != null;//assuming driver is always present
                        name.setText("Name :- "+driver.getName());
                      email.setText("Email :- "+driver.getEmail());
                      contact.setText("Contact :- "+driver.getContact());
                        image=driver.getImageurl();
                        Glide.with(DriverProfile.this)
                                .load(image)
                                .into(driverimage);
                        clicenseno.setText("License No :- "+driver.getCLicenseno());
                        address.setText("Address :- "+driver.getAddress());
                        route.setText("Route :- "+driver.getRoute());

                        updateintent=new Intent(getBaseContext(),Update.class);
                        updateintent.putExtra("name",driver.getName());
                        updateintent.putExtra("address",driver.getAddress());
                        updateintent.putExtra("route",driver.getRoute());
                        updateintent.putExtra("contact",driver.getContact());
                        //Toast.makeText(Driverprofile.this, "okay"+name, Toast.LENGTH_SHORT).show();
                    }
                }
                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
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

    public void gotoupdatepage(View view) {
        startActivity(updateintent);
        finish();
        finishAffinity();
    }

    public void gotophotoupdatepage(View view) {
        startActivity(new Intent(this,UpdatePhoto.class));
        finish();
        finishAffinity();
    }

    @Override
    public void onBackPressed() {
        startActivity(new Intent(this,Navigation.class));
        finishAffinity();//clears every activity in the stack
        finish();//finishes every last process in this activity
    }

    @Override
    protected void onStart() {
        super.onStart();
        if(isNetworkConnected()){
            GetDriverInfo();
        }
        else{
            new AlertDialog.Builder(DriverProfile.this)
                    .setMessage("Please check your internet conncection")
                    .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                        public void onClick(final DialogInterface dialog, final int id) {
                            dialog.cancel();
                        }
                    })
                    .create()
                    .show();
        }
    }
}
