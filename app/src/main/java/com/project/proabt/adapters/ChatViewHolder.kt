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
    fun bind(item: Inbox, onClick: (name: String, photo: String, id: String) -> Unit)=
        with(itemView){
            val countTv=findViewById<TextView>(R.id.countTv)
            val timeTv=findViewById<TextView>(R.id.timeTv)
            val titleTv=findViewById<TextView>(R.id.titleTv)
            val subtitleTv=findViewById<TextView>(R.id.subtitleTv)
            val userImgView=findViewById<ImageView>(R.id.userImgView1)
            val subtitleImage=findViewById<ImageView>(R.id.subtitleImage)
            subtitleImage.isVisible=true
            subtitleImage.setImageResource(R.drawable.ic_pdf_inbox)
            countTv.isVisible=item.count>0
            countTv.text=item.count.toString()
            timeTv.text=item.time.formatAsListItem(context)
            if(item.msg==""){
                timeTv.isVisible=false
                subtitleImage.isVisible=false
            }
            if(item.type=="IMAGE"){
                subtitleImage.setImageResource(R.drawable.ic_image_24)
                subtitleTv.text="Image"
            }else if(item.type=="DOC"){
                subtitleImage.setImageResource(R.drawable.ic_pdf_inbox)
                subtitleTv.text="Doc"
            }else if(item.type=="AUDIO"){
                subtitleImage.setImageResource(R.drawable.ic_headphones_24)
                subtitleImage.setColorFilter(R.color.black)
                subtitleTv.text="Audio"
            }else if(item.type=="VIDEO"){
                subtitleImage.setImageResource(R.drawable.ic_videocam)
                subtitleTv.text="Video"
            }
            else{
                subtitleImage.isVisible=false
                subtitleTv.text=item.msg
            }
            titleTv.text=item.name
            Log.d("Image", "Item Image from viewholder:${item.image}")
            Picasso.get()
                .load(item.image)
                .placeholder(R.drawable.defaultavatar)
                .error(R.drawable.defaultavatar)
                .into(userImgView)
            setOnClickListener {
                onClick.invoke(item.name, item.image, item.from)
            }
        }
}

