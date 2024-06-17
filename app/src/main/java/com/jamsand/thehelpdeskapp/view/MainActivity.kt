package com.jamsand.thehelpdeskapp.view

import android.Manifest
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.graphics.Color
import android.location.Location
import android.os.AsyncTask
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Looper
import android.telecom.TelecomManager.EXTRA_LOCATION
import android.util.Log
import android.widget.Button
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
import com.google.android.gms.maps.model.PolylineOptions
import com.google.android.libraries.places.api.Places
import com.google.gson.Gson
import com.jamsand.thehelpdeskapp.BuildConfig
import com.jamsand.thehelpdeskapp.R
import com.jamsand.thehelpdeskapp.model.MapData
import okhttp3.OkHttpClient
import okhttp3.Request

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

    private lateinit var map: GoogleMap

    //for testing purposes get 2 coordinates
    // manually created declared two places

    private var originLatitude: Double = 28.50213593
    private var originLongitude: Double = 77.4054901

    private var destinationLatitude: Double = 28.5151087
    private var destinationLongitude: Double = 77.3932163


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val ai: ApplicationInfo = applicationContext.packageManager
            .getApplicationInfo(applicationContext.packageName, PackageManager.GET_META_DATA)
        val value = ai.metaData["com.google.android.PLACES_KEY"]

        val key = value.toString()
        Toast.makeText(applicationContext, key,Toast.LENGTH_LONG).show()

        // Define a variable to hold the Places API key.
        val apiKey = BuildConfig.PLACES_API_KEY

        // Log an error if apiKey is not set
        if (apiKey.isEmpty() || apiKey == "DEFAULT_API_KEY"){
            Toast.makeText(applicationContext, "NO API KEY",Toast.LENGTH_LONG).show()
            finish()
            return
        }

        // validate key
        if (!Places.isInitialized()) {
            Places.initializeWithNewPlacesApiEnabled(applicationContext, apiKey)
        }
        // Create a new PlacesClient instance
        val placesClient = Places.createClient(this)
        // requestLocationPermission()
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        val gd = findViewById<Button>(R.id.directions)
//        gd.setOnClickListener {
//            mapFragment.getMapAsync {
//                map = it
//                val originLocation = LatLng(originLatitude, originLongitude)
//                map.addMarker(MarkerOptions().position(originLocation))
//                val destinationLocation = LatLng(destinationLatitude, destinationLongitude)
//                map.addMarker(MarkerOptions().position(destinationLocation).title("ODWA"))
//                val urll = getDirectionURL(originLocation, destinationLocation, apiKey)
//                GetDirection(urll).execute()
//                map.animateCamera(CameraUpdateFactory.newLatLngZoom(originLocation, 14F))
//                enableMyLocation()
//            }
//        }



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
                intent.putExtra(EXTRA_LOCATION, currentLocation)

                LocalBroadcastManager.getInstance(applicationContext).sendBroadcast(intent)

                //updates notification content if this service is running as a foreground service



            }
        }

    }

    //check user has permission is granted
    private fun requestLocationPermission() {
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
        if(requestCode == REQUEST_CODE_LOCATION_PERMISSION && grantResults.isNotEmpty() &&
            grantResults[0] == PackageManager.PERMISSION_GRANTED)
            enableMyLocation()
            super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    // retrieve the devices's last known location
    private fun initLocationClient() {
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
    private fun updateMarker(location: LatLng) {
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
        map = googleMap

        // zoom level
        val zoomLevel = 18F
        // Add a marker in any Location and move camera
        val originLocation = LatLng(originLatitude, originLongitude)
        map.clear()
        map.addMarker(MarkerOptions().position(originLocation).title("Sabelo ubaleke eTransnet"))
        map.animateCamera(CameraUpdateFactory.newLatLngZoom(originLocation, zoomLevel))
        enableMyLocation()

    }

    // generate the direction URL
    private fun getDirectionURL(origin: LatLng, dest: LatLng, secret: String): String {
        return "https://maps.googleapis.com/maps/api/directions/json?origin=${origin.latitude},${origin.longitude}" +
                "&destination=${dest.latitude},${dest.longitude}" +
                "&sensor=false" +
                "&mode=driving" +
                "&key=$secret"
    }

    //A polyline is a list of points, where line segments are drawn between consecutive points
    // Decode Polyline
    fun decodePolyline(encoded: String): List<LatLng> {
        val poly = ArrayList<LatLng>()
        var index = 0
        val len = encoded.length
        var lat = 0
        var lng = 0

        while (index < len) {
            var b: Int
            var shift = 0
            var result = 0

            do {
                b = encoded[index++].code - 63
                result = result or (b and 0x1f shl shift)
                shift += 5

            } while (b >= 0x20)
            val dlng = if (result and 1 != 0) (result shr 1).inv() else result shr 1
            lng += dlng
            val latLng = LatLng((lat.toDouble() / 1E5), (lng.toDouble() / 1E5))
            poly.add(latLng)
        }
        return poly
    }

    // create inner class to pass the URL string generated
    @Suppress("StaticFieldLeak")
    private inner class GetDirection(val url : String) : AsyncTask<Void, Void, List<List<LatLng>>>(){
        override fun doInBackground(vararg params: Void?): List<List<LatLng>> {

            val client = OkHttpClient()
            val request = Request.Builder().url(url).build()
            val response = client.newCall(request).execute()
            val data = response.body!!.string()

            val result =  ArrayList<List<LatLng>>()
            try{
                val respObj = Gson().fromJson(data,MapData::class.java)
                val path =  ArrayList<LatLng>()
                for (i in 0 until respObj.routes[0].legs[0].steps.size){
                    path.addAll(decodePolyline(respObj.routes[0].legs[0].steps[i].polyline.points))
                }
                result.add(path)
            }catch (e:Exception){
                e.printStackTrace()
            }
            return result
        }

        @Deprecated("Deprecated in Java")
        override fun onPostExecute(result: List<List<LatLng>>) {
            val lineoption = PolylineOptions()
            for (i in result.indices) {
                lineoption.addAll(result[i])
                lineoption.width(10f)
                lineoption.color(Color.GREEN)
                lineoption.geodesic(true)
            }
            map.addPolyline(lineoption)
            Log.i("Checking Route on Post ", result.toString())
        }

    }

    //check is permissions are granted
    private fun isPermissionGranted(): Boolean {
        return ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
    }

    //enable location tracking
    private fun enableMyLocation() {
        if (isPermissionGranted()) {
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
            map.isMyLocationEnabled = true
        }
        else {
            ActivityCompat.requestPermissions(
                this,
                arrayOf<String>(Manifest.permission.ACCESS_FINE_LOCATION),
                REQUEST_CODE_LOCATION_PERMISSION
            )
        }
    }



}