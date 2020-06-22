package com.romanoindustries.creamsoda.viewcategory

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.google.gson.Gson
import com.romanoindustries.creamsoda.MyApp
import com.romanoindustries.creamsoda.R
import com.romanoindustries.creamsoda.datamodel.MenuCategory
import com.romanoindustries.creamsoda.datamodel.MenuItem
import com.romanoindustries.creamsoda.editcategory.CATEGORY_OBJECT_KEY
import com.romanoindustries.creamsoda.newcategory.CATEGORY_TYPE_KEY
import kotlinx.android.synthetic.main.fragment_food.*

class ViewMenuItemsFragment : Fragment() {
    private val TAG = "ViewCategoryFragment"

    private lateinit var viewModel: ViewMenuItemsViewModel

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_view_menu_items, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        arguments?.let { bundle ->
            parseBundle(bundle)
        }
    }

    private fun displayMenuCategory(menuCategory: MenuCategory) {
        toolbar.title = menuCategory.name
    }

    private fun parseBundle(bundle: Bundle) {
        val categoryType = bundle.getString(CATEGORY_TYPE_KEY)
        val passedCategory = Gson()
            .fromJson(bundle.getString(CATEGORY_OBJECT_KEY), MenuCategory::class.java)
        val repositoryComponent = (requireActivity().application as MyApp).repositoryComponent

        if (categoryType != null) {
            viewModel = ViewModelProvider(requireActivity()).get(ViewMenuItemsViewModel::class.java)
            viewModel.setupMenuItems(categoryType, passedCategory, repositoryComponent)
            viewModel.menuItems.observe(requireActivity(), Observer {
                displayMenuItems(it)
            })
        }

        displayMenuCategory(passedCategory)
    }

    private fun displayMenuItems(menuItems: List<MenuItem>) {

    }
}














