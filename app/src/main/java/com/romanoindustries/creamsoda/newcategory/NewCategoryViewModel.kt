package com.romanoindustries.creamsoda.newcategory

import android.content.ClipDescription
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
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.subjects.PublishSubject
import java.lang.Exception
import javax.inject.Inject

const val ERROR_UPLOADING_IMAGE = 1
const val ERROR_IMAGE_NOT_LOADED = 2
const val ERROR_EMPTY_NAME = 3
const val ERROR_SAVING_CATEGORY = 4

const val STATE_DEFAULT = 0
const val STATE_SAVING = 1
const val STATE_SAVED = 2

open class NewCategoryViewModel : ViewModel() {

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

    fun setCorrectRepository(repositoryComponent: RepositoryComponent, category: String) {
        menuRepo = when (category) {
            CATEGORY_FOOD -> repositoryComponent.getFoodRepository()
            else -> repositoryComponent.getDrinksRepository()
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

    fun cancelImageUpload() {
        currentTask?.cancel()
        isLoadingMutable.value = false
        uploadProgressMutable.value = 0
        imageUrlMutable.value = "-" /* cannot set to empty - picasso will throw error*/
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
        val category = MenuCategory(name, description, imageUrl!!, imageName)
        menuRepo.insertMenuCategory(category)
            .subscribe({
                stateMutable.value = STATE_SAVED
            }, {
                errorChannelSubject.onNext(ERROR_SAVING_CATEGORY)
                stateMutable.value = STATE_DEFAULT
            })
    }

    fun deleteCurrentImage() {
        if (imageName.isNotBlank()) {
            try {
                storageReference.child(imageName).delete()
            } catch (e: Exception) {}
        }
    }

    private fun getFileExtension(uri: Uri, contentResolver: ContentResolver): String? {
        val mime = MimeTypeMap.getSingleton()
        return mime.getExtensionFromMimeType(contentResolver.getType(uri))
    }
}














