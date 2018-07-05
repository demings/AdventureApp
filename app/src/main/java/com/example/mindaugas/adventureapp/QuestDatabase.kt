package com.example.mindaugas.adventureapp

import com.google.android.gms.maps.model.LatLng


class QuestDatabase{
    var quests: Map<String, Quest> = generateMockQuests()

    private fun generateMockQuests(): Map<String, Quest> {
        return mapOf("123456" to Quest("Sidnejus", "uzduotis australijoje", "02", LatLng(-34.0, 151.0), false),
                "123457" to Quest("Lekt. Magistr. Doc. Habil. Dakt. Litenantas Lukas Alfredas Ružauskas", "Blemba kokia lengva uzduotis", "02", LatLng(54.926746, 24.100789), false),
                "123458" to Quest("Arnelio Kvedaraelio", "Imink misle", "02", LatLng(55.4095649, 23.7285911), false),
                "123555" to Quest("Radvilenai", "uzduotis namuose","03", LatLng(54.90720214, 23.93623281), false))
    }
}