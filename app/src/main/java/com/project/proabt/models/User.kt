package com.project.proabt.models

data class User(
    val name: String,
    val upper_name:String,
    val imageUrl: String,
    val thumbImage: String,
    val uid: String,
    val deviceToken: String,
    val status: String,
    val onlineStatus: String,
    var creator:Boolean=false,
    val rating: Float = 0F,
    val skills: List<String>
) {
    constructor() : this("","", "", "", "", "", "", "",false, 0F, listOf())
    constructor(
        name: String,
        upper_name: String=name.toUpperCase(),
        imageUrl: String,
        thumbImage: String,
        uid: String,
        deviceToken: String,
        rating: Float,
        skills:List<String>
    ) : this(
        name,
        upper_name,
        imageUrl,
        thumbImage,
        uid,
        deviceToken,
        "Hey there I am using whatsapp",
        "",
        rating=rating,
        skills = skills
    )
}
