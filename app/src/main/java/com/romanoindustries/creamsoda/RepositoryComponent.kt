package com.romanoindustries.creamsoda

import com.romanoindustries.creamsoda.menurepository.MenuRepository
import com.romanoindustries.creamsoda.menurepository.MenuRepositoryModule
import dagger.Component

@Component(modules = [MenuRepositoryModule::class])
interface RepositoryComponent {

    fun getMenuRepository(): MenuRepository

}