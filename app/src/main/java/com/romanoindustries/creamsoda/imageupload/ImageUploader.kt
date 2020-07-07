package com.romanoindustries.creamsoda.imageupload

import android.content.ContentResolver
import android.net.Uri
import android.webkit.MimeTypeMap
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.StorageTask
import com.google.firebase.storage.UploadTask
import java.lang.Exception
import javax.inject.Inject


class ImageUploader @Inject constructor(var storageReference: StorageReference) {

    private var currentTask: StorageTask<UploadTask.TaskSnapshot>? = null
    var imageName = ""

    private val imageUrlMutable: MutableLiveData<String> = MutableLiveData()
    val imageUrl: LiveData<String> = imageUrlMutable

    private val uploadProgressMutable: MutableLiveData<Int> = MutableLiveData(0)
    val uploadProgress: LiveData<Int> = uploadProgressMutable

    private val isLoadingMutable: MutableLiveData<Boolean> = MutableLiveData(false)
    val isLoading: LiveData<Boolean> = isLoadingMutable

    fun uploadImage(imageUri: Uri, contentResolver: ContentResolver): StorageTask<UploadTask.TaskSnapshot> {
        cancelImageUpload()
        deleteCurrentImage()
        imageName = "${System.currentTimeMillis()}.${getFileExtension(imageUri, contentResolver)}"
        return storageReference.child(imageName).putFile(imageUri)
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