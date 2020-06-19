package com.romanoindustries.creamsoda.newcategory

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.romanoindustries.creamsoda.R
import kotlinx.android.synthetic.main.activity_new_category.*

const val CATEGORY_TYPE_KEY = "category_type"
const val CATEGORY_FOOD = "food"
const val CATEGORY_DRINKS = "drinks"

class NewCategoryActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_new_category)
        toolbar.setNavigationOnClickListener { onBackPressed() }
    }

    private fun processIntent(intent: Intent) {
        val category = intent.getStringExtra(CATEGORY_TYPE_KEY)
        if (category == CATEGORY_FOOD) {

        }
    }
}