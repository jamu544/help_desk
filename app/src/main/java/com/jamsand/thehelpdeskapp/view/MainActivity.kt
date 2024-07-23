package com.jamsand.thehelpdeskapp.view

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.IntentSender
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.google.android.gms.common.GooglePlayServicesNotAvailableException
import com.google.android.gms.common.GooglePlayServicesRepairableException
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationSettingsRequest
import com.google.android.gms.location.places.ui.PlacePicker
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.libraries.places.api.Places
import com.jamsand.thehelpdeskapp.BuildConfig
import com.jamsand.thehelpdeskapp.R
import com.jamsand.thehelpdeskapp.utils.AnimationUtils
import com.jamsand.thehelpdeskapp.utils.MapUtils
import java.io.IOException


class MainActivity : AppCompatActivity(), OnMapReadyCallback,GoogleMap.OnMarkerClickListener {
    private val mPERMISSION_ID = 42
    private val REQUEST_CODE_LOCATION_PERMISSION = 1

    //-main class for receiving location updates
    private lateinit var mFusedLocationClient: FusedLocationProviderClient

    //requirements for location updates i.e how often you
    // should receive updates, the priority, etc.
 //   private lateinit var locationRequest: LocationRequest

    // called when FusedLocationProviderClient has a new Location.
  //  private lateinit var mLocationCallback: LocationCallback

    private var locationUpdateState = false

    //used only for local storage of the last known location. Usually, this would be saved to your
    // database, but because this is a simplified sample without a full database, we only need the
    // last location to create a Notification if the user navigates away from the app.
    private var currentLocation: Location? = null

    // private lateinit var mapView: MapView
 //   private lateinit var googleMap: GoogleMap

    private lateinit var map: GoogleMap

    //for testing purposes get 2 coordinates
    // manually created declared two places

    private var originLatitude: Double = 28.5021359
    private var originLongitude: Double = 77.4054901

    private var destinationLatitude: Double = 28.5151087
    private var destinationLongitude: Double = 77.3932163
    private lateinit var lastLocation: Location


    //new code
    private lateinit var defaultLocation: LatLng

    private lateinit var myHandler: Handler
    private lateinit var myRunnable: Runnable
    private lateinit var movingMarker: Marker

    private lateinit var  previousLL: LatLng
    private lateinit var currentLL: LatLng

    val apiKey = BuildConfig.PLACES_API_KEY
    var currentLocationIndia: LatLng = LatLng(20.5, 78.9)
    @SuppressLint("SuspiciousIndentation")
    @RequiresApi(Build.VERSION_CODES.P)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val ai: ApplicationInfo = applicationContext.packageManager
            .getApplicationInfo(applicationContext.packageName, PackageManager.GET_META_DATA)
        val value = ai.metaData["com.google.android.PLACES_KEY"]

        val key = value.toString()
        Toast.makeText(applicationContext, key, Toast.LENGTH_LONG).show()

        // Define a variable to hold the Places API key.
     //   val apiKey = BuildConfig.PLACES_API_KEY

        // Log an error if apiKey is not set
        if (apiKey.isEmpty() || apiKey == "DEFAULT_API_KEY") {
            Toast.makeText(applicationContext, "NO API KEY", Toast.LENGTH_LONG).show()
            finish()
            return
        }

        // validate key
        if (!Places.isInitialized()) {
            Places.initializeWithNewPlacesApiEnabled(applicationContext, apiKey)
        }
        // Create a new PlacesClient instance
        val placesClient = Places.createClient(this)

