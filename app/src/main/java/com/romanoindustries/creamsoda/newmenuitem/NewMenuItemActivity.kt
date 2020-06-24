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
import com.romanoindustries.creamsoda.helpers.textChanges
import com.romanoindustries.creamsoda.helpers.trimmedText
import com.romanoindustries.creamsoda.newcategory.*
import com.squareup.picasso.Picasso
import io.reactivex.rxjava3.disposables.CompositeDisposable
import kotlinx.android.synthetic.main.activity_new_menu_item.*
import kotlinx.android.synthetic.main.activity_new_menu_item_inner.*
import java.lang.NumberFormatException

class NewMenuItemActivity : AppCompatActivity() {

    private lateinit var viewModel: NewMenuItemViewModel
    private val compositeDisposable = CompositeDisposable()

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

    override fun onDestroy() {
        super.onDestroy()
        compositeDisposable.dispose()
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

    private fun hideAllTags() {
        card_view_tag_1.visibility = View.INVISIBLE
        card_view_tag_2.visibility = View.INVISIBLE
        card_view_tag_3.visibility = View.INVISIBLE
        text_view_tag_1.visibility = View.INVISIBLE
        text_view_tag_2.visibility = View.INVISIBLE
        text_view_tag_3.visibility = View.INVISIBLE
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
















