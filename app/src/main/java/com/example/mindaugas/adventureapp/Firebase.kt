package com.example.mindaugas.adventureapp

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

}