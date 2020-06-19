package com.romanoindustries.creamsoda.drinksandfood

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.romanoindustries.creamsoda.MyApp
import com.romanoindustries.creamsoda.R
import com.romanoindustries.creamsoda.drinksandfood.common.CategoriesViewModel
import com.romanoindustries.creamsoda.drinksandfood.common.CategoryAdapter

class DrinksFragment : Fragment() {

    lateinit var viewModel: CategoriesViewModel
    lateinit var recyclerView: RecyclerView
    lateinit var adapter: CategoryAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val rootView = inflater.inflate(R.layout.fragment_drinks, container, false)
        setupRecyclerView(rootView)
        return rootView
    }

    private fun setupRecyclerView(view: View) {
        recyclerView = view.findViewById(R.id.recycler_view)
        adapter = CategoryAdapter(arrayListOf())
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