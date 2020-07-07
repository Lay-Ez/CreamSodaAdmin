package com.romanoindustries.creamsoda.newcategory

import android.content.ContentResolver
import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.romanoindustries.creamsoda.RepositoryComponent
import com.romanoindustries.creamsoda.datamodel.MenuCategory
import com.romanoindustries.creamsoda.imageupload.ImageUploader
import com.romanoindustries.creamsoda.menurepository.MenuRepository
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.subjects.PublishSubject

const val ERROR_UPLOADING_IMAGE = 1
const val ERROR_IMAGE_NOT_LOADED = 2
const val ERROR_EMPTY_NAME = 3
const val ERROR_SAVING_CATEGORY = 4

const val STATE_DEFAULT = 0
const val STATE_SAVING = 1
const val STATE_SAVED = 2

open class NewCategoryViewModel : ViewModel() {

    private lateinit var menuRepo: MenuRepository
    private lateinit var imageUploader: ImageUploader

    lateinit var uploadedImageUrl: LiveData<String>
    lateinit var imageUploadProgress: LiveData<Int>
    lateinit var isUploadingImage: LiveData<Boolean>

    private val errorChannelSubject = PublishSubject.create<Int>()
    val errorChannel: Observable<Int> = errorChannelSubject

    private val stateMutable = MutableLiveData<Int>(STATE_DEFAULT)
    val state: LiveData<Int> = stateMutable

    fun initValues(repositoryComponent: RepositoryComponent, category: String) {
        if (this::menuRepo.isInitialized) return
        menuRepo = when (category) {
            CATEGORY_FOOD -> repositoryComponent.getFoodRepository()
            else -> repositoryComponent.getDrinksRepository()
        }
        imageUploader = repositoryComponent.getImageUploader()
        imageUploader.let {
            uploadedImageUrl = it.imageUrl
            imageUploadProgress = it.uploadProgress
            isUploadingImage = it.isLoading
        }
    }

    fun uploadImage(imageUri: Uri, contentResolver: ContentResolver) {
        imageUploader.uploadImage(imageUri, contentResolver)
            .addOnFailureListener {
                errorChannelSubject.onNext(ERROR_UPLOADING_IMAGE)
            }
    }

    fun cancelImageUpload() {
        imageUploader.cancelImageUpload()
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
        val category = MenuCategory(name, description, imageUrl!!, imageUploader.imageName)
        menuRepo.insertMenuCategory(category)
            .subscribe({
                stateMutable.value = STATE_SAVED
            }, {
                errorChannelSubject.onNext(ERROR_SAVING_CATEGORY)
                stateMutable.value = STATE_DEFAULT
            })
    }

    fun deleteCurrentImage() {
        imageUploader.deleteCurrentImage()
    }
}