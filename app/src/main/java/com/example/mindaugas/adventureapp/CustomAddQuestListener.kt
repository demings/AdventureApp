package com.example.mindaugas.adventureapp

import android.app.Dialog
import android.content.Context
import android.os.Build
import android.support.annotation.RequiresApi
import android.view.View
import android.widget.Toast
import kotlinx.android.synthetic.main.add_quest_dialog.*

class CustomAddQuestListener(var dialog: Dialog, var activity: Context) : View.OnClickListener {

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    override fun onClick(p0: View?) {
        var valid = true
        var validString = "Not valid:"

//        val questDescription = dialogView.findViewById<View>(R.id.questDescription) as EditText
//        val questName = dialogView.findViewById<View>(R.id.questName) as EditText
//        val questAnswer = dialogView.findViewById<View>(R.id.questAnswer) as EditText

        if(dialog.questName.text.isBlank()){
            valid = false
            validString += " name;"
        }

        if(dialog.questDescription.text.isBlank()){
            valid = false
            validString += " description;"
        }


        if(dialog.questAnswer.text.isBlank()){
            valid = false
            validString += " answer;"
        }



        if(!(dialog.addQuestPhotoButton.tag as Boolean)){
            valid = false
            validString += " picture;"
        }


        if(valid){
            dialog.dismiss()
        }else{
            Toast.makeText(activity, validString, Toast.LENGTH_SHORT).show()
        }
    }
}