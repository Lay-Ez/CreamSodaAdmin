package com.romanoindustries.creamsoda.editmenuitem

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
import com.romanoindustries.creamsoda.datamodel.MenuItem
import com.romanoindustries.creamsoda.editcategory.CATEGORY_OBJECT_KEY
import com.romanoindustries.creamsoda.newcategory.*
import com.squareup.picasso.Picasso
import io.reactivex.rxjava3.disposables.CompositeDisposable
import kotlinx.android.synthetic.main.activity_edit_menu_item.*
import kotlinx.android.synthetic.main.activity_edit_menu_item_inner.*

const val MENU_ITEM_OBJECT_KEY = "menu_item"

class EditMenuItemActivity : AppCompatActivity() {

    private lateinit var viewModel: EditMenuItemViewModel
    private val compositeDisposable = CompositeDisposable()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_menu_item)
        parseIntent()
        observeViewModel()
    }

    private fun parseIntent() {
        viewModel = ViewModelProvider(this).get(EditMenuItemViewModel::class.java)

        val repoComponent = (application as MyApp).repositoryComponent
        val category = intent.getStringExtra(CATEGORY_TYPE_KEY)!!
        val menuCategory = Gson().fromJson(intent.getStringExtra(CATEGORY_OBJECT_KEY), MenuCategory::class.java)
        val menuItem = Gson().fromJson(intent.getStringExtra(MENU_ITEM_OBJECT_KEY), MenuItem::class.java)
        if (!viewModel.valuesInitialized) {
            viewModel.setupValues(repoComponent, category, menuCategory, menuItem)
            fillTextFields(menuItem)
        }
    }

    private fun fillTextFields(menuItem: MenuItem) {
        edit_text_name.setText(menuItem.name)
        edit_text_description.setText(menuItem.description)
        edit_text_ingredients.setText(menuItem.ingredients)
        edit_text_weight.setText(menuItem.weight.toString())
        edit_text_price.setText(menuItem.price.toString())
        val sb = StringBuilder()
        for (i in menuItem.tags.indices) {
            sb.append(menuItem.tags[i])
            if (i != menuItem.tags.size-1) {
                sb.append(", ")
            }
        }
        edit_text_tags.setText(sb.toString())
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
        }.also { compositeDisposable.add(it) }

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
}






















