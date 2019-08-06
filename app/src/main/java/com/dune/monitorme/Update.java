package com.dune.monitorme;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class Update extends AppCompatActivity {

    String drivername,drivercontact,driveraddress,driverroute;
    ImageView img;
    EditText contact,name,address,route;
    DatabaseReference mdatabaseref;


    String user_id;

    FirebaseAuth mauth;

    ProgressDialog mProgress;


    @Override
    public void onBackPressed() {
        startActivity(new Intent(this,DriverProfile.class));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update);

        name=findViewById(R.id.name);
        contact=findViewById(R.id.contact);
        address=findViewById(R.id.address);
        route=findViewById(R.id.route);

        img=findViewById(R.id.profileimage);

        mdatabaseref= FirebaseDatabase.getInstance().getReference();

        mauth= FirebaseAuth.getInstance();
        assert mauth.getCurrentUser() != null;//assuming that the user is always present
        user_id=mauth.getCurrentUser().getUid();

        getdatafromintent();//getting data from intent passed from DriverProfile class
    }

    public void back(View view) {
        startActivity(new Intent(this,DriverProfile.class));
    }

    public void updateinfotofirebase(View view) {
        if(!isNetworkConnected()){
            Toast.makeText(this, "Network not Connected", Toast.LENGTH_SHORT).show();
            return;
        }
        mProgress = new ProgressDialog(Update.this);
        mProgress.setTitle("Updating...");
        mProgress.setMessage("Please wait...");
        mProgress.setCancelable(false);
        mProgress.setIndeterminate(true);
        mProgress.show();

        // progressBar.setVisibility(View.VISIBLE);
        if (name.getText().toString().isEmpty()) {
            name.setError("Name field cannot be empty!!");
            name.requestFocus();
            mProgress.dismiss();
            return;
        }
        if (contact.getText().toString().isEmpty()) {
            contact.setError("Contact field cannot be empty!!");
            contact.requestFocus();
            mProgress.dismiss();
            return;
        }

        if(address.getText().toString().isEmpty()){
            address.setError("Address field cannot be empty!!");
            address.requestFocus();
            mProgress.dismiss();
            return;
        }

        if(route.getText().toString().isEmpty()){
            route.setError("Route field cannot be empty!!");
            route.requestFocus();
            mProgress.dismiss();
            return;
        }

        assert mauth.getCurrentUser() != null;//assuming user is always present at this stage
        mdatabaseref.child("Users").child("Drivers").child(mauth.getCurrentUser().getUid()).child("name").setValue(name.getText().toString());
        mdatabaseref.child("Users").child("Drivers").child(mauth.getCurrentUser().getUid()).child("contact").setValue(contact.getText().toString());
        mdatabaseref.child("Users").child("Drivers").child(mauth.getCurrentUser().getUid()).child("address").setValue(address.getText().toString());
        mdatabaseref.child("Users").child("Drivers").child(mauth.getCurrentUser().getUid()).child("route").setValue(route.getText().toString());
        Toast.makeText(this, "Updated", Toast.LENGTH_SHORT).show();
        mProgress.dismiss();
        startActivity(new Intent(this,DriverProfile.class));
        finish();
    }

    @Override
    protected void onStart() {
        super.onStart();
        name.setText(drivername);
        contact.setText(drivercontact);
        address.setText(driveraddress);
        route.setText(driverroute);
    }

    private boolean isNetworkConnected() {
        //This method checks whether the deveice is connected
        //to the internet or not
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo mNetworkInfo = cm.getActiveNetworkInfo();//
        if (mNetworkInfo != null) {
            return mNetworkInfo.isAvailable() && mNetworkInfo.isConnected();//This returns true
        }
        return false;
    }

    private void getdatafromintent(){
        drivername=getIntent().getStringExtra("name");
        driverroute=getIntent().getStringExtra("route");
        driveraddress=getIntent().getStringExtra("address");
        drivercontact=getIntent().getStringExtra("contact");
    }



}
