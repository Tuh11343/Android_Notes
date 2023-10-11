package com.example.bai2_kotline_final

import android.app.AlertDialog
import android.content.Intent
import android.content.SharedPreferences
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.os.bundleOf
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.bai2_kotline_final.databinding.MainLayoutBinding
import android.Manifest
import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.IntentFilter
import android.content.pm.PackageManager
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import java.util.Calendar
import java.util.concurrent.TimeUnit


class MainActivity : AppCompatActivity() {

    private lateinit var binding: MainLayoutBinding
    private var noteList = mutableListOf<Note>()
    lateinit var noteAdapter: NoteAdapter
    private var noteDBHelper = NoteDBHelper(this)
    private lateinit var sharedPreferences: SharedPreferences

    companion object {
        const val EDIT_NOTE_KEY = "EDIT_NOTE_KEY"
        const val NOTE_INDEX = "NOTE_INDEX"
        const val EDIT_NOTE_ACTION = "EDIT_NOTE_ACTION"
        const val ADD_NOTE_ACTION = "ADD_NOTE_ACTION"
        var isActive = false
    }

    private var addNoteResultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            run {
                if (result.resultCode == RESULT_OK) {
                    if (result.data != null) {
                        val data = result.data
                        val bundle = data!!.extras
                        val note = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            bundle!!.getSerializable(NoteManagerActivity.NOTE_KEY, Note::class.java)
                                ?: Note()
                        } else {
                            bundle!!.getSerializable(NoteManagerActivity.NOTE_KEY) as Note
                        }

                        if (note.id != -1) {
                            if (noteDBHelper.addNote(note)) {

                                noteList.add(note)
                                noteAdapter.notifyDataSetChanged()
                                Toast.makeText(
                                    this@MainActivity,
                                    "Add Note Succeed",
                                    Toast.LENGTH_SHORT
                                ).show()

                                //Set up alarm
                                if (note.isAlarm == 0) {
                                    setUpAlarm(note)
                                }

                            } else {
                                Toast.makeText(
                                    this@MainActivity,
                                    "Add Note Failure",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                    }
                }
            }
        }

    private var editNoteResultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            run {
                if (result.resultCode == RESULT_OK) {
                    if (result.data != null) {
                        val data = result.data
                        val bundle = data!!.extras
                        val note = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            bundle!!.getSerializable(NoteManagerActivity.NOTE_KEY, Note::class.java)
                                ?: Note()
                        } else {
                            bundle!!.getSerializable(NoteManagerActivity.NOTE_KEY) as Note
                        }

                        if (note.id != -1) {
                            val noteIndex = sharedPreferences.getInt(NOTE_INDEX, -1)
                            note.id = noteList[noteIndex].id

                            if (noteDBHelper.editNote(note)) {
                                noteList[noteIndex] = note
                                noteAdapter.notifyItemChanged(noteIndex)
                                Toast.makeText(
                                    this@MainActivity,
                                    "Edit Note Succeed",
                                    Toast.LENGTH_SHORT
                                ).show()

                                //Set up alarm
                                if (note.isAlarm == 0) {
                                    cancelAlarm(note)
                                    setUpAlarm(note)
                                }

                            } else {
                                Toast.makeText(
                                    this@MainActivity,
                                    "Edit Note Failure",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                    }
                }
            }
        }

    private var requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            run {

                if (ContextCompat.checkSelfPermission(
                        this,
                        Manifest.permission.READ_EXTERNAL_STORAGE
                    )
                    ==
                    PackageManager.PERMISSION_GRANTED
                ) {
                    Log.i("DEBUG", "Read External storage permission granted")
                } else {
                    Log.i("DEBUG", "Read External storage permission failure")
                }
            }
        }


    override fun onStart() {
        isActive = true
        super.onStart()
    }

    override fun onPause() {
        isActive = false
        super.onPause()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = MainLayoutBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.mainToolbar)
        setUpComponent()

    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.menuMain_addNote -> {
                val intent = Intent(this, NoteManagerActivity::class.java)
                intent.action = ADD_NOTE_ACTION
                addNoteResultLauncher.launch(intent)
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun setUpComponent() {

        //Request Permission
        requestPermission()

        //Create Notification Channel
        createNotificationChannel()

        //Set Up Adapter
        sharedPreferences = getSharedPreferences("MyPreferences", MODE_PRIVATE)
        noteList = noteDBHelper.getAllNote()
        noteAdapter = NoteAdapter(noteList, object : INoteListener {
            override fun onLongClickHandle(lineIndex: Int) {
                val builder = AlertDialog.Builder(this@MainActivity)
                builder.setTitle("Delete Note")
                    .setCancelable(true)
                    .setMessage("Do you want to delete this note?")
                    .setNegativeButton("No") { dialogInterface, i ->
                        run {
                            dialogInterface.dismiss()
                        }
                    }
                    .setPositiveButton("Yes") { _, _ ->
                        run {
                            if (noteDBHelper.deleteNote(noteList[lineIndex].id)) {
                                Toast.makeText(
                                    this@MainActivity, "Delete Note Completed",
                                    Toast.LENGTH_SHORT
                                ).show()
                                noteList.removeAt(lineIndex)
                                noteAdapter.notifyDataSetChanged()
                            } else {
                                Toast.makeText(
                                    this@MainActivity, "Delete Note Database Failure",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                    }
                    .create().show()
            }

            override fun onCheckClick(note: Note, isChecked: Boolean) {

                val alarmIntent = Intent(this@MainActivity, AlarmReceiver::class.java)
                val pendingIntent = PendingIntent.getBroadcast(
                    this@MainActivity, note.id, alarmIntent,
                    PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
                )

                val alarmUp = (pendingIntent != null)
                if (alarmUp && !isChecked) {
                    note.isAlarm = 1
                    cancelAlarm(note)
                } else if (!alarmUp && isChecked) {
                    note.isAlarm = 0
                    setUpAlarm(note)
                } else if (note.isAlarm == 1 && alarmUp && isChecked) {
                    note.isAlarm = 0
                    cancelAlarm(note)
                    setUpAlarm(note)
                }
                noteDBHelper.editNote(note)

            }

            override fun onClickHandle(note: Note, lineIndex: Int) {
                val intent = Intent(this@MainActivity, NoteManagerActivity::class.java)
                val bundle = bundleOf()
                bundle.putSerializable(EDIT_NOTE_KEY, note)
                intent.putExtras(bundle)
                intent.action = EDIT_NOTE_ACTION

                //Luu lineIndex cua note duoc chon
                val editor = sharedPreferences.edit()
                editor.putInt(NOTE_INDEX, lineIndex)
                editor.apply()

                editNoteResultLauncher.launch(intent)
            }
        }, this)
        binding.mainRclv.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        binding.mainRclv.adapter = noteAdapter

        setUpMainReceiver()

    }

    private fun requestPermission() {
        requestPermissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
    }

    private fun createNotificationChannel() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Alarm Channel"
            val description = "This is Alarm Description"
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(AlarmReceiver.CHANNEL_ID, name, importance)
            channel.description = description

            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }

    }

    private fun setUpMainReceiver() {
        val broadcastReceiver = object : BroadcastReceiver() {
            @RequiresApi(Build.VERSION_CODES.TIRAMISU)
            override fun onReceive(context: Context, intent: Intent) {
                Log.i("DEBUG", "Broad cast o main dang chay")

                //Tat alarm trung
                val bundle = intent.extras
                var note = Note()
                if (bundle != null) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        note = bundle.getSerializable(AlarmReceiver.NOTE_KEY, Note::class.java)
                            ?: Note()
                    } else {
                        note = bundle.getSerializable(AlarmReceiver.NOTE_KEY) as Note
                    }
                }
                for (noteTemp in noteList) {
                    if (noteTemp.id != note.id && noteTemp.timeAlarm == note.timeAlarm && noteTemp.isAlarm == 0) {
                        Log.i("DEBUG", "Cancel alarm:${noteTemp.toString()}")
                        cancelAlarm(noteTemp)
                        noteTemp.isAlarm = 1
                        if (noteDBHelper.editNote(noteTemp)) {
                            Log.i("DEBUG", "Cap nhat noteTemp thanh cong")
                        }
                    }
                }

                val noteList = noteDBHelper.getAllNote()
                noteAdapter.updateUI(noteList)

            }
        }
        val intentFilter = IntentFilter("ACTION_TEST")
        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.TIRAMISU){
            this.registerReceiver(broadcastReceiver, intentFilter, RECEIVER_NOT_EXPORTED)
        }else{
            this.registerReceiver(broadcastReceiver, intentFilter)
        }

    }

