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

    private val imageUrlMutable: MutableLiveData<String> = MutableLiveData(/*"https://im0-tub-ru.yandex.net/i?id=b07a998c13dd818d596d4d1212099218&n=13&exp=1"*/)
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
        isLoadingMutable.value = true
        currentTask?.cancel()
        currentTask = storageReference.child("${System.currentTimeMillis()}.${getFileExtension(imageUri, contentResolver)}")
            .putFile(imageUri)
            .addOnSuccessListener {taskSnapshot ->
                val taskUri = taskSnapshot.storage.downloadUrl
                taskUri.addOnSuccessListener {
                    imageUrlMutable.value = it.toString()
                }
                isLoadingMutable.value = false
            }
            .addOnProgressListener {
                val progress = (100.00 * it.bytesTransferred / it.totalByteCount)
                uploadProgressMutable.value = progress.toInt()
            }
    }

    private fun getFileExtension(uri: Uri, contentResolver: ContentResolver): String? {
        val mime = MimeTypeMap.getSingleton()
        return mime.getExtensionFromMimeType(contentResolver.getType(uri))
    }

}