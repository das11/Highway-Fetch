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
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
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

    Location coord;

    String place,name_s, phone_s, dis_s, town_s, location_name;

    EditText name, phone, dis, town, location_name_ed;
    RadioButton radioButton;
    TextView textView;

    FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
    DatabaseReference root = firebaseDatabase.getReference();

    DatabaseReference district, f_town, f_name, f_phone, f_place, f_location, f_placename;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        name = (EditText)findViewById(R.id.editText_name);
        phone = (EditText)findViewById(R.id.editText2);
        dis = (EditText)findViewById(R.id.editText3);
        town = (EditText)findViewById(R.id.editText4);
        location_name_ed = (EditText)findViewById(R.id.editText5);

        radioButton = (RadioButton)findViewById(R.id.radioButton);
        textView = (TextView)findViewById(R.id.textView);

        Spinner spinner = (Spinner)findViewById(R.id.spinner);
        final List<String> places = new ArrayList<>();
        places.add("Hospital");
        places.add("Police Station");
        places.add("Car service");
        places.add("Bike service");
        places.add("Public Toilet");
        places.add("Tyre Repair");
        places.add("Refueling station");
        places.add("Drinking Water");

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

        Button submit = (Button)findViewById(R.id.btn);
        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (fetched){
                    name_s = name.getText().toString();
                    phone_s = phone.getText().toString();
                    dis_s = dis.getText().toString();
                    town_s = town.getText().toString();
                    location_name = location_name_ed.getText().toString();

                    Log.d("ds", place +  name_s + phone_s + dis_s + town_s + "");

                    district = root.child(dis_s);
                    f_town = district.child(town_s);
                    f_place = f_town.child(place);
                    f_placename = f_place.child(location_name);
                    f_location = f_placename.child("location");
                    f_name = f_placename.child("name");
                    f_phone = f_placename.child("phone");

                    f_name.setValue(name_s);
                    f_phone.setValue(phone_s);
                    f_location.setValue(coord, new DatabaseReference.CompletionListener() {
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

        if (location.getAccuracy() < 100 && !once){
            coord = location;
            fetched = true;
            once = true;

            textView.setText("Co-ordinates fetched");

            int color = Color.parseColor("#1D9788");
            radioButton.setButtonTintList(ColorStateList.valueOf(color));
            radioButton.toggle();
        }


        Toast.makeText(getApplicationContext(), "Location \n" + location + "acc" + location.getAccuracy(), Toast.LENGTH_LONG).show();
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
}
