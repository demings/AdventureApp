package com.example.mindaugas.adventureapp

import com.google.android.gms.maps.model.LatLng


class PlacesDatabase{
    var places: Map<String, LatLng> = generateMockPlaces()

    private fun generateMockPlaces(): Map<String, LatLng> {
        return mapOf("Sydney" to LatLng(-34.0, 151.0),
                "House" to LatLng(54.90720214, 23.93623281))
    }
}