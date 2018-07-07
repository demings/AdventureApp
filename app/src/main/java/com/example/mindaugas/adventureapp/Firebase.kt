package com.example.mindaugas.adventureapp

import android.content.ContentValues.TAG
import android.util.Log
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.FirebaseFirestore


class Firebase {
    var database = FirebaseDatabase.getInstance()
    var questsReference = database.getReference("quests")

    var firestore = FirebaseFirestore.getInstance()

    fun addQuest(quest: Quest){
        firestore.collection("quests")
                .add(quest)
                .addOnSuccessListener { documentReference -> Log.d(TAG, "DocumentSnapshot added with ID: " + documentReference.id) }
                .addOnFailureListener { e -> Log.w(TAG, "Error adding document", e) }
    }
    
    fun getQuests(): Map<String, Quest>{
        var quests: MutableMap<String, Quest> = mutableMapOf()

        firestore.collection("quests")
                .get()
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        for (document in task.result) {
                            var quest: Quest = document.toObject(Quest::class.java)
                            quests.put(document.id, quest)
                            Log.d(TAG, document.id + " => " + document.data)
                        }
                    } else {
                        Log.w(TAG, "Error getting documents.", task.exception)
                    }
                }

        return quests
    }

}