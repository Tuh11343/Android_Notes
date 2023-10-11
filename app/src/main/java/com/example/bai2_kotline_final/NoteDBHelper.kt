package com.example.bai2_kotline_final

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log

class NoteDBHelper(context: Context):SQLiteOpenHelper(context,DATABASE_NAME,null, DATABASE_VERSION) {

    companion object{
        private const val DATABASE_NAME="Note Manager"
        private const val DATABASE_VERSION=1
        private const val TABLE_NAME="Note"
        private const val COLUMN_ID="id"
        private const val COLUMN_TITLE="title"
        private const val COLUMN_DESCRIPTION="description"
        private const val COLUMN_TIME_CREATED="timeCreated"
        private const val COLUMN_IMAGE_URL="imageURL"
        private const val COLUMN_TIME_ALARM="timeAlarm"
        private const val COLUMN_IS_ALARM="isAlarm"
    }

    override fun onCreate(db: SQLiteDatabase?) {
        val query="CREATE TABLE $TABLE_NAME ( "+
                "$COLUMN_ID INTEGER PRIMARY KEY AUTOINCREMENT, "+
                "$COLUMN_TITLE TEXT, "+
                "$COLUMN_DESCRIPTION TEXT, "+
                "$COLUMN_TIME_CREATED LONG," +
                "$COLUMN_IMAGE_URL TEXT," +
                "$COLUMN_TIME_ALARM LONG," +
                "$COLUMN_IS_ALARM INTEGER )"
        db?.execSQL(query)
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        val query="DROP TABLE IF EXISTS $TABLE_NAME"
        db?.execSQL(query)
        onCreate(db)
    }

    fun addNote(note:Note):Boolean{
        val db=writableDatabase
        val cv=ContentValues()
        try{
            cv.put(COLUMN_TITLE,note.title)
            cv.put(COLUMN_DESCRIPTION,note.description)
            cv.put(COLUMN_TIME_CREATED,note.timeCreated)
            cv.put(COLUMN_IMAGE_URL,note.imageURL)
            cv.put(COLUMN_TIME_ALARM,note.timeAlarm)
            cv.put(COLUMN_IS_ALARM,note.isAlarm)
            val result=db.insert(TABLE_NAME,null,cv)
            note.id=Integer.parseInt(result.toString())
            return result !=-1L
        }catch (er:Exception){
            Log.i("ERROR","Error from addNote:$er")
        }finally {
            db.close()
        }
        return false
    }

    fun editNote(note:Note):Boolean{
        val db=writableDatabase
        val cv=ContentValues()
        try{
            cv.put(COLUMN_ID,note.id)
            cv.put(COLUMN_TITLE,note.title)
            cv.put(COLUMN_DESCRIPTION,note.description)
            cv.put(COLUMN_TIME_CREATED,note.timeCreated)
            cv.put(COLUMN_IMAGE_URL,note.imageURL)
            cv.put(COLUMN_TIME_ALARM,note.timeAlarm)
            cv.put(COLUMN_IS_ALARM,note.isAlarm)
            val result=db.update(TABLE_NAME,cv, "$COLUMN_ID = ${note.id}",null)
            return result >0
        }catch (er:Exception){
            Log.i("ERROR","Error from editNote:$er")
        }finally {
            db.close()
        }
        return false
    }

    fun deleteNote(id:Int):Boolean{
        val db=writableDatabase
        try{
            val result=db.delete(TABLE_NAME, "$COLUMN_ID = $id", null)
            return result > 0
        }catch (er:Exception){
            Log.i("ERROR","Error from deleteNote:$er")
        }finally {
            db.close()
        }
        return false
    }

    fun getAllNote():MutableList<Note>{
        val db=readableDatabase
        val noteList= mutableListOf<Note>()
        try{
            val query="SELECT * FROM $TABLE_NAME"
            val cursor=db.rawQuery(query,null)
            cursor.moveToFirst()
            while(cursor.moveToNext()){

                val INDEX_ID=cursor.getColumnIndex(COLUMN_ID)
                val INDEX_TITLE=cursor.getColumnIndex(COLUMN_TITLE)
                val INDEX_DESCRIPTION=cursor.getColumnIndex(COLUMN_DESCRIPTION)
                val INDEX_TIMECREATED=cursor.getColumnIndex(COLUMN_TIME_CREATED)
                val INDEX_IMAGEURL=cursor.getColumnIndex(COLUMN_IMAGE_URL)
                val INDEX_TIMEALARM=cursor.getColumnIndex(COLUMN_TIME_ALARM)
                val INDEX_ISALARM=cursor.getColumnIndex(COLUMN_IS_ALARM)

                val id=cursor.getInt(INDEX_ID)
                val title=cursor.getString(INDEX_TITLE)
                val description=cursor.getString(INDEX_DESCRIPTION)
                val timeCreated=cursor.getLong(INDEX_TIMECREATED)
                val imageURL=cursor.getString(INDEX_IMAGEURL)
                val timeAlarm=cursor.getLong(INDEX_TIMEALARM)
                val isAlarm=cursor.getInt(INDEX_ISALARM)

                val note=Note(id,title,description,timeCreated,imageURL,timeAlarm,isAlarm)
                noteList.add(note)

            }
            cursor.close()
        }catch (er:Exception){
            Log.i("ERROR","Error from getAllNote:$er")
        }finally {
            db.close()
        }
        Log.i("DEBUG","So luong note:${noteList.size}")
        return noteList
    }


}






























