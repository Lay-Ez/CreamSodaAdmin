package com.romanoindustries.creamsoda.foodsection

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.romanoindustries.creamsoda.R
import com.romanoindustries.creamsoda.datamodel.MenuCategory
import com.squareup.picasso.Picasso

class CategoryAdapter(var menuCategories: List<MenuCategory>): RecyclerView.Adapter<CategoryAdapter.CategoryViewHolder>() {

    fun updateCategories(menuCategories: List<MenuCategory>) {
        this.menuCategories = menuCategories
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.category_list_item, parent, false)
        return CategoryViewHolder(view)
    }

    override fun getItemCount(): Int = menuCategories.size

    override fun onBindViewHolder(holder: CategoryViewHolder, position: Int) {
        holder.bind(menuCategories[position])
    }

    class CategoryViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {

        private var textViewName: TextView = itemView.findViewById(R.id.text_view_name)
        private var imageView: ImageView = itemView.findViewById(R.id.image_view)

        fun bind(menuCategory: MenuCategory) {
            textViewName.text = menuCategory.name
            Picasso.get().load(menuCategory.imageUrl).into(imageView)
        }

    }
}


















