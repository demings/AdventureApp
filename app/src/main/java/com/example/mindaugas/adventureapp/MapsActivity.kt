package com.example.mindaugas.adventureapp

import android.Manifest
import android.app.Activity
import android.app.PendingIntent
import android.content.ContentValues
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.location.Location
import android.os.Bundle
import android.provider.MediaStore
import android.support.v4.content.ContextCompat
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.util.Base64
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.Toast
import com.example.mindaugas.adventureapp.Constants.Companion.PLACE_PICKER_REQUEST
import com.example.mindaugas.adventureapp.Constants.Companion.RC_SIGN_IN
import com.example.mindaugas.adventureapp.Constants.Companion.REQUEST_IMAGE_CAPTURE
import com.google.android.gms.location.places.Place
import com.google.android.gms.location.places.ui.PlacePicker
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import kotlinx.android.synthetic.main.activity_maps.*
import kotlinx.android.synthetic.main.add_quest_dialog.*
import java.io.ByteArrayOutputStream
import java.util.*


class MapsActivity : AppCompatActivity(), OnMapReadyCallback{

    private lateinit var mMap: GoogleMap
    private val TAG = MapsActivity::class.java.simpleName


    private val geofencePendingIntent: PendingIntent by lazy {
        val intent = Intent(this, GeofenceTransitionsIntentService::class.java)
        // We use FLAG_UPDATE_CURRENT so that we get the same pending intent back when calling
        // addGeofences() and removeGeofences().
        PendingIntent.getService(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)
    }

    lateinit var locationMethods: LocationMethods
    lateinit var geofenceMethods: GeofenceMethods


    lateinit var firebaseMethods : FirebaseMethods

    lateinit var lastAddQuestDialog: AlertDialog
    lateinit var lastAddQuestImage: Bitmap

    companion object {
        var isAnswered = mutableMapOf<String, Boolean>()
        var quests = mutableMapOf<String, Quest>()
        var currentUser: User = User()
    }



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
                .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        geofenceMethods = GeofenceMethods(this)
        firebaseMethods = FirebaseMethods(this)

