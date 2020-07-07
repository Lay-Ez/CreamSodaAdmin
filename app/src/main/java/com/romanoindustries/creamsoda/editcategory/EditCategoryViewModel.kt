package com.romanoindustries.creamsoda.editcategory

import android.content.ContentResolver
import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.storage.StorageReference
import com.romanoindustries.creamsoda.RepositoryComponent
import com.romanoindustries.creamsoda.datamodel.MenuCategory
import com.romanoindustries.creamsoda.imageupload.ImageUploader
import com.romanoindustries.creamsoda.menurepository.MenuRepository
import com.romanoindustries.creamsoda.newcategory.*
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.subjects.PublishSubject
import java.lang.Exception
import javax.inject.Inject

class EditCategoryViewModel: ViewModel() {

    @Inject
    lateinit var storageReference: StorageReference

    @Inject
    lateinit var imageUploader: ImageUploader

    private lateinit var menuRepo: MenuRepository

    lateinit var uploadedImageUrl: LiveData<String>
    lateinit var imageUploadProgress: LiveData<Int>
    lateinit var isImageUploading: LiveData<Boolean>

    private val errorChannelSubject = PublishSubject.create<Int>()
    val errorChannel: Observable<Int> = errorChannelSubject

    private val stateMutable = MutableLiveData<Int>(STATE_DEFAULT)
    val state: LiveData<Int> = stateMutable

    lateinit var initialCategory: MenuCategory
    var valuesInitialized = false


    fun setCorrectRepository(repositoryComponent: RepositoryComponent, category: String) {
        menuRepo = when (category) {
            CATEGORY_FOOD -> repositoryComponent.getFoodRepository()
            else -> repositoryComponent.getDrinksRepository()
        }
        repositoryComponent.inject(this)
    }

    fun initValues(menuCategory: MenuCategory) {
        menuCategory.let {
            imageUploader.setInitialValues(it.imageName, it.imageUrl)
            initialCategory = it
        }
        imageUploader.let {
            uploadedImageUrl = it.imageUrl
            imageUploadProgress = it.uploadProgress
            isImageUploading = it.isLoading
        }
    }

    fun uploadImage(imageUri: Uri, contentResolver: ContentResolver) {
        cancelImageUpload()
        deleteCurrentImage()
        imageUploader.uploadImage(imageUri, contentResolver)
            .addOnFailureListener {
                errorChannelSubject.onNext(ERROR_UPLOADING_IMAGE)
            }
    }

    fun saveCategory(name: String, description: String) {
        var isInputOk = true
        if (name.isBlank()) {
            isInputOk = false
            errorChannelSubject.onNext(ERROR_EMPTY_NAME)
        }
        val imageUrl = uploadedImageUrl.value
        if (imageUrl == null || imageUrl.length  <= 5) {
            isInputOk = false
            errorChannelSubject.onNext(ERROR_IMAGE_NOT_LOADED)
        }
        if (!isInputOk) {
            stateMutable.value = STATE_DEFAULT
            return
        }

        stateMutable.value = STATE_SAVING
        val initialImageName = initialCategory.imageName
        val currentImageName = imageUploader.imageName
        initialCategory.apply {
            this.name = name
            this.description = description
            this.imageUrl = imageUrl!!
        }
        initialCategory.imageName = currentImageName
        if (currentImageName != initialImageName && currentImageName.isNotBlank()) {
            storageReference.child(initialImageName).delete()
        }
        menuRepo.updateMenuCategory(initialCategory)
            .subscribe({
                stateMutable.value = STATE_SAVED
            }, {
                errorChannelSubject.onNext(ERROR_SAVING_CATEGORY)
                stateMutable.value = STATE_DEFAULT
            })
    }

    fun deleteCategory() {
        stateMutable.value = STATE_SAVING
        cancelImageUpload()
        deleteCurrentImage()
        storageReference.child(initialCategory.imageName)
            .delete()
            .addOnSuccessListener {
                menuRepo.deleteMenuCategory(initialCategory)
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

    fun deleteCurrentImage() {
        val currentImageName = imageUploader.imageName
        if (currentImageName != initialCategory.imageName) {
            try {
                imageUploader.deleteCurrentImage()
            } catch (e: Exception) {}
        }
    }

    fun cancelImageUpload() {
        imageUploader.cancelImageUpload()
    }
}