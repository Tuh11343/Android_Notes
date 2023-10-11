package com.example.bai2_kotline_final

import java.io.Serializable

class Note(
    var id:Int,
    var title:String,
    var description:String,
    var timeCreated:Long,
    var imageURL:String,
    var timeAlarm:Long,
    var isAlarm:Int,
):Serializable {
    constructor():this(-1,"","",-1L,"",-1L,0)
    override fun toString(): String {
        return "Note(id=$id, title='$title', description='$description', timeCreated=$timeCreated, imageURL='$imageURL', timeAlarm='$timeAlarm', isAlarm=$isAlarm)"
    }

}

