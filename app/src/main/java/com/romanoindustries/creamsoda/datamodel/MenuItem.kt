package com.romanoindustries.creamsoda.datamodel

import com.google.firebase.firestore.Exclude
import com.google.firebase.firestore.ServerTimestamp
import java.util.*

data class MenuItem(var name: String,
                    var ingredients: String,
                    var description: String,
                    var imageUrl: String,
                    var price: Int,
                    var weight: Int,
                    var tags: List<String>) {

    @get: Exclude
    var documentID = ""

    @ServerTimestamp
    var date: Date = Date()

    //Needed for FireStore, do not delete
    constructor(): this("", "", "", "",
        0, 0, listOf())
}












