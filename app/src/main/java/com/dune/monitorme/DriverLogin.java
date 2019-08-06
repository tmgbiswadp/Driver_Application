package com.dune.monitorme;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class DriverLogin extends AppCompatActivity {

    private EditText inputEmail,inputPassword;
    //Input textfiedl for email and password
    private FirebaseAuth auth;
    //button for login,register and reset
    int p=0;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_driver_login);

        inputEmail =  findViewById(R.id.email);
        inputPassword =  findViewById(R.id.password);
        Button btnRegister = findViewById(R.id.buttonRegister);
        //Firebase Authentication provides backend services, easy-to-use SDKs, and
        // ready-made UI libraries to authenticate users to your app.
        // It supports authentication using passwords, phone numbers,
        // popular federated identity providers like Google, Facebook and Twitter, and more.
        Button btnLogin = findViewById(R.id.buttonLogin);
        Button btnReset = findViewById(R.id.buttonReset);

       // Most apps need to know the identity of a user. Knowing a user's
        // identity allows an app to securely save user data in the cloud
        // and provide the same personalized experience across all of the user's devices.
        //Firebase Authentication provides backend services, easy-to-use SDKs, and
        // ready-made UI libraries to authenticate users to your app.
        // It supports authentication using passwords, phone numbers,
        // popular federated identity providers like Google, Facebook and Twitter, and more.
        auth=FirebaseAuth.getInstance();  //Get firebase authentication instance

        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(DriverLogin.this, DriverRegister.class));
            }
        });
        btnReset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(DriverLogin.this, ResetPassword.class));
            }
        });
        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(isNetworkConnected()){
                    final String email = inputEmail.getText().toString();
                    final String password = inputPassword.getText().toString();

                    if (TextUtils.isEmpty(email)) { //
                        inputEmail.setError("Email field cannot be empty");
                        inputEmail.requestFocus();
                        return;
                    }
                    if (TextUtils.isEmpty(password)) {
                        inputPassword.setError("Password field cannot be empty");
                        inputPassword.requestFocus();
                        return;
                    }
                    //displays a dialog after the Login button is pressed
                    final ProgressDialog mProgress = new ProgressDialog(DriverLogin.this);
                    mProgress.setTitle("Authenticating...");
                    mProgress.setMessage("Please wait...");
                    mProgress.setCancelable(false);
                    mProgress.setIndeterminate(true);
                    mProgress.show();
                    //authenticate user
                    auth.signInWithEmailAndPassword(email, password)
                            .addOnCompleteListener(DriverLogin.this, new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {
                                    //trigerred after the signin process is completed
                                    // If sign in fails, display a message to the user.
                                    mProgress.dismiss();//dismiss the progress dialog box
                                    if (!task.isSuccessful()) {
                                            AlertDialog.Builder builder = new AlertDialog.Builder(DriverLogin.this);
                                            builder.setMessage("Authentication failed, check your email and password")
                                                    .setCancelable(false)
                                                    .setNegativeButton("Retry", null)
                                                    .create()
                                                    .show();
                                    } else {
                                        Intent intent = new Intent(DriverLogin.this, Navigation.class);
                                        startActivity(intent);
                                        finish();
                                    }
                                }
                            });
                }

                else {
                    AlertDialog.Builder builder = new AlertDialog.Builder(DriverLogin.this);
                    builder.setCancelable(false)
                            .setMessage("Please check your network connection")
                            .setPositiveButton("Ok", null)
                            // A null listener allows the button to dismiss the dialog and take no further action.
                            .create()
                            .show();
                }
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        if(auth.getCurrentUser() != null ){
            startActivity(new Intent(this,Navigation.class));
            finish();
        }
    }
    private boolean isNetworkConnected() {//This method checks whether the deveice is connected
        //to the internet or not
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo mNetworkInfo = cm.getActiveNetworkInfo();//
        if (mNetworkInfo != null)
            return  mNetworkInfo.isConnected();//This returns true
        return false;
    }

    @Override
    public void onBackPressed() {
        p++;
        if (p==1)
            Toast.makeText(this, "Press back button again to exit!", Toast.LENGTH_SHORT).show();
        else
            finishAffinity();
    }
}

