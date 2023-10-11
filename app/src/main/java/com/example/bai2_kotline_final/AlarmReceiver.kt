package com.example.bai2_kotline_final

import android.app.PendingIntent
import android.app.TaskStackBuilder
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.os.bundleOf


class AlarmReceiver() : BroadcastReceiver() {

    companion object {
        const val CHANNEL_ID = "alarmChannel"
        const val INTENT_ACTION="OPENFROMNOTIFICATION"
        const val NOTE_KEY="NOTE_KEY"
    }

    override fun onReceive(context: Context?, intent: Intent?) {

        intent!!.flags=Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK

        //Tien hanh lay note
        val bundle=intent.extras
        val note = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            bundle!!.getSerializable(NoteManagerActivity.NOTE_KEY,Note::class.java)?:Note()
        } else {
            bundle!!.getSerializable(NoteManagerActivity.NOTE_KEY)as Note
        }

        //Tien hanh tao moi intent de goi toi activity
        val putIntent=Intent(context,AlarmActivity::class.java)
        val putBundle= bundleOf()
        putBundle.putSerializable(NOTE_KEY,note)
        putIntent.putExtras(putBundle)
        putIntent.action= INTENT_ACTION

        // Tạo một back stack
        val resultPendingIntent: PendingIntent? = TaskStackBuilder.create(context).run {
            // Thêm tất cả parent activity vào back stack
            addNextIntentWithParentStack(putIntent)
            // Nhận PendingIntent chứa toàn bộ back stack
            getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        }

        //Tien hanh tao thanh thong bao
        val alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
        val builder=NotificationCompat.Builder(context!!, CHANNEL_ID)
        builder.setContentTitle("Alarming")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentText("OK Alarming")
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .setContentIntent(resultPendingIntent)
            .setSound(alarmSound)

        //Tien hanh thong bao
        val notificationManager=NotificationManagerCompat.from(context)
        try{
            notificationManager.notify(123,builder.build())
        }catch (er:SecurityException){
            Log.e("ERROR","Error from alarmReceiver:$er")
        }

        //Cap nhat UI MainActivity
        val noteDBHelper=NoteDBHelper(context)
        note.isAlarm=1

        if(noteDBHelper.editNote(note)){
            Log.i("DEBUG","Sua isAlarm o AlarmReceiver thanh cong")
        }
        if(MainActivity.isActive){
            val broadcastIntent = Intent("ACTION_TEST")
            val bundleActionTest= bundleOf()
            bundleActionTest.putSerializable(NOTE_KEY,note)
            broadcastIntent.putExtras(bundleActionTest)
            context.sendBroadcast(broadcastIntent)
        }else{
            Log.i("DEBUG","Main dang bi an")
        }



    }


}