    private fun setUpAlarm(note: Note) {

        val calendar = Calendar.getInstance()
        calendar.timeInMillis = note.timeAlarm
        val diff = calendar.timeInMillis - System.currentTimeMillis()

        // Convert the time difference to hours and minutes
        val hours = TimeUnit.MILLISECONDS.toHours(diff)
        val minutes = TimeUnit.MILLISECONDS.toMinutes(diff) % 60

        // Display a Toast with the time difference
        if (hours >= 1) {
            Toast.makeText(
                this,
                "Báo thức sẽ kêu sau $hours giờ và $minutes phút nữa.",
                Toast.LENGTH_SHORT
            ).show()
        }else if (minutes >= 1L && hours==0L) {
            Toast.makeText(
                this,
                "Báo thức sẽ kêu sau $minutes phút nữa.",
                Toast.LENGTH_LONG
            ).show()
        } else {
            Toast.makeText(this, "Báo thức sau gần 1 phút nữa", Toast.LENGTH_LONG).show()
        }

        val alarmManager = getSystemService(ALARM_SERVICE) as AlarmManager
        val intent = Intent(this, AlarmReceiver::class.java)

        //Put Note
        val bundle = bundleOf()
        bundle.putSerializable(NoteManagerActivity.NOTE_KEY, note)
        intent.putExtras(bundle)

        //Request Code nen su dung note.id để request code la duy nhat
        val pendingIntent =
            PendingIntent.getBroadcast(this, note.id, intent, PendingIntent.FLAG_IMMUTABLE)

        //For Test Only
//        val calendarTest = Calendar.getInstance()
//        calendarTest.add(Calendar.SECOND, 5)
//        alarmManager.setAndAllowWhileIdle(
//            AlarmManager.RTC_WAKEUP, calendarTest.timeInMillis,
//            pendingIntent
//        )

        //Set up alarm
        alarmManager.setAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP, calendar.timeInMillis,
            pendingIntent
        )
    }

    private fun cancelAlarm(note: Note) {
        val alarmManager = getSystemService(ALARM_SERVICE) as AlarmManager
        val intent = Intent(this, AlarmReceiver::class.java)
        val pendingIntent =
            PendingIntent.getBroadcast(this, note.id, intent, PendingIntent.FLAG_IMMUTABLE)
        alarmManager.cancel(pendingIntent)
    }

}