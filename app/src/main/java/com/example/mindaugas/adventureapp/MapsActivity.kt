package com.example.mindaugas.adventureapp

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat

import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.CameraPosition
import android.location.Criteria
import android.location.Location
import android.location.LocationManager
import android.util.Log
import com.google.android.gms.maps.model.MarkerOptions
import android.support.v7.app.AlertDialog
import android.view.View
import android.widget.EditText
import android.widget.Toast
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.Marker
import android.app.PendingIntent;
import android.content.Intent
import com.google.android.gms.location.GeofencingRequest

class MapsActivity : AppCompatActivity(), OnMapReadyCallback{


    private val geofencePendingIntent: PendingIntent by lazy {
        val intent = Intent(this, GeofenceTransitionsJobIntentService::class.java)
        // We use FLAG_UPDATE_CURRENT so that we get the same pending intent back when calling
        // addGeofences() and removeGeofences().
        PendingIntent.getService(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)
    }

    private fun getGeofencingRequest(): GeofencingRequest {
        return GeofencingRequest.Builder().apply {
            setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER)
            addGeofences(geofenceList)
        }.build()
    }

    private lateinit var mMap: GoogleMap
    private val TAG = MapsActivity::class.java.simpleName

    private val MY_PERMISSIONS_REQUEST_ACCESS_LOCATION: Int = 0
    var questDatabase: QuestDatabase = QuestDatabase()

    private lateinit var currentLocation: Location

    lateinit var geofencingClient: GeofencingClient
    lateinit var geofenceList: ArrayList<Geofence>


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
                .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        geofencingClient = LocationServices.getGeofencingClient(this)



    }

    private fun removeGeofences(){
        geofencingClient?.removeGeofences(geofencePendingIntent)?.run {
            addOnSuccessListener {
                // Geofences removed
                // ...
            }
            addOnFailureListener {
                // Failed to remove geofences
                // ...
            }
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
    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        geofenceList = ArrayList()
        removeGeofences()

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            // Permission is not granted
            requestLocationPermission()
        }else{
            centerMapOnMyLocation()
        }

        questDatabase.quests.forEach{

            var marker : Marker = mMap.addMarker(MarkerOptions()
                    .position(it.value.location)
                    .title(it.value.name)
                    .snippet(it.value.description))

            marker.tag = it.value



            geofenceList.add(Geofence.Builder()
                    // Set the request ID of the geofence. This is a string to identify this
                    // geofence.
                    .setRequestId(marker.id)

                    // Set the circular region of this geofence.
                    .setCircularRegion(
                            it.value.location.latitude,
                            it.value.location.longitude,
                            Constants.GEOFENCE_RADIUS_IN_METERS
                    )

                    // Set the expiration duration of the geofence. This geofence gets automatically
                    // removed after this period of time.
                    .setExpirationDuration(Constants.GEOFENCE_EXPIRATION_IN_MILLISECONDS)

                    // Set the transition types of interest. Alerts are only generated for these
                    // transition. We track entry and exit transitions in this sample.
                    .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER or Geofence.GEOFENCE_TRANSITION_EXIT)

                    // Create the geofence.
                    .build())



        }

        mMap.setOnInfoWindowClickListener{
            if(!(it.tag as Quest).isAnswered) {
                //checks if location permission is granted
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                        != PackageManager.PERMISSION_GRANTED) {
                    // Permission is not granted
                    requestLocationPermission()
                } else {
                    if (currentLocation != null) {
                        if (getDistanceFromLatLonInMeters(
                                        LatLng(currentLocation!!.latitude, currentLocation!!.longitude),
                                        it.position) < Constants.GEOFENCE_RADIUS_IN_METERS) {
                            showQuestDialog(it.tag as Quest)
                        } else {
                            Toast.makeText(this, "Too far!", Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        //location is null
                    }
                }
            }else{
                Toast.makeText(this, "Already answered", Toast.LENGTH_SHORT).show()
            }
        }
    }


    fun getDistanceFromLatLonInMeters(location1: LatLng, location2: LatLng): Double {
        var R = 6371 // Radius of the earth in km
        var dLat = deg2rad(location2.latitude - location1.latitude)  // deg2rad below
        var dLon = deg2rad(location2.longitude - location1.longitude)

        var a = Math.sin(dLat/2) * Math.sin(dLat/2) +
                        Math.cos(deg2rad(location1.latitude)) * Math.cos(deg2rad(location2.latitude)) *
                        Math.sin(dLon/2) * Math.sin(dLon/2)

        var c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a))
        var d = R * c // Distance in km
        return d * 1000
    }

    fun deg2rad(deg: Double): Double {
        return deg * (Math.PI/180)
    }

    fun showQuestDialog(quest: Quest) {
        val dialogBuilder = AlertDialog.Builder(this)
        val inflater = this.layoutInflater
        val dialogView = inflater.inflate(R.layout.quest_dialog, null)
        dialogBuilder.setView(dialogView)
        val editText = dialogView.findViewById<View>(R.id.editTextName) as EditText
//        Toast.makeText(this, editTextName.text, Toast.LENGTH_SHORT).show()

        dialogBuilder.setTitle(quest.name)
        dialogBuilder.setMessage(quest.description)
        dialogBuilder.setPositiveButton("Submit") { dialog, whichButton ->
            if(editText.text.toString().equals(quest.answer)){
                Toast.makeText(this, "Answer is correct!", Toast.LENGTH_SHORT).show()
                quest.isAnswered = true
                //TODO: change marker color to green
            }else{
                Toast.makeText(this, "Answer is wrong!", Toast.LENGTH_SHORT).show()
                //TODO: don't reset the dialog
                showQuestDialog(quest)
            }
        }
        dialogBuilder.setNegativeButton("Cancel") { dialog, whichButton ->
            //pass
        }
        val b = dialogBuilder.create()
        b.show()
    }

    @SuppressLint("MissingPermission")
    override fun onRequestPermissionsResult(requestCode: Int,
                                            permissions: Array<String>, grantResults: IntArray) {
        when (requestCode) {
            MY_PERMISSIONS_REQUEST_ACCESS_LOCATION -> {
                // If request is cancelled, the result arrays are empty.
                if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.
                    centerMapOnMyLocation()
                } else {
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                    requestLocationPermission()
                }
                return
            }

        // Add other 'when' lines to check for other
        // permissions this app might request.
            else -> {
                // Ignore all other requests.
            }
        }
    }

    private fun requestLocationPermission(){
        ActivityCompat.requestPermissions(this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                MY_PERMISSIONS_REQUEST_ACCESS_LOCATION)
    }

    @SuppressLint("MissingPermission")
    private fun centerMapOnMyLocation() {

        mMap.isMyLocationEnabled = true
        val locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        val criteria = Criteria()

        val location = locationManager.getLastKnownLocation(locationManager.getBestProvider(criteria, false))
        if (location != null) {
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(LatLng(location.latitude, location.longitude), 13f))

            val cameraPosition = CameraPosition.Builder()
                    .target(LatLng(location.latitude, location.longitude))      // Sets the center of the map to location user
                    .zoom(17f)                   // Sets the zoom
                    .bearing(90f)                // Sets the orientation of the camera to east
                    .tilt(40f)                   // Sets the tilt of the camera to 30 degrees
                    .build()                   // Creates a CameraPosition from the builder
            mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition))
        }

        Log.i(TAG, String.format("Current location: lat %s; long %s", location.latitude.toString(), location.longitude.toString()))
        currentLocation = location
    }
}
