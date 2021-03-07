package com.project.proabt.adapters

import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.project.proabt.R
import com.project.proabt.models.Inbox
import com.project.proabt.utils.formatAsListItem
import com.squareup.picasso.Picasso

class ChatViewHolder(itemView: View): RecyclerView.ViewHolder(itemView){
    fun bind(item: Inbox, onClick:(name:String, photo:String, id:String)->Unit)=
        with(itemView){
            val countTv=findViewById<TextView>(R.id.countTv)
            val timeTv=findViewById<TextView>(R.id.timeTv)
            val titleTv=findViewById<TextView>(R.id.titleTv)
            val subtitleTv=findViewById<TextView>(R.id.subtitleTv)
            val userImgView=findViewById<ImageView>(R.id.userImgView1)
            countTv.isVisible=item.count>0
            countTv.text=item.count.toString()
            timeTv.text=item.time.formatAsListItem(context)
            if(item.msg==""){
                timeTv.isVisible=false
            }
            titleTv.text=item.name
            subtitleTv.text=item.msg
            Log.wtf("Image","Item Image from viewholder:${item.image}")
            Picasso.get()
                .load(item.image)
                .placeholder(R.drawable.defaultavatar)
                .error(R.drawable.defaultavatar)
                .into(userImgView)
            setOnClickListener {
                onClick.invoke(item.name,item.image,item.from)
            }
        }
}

