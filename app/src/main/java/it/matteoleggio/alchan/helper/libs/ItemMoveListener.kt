package it.matteoleggio.alchan.helper.libs

import it.matteoleggio.alchan.ui.base.BaseViewHolder

interface ItemMoveListener {
    fun onRowMoved(fromPosition: Int, toPosition: Int)
    fun onRowSelected(itemViewHolder: BaseViewHolder)
    fun onRowClear(itemViewHolder: BaseViewHolder)
}