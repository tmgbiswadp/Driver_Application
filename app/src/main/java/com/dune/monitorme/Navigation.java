package com.dune.monitorme;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.*;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationManager;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.*;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.*;
import android.telephony.SmsManager;
import android.util.Log;

import android.widget.*;
import com.bumptech.glide.Glide;

import com.firebase.geofire.GeoFire; //an open-source JavaScript library that allows you to store and
// query a set of items based on their geographic location.
// GeoFire uses Firebase for data storage, allowing query results to be updated in realtime as they change.

import com.firebase.geofire.GeoLocation;

import com.google.android.gms.location.*;

import android.view.View;

import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.*;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static android.content.pm.PackageManager.PERMISSION_GRANTED;

public class Navigation extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, OnMapReadyCallback, SensorEventListener, GoogleMap.OnMyLocationButtonClickListener, GoogleMap.OnMyLocationClickListener {

    DrawerLayout drawer;

    NetworkChangereceiver networkchangereceiver;
    private float[] speed = {0, 0, 0};
    private int currentSpeed = 0;

    float[] gravity = {0, 0, (float) 9.81};
    private SensorManager mSensorManager;
    private Sensor mAccelerometer;
    private AlertDialog alert,alert1;

    boolean init = false;
    Vibrator vibrator;

    private GoogleMap mMap;
    LocationRequest mLocationRequest;
    private FirebaseAuth mauth;

    private FusedLocationProviderClient mFusedLocationProviderClient;
    //The main entry point for interacting with the fused location provider.
    //By default, the Android SDK offers the Location API.
    // However, this API is not really optimized to save your battery life.
    //So, Google has created the Fused Location Provider API integrated in the Google Play Services.
    // It is a simple and battery-efficient location API for Android.
    //

    private static final String TAG = "Navigation";

    LocationCallback mLocationCallback;

    String user_id;

    LatLng latLng;

    DatabaseReference contactref;

    private TextView drivername;
    private ImageView navheaderimage;


    private boolean initialized;

    CountDownTimer timer;

    static ArrayList<String> contactlist = new ArrayList<>();//for driver's contact list
   static ArrayList<String> newlist = new ArrayList<>();//decalaring array list for new contact list


    private Marker mDriverMarker;


    private String[] permissions = {Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission
            .ACCESS_COARSE_LOCATION, Manifest.permission.SEND_SMS};

    GPSChangereceiver gpsChangereceiver;


    boolean internetchange=false;
    boolean gpschange=false;


    AlertDialog.Builder builder;

    Ringtone r;

