package com.example.mindaugas.adventureapp

import com.google.android.gms.maps.model.LatLng


class PlacesDatabase(var places: Map<String, LatLng>){
    fun generatePlaces(){
        places = mapOf("Sydney" to LatLng(-34.0, 151.0))
    }
}