        //initailizing map
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        // initializing fused location client
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        val buton = findViewById<Button>(R.id.fab)
            buton.setOnClickListener {
                getLastLocation()
            }

//        locationCallback = object : LocationCallback() {
//            override fun onLocationResult(p0: LocationResult) {
//                super.onLocationResult(p0)
//
//                lastLocation = p0.lastLocation!!
//                placeMarkerOnMap(LatLng(lastLocation.latitude, lastLocation.longitude))
//            }
//        }
//        createLocationRequest()
//        val fab = findViewById<FloatingActionButton>(R.id.fab)
//        fab.setOnClickListener {
//            loadPlacePicker()
//            Log.d("FLOAT", "FLOATING")
//        }

    }
    // get current location
    @RequiresApi(Build.VERSION_CODES.P)
    @SuppressLint("MissingPermission")
    private fun getLastLocation(){
        if (checkPermissions()) {
            if (isLocationEnabled()) {
                mFusedLocationClient.lastLocation.addOnCompleteListener(this) { task ->
                    val location: Location? = task.result
                    if (location == null) {
                        requestNewLocationData()
                    } else {
                        currentLocationIndia = LatLng(location.latitude, location.longitude)
                        map.clear()
                        map.addMarker(
                            MarkerOptions().position(currentLocationIndia)
                                .title("Odwa")
                                .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_car)) //set icon size
                        )
                        map.animateCamera(
                            CameraUpdateFactory.newLatLngZoom(
                                currentLocationIndia,
                                18F
                            )
                        )

                    }

                }
            } else {
                Toast.makeText(this, "Turn on Location", Toast.LENGTH_LONG).show()
                val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                startActivity(intent)
            }
        } else {
            requestPermissions()
        }
    }
    //get current location,if shifted
    // from previous location
    @SuppressLint("MissingPermission")
    private fun requestNewLocationData(){
        val mLocationRequest = LocationRequest()
        mLocationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        mLocationRequest.interval = 0
        mLocationRequest.fastestInterval = 0
        mLocationRequest.numUpdates = 1

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        mFusedLocationClient.requestLocationUpdates(
            mLocationRequest,mLocationCallback,
            Looper.myLooper()
        )
    }
    // if current location could not be located, use last location
    private val mLocationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult) {
            val mLastLocation: Location? = locationResult.lastLocation
            if (mLastLocation != null) {
                currentLocationIndia = LatLng(mLastLocation.latitude,mLastLocation.longitude)
            }
        }
    }
    // check if GPS is on
    @RequiresApi(Build.VERSION_CODES.P)
    private fun isLocationEnabled(): Boolean{
        val locationManager: LocationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(
            LocationManager.NETWORK_PROVIDER
        )
    }
    //check if location permission are granted to the application
    private fun checkPermissions(): Boolean {
        if (ActivityCompat.checkSelfPermission(this,Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this,Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
        ) {
            return true
        }
        return false
    }

    // request permissions if not granted before
    private fun requestPermissions() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION),
                    mPERMISSION_ID
                    )
    }
    // what must happen when permission is granted
    @RequiresApi(Build.VERSION_CODES.P)
    override fun onRequestPermissionsResult(
        requestCode: Int,permissions: Array<out String>,grantResults: IntArray) {
        if (requestCode == mPERMISSION_ID) {
            if ((grantResults.isEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED))
                getLastLocation()
        }

        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }
    // called when the map is ready for use
    private fun onMapReady2(googleMap: GoogleMap) {
        map = googleMap
        // enabled zoom controls on the map
        map.uiSettings.isZoomControlsEnabled = true


//        // zoom level

//        //  declare bounds object to fit whole route in screen
//        val LatLongB = LatLngBounds.builder()
//        // Add a marker in any Location and move camera
//        var khayelitsha = LatLng(-34.049461, 18.648170)
//        // val opera = LatLng(-33.9320447,151.1597271)
//        map.addMarker(MarkerOptions().position(khayelitsha).title("MARKER IN KHAYELITSHA"))
//        map.moveCamera(CameraUpdateFactory.newLatLngZoom(khayelitsha, zoomLevel))


        setUpMap()
        createLocationRequest()

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
    //restart location updates
//    override fun onResume() {
//        super.onResume()
//        //     mapView.onResume()
//        if (!locationUpdateState) {
//            startLocationUpdates()
//        }
//    }

    //stop location updates
    override fun onPause() {
        super.onPause()
        //    mapView.onPause()
        mFusedLocationClient.removeLocationUpdates(mLocationCallback)
    }

    //display location on the map,add marker at the current location
//    private fun updateMarker(location: LatLng) {
//        googleMap.clear()
//
//        var khayelitsha = LatLng(-34.049461, 18.648170)
//        val markerOptions = MarkerOptions()
//            .position(khayelitsha)
//            .title("Current Location")
//            .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED))
//        googleMap.addMarker(markerOptions)
//
//    }

//    private val locationCallback = object : LocationCallback() {
//        override fun onLocationResult(locationResult: LocationResult) {
//            locationResult.lastLocation?.let { location ->
//
//                val latLng = LatLng(location.latitude, location.longitude)
//                updateMarker(latLng)
//            }
//        }
//    }

    // called when a marker is clicked or tapped.
    override fun onMarkerClick(p0: Marker) = false

    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1
        private const val REQUEST_CHECK_SETTINGS = 2
        private const val PLACE_PICKER_REQUEST = 3

    }

    //checks if the app has granted access permission.
    //if it hasn't, then request it from the user
    // later to be renamed to  requestLocationPermission()
    private fun setUpMap() {
        if (ActivityCompat.checkSelfPermission(
                this,
                android.Manifest.permission.ACCESS_FINE_LOCATION
            ) !=
            PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION),
                LOCATION_PERMISSION_REQUEST_CODE
            )
        }
        //zoom level
        val zoomLevel = 15.5f
        // enables user location with blue dot
        map.isMyLocationEnabled = true
        //set map type
        map.mapType = GoogleMap.MAP_TYPE_TERRAIN
        // gives you the most recent location currently available
        mFusedLocationClient.lastLocation.addOnSuccessListener(this) { location ->
            // got last known location. In some rare situations this can be null
            //3
            if (location != null) {
                lastLocation = location
                val currentLatLng = LatLng(location.latitude, location.longitude)
                placeMarkerOnMap(currentLatLng)
                map.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, zoomLevel))
            }

        }
    }

    //set the user's current locationas the position for the marker
    private fun placeMarkerOnMap(location: LatLng) {
        val markerOptions = MarkerOptions().position(location)

        val titleStr = getAddress(location)
        markerOptions.title(titleStr)

        map.addMarker(markerOptions)

    }

    // show address of that location when user clicks on the marker
    private fun getAddress(latLng: LatLng): String {
        //geocoder to turn object to turn lat/lng into address and vice versa
        val geocoder = Geocoder(this)
        val addresses: List<Address>?
        val address: Address?
        var addressText = ""

        try {
            //  get address from the location
            addresses = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1)

            //
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


//    private fun startLocationUpdates() {
//        // if permission has not been granted, request it now and return
//        if (ActivityCompat.checkSelfPermission(
//                this,
//                android.Manifest.permission.ACCESS_FINE_LOCATION
//            ) !=
//            PackageManager.PERMISSION_GRANTED
//        ) {
//            ActivityCompat.requestPermissions(
//                this,
//                arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION),
//                LOCATION_PERMISSION_REQUEST_CODE
//            )
//            return
//        }
//        // if theres permission, request for location updates
//        mFusedLocationClient.requestLocationUpdates(
//            locationRequest,
//            mLocationCallback, null
//        )
//    }

    private fun createLocationRequest() {
        // retrieve and handle any changes to be made based on current state of the user location
        val mlocationRequest = LocationRequest()

        mlocationRequest.interval = 10000 //rate which the app will like to receive updates
        //3
        mlocationRequest.fastestInterval =
            5000 //specifies the fastest rate at which the app can handle updates
        mlocationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY

        val builder = LocationSettingsRequest.Builder()
            .addLocationRequest(mlocationRequest)

        // create s settings client and a task to check location settings
        val client = LocationServices.getSettingsClient(this)
        val task = client.checkLocationSettings(builder.build())

        // success task means all is well and you can go ahead and initiate a location request
//        task.addOnSuccessListener {
//            locationUpdateState = true
//            startLocationUpdates()
//        }

        task.addOnSuccessListener { e ->
            // on failure means location could be turned off
            if (e is ResolvableApiException) {
                //Location settings are not satisfied, but this can be fixed by showing the user dialog
                try {
                    //show the dialog by calling startResolutionForResult(),
                    //and check the result in onActivityResult()
                    e.startResolutionForResult(
                        this@MainActivity,
                        REQUEST_CHECK_SETTINGS
                    )
                } catch (sendEx: IntentSender.SendIntentException) {
                    //ignore Error
                }
            }
        }

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CHECK_SETTINGS) {
            if (resultCode == Activity.RESULT_OK) {
                val place = PlacePicker.getPlace(this, data)
                var addressText = place.name.toString()
                addressText += "\n" + place.address.toString()

                placeMarkerOnMap(place.latLng)
            }
        }
    }

    private fun loadPlacePicker() {
        val builder = PlacePicker.IntentBuilder()

        try {
            startActivityForResult(
                builder.build(this@MainActivity),
                PLACE_PICKER_REQUEST
            )
        } catch (e: GooglePlayServicesRepairableException) {
            e.printStackTrace()
        } catch (e: GooglePlayServicesNotAvailableException) {
            e.printStackTrace()
        }
    }

    // onMapReady for new code
    //override
    @RequiresApi(Build.VERSION_CODES.P)
    override fun onMapReady(googleMap: GoogleMap) {
        this.map = googleMap
//       defaultLocation = LatLng(28.435350000000003, 77.11368)
//        displayDefaultLocation(defaultLocation)
//        displayPath(MapUtils.getLocations())
//        displayMovingCar(MapUtils.getLocations())
//        val originLocation = LatLng(originLatitude, originLongitude)
//        map.addMarker(MarkerOptions().position(originLocation))
//        val destinationLocation = LatLng(destinationLatitude, destinationLongitude)
//        map.addMarker(MarkerOptions().position(destinationLocation))
//        map.animateCamera(CameraUpdateFactory.newLatLngZoom(originLocation, 15F))
                getLastLocation()
    }

    //repositioning the camera to some lat and long
    private fun moveView(ll: LatLng) {
        map.moveCamera(CameraUpdateFactory.newLatLng(ll))
    }

    //animate  the movement of the camera from the current to new position
    private fun animateView(ll: LatLng) {
        val cameraPosition = CameraPosition.Builder().target(ll).zoom(15.5f).build()
        map.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition))
    }

    //display default location
    private fun displayDefaultLocation(ll: LatLng) {
        moveView(ll)
        animateView(ll)
    }

    private fun getCarMarker(ll: LatLng): Marker {
//        val bitmapDescriptor = BitmapDescriptorFactory.
//        fromBitmap(MapUtils.getStartingLocationBitmap())
        return map.addMarker(
            MarkerOptions().position(ll).flat(true).icon(BitmapDescriptorFactory.fromResource(R.mipmap.ic_launcher_round))
        )!!
    }
    private fun getOriginMarker(ll: LatLng): Marker? {
        val bitmapDescriptor =
            BitmapDescriptorFactory.fromBitmap(MapUtils.getStartingLocationBitmap())
        return map.addMarker(
            MarkerOptions().position(ll).flat(true).icon(bitmapDescriptor)
        )
    }

    //to draw path between origin and destination
