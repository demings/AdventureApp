package com.example.mindaugas.adventureapp

import android.app.Activity
import android.content.Context
import android.view.View
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.Marker
import kotlinx.android.synthetic.main.quest_info_window.view.*

class QuestInfoWindow(var context: Context) : GoogleMap.InfoWindowAdapter{

    override fun getInfoWindow(p0: Marker?): View? {
        //null sets the default view
        return null
    }

    override fun getInfoContents(p0: Marker?): View {
        var view = (context as Activity).layoutInflater.inflate(R.layout.quest_info_window, null)

        if (p0 != null) {
            view.questInfoWindowImage.setImageBitmap(
                    MapsActivity.decodeBase64ToBitmap((p0.tag as Quest).icon))
        }

        return view
    }
}