package com.gdsc.bingo.services.realm.model

import io.realm.kotlin.types.EmbeddedRealmObject

class GeoPointRealm : EmbeddedRealmObject {
    var latitude: Double = 0.0
    var longitude: Double = 0.0
}