package com.example.mindaugas.adventureapp

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.PendingIntent
import android.content.ContentValues
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.Toast
import com.firebase.ui.auth.AuthUI
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingClient
import com.google.android.gms.location.GeofencingRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.firebase.auth.FirebaseAuth
import java.util.*


class MapsActivity : AppCompatActivity(), OnMapReadyCallback{

    private lateinit var mMap: GoogleMap

    private val TAG = MapsActivity::class.java.simpleName
    private val RC_SIGN_IN = 123

    private val MY_PERMISSIONS_REQUEST_ACCESS_LOCATION: Int = 0

    // geofencing
    lateinit var geofencingClient: GeofencingClient
    private var geofenceList : MutableList<Geofence> = mutableListOf()
    private val geofencePendingIntent: PendingIntent by lazy {
        val intent = Intent(this, GeofenceTransitionsIntentService::class.java)
        // We use FLAG_UPDATE_CURRENT so that we get the same pending intent back when calling
        // addGeofences() and removeGeofences().
        PendingIntent.getService(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)
    }

    lateinit var locationMethods: LocationMethods

    var firebase = Firebase()
    var mFirebaseAuth =  FirebaseAuth.getInstance()
    lateinit var mAuthStateListener: FirebaseAuth.AuthStateListener

    var quests = mutableMapOf<String, Quest>()

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
                .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        geofencingClient = LocationServices.getGeofencingClient(this)


        mAuthStateListener = FirebaseAuth.AuthStateListener(){
            var user = it.currentUser
            if(user != null){
                //user is signed in
//                Toast.makeText(this, "Welcome to adventure app", Toast.LENGTH_SHORT).show()
            }else{
                //user is signed out
                startActivityForResult(
                    AuthUI.getInstance()
                        .createSignInIntentBuilder().setIsSmartLockEnabled(!BuildConfig.DEBUG)
                        .setAvailableProviders(Arrays.asList(
                                AuthUI.IdpConfig.FacebookBuilder().build()))
                        .build(),
                RC_SIGN_IN)
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode == RC_SIGN_IN){
            if(resultCode == Activity.RESULT_OK){
                Toast.makeText(this, "Signed in!", Toast.LENGTH_SHORT).show()
            }else if(resultCode == Activity.RESULT_CANCELED){
                Toast.makeText(this, "Sign in canceled", Toast.LENGTH_SHORT).show()
                finish()
            }
        }
    }

    override fun onPause() {
        super.onPause()
        mFirebaseAuth.removeAuthStateListener(mAuthStateListener)
    }

    override fun onResume() {
        super.onResume()
        mFirebaseAuth.addAuthStateListener(mAuthStateListener)
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
        locationMethods = LocationMethods(this, mMap)
        locationMethods.centerMapOnMyLocation()

        firebase.firestore.collection("quests")
                .get()
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        for (document in task.result) {
                            var quest: Quest = document.toObject(Quest::class.java)
                            quests.put(document.id, quest)
                            Log.d(ContentValues.TAG, document.id + " => " + document.data)

                            addMarkerToMapWithQuest(quest)
                            addGeofenceForQuest(document.id, quest)
                            addGeofencesToClient()
                        }
                    } else {
                        Log.w(ContentValues.TAG, "Error getting documents.", task.exception)
                    }
                }

        //  geofencePendingIntent.send()

        mMap.setOnInfoWindowClickListener {

            var currentLocation = locationMethods.getLastKnownLocation()

            if (!(it.tag as Quest).isAnswered) {
                if (currentLocation != null) {
                    if (locationMethods.getDistanceFromLatLonInMeters(
                                    LatLng(currentLocation!!.latitude, currentLocation!!.longitude),
                                    it.position) < Constants.GEOFENCE_RADIUS_IN_METERS) {
                        showQuestDialog(it.tag as Quest)
                    } else {
                        Toast.makeText(this, "Too far!", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(this, "Location is null", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this, "Already answered", Toast.LENGTH_SHORT).show()
            }
        }

    }


    fun addMarkerToMapWithQuest(quest: Quest){
        var marker : Marker = mMap.addMarker(MarkerOptions()
                .position(com.google.android.gms.maps.model.LatLng(quest.latitude, quest.longitude))
                .title(quest.name)
                .snippet(quest.description))

        marker.tag = quest
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

    private fun getGeofencingRequest(): GeofencingRequest {
        return GeofencingRequest.Builder().apply {
            setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER)
            addGeofences(geofenceList)
        }.build()
    }

    fun addGeofenceForQuest(id: String, quest: Quest){
        geofenceList.add(Geofence.Builder()
                // Set the request ID of the geofence. This is a string to identify this
                // geofence.
                .setRequestId(id)

                // Set the circular region of this geofence.
                .setCircularRegion(
                        quest.latitude,
                        quest.longitude,
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

    @SuppressLint("MissingPermission")
    fun addGeofencesToClient(){
        geofencingClient?.addGeofences(getGeofencingRequest(), geofencePendingIntent)?.run {
            addOnSuccessListener {
                // Geofences added
                // ...

                Log.i(TAG, "Geofence added succesfully")
            }
            addOnFailureListener {
                // Failed to add geofences
                // ...
                Log.i(TAG, "Failed to add geofence")
            }
        }
    }
}
