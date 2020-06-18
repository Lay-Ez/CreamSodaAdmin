package com.romanoindustries.creamsoda.menurepository

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.FirebaseFirestore
import com.romanoindustries.creamsoda.datamodel.MenuCategory
import com.romanoindustries.creamsoda.datamodel.MenuItem
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.CompletableEmitter
import javax.inject.Inject
import javax.inject.Named

const val MENU_ITEMS_COLLECTION = "menuItems"
const val MENU_REPO_NAME = "menu_repo"
const val DRINKS_REPO_NAME = "drinks_repo"

open class MenuRepositoryImpl @Inject constructor(menuCategoriesLiveData: MenuCategoriesLiveData,
        @Named("menu_collection_root_name") rootCollectionName: String): MenuRepository {

    private val db = FirebaseFirestore.getInstance()
    private val menuCollectionRef = db.collection(rootCollectionName)

    override val menuCategories: LiveData<List<MenuCategory>> = menuCategoriesLiveData

    private val menuItemsMutable = MutableLiveData<List<MenuItem>>()
    override val menuItems: LiveData<List<MenuItem>> = menuItemsMutable

    override fun insertMenuCategory(menuCategory: MenuCategory): Completable {
        return Completable.create { subscriber ->
            val task = menuCollectionRef
                .document()
                .set(menuCategory)
            addListenersToTask(task, subscriber)
        }
    }

    override fun updateMenuCategory(menuCategory: MenuCategory): Completable {
        return Completable.create {subscriber ->
            val task = menuCollectionRef
                .document(menuCategory.documentId)
                .set(menuCategory)
            addListenersToTask(task, subscriber)
        }
    }

    override fun deleteMenuCategory(menuCategory: MenuCategory): Completable {
        return Completable.create {subscriber ->
            val task = menuCollectionRef
                .document(menuCategory.documentId)
                .delete()
            addListenersToTask(task, subscriber)
        }
    }

    override fun insertMenuItem(menuCategory: MenuCategory, menuItem: MenuItem): Completable {
        return Completable.create {subscriber ->
            val task = menuCollectionRef
                .document(menuCategory.documentId)
                .collection(MENU_ITEMS_COLLECTION)
                .document()
                .set(menuItem)
            addListenersToTask(task, subscriber)
        }
    }

    override fun updateMenuItem(menuCategory: MenuCategory, menuItem: MenuItem): Completable {
        return Completable.create {subscriber ->
            val task = menuCollectionRef
                .document(menuCategory.documentId)
                .collection(MENU_ITEMS_COLLECTION)
                .document(menuItem.documentID)
                .set(menuItem)
            addListenersToTask(task, subscriber)
        }
    }

    override fun deleteMenuItem(menuCategory: MenuCategory, menuItem: MenuItem): Completable {
        return Completable.create {subscriber ->
            val task = menuCollectionRef
                .document(menuCategory.documentId)
                .collection(MENU_ITEMS_COLLECTION)
                .document(menuItem.documentID)
                .delete()
            addListenersToTask(task, subscriber)
        }
    }

    override fun loadMenuItems(menuCategory: MenuCategory): Completable {
        return Completable.create {subscriber ->
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

    private fun addListenersToTask(task: Task<Void>, subscriber: CompletableEmitter) {
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






