package com.romanoindustries.creamsoda.datamodel

import com.google.firebase.firestore.Exclude
import com.google.firebase.firestore.ServerTimestamp
import java.util.*

data class MenuCategory(var name: String,
                        var description: String,
                        var imageUrl: String,
                        var imageName: String) {

    @get: Exclude
    var documentId = ""

    @ServerTimestamp
    var date: Date = Date()

    //Needed for FireStore, do not delete
    constructor(): this("", "", "", "")
}