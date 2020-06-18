package com.romanoindustries.creamsoda

import com.romanoindustries.creamsoda.menurepository.MenuRepository
import com.romanoindustries.creamsoda.menurepository.MenuRepositoryModule
import dagger.Component
import javax.inject.Named

@Component(modules = [MenuRepositoryModule::class])
interface RepositoryComponent {

    @Named("menu_repo")
    fun getMenuRepository(): MenuRepository

    @Named("drinks_repo")
    fun getDrinksRepository(): MenuRepository
}