        addQuestButton.setOnClickListener{
            var builder = PlacePicker.IntentBuilder()
            startActivityForResult(builder.build(this), PLACE_PICKER_REQUEST)
        }
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode == RC_SIGN_IN){
            if(resultCode == Activity.RESULT_OK){
                Toast.makeText(this, "Signed in!", Toast.LENGTH_SHORT).show()
                getQuestsFromFireStore()
            }else if(resultCode == Activity.RESULT_CANCELED){
                Toast.makeText(this, "Sign in canceled", Toast.LENGTH_SHORT).show()
                finish()
            }
        }else

        if (requestCode == PLACE_PICKER_REQUEST) {
            if (resultCode == RESULT_OK) {
                var place = PlacePicker.getPlace(data, this)
                var toastMsg = String.format("Place: %s", place.name)
                Toast.makeText(this, toastMsg, Toast.LENGTH_LONG).show()
                showAddQuestDialog(place)
            }
        }else

        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            var extras = data!!.extras
            var imageBitmap = extras.get("data")

            lastAddQuestImage = imageBitmap as Bitmap
            lastAddQuestDialog.addQuestPhotoButton.setImageBitmap(lastAddQuestImage)

        }
    }

    override fun onPause() {
        super.onPause()
        firebaseMethods.removeAuthStateListener()
    }

    override fun onResume() {
        super.onResume()
        firebaseMethods.addAuthStateListener()
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

        getQuestsFromFireStore()

        geofencePendingIntent.send()

        mMap.setOnInfoWindowClickListener {

            var currentLocation: Location?

            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED) {
                locationMethods.requestLocationPermission()
            }else {
                locationMethods.fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                    currentLocation = location
                    if(isAnswered[(it.tag as Quest).ID] == null || !isAnswered[(it.tag as Quest).ID]!!){
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
        }
    }

    fun updateScoreView(){
        questScoreTextView.text = String.format("Your score: %d", isAnswered.size)
    }

    fun getQuestsFromFireStore(){
        firebaseMethods.firestore.collection("quests")
                .get()
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        for (document in task.result) {
                            var quest: Quest = document.toObject(Quest::class.java)
                            quests[document.id] = quest
                            Log.d(ContentValues.TAG, document.id + " => " + document.data)

                            addMarkerToMapWithQuest(quest)
                            geofenceMethods.addGeofenceForQuest(document.id, quest)
                            geofenceMethods.addGeofencesToClient(geofencePendingIntent)
                        }
                    } else {
                        Log.w(ContentValues.TAG, "Error getting documents.", task.exception)
                    }
                }
    }

    fun addMarkerToMapWithQuest(quest: Quest){
        var marker : Marker = mMap.addMarker(MarkerOptions()
                .position(com.google.android.gms.maps.model.LatLng(quest.latitude, quest.longitude))
                .title(quest.name)
                .snippet(quest.description)
                .icon(BitmapDescriptorFactory.fromBitmap(decodeBase64ToBitmap(quest.icon)))
        )

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
            if(editText.text.toString() == quest.answer){
                Toast.makeText(this, "Answer is correct!", Toast.LENGTH_SHORT).show()
//                quest.isAnswered = true
                isAnswered[quest.ID] = true
                currentUser.score = isAnswered.size
                firebaseMethods.setIsAnswered(isAnswered)
                firebaseMethods.setUser(currentUser)
                updateScoreView()
                //TODO: change marker color
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

    fun showAddQuestDialog(place: Place) {
        val dialogBuilder = AlertDialog.Builder(this)
        val inflater = this.layoutInflater
        val dialogView = inflater.inflate(R.layout.add_quest_dialog, null)
        dialogBuilder.setView(dialogView)
        val questDescription = dialogView.findViewById<View>(R.id.questDescription) as EditText
        val questName = dialogView.findViewById<View>(R.id.questName) as EditText
        val questAnswer = dialogView.findViewById<View>(R.id.questAnswer) as EditText
//        Toast.makeText(this, editTextName.text, Toast.LENGTH_SHORT).show()

        dialogBuilder.setTitle("Enter quest info")
//        dialogBuilder.setMessage(quest.description)
        dialogBuilder.setPositiveButton("Add") { dialog, whichButton ->
            //TODO: validate



            var quest = Quest(
                    UUID.randomUUID().toString(),
                    questName.text.toString(),
                    questDescription.text.toString(),
                    questAnswer.text.toString(),
                    place.latLng.latitude,
                    place.latLng.longitude,
                    encodeBitmapToBase64(lastAddQuestImage)
            )

            firebaseMethods.addQuest(quest)

            getQuestsFromFireStore()
        }
        dialogBuilder.setNegativeButton("Cancel") { dialog, whichButton ->
            //pass
        }
        lastAddQuestDialog = dialogBuilder.create()
        lastAddQuestDialog.show()


        lastAddQuestDialog.addQuestPhotoButton.setOnClickListener {
            val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            if (takePictureIntent.resolveActivity(packageManager) != null) {
                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE)
            }
        }
    }

    fun encodeBitmapToBase64(bitmap: Bitmap): String{
        var baos = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos)
        return android.util.Base64.encodeToString(baos.toByteArray(), android.util.Base64.DEFAULT)
    }

    fun decodeBase64ToBitmap(encodedImage: String): Bitmap{
        var decodedString = Base64.decode(encodedImage, Base64.DEFAULT)
        return BitmapFactory.decodeByteArray(decodedString, 0, decodedString.size)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        when (requestCode) {
            Constants.MY_PERMISSIONS_REQUEST_ACCESS_LOCATION -> {
                // If request is cancelled, the result arrays are empty.
                if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.
//                    centerMapOnMyLocation()

                    locationMethods.createLocationRequest()
                    locationMethods.centerMapOnMyLocation()
                } else {
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
//                    requestLocationPermission()
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
}
