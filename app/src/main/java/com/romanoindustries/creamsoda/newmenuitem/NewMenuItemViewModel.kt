package com.romanoindustries.creamsoda.newmenuitem

import android.content.ContentResolver
import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.romanoindustries.creamsoda.RepositoryComponent
import com.romanoindustries.creamsoda.datamodel.MenuCategory
import com.romanoindustries.creamsoda.datamodel.MenuItem
import com.romanoindustries.creamsoda.imageupload.ImageUploader
import com.romanoindustries.creamsoda.menurepository.MenuRepository
import com.romanoindustries.creamsoda.newcategory.*
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.subjects.PublishSubject

class NewMenuItemViewModel: ViewModel() {

    private lateinit var menuRepo: MenuRepository
    private lateinit var menuCategory: MenuCategory
    private lateinit var imageUploader: ImageUploader

    lateinit var uploadedImageUrl: LiveData<String>
    lateinit var imageUploadProgress: LiveData<Int>
    lateinit var isUploadingImage: LiveData<Boolean>

    private val errorChannelSubject = PublishSubject.create<Int>()
    val errorChannel: Observable<Int> = errorChannelSubject

    private val stateMutable = MutableLiveData<Int>(STATE_DEFAULT)
    val state: LiveData<Int> = stateMutable

    fun initValues(repositoryComponent: RepositoryComponent,
                   category: String, menuCategory: MenuCategory) {

        if (this::menuRepo.isInitialized) return

        this.menuRepo = when (category) {
            CATEGORY_FOOD -> repositoryComponent.getFoodRepository()
            else -> repositoryComponent.getDrinksRepository()
        }
        this.imageUploader = repositoryComponent.getImageUploader()
        this.menuCategory = menuCategory
        this.imageUploader.let {
            uploadedImageUrl = it.imageUrl
            imageUploadProgress = it.uploadProgress
            isUploadingImage = it.isLoading
        }
    }

    fun uploadImage(imageUri: Uri, contentResolver: ContentResolver) {
       imageUploader.run {
           cancelImageUpload()
           deleteCurrentImage()
           uploadImage(imageUri, contentResolver)
               .addOnFailureListener {
                   errorChannelSubject.onNext(ERROR_UPLOADING_IMAGE)
               }
       }
    }

    fun saveItem(name: String,
                 ingredients: String,
                 description: String,
                 price: Int,
                 weight: Int,
                 tags: List<String>) {
        val imageUrl = uploadedImageUrl.value
        val imageName = imageUploader.imageName
        if (imageUrl == null || imageUrl.length < 5 || imageName.isEmpty()) {
            errorChannelSubject.onNext(ERROR_IMAGE_NOT_LOADED)
            return
        }
        stateMutable.value = STATE_SAVING
        val newMenuItem = MenuItem(name, ingredients, description, imageUrl, imageName, price, weight, tags)
        menuRepo.insertMenuItem(menuCategory, newMenuItem)
            .subscribe({
                stateMutable.value = STATE_SAVED
            }, {
                errorChannelSubject.onNext(ERROR_SAVING_CATEGORY)
                stateMutable.value = STATE_DEFAULT
            })
    }

    fun cancelImageUpload() {
        imageUploader.cancelImageUpload()
    }

    fun deleteCurrentImage() {
        imageUploader.deleteCurrentImage()
    }
}














