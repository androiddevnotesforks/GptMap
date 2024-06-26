package com.espressodev.gptmap.core.model.realm

import com.espressodev.gptmap.core.model.Content
import com.espressodev.gptmap.core.model.Coordinates
import io.realm.kotlin.types.EmbeddedRealmObject

open class RealmContent : EmbeddedRealmObject {
    var latitude: Double = 0.0
    var longitude: Double = 0.0
    var city: String = ""
    var district: String = ""
    var country: String = ""
    var poeticDescription: String = ""
    var normalDescription: String = ""
}

fun RealmContent.toContent(): Content = Content(
    coordinates = Coordinates(latitude, longitude),
    city = this.city,
    district = this.district,
    country = this.country,
    poeticDescription = this.poeticDescription,
    normalDescription = this.normalDescription
)
