package com.romanoindustries.creamsoda.viewmenuitems

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.romanoindustries.creamsoda.R
import com.romanoindustries.creamsoda.datamodel.MenuItem
import com.squareup.picasso.Picasso

class MenuItemsAdapter(var menuItems: List<MenuItem>,
                       val clickListener: MenuItemClickListener):
    RecyclerView.Adapter<MenuItemsAdapter.MenuItemViewHolder>() {

    fun updateMenuItems(menuItems: List<MenuItem>) {
        this.menuItems = menuItems
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MenuItemViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.menu_item_list_item, parent, false)
        return MenuItemViewHolder(view, clickListener)
    }

    override fun getItemCount(): Int = menuItems.size

    override fun onBindViewHolder(holder: MenuItemViewHolder, position: Int) {
        holder.bind(menuItems[position])
    }

    class MenuItemViewHolder(itemView: View, val clickListener: MenuItemClickListener):
        RecyclerView.ViewHolder(itemView) {

        private var imageView: ImageView = itemView.findViewById(R.id.image_view)
        private var editBtn: ImageButton = itemView.findViewById(R.id.image_btn_edit)

        private var cardViewTag1: CardView = itemView.findViewById(R.id.card_view_tag_1)
        private var textViewTag1: TextView = itemView.findViewById(R.id.text_view_tag_1)

        private var cardViewTag2: CardView = itemView.findViewById(R.id.card_view_tag_2)
        private var textViewTag2: TextView = itemView.findViewById(R.id.text_view_tag_2)

        private var cardViewTag3: CardView = itemView.findViewById(R.id.card_view_tag_3)
        private var textViewTag3: TextView = itemView.findViewById(R.id.text_view_tag_3)

        private var textViewName: TextView = itemView.findViewById(R.id.text_view_name)
        private var textViewDescription: TextView = itemView.findViewById(R.id.text_view_description)
        private var textViewIngredients: TextView = itemView.findViewById(R.id.text_view_ingredients)
        private var textViewWeight: TextView = itemView.findViewById(R.id.text_view_weight)
        private var textViewWeightLabel: TextView = itemView.findViewById(R.id.text_view_weight_label)
        private var textViewPrice: TextView = itemView.findViewById(R.id.text_view_price)

        fun bind(menuItem: MenuItem) {
            hideAllTags()
            Picasso.get().load(menuItem.imageUrl).into(imageView)
            textViewName.text = menuItem.name
            textViewDescription.text = menuItem.description
            textViewIngredients.text = menuItem.ingredients
            textViewWeight.text = menuItem.weight.toString()
            textViewPrice.text = menuItem.price.toString()
            displayTags(menuItem.tags)
            editBtn.setOnClickListener{ clickListener.onMenuItemEditClicked(adapterPosition) }
        }

        private fun displayTags(tags: List<String>) {
            for (i in tags.indices) {
                when (i) {
                    0 -> {
                        cardViewTag1.visibility = View.VISIBLE
                        textViewTag1.text = tags[i].trim()
                    }
                    1 -> {
                        cardViewTag2.visibility = View.VISIBLE
                        textViewTag2.text = tags[i].trim()
                    }
                    2 -> {
                        cardViewTag3.visibility = View.VISIBLE
                        textViewTag3.text = tags[i].trim()
                    }
                }
            }
        }

        private fun hideAllTags() {
            cardViewTag1.visibility = View.INVISIBLE
            cardViewTag2.visibility = View.INVISIBLE
            cardViewTag3.visibility = View.INVISIBLE
        }
    }

    interface MenuItemClickListener {
        fun onMenuItemEditClicked(position: Int)
    }
}




















