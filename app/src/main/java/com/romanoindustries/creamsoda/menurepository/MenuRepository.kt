package com.romanoindustries.creamsoda.menurepository

import androidx.lifecycle.LiveData
import com.romanoindustries.creamsoda.datamodel.MenuCategory
import com.romanoindustries.creamsoda.datamodel.MenuItem
import io.reactivex.rxjava3.core.Completable

interface MenuRepository {

    val menuCategories: LiveData<List<MenuCategory>>
    val menuItems: LiveData<List<MenuItem>>

    fun insertMenuCategory(menuCategory: MenuCategory): Completable
    fun updateMenuCategory(menuCategory: MenuCategory): Completable
    fun deleteMenuCategory(menuCategory: MenuCategory): Completable


    fun insertMenuItem(menuCategory: MenuCategory, menuItem: MenuItem): Completable
    fun updateMenuItem(menuCategory: MenuCategory, menuItem: MenuItem): Completable
    fun deleteMenuItem(menuCategory: MenuCategory, menuItem: MenuItem): Completable
    fun loadMenuItems(menuCategory: MenuCategory): Completable
}