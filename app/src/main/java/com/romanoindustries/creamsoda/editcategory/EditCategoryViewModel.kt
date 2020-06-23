package com.romanoindustries.creamsoda.editcategory

import android.content.ContentResolver
import android.net.Uri
import android.webkit.MimeTypeMap
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.StorageTask
import com.google.firebase.storage.UploadTask
import com.romanoindustries.creamsoda.RepositoryComponent
import com.romanoindustries.creamsoda.datamodel.MenuCategory
import com.romanoindustries.creamsoda.menurepository.MenuRepository
import com.romanoindustries.creamsoda.newcategory.*
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.subjects.PublishSubject
import java.lang.Exception
import javax.inject.Inject

class EditCategoryViewModel: ViewModel() {

    @Inject
    lateinit var storageReference: StorageReference

    private lateinit var menuRepo: MenuRepository

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
            imageUrlMutable.value = it.imageUrl
            imageName = it.imageName
            initialCategory = it
        }
    }

    fun uploadImage(imageUri: Uri, contentResolver: ContentResolver) {
        cancelImageUpload()
        deleteCurrentImage()
        isLoadingMutable.value = true
        imageName = "${System.currentTimeMillis()}.${getFileExtension(imageUri, contentResolver)}"
        currentTask = storageReference.child(imageName)
            .putFile(imageUri)
            .addOnSuccessListener {taskSnapshot ->
                val taskUri = taskSnapshot.storage.downloadUrl
                taskUri.addOnSuccessListener {
                    imageUrlMutable.value = it.toString()
                }
            }
            .addOnFailureListener {
                errorChannelSubject.onNext(ERROR_UPLOADING_IMAGE)
            }
            .addOnProgressListener {
                val progress = (100.00 * it.bytesTransferred / it.totalByteCount)
                uploadProgressMutable.value = progress.toInt()
            }
            .addOnCompleteListener{
                isLoadingMutable.value = false
            }
    }

    fun saveCategory(name: String, description: String) {
        var isInputOk = true
        if (name.isBlank()) {
            isInputOk = false
            errorChannelSubject.onNext(ERROR_EMPTY_NAME)
        }
        val imageUrl = imageUrl.value
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
        initialCategory.apply {
            this.name = name
            this.description = description
            this.imageUrl = imageUrl!!
        }
        initialCategory.imageName = imageName
        if (imageName != initialImageName && imageName.isNotBlank()) {
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
        if (imageName != initialCategory.imageName) {
            try {
                storageReference.child(imageName).delete()
            } catch (e: Exception) {}
        }
    }

    fun cancelImageUpload() {
        currentTask?.cancel()
        isLoadingMutable.value = false
        uploadProgressMutable.value = 0
        imageUrlMutable.value = "-" /* cannot set to empty - picasso will throw error*/
    }

    private fun getFileExtension(uri: Uri, contentResolver: ContentResolver): String? {
        val mime = MimeTypeMap.getSingleton()
        return mime.getExtensionFromMimeType(contentResolver.getType(uri))
    }
}