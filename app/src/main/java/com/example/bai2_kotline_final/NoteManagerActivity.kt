package com.example.bai2_kotline_final

import android.app.TimePickerDialog
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.os.bundleOf
import com.example.bai2_kotline_final.databinding.NotemanagerLayoutBinding
import java.util.Calendar
import android.net.Uri
import java.text.SimpleDateFormat
import java.util.Locale


class NoteManagerActivity : AppCompatActivity() {

    private lateinit var calendar:Calendar
    private lateinit var imageURL: String
    private lateinit var timePickerDialog: TimePickerDialog
    private var timeAlarm: Long=-1L

    private lateinit var binding: NotemanagerLayoutBinding
    private var imageSelectResultLauncher =
        registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
            run {
                if (uri != null) {
                    grantUriPermission(packageName, uri, Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    binding.IVPreviewImage.setImageURI(uri)
                    imageURL = uri.toString()
                }
            }
        }


    companion object {
        const val NOTE_KEY = "NOTE_KEY"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = NotemanagerLayoutBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.noteManagerToolbar)

        val intent=intent
        val bundle=intent.extras

         if(intent.action==MainActivity.EDIT_NOTE_ACTION){//Check if open from noteClick
            val editNote:Note
            if(bundle!=null){
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    editNote= bundle.getSerializable(MainActivity.EDIT_NOTE_KEY,Note::class.java)?:Note()
                }else{
                    editNote=bundle.getSerializable(MainActivity.EDIT_NOTE_KEY) as Note
                }
                setUpComponent()
                editNoteSetUpComponent(editNote)
            }

        }
        else{
            setUpComponent()
        }

    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.notemanager_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.menuNoteManager_addNote -> {
                if (binding.noteManagerTitle.text.isNotBlank() && binding.noteManagerDescription.text.isNotBlank() &&
                    binding.timeSelected.text.isNotBlank()
                ) {
                    val isAlarm= if(binding.checkBoxAlarm.isChecked) 0 else 1
                    val note = Note(
                        0,
                        binding.noteManagerTitle.text.toString(),
                        binding.noteManagerDescription.text.toString(),
                        System.currentTimeMillis(),
                        imageURL, timeAlarm, isAlarm
                    )
                    val intent = Intent()
                    val bundle = bundleOf()
                    bundle.putSerializable(NOTE_KEY, note)

                    intent.putExtras(bundle)
                    setResult(RESULT_OK, intent)
                    finish()
                } else {
                    Toast.makeText(this, "Không thể để trống dữ liệu", Toast.LENGTH_SHORT).show()
                }
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun setUpComponent() {

        calendar = Calendar.getInstance()
        setUpTimePicker()

        binding.btnSelectImage.setOnClickListener {

            imageSelectResultLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))

        }

        binding.btnSelectTime.setOnClickListener {
            if (timePickerDialog.isShowing) {
                timePickerDialog.dismiss()
            } else {
                timePickerDialog.show()
            }
        }

    }

    private fun editNoteSetUpComponent(note: Note) {

        //Set Up View
        binding.noteManagerTitle.setText(note.title)
        binding.noteManagerDescription.setText(note.description)
        binding.IVPreviewImage.setImageURI(Uri.parse(note.imageURL))
        val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
        binding.timeSelected.text=timeFormat.format(note.timeAlarm)
        binding.checkBoxAlarm.isChecked= note.isAlarm ==0

        //Set up variable
        imageURL=note.imageURL
        timeAlarm=note.timeAlarm
        calendar.timeInMillis=note.timeAlarm

    }

    private fun setUpTimePicker() {
        timePickerDialog = TimePickerDialog(
            this,
            { _, selectedHour, selectedMinute ->

                calendar[Calendar.HOUR_OF_DAY] = selectedHour
                calendar[Calendar.MINUTE] = selectedMinute
                calendar[Calendar.SECOND] = 0
                calendar[Calendar.MILLISECOND] = 0

                // Check if the selected time is in the past
                if (calendar.timeInMillis < System.currentTimeMillis()) {
                    // If it is, add one day (24 hours) to it
                    calendar.add(Calendar.DAY_OF_YEAR, 1)

                    // Subtract the "extra" time from the current time to the start of the new day
                    val now = Calendar.getInstance()
                    val extraTime = now.get(Calendar.HOUR_OF_DAY) * 60 * 60 * 1000 +
                            now.get(Calendar.MINUTE) * 60 * 1000 +
                            now.get(Calendar.SECOND) * 1000 +
                            now.get(Calendar.MILLISECOND)
                    calendar.timeInMillis -= extraTime
                }

                val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
                timeAlarm = calendar.timeInMillis
                binding.timeSelected.text = timeFormat.format(timeAlarm)

            },
            12,
            0,
            false
        )
    }

}