    int p=0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_navigation);

        requestPermissions();

        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);//Create a new instance of FusedLocationProviderClient for use in this Activity.
        createLocationRequest();


        drawer = findViewById(R.id.drawer_layout);

        assert FirebaseAuth.getInstance().getCurrentUser() != null;//assuming that user is always present for this phase
        user_id = FirebaseAuth.getInstance().getCurrentUser().getUid(); //gets the user id of the signed user

        mauth = FirebaseAuth.getInstance();

        Button emergencybutton = findViewById(R.id.emergency);
        emergencybutton.setVisibility(View.VISIBLE);


        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                drawer.openDrawer(GravityCompat.START);
            }
        });

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        assert mapFragment != null;
        mapFragment.getMapAsync(this);

        //getMapAsync let you use a callback when the map is initialized
        //Obtain the map fragment and get notified when the map is ready to be used


        View header = navigationView.getHeaderView(0);
        drivername = header.findViewById(R.id.drivername);

        navheaderimage = header.findViewById(R.id.navheaderdriverimage);//finding image in navigation drawer
        emergencybutton.setVisibility(View.VISIBLE);


        contactref = FirebaseDatabase.getInstance().getReference("DriverParentGroup").child(user_id);


        emergencybutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(Navigation.this);
                builder.setMessage("Do u want to call for help??")
                        .setCancelable(false)
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            String stringLatitude = String.valueOf(latLng.latitude);
                            String stringLongitude = String.valueOf(latLng.longitude);

                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                try {
                                    String message = "I am in danger, I am at ";

                                    String urlWithPrefix = "https://www.google.com/maps/search/?api=1&query=" + stringLatitude + "," + stringLongitude;
                                    message = message + urlWithPrefix;
                                    //
                                    for (String s : newlist
                                    ) {
                                        SmsManager smsManager = SmsManager.getDefault();
                                        smsManager.sendTextMessage(s, null, message, null, null);

                                        //   Toast.makeText(Navigation.this, "really" + newlist, Toast.LENGTH_SHORT).show();
                                        // Toast.makeText(Navigation.this, "longitude: " + stringLatitude + "latitude :" + stringLongitude, Toast.LENGTH_SHORT).show();
                                    }
                                } catch (Exception e) {
                                    Toast.makeText(Navigation.this, "Message not sent", Toast.LENGTH_SHORT).show();
                                }
                                finally {
                                    Snackbar snackbar=Snackbar.make(drawer, "Help is on the way", Snackbar.LENGTH_LONG);
                                    View snackview=snackbar.getView();
                                    snackview.setBackgroundColor(Color.parseColor("#FF9C27B0"));
                                    snackbar.show();
                                }
                            }
                        }).setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                        .create()
                        .show();
            }
        });

        //Used for receiving notifications from the FusedLocationProviderApi when the device location has changed or
        // can no longer be determined. The methods are called if the LocationCallback has been registered with the
        // location client using the requestLocationUpdates(GoogleApiClient, LocationRequest, LocationCallback, Looper) method.
        //https://developers.google.com/android/reference/com/google/android/gms/location/LocationCallback

        //implementing LocationCallback constructor/method


        mLocationCallback = new LocationCallback() {
            DatabaseReference reference = FirebaseDatabase.getInstance().getReference("DriverLocation");
            //Creates a DriverLocation child in the firebase database
            @Override
            public void onLocationResult(LocationResult locationResult) {
                for (Location location : locationResult.getLocations()) {
                    try {
                        latLng = new LatLng(location.getLatitude(), location.getLongitude());
                        GeoFire geoFire = new GeoFire(reference);//uses geofire on the given path by reference above
                        geoFire.setLocation(user_id, new GeoLocation(latLng.latitude, latLng.longitude), new GeoFire.CompletionListener() {
                            //sets id,g and location timely on the firebase database
                            @Override
                            public void onComplete(String key, DatabaseError error) {
                                //timely called after the id, g and location are saved into the database
                                if (mDriverMarker != null) {
                                    mDriverMarker.setPosition(latLng);//clears the previous marker in the map
                                } else {
                                    mDriverMarker = mMap.addMarker(new MarkerOptions().position(latLng).title("Marker at ur location"));
                                }
                            }
                        });
                        speed[currentSpeed] = location.getSpeed() * (float) 3.6;
                        // Toast.makeText(Navigation.this, "" + speed[currentSpeed], Toast.LENGTH_SHORT).show();
                        if (currentSpeed < 2) {
                            currentSpeed++;
                        } else {
                            currentSpeed = 0;
                        }
                    } catch (NoSuchMethodError error) {
                        Toast.makeText(Navigation.this, "Help me", Toast.LENGTH_SHORT).show();
                    } catch (SecurityException e) {
                        Toast.makeText(Navigation.this, "Some exception occured", Toast.LENGTH_SHORT).show();
                    } finally {
                        if (!initialized) {
                            //check whether the initialized variable is true or false
                            // Since the camera was changing everytime the latitude and longitude changed
                            //this method makes the camera movement take place only once
                            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15.2f));
                            initialized = true;//sets the initialization to true
                        }
                    }
                }
            }
        };


        networkchangereceiver = new NetworkChangereceiver();
        IntentFilter filter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
        registerReceiver(networkchangereceiver, filter);//registering the receiver to detect any change in connectivity

        gpsChangereceiver = new GPSChangereceiver();
        IntentFilter filter1 = new IntentFilter(LocationManager.PROVIDERS_CHANGED_ACTION);
        registerReceiver(gpsChangereceiver, filter1);
    }

    protected void createLocationRequest() {
        //A data object that contains quality of service parameters for requests to the FusedLocationProviderApi.
        // LocationRequest objects are used to request a quality of service for location updates from the FusedLocationProviderApi.
        //Used to request location updates from FusedLocationProviderClient
        mLocationRequest = LocationRequest.create();
        mLocationRequest.setInterval(3000);//3000ms =3seconds
        mLocationRequest.setFastestInterval(3000);//
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    private void startLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PERMISSION_GRANTED) {
            requestPermissions();
            return;
        }
        if(mMap!=null){
            mMap.setMyLocationEnabled(true); //enables the device location and create a new button on top right corner
        }
        mFusedLocationProviderClient.requestLocationUpdates(mLocationRequest, mLocationCallback, Looper.myLooper());
        //Requests location updates with a callback on the specified Looper thread.
        // A Looper is basically a thread that runs in the background and does work whenever it receives message or runnable from a Handler object.
        // The main looper is part of the UI thread. Other loopers are usually created by contructing new HandlerThread and then calling thread.start(), followed by thread.getLooper().
        //LocationManager allows you to request location with a specific Looper or on the main Looper (UI thread).
    }


    private void getcontactfromfirebase() {
        contactref.addListenerForSingleValueEvent(new ValueEventListener() {
            @RequiresApi(api = Build.VERSION_CODES.KITKAT)//used for using requireNonNull since it was used starting in KITKAT
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot d : dataSnapshot.getChildren()) {
                    String contact = Objects.requireNonNull(d.child("contact").getValue()).toString();
                    contactlist.add(contact);
                    for (String b : contactlist) {
                        if (!newlist.contains(b)) {
                            newlist.add(b);
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Log.d(TAG, "Error occured:"+databaseError);
                Toast.makeText(Navigation.this, "Error occurred: -" + databaseError, Toast.LENGTH_SHORT).show();
            }
        });
    }


    @Override
    protected void onStart() {
        super.onStart();
        mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);

        mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        if(mAccelerometer != null){
            mSensorManager.registerListener(this, mAccelerometer, SensorManager.SENSOR_DELAY_NORMAL);
        }
        else{
            Toast.makeText(this, "Accelerometer not found", Toast.LENGTH_SHORT).show();
        }
        //By default, the Android SDK offers the Location API.
        // However, this API is not really optimized to save your battery life.
        //So, Google has created the Fused Location Provider API integrated in the Google Play Services.
        // It is a simple and battery-efficient location API for Android.
        //
    }


    @Override
    protected void onPause() {
        super.onPause();

    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!isNetworkConnected()) {
            Snackbar snackbar=Snackbar.make(drawer, "Connection Lost", Snackbar.LENGTH_LONG);
            View snackview=snackbar.getView();
            snackview.setBackgroundColor(Color.parseColor("#FF9C27B0"));
            snackbar.show();
        }
        else {
            Getuserdetails();
            getcontactfromfirebase();
            // startLocationUpdates();
            // Toast.makeText(this, "Network not connected", Toast.LENGTH_SHORT).show();
            mSensorManager.registerListener(this, mAccelerometer, SensorManager.SENSOR_DELAY_NORMAL);

        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(mauth.getCurrentUser()!=null){
            stopLocationUpdates();
            mSensorManager.unregisterListener(this);
            unregisterReceiver(networkchangereceiver);
            unregisterReceiver(gpsChangereceiver);
        }

    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    public void onBackPressed() {
        p++;
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            if (p == 1)
                Toast.makeText(this, "Press back button again to exit!", Toast.LENGTH_SHORT).show();
            else {
                finishAffinity();
                finish();
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.navigation, menu);
        return true;
    }

    @Override
    public boolean onNavigationItemSelected(@NotNull MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_profile) {
            finishAffinity();//clears every activity in the stack
            startActivity(new Intent(this, DriverProfile.class));
            //Toast.makeText(this, "hello bitch", Toast.LENGTH_SHORT).show();
        } else if (id == R.id.nav_recalibrate) {
            init = false;
            Snackbar snackbar=Snackbar.make(drawer, "Sensor Recaibrated", Snackbar.LENGTH_LONG);
            View snackview=snackbar.getView();
            snackview.setBackgroundColor(Color.parseColor("#FF9C27B0"));
            snackbar.show();
        } else if (id == R.id.nav_notice) {
            Toast.makeText(this, "Will be available in future update", Toast.LENGTH_SHORT).show();
        }
        else if(id==R.id.nav_message){
            startActivity(new Intent(this,Message.class));
        }
        else if (id == R.id.nav_signout) {
            stopLocationUpdates();
            finish();
            unregisterReceiver(networkchangereceiver);
            unregisterReceiver(gpsChangereceiver);
            mauth.signOut();
            startActivity(new Intent(this, DriverLogin.class));
        }
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @SuppressLint("MissingPermission")
    @Override
    public void onMapReady(GoogleMap googleMap) {
        //Trigerred when the map is ready to be used
        mMap = googleMap;
        // Add a marker in Sydney and move the camera
        //getDeviceLocation();
        try {
            // Customise map styling via JSON file
            googleMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(this, R.raw.map_styles)); //load new retro style map
            mMap.getUiSettings().setCompassEnabled(false);
            //disables the compass on the top left hand side of the screen
            //in place of standard one
            if (!arePermissionsEnabled()) {
               requestPermissions();
                return;
            }
            mMap.setMyLocationEnabled(true); //enables the device location and create a new button on top right corner
            mMap.setOnMyLocationButtonClickListener(this);
            mMap.setOnMyLocationClickListener(this);
        } catch (Resources.NotFoundException e) {  //catch statement for loading map
            Log.e(TAG, "Can't find style. Error: ", e);
        }
    }

    @Override
    public boolean onMyLocationButtonClick() {
        Snackbar snackbar=Snackbar.make(drawer, "Moving to you current location", Snackbar.LENGTH_LONG);
        View snackview=snackbar.getView();
        snackview.setBackgroundColor(Color.parseColor("#FF9C27B0"));
        snackbar.show();
        return false;
    }

    @Override
    public void onMyLocationClick(@NonNull Location location) {
        Snackbar snackbar=Snackbar.make(drawer, "Your current location", Snackbar.LENGTH_LONG);
        View snackview=snackbar.getView();
        snackview.setBackgroundColor(Color.parseColor("#FF9C27B0"));
        snackbar.show();
    }

    private void stopLocationUpdates() {
        mFusedLocationProviderClient.removeLocationUpdates(mLocationCallback);
    }

    private void buildAlertMessageNoGps() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Your GPS seems to be disabled, do you want to enable it?")//pops up a alertdialog box
                .setCancelable(false)
                //the alert dialog box is uncancelable i.e even then back button or touched on other parts of screen
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {//sets a Yes button on the dialog box
                    public void onClick(final DialogInterface dialog, final int id) {
                        startActivity(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                        //starts the location(gps) settings page of the
                        //device when yes is clicked
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    public void onClick(final DialogInterface dialog, final int id) {
                        dialog.cancel();
                        //closes the dialog box when no is clicked
                        new AlertDialog.Builder(Navigation.this)
                                .setMessage("The application will not be able to work properly.Are you sure??")
                                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                    public void onClick(final DialogInterface dialog, final int id) {
                                        dialog.cancel();
                                    }
                                })
                                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                                    public void onClick(final DialogInterface dialog, final int id) {
                                        startActivity(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                                    }
                                })
                                .create()
                                .show();
                    }
                });
        //pops up a alert message to notify
         alert1 = builder.create();
        alert1.show();
    }

    private void Getuserdetails() {
        assert mauth.getUid() !=null;//assuming that user is always present while calling this method
        DatabaseReference dref = FirebaseDatabase.getInstance().getReference("Users").child("Drivers").child(mauth.getUid());
        //Toast.makeText(this, ""+driverref, Toast.LENGTH_SHORT).show();
        dref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    Driver driver = dataSnapshot.getValue(Driver.class);
                    String image = null;
                    if (driver != null) {
                        image = driver.getImageurl();
                        drivername.setText(driver.getName());
                    }
                    //Toast.makeText(Navigation.this, ""+image, Toast.LENGTH_SHORT).show();
                    Glide.with(Navigation.this)
                            .load(image)
                            .into(navheaderimage);
                }
                //Toast.makeText(Driverprofile.this, "okay"+name, Toast.LENGTH_SHORT).show();
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
    ///Sensor events

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            // alpha is calculated as t / (t + dT)
            // with t, the low-pass filter's time-constant
            // and dT, the event delivery rate
            float alpha = (float) 0.8;
            gravity[0] = alpha * gravity[0] + (1 - alpha) * event.values[0];
            gravity[1] = alpha * gravity[1] + (1 - alpha) * event.values[1];
            gravity[2] = alpha * gravity[2] + (1 - alpha) * event.values[2];
            event.values[0] = event.values[0] - gravity[0];
            event.values[1] = event.values[1] - gravity[1];
            event.values[2] = event.values[2] - gravity[2];

            calculateShock(Math.abs(event.values[0]) + Math.abs(event.values[1]) + Math.abs(event.values[2]));
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }

    public void sendmessage() {
        String stringLatitude = String.valueOf(latLng.latitude);
        String stringLongitude = String.valueOf(latLng.longitude);
        try {
            String message = "I just had an accident, I am at ";
            String urlWithPrefix = "https://www.google.com/maps/search/?api=1&query=" +stringLatitude+ "," +stringLongitude;
            message = message + urlWithPrefix;
            //
            for (String s : newlist
            ) {
                //Log.d("susan","");
                Toast.makeText(this, "Sending message", Toast.LENGTH_SHORT).show();
                    SmsManager smsManager = SmsManager.getDefault();
                    smsManager.sendTextMessage(s, null, message, null, null);
                   // Toast.makeText(Navigation.this, "really" + newlist, Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Toast.makeText(this, "Exception occured", Toast.LENGTH_SHORT).show();
        }
        finally {
            Toast.makeText(this, "Sent", Toast.LENGTH_SHORT).show();
        }
    }

    private void setVibrate() {
        vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        long[] pattern = { 0, 2000, 2000 };
        vibrator.vibrate(pattern,0); // for 500 ms
    }

    private void setAlarm(){
        try {
            Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);
            r = RingtoneManager.getRingtone(getApplicationContext(), notification);
            r.play();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void calculateShock(float pwr) {
        //if choc < 2 m/s^2 do nothing
        if (pwr < 9) {
            return;
        }
        float AccidentProbability;
        AccidentProbability = pwr / 2;
        //We are looking for a drop in speed and average speed
        float shockDif = 0;
        for (int i = 0; i < 2; i++) {
            float tmpChocDif = speed[i + 1] - speed[i];
            if (tmpChocDif < 0) {
                if (tmpChocDif < shockDif) {
                    shockDif = tmpChocDif;
                }
            }
        }
        AccidentProbability = ((Math.abs(shockDif) / 2) + 1) * AccidentProbability;
        if (AccidentProbability > 70   ) {
              try {
                  if(!init){
                      setAlarm();
                      setVibrate();
                      shouldISendMessage();
                  }
                  else {
                      Toast.makeText(this, "Please recalibrate the sensor", Toast.LENGTH_SHORT).show();
                  }
                }
                catch (Exception e) {
                    Toast.makeText(this, "Something went wrong", Toast.LENGTH_SHORT).show();
                }
        }
    }

    public void shouldISendMessage() {
                builder=new AlertDialog.Builder(Navigation.this);
                builder.setMessage("Have u been in an accident??");
                builder.setCancelable(false);
                builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        vibrator.cancel();
                        dialog.cancel();
                        init=true;
                        timer.cancel();
                        sendmessage();
                        r.stop();
                    }
                });
                builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                        vibrator.cancel();
                        timer.cancel();
                        r.stop();
                        init=true;

                        Snackbar snackbar=Snackbar.make(drawer, "Hope you are okay", Snackbar.LENGTH_LONG);
                        View snackview=snackbar.getView();
                        snackview.setBackgroundColor(Color.parseColor("#FF9C27B0"));
                        snackbar.show();
                    }
                });
                alert=builder.create();
                alert.show();

                timer= new CountDownTimer(60000, 1000) {
                    @Override
                    public void onTick(long millisUntilFinished) {
                        alert.setMessage("You have 60 seconds before sending the emergency SMS:\n\n 00:" + (millisUntilFinished / 1000));
                    }
                    @Override
                    public void onFinish() {
                        if (alert.isShowing()) {
                            sendmessage();
                            alert.cancel();
                            vibrator.cancel();
                            timer.cancel();
                            init=true;
                            r.stop();

                            Snackbar snackbar=Snackbar.make(drawer, "Help is on the way", Snackbar.LENGTH_LONG);
                            View snackview=snackbar.getView();
                            snackview.setBackgroundColor(Color.parseColor("#FF9C27B0"));
                            snackbar.show();
                        }
                    }
                };
                timer.start();
                init=true;
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


    public class NetworkChangereceiver extends BroadcastReceiver{
        @Override
        public void onReceive(Context context, Intent intent) {
            if(!isNetworkConnected()){
                internetchange=true;//for snackbar when internet is connected
                //it was showing every time the activity was opened
                connectionlostnotification();
                stopLocationUpdates();

                Snackbar snackbar=Snackbar.make(drawer, "Not Connected", Snackbar.LENGTH_LONG);
                View snackview=snackbar.getView();
                snackview.setBackgroundColor(Color.parseColor("#FF9C27B0"));
                snackbar.show();
            }

            if(arePermissionsEnabled() && isNetworkConnected()) {
                startLocationUpdates();
                if (internetchange) {
                    Snackbar snackbar = Snackbar.make(drawer, "Connected", Snackbar.LENGTH_LONG);
                    View snackview = snackbar.getView();
                    snackview.setBackgroundColor(Color.parseColor("#FF9C27B0"));
                    snackbar.show();
                }
                internetchange=false;

            }
            if(!arePermissionsEnabled()){
                requestPermissions();
            }

        }
    }

    public class GPSChangereceiver extends BroadcastReceiver{
        @Override
        public void onReceive(Context context, Intent intent) {
            LocationManager manager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
            if (!manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {//checks whether th gps is enabled or not and if not enabled calls the method below
                gpschange=true;
                gpsnotavailablenotification();
                Snackbar snackbar=Snackbar.make(drawer, "GPS Offline", Snackbar.LENGTH_LONG);
                View snackview=snackbar.getView();
                snackview.setBackgroundColor(Color.parseColor("#FF9C27B0"));
                snackbar.show();

                buildAlertMessageNoGps();//pops up alert box
            }

            if(arePermissionsEnabled() && gpsstatus() && isNetworkConnected()){
                startLocationUpdates();
                if(alert1!=null){
                    alert1.cancel();
                }
                if(gpschange){
                    Snackbar snackbar=Snackbar.make(drawer, "GPS online", Snackbar.LENGTH_LONG);
                    View snackview=snackbar.getView();
                    snackview.setBackgroundColor(Color.parseColor("#FF9C27B0"));
                    snackbar.show();
                }
                gpschange=false;
            }
            if(!arePermissionsEnabled()){
                requestPermissions();
            }
        }
    }

    private boolean arePermissionsEnabled(){
        for(String permission : permissions){
            if(ActivityCompat.checkSelfPermission(this,permission) != PERMISSION_GRANTED)
                return false;
        }
        return true;
    }

    private boolean gpsstatus(){
        LocationManager manager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        //checks whether the gps is enabled or not
        return manager.isProviderEnabled(LocationManager.GPS_PROVIDER);
    }

    private void GPSstatuscheck(){
        LocationManager manager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (!manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {//checks whether th gps is enabled or not and if not enabled calls the method below
            buildAlertMessageNoGps();//pops up alert box
        }
    }

    private void requestPermissions(){
        if(arePermissionsEnabled()){
            GPSstatuscheck();
        }
        else{
            ArrayList<String> remainingPermissions = new ArrayList<>();
            for (String permission : permissions) {
                if (ActivityCompat.checkSelfPermission(this,permission) != PERMISSION_GRANTED) {
                    remainingPermissions.add(permission);
                }
            }
            ActivityCompat.requestPermissions(this,remainingPermissions.toArray(new String[1]), 101);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,@NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(grantResults.length > 0 && grantResults[0]==PackageManager.PERMISSION_GRANTED){
            GPSstatuscheck();//get application context is used to know whether the gps is already turned on or not
            if(gpsstatus()){
                startLocationUpdates();
            }
        }
        if(requestCode == 101){
            for(int i=0;i<grantResults.length;i++){
                if(grantResults[i] != PERMISSION_GRANTED){
                    if(ActivityCompat.shouldShowRequestPermissionRationale(this,permissions[i])){
                        new AlertDialog.Builder(this)
                                .setMessage("The application will not be able t o work properly.\nDo you want to continue??")
                                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                    public void onClick(final DialogInterface dialog, final int id) {
                                        dialog.cancel();
                                    }
                                })
                                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                                    public void onClick(final DialogInterface dialog, final int id) {
                                        requestPermissions();

                                    }
                                })
                                .create()
                                .show();
                    }
                    return;
                }
            }
        }
    }

    private void gpsnotavailablenotification(){
        String channelid="GPS";
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this,channelid)
                .setContentTitle("GPS is Off")
                .setContentText("Please turn on the gps. The parents might not be able to monitor your location")
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setStyle(new NotificationCompat.BigTextStyle())
                .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
                .setSmallIcon(R.drawable.ic_notifications_black_24dp)
                .setAutoCancel(true);

        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
        {
            NotificationChannel channel = new NotificationChannel(channelid, "Default notification", NotificationManager.IMPORTANCE_DEFAULT);
            notificationManager.createNotificationChannel(channel);
        }

        notificationManager.notify(1, notificationBuilder.build());
    }

    void connectionlostnotification(){
        String channelid="Internet";
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this,channelid)
                .setContentTitle("Internet Connection lost")
                .setContentText("It looks like the device lost the internet connection")
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setStyle(new NotificationCompat.BigTextStyle())
                .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
                .setSmallIcon(R.drawable.ic_notifications_black_24dp)
                .setAutoCancel(true);

        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
        {
            NotificationChannel channel = new NotificationChannel(channelid, "Default notification", NotificationManager.IMPORTANCE_DEFAULT);
            notificationManager.createNotificationChannel(channel);
        }
        notificationManager.notify(0, notificationBuilder.build());
    }
}



