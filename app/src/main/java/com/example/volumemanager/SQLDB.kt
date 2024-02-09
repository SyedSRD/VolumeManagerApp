package com.example.volumemanager


import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log
import java.text.SimpleDateFormat
import java.util.*

class SQLDB(context: Context):SQLiteOpenHelper(context,DB_name,null,1) {

    companion object {

        val DB_name = "VolumeMan.db "
        val TB_name = "ScheduleItems "
        val id = "ID"
        val sec_id = "SEC_ID"
        val setting_enabled = "Setting"
        val title = "Title"
        val s_time="S_Time"
        val e_time="E_Time"
        val mbar="MBar"
        val rbar="RBar"
        val abar="ABar"
        val DontD="DontDisturb"
        val airplane="Airplane"
        val vibrate="Vibrate"
        val just_vib="JustVibrate"
        val repeat="Repeat"
        val sun="Sunday"
        val mon="Monday"
        val tue="Tuesday"
        val wed="Wednesday"
        val thu="Thursday"
        val fri="Friday"
        val sat="Saterday"
        val dayS="Day"

    }

    override fun onCreate(db: SQLiteDatabase?) {
        db?.execSQL("create Table $TB_name(ID INTEGER PRIMARY KEY AUTOINCREMENT," +
                "SEC_ID INTEGER ," +
                "Title TEXT," +
                "MBar INT,RBar INT,ABar INT," +
                "DontDisturb BOOLEAN DEFAULT false,Airplane BOOLEAN DEFAULT false," +
                "Vibrate BOOLEAN DEFAULT false,JustVibrate BOOLEAN DEFAULT false," +
                "Repeat BOOLEAN DEFAULT false," +
                "Sunday BOOLEAN DEFAULT true ,Monday BOOLEAN DEFAULT true,Tuesday BOOLEAN DEFAULT true,Wednesday BOOLEAN DEFAULT true," +
                "Thursday BOOLEAN DEFAULT true,Friday BOOLEAN DEFAULT true,Saterday BOOLEAN DEFAULT true," +
                "Setting BOOLEAN DEFAULT false,S_Time TIME  ,E_Time TIME ,Arrow BOOLEAN DEFAULT false ,Day TEXT DEFAULT 'Tommorow')")
        val values = ContentValues()
        values.put(id,0)
        values.put(sec_id, 0)
        values.put(title, "Defaults")
        values.put(mbar, 7)
        values.put(rbar, 4)
        values.put(abar, 4)
        values.put(setting_enabled,true)
        db?.insert(TB_name, null, values)
        }

    override fun onUpgrade(db: SQLiteDatabase?, p1: Int, p2: Int) {
        db?.execSQL("DROP TABLE IF EXISTS $TB_name")
    }

    val data_geter: Cursor
        get() {
                val db = this.writableDatabase
                var data = db.rawQuery("select * from " + TB_name, null)
                return data
        }

    fun add_data(sec_id1: Int,title_text: String,mbar1:Int,rbar1:Int,abar1:Int) {
        val db = this.writableDatabase

        val values = ContentValues()
        values.put(sec_id, sec_id1)
        values.put(title, title_text)
        values.put(mbar, mbar1)
        values.put(rbar, rbar1)
        values.put(abar, abar1)
        values.put(s_time,SimpleDateFormat("hh:mm a").format(Calendar.getInstance().time))
        values.put(e_time,SimpleDateFormat("hh:mm a").format(Calendar.getInstance().time))
        db.insert(TB_name, null, values)
    }

    fun delete_data(sec_id1: String): Int {

        val db = this.writableDatabase
        val item = db.delete(TB_name, "SEC_ID = ? ", arrayOf(sec_id1))
        return item
    }
    fun updateDB(sec_id1: Int,Utype: String,Uvalue: String): Boolean {
        val db = this.writableDatabase
        val values = ContentValues()

        when(Utype){
            "title" -> {values.put(title, Uvalue)}
            setting_enabled -> {values.put(setting_enabled,Uvalue.toBoolean())}
            "MBar" ->  {println(Uvalue+Uvalue::class.simpleName)
                        values.put(mbar, Uvalue.toInt())}
            "RBar" ->  {values.put(rbar, Uvalue.toInt())}
            "ABar" ->  {values.put(abar, Uvalue.toInt())}
            DontD -> { values.put(DontD, Uvalue.toBoolean())}
            airplane -> {values.put(airplane, Uvalue.toBoolean())}
            vibrate -> {values.put(vibrate, Uvalue.toBoolean())}
            just_vib -> {values.put(just_vib, Uvalue.toBoolean())}
            repeat -> {values.put(repeat, Uvalue.toBoolean())}
            sun -> {values.put(sun,Uvalue.toBoolean())}
            mon -> {values.put(mon,Uvalue.toBoolean())}
            tue -> {values.put(tue,Uvalue.toBoolean())}
            wed-> {values.put(wed,Uvalue.toBoolean())}
            thu -> {values.put(thu,Uvalue.toBoolean())}
            fri -> {values.put(fri,Uvalue.toBoolean())}
            sat -> {values.put(sat,Uvalue.toBoolean())}
            s_time -> {values.put(s_time,Uvalue)}
            e_time -> {values.put(e_time,Uvalue)}
            "Arrow" -> {values.put("Arrow",Uvalue.toBoolean())}
            dayS -> {values.put(dayS,Uvalue)}
        }
        var success: Long=0
        try {
            if (sec_id1 == 0) {
                success = db.update(TB_name, values, "ID = 0 AND SEC_ID = 0 ", null).toLong()//, arrayOf(sec_id1.toString())).toLong()
            } else {
                success = db.update(TB_name, values, "SEC_ID = ? ", arrayOf(sec_id1.toString())).toLong()
            }
        }catch (e:Exception){
            Log.i("MyAlarmReceiver", e.toString())
        }
        //Log.i("MainActivity","datatattatatatat bas Succss "+success)
        return Integer.parseInt("$success") != -1
    }
}

