package com.romanoindustries.creamsoda.menurepository

import android.util.Log
import androidx.lifecycle.LiveData
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore
import com.romanoindustries.creamsoda.datamodel.MenuCategory
import javax.inject.Inject

class MenuCategoriesLiveData @Inject constructor(): LiveData<List<MenuCategory>>() {

    private val menuCollectionRef = FirebaseFirestore.getInstance().collection(MENU_COLLECTION)

    override fun onActive() {
        menuCollectionRef
            .addSnapshotListener{ querySnapshot, firestoreException ->
                if (firestoreException != null) {
                    // Error
                } else {
                    querySnapshot?.let {query->
                        val menuCategoryList = arrayListOf<MenuCategory>()
                        for (documentSnapshot in query) {
                            val menuCategory = documentSnapshot.toObject(MenuCategory::class.java)
                            menuCategory.documentId = documentSnapshot.id
                            menuCategoryList.add(menuCategory)
                        }
                        value = menuCategoryList
                    }
                }
            }
    }
}