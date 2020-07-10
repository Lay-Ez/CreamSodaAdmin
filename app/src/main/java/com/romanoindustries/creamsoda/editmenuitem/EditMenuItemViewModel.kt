package com.romanoindustries.creamsoda.editmenuitem

import android.content.ContentResolver
import android.net.Uri
import android.webkit.MimeTypeMap
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.storage.StorageReference
import com.romanoindustries.creamsoda.RepositoryComponent
import com.romanoindustries.creamsoda.datamodel.MenuCategory
import com.romanoindustries.creamsoda.datamodel.MenuItem
import com.romanoindustries.creamsoda.imageupload.ImageUploader
import com.romanoindustries.creamsoda.menurepository.MenuRepository
import com.romanoindustries.creamsoda.newcategory.*
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.subjects.PublishSubject
import java.lang.Exception

class EditMenuItemViewModel: ViewModel() {

    var valuesInitialized = false

    private lateinit var storageReference: StorageReference
    private lateinit var menuRepo: MenuRepository
    private lateinit var menuCategory: MenuCategory
    private lateinit var initialMenuItem: MenuItem
    private lateinit var imageUploader: ImageUploader

    lateinit var uploadedImageUrl: LiveData<String>
    lateinit var imageUploadProgress: LiveData<Int>
    lateinit var isImageUploading: LiveData<Boolean>

    private val errorChannelSubject = PublishSubject.create<Int>()
    val errorChannel: Observable<Int> = errorChannelSubject

    private val stateMutable = MutableLiveData<Int>(STATE_DEFAULT)
    val state: LiveData<Int> = stateMutable

    fun setupValues(repositoryComponent: RepositoryComponent,
                    category: String, menuCategory: MenuCategory, menuItem: MenuItem) {
        this.menuRepo = when (category) {
            CATEGORY_FOOD -> repositoryComponent.getFoodRepository()
            else -> repositoryComponent.getDrinksRepository()
        }
        this.storageReference = repositoryComponent.getImagesStorageReference()
        this.imageUploader = repositoryComponent.getImageUploader()
        this.menuCategory = menuCategory
        menuItem.let {
            initialMenuItem = it
            imageUploader.setInitialValues(it.imageName, it.imageUrl)
        }
        imageUploader.let {
            uploadedImageUrl = it.imageUrl
            imageUploadProgress = it.uploadProgress
            isImageUploading = it.isLoading
        }
        valuesInitialized = true
    }

    fun uploadImage(imageUri: Uri, contentResolver: ContentResolver) {
        cancelImageUpload()
        deleteCurrentImage()
        imageUploader.uploadImage(imageUri, contentResolver)
            .addOnFailureListener {
                errorChannelSubject.onNext(ERROR_UPLOADING_IMAGE)
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
        val initialImageName = initialMenuItem.imageName
        if (initialImageName != imageName && initialImageName.isNotBlank()) {
            storageReference.child(initialImageName).delete()
        }
        initialMenuItem.apply {
            this.name = name
            this.ingredients = ingredients
            this.description = description
            this.price = price
            this.weight = weight
            this.tags = tags
            this.imageUrl = imageUrl
        }
        initialMenuItem.imageName = imageName
        menuRepo.updateMenuItem(menuCategory, initialMenuItem)
            .subscribe({
                stateMutable.value = STATE_SAVED
            }, {
                errorChannelSubject.onNext(ERROR_SAVING_CATEGORY)
                stateMutable.value = STATE_DEFAULT
            })
    }

    fun deleteItem() {
        stateMutable.value = STATE_SAVING
        cancelImageUpload()
        deleteCurrentImage()
        storageReference.child(initialMenuItem.imageName)
            .delete()
            .addOnSuccessListener {
                menuRepo.deleteMenuItem(menuCategory, initialMenuItem)
                    .subscribe({
                        stateMutable.value = STATE_SAVED
                    }, {
                        stateMutable.value = STATE_DEFAULT
                        errorChannelSubject.onNext(ERROR_SAVING_CATEGORY)
                    })
            }
            .addOnFailureListener {
                stateMutable.value = STATE_DEFAULT
                errorChannelSubject.onNext(ERROR_SAVING_CATEGORY)
            }
    }

    fun cancelImageUpload() {
        imageUploader.cancelImageUpload()
    }

    fun deleteCurrentImage() {
        val currentImageName = imageUploader.imageName
        if (currentImageName != initialMenuItem.imageName) {
            try {
                storageReference.child(currentImageName).delete()
            } catch (e: Exception) {}
        }
    }

}