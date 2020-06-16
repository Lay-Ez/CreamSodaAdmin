package com.romanoindustries.creamsoda.menurepository

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore
import com.romanoindustries.creamsoda.datamodel.MenuItem
import com.romanoindustries.creamsoda.datamodel.MenuItemType
import io.reactivex.rxjava3.core.Observable
import java.net.ConnectException

const val MENU_ITEMS = "menuItems"
const val DISH_ITEMS = "items"
const val HOT_DISH = "hotDish"
const val SALAD = "salad"
const val SOUP = "soup"
const val SIDE = "side"
const val DESSERT = "dessert"
const val SUSHI = "sushi"
const val BURGER = "burger"
const val WOK_NOODLE = "wokNoodle"
const val BRUSCHETTA = "bruschetta"

class MenuRepositoryImpl: MenuRepository {

    private val db = FirebaseFirestore.getInstance()

    private val menuItemsMutable: MutableLiveData<List<MenuItem>> = MutableLiveData()
    override val menuItems: LiveData<List<MenuItem>> = menuItemsMutable

    override fun insertMenuItem(menuItem: MenuItem): Observable<Int> {
        return Observable.create {subscriber ->
            getMenuItemCollection(menuItem).document().set(menuItem)
                .addOnSuccessListener {
                    subscriber.onComplete()
                }
                .addOnFailureListener {
                    subscriber.onError(ConnectException("Couldn't insert menu item to database"))
                }
                .addOnCompleteListener {
                    subscriber.onComplete()
                }
        }
    }

    override fun updateMenuItem(menuItem: MenuItem): Observable<Int> {
        TODO("Not yet implemented")
    }

    override fun loadMenuItems(menuItemType: MenuItemType): Observable<Int> {
        TODO("Not yet implemented")
    }

    override fun deleteMenuItem(menuItem: MenuItem): Observable<Int> {
        TODO("Not yet implemented")
    }

    private fun getMenuItemCollection(menuItem: MenuItem): CollectionReference {
        val docName = when (menuItem.itemType) {
            MenuItemType.HOT_DISH -> HOT_DISH
            MenuItemType.SALAD -> SALAD
            MenuItemType.SOUP -> SOUP
            MenuItemType.SIDE -> SIDE
            MenuItemType.DESSERT -> DESSERT
            MenuItemType.SUSHI -> SUSHI
            MenuItemType.BURGER -> BURGER
            MenuItemType.WOK_NOODLE -> WOK_NOODLE
            MenuItemType.BRUSCHETTA -> BRUSCHETTA
        }
        return db.collection(MENU_ITEMS).document(docName).collection(DISH_ITEMS)
    }
}



















