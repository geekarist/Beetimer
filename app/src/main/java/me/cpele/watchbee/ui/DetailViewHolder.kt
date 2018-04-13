package me.cpele.watchbee.ui

import android.support.v7.widget.RecyclerView
import android.view.View
import kotlinx.android.synthetic.main.view_detail_item.view.*
import me.cpele.watchbee.domain.DatapointBo

class DetailViewHolder(itemView: View?) : RecyclerView.ViewHolder(itemView) {

    fun bind(item: DatapointBo?) {
        itemView.detail_item_title.text = item?.updatedAt.toString()
        itemView.detail_item_desc.text = item?.comment
        itemView.detail_item_value.text = item?.datapointValue.toString()
    }
}
