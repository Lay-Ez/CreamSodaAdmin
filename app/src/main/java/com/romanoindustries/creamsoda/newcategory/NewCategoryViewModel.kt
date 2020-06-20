package com.romanoindustries.creamsoda.newcategory

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
import com.romanoindustries.creamsoda.menurepository.MenuRepository
import javax.inject.Inject

class NewCategoryViewModel : ViewModel() {

    @Inject
    lateinit var storageReference: StorageReference

    private lateinit var menuRepo: MenuRepository

    private val imageUrlMutable: MutableLiveData<String> = MutableLiveData()
    val imageUrl: LiveData<String> = imageUrlMutable

    private val uploadProgressMutable: MutableLiveData<Int> = MutableLiveData(0)
    val uploadProgress: LiveData<Int> = uploadProgressMutable

    private val isLoadingMutable: MutableLiveData<Boolean> = MutableLiveData(false)
    val isLoading: LiveData<Boolean> = isLoadingMutable

    private var currentTask: StorageTask<UploadTask.TaskSnapshot>? = null

    fun setCorrectRepository(repositoryComponent: RepositoryComponent, category: String) {
        menuRepo = when (category) {
            CATEGORY_FOOD -> repositoryComponent.getFoodRepository()
            else -> repositoryComponent.getDrinksRepository()
        }
    }

    fun uploadImage(imageUri: Uri, contentResolver: ContentResolver) {
        cancelImageUpload()
        isLoadingMutable.value = true
        currentTask = storageReference.child("${System.currentTimeMillis()}.${getFileExtension(imageUri, contentResolver)}")
            .putFile(imageUri)
            .addOnSuccessListener {taskSnapshot ->
                val taskUri = taskSnapshot.storage.downloadUrl
                taskUri.addOnSuccessListener {
                    imageUrlMutable.value = it.toString()
                }
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

    private fun getFileExtension(uri: Uri, contentResolver: ContentResolver): String? {
        val mime = MimeTypeMap.getSingleton()
        return mime.getExtensionFromMimeType(contentResolver.getType(uri))
    }

}