package com.project.proabt.adapters

import android.util.Log
import android.view.View
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.imageview.ShapeableImageView
import com.project.proabt.R
import com.project.proabt.models.User
import com.squareup.picasso.Picasso

class UserViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    fun bind(user: User, onClick: (name: String, photo: String, id: String) -> Unit) =
        with(itemView) {
            val countTv = itemView.findViewById<TextView>(R.id.countTv)
            val timeTv = itemView.findViewById<TextView>(R.id.timeTv)
            val titleTv = itemView.findViewById<TextView>(R.id.titleTv)
            val subtitleTv = itemView.findViewById<TextView>(R.id.subtitleTv)
            val userImgView = itemView.findViewById<ShapeableImageView>(R.id.userImgView1)
            val ratingLayout = itemView.findViewById<RelativeLayout>(R.id.rating_layout)
            val ratingTv = itemView.findViewById<TextView>(R.id.ratingTv)
            val skillsContainer = itemView.findViewById<LinearLayout>(R.id.skillsContainer)
            val skillLayout1 = itemView.findViewById<RelativeLayout>(R.id.skillLayout1)
            val skillTv1 = itemView.findViewById<TextView>(R.id.skillTv1)
            val skillLayout2 = itemView.findViewById<RelativeLayout>(R.id.skillLayout2)
            val skillTv2 = itemView.findViewById<TextView>(R.id.skillTv2)
            val skillLayout3 = itemView.findViewById<RelativeLayout>(R.id.skillLayout3)
            val skillTv3 = itemView.findViewById<TextView>(R.id.skillTv3)
            countTv.isVisible = false
            timeTv.isVisible = false
            ratingLayout.isVisible=true
            skillsContainer.isVisible=true
            val rating=String.format("%.1f",user.rating)
            Log.d("Skills",user.skills.size.toString())
            skillLayout1.isVisible=true
            skillTv1.text=user.skills[0]
            if(user.skills.size==2){
                skillLayout2.isVisible=true
                skillTv2.text=user.skills[1]
            }else if(user.skills.size==3){
                skillLayout2.isVisible=true
                skillTv2.text=user.skills[1]
                skillLayout3.isVisible=true
                skillTv3.text=user.skills[2]
            }
            ratingTv.text=rating
            titleTv.text = user.name
            subtitleTv.text = user.status
            Picasso.get()
                .load(user.thumbImage)
                .placeholder(R.drawable.defaultavatar)
                .error(R.drawable.defaultavatar)
                .into(userImgView)
            setOnClickListener {
                onClick.invoke(user.name, user.thumbImage, user.uid)
            }
        }

}