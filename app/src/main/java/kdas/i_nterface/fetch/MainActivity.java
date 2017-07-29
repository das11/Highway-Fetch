package kdas.i_nterface.fetch;

import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStates;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements
        ActivityCompat.OnRequestPermissionsResultCallback,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        com.google.android.gms.location.LocationListener{

    private GoogleApiClient mGoogleApiClient;
    private Location location;
    private final static int CONNECTION_FAILURE_RESOLUTION_REQUEST = 9000;
    private LocationRequest mLocationRequest;

    boolean chk;
    boolean fetched = false;
    boolean once = false;
    boolean flag = false;

    Location coord;

    String place,name_s, phone_s, town_s, location_name, nic_s, dic_s, cic_s, district_s, userid_s;
    EditText name, phone, town, location_name_ed, nic, dic, cic;
    RadioButton radioButton;
    TextView textView, acc_tv;

    com.wang.avi.AVLoadingIndicatorView avLoadingIndicatorView;

    FirebaseUser currentUser;
    FirebaseAuth mAuth;

    FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
    DatabaseReference root = firebaseDatabase.getReference();

    DatabaseReference district, f_town, f_name, f_phone, f_place, f_location, f_placename, f_nic, f_dic, f_cic, incharge, userid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mAuth = FirebaseAuth.getInstance();

        name = (EditText)findViewById(R.id.editText_name);
        phone = (EditText)findViewById(R.id.editText2);
        town = (EditText)findViewById(R.id.editText4);
        location_name_ed = (EditText)findViewById(R.id.editText5);

        nic = (EditText)findViewById(R.id.editText_nic);
        dic = (EditText)findViewById(R.id.editText8_dic);
        cic = (EditText)findViewById(R.id.editText7_cic);

        radioButton = (RadioButton)findViewById(R.id.radioButton);
        textView = (TextView)findViewById(R.id.textView);

        avLoadingIndicatorView = (com.wang.avi.AVLoadingIndicatorView)findViewById(R.id.loader_accuracy);
        acc_tv = (TextView)findViewById(R.id.acc_tv);

        Spinner spinner = (Spinner)findViewById(R.id.spinner);
        final List<String> places = new ArrayList<>();
        places.add("Select");
        places.add("Hospital");
        places.add("Police Station");
        places.add("Police Control Room");
        places.add("Pharmacy");
        places.add("Car service");
        places.add("Bike service");
        places.add("Public Toilet");
        places.add("Tyre Repair");
        places.add("Refueling station");
        places.add("Drinking Water");
        places.add("ATM");

        Spinner spinner_dis = (Spinner)findViewById(R.id.editText3);
        final List<String> districts = new ArrayList<>();
        districts.add("Districts");
        districts.add("Tinsukia");
        districts.add("Dibrugarh");
        districts.add("Dhemaji");
        districts.add("Charaideo");
        districts.add("Sivasagar");
        districts.add("Lakhimpur");
        districts.add("Majuli");
        districts.add("Jorhat");
        districts.add("Biswanath");
        districts.add("Golaghat");
        districts.add("Karbi Anglong East");
        districts.add("Sonitpur");
        districts.add("Nagaon");
        districts.add("Hojai");
        districts.add("Karbi Anglong West");
        districts.add("Dima Hassao");
        districts.add("Cachar");
        districts.add("Hailakandi");
        districts.add("Karimganj");
        districts.add("Morigaon");
        districts.add("Udalguri");
        districts.add("Darrang");
        districts.add("Kamrup Metro");
        districts.add("Baksa");
        districts.add("Nalbari");
        districts.add("Kamrup");
        districts.add("Barpeta");
        districts.add("Chirang");
        districts.add("Bongaigaon");
        districts.add("Goalpara");
        districts.add("Kokrajhar");
        districts.add("Dhubri");
        districts.add("South Salmara-Mankachar");


        final ArrayAdapter<String> dataAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, places);
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(dataAdapter);


        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {

                Log.d("pl", dataAdapter.getItem(i).toString() + "");
                place = dataAdapter.getItem(i).toString();
            }
            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        final ArrayAdapter<String> districtAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, districts);
        districtAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner_dis.setAdapter(districtAdapter);

        spinner_dis.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {

                district_s = districtAdapter.getItem(i).toString();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        Button submit = (Button)findViewById(R.id.btn);
        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (fetched && flag_check()){
                    name_s = name.getText().toString();
                    town_s = town.getText().toString();
                    location_name = location_name_ed.getText().toString();

                    nic_s = nic.getText().toString();
                    dic_s = dic.getText().toString();
                    cic_s = cic.getText().toString();

                    Log.d("ds", place +  name_s + phone_s + district_s + town_s + "");

                    district = root.child(district_s);
                    f_town = district.child(town_s);
                    f_place = f_town.child(place);
                    f_placename = f_place.child(location_name);
                    f_location = f_placename.child("location");
                    f_name = f_placename.child("name");
                    f_phone = f_placename.child("phone");
                    userid = f_placename.child("userod");
                    incharge = f_placename.child("Incharge");
                    f_nic = incharge.child("name");
                    f_dic = incharge.child("designation");
                    f_cic = incharge.child("phone");

                    f_name.setValue(name_s);
                    f_phone.setValue(phone_s);
                    f_location.setValue(coord);
                    f_nic.setValue(nic_s);
                    f_dic.setValue(dic_s);
                    f_cic.setValue(cic_s);
                    userid.setValue(userid_s, new DatabaseReference.CompletionListener() {
                        @Override
                        public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                            textView.setText("Location saved");
                        }
                    });

                }

            }
        });

        FloatingActionButton fab = (FloatingActionButton)findViewById(R.id.floatingActionButton);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mGoogleApiClient.connect();
                avLoadingIndicatorView.setVisibility(View.VISIBLE);
            }
        });

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();

        if (chk = checkperm()){
            mLocationRequest = LocationRequest.create()
                    .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                    .setInterval(10 * 1000)        // 10 seconds, in milliseconds
                    .setFastestInterval(4 * 1000); // 1 second, in milliseconds

            gps(mGoogleApiClient, mLocationRequest);
            Log.d("##1","1");

        }else{
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, 12);
            if (checkperm()){
                mLocationRequest = LocationRequest.create()
                        .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                        .setInterval(10 * 1000)        // 10 seconds, in milliseconds
                        .setFastestInterval(4 * 1000); // 4 seconds, in milliseconds
            }
        }

    }

