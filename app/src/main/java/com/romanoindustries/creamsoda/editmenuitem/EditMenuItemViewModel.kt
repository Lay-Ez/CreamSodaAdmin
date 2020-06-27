package com.romanoindustries.creamsoda.editmenuitem

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.StorageTask
import com.google.firebase.storage.UploadTask
import com.romanoindustries.creamsoda.RepositoryComponent
import com.romanoindustries.creamsoda.datamodel.MenuCategory
import com.romanoindustries.creamsoda.datamodel.MenuItem
import com.romanoindustries.creamsoda.menurepository.MenuRepository
import com.romanoindustries.creamsoda.newcategory.CATEGORY_FOOD
import com.romanoindustries.creamsoda.newcategory.STATE_DEFAULT
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.subjects.PublishSubject

class EditMenuItemViewModel: ViewModel() {

    var valuesInitialized = false

    private lateinit var storageReference: StorageReference
    private lateinit var menuRepo: MenuRepository
    private lateinit var menuCategory: MenuCategory
    private lateinit var initialMenuItem: MenuItem

    private val imageUrlMutable: MutableLiveData<String> = MutableLiveData()
    val imageUrl: LiveData<String> = imageUrlMutable

    private val uploadProgressMutable: MutableLiveData<Int> = MutableLiveData(0)
    val uploadProgress: LiveData<Int> = uploadProgressMutable

    private val isLoadingMutable: MutableLiveData<Boolean> = MutableLiveData(false)
    val isLoading: LiveData<Boolean> = isLoadingMutable

    private val errorChannelSubject = PublishSubject.create<Int>()
    val errorChannel: Observable<Int> = errorChannelSubject

    private val stateMutable = MutableLiveData<Int>(STATE_DEFAULT)
    val state: LiveData<Int> = stateMutable

    private var currentTask: StorageTask<UploadTask.TaskSnapshot>? = null
    private var imageName = ""


    fun setupValues(repositoryComponent: RepositoryComponent,
                    category: String, menuCategory: MenuCategory, menuItem: MenuItem) {
        this.menuRepo = when (category) {
            CATEGORY_FOOD -> repositoryComponent.getFoodRepository()
            else -> repositoryComponent.getDrinksRepository()
        }
        this.storageReference = repositoryComponent.getImagesStorageReference()
        this.menuCategory = menuCategory
        initialMenuItem = menuItem
    }

    fun cancelImageUpload() {
        currentTask?.cancel()
        isLoadingMutable.value = false
        uploadProgressMutable.value = 0
        imageUrlMutable.value = "-" /* cannot set to empty - picasso will throw error*/
    }

}