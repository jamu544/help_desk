package com.jamsand.thehelpdeskapp.view

import android.Manifest
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Looper
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.jamsand.thehelpdeskapp.R

class MainActivity : AppCompatActivity() {

    private val REQUEST_CODE_LOCATION_PERMISSION = 1
    private lateinit var  fusedLocationClient: FusedLocationProviderClient
    private lateinit var mapView: MapView
    private lateinit var googleMap: GoogleMap

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        requestLocationPermission()
        initLocationClient()
        mapView = findViewById(R.id.mapView)
        mapView.onCreate(savedInstanceState)
        mapView.getMapAsync{ google ->
            // Map is ready, do something with the map
        }

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
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if (requestCode == REQUEST_CODE_LOCATION_PERMISSION && grantResults.isNotEmpty() &&
            grantResults[0] == PackageManager.PERMISSION_GRANTED)
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    // retrieve the devices's last known location
    private fun initLocationClient(){
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
    }

    private val locationRequest = LocationRequest.create().apply {
        interval = 1000 // update interval in milliseconds
        fastestInterval = 500 // fastest update interval in milliseconds
        priority  = LocationRequest.PRIORITY_HIGH_ACCURACY // location accuracy is priority

    }

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

    override fun onStart() {
        super.onStart()
        mapView.onStart()
    }

    override fun onResume() {
        super.onResume()
        mapView.onResume()
    }
    override fun onPause() {
        super.onPause()
        mapView.onPause()
    }

    override fun onStop() {
        super.onStop()
        mapView.onStop()
    }

    override fun onDestroy() {
        super.onDestroy()
        mapView.onDestroy()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        mapView.onLowMemory()
    }

    //display location on the map,add marker at the current location
    private fun updateMarker( location: LatLng){
        googleMap.clear()

        val markerOptions = MarkerOptions()
            .position(location)
            .title("Current Location")
            .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED))
        googleMap.addMarker(markerOptions)

    }

    private val locationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult?) {
            locationResult?.lastLocation?.let { location ->

                val latLng = LatLng(location.latitude, location.longitude)
                updateMarker(latLng)
            }
        }
    }





}