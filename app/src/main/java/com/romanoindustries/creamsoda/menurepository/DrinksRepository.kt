package com.romanoindustries.creamsoda.menurepository

import javax.inject.Inject
import javax.inject.Named

class DrinksRepository @Inject constructor(drinksCategoriesLiveData: DrinksCategoriesLiveData,
                    @Named("drinks_collection_root_name") rootCollectionName: String)
    : MenuRepositoryImpl(drinksCategoriesLiveData, rootCollectionName)