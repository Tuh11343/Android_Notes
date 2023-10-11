package com.example.bai2_kotline_final

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.example.bai2_kotline_final.databinding.ActivityAlarmBinding

class AlarmActivity : AppCompatActivity() {

    private lateinit var binding:ActivityAlarmBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding=ActivityAlarmBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setUpComponent()
    }

    private fun setUpComponent(){

        val bundle=intent.extras
        if(bundle!=null){

            Log.i("DEBUG","Open from notification")
            val alarmNote:Note
            Log.i("DEBUG","Open from notification:${intent.action}")
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                alarmNote= bundle.getSerializable(AlarmReceiver.NOTE_KEY,Note::class.java)?:Note()
            }else{
                alarmNote=bundle.getSerializable(AlarmReceiver.NOTE_KEY) as Note
            }

            binding.title.text=alarmNote.title
            binding.description.text=alarmNote.description
            binding.closeButton.setOnClickListener{
                val alarmManager = getSystemService(ALARM_SERVICE) as AlarmManager
                val intent = Intent(this, AlarmReceiver::class.java)
                val pendingIntent =
                    PendingIntent.getBroadcast(this, alarmNote.id, intent, PendingIntent.FLAG_IMMUTABLE)
                alarmManager.cancel(pendingIntent)
                finish()
            }

        }

    }

}