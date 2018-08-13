package com.example.mindaugas.adventureapp

import android.app.Dialog
import android.os.Build
import android.support.annotation.RequiresApi
import android.view.View
import android.widget.Toast
import com.google.android.gms.location.places.Place
import kotlinx.android.synthetic.main.add_quest_dialog.*
import java.util.*

class CustomAddQuestListener(private var dialog: Dialog, private var activity: MapsActivity, private var place: Place) : View.OnClickListener {

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    override fun onClick(p0: View?) {
        var valid = true
        var validString = "Not valid:"

        // validation

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

            val quest = Quest(
                    UUID.randomUUID().toString(),
                    dialog.questName.text.toString(),
                    dialog.questDescription.text.toString(),
                    dialog.questAnswer.text.toString(),
                    place.latLng.latitude,
                    place.latLng.longitude,
                    MapsActivity.encodeBitmapToBase64(activity.lastAddQuestImage),
                    MapsActivity.currentUser.ID,
                    Constants.DEFAULT_RATING
            )

            activity.firebaseMethods.addQuest(quest)

            activity.getQuestsFromFireStore()

        }else{
            Toast.makeText(activity, validString, Toast.LENGTH_SHORT).show()
        }
    }
}