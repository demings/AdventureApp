package com.example.mindaugas.adventureapp

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
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
import android.location.LocationManager
import android.provider.AlarmClock.EXTRA_MESSAGE
import android.util.Log
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions


class MapsActivity : AppCompatActivity(), OnMapReadyCallback{


    private lateinit var mMap: GoogleMap
    private val TAG = MapsActivity::class.java!!.simpleName

    private val MY_PERMISSIONS_REQUEST_ACCESS_LOCATION: Int = 0
    var questDatabase: QuestDatabase = QuestDatabase()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
                .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
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

        // Add a marker in Sydney and move the camera
//        val sydney = LatLng(-34.0, 151.0)
//        mMap.addMarker(MarkerOptions().position(sydney).title("Marker in Sydney"))
//        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney))

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_CALENDAR)
                != PackageManager.PERMISSION_GRANTED) {
            // Permission is not granted
            requestLocationPermission()
        }else{
            centerMapOnMyLocation()
        }

        questDatabase.quests.forEach{
            it.value.markerID = mMap.addMarker(
                    MarkerOptions().position(it.value.location)
                            .title(it.value.name)
                            .snippet(it.value.description))
                    .id
        }

//        mMap.setOnMarkerClickListener(this)
    }

//    override fun onMarkerClick(p0: Marker?): Boolean {
//        //make an intent to start a new activity
//        var quest: Quest? = null
//
//        questDatabase.quests.forEach{
//            if(it.value.markerID.equals(p0!!.id)){
//                quest = it.value
//            }
//        }
//
//        val intent = Intent(this, QuestActivity::class.java).putExtra("quest", quest)
//
//        startActivity(intent)
//        return true
//    }

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

//        Toast.makeText(this,
//                String.format("lat- %s; long %s", location.latitude.toString(), location.longitude.toString()),
//                Toast.LENGTH_LONG).show()
        Log.i(TAG, String.format("Current location: lat %s; long %s", location.latitude.toString(), location.longitude.toString()))
    }
}
