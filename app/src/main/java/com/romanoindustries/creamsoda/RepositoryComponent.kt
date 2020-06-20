package com.romanoindustries.creamsoda

import com.google.firebase.storage.StorageReference
import com.romanoindustries.creamsoda.drinksandfood.common.CategoriesViewModel
import com.romanoindustries.creamsoda.menurepository.*
import com.romanoindustries.creamsoda.newcategory.NewCategoryActivity
import com.romanoindustries.creamsoda.newcategory.NewCategoryViewModel
import dagger.Component
import javax.inject.Named

@Component(modules = [MenuRepositoryModule::class])
interface RepositoryComponent {

    @Named(FOOD_REPO_NAME)
    fun getFoodRepository(): MenuRepository

    @Named(DRINKS_REPO_NAME)
    fun getDrinksRepository(): MenuRepository

    fun getImagesStorageReference(): StorageReference

    fun inject(categoriesViewModel: CategoriesViewModel)

    fun inject(newCategoryViewModel: NewCategoryViewModel)

    fun inject(newCategoryActivity: NewCategoryActivity)
}