package com.dune.monitorme;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;


public class DriverRegister extends AppCompatActivity {
    //private ProgressBar progressBar;

    private EditText inputEmail,inputPassword,inputfullname,inputcontact,inputcitizen,inputaddress,inputroute;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_driver_register);

        //Initializing fireabsae authentication
        //Firebase Auth is used to let users authenticate with firebase using
        // their email address,phone numbers and popular identity providers like
        //Google,Facebook,twitter,etc
        // and passwords
        //variable for firebaseauth

        Button btnRegister = findViewById(R.id.buttonRegister);
        Button btnLogin = findViewById(R.id.buttonLogin);
        inputEmail=findViewById(R.id.email);
        inputPassword=findViewById(R.id.password);
        inputfullname=findViewById(R.id.fullName);
        inputcontact=findViewById(R.id.Contact);
        inputcitizen=findViewById(R.id.citizenshipno);
        inputaddress=findViewById(R.id.Address);
        inputroute=findViewById(R.id.route);
        //progressBar=findViewById(R.id.progressBar);


        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(DriverRegister.this,DriverLogin.class));
            }
        }
        );
        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!isNetworkConnected()){
                    //checks the internet connectivity
                    Toast.makeText(DriverRegister.this, "Please check your internet connection", Toast.LENGTH_SHORT).show();
                    return;
                }
                    final String fullname = inputfullname.getText().toString().trim();
                    //gets value from fullname edittext and set it to fullname
                    //trim removes the side spaces if any
                    final String clicenseno = inputcitizen.getText().toString().trim();
                    final String email = inputEmail.getText().toString().trim();
                    final String password = inputPassword.getText().toString().trim();
                    final String contact = inputcontact.getText().toString().trim();
                    final String address = inputaddress.getText().toString().trim();
                    final String route = inputroute.getText().toString().trim();

                    if (TextUtils.isEmpty(fullname)) {//checks whether the input fullname is empty or not
                        inputfullname.setError("Name field cannot be empty!");
                        inputfullname.requestFocus();
                        return;
                    }
                    if (TextUtils.isEmpty(clicenseno)) {//checks whether the input licenseno is empty or not
                        inputcitizen.setError("License Number field cannot be empty!");
                        inputcitizen.requestFocus();
                        return;
                    }
                    if (TextUtils.isEmpty(email)) {//checks whether the input email is email or not
                        inputEmail.setError("Email field cannot be empty!");
                        inputEmail.requestFocus();
                        return;
                    }
                    if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {//checks whether the input email is email or not
                        inputEmail.setError("Enter valid email address!");
                        inputEmail.requestFocus();
                        return;
                    }
                    if (TextUtils.isEmpty(password)) {//checks whether the input contact value is empty or not
                        inputPassword.setError("Password field cannot be empty!");
                        inputPassword.requestFocus();
                        return;
                    }
                    if (TextUtils.isEmpty(contact)) {//checks whether the input contact value is empty or not
                        inputcontact.setError("Contact field cannot be empty!");
                        inputcontact.requestFocus();
                        return;
                    }
                    if(contact.length()!=10){//checks whether the the input contact length is 10 or not
                        inputcontact.setError("Input Valid Contact!");
                        inputcontact.requestFocus();
                        return;
                    }
                    if (TextUtils.isEmpty(address)) {//checks whether the input address value is email or not
                        inputaddress.setError("Address field cannot be empty!");
                        inputaddress.requestFocus();
                        return;
                    }
                    if (TextUtils.isEmpty(route)) {//checks whether the input route value is empty or not
                        inputroute.setError("Route field cannot be empty!");
                        inputroute.requestFocus();
                        return;
                    }
                    if (password.length() < 6) {//checks the lenth of the password
                        Toast.makeText(getApplicationContext(), "Password too short, enter more than 6 characters!", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    writeNewUser(address, clicenseno, contact, email, fullname, password, route);
                    //invoking the writeNewUser method with parameter from edit text.
            }

        });
    }
    private void writeNewUser(String address,String clicenseno, String contact,String email,String name,String password,String route) {//method to create a new driver user in the firebase database
        Intent imageintent=new Intent(this,Image.class);//An intent is an abstract description of an operation to be performed
        // Intents in android offer this convenient way to pass data between activities using Extras.
        imageintent.putExtra("contact", contact);
        imageintent.putExtra("email", email);
        imageintent.putExtra("name", name);
        imageintent.putExtra("password",password);
        imageintent.putExtra("address", address);
        imageintent.putExtra("licenseno", clicenseno);
        imageintent.putExtra("route", route);
        //This string can later be extracted using getExtra method
        startActivity(imageintent);
        finish();
        finishAffinity();//clears every activity in the stack

        //An Intent provides a facility for performing late runtime binding between the code in different applications.
        // Its most significant use is in the launching of activities, where it can be thought of as the glue between activities.
        // It is basically a passive data structure holding an abstract description of an action to be performed.
        //https://developer.android.com/reference/android/content/Intent

        //We can start adding data into the Intent object,
        // we use the method defined in the Intent class putExtra() or putExtras() to
        // store certain data as a key value pair or Bundle data object.
        // These key-value pairs are known as Extras in the sense we are talking about Intents.

    }
    private boolean isNetworkConnected() {//This method checks whether the deveice is connected
        //to the internet or not
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo mNetworkInfo = cm.getActiveNetworkInfo();//
        if (mNetworkInfo != null) {
            return mNetworkInfo.isAvailable();//This returns true
        }
        return false;
    }

}


