package com.romanoindustries.creamsoda.viewmenuitems

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.romanoindustries.creamsoda.RepositoryComponent
import com.romanoindustries.creamsoda.datamodel.MenuCategory
import com.romanoindustries.creamsoda.datamodel.MenuItem
import com.romanoindustries.creamsoda.menurepository.MenuRepository
import com.romanoindustries.creamsoda.newcategory.CATEGORY_FOOD

class ViewMenuItemsViewModel: ViewModel() {

    private lateinit var menuRepository: MenuRepository
    lateinit var menuItems: LiveData<List<MenuItem>>

    fun setupMenuItems(categoryType: String,
                        menuCategory: MenuCategory,
                        repositoryComponent: RepositoryComponent) {
        menuRepository = when (categoryType) {
            CATEGORY_FOOD -> repositoryComponent.getFoodRepository()
            else -> repositoryComponent.getDrinksRepository()
        }
        menuItems = menuRepository.menuItems
        menuRepository.loadMenuItems(menuCategory)
            .subscribe()
    }

}