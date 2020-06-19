package com.romanoindustries.creamsoda

import com.google.firebase.storage.StorageReference
import com.romanoindustries.creamsoda.drinksandfood.common.CategoriesViewModel
import com.romanoindustries.creamsoda.menurepository.*
import dagger.Component
import javax.inject.Named

@Component(modules = [MenuRepositoryModule::class])
interface RepositoryComponent {

    @Named(FOOD_REPO_NAME)
    fun getMenuRepository(): MenuRepository

    @Named(DRINKS_REPO_NAME)
    fun getDrinksRepository(): MenuRepository

    fun getImagesStorageReference(): StorageReference

    fun inject(categoriesViewModel: CategoriesViewModel)
}