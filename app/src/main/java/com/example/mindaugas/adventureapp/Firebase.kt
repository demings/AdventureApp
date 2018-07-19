package com.example.mindaugas.adventureapp


import android.content.ContentValues
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
//                Constants.userID = user.uid
                MapsActivity.currentUser.ID = user.uid
                getIsAnswered()
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
                .document(quest.ID)
                .set(quest)
                .addOnSuccessListener { documentReference ->
                    Log.i(TAG, "DocumentSnapshot added with ID: " + quest.ID)
                }
                .addOnFailureListener { e -> Log.w(TAG, "Error adding document", e) }
    }

    fun addCollection(isAnswered: MutableMap<String, Boolean>){
        firestore.collection("isAnswered")
                .document(MapsActivity.currentUser.ID)
                .set(isAnswered as Map<String, Any>)
                .addOnSuccessListener { documentReference ->
                    Log.i(TAG, "DocumentSnapshot added with ID: " + MapsActivity.currentUser.ID)
                }
                .addOnFailureListener { e -> Log.w(TAG, "Error adding document", e) }
    }

    fun getIsAnswered() {
        firestore.collection("isAnswered").document(MapsActivity.currentUser.ID)
                .get()
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        if (task.result.exists()) {
                            MapsActivity.isAnswered = task.result.data!!.toMutableMap() as MutableMap<String, Boolean>
                        }
                    } else {
                        Log.w(ContentValues.TAG, "Error getting documents.", task.exception)
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