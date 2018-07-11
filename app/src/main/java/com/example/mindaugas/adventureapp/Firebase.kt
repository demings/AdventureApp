package com.example.mindaugas.adventureapp

import android.app.Activity
import android.content.ContentValues.TAG
import android.content.Context
import android.support.v4.app.FragmentActivity
import android.util.Log
import com.firebase.ui.auth.AuthUI
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.util.*


class Firebase {
    var firestore = FirebaseFirestore.getInstance()
    var activity : Activity


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



    fun addQuest(quest: Quest){
        firestore.collection("quests")
                .add(quest)
                .addOnSuccessListener { documentReference ->
                    Log.i(TAG, "DocumentSnapshot added with ID: " + documentReference.id)
                }
                .addOnFailureListener { e -> Log.w(TAG, "Error adding document", e) }
    }

    fun removeAuthStateListener(){
        mFirebaseAuth.removeAuthStateListener(mAuthStateListener)
    }

    fun addAuthStateListener(){
        mFirebaseAuth.addAuthStateListener(mAuthStateListener)
    }
}