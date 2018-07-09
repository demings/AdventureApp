package com.example.mindaugas.adventureapp

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.IntentSender
import android.content.pm.PackageManager
import android.location.Criteria
import android.location.Location
import android.location.LocationManager
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.widget.Toast
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.tasks.Task
import android.content.Context.LOCATION_SERVICE
import android.support.v4.content.ContextCompat.getSystemService
import com.facebook.FacebookSdk.getApplicationContext


class LocationMethods{

    private var MY_PERMISSIONS_REQUEST_ACCESS_LOCATION: Int = 0
    private var REQUEST_CHECK_SETTINGS = 0x1
    val fusedLocationClient: FusedLocationProviderClient
    val context: Context
    val map: GoogleMap

    constructor(context: Context, map: GoogleMap) {
        this.map = map
        this.context = context
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)

        createLocationRequest()
    }

    fun getDistanceFromLatLonInMeters(location1: LatLng, location2: LatLng): Double {
        val R = 6371 // Radius of the earth in km
        val dLat = deg2rad(location2.latitude - location1.latitude)  // deg2rad below
        val dLon = deg2rad(location2.longitude - location1.longitude)

        val a = Math.sin(dLat/2) * Math.sin(dLat/2) +
                Math.cos(deg2rad(location1.latitude)) * Math.cos(deg2rad(location2.latitude)) *
                Math.sin(dLon/2) * Math.sin(dLon/2)

        val c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a))
        val d = R * c // Distance in km
        return d * 1000
    }

    fun deg2rad(deg: Double): Double {
        return deg * (Math.PI/180)
    }


    fun requestLocationPermission(){
        var activity: Activity = context as Activity
        ActivityCompat.requestPermissions(activity,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                MY_PERMISSIONS_REQUEST_ACCESS_LOCATION)
    }



    fun centerMapOnMyLocation() {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            requestLocationPermission()
        }else {
            map.isMyLocationEnabled = true
            fusedLocationClient.lastLocation
                    .addOnSuccessListener { location: Location? ->
                        // Got last known location. In some rare situations this can be null.

                        if (location != null) {
                            map.animateCamera(CameraUpdateFactory.newLatLngZoom(LatLng(location.latitude, location.longitude), 13f))

                            val cameraPosition = CameraPosition.Builder()
                                    .target(LatLng(location.latitude, location.longitude))      // Sets the center of the map to location user
                                    .zoom(17f)                   // Sets the zoom
                                    .bearing(90f)                // Sets the orientation of the camera to east
                                    .tilt(40f)                   // Sets the tilt of the camera to 30 degrees
                                    .build()                   // Creates a CameraPosition from the builder
                            map.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition))
                        } else {
                            Toast.makeText(context, "Location is null", Toast.LENGTH_SHORT).show()
                        }
//                        Log.i(TAG, String.format("Current location: lat %s; long %s", location.latitude.toString(), location.longitude.toString()))
                    }
        }
    }

    fun createLocationRequest() {
        val locationRequest = LocationRequest().apply {
            interval = 10000
            fastestInterval = 5000
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        }

        val builder = LocationSettingsRequest.Builder()
                .addLocationRequest(locationRequest)

// ...

        val client: SettingsClient = LocationServices.getSettingsClient(context)
        val task: Task<LocationSettingsResponse> = client.checkLocationSettings(builder.build())


        task.addOnSuccessListener { locationSettingsResponse ->
            // All location settings are satisfied. The client can initialize
            // location requests here.
            // ...
            centerMapOnMyLocation()
        }

        task.addOnFailureListener { exception ->
            if (exception is ResolvableApiException){
                // Location settings are not satisfied, but this can be fixed
                // by showing the user a dialog.
                try {
                    // Show the dialog by calling startResolutionForResult(),
                    // and check the result in onActivityResult().
                    exception.startResolutionForResult(context as Activity,
                            REQUEST_CHECK_SETTINGS)
                } catch (sendEx: IntentSender.SendIntentException) {
                    // Ignore the error.
                }
            }
        }
    }
}