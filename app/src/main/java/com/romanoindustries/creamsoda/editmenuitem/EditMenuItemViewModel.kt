package com.romanoindustries.creamsoda.editmenuitem

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
import com.romanoindustries.creamsoda.datamodel.MenuItem
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

    fun uploadImage(imageUri: Uri, contentResolver: ContentResolver) {
        cancelImageUpload()
        deleteCurrentImage()
        isLoadingMutable.value = true
        imageName = "${System.currentTimeMillis()}.${getFileExtension(imageUri, contentResolver)}"
        currentTask = storageReference.child(imageName)
            .putFile(imageUri)
            .addOnSuccessListener {taskSnapshot ->
                val taskUri = taskSnapshot.storage.downloadUrl
                taskUri.addOnSuccessListener {uri ->
                    currentTask?.let {
                        if (it.isCanceled) {
                            deleteCurrentImage()
                        } else {
                            imageUrlMutable.value = uri.toString()
                        }
                    }
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

    fun saveItem(name: String,
                 ingredients: String,
                 description: String,
                 price: Int,
                 weight: Int,
                 tags: List<String>) {
        val imageUrl = imageUrl.value
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

    private fun getFileExtension(uri: Uri, contentResolver: ContentResolver): String? {
        val mime = MimeTypeMap.getSingleton()
        return mime.getExtensionFromMimeType(contentResolver.getType(uri))
    }

    fun setupValues(repositoryComponent: RepositoryComponent,
                    category: String, menuCategory: MenuCategory, menuItem: MenuItem) {
        this.menuRepo = when (category) {
            CATEGORY_FOOD -> repositoryComponent.getFoodRepository()
            else -> repositoryComponent.getDrinksRepository()
        }
        this.storageReference = repositoryComponent.getImagesStorageReference()
        this.menuCategory = menuCategory
        initialMenuItem = menuItem
        imageName = initialMenuItem.imageName
        imageUrlMutable.value = menuItem.imageUrl
    }

    fun cancelImageUpload() {
        currentTask?.cancel()
        isLoadingMutable.value = false
        uploadProgressMutable.value = 0
        imageUrlMutable.value = "-" /* cannot set to empty - picasso will throw error*/
    }

    fun deleteCurrentImage() {
        if (imageName != initialMenuItem.imageName) {
            try {
                storageReference.child(imageName).delete()
            } catch (e: Exception) {}
        }
    }

}