package com.project.proabt.adapters

import android.content.Intent
import android.net.Uri
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.card.MaterialCardView
import com.project.proabt.*
import com.project.proabt.models.ChatEvent
import com.project.proabt.models.DateHeader
import com.project.proabt.models.Message
import com.project.proabt.utils.formatAsTime
import com.squareup.picasso.Picasso


class ChatAdapter(private val list: MutableList<ChatEvent>, private val mCurrentUid: String) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    var highFiveClick: ((id: String, status: Boolean) -> Unit)? = null
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflate = { layout: Int ->
            LayoutInflater.from(parent.context).inflate(layout, parent, false)
        }
        return when (viewType) {
            PDF_MESSAGE_RECEIVED -> {
                MessageViewHolder(inflate(R.layout.list_item_chat_recv_doc))
            }
            PDF_MESSAGE_SENT -> {
                MessageViewHolder(inflate(R.layout.list_item_chat_send_doc))
            }
            TEXT_MESSAGE_RECEIVED -> {
                MessageViewHolder(inflate(R.layout.list_item_chat_recv_message))
            }
            TEXT_MESSAGE_SENT -> {
                MessageViewHolder(inflate(R.layout.list_item_chat_sent_message))
            }
            IMAGE_MESSAGE_RECEIVED -> {
                MessageViewHolder(inflate(R.layout.list_item_chat_recv_image))
            }
            IMAGE_MESSAGE_SENT -> {
                MessageViewHolder(inflate(R.layout.list_item_chat_sent_image))
            }
            DATE_HEADER -> {
                DateViewHolder(inflate(R.layout.list_item_date_header))
            }
            else -> MessageViewHolder(inflate(R.layout.list_item_chat_recv_message))

        }
    }
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (val item = list[position]) {
            is DateHeader -> {
                val textView = holder.itemView.findViewById<TextView>(R.id.textView)
                textView.text = item.date
            }
            is Message -> {
                val time = holder.itemView.findViewById<TextView>(R.id.time)
                time.text = item.sentAt.formatAsTime()
                when (getItemViewType(position)) {
                    TEXT_MESSAGE_RECEIVED -> {
                        val messageCardView =
                            holder.itemView.findViewById<MaterialCardView>(R.id.messageCardView)
                        val content = holder.itemView.findViewById<TextView>(R.id.content)
                        content.text = item.msg
                        val highFiveImg = holder.itemView.findViewById<ImageView>(R.id.highFiveImg)
                        messageCardView.setOnClickListener(object :
                            DoubleClickListener() {
                            override fun onDoubleClick(v: View?) {
                                highFiveClick?.invoke(item.msgId, !item.liked)
                            }
                        })
                        highFiveImg.apply {
                            isVisible = position == itemCount - 1 || item.liked
                            isSelected = item.liked
                            setOnClickListener {
                                highFiveClick?.invoke(item.msgId, !isSelected)
                            }
                        }
                    }
                    TEXT_MESSAGE_SENT -> {
                        val content = holder.itemView.findViewById<TextView>(R.id.content)
                        content.text = item.msg
                        val highFiveImg = holder.itemView.findViewById<ImageView>(R.id.highFiveImg)
                        highFiveImg.apply {
                            isVisible = item.liked
                        }
                    }
                    PDF_MESSAGE_RECEIVED -> {
                        val content = holder.itemView.findViewById<ImageView>(R.id.content)
                        val content_container =
                            holder.itemView.findViewById<RelativeLayout>(R.id.content_container)
                        content.setOnClickListener { v ->
                            Log.d("Clicked", "Clicked")
                            val intent = Intent(Intent.ACTION_VIEW)
                            intent.setDataAndType(Uri.parse(item.msg), "application/pdf")
                            intent.flags = Intent.FLAG_ACTIVITY_NO_HISTORY
                            v.context.startActivity(intent)
                        }
                        content_container.setOnClickListener { v ->
                            Log.d("Clicked", "Clicked")
                            val intent = Intent(Intent.ACTION_VIEW)
                            intent.setDataAndType(Uri.parse(item.msg), "application/pdf")
                            intent.flags = Intent.FLAG_ACTIVITY_NO_HISTORY
                            v.context.startActivity(intent)
                        }
                        val messageCardView =
                            holder.itemView.findViewById<MaterialCardView>(R.id.messageCardView)
                        val highFiveImg = holder.itemView.findViewById<ImageView>(R.id.highFiveImg)
                        val namePdf = holder.itemView.findViewById<TextView>(R.id.namePdf)
                        namePdf.text=item.fileName
                        messageCardView.setOnClickListener(object :
                            DoubleClickListener() {
                            override fun onDoubleClick(v: View?) {
                                highFiveClick?.invoke(item.msgId, !item.liked)
                            }
                        })
                        highFiveImg.apply {
                            isVisible = position == itemCount - 1 || item.liked
                            isSelected = item.liked
                            setOnClickListener {
                                highFiveClick?.invoke(item.msgId, !isSelected)
                            }
                        }
                    }
                    PDF_MESSAGE_SENT -> {
                        val content = holder.itemView.findViewById<ImageView>(R.id.content)
                        val content_container =
                            holder.itemView.findViewById<RelativeLayout>(R.id.content_container)
                        content.setOnClickListener { v ->
                            Log.d("Clicked", "Clicked")
                            val intent = Intent(Intent.ACTION_VIEW)
                            intent.setDataAndType(Uri.parse(item.msg), "application/pdf")
                            intent.flags = Intent.FLAG_ACTIVITY_NO_HISTORY
                            v.context.startActivity(intent)
                        }
                        val namePdf = holder.itemView.findViewById<TextView>(R.id.namePdf)
                        namePdf.text=item.fileName
                        content_container.setOnClickListener { v ->
                            Log.d("Clicked", "Clicked")
                            val intent = Intent(Intent.ACTION_VIEW)
                            intent.setDataAndType(Uri.parse(item.msg), "application/pdf")
                            intent.flags = Intent.FLAG_ACTIVITY_NO_HISTORY
                            v.context.startActivity(intent)
                        }
                        val highFiveImg = holder.itemView.findViewById<ImageView>(R.id.highFiveImg)
                        highFiveImg.apply {
                            isVisible = item.liked
                        }
                    }
                    IMAGE_MESSAGE_RECEIVED -> {
                        val content = holder.itemView.findViewById<ImageView>(R.id.content)
                        val angle = item.angle
                        content.setOnClickListener { v ->
                            Log.d("Clicked", "Clicked")
                            val intent = Intent(v.context, ViewImageActivity::class.java)
                            intent.putExtra(UID, item.senderId)
                            intent.putExtra(NAME, item.senderName)
                            intent.putExtra(IMAGE, item.imageUrl)
                            intent.putExtra("MSG", item.msg)
                            v.context.startActivity(intent)
                        }
                        Picasso.get()
                            .load(item.msg)
                            .rotate(angle)
                            .placeholder(R.drawable.defaultavatar)
                            .error(R.drawable.defaultavatar)
                            .into(content)
                        val messageCardView =
                            holder.itemView.findViewById<MaterialCardView>(R.id.messageCardView)
                        val highFiveImg = holder.itemView.findViewById<ImageView>(R.id.highFiveImg)
                        messageCardView.setOnClickListener(object :
                            DoubleClickListener() {
                            override fun onDoubleClick(v: View?) {
                                highFiveClick?.invoke(item.msgId, !item.liked)
                            }
                        })
                        highFiveImg.apply {
                            isVisible = position == itemCount - 1 || item.liked
                            isSelected = item.liked
                            setOnClickListener {
                                highFiveClick?.invoke(item.msgId, !isSelected)
                            }
                        }
                    }
                    IMAGE_MESSAGE_SENT -> {
                        val content = holder.itemView.findViewById<ImageView>(R.id.content)
                        content.setOnClickListener { v ->
                            val intent = Intent(v.context, ViewImageActivity::class.java)
                            intent.putExtra(UID, mCurrentUid)
                            intent.putExtra(NAME, "YOU")
                            intent.putExtra(IMAGE, "IMAGE")
                            intent.putExtra("MSG", item.msg)
                            v.context.startActivity(intent)
                        }
                        Picasso.get()
                            .load(item.msg)
                            .rotate(item.angle)
                            .placeholder(R.drawable.defaultavatar)
                            .error(R.drawable.defaultavatar)
                            .into(content)
                        val highFiveImg = holder.itemView.findViewById<ImageView>(R.id.highFiveImg)
                        highFiveImg.apply {
                            isVisible = item.liked
                        }
                    }
                }
            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        return when (val event = list[position]) {
            is Message -> {
                if (event.senderId == mCurrentUid) {
                    if (event.type == "IMAGE") {
                        IMAGE_MESSAGE_SENT
                    } else if (event.type == "DOC") {
                        PDF_MESSAGE_SENT
                    } else {
                        TEXT_MESSAGE_SENT
                    }
                } else {
                    if (event.type == "IMAGE") {
                        IMAGE_MESSAGE_RECEIVED
                    } else if (event.type == "DOC") {
                        PDF_MESSAGE_RECEIVED
                    } else {
                        TEXT_MESSAGE_RECEIVED
                    }
                }
            }
            is DateHeader -> DATE_HEADER
            else -> UNSUPPORTED
        }
    }

    override fun getItemCount(): Int = list.size

    class DateViewHolder(view: View) : RecyclerView.ViewHolder(view)
    class MessageViewHolder(view: View) : RecyclerView.ViewHolder(view)
    companion object {
        private const val UNSUPPORTED = -1
        private const val TEXT_MESSAGE_RECEIVED = 0
        private const val TEXT_MESSAGE_SENT = 1
        private const val DATE_HEADER = 2
        private const val IMAGE_MESSAGE_SENT = 3
        private const val IMAGE_MESSAGE_RECEIVED = 4
        private const val PDF_MESSAGE_RECEIVED = 5
        private const val PDF_MESSAGE_SENT = 6
    }

    abstract class DoubleClickListener : View.OnClickListener {
        var lastClickTime: Long = 0
        override fun onClick(v: View?) {
            val clickTime = System.currentTimeMillis()
            if (clickTime - lastClickTime < DOUBLE_CLICK_TIME_DELTA) {
                onDoubleClick(v)
                lastClickTime = 0
            }
            lastClickTime = clickTime
        }

        abstract fun onDoubleClick(v: View?)

        companion object {
            private const val DOUBLE_CLICK_TIME_DELTA: Long = 300
        }
    }
}