//    @Override
//    protected void onStart(){
//        if (mGoogleApiClient != null)
//            mGoogleApiClient.connect();
//    }

    private boolean flag_check() {

        if (!name.getText().toString().isEmpty()
                && !town.getText().toString().isEmpty()
                && !location_name_ed.getText().toString().isEmpty()
                && !nic.getText().toString().isEmpty()
                && !dic.getText().toString().isEmpty()
                && !cic.getText().toString().isEmpty()) {

            return true;
        } else {

            Toast.makeText(getApplicationContext(), "ALL FIELDS MANDATORY", Toast.LENGTH_LONG).show();

            return false;
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mGoogleApiClient.isConnected()) {
            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
            mGoogleApiClient.disconnect();
        }
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        if (checkperm()){
            location = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
        }

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        if (connectionResult.hasResolution()) {
            try {
                // Start an Activity that tries to resolve the error
                connectionResult.startResolutionForResult(this, CONNECTION_FAILURE_RESOLUTION_REQUEST);
            } catch (IntentSender.SendIntentException e) {
                e.printStackTrace();
            }
        } else {
            Log.i("test", "Location services connection failed with code " + connectionResult.getErrorCode());
        }

    }

    @Override
    public void onLocationChanged(Location location) {
        handleLocation(location);

    }

    private void handleLocation(Location location) {

        if (location.getAccuracy() < 80 && !once){
            coord = location;
            fetched = true;

            avLoadingIndicatorView.hide();
            acc_tv.setText(coord.getAccuracy() + " M accuracy");

            textView.setText("Co-ordinates fetched");

            int color = Color.parseColor("#1D9788");
            radioButton.setButtonTintList(ColorStateList.valueOf(color));
            radioButton.toggle();
        }


        //Toast.makeText(getApplicationContext(), "Location \n" + location + "acc" + location.getAccuracy(), Toast.LENGTH_LONG).show();
        Log.d("loc", location + "");

    }

    public boolean checkperm(){
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            return false;
        }else{
            return true;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        Intent i = new Intent(MainActivity.this, help.class);
        startActivity(i);

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onStart() {
        super.onStart();
        currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            userid_s = currentUser.getUid().toString();
            phone_s = currentUser.getPhoneNumber();

            Toast.makeText(getApplicationContext(), "Already Signed-in", Toast.LENGTH_SHORT).show();
            Log.d("phone", phone_s + "");
        }else {
            startActivity(new Intent(MainActivity.this, SignIn.class));
            finish();
        }
    }

    public void gps(GoogleApiClient googleApiClient, LocationRequest locationRequest){
        Log.d("GPS", "\n\n");
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                .addLocationRequest(locationRequest);

        builder.setAlwaysShow(true);

        PendingResult<LocationSettingsResult> result = LocationServices.SettingsApi.checkLocationSettings(googleApiClient, builder.build());

        result.setResultCallback(new ResultCallback<LocationSettingsResult>() {
            @Override
            public void onResult(LocationSettingsResult result) {
                final Status status = result.getStatus();
                final LocationSettingsStates locationSettingsStates = result.getLocationSettingsStates();
                switch (status.getStatusCode()) {
                    case LocationSettingsStatusCodes.SUCCESS:
                        // All location settings are satisfied. The client can initialize location
                        // requests here.

                        break;
                    case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                        // Location settings are not satisfied. But could be fixed by showing the user
                        // a dialog.
                        try {
                            // Show the dialog by calling startResolutionForResult(),
                            // and check the result in onActivityResult().
                            status.startResolutionForResult(MainActivity.this,1000);
                        } catch (IntentSender.SendIntentException e) {
                            // Ignore the error.
                        }
                        break;
                    case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                        // Location settings are not satisfied. However, we have no way to fix the
                        // settings so we won't show the dialog.
                        break;

                    case  LocationSettingsStatusCodes.CANCELED:
                        Toast.makeText(getApplicationContext(), "We don't have a Warp Drive, so turn the GPS ON", Toast.LENGTH_LONG).show();
                }
            }
        });

    }
}
