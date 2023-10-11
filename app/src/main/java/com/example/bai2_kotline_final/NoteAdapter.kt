package com.example.bai2_kotline_final

import android.content.Context
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.bai2_kotline_final.databinding.NoteItemBinding
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.Locale

class NoteAdapter(
    private var noteList: MutableList<Note>,
    private var mListener: INoteListener,
    private var context: Context
) : RecyclerView.Adapter<NoteAdapter.MyViewHolder>() {

    inner class MyViewHolder(val binding: NoteItemBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val binding = NoteItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MyViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return noteList.size
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val note = noteList[holder.absoluteAdapterPosition]
        if (note != null) {
            holder.binding.itemNoteTitle.text = note.title
            holder.binding.itemNoteDescription.text = note.description
            holder.binding.itemNoteTimeCreated.text =
                DateFormat.getDateTimeInstance().format(note.timeCreated)

            val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
            holder.binding.timeAlarm.text= timeFormat.format(note.timeAlarm)

            try {
                val inputStream = context.contentResolver.openInputStream(Uri.parse(note.imageURL))
                val bitmap = BitmapFactory.decodeStream(inputStream)
                holder.binding.image.setImageBitmap(bitmap)
            } catch (er: SecurityException) {
                Log.e("ERROR", "Error from:$er")
            }
            holder.binding.isAlarm.isChecked = note.isAlarm == 0
            holder.binding.isAlarm.setOnCheckedChangeListener{ compoundButton, isChecked ->
                run {
                        mListener.onCheckClick(note,isChecked)
                }
            }
            holder.binding.itemNoteLayout.setOnClickListener{
                mListener.onClickHandle(note, holder.absoluteAdapterPosition)
            }
            holder.binding.itemNoteLayout.setOnLongClickListener{
                mListener.onLongClickHandle(holder.absoluteAdapterPosition)
                true
            }
        }
    }

    fun updateUI(noteList: MutableList<Note>){
        this.noteList =noteList
        notifyDataSetChanged()
    }

}