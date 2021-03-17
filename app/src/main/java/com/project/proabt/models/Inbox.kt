package com.project.proabt.models

import java.util.*


data class Inbox(
    val msg: String,
    var from: String,
    var name: String,
    var image: String,
    val time: Date = Date(),
    var count: Int = 0,
    val type: String="TEXT"
) {
    constructor() : this("", "", "", "", Date(), 0,"")
}
