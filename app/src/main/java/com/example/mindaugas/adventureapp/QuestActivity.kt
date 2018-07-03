package com.example.mindaugas.adventureapp

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.widget.TextView

class QuestActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_quest)

        val quest: Quest = intent.getParcelableExtra("quest") as Quest

        val textview : TextView = findViewById(R.id.questTextView)
        textview.text = quest.description
    }
}
