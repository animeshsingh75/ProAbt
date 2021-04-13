package com.project.proabt.models

data class FriendRating(
    val sentAt:Long,
    val indiRating:Int,
    val imageUrl :String,
    val name:String
){
    constructor():this(0,0,"","")
}