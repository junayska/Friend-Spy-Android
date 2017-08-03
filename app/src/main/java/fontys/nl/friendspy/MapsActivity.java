package fontys.nl.friendspy;

import android.Manifest;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;

import com.google.android.gms.location.LocationListener;

import android.location.Location;
import android.location.LocationManager;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.UUID;

import static android.widget.Toast.LENGTH_SHORT;

public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback,
                                                    GoogleApiClient.OnConnectionFailedListener,
                                                    GoogleApiClient.ConnectionCallbacks,
                                                    LocationListener {
    public static final String ANONYMOUS = "anonymous";
    public static final String TAG_FIREBASE = "firebase_db";
    public static final String USERS_CHILD = "users";
    public static final String NAME_CHILD = "name";
    public static final String EMAIL_CHILD = "email";
    public static final String LONGITUDE_CHILD = "longitude";
    public static final String LATITUDE_CHILD = "latitude";
    public static final int TRIGGER = 10;
    private Double ownLatitude = 36.217522;
    private Double ownLongitude = 37.150189;
    private String mUsername;
    private GoogleMap mMap;
    GoogleApiClient mGoogleApiClient;
    Location mLastLocation;
    Marker mCurrLocationMarker;
    LocationRequest mLocationRequest;
    Vibrator mVibrator;
    SharedPreferences mSharedPreferences;
    LocationManager lm;
    ArrayList<String> notificationList;
    ArrayList<String> tempFriendsList;
    ArrayList<String> friendsList;
    ContextUser user;

    FirebaseMessagingService fms;

    boolean isPushed = false;
    boolean isInternet = false;


    // Firebase variables
    FirebaseAuth mFirebaseAuth;
    private FirebaseUser mFirebaseUser;

    // Firebase references
    DatabaseReference mRootRef;
    DatabaseReference mUser;
    DatabaseReference mFriendRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        mVibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);

        notificationList = new ArrayList<>();
        tempFriendsList = new ArrayList<>();

        // Set default username is anonymous.
        mUsername = ANONYMOUS;

        // Here, thisActivity is the current activity
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.ACCESS_FINE_LOCATION)) {

                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.

            } else {

                // No explanation needed, we can request the permission.

                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, MY_PERMISSIONS_REQUEST_LOCATION);

                // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
                // app-defined int constant. The callback method gets the
                // result of the request.
            }
        }

        // Initialize Firebase Auth
        mFirebaseAuth = FirebaseAuth.getInstance();
        mFirebaseUser = mFirebaseAuth.getCurrentUser();

        if (mFirebaseUser == null) {
            // Not signed in, launch the Sign In activity
            try {
                startActivity(new Intent(this, SignInActivity.class));
                finish();
                return;
            }catch (Exception e){
            }

        } else {
            getSupportActionBar().setTitle(mFirebaseUser.getDisplayName());
            user = ContextUser.getInstance();
            mUsername = mFirebaseUser.getDisplayName();
            mRootRef = FirebaseDatabase.getInstance().getReference();
            mUser = mRootRef.child(USERS_CHILD);
            mFriendRef = FirebaseDatabase.getInstance().getReference()
                    .child(USERS_CHILD).child(mFirebaseUser.getUid()).child("friends");
            user.setId(mFirebaseUser.getUid());
            user.setEmail(mFirebaseUser.getEmail());
            user.setName(mFirebaseUser.getDisplayName());
        }

        mFriendRef.addListenerForSingleValueEvent(
                new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        for(DataSnapshot snapshot : dataSnapshot.getChildren()) {
                            tempFriendsList.add(snapshot.getValue(String.class));
                        }

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                }
        );
        user.setFriends(tempFriendsList);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            checkLocationPermission();
        }
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.sign_out_menu:
                mFirebaseAuth.signOut();
                mUsername = ANONYMOUS;
                stopLocationUpdates();
                startActivity(new Intent(this, SignInActivity.class));
                finish();
                return true;
            case R.id.friends_menu:
                if (isPushed) {
                    stopLocationUpdates();
                    startActivity(new Intent(this, FriendsActivity.class));
                    finish();
                } else {
                    Toast.makeText(MapsActivity.this, "Please, turn on your location!", LENGTH_SHORT).show();
                }
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void startFirebaseListener(){
        // Firebase database references listeners
        mRootRef.child(USERS_CHILD).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                ArrayList<User> usersList = new ArrayList<>();
                mMap.clear();

                friendsList = user.getFriends();

                for (DataSnapshot usersSnapshot : dataSnapshot.getChildren()) {
                    try {
                        User urs = usersSnapshot.getValue(User.class);
                        if (urs.getEmail().equals(mFirebaseUser.getEmail())) {
                            showCurrentUserOnMap(urs);
                        }

                        for (String friend : friendsList){
                            if (urs.getEmail().equals(friend)){
                                usersList.add(urs);
                            }
                        }

                    } catch (Exception e) {
                    }
                }
                showUsersOnMap(usersList);
                usersList.clear();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();


    }

    public void showUsersOnMap(ArrayList<User> users) {

        Location myLocation = new Location(mFirebaseUser.getDisplayName());
        myLocation.setLatitude(ownLatitude);
        myLocation.setLongitude(ownLongitude);

        for (User user : users) {
            if (!(user.getLatitude() == null) && !(user.getLongitude() == null)) {
                float tempDistance = calculateDistance(user, myLocation);
                double distanceAsDouble = (double) tempDistance;
                distanceAsDouble = Math.round(distanceAsDouble);

                if (tempDistance <= TRIGGER && !(isNotificated(user.getEmail()))) {
                    notificate(user.getName(), distanceAsDouble);
                    addToNotificatedList(user.getEmail());
                }
                if (tempDistance > TRIGGER && isNotificated(user.getEmail())){
                    removeFromTheNotificatedList(user.getEmail());
                }
            }

            //Place current location marker
            LatLng latLng = new LatLng(user.getLatitude(), user.getLongitude());
            MarkerOptions markerOptions = new MarkerOptions();
            markerOptions.position(latLng);
            double distanceAsDouble = distance(user, myLocation);
            markerOptions.title(user.getName()+" "+distanceAsDouble+"m");
            markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));
            mCurrLocationMarker = mMap.addMarker(markerOptions);
        }
    }

    public void showCurrentUserOnMap(User userr) {

        ownLatitude = userr.getLatitude();
        ownLongitude = userr.getLongitude();

        //Place current location marker
        LatLng latLng = new LatLng(userr.getLatitude(), userr.getLongitude());
        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(latLng);
        markerOptions.title(user.getName());
        markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_MAGENTA));
        mCurrLocationMarker = mMap.addMarker(markerOptions);

        //move map camera
        mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
        mMap.animateCamera(CameraUpdateFactory.zoomTo(11));
    }

    public double distance(User friend, Location location){
        Location tempLocation = new Location("Friend");
        tempLocation.setLatitude(friend.getLatitude());
        tempLocation.setLongitude(friend.getLongitude());

        float tempDistance = location.distanceTo(tempLocation);
        float distance = tempDistance / 1000;
        double distanceAsDouble = (double) tempDistance;
        distanceAsDouble = Math.round(distanceAsDouble);

        return distanceAsDouble;
    }

    public float calculateDistance(User user, Location tempLocation2) {
        Location tempLocation = new Location(user.getName());
        tempLocation.setLatitude(user.getLatitude());
        tempLocation.setLongitude(user.getLongitude());

        float tempDistance = tempLocation2.distanceTo(tempLocation);
        //float distance = tempDistance / 1000;
        return tempDistance;
    }

    public void addToNotificatedList(String email){
        notificationList.add(email);
    }

    public void removeFromTheNotificatedList (String email){
        for (int i = 0; i < notificationList.size(); i++) {
            if (email.equals(notificationList.get(i))) {
                notificationList.remove(i);
            }
        }
    }

    public boolean isNotificated(String email) {
        for (int i = 0; i < notificationList.size(); i++) {
            if (email.equals(notificationList.get(i))) {
                return true;
            }
        }
        return false;
    }

    public void notificate(String name, double distance){
        mVibrator.vibrate(100);
        try {
            Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
            Ringtone r = RingtoneManager.getRingtone(getApplicationContext(), notification);
            r.play();
        } catch (Exception e) {
        }
        Toast.makeText(MapsActivity.this, name + getString(R.string.is_near)
                + " Only " + distance + " meters!", LENGTH_SHORT).show();

        //Handling notifications
        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(this)
                        .setSmallIcon(R.drawable.ic_audiotrack)
                        .setContentTitle("Someone near!")
                        .setContentText(name + getString(R.string.is_near)
                                + " Only " + distance + " meters!");

                // Creates an explicit intent for an Activity in your app
                Intent resultIntent = new Intent(this, MapsActivity.class);

        // The stack builder object will contain an artificial back stack for the
        // started Activity.
        // This ensures that navigating backward from the Activity leads out of
        // your application to the Home screen.
                TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
        // Adds the back stack for the Intent (but not the Intent itself)
                stackBuilder.addParentStack(MapsActivity.class);
        // Adds the Intent that starts the Activity to the top of the stack
                stackBuilder.addNextIntent(resultIntent);
                PendingIntent resultPendingIntent =
                        stackBuilder.getPendingIntent(
                                0,
                                PendingIntent.FLAG_UPDATE_CURRENT
                        );
        mBuilder.setContentIntent(resultPendingIntent);
        int mNotificationId = 001;


        // Gets an instance of the NotificationManager service
        NotificationManager mNotifyMgr =
                (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        // Builds the notification and issues it.
        mNotifyMgr.notify(mNotificationId, mBuilder.build());

    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);

        //Initialize Google Play Services
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(this,
                    android.Manifest.permission.ACCESS_FINE_LOCATION)
                    == PackageManager.PERMISSION_GRANTED) {
                buildGoogleApiClient();
                mMap.setMyLocationEnabled(true);
            }
        } else {
            buildGoogleApiClient();
            mMap.setMyLocationEnabled(true);
        }
    }

    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        mGoogleApiClient.connect();
    }

    @Override
    public void onLocationChanged(Location location) {
        mLastLocation = location;
        ownLatitude = location.getLatitude();
        ownLongitude = location.getLongitude();

        if(!isPushed){
            mUser.child(mFirebaseUser.getUid())
                    .child(NAME_CHILD).setValue(mFirebaseUser.getDisplayName());
            mUser.child(mFirebaseUser.getUid())
                    .child(EMAIL_CHILD).setValue(mFirebaseUser.getEmail());
            startFirebaseListener();
            isPushed = true;
        }

        if (!mUsername.equals(ANONYMOUS)){
            try {
                mUser.child(mFirebaseUser.getUid())
                        .child(LONGITUDE_CHILD).setValue(location.getLongitude());
                mUser.child(mFirebaseUser.getUid())
                        .child(LATITUDE_CHILD).setValue(location.getLatitude());
            } catch (Exception e){
                Log.e(TAG_FIREBASE, e.toString());
            }
        }
    }

    public void stopLocationUpdates() {
        if (mGoogleApiClient != null) {
            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
        }
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(1000);
        mLocationRequest.setFastestInterval(1000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
        }
    }

    @Override
    public void onConnectionSuspended(int i) {    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // permission was granted. Do the
                    // contacts-related task you need to do.
                    if (ContextCompat.checkSelfPermission(this,
                            Manifest.permission.ACCESS_FINE_LOCATION)
                            == PackageManager.PERMISSION_GRANTED) {
                        if (mGoogleApiClient == null) {
                            buildGoogleApiClient();
                        }
                        mMap.setMyLocationEnabled(true);
                    }
                } else {

                    // Permission denied, Disable the functionality that depends on this permission.
                    Toast.makeText(this, "permission denied", Toast.LENGTH_LONG).show();
                }
            }

            // other 'case' lines to check for other permissions this app might request.
            // You can add here other case statements according to your requirement.
        }
    }
    public static final int MY_PERMISSIONS_REQUEST_LOCATION = 99;
    public boolean checkLocationPermission(){
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            // Asking user if explanation is needed
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.ACCESS_FINE_LOCATION)) {

                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.

                //Prompt the user once explanation has been shown
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        MY_PERMISSIONS_REQUEST_LOCATION);
            } else {
                // No explanation needed, we can request the permission.
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        MY_PERMISSIONS_REQUEST_LOCATION);
            }
            return false;
        } else {
            return true;
        }
    }

}