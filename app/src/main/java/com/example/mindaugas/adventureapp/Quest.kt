package com.example.mindaugas.adventureapp

import com.google.android.gms.maps.model.LatLng
import java.io.Serializable

class Quest(var name: String, var description: String, var Answer: String, var location: LatLng, var markerID: String?): Serializable