package com.example.mindaugas.adventureapp

import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase



class Firebase {
    var database = FirebaseDatabase.getInstance()
    var questsReference = database.getReference("quests")

    fun pushAQuest(quest: Quest){
        questsReference.push().setValue(quest)
    }
}