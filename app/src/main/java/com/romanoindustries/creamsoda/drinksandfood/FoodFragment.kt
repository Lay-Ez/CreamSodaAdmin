package com.romanoindustries.creamsoda.drinksandfood

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.recyclerview.widget.GridLayoutManager
import com.google.gson.Gson
import com.romanoindustries.creamsoda.MyApp
import com.romanoindustries.creamsoda.R
import com.romanoindustries.creamsoda.drinksandfood.common.CategoriesViewModel
import com.romanoindustries.creamsoda.drinksandfood.common.CategoryAdapter
import com.romanoindustries.creamsoda.editcategory.CATEGORY_OBJECT_KEY
import com.romanoindustries.creamsoda.editcategory.EditCategoryActivity
import com.romanoindustries.creamsoda.newcategory.CATEGORY_FOOD
import com.romanoindustries.creamsoda.newcategory.CATEGORY_TYPE_KEY
import com.romanoindustries.creamsoda.newcategory.NewCategoryActivity
import kotlinx.android.synthetic.main.fragment_food.*

class FoodFragment : Fragment(), CategoryAdapter.CategoryClickListener {
    private val TAG = "FoodFragment"

    private lateinit var viewModel: CategoriesViewModel
    private lateinit var adapter: CategoryAdapter
    private lateinit var navController: NavController

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_food, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        navController = Navigation.findNavController(view)
        setupViews()
        setupRecyclerView()
    }

    private fun setupViews() {
        floating_action_button.setOnClickListener { startNewCategoryActivity() }
    }

    private fun startNewCategoryActivity() {
        with(Intent(requireContext(), NewCategoryActivity::class.java)) {
            putExtra(CATEGORY_TYPE_KEY, CATEGORY_FOOD)
            startActivity(this)
        }
    }

    override fun onCategoryEditClicked(itemPosition: Int) {
        val clickedCategory = adapter.menuCategories[itemPosition]
        val categorySerialized = Gson().toJson(clickedCategory)
        with((Intent(requireContext(), EditCategoryActivity::class.java))) {
            putExtra(CATEGORY_TYPE_KEY, CATEGORY_FOOD)
            putExtra(CATEGORY_OBJECT_KEY, categorySerialized)
            startActivity(this)
        }
    }

    override fun onCategoryClicked(itemPosition: Int) {
        navController.navigate(R.id.action_food_to_category)
    }

    private fun setupRecyclerView() {
        adapter = CategoryAdapter(arrayListOf(), this)
        recycler_view.adapter = adapter
        recycler_view.layoutManager = GridLayoutManager(requireContext(), 2)
        viewModel = ViewModelProvider(requireActivity()).get(CategoriesViewModel::class.java)
        (requireActivity().application as MyApp).repositoryComponent.inject(viewModel)
        viewModel.init()
        viewModel.foodCategoriesLiveData.observe(requireActivity(), Observer {list ->
            adapter.updateCategories(list)
        })
    }
}














