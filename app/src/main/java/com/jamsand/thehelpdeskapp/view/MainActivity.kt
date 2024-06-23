package com.jamsand.thehelpdeskapp.view

import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.graphics.Color
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.os.AsyncTask
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.PolylineOptions
import com.google.android.libraries.places.api.Places
import com.google.gson.Gson
import com.jamsand.thehelpdeskapp.BuildConfig
import com.jamsand.thehelpdeskapp.R
import com.jamsand.thehelpdeskapp.model.MapData
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.IOException


class MainActivity : AppCompatActivity(), OnMapReadyCallback,GoogleMap.OnMarkerClickListener {

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

    private var originLatitude: Double = 28.5021359
    private var originLongitude: Double = 77.4054901

    private var destinationLatitude: Double = 28.5151087
    private var destinationLongitude: Double = 77.3932163
    private lateinit var lastLocation: Location


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

//        val gd = findViewById<Button>(R.id.directions)
//        gd.setOnClickListener {
//            mapFragment.getMapAsync {
//                map = it
//                val originLocation = LatLng(originLatitude, originLongitude)
//                map.addMarker(MarkerOptions().position(originLocation).title("SABELO"))
//                val destinationLocation = LatLng(destinationLatitude, destinationLongitude)
//                map.addMarker(MarkerOptions().position(destinationLocation).title("ODWA"))
//                val urll = getDirectionURL(originLocation, destinationLocation, apiKey)
//                GetDirection(urll).execute()
//                map.animateCamera(CameraUpdateFactory.newLatLngZoom(originLocation, 14F))
//                enableMyLocation()
//                val line = map.addPolyline(
//                    PolylineOptions()
//                        .add(LatLng(originLatitude, originLongitude), LatLng(destinationLatitude, destinationLongitude))
//                        .width(5f)
//                        .color(Color.RED)
//                )

//            }
//        }




        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

    }

    // called when the map is ready for use
    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap
        // enabled zoom controls on the map
        map.uiSettings.isZoomControlsEnabled = true
        map.setOnMarkerClickListener(this)

//        // zoom level

//        //  declare bounds object to fit whole route in screen
//        val LatLongB = LatLngBounds.builder()
//        // Add a marker in any Location and move camera
//        var khayelitsha = LatLng(-34.049461, 18.648170)
//        // val opera = LatLng(-33.9320447,151.1597271)
//        map.addMarker(MarkerOptions().position(khayelitsha).title("MARKER IN KHAYELITSHA"))
//        map.moveCamera(CameraUpdateFactory.newLatLngZoom(khayelitsha, zoomLevel))


        setUpMap()

    }


    // request permission if not granted
//    override fun onRequestPermissionsResult(
//        requestCode: Int,
//        permissions: Array<out String>,
//        grantResults: IntArray
//    ) {
//        if  (requestCode == REQUEST_CODE_LOCATION_PERMISSION && grantResults.isNotEmpty() &&
//            grantResults[0] == PackageManager.PERMISSION_GRANTED)
//            enableMyLocation()
//            super.onRequestPermissionsResult(requestCode, permissions, grantResults)
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



    // generate the direction URL
    private fun getDirectionURL(origin: LatLng, dest: LatLng, secret: String): String {
        return "https://maps.googleapis.com/maps/api/directions/json?origin=" +
                "${origin.latitude},${origin.longitude}" +
                "&destination=${dest.latitude},${dest.longitude}" +
                "&sensor=false" +
                "&mode=driving" +
                "&key=$secret"
    }
//    private fun getDirectionURL(from: LatLng, to: LatLng) : String {
//        val origin  = "origin=" + from.latitude + "," + from.longitude
//        val dest = "destination=" + to.latitude + "," + to.longitude
//        val sensor = "sensor=false"
//        val params = "$origin&$dest&$sensor"
//        return "https://maps.googleapis.com/maps/api/directions/json?$params"
//    }

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
            val dlat = if (result and 1 != 0) (result shr 1).inv() else result shr 1
            lat += dlat

            val p = LatLng(lat.toDouble() / 1E5,
                lng.toDouble() / 1E5)
            poly.add(p)
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
        // called when a marker is clicked or tapped.
    override fun onMarkerClick(p0: Marker) = false

        companion object {
            private const val LOCATION_PERMISSION_REQUEST_CODE = 1

        }

    //checks if the app has granted access permission.
    //if it hasn't, then request it from the user
    // later to be renamed to  requestLocationPermission()
    private fun setUpMap(){
        if (ActivityCompat.checkSelfPermission(this,
            android.Manifest.permission.ACCESS_FINE_LOCATION) !=
            PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this,
                arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION),
                LOCATION_PERMISSION_REQUEST_CODE
                )
        }
        //zoom level
        val zoomLevel = 12.0f
        // enables user location with blue dot
        map.isMyLocationEnabled = true
        //set map type
        map.mapType = GoogleMap.MAP_TYPE_TERRAIN
        // gives you the most recent location currently available
        fusedLocationClient.lastLocation.addOnSuccessListener(this) { location ->
            // got last known location. In some rare situations this can be null
            //3
            if (location != null){
                lastLocation = location
                val currentLatLng = LatLng(location.latitude, location.longitude)
                placeMarkerOnMap(currentLatLng)
                map.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng,zoomLevel))
            }

        }
    }
    //set the user's current locationas the position for the marker
    private fun placeMarkerOnMap(location: LatLng){
        val markerOptions = MarkerOptions().position(location)
        map.addMarker(markerOptions)

    }
    // show address of that location when user clicks on the marker
    private fun getAddress(latLng: LatLng): String {
        //1
        val geocoder = Geocoder(this)
        val addresses: List<Address>?
        val address: Address?
        var addressText = ""

        try {
        //2
            addresses = geocoder.getFromLocation(latLng.latitude, latLng.longitude,1)

            //3
            if (null != addresses && !addresses.isEmpty()) {
                address = addresses[0]
                for (i in 0 until address.maxAddressLineIndex) {
                    addressText += if (i == 0) address.getAddressLine(i) else "\n" +
                            address.getAddressLine(i)
                }
            }
        } catch (e: IOException) {
            Log.e("MainActivity", e.localizedMessage)
        }
        return addressText
    }
}