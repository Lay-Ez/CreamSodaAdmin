package com.romanoindustries.creamsoda

import com.romanoindustries.creamsoda.menurepository.DRINKS_REPO_NAME
import com.romanoindustries.creamsoda.menurepository.MENU_REPO_NAME
import com.romanoindustries.creamsoda.menurepository.MenuRepository
import com.romanoindustries.creamsoda.menurepository.MenuRepositoryModule
import dagger.Component
import javax.inject.Named

@Component(modules = [MenuRepositoryModule::class])
interface RepositoryComponent {

    @Named(MENU_REPO_NAME)
    fun getMenuRepository(): MenuRepository

    @Named(DRINKS_REPO_NAME)
    fun getDrinksRepository(): MenuRepository

    fun inject(mainActivity: MainActivity)
}