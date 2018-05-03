package me.cpele.fleabrainer.ui

import android.support.v7.widget.RecyclerView
import android.text.format.DateUtils
import android.view.View
import kotlinx.android.synthetic.main.view_detail_item.view.*
import me.cpele.fleabrainer.R
import me.cpele.fleabrainer.domain.DatapointBo
import me.cpele.fleabrainer.domain.formatHoursAsDuration

private const val LEFT: Int = 0

class DetailViewHolder(itemView: View?) : RecyclerView.ViewHolder(itemView) {

    fun bind(item: DatapointBo?) {

        itemView.detail_item_title.text = item?.updatedAt?.time?.let {
            DateUtils.formatDateTime(itemView.context, it, DateUtils.FORMAT_ABBREV_ALL)
        }

        val drawableSync = if (item?.pending == true) R.drawable.ic_sync_disabled_black_24dp else 0
        itemView.detail_item_title.setCompoundDrawablesWithIntrinsicBounds(0, 0, drawableSync, 0)

        itemView.detail_item_desc.text = item?.comment

        val formattedValue = item?.datapointValue?.let { formatHoursAsDuration(it, displaySign = false) }
        itemView.detail_item_value.text = formattedValue
    }
}
