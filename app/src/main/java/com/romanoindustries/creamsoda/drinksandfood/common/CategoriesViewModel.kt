package com.romanoindustries.creamsoda.drinksandfood.common

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.romanoindustries.creamsoda.datamodel.MenuCategory
import com.romanoindustries.creamsoda.menurepository.DRINKS_REPO_NAME
import com.romanoindustries.creamsoda.menurepository.FOOD_REPO_NAME
import com.romanoindustries.creamsoda.menurepository.MenuRepository
import javax.inject.Inject
import javax.inject.Named

class CategoriesViewModel: ViewModel() {

    @Inject
    @Named(FOOD_REPO_NAME)
    lateinit var foodRepo: MenuRepository

    @Inject
    @Named(DRINKS_REPO_NAME)
    lateinit var drinksRepo: MenuRepository

    lateinit var foodCategoriesLiveData: LiveData<List<MenuCategory>>
    lateinit var drinkCategoriesLiveData: LiveData<List<MenuCategory>>

    fun init() {
        foodCategoriesLiveData = foodRepo.menuCategories
        drinkCategoriesLiveData = drinksRepo.menuCategories
    }
}






















