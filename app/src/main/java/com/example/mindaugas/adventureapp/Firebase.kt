package com.example.mindaugas.adventureapp

import android.content.ContentValues
import android.content.ContentValues.TAG
import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore


class Firebase {
    var firestore = FirebaseFirestore.getInstance()

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
                .document(Constants.userID)
                .set(isAnswered as Map<String, Any>)
                .addOnSuccessListener { documentReference ->
                    Log.i(TAG, "DocumentSnapshot added with ID: " + Constants.userID)
                }
                .addOnFailureListener { e -> Log.w(TAG, "Error adding document", e) }
    }

    fun getIsAnswered(){
        firestore.collection("isAnswered").document(Constants.userID)
                .get()
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        if(task.result.exists()) {
                            MapsActivity.isAnswered = task.result.data!!.toMutableMap() as MutableMap<String, Boolean>
                        }
                    } else {
                        Log.w(ContentValues.TAG, "Error getting documents.", task.exception)
                    }
                }
    }
}