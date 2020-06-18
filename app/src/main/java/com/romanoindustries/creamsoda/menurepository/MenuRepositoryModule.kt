package com.romanoindustries.creamsoda.menurepository

import dagger.Binds
import dagger.Module
import dagger.Provides
import javax.inject.Named

@Module
abstract class MenuRepositoryModule {

    @Binds
    @Named(MENU_REPO_NAME)
    abstract fun bindMenuRepository(menuRepositoryImpl: MenuRepositoryImpl): MenuRepository

    @Binds
    @Named(DRINKS_REPO_NAME)
    abstract fun bindDrinksRepository(drinksRepository: DrinksRepository): MenuRepository


    companion object {
        @Provides
        @Named("menu_collection_root_name")
        fun provideMenuCollectionRootName(): String  = "menu"

        @Provides
        @Named("drinks_collection_root_name")
        fun provideDrinksCollectionRootName(): String  = "drinks"
    }
}