package com.example.whatsappclone.adapters

import android.view.View
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.example.whatsappclone.R
import com.google.android.material.imageview.ShapeableImageView
import com.squareup.picasso.Picasso

class UserViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    fun bind(user: com.example.whatsappclone.models.User) = with(itemView) {
        val countTv = itemView.findViewById<TextView>(R.id.countTv)
        val timeTv = itemView.findViewById<TextView>(R.id.timeTv)
        val titleTv = itemView.findViewById<TextView>(R.id.titleTv)
        val subtitleTv = itemView.findViewById<TextView>(R.id.subtitleTv)
        val userImgView = itemView.findViewById<ShapeableImageView>(R.id.userImgView)
        countTv.isVisible = false
        timeTv.isVisible = false
        titleTv.text = user.name
        subtitleTv.text = user.status
        Picasso.get()
            .load(user.thumbImage)
            .placeholder(R.drawable.defaultavatar)
            .error(R.drawable.defaultavatar)
            .into(userImgView)
    }

}