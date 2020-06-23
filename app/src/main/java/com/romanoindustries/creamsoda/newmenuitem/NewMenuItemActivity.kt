package com.romanoindustries.creamsoda.newmenuitem

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.snackbar.Snackbar
import com.google.gson.Gson
import com.romanoindustries.creamsoda.MyApp
import com.romanoindustries.creamsoda.R
import com.romanoindustries.creamsoda.datamodel.MenuCategory
import com.romanoindustries.creamsoda.editcategory.CATEGORY_OBJECT_KEY
import com.romanoindustries.creamsoda.newcategory.*
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_new_menu_item.*
import kotlinx.android.synthetic.main.activity_new_menu_item_inner.*

class NewMenuItemActivity : AppCompatActivity() {

    private lateinit var viewModel: NewMenuItemViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_new_menu_item)
        parseIntent()
        observeViewModel()
        setupListeners()
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

    override fun onBackPressed() {
        super.onBackPressed()
        viewModel.imageUrl.removeObservers(this)
        viewModel.cancelImageUpload()
        viewModel.deleteCurrentImage()
    }

    private fun setupListeners() {
        toolbar.setNavigationOnClickListener { onBackPressed() }
        toolbar.setOnMenuItemClickListener {menuItem ->
            when (menuItem.itemId) {
                R.id.mnu_item_save -> {
                    onSavePressed()
                    true
                }
                else -> false
            }
        }
    }

    private fun onSavePressed() {
        if (isInputCorrect()) {

        }
    }

    private fun isInputCorrect(): Boolean {
        return true
    }

    private fun observeViewModel() {
        viewModel.imageUrl.observe(this, Observer {imageUrl ->
            Picasso.get().load(imageUrl).into(image_view_preview)
        })

        viewModel.state.observe(this, Observer {state ->
            when (state) {
                STATE_DEFAULT -> progress_bar_save.visibility = View.INVISIBLE
                STATE_SAVING -> progress_bar_save.visibility = View.VISIBLE
                STATE_SAVED -> finish()
            }
        })

        viewModel.isLoading.observe(this, Observer { uploading ->
            if (uploading) {
                displayUploadingImageState()
            } else {
                displayDefaultImageState()
            }
        })

        viewModel.errorChannel.subscribe {errorCode ->
            val errorMsgResource = when (errorCode) {
                ERROR_UPLOADING_IMAGE -> R.string.error_uploading_image
                ERROR_IMAGE_NOT_LOADED ->  R.string.error_image_not_uploaded
                ERROR_SAVING_CATEGORY ->  R.string.error_saving_category
                else ->  R.string.unknown_error_occurred
            }
            Snackbar.make(toolbar, errorMsgResource, Snackbar.LENGTH_LONG).show()
        }

        viewModel.uploadProgress.observe(this, Observer { progress ->
            progress_bar_upload.progress = progress
        })
    }

    private fun displayUploadingImageState() {
        progress_bar_upload.visibility = View.VISIBLE
        image_button_add_photo.setImageResource(R.drawable.ic_black_clear_24)
        image_button_add_photo.setOnClickListener { viewModel.cancelImageUpload() }
    }

    private fun displayDefaultImageState() {
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

    private fun parseIntent() {
        val categoryType = intent.getStringExtra(CATEGORY_TYPE_KEY)!!
        val passedCategory = Gson().fromJson(intent.getStringExtra(CATEGORY_OBJECT_KEY), MenuCategory::class.java)
        val repositoryComponent = (application as MyApp).repositoryComponent
        viewModel = ViewModelProvider(this).get(NewMenuItemViewModel::class.java)
        viewModel.setupValues(repositoryComponent, categoryType, passedCategory)
        if (categoryType == CATEGORY_FOOD) {
            text_input_weight.hint = getString(R.string.weight_hint)
        } else {
            text_input_weight.hint = getString(R.string.volume_hint)
        }
    }
}
















