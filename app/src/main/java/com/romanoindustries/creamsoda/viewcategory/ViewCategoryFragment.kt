package com.romanoindustries.creamsoda.viewcategory

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.gson.Gson
import com.romanoindustries.creamsoda.R
import com.romanoindustries.creamsoda.datamodel.MenuCategory
import com.romanoindustries.creamsoda.editcategory.CATEGORY_OBJECT_KEY
import com.romanoindustries.creamsoda.newcategory.CATEGORY_TYPE_KEY

class ViewCategoryFragment : Fragment() {
    private val TAG = "ViewCategoryFragment"

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_view_category, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    }
}