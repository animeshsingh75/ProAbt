package com.project.proabt.models

import android.content.Context
import com.project.proabt.utils.formatAsHeader
import java.util.*

interface ChatEvent {
    val sentAt: Date
}

data class Message(
    val msg: String,
    val senderId: String,
    val msgId: String,
    val imageUrl: String,
    val senderName:String,
    val type: String,
    val angle:Float=0F,
    val fileName:String="",
    val duration:String="",
    val status: Int = 1,
    val liked: Boolean = false,
    override val sentAt: Date = Date()
) : ChatEvent {
    constructor() : this("", "", "", "","","", 0F,"","",1, false,Date())
}

data class DateHeader(
    override val sentAt: Date = Date(),
    val context:Context
):ChatEvent{
    val date:String=sentAt.formatAsHeader(context)
}