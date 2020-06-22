package com.romanoindustries.creamsoda.drinksandfood

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.gson.Gson
import com.romanoindustries.creamsoda.MyApp
import com.romanoindustries.creamsoda.R
import com.romanoindustries.creamsoda.drinksandfood.common.CategoriesViewModel
import com.romanoindustries.creamsoda.drinksandfood.common.CategoryAdapter
import com.romanoindustries.creamsoda.editcategory.CATEGORY_OBJECT_KEY
import com.romanoindustries.creamsoda.editcategory.EditCategoryActivity
import com.romanoindustries.creamsoda.newcategory.CATEGORY_DRINKS
import com.romanoindustries.creamsoda.newcategory.CATEGORY_FOOD
import com.romanoindustries.creamsoda.newcategory.CATEGORY_TYPE_KEY
import com.romanoindustries.creamsoda.newcategory.NewCategoryActivity

class DrinksFragment : Fragment(), CategoryAdapter.CategoryClickListener {

    lateinit var viewModel: CategoriesViewModel
    lateinit var fab: FloatingActionButton
    lateinit var recyclerView: RecyclerView
    lateinit var adapter: CategoryAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val rootView = inflater.inflate(R.layout.fragment_drinks, container, false)
        setupRecyclerView(rootView)
        setupViews(rootView)
        return rootView
    }

    private fun setupViews(view: View) {
        fab = view.findViewById(R.id.floating_action_button)
        fab.setOnClickListener { startNewCategoryActivity() }
    }

    private fun startNewCategoryActivity() {
        with(Intent(requireContext(), NewCategoryActivity::class.java)) {
            putExtra(CATEGORY_TYPE_KEY, CATEGORY_DRINKS)
            startActivity(this)
        }
    }

    override fun onCategoryEditClicked(itemPosition: Int) {
        val clickedCategory = adapter.menuCategories[itemPosition]
        val categorySerialized = Gson().toJson(clickedCategory)
        val intent = (Intent(requireContext(), EditCategoryActivity::class.java))
        intent.putExtra(CATEGORY_TYPE_KEY, CATEGORY_DRINKS)
        intent.putExtra(CATEGORY_OBJECT_KEY, categorySerialized)
        startActivity(intent)
    }

    private fun setupRecyclerView(view: View) {
        recyclerView = view.findViewById(R.id.recycler_view)
        adapter = CategoryAdapter(arrayListOf(), this)
        recyclerView.adapter = adapter
        recyclerView.layoutManager = GridLayoutManager(requireContext(), 2)
        viewModel = ViewModelProvider(requireActivity()).get(CategoriesViewModel::class.java)
        (requireActivity().application as MyApp).repositoryComponent.inject(viewModel)
        viewModel.init()
        viewModel.drinkCategoriesLiveData.observe(requireActivity(), Observer {list ->
            adapter.updateCategories(list)
        })
    }
}