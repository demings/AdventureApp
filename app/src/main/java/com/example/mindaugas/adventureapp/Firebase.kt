package com.example.mindaugas.adventureapp

import android.content.ContentValues.TAG
import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore


class Firebase {
    var firestore = FirebaseFirestore.getInstance()

    fun addQuest(quest: Quest){
        firestore.collection("quests")
                .add(quest)
                .addOnSuccessListener { documentReference -> Log.d(TAG, "DocumentSnapshot added with ID: " + documentReference.id) }
                .addOnFailureListener { e -> Log.w(TAG, "Error adding document", e) }
    }

}