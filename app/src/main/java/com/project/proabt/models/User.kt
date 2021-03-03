
package com.project.proabt.models

data class User(
    val name: String,
    val imageUrl: String,
    val thumbImage: String,
    val uid: String,
    val deviceToken: String,
    val status: String,
    val onlineStatus: String

) {
    constructor() : this("", "", "", "", "", "", "")
    constructor(
        name: String,
        imageUrl: String,
        thumbImage: String,
        uid: String,
        deviceToken: String
    ) : this(
        name,
        imageUrl,
        thumbImage,
        uid,
        deviceToken,
        "Hey there I am using whatsapp",
        ""
    )
}
