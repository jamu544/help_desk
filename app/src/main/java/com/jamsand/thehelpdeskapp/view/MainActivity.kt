package com.jamsand.thehelpdeskapp.view

import android.Manifest
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.location.Location
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Looper
import android.telecom.TelecomManager.EXTRA_LOCATION
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.jamsand.thehelpdeskapp.R

class MainActivity : AppCompatActivity(), OnMapReadyCallback {

    private val REQUEST_CODE_LOCATION_PERMISSION = 1
    //-main class for receiving location updates
    private lateinit var  fusedLocationClient: FusedLocationProviderClient

    //requirements for location updates i.e how often you
    // should receive updates, the priority, etc.
    private lateinit var locationRequest: LocationRequest

    // called when FusedLocationProviderClient has a new Location.
    private lateinit var locationCallback: LocationCallback

    //used only for local storage of the last known location. Usually, this would be saved to your
    // database, but because this is a simplified sample without a full database, we only need the
    // last location to create a Notification if the user navigates away from the app.
    private var currentLocation: Location? = null

    // private lateinit var mapView: MapView
    private lateinit var googleMap: GoogleMap

    private lateinit var mMap: GoogleMap

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val ai: ApplicationInfo = applicationContext.packageManager
            .getApplicationInfo(applicationContext.packageName, PackageManager.GET_META_DATA)
        val value = ai.metaData["com.google.android.PLACES_KEY"]

        val key = value.toString()
        Toast.makeText(applicationContext, key,Toast.LENGTH_LONG).show()

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        locationRequest = LocationRequest.create().apply {
            interval = 1000

            //sets the fastest rate for active location updates
            fastestInterval = 500

            // sets the maximum time when batched location updates are delivered
            maxWaitTime  = java.util.concurrent.TimeUnit.SECONDS.toMillis(2)

            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        }

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                super.onLocationResult(locationResult)

                // save new location to a database. for this(save to local database), as we need it
                // again if a Notification is created(when the user navigates away from the app).
                currentLocation = locationResult.lastLocation

                //notify activity that a new location was added. Again, if this was a
                // production app, the Activity would be listening for changes to a database
                // with new locations,

                val intent = Intent(Intent.ACTION_USER_FOREGROUND)
                    intent.putExtra(EXTRA_LOCATION,currentLocation)

                LocalBroadcastManager.getInstance(applicationContext).sendBroadcast(intent)

                //updates notification content if this service is running as a foreground service



            }
        }
       // requestLocationPermission()
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)



//        initLocationClient()
//        mapView = findViewById(R.id.mapView)
//        mapView.onCreate(savedInstanceState)
//        mapView.getMapAsync{ google ->
        // Map is ready, do something with the map
//        }

    }

    //check user has permission is granted
    private fun requestLocationPermission(){
        if (ContextCompat.checkSelfPermission(
                this,
                android.Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION),
                REQUEST_CODE_LOCATION_PERMISSION
            )
        }
    }

    // request permission if not granted
//    override fun onRequestPermissionsResult(
//        requestCode: Int,
//        permissions: Array<out String>,
//        grantResults: IntArray
//    ) {
//        if(requestCode == REQUEST_CODE_LOCATION_PERMISSION && grantResults.isNotEmpty() &&
//            grantResults[0] == PackageManager.PERMISSION_GRANTED)
//            super.onRequestPermissionsResult(requestCode, permissions, grantResults)
//    }

    // retrieve the devices's last known location
    private fun initLocationClient(){
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
    }

//    private val locationRequest = LocationRequest.create().apply {
//        interval = 1000 // update interval in milliseconds
//        fastestInterval = 500 // fastest update interval in milliseconds
//        priority  = LocationRequest.PRIORITY_HIGH_ACCURACY // location accuracy is priority
//
//    }

    private fun startLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return
        }
        // start recieving location updated to save
        fusedLocationClient.requestLocationUpdates(
            locationRequest, //update interval
            locationCallback,// new location update when available
            Looper.getMainLooper()
        )
    }
//    private val locationCallback = object : LocationCallback() {
//        override fun onLocationResult(locationResult: LocationResult) {
//            locationResult?.lastLocation?.let { location ->
//                //location updates recieved, do something with the location
//            }
//        }
//    }

//    override fun onStart() {
//        super.onStart()
//        mapView.onStart()
//    }
//
//    override fun onResume() {
//        super.onResume()
//        mapView.onResume()
//    }
//    override fun onPause() {
//        super.onPause()
//        mapView.onPause()
//    }
//
//    override fun onStop() {
//        super.onStop()
//        mapView.onStop()
//    }
//
//    override fun onDestroy() {
//        super.onDestroy()
//        mapView.onDestroy()
//    }
//
//    override fun onLowMemory() {
//        super.onLowMemory()
//        mapView.onLowMemory()
//    }

    //display location on the map,add marker at the current location
    private fun updateMarker(location: LatLng){
        googleMap.clear()

        var khayelitsha = LatLng(-34.049461, 18.648170)
        val markerOptions = MarkerOptions()
            .position(khayelitsha)
            .title("Current Location")
            .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED))
        googleMap.addMarker(markerOptions)

    }

//    private val locationCallback = object : LocationCallback() {
//        override fun onLocationResult(locationResult: LocationResult) {
//            locationResult.lastLocation?.let { location ->
//
//                val latLng = LatLng(location.latitude, location.longitude)
//                updateMarker(latLng)
//            }
//        }
//    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        // Add a marker in any Location and move camera
        val locationCoordinates = LatLng(-34.049461,18.648170)
        val marker = MarkerOptions().position(locationCoordinates).title("Sabelo ubaleke eTransnet")
        // zoom level
        var zoomLevel = 15f


        mMap.addMarker(marker)
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(locationCoordinates, zoomLevel))
        // default way without specifying zoom level
        //  mMap.moveCamera(CameraUpdateFactory.newLatLng(myLocation))

        // testing gitignore

    }

}