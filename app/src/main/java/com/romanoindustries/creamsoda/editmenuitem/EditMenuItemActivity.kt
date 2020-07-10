package com.romanoindustries.creamsoda.editmenuitem

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
import com.romanoindustries.creamsoda.datamodel.MenuItem
import com.romanoindustries.creamsoda.editcategory.CATEGORY_OBJECT_KEY
import com.romanoindustries.creamsoda.helpers.textChanges
import com.romanoindustries.creamsoda.helpers.trimmedText
import com.romanoindustries.creamsoda.newcategory.*
import com.squareup.picasso.Picasso
import io.reactivex.rxjava3.disposables.CompositeDisposable
import kotlinx.android.synthetic.main.activity_edit_menu_item.toolbar
import kotlinx.android.synthetic.main.activity_edit_menu_item_inner.card_view_tag_1
import kotlinx.android.synthetic.main.activity_edit_menu_item_inner.card_view_tag_2
import kotlinx.android.synthetic.main.activity_edit_menu_item_inner.card_view_tag_3
import kotlinx.android.synthetic.main.activity_edit_menu_item_inner.edit_text_description
import kotlinx.android.synthetic.main.activity_edit_menu_item_inner.edit_text_ingredients
import kotlinx.android.synthetic.main.activity_edit_menu_item_inner.edit_text_name
import kotlinx.android.synthetic.main.activity_edit_menu_item_inner.edit_text_price
import kotlinx.android.synthetic.main.activity_edit_menu_item_inner.edit_text_tags
import kotlinx.android.synthetic.main.activity_edit_menu_item_inner.edit_text_weight
import kotlinx.android.synthetic.main.activity_edit_menu_item_inner.image_button_add_photo
import kotlinx.android.synthetic.main.activity_edit_menu_item_inner.image_view_preview
import kotlinx.android.synthetic.main.activity_edit_menu_item_inner.progress_bar_save
import kotlinx.android.synthetic.main.activity_edit_menu_item_inner.progress_bar_upload
import kotlinx.android.synthetic.main.activity_edit_menu_item_inner.text_input_description
import kotlinx.android.synthetic.main.activity_edit_menu_item_inner.text_input_ingredients
import kotlinx.android.synthetic.main.activity_edit_menu_item_inner.text_input_name
import kotlinx.android.synthetic.main.activity_edit_menu_item_inner.text_input_price
import kotlinx.android.synthetic.main.activity_edit_menu_item_inner.text_input_weight
import kotlinx.android.synthetic.main.activity_edit_menu_item_inner.text_view_tag_1
import kotlinx.android.synthetic.main.activity_edit_menu_item_inner.text_view_tag_2
import kotlinx.android.synthetic.main.activity_edit_menu_item_inner.text_view_tag_3
import java.lang.NumberFormatException

const val MENU_ITEM_OBJECT_KEY = "menu_item"

class EditMenuItemActivity : AppCompatActivity() {

