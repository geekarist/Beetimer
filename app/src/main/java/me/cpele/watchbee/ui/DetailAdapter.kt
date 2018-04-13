package me.cpele.watchbee.ui

import android.support.v7.recyclerview.extensions.ListAdapter
import android.support.v7.util.DiffUtil
import android.view.LayoutInflater
import android.view.ViewGroup
import me.cpele.watchbee.R
import me.cpele.watchbee.domain.DatapointBo

class DetailAdapter : ListAdapter<DatapointBo, DetailViewHolder>(DetailAdapter.DiffCalback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DetailViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val view = inflater.inflate(R.layout.view_detail_item, parent, false)
        return DetailViewHolder(view)
    }

    override fun onBindViewHolder(holder: DetailViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    private class DiffCalback : DiffUtil.ItemCallback<DatapointBo>() {

        override fun areItemsTheSame(oldItem: DatapointBo?, newItem: DatapointBo?): Boolean {
            return oldItem?.id == newItem?.id
        }

        override fun areContentsTheSame(oldItem: DatapointBo?, newItem: DatapointBo?): Boolean {
            return oldItem == newItem
        }

    }
}
