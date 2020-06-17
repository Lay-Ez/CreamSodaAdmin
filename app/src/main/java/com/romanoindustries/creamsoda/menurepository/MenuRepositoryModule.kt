package com.romanoindustries.creamsoda.menurepository

import dagger.Binds
import dagger.Module

@Module
abstract class MenuRepositoryModule {

    @Binds
    abstract fun bindsMenuRepository(menuRepositoryImpl: MenuRepositoryImpl): MenuRepository
}