    private lateinit var viewModel: EditMenuItemViewModel
    private val compositeDisposable = CompositeDisposable()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_menu_item)
        setupListeners()
        parseIntent()
        observeViewModel()
    }

    override fun onDestroy() {
        super.onDestroy()
        compositeDisposable.dispose()
    }

    override fun onBackPressed() {
        super.onBackPressed()
        viewModel.uploadedImageUrl.removeObservers(this)
        viewModel.cancelImageUpload()
        viewModel.deleteCurrentImage()
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

    private fun onSavePressed() {
        if (isInputCorrect()) {
            loadInputToViewModel()
        }
    }

    private fun loadInputToViewModel() {
        val name = edit_text_name.trimmedText()
        val description = edit_text_description.trimmedText()
        val ingredients = edit_text_ingredients.trimmedText()
        val weight = edit_text_weight.trimmedText().toInt()
        val price = edit_text_price.trimmedText().toInt()
        var tagsList = edit_text_tags.trimmedText().split(",")
        tagsList.forEach { it.trim() }
        if (tagsList.size > 3) {
            tagsList = tagsList.subList(0, 3)
        }
        viewModel.saveItem(name, ingredients, description, price, weight, tagsList)
    }

    private fun isInputCorrect(): Boolean {
        var inputCorrect = true
        val name = edit_text_name.trimmedText()
        val description = edit_text_description.trimmedText()
        val ingredients = edit_text_ingredients.trimmedText()
        val weightStr = edit_text_weight.trimmedText()
        val priceStr = edit_text_price.trimmedText()

        if (name.isBlank()) {
            inputCorrect = false
            text_input_name.error = getString(R.string.error_empty_field)
        }
        if (description.isBlank()) {
            inputCorrect = false
            text_input_description.error = getString(R.string.error_empty_field)
        }
        if (ingredients.isBlank()) {
            inputCorrect = false
            text_input_ingredients.error = getString(R.string.error_empty_field)
        }

        try {
            val weight = weightStr.toInt()
            if (weight <= 0) {
                text_input_weight.error = getString(R.string.error_incorrect_format)
                inputCorrect = false
            }
        } catch (e: NumberFormatException) {
            text_input_weight.error = getString(R.string.error_incorrect_format)
            inputCorrect = false
        }

        try {
            val price = priceStr.toInt()
            if (price <= 0) {
                text_input_price.error = getString(R.string.error_incorrect_format)
                inputCorrect = false
            }
        } catch (e: NumberFormatException) {
            text_input_price.error = getString(R.string.error_incorrect_format)
            inputCorrect = false
        }

        return inputCorrect
    }

    private fun setupListeners() {
        toolbar.setNavigationOnClickListener { onBackPressed() }
        toolbar.setOnMenuItemClickListener {menuItem ->
            when (menuItem.itemId) {
                R.id.mnu_item_save -> {
                    onSavePressed()
                    true
                }
                R.id.mnu_item_delete -> {
                    onDeletePressed()
                    true
                }
                else -> false
            }
        }

        hideAllTags()
        edit_text_tags.textChanges().subscribe {text ->
            val tagsList = text.trim().split(",")
            tagsList.forEach { it.trim() }
            hideAllTags()
            for (i in tagsList.indices) {
                when (i) {
                    0 -> {
                        card_view_tag_3.visibility = View.VISIBLE
                        text_view_tag_3.visibility = View.VISIBLE
                        text_view_tag_3.text = tagsList[i]
                    }
                    1 -> {
                        card_view_tag_2.visibility = View.VISIBLE
                        text_view_tag_2.visibility = View.VISIBLE
                        text_view_tag_2.text = tagsList[i]
                    }
                    2 -> {
                        card_view_tag_1.visibility = View.VISIBLE
                        text_view_tag_1.visibility = View.VISIBLE
                        text_view_tag_1.text = tagsList[i]
                    }
                }
            }
        }.also { compositeDisposable.add(it) }

        edit_text_name.textChanges()
            .subscribe { text_input_name.error = null }.also { compositeDisposable.add(it) }
        edit_text_description.textChanges()
            .subscribe { text_input_description.error = null }.also { compositeDisposable.add(it) }
        edit_text_ingredients.textChanges()
            .subscribe { text_input_ingredients.error = null }.also { compositeDisposable.add(it) }
        edit_text_weight.textChanges()
            .subscribe { text_input_weight.error = null }.also { compositeDisposable.add(it) }
        edit_text_price.textChanges()
            .subscribe { text_input_price.error = null }.also { compositeDisposable.add(it) }
    }

    private fun onDeletePressed() {
        viewModel.deleteItem()
    }

    private fun hideAllTags() {
        card_view_tag_1.visibility = View.INVISIBLE
        card_view_tag_2.visibility = View.INVISIBLE
        card_view_tag_3.visibility = View.INVISIBLE
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
        viewModel.uploadedImageUrl.observe(this, Observer { imageUrl ->
            Picasso.get().load(imageUrl).into(image_view_preview)
        })

        viewModel.state.observe(this, Observer {state ->
            when (state) {
                STATE_DEFAULT -> progress_bar_save.visibility = View.INVISIBLE
                STATE_SAVING -> progress_bar_save.visibility = View.VISIBLE
                STATE_SAVED -> finish()
            }
        })

        viewModel.isImageUploading.observe(this, Observer { uploading ->
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

        viewModel.imageUploadProgress.observe(this, Observer { progress ->
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






















