package it.matteoleggio.alchan.ui.base

import androidx.recyclerview.widget.RecyclerView
import it.matteoleggio.alchan.helper.libs.ItemMoveListener

// For drag and drop recycler view
abstract class BaseRecyclerViewAdapter : RecyclerView.Adapter<BaseViewHolder>(), ItemMoveListener