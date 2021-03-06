package com.romanoindustries.creamsoda.viewmenuitems

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.gson.Gson
import com.romanoindustries.creamsoda.MyApp
import com.romanoindustries.creamsoda.R
import com.romanoindustries.creamsoda.datamodel.MenuCategory
import com.romanoindustries.creamsoda.datamodel.MenuItem
import com.romanoindustries.creamsoda.editcategory.CATEGORY_OBJECT_KEY
import com.romanoindustries.creamsoda.editmenuitem.EditMenuItemActivity
import com.romanoindustries.creamsoda.editmenuitem.MENU_ITEM_OBJECT_KEY
import com.romanoindustries.creamsoda.newcategory.CATEGORY_FOOD
import com.romanoindustries.creamsoda.newcategory.CATEGORY_TYPE_KEY
import com.romanoindustries.creamsoda.newmenuitem.NewMenuItemActivity
import kotlinx.android.synthetic.main.fragment_view_menu_items.*

class ViewMenuItemsFragment : Fragment(), MenuItemsAdapter.MenuItemClickListener {
    private val TAG = "ViewMenuItemsFragment"

    private lateinit var viewModel: ViewMenuItemsViewModel
    private lateinit var menuCategory: MenuCategory
    private lateinit var categoryType: String
    private lateinit var adapter: MenuItemsAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_view_menu_items, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        arguments?.let { bundle ->
            parseBundle(bundle)
        }
    }

    override fun onMenuItemEditClicked(position: Int) {
        val menuItemToEdit = adapter.menuItems[position]
        with(Intent(requireContext(), EditMenuItemActivity::class.java)) {
            putExtra(CATEGORY_TYPE_KEY, categoryType)
            putExtra(CATEGORY_OBJECT_KEY, Gson().toJson(menuCategory, MenuCategory::class.java))
            putExtra(MENU_ITEM_OBJECT_KEY, Gson().toJson(menuItemToEdit, MenuItem::class.java))
            startActivity(this)
        }
    }

    private fun setupRecyclerView() {
        adapter = MenuItemsAdapter(arrayListOf(), this)
        recycler_view.adapter = adapter
        recycler_view.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
    }

    private fun displayMenuCategory(menuCategory: MenuCategory) {
        toolbar.title = menuCategory.name
        floating_action_button.setOnClickListener { startNewMenuItemActivity() }
    }

    private fun startNewMenuItemActivity() {
        with(Intent(requireContext(), NewMenuItemActivity::class.java)) {
            putExtra(CATEGORY_TYPE_KEY, categoryType)
            putExtra(CATEGORY_OBJECT_KEY, Gson().toJson(menuCategory, MenuCategory::class.java))
            startActivity(this)
        }
    }

    private fun parseBundle(bundle: Bundle) {
        val categoryType = bundle.getString(CATEGORY_TYPE_KEY)
        val passedCategory = Gson()
            .fromJson(bundle.getString(CATEGORY_OBJECT_KEY), MenuCategory::class.java)
        val repositoryComponent = (requireActivity().application as MyApp).repositoryComponent

        if (categoryType != null) {
            this.categoryType = categoryType
            this.menuCategory = passedCategory
            viewModel = ViewModelProvider(requireActivity()).get(ViewMenuItemsViewModel::class.java)
            viewModel.setupMenuItems(categoryType, passedCategory, repositoryComponent)
            viewModel.menuItems.observe(requireActivity(), Observer {
                displayMenuItems(it)
            })
        }

        displayMenuCategory(passedCategory)
    }

    private fun displayMenuItems(menuItems: List<MenuItem>) {
        menuItems.forEach {
            Log.d(TAG, "displayMenuItems: $it")
        }
        adapter.updateMenuItems(menuItems)
    }
}














