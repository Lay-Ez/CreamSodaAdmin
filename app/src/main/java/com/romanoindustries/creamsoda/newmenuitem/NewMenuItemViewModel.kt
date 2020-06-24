package com.romanoindustries.creamsoda.newmenuitem

import android.content.ContentResolver
import android.net.Uri
import android.util.Log
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

class NewMenuItemViewModel: ViewModel() {

    private lateinit var storageReference: StorageReference
    private lateinit var menuRepo: MenuRepository
    private lateinit var menuCategory: MenuCategory

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
                    category: String, menuCategory: MenuCategory) {
        this.menuRepo = when (category) {
            CATEGORY_FOOD -> repositoryComponent.getFoodRepository()
            else -> repositoryComponent.getDrinksRepository()
        }
        this.storageReference = repositoryComponent.getImagesStorageReference()
        this.menuCategory = menuCategory
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
        val newMenuItem = MenuItem(name, ingredients, description, imageUrl, imageName, price, weight, tags)
        menuRepo.insertMenuItem(menuCategory, newMenuItem)
            .subscribe({
                stateMutable.value = STATE_SAVED
            }, {
                errorChannelSubject.onNext(ERROR_SAVING_CATEGORY)
                stateMutable.value = STATE_DEFAULT
            })
    }

    private fun getFileExtension(uri: Uri, contentResolver: ContentResolver): String? {
        val mime = MimeTypeMap.getSingleton()
        return mime.getExtensionFromMimeType(contentResolver.getType(uri))
    }

    fun cancelImageUpload() {
        currentTask?.cancel()
        isLoadingMutable.value = false
        uploadProgressMutable.value = 0
        imageUrlMutable.value = "-" /* cannot set to empty - picasso will throw error*/
    }

    fun deleteCurrentImage() {
        if (imageName.isNotBlank()) {
            try {
                storageReference.child(imageName).delete()
            } catch (e: Exception) {}
        }
    }
}














