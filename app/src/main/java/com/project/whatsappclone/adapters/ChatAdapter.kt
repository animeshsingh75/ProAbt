
package com.project.whatsappclone.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.project.whatsappclone.R
import com.project.whatsappclone.models.ChatEvent
import com.project.whatsappclone.models.DateHeader
import com.project.whatsappclone.models.Message
import com.project.whatsappclone.utils.formatAsTime
import com.google.android.material.card.MaterialCardView

class ChatAdapter(private val list: MutableList<ChatEvent>, private val mCurrentUid: String) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    var highFiveClick: ((id: String, status: Boolean) -> Unit)? = null
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflate = { layout: Int ->
            LayoutInflater.from(parent.context).inflate(layout, parent, false)
        }
        return when (viewType) {
            TEXT_MESSAGE_RECEIVED -> {
                MessageViewHolder(inflate(R.layout.list_item_chat_recv_message))
            }
            TEXT_MESSAGE_SENT -> {
                MessageViewHolder(inflate(R.layout.list_item_chat_sent_message))
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
                val content = holder.itemView.findViewById<TextView>(R.id.content)
                val time = holder.itemView.findViewById<TextView>(R.id.time)
                content.text = item.msg
                time.text = item.sentAt.formatAsTime()
                when (getItemViewType(position)) {
                    TEXT_MESSAGE_RECEIVED -> {
                        val messageCardView = holder.itemView.findViewById<MaterialCardView>(R.id.messageCardView)
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
                    TEXT_MESSAGE_SENT
                } else {
                    TEXT_MESSAGE_RECEIVED
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