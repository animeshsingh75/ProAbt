package com.project.proabt.models

import java.util.*


data class Inbox(
    val msg: String,
    var from: String,
    var to:String,
    var name: String,
    var upper_name:String,
    var image: String,
    val time: Date = Date(),
    var count: Int = 0,
    val type: String="TEXT",
    var invertedDate:Long
) {
    constructor() : this("", "","", "", "", "",Date(), 0,"",0L)
}
