package com.dune.monitorme;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class Message extends AppCompatActivity {

    private EditText sms,contact;
    Button send;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message);

        sms=findViewById(R.id.sms);
        contact=findViewById(R.id.contact);
        send=findViewById(R.id.send);

        send.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.KITKAT)
            @Override
            public void onClick(View v) {
                if(contact.getText().toString().isEmpty()){
                    contact.setError("Contact field cannot be empty");
                    contact.requestFocus();
                    return;
                }
                if(sms.getText().toString().isEmpty()){
                    sms.setError("Text cannot be empty");
                    sms.requestFocus();
                    return;
                }
                if (contact.getText().toString().length() != 10 ) {
                    contact.setError("Enter valid Contact Number");
                    contact.requestFocus();
                    return;
                }
                ProgressDialog mProgress = new ProgressDialog(Message.this);
                mProgress.setTitle("Creating account...");
                mProgress.setMessage("Please wait...");
                mProgress.setCancelable(false);
                mProgress.setIndeterminate(true);
                mProgress.show();//shows the progress


                    try{
                        SmsManager smsManager = SmsManager.getDefault();
                        smsManager.sendTextMessage(contact.getText().toString(), null, sms.getText().toString(), null, null);
                        mProgress.dismiss();
                        Toast.makeText(Message.this, "Message sent", Toast.LENGTH_SHORT).show();
                    }
                    catch (Exception e){
                        Toast.makeText(Message.this, "Error Occurred", Toast.LENGTH_SHORT).show();
                        mProgress.dismiss();
                    }
            }
        });
    }

    public void backtonavigation(View view) {
        startActivity(new Intent(this,Navigation.class));
        finish();
        finishAffinity();
    }
}
