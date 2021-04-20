package com.project.proabt.adapters

import android.content.Context
import android.content.Intent
import android.media.MediaPlayer
import android.net.Uri
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.SeekBar
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.card.MaterialCardView
import com.project.proabt.*
import com.project.proabt.attachment_types.ViewImageActivity
import com.project.proabt.models.ChatEvent
import com.project.proabt.models.DateHeader
import com.project.proabt.models.Message
import com.project.proabt.utils.formatAsTime
import com.squareup.picasso.Picasso
import kotlinx.coroutines.*


class ChatAdapter(private val list: MutableList<ChatEvent>, private val mCurrentUid: String) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    var highFiveClick: ((id: String, status: Boolean) -> Unit)? = null
    var wasPlaying = false
    var isPaused = false
    lateinit var context: Context
    lateinit var updateSeekbar: Job
    lateinit var holderList:HashMap<Int,RecyclerView.ViewHolder>
    var previousPosition: Int = 0
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflate = { layout: Int ->
            LayoutInflater.from(parent.context).inflate(layout, parent, false)
        }
        return when (viewType) {
            AUDIO_MESSAGE_RECEIVED -> {
                MessageViewHolder(inflate(R.layout.list_item_chat_recv_audio))
            }
            AUDIO_MESSAGE_SENT -> {
                MessageViewHolder(inflate(R.layout.list_item_chat_sent_audio))
            }
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
                        namePdf.text = item.fileName
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
                        namePdf.text = item.fileName
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
                        content.setOnClickListener { v ->
                            Log.d("Clicked", "Clicked")
                            val intent = Intent(v.context, ViewImageActivity::class.java)
                            intent.putExtra("angle", item.angle)
                            intent.putExtra(NAME, item.senderName)
                            intent.putExtra(IMAGE, item.imageUrl)
                            intent.putExtra("MSG", item.msg)
                            v.context.startActivity(intent)
                        }
                        Picasso.get()
                            .load(item.msg)
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
                            .placeholder(R.drawable.defaultavatar)
                            .error(R.drawable.defaultavatar)
                            .into(content)
                        val highFiveImg = holder.itemView.findViewById<ImageView>(R.id.highFiveImg)
                        highFiveImg.apply {
                            isVisible = item.liked
                        }
                    }
                    AUDIO_MESSAGE_RECEIVED -> {
                        val messageCardView =
                            holder.itemView.findViewById<MaterialCardView>(R.id.messageCardView)
                        val highFiveImg = holder.itemView.findViewById<ImageView>(R.id.highFiveImg)
                        val seekBar = holder.itemView.findViewById<SeekBar>(R.id.seekBar)
                        val durationTv = holder.itemView.findViewById<TextView>(R.id.durationTv)
                        durationTv.text = item.duration
                        val playbtn = holder.itemView.findViewById<ImageView>(R.id.playbtn)
                        context = holder.itemView.context
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
                        playbtn.setOnClickListener {
                            if(previousPosition!=position &&previousPosition!=0){
                                Log.d("Status1","Changed Position")
                                val previousHolder=holderList[previousPosition]
                                val view=previousHolder!!.itemView
                                val previousSeekBar=view.findViewById<SeekBar>(R.id.seekBar)
                                val previousPlaybtn=view.findViewById<ImageView>(R.id.playbtn)
                                previousPlaybtn.setImageDrawable(
                                    ContextCompat.getDrawable(
                                        context,
                                        R.drawable.ic_play
                                    )
                                )
                                previousSeekBar.progress=0
                                Log.d("Status1", item.msg)
                                clearMediaPlayer()
                                mediaPlayer= MediaPlayer()
                                playbtn.setImageDrawable(
                                    ContextCompat.getDrawable(
                                        context,
                                        R.drawable.ic_pause
                                    )
                                )
                                mediaPlayer!!.setDataSource(item.msg)
                                mediaPlayer!!.prepare()
                                mediaPlayer!!.start()
                                mediaPlayer!!.isLooping = false
                                updateSeekbar.cancel()
                                updateSeekbar.start()
                                seekBar.max = mediaPlayer!!.duration
                                previousPosition=position
                                holderList.clear()
                                holderList[position]=holder
                            }else{
                                if (mediaPlayer != null && !isPaused) {
                                    wasPlaying = true
                                    mediaPlayer!!.pause()
                                    isPaused = true
                                    playbtn.setImageDrawable(
                                        ContextCompat.getDrawable(
                                            context,
                                            R.drawable.ic_play
                                        )
                                    )
                                    Log.d("Status1", "Paused")
                                } else if (!wasPlaying) {
                                    Log.d("Status1", "Started")
                                    if (mediaPlayer == null) {
                                        mediaPlayer = MediaPlayer()
                                    }
                                    playbtn.setImageDrawable(
                                        ContextCompat.getDrawable(
                                            context,
                                            R.drawable.ic_pause
                                        )
                                    )
                                    mediaPlayer!!.setDataSource(item.msg)
                                    mediaPlayer!!.prepare()
                                    mediaPlayer!!.isLooping = false
                                    seekBar.max = mediaPlayer!!.duration
                                    previousPosition=position
                                    holderList= HashMap()
                                    holderList[previousPosition] = holder
                                    Log.d("Status1",previousPosition.toString())
                                    mediaPlayer!!.start()
                                } else {
                                    wasPlaying = true
                                    mediaPlayer!!.start()
                                    isPaused = false
                                    playbtn.setImageDrawable(
                                        ContextCompat.getDrawable(
                                            context,
                                            R.drawable.ic_pause
                                        )
                                    )
                                    Log.d("Status1", "Played")
                                }
                            }
                            updateSeekbar = GlobalScope.launch(Dispatchers.IO) {
                                var currentPosition = mediaPlayer!!.currentPosition
                                val total = mediaPlayer!!.duration
                                while (mediaPlayer != null && !isPaused && currentPosition < total) {
                                    Log.d("Status1", "Updated")
                                    currentPosition =
                                        mediaPlayer!!.currentPosition
                                    withContext(Dispatchers.Main) {
                                        seekBar.progress = currentPosition
                                    }
                                }
                                suspend {
                                    delay((mediaPlayer!!.duration / 100).toLong())
                                }

                            }
                        }

                        seekBar.setOnSeekBarChangeListener(object :
                            SeekBar.OnSeekBarChangeListener {
                            override fun onStartTrackingTouch(seekBar: SeekBar) {
                            }

                            override fun onProgressChanged(
                                seekBar: SeekBar,
                                progress: Int,
                                fromTouch: Boolean
                            ) {
                                if (mediaPlayer != null && progress == mediaPlayer!!.duration && !isPaused) {
                                    Log.d("Status1", "Completed")
                                    clearMediaPlayer()
                                    Log.d("Status1",holderList.toString())
                                    holderList.clear()
                                    Log.d("Status1",holderList.toString())
                                    previousPosition=0
                                    wasPlaying = false
                                    seekBar.progress = 0
                                    playbtn.setImageDrawable(
                                        ContextCompat.getDrawable(
                                            context,
                                            R.drawable.ic_play
                                        )
                                    )
                                }
                            }

                            override fun onStopTrackingTouch(seekBar: SeekBar) {
                                if (mediaPlayer != null && mediaPlayer!!.isPlaying) {
                                    mediaPlayer!!.seekTo(seekBar.progress)
                                }
                            }
                        })
                    }
                    AUDIO_MESSAGE_SENT -> {
                        val highFiveImg = holder.itemView.findViewById<ImageView>(R.id.highFiveImg)
                        highFiveImg.apply {
                            isVisible = item.liked
                        }
                        val seekBar = holder.itemView.findViewById<SeekBar>(R.id.seekBar)
                        val durationTv = holder.itemView.findViewById<TextView>(R.id.durationTv)
                        durationTv.text = item.duration
                        val playbtn = holder.itemView.findViewById<ImageView>(R.id.playbtn)
                        context = holder.itemView.context
                        playbtn.setOnClickListener {
                            if(previousPosition!=position &&previousPosition!=0){
                                Log.d("Status1","Changed Position")
                                val previousHolder=holderList[previousPosition]
                                val view=previousHolder!!.itemView
                                val previousSeekBar=view.findViewById<SeekBar>(R.id.seekBar)
                                val previousPlaybtn=view.findViewById<ImageView>(R.id.playbtn)
                                previousPlaybtn.setImageDrawable(
                                    ContextCompat.getDrawable(
                                        context,
                                        R.drawable.ic_play
                                    )
                                )
                                previousSeekBar.progress=0
                                Log.d("Status1", item.msg)
                                clearMediaPlayer()
                                mediaPlayer= MediaPlayer()
                                playbtn.setImageDrawable(
                                    ContextCompat.getDrawable(
                                        context,
                                        R.drawable.ic_pause
                                    )
                                )
                                mediaPlayer!!.setDataSource(item.msg)
                                mediaPlayer!!.prepare()
                                mediaPlayer!!.start()
                                mediaPlayer!!.isLooping = false
                                updateSeekbar.cancel()
                                updateSeekbar.start()
                                seekBar.max = mediaPlayer!!.duration
                                previousPosition=position
                                holderList.clear()
                                holderList[position]=holder
                            }else{
                                if (mediaPlayer != null && !isPaused) {
                                    wasPlaying = true
                                    mediaPlayer!!.pause()
                                    isPaused = true
                                    playbtn.setImageDrawable(
                                        ContextCompat.getDrawable(
                                            context,
                                            R.drawable.ic_play
                                        )
                                    )
                                    Log.d("Status1", "Paused")
                                } else if (!wasPlaying) {
                                    Log.d("Status1", "Started")
                                    if (mediaPlayer == null) {
                                        mediaPlayer = MediaPlayer()
                                    }
                                    playbtn.setImageDrawable(
                                        ContextCompat.getDrawable(
                                            context,
                                            R.drawable.ic_pause
                                        )
                                    )
                                    mediaPlayer!!.setDataSource(item.msg)
                                    mediaPlayer!!.prepare()
                                    mediaPlayer!!.isLooping = false
                                    seekBar.max = mediaPlayer!!.duration
                                    previousPosition=position
                                    holderList= HashMap()
                                    holderList[previousPosition] = holder
                                    Log.d("Status1",previousPosition.toString())
                                    mediaPlayer!!.start()
                                } else {
                                    wasPlaying = true
                                    mediaPlayer!!.start()
                                    isPaused = false
                                    playbtn.setImageDrawable(
                                        ContextCompat.getDrawable(
                                            context,
                                            R.drawable.ic_pause
                                        )
                                    )
                                    Log.d("Status1", "Played")
                                }
                            }
                            updateSeekbar = GlobalScope.launch(Dispatchers.IO) {
                                var currentPosition = mediaPlayer!!.currentPosition
                                val total = mediaPlayer!!.duration
                                while (mediaPlayer != null && !isPaused && currentPosition < total) {
                                    Log.d("Status1", "Updated")
                                    currentPosition =
                                        mediaPlayer!!.currentPosition
                                    withContext(Dispatchers.Main) {
                                        seekBar.progress = currentPosition
                                    }
                                }
                                suspend {
                                    delay((mediaPlayer!!.duration / 100).toLong())
                                }

                            }
                        }

                        seekBar.setOnSeekBarChangeListener(object :
                            SeekBar.OnSeekBarChangeListener {
                            override fun onStartTrackingTouch(seekBar: SeekBar) {
                            }

                            override fun onProgressChanged(
                                seekBar: SeekBar,
                                progress: Int,
                                fromTouch: Boolean
                            ) {
                                if (mediaPlayer != null && progress == mediaPlayer!!.duration && !isPaused) {
                                    Log.d("Status1", "Completed")
                                    clearMediaPlayer()
                                    Log.d("Status1",holderList.toString())
                                    holderList.clear()
                                    Log.d("Status1",holderList.toString())
                                    previousPosition=0
                                    wasPlaying = false
                                    seekBar.progress = 0
                                    playbtn.setImageDrawable(
                                        ContextCompat.getDrawable(
                                            context,
                                            R.drawable.ic_play
                                        )
                                    )
                                }
                            }

                            override fun onStopTrackingTouch(seekBar: SeekBar) {
                                if (mediaPlayer != null && mediaPlayer!!.isPlaying) {
                                    mediaPlayer!!.seekTo(seekBar.progress)
                                }
                            }
                        })
                    }

                }
            }
        }
    }
    private fun clearMediaPlayer() {
        mediaPlayer!!.stop()
        mediaPlayer!!.release()
        mediaPlayer = null
    }

    override fun getItemViewType(position: Int): Int {
        return when (val event = list[position]) {
            is Message -> {
                if (event.senderId == mCurrentUid) {
                    if (event.type == "IMAGE") {
                        IMAGE_MESSAGE_SENT
                    } else if (event.type == "DOC") {
                        PDF_MESSAGE_SENT
                    } else if (event.type == "AUDIO") {
                        AUDIO_MESSAGE_SENT
                    } else {
                        TEXT_MESSAGE_SENT
                    }
                } else {
                    if (event.type == "IMAGE") {
                        IMAGE_MESSAGE_RECEIVED
                    } else if (event.type == "DOC") {
                        PDF_MESSAGE_RECEIVED
                    } else if (event.type == "AUDIO") {
                        AUDIO_MESSAGE_RECEIVED
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
        private const val AUDIO_MESSAGE_SENT = 7
        private const val AUDIO_MESSAGE_RECEIVED = 8
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