//    private fun displayPath(latLngList: ArrayList<LatLng>){
//
//        val builder = LatLngBounds.Builder()
//        for (latLng in latLngList) {
//            builder.include(latLng)
//        }
//        val boundBuilds = builder.build()
//        map.animateCamera(CameraUpdateFactory.newLatLngBounds(boundBuilds, 2))
//
//        val polyOptions = PolylineOptions()
//        polyOptions.color(Color.GRAY)
//        polyOptions.width(5f)
//        polyOptions.addAll(latLngList)
//       val greyLine = map.addPolyline(polyOptions)
//
//        val blackPolyOptions = PolylineOptions()
//        blackPolyOptions.color(Color.BLACK)
//        blackPolyOptions.width(5f)
//       val blackLine = map.addPolyline(blackPolyOptions)
//
//       val oMarker = getOriginMarker(latLngList[0])
//        oMarker?.setAnchor(0.5f, 0.5f)
//       val dMarker = getOriginMarker(latLngList[latLngList.size - 1])
//        dMarker?.setAnchor(0.5f, 0.5f)
//
//        val polyAnimator = polyAnimator()
//        polyAnimator.addUpdateListener { valueAnimator ->
//            val precent = (valueAnimator.animatedValue as Int)
//            val indexNumber = (greyLine?.points!!.size) * (precent / 100.0f).toInt()
//            blackLine?.points = greyLine?.points!!.subList(0, indexNumber)
//        }
//        polyAnimator.start()
//    }

    //check if car is moving or not...call to move
    private fun updateCarLoc(ll: LatLng) {
        if (movingMarker == null) {
            movingMarker = getCarMarker(ll)
        }
        if (previousLL == null) {
            currentLL = ll
            previousLL = currentLL
            movingMarker?.position = currentLL
            movingMarker?.setAnchor(0.5f, 0.5f)
            animateView(currentLL!!)
        } else {
            previousLL = currentLL
            currentLL = ll
            val valueAnimator = AnimationUtils.carAnimator()
            valueAnimator.addUpdateListener { va ->
                if (currentLL != null && previousLL != null) {
                    val multiplier = va.animatedFraction
                    val nxtLoc = LatLng(
                        multiplier * currentLL!!.latitude + (1 - multiplier) * previousLL!!.latitude,
                        multiplier * currentLL!!.longitude + (1 - multiplier) * previousLL!!.longitude
                    )
                    movingMarker?.position = nxtLoc
                    val rotation = MapUtils.getCarRotation(previousLL!!, nxtLoc)
                    if (!rotation.isNaN()) {
                        movingMarker?.rotation = rotation
                    }
                    movingMarker?.setAnchor(0.5f, 0.5f)
                    animateView(nxtLoc)
                }
            }
            valueAnimator.start()
        }
    }


    private fun displayMovingCar(cabLatLngList: ArrayList<LatLng>){
        myHandler = Handler()
        var index = 0
        myRunnable = Runnable {
            run {
                if (index < 10) {
                    updateCarLoc(cabLatLngList[index])
                    myHandler.postDelayed(myRunnable, 3000)
                    ++index
                } else {
                    myHandler.removeCallbacks(myRunnable)
                    Toast.makeText(this@MainActivity,"Trip Ends", Toast.LENGTH_LONG).show()
                }

            }
        }
    }

    private fun searchLocation(view: View){
        val locationSearch: EditText = findViewById(R.id.searchEditText)
        var location: String
        location = locationSearch.text.toString()
        var addressList: List<Address>

    }

}
