package com.romanoindustries.creamsoda.datamodel

import com.google.firebase.firestore.Exclude
import com.google.firebase.firestore.ServerTimestamp
import java.util.*

data class MenuItem(val name: String,
                    val ingredients: String,
                    val description: String,
                    var imageUrl: String,
                    val price: Int,
                    val weight: Int,
                    val tags: List<String>,
                    @get: Exclude var itemType: MenuItemType) {

    @get: Exclude
    var documentID = ""

    @ServerTimestamp
    var date: Date = Date()

    //Needed for FireStore
    constructor(): this("", "", "", "",
        0, 0, listOf(), MenuItemType.HOT_DISH)
}

enum class MenuItemType{
    HOT_DISH,
    SALAD,
    SOUP,
    SIDE,
    DESSERT,
    SUSHI,
    BURGER,
    WOK_NOODLE,
    BRUSCHETTA
}












