package start.koushikbaktha.emc;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.Api;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.FileDescriptor;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback,GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    private GoogleMap mMap;
    private GoogleApiClient mGoogleApiClient;
    private Location mLastLocation;
    private Button toBarker;
    private Button toSears;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        toBarker = (Button) findViewById(R.id.toBarker);
        toSears = (Button) findViewById(R.id.toSears);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        buildGoogleApiClient();
    }
    @Override
    protected void onStart() {
        super.onStart();
        mGoogleApiClient.connect();

    }
    @Override
    protected void onPause() {
        super.onPause();
        mGoogleApiClient.disconnect();
    }
    protected synchronized void buildGoogleApiClient() {
        // Create an instance of GoogleAPIClient.
        if (mGoogleApiClient == null)

        {
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
        }


    }
    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        /*// Add a marker in Sydney and move the camera
        LatLng currentLocation = new LatLng(mLastLocation.getLatitude(),mLastLocation.getLongitude());
        mMap.addMarker(new MarkerOptions().position(currentLocation).title("My Current Location"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(currentLocation));*/
    }
    public void toBarkerHonda(View v){
        MarkerOptions moBarker = new MarkerOptions();
        //String Location = "1602 General Electric Rd, Bloomington, IL 61704";
        //Geocoder geocoder = new Geocoder(this);
        LatLng latlngBarker = new LatLng(40.5033678,-88.9542291);
        moBarker.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));
        moBarker.title("Barker Honda");
        mMap.addMarker(moBarker.position(latlngBarker));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(latlngBarker));
        mMap.moveCamera(CameraUpdateFactory.zoomTo(16));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(latlngBarker));

    }
    public void toSearsAutoCenter(View v){
        MarkerOptions moSears = new MarkerOptions();
        //String Location = "1631 E Empire St, Bloomington, IL 61701";
        //Geocoder geocoder = new Geocoder(this);
        //List <Address> addressList = null;

        LatLng latlngSears = new LatLng(40.4860597,-88.9545786);
        moSears.title("Sears Service Center");
        moSears.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));
        mMap.addMarker(moSears.position(latlngSears));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(latlngSears));
        mMap.moveCamera(CameraUpdateFactory.zoomTo(16));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(latlngSears));

    }

    @Override
    public void onConnected(Bundle bundle) {

        mLastLocation = LocationServices.FusedLocationApi.getLastLocation(
                mGoogleApiClient);
        if (mLastLocation != null) {

            Log.e("this is the Latitude:", String.valueOf(mLastLocation.getLatitude()));
            Log.e("this is the Longitude:", String.valueOf(mLastLocation.getLongitude()));
            LatLng currentLocation = new LatLng(mLastLocation.getLatitude(),mLastLocation.getLongitude());
            MarkerOptions markerOptions = new MarkerOptions();
            markerOptions.position(currentLocation);
            markerOptions.title("My current Location");
            markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE));
            mMap.addMarker(markerOptions.position(currentLocation));
            mMap.moveCamera(CameraUpdateFactory.newLatLng(currentLocation));
            mMap.moveCamera(CameraUpdateFactory.zoomTo(16));
            mMap.moveCamera(CameraUpdateFactory.newLatLng(currentLocation));
            toBarker.setVisibility(View.VISIBLE);
            toSears.setVisibility(View.VISIBLE);
            //mMap.animateCamera(CameraUpdateFactory.newLatLng(currentLocation));



        }
    }


    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

    }
}
