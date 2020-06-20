package com.romanoindustries.creamsoda.newcategory

import android.app.Activity
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.webkit.MimeTypeMap
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.storage.StorageReference
import com.romanoindustries.creamsoda.MyApp
import com.romanoindustries.creamsoda.R
import com.romanoindustries.creamsoda.RepositoryComponent
import com.squareup.picasso.Picasso
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.disposables.CompositeDisposable
import kotlinx.android.synthetic.main.activity_new_category.*
import kotlinx.android.synthetic.main.activity_new_category_inner.*

const val CATEGORY_TYPE_KEY = "category_type"
const val CATEGORY_FOOD = "food"
const val CATEGORY_DRINKS = "drinks"
const val PICK_IMAGE_REQUEST = 1

class NewCategoryActivity : AppCompatActivity() {

    lateinit var repositoryComponent: RepositoryComponent
    lateinit var viewModel: NewCategoryViewModel
    private val compositeDisposable = CompositeDisposable()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_new_category)
        repositoryComponent = (application as MyApp).repositoryComponent
        repositoryComponent.inject(this)
        setupViewModel(intent)
    }

    override fun onDestroy() {
        super.onDestroy()
        compositeDisposable.dispose()
    }

    override fun onResume() {
        super.onResume()
        setupListeners()
        text_view_name.text = edit_text_name.text
    }

    private fun setupListeners() {
        toolbar.setNavigationOnClickListener { onBackPressed() }
        val dispose = getNameChanges().subscribe {
            text_view_name.text = it
            text_input_layout_name.error = null
        }
        compositeDisposable.add(dispose)
    }

    private fun setupViewModel(intent: Intent) {
        viewModel = ViewModelProvider(this).get(NewCategoryViewModel::class.java)
        val category = intent.getStringExtra(CATEGORY_TYPE_KEY)
        if (category == null) {
            Snackbar.make(toolbar, R.string.unknown_error_occurred, Snackbar.LENGTH_SHORT).show()
            onBackPressed()
        } else {
            repositoryComponent.inject(viewModel)
            viewModel.setCorrectRepository(repositoryComponent, category)
        }
        viewModel.imageUrl.observe(this, Observer {url ->
            Picasso.get().load(url).into(image_view_preview)
        })
        viewModel.isLoading.observe(this, Observer { uploading ->
            if (uploading) {
                displayLoadingState()
            } else {
                displayDefaultState()
            }
        })

        viewModel.uploadProgress.observe(this, Observer {progress ->
            if (progress <= 10) {
                progress_bar_upload.progress = 10
            } else {
                progress_bar_upload.progress = progress
            }
        })
    }

    private fun displayLoadingState() {
        progress_bar_upload.visibility = View.VISIBLE
        image_button_add_photo.setImageResource(R.drawable.ic_black_clear_24)
        image_button_add_photo.setOnClickListener { viewModel.cancelImageUpload() }
    }

    private fun displayDefaultState() {
        progress_bar_upload.visibility = View.INVISIBLE
        image_button_add_photo.setImageResource(R.drawable.ic_black_add_a_photo_24)
        image_button_add_photo.setOnClickListener { openImagePicker() }
    }

    private fun openImagePicker() {
        with(Intent()) {
            type = "image/*"
            action = Intent.ACTION_GET_CONTENT
            startActivityForResult(this, PICK_IMAGE_REQUEST)
        }
    }

    private fun getNameChanges(): Observable<String> {
        return Observable.create {subscriber ->
            edit_text_name.addTextChangedListener( object: TextWatcher {
                override fun afterTextChanged(s: Editable?) {}
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                    subscriber.onNext(s.toString())
                }
            })
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == PICK_IMAGE_REQUEST
            && resultCode == Activity.RESULT_OK
            && data != null
            && data.data != null) {
            val imageUri = data.data!!
            viewModel.uploadImage(imageUri, contentResolver)
        }
    }
}