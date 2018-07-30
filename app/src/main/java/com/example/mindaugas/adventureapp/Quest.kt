package com.example.mindaugas.adventureapp

import android.os.Parcel
import android.os.Parcelable
import java.util.*

//import com.google.android.gms.maps.model.LatLng

data class Quest(var ID: String = UUID.randomUUID().toString(),
                 var name: String = "",
                 var description: String = "",
                 var answer: String = "",
                 var latitude: Double = 0.0,
                 var longitude: Double = 0.0,
                 var icon: String = "",
                 var creator: String = ""
                 ): Parcelable {
    constructor(parcel: Parcel) : this(
            parcel.readString(),
            parcel.readString(),
            parcel.readString(),
            parcel.readString(),
            parcel.readDouble(),
            parcel.readDouble(),
            parcel.readString(),
            parcel.readString()) {
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(ID)
        parcel.writeString(name)
        parcel.writeString(description)
        parcel.writeString(answer)
        parcel.writeDouble(latitude)
        parcel.writeDouble(longitude)
        parcel.writeString(icon)
        parcel.writeString(creator)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Quest> {
        override fun createFromParcel(parcel: Parcel): Quest {
            return Quest(parcel)
        }

        override fun newArray(size: Int): Array<Quest?> {
            return arrayOfNulls(size)
        }
    }
}