package com.romanoindustries.creamsoda.menurepository

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.FirebaseFirestore
import com.romanoindustries.creamsoda.datamodel.MenuCategory
import com.romanoindustries.creamsoda.datamodel.MenuItem
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.ObservableEmitter

const val MENU_ITEMS_COLLECTION = "menuItems"

class MenuRepositoryImpl: MenuRepository {

    private val db = FirebaseFirestore.getInstance()
    private val menuCollectionRef = db.collection("menu")

    private val menuCategoriesMutable = MutableLiveData<List<MenuCategory>>()
    override val menuCategories: LiveData<List<MenuCategory>> = menuCategoriesMutable

    private val menuItemsMutable = MutableLiveData<List<MenuItem>>()
    override val menuItems: LiveData<List<MenuItem>> = menuItemsMutable

    override fun insertMenuCategory(menuCategory: MenuCategory): Observable<Int> {
        return Observable.create { subscriber ->
            val task = menuCollectionRef.document().set(menuCategory)
            addListenersToTask(task, subscriber)
        }
    }

    override fun updateMenuCategory(menuCategory: MenuCategory): Observable<Int> {
        return Observable.create {subscriber ->
            val task = menuCollectionRef.document(menuCategory.documentId).set(menuCategory)
            addListenersToTask(task, subscriber)
        }
    }

    override fun deleteMenuCategory(menuCategory: MenuCategory): Observable<Int> {
        return Observable.create {subscriber ->
            val task = menuCollectionRef.document(menuCategory.documentId).delete()
            addListenersToTask(task, subscriber)
        }
    }

    override fun loadMenuCategories(): Observable<Int> {
        return Observable.create { subscriber ->
            menuCollectionRef
                .addSnapshotListener{ querySnapshot, firestoreException ->
                if (firestoreException != null) {
                    subscriber.onError(firestoreException)
                } else {
                    querySnapshot?.let {query->
                        val menuCategoryList = arrayListOf<MenuCategory>()
                        for (documentSnapshot in query) {
                            val menuCategory = documentSnapshot.toObject(MenuCategory::class.java)
                            menuCategory.documentId = documentSnapshot.id
                            menuCategoryList.add(menuCategory)
                        }
                        menuCategoriesMutable.value = menuCategoryList
                        subscriber.onComplete()
                    }
                }
            }
        }
    }


    override fun insertMenuItem(menuCategory: MenuCategory, menuItem: MenuItem): Observable<Int> {
        return Observable.create {subscriber ->
            val task = menuCollectionRef
                .document(menuCategory.documentId)
                .collection(MENU_ITEMS_COLLECTION)
                .document()
                .set(menuItem)
            addListenersToTask(task, subscriber)
        }
    }

    override fun updateMenuItem(menuCategory: MenuCategory, menuItem: MenuItem): Observable<Int> {
        return Observable.create {subscriber ->
            val task = menuCollectionRef
                .document(menuCategory.documentId)
                .collection(MENU_ITEMS_COLLECTION)
                .document(menuItem.documentID)
                .set(menuItem)
            addListenersToTask(task, subscriber)
        }
    }

    override fun deleteMenuItem(menuCategory: MenuCategory, menuItem: MenuItem): Observable<Int> {
        return Observable.create {subscriber ->
            val task = menuCollectionRef
                .document(menuCategory.documentId)
                .collection(MENU_ITEMS_COLLECTION)
                .document(menuItem.documentID)
                .delete()
            addListenersToTask(task, subscriber)
        }
    }

    override fun loadMenuItems(menuCategory: MenuCategory): Observable<Int> {
        return Observable.create {subscriber ->
            menuCollectionRef.document(menuCategory.documentId).collection(MENU_ITEMS_COLLECTION)
                .addSnapshotListener { querySnapshot, firestoreException ->
                    if (firestoreException != null) {
                        subscriber.onError(firestoreException)
                    } else {
                        querySnapshot?.let { query ->
                            val menuItemList = arrayListOf<MenuItem>()
                            for (documentSnapshot in query) {
                                val menuItem = documentSnapshot.toObject(MenuItem::class.java)
                                menuItem.documentID = documentSnapshot.id
                                menuItemList.add(menuItem)
                            }
                            menuItemsMutable.value = menuItemList
                            subscriber.onComplete()
                        }
                    }
                }
        }
    }

    private fun addListenersToTask(task: Task<Void>, subscriber: ObservableEmitter<Int>) {
        task.addOnSuccessListener {
            subscriber.onComplete()
            }
            .addOnCompleteListener {
                subscriber.onComplete()
            }
            .addOnFailureListener { e ->
                subscriber.onError(e)
            }
            .addOnCanceledListener {
                subscriber.onComplete()
            }
    }
}






