package com.romanoindustries.creamsoda.menurepository

import androidx.lifecycle.LiveData
import com.romanoindustries.creamsoda.datamodel.MenuItem
import com.romanoindustries.creamsoda.datamodel.MenuItemType
import io.reactivex.rxjava3.core.Observable

interface MenuRepository {
    val menuItems: LiveData<List<MenuItem>>

    fun insertMenuItem(menuItem: MenuItem): Observable<Int>
    fun updateMenuItem(menuItem: MenuItem): Observable<Int>
    fun loadMenuItems(menuItemType: MenuItemType): Observable<Int>
    fun deleteMenuItem(menuItem: MenuItem): Observable<Int>
}