package com.example.bai2_kotline_final

interface INoteListener {

    fun onClickHandle(note:Note, lineIndex:Int)
    fun onLongClickHandle(lineIndex: Int)
    fun onCheckClick(note:Note,isChecked:Boolean)

}