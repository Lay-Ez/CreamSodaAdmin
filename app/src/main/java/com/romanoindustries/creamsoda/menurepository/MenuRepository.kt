package com.romanoindustries.creamsoda.menurepository

import androidx.lifecycle.LiveData
import com.romanoindustries.creamsoda.datamodel.MenuCategory
import com.romanoindustries.creamsoda.datamodel.MenuItem
import io.reactivex.rxjava3.core.Observable

interface MenuRepository {

    val menuCategories: LiveData<List<MenuCategory>>
    val menuItems: LiveData<List<MenuItem>>

    fun insertMenuCategory(menuCategory: MenuCategory): Observable<Int>
    fun updateMenuCategory(menuCategory: MenuCategory): Observable<Int>
    fun deleteMenuCategory(menuCategory: MenuCategory): Observable<Int>
    fun loadMenuCategories(): Observable<Int>

    fun insertMenuItem(menuCategory: MenuCategory, menuItem: MenuItem): Observable<Int>
    fun updateMenuItem(menuCategory: MenuCategory, menuItem: MenuItem): Observable<Int>
    fun deleteMenuItem(menuCategory: MenuCategory, menuItem: MenuItem): Observable<Int>
    fun loadMenuItems(menuCategory: MenuCategory): Observable<Int>
}