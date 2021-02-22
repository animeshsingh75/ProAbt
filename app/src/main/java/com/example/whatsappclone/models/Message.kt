package com.example.whatsappclone.models

import android.content.Context
import com.example.whatsappclone.utils.formatAsHeader
import java.util.*

interface ChatEvent {
    val sentAt: Date
}

data class Message(
    val msg: String,
    val senderId: String,
    val msgId: String,
    val type: String = "TEXT",
    val status: Int = 1,
    val liked: Boolean = false,
    override val sentAt: Date = Date()
) : ChatEvent {
    constructor() : this("", "", "", "", 1, false, Date())
}

data class DateHeader(
    override val sentAt: Date = Date(),
    val context:Context
):ChatEvent{
    val dte:String=sentAt.formatAsHeader(context)
}