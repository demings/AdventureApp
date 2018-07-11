package com.example.mindaugas.adventureapp

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.support.v4.app.ActivityCompat.startActivityForResult
import android.support.v4.app.FragmentActivity
import android.support.v7.app.AppCompatActivity
import android.widget.Toast
import com.firebase.ui.auth.AuthUI
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.places.ui.PlacePicker
import com.google.android.gms.maps.GoogleMap
import com.google.firebase.auth.FirebaseAuth
import java.util.*

class AuthenticationMethods {

    lateinit var activity : Activity
    var firebase = Firebase()
    var mFirebaseAuth =  FirebaseAuth.getInstance()
    var mAuthStateListener: FirebaseAuth.AuthStateListener

    constructor(context: Context) {
        activity = context as FragmentActivity

        mAuthStateListener = FirebaseAuth.AuthStateListener(){
            var user = it.currentUser
            if(user != null){
                //user is signed in
//                Toast.makeText(this, "Welcome to adventure app", Toast.LENGTH_SHORT).show()
            }else{
                //user is signed out
                activity.startActivityForResult(
                        AuthUI.getInstance()
                                .createSignInIntentBuilder().setIsSmartLockEnabled(!BuildConfig.DEBUG)
                                .setAvailableProviders(Arrays.asList(
                                        AuthUI.IdpConfig.FacebookBuilder().build()))
                                .build(),
                        Constants.RC_SIGN_IN)
            }
        }

    }

    fun removeAuthStateListener(){
        mFirebaseAuth.removeAuthStateListener(mAuthStateListener)
    }

    fun addAuthStateListener(){
        mFirebaseAuth.addAuthStateListener(mAuthStateListener)
    }
}