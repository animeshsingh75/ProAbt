package com.project.proabt.models

data class FriendRating(
    val sentAt:Long,
    val indiRating:Int
){
    constructor():this(0,0)
}