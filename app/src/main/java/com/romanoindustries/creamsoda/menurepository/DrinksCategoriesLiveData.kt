package com.romanoindustries.creamsoda.menurepository

import javax.inject.Inject
import javax.inject.Named

class DrinksCategoriesLiveData @Inject constructor( @Named("drinks_collection_root_name") rootCollection: String):
    MenuCategoriesLiveData(rootCollection)