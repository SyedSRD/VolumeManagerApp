package com.example.volumemanager

import android.app.AlertDialog
import android.content.Intent
import android.database.Cursor
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.util.TypedValue
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import java.util.*
import kotlin.collections.ArrayList


class MainActivity : AppCompatActivity() {

    lateinit var lists:ArrayList<custom_class>
    lateinit var DB: SQLDB
    lateinit var cur: Cursor
    lateinit var adapter:Adapter
    var id_str=1
    var listSize: Int = 0
    lateinit var rcycler :RecyclerView
    lateinit var FABtn :FloatingActionButton
    lateinit var saveAndLock :Switch
    lateinit var DmediaSbar:SeekBar
    lateinit var DringSbar:SeekBar
    lateinit var DalarmSbar:SeekBar
    lateinit var DvibrateSw:Switch
    lateinit var VerticalLHiddenView:LinearLayout
    var idd:Int=0
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        Log.i("MainActivity", "onCreate")


        FABtn= findViewById<FloatingActionButton>(R.id.floatingAction_btn)
        saveAndLock =findViewById<Switch>(R.id.savealock_sw)
        //saveAndLock.isChecked=true
        DmediaSbar=findViewById<SeekBar>(R.id.media_sbar_N)
        DringSbar=findViewById<SeekBar>(R.id.ring_sbar_N)
        DalarmSbar=findViewById<SeekBar>(R.id.alarm_sbar_N)
        DvibrateSw=findViewById<Switch>(R.id.vibrate_sw_N)
        VerticalLHiddenView=findViewById<LinearLayout>(R.id.LLNV1)
        rcycler=findViewById<RecyclerView>(R.id.list_rv)

        //findViewById<TextView>(R.id.default_tv).setTextSize(TypedValue.COMPLEX_UNIT_PX,resources.getDimension(R.dimen.large))

        lists = ArrayList<custom_class>()
        DB=SQLDB(applicationContext)
        adapter=Adapter(applicationContext, lists)

            checkTheme()

            showData()

        // Add button
        FABtn.setOnClickListener{//(object : View.OnClickListener {
            addItem()
        }

        // save the defaults settings
        saveAndLock.setOnCheckedChangeListener{ _, isChecked ->
                Log.d("MyAlarmReceiver", "Setting 7777" + isChecked.toString().toBoolean())
                DB.updateDB(0, "Setting", isChecked.toString())

                adapter.setEnabled=saveAndLock.isChecked
                adapter.notifyDataSetChanged()
                FABtn.isEnabled = isChecked
                VerticalLHiddenView.forEachChildView{  it.isEnabled = !(isChecked) }
            }

///////////////////////////////////////////////////////       SeekBar for Volume /////////////////////////////////////////////////////////////////////////////
        DmediaSbar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                //Toast.makeText(context, "seekbar progress: $progress", Toast.LENGTH_SHORT).show()
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {
                //Toast.makeText(this@MainActivity, "seekbar touch started!", Toast.LENGTH_SHORT).show()
            }

            override fun onStopTrackingTouch(seekBar: SeekBar) {
                val res = DB.updateDB(0, "MBar", seekBar.progress.toString())
            }
        })
        DringSbar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                //Toast.makeText(context, "seekbar progress: $progress", Toast.LENGTH_SHORT).show()
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {
                //Toast.makeText(this@MainActivity, "seekbar touch started!", Toast.LENGTH_SHORT).show()
            }

            override fun onStopTrackingTouch(seekBar: SeekBar) {
                val res = DB.updateDB(0, "RBar", seekBar.progress.toString())
            }
        })
        DalarmSbar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                //Toast.makeText(context, "seekbar progress: $progress", Toast.LENGTH_SHORT).show()
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {
               // Toast.makeText(this@MainActivity, "seekbar touch started!", Toast.LENGTH_SHORT).show()
            }

            override fun onStopTrackingTouch(seekBar: SeekBar) {
                val res = DB.updateDB(0, "ABar", seekBar.progress.toString())
            }
        })
///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

        DvibrateSw.setOnCheckedChangeListener{ _, isChecked ->
            try {

                if (checkSystemWritePermission()) {
                    val res=DB.updateDB(0, "Vibrate", isChecked.toString())
                } else {
                    DvibrateSw.isChecked=false
                    val res=DB.updateDB(0, "Vibrate", false.toString())
                }
            } catch (e: Exception) {
                Log.i("MyAlarmReceiver", e.toString())
                toast("unable to set as Ringtoon ")
            }

        }

        cur=DB.data_geter
        listSize=adapter.itemCount


        adapter.setEnabled=saveAndLock.isChecked
        //adapter.notifyDataSetChanged()
        VerticalLHiddenView.forEachChildView{  it.isEnabled = !(saveAndLock.isChecked) }
        FABtn.isEnabled =saveAndLock.isChecked

        rcycler.layoutManager= LinearLayoutManager(applicationContext)
        rcycler.adapter=adapter

    }
//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////


    private fun showData(){
        try {
       // if(cur.count== 1) Toast.makeText(applicationContext, "No item found", Toast.LENGTH_SHORT).show()

        while (cur.moveToNext()) {
            idd = cur.getInt(1)
            if (idd == 0) {
                DmediaSbar.progress=cur.getInt(3)
                DringSbar.progress=cur.getInt(4)
                DalarmSbar.progress=cur.getInt(5)
                DvibrateSw.isChecked=cur.getInt(8)!= 0
                saveAndLock.isChecked=cur.getInt(18) != 0
            } else {
                lists.add(custom_class(
                        idd,
                        title = cur.getString(2),
                        mbar = cur.getInt(3),
                        rbar = cur.getInt(4),
                        abar = cur.getInt(5),
                        dontdisturb = cur.getInt(6) != 0,
                        airplane = cur.getInt(7) != 0,
                        vibrate = cur.getInt(8) != 0,
                        just_vib = cur.getInt(9) != 0,
                        repeat = cur.getInt(10) != 0,
                        sun = cur.getInt(11) != 0,
                        mon = cur.getInt(12) != 0,
                        tue = cur.getInt(13) != 0,
                        wed = cur.getInt(14) != 0,
                        thu = cur.getInt(15) != 0,
                        fri = cur.getInt(16) != 0,
                        sat = cur.getInt(17) != 0,
                        setting_enabled = cur.getInt(18) != 0,
                        s_time = cur.getString(19),
                        e_time = cur.getString(20),
                        arrow = cur.getInt(21) != 0,
                        dayS = cur.getString(22)
                ))
            }
        }
        }catch (e: Exception) {Log.i("DBIN", e.message)}
        //adapter.notifyDataSetChanged()
        VerticalLHiddenView.forEachChildView{  it.isEnabled = !saveAndLock.isChecked }
        adapter.setEnabled=saveAndLock.isChecked
        FABtn.isEnabled =saveAndLock.isChecked
    }

    private fun View.forEachChildView(closure: (View) -> Unit) {
        closure(this)
        val groupView = this as? ViewGroup ?: return
        val size = groupView.childCount - 1
        for (i in 0..size) {
            groupView.getChildAt(i).forEachChildView(closure)
        }
    }

    private fun openAndroidPermissionsMenu() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val intent = Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS)
            intent.data = Uri.parse("package:" + this.packageName)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            this.startActivity(intent)

        }
    }

    private fun checkSystemWritePermission(): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (Settings.System.canWrite(this)){
                return true
            } else {
                openAndroidPermissionsMenu()
            }
        }
        return false
    }

    fun toast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    fun addItem(){
        val cur2=DB.data_geter
        cur2.moveToLast()
        var id2=cur2.getInt(1)

        println("id2  " + id2)
        //if(id_str!=id2)
        try {
            lists.add(
                    custom_class(
                            ++id2, "Normal", 7, 4, 4, false, false, false, false, false,
                            true, false, false, false, false, false, false, false, "07:30 AM", "08:30 AM",
                            false, "Today"
                    )
            )
            adapter.notifyItemInserted(cur2.count)
            DB.add_data(id2, "Normal", 7, 4, 4)
            //id_str = id_str + 1
            println("id2 mh " + id2)
        }catch (e: Exception) {Log.i("DBin", e.message)}
        //////DB.close()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val inflater=menuInflater
        inflater.inflate(R.menu.main_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        when(item?.itemId){
            R.id.permission -> {
                val permission = Intent(applicationContext, permission::class.java)
                startActivity(permission)
                return true
            }
            R.id.add -> {
                addItem()
                return true
            }
            R.id.theme -> {
                chooseThemeDialog()
                return true
            }
            R.id.close -> {
                finish()
                return true
            }
        }


        return super.onOptionsItemSelected(item)
    }

    private fun chooseThemeDialog() {

        val builder = AlertDialog.Builder(this@MainActivity)
        builder.setTitle("Select Theme")
        val styles = arrayOf("Light", "Dark (Preferred)", "System default")
        val checkedItem = MyPreferences(this).darkMode

        builder.setSingleChoiceItems(styles, checkedItem) { dialog, which ->

            when (which) {
                0 -> {
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
                    MyPreferences(this).darkMode = 0
                    delegate.applyDayNight()
                    dialog.dismiss()
                }
                1 -> {
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
                    MyPreferences(this).darkMode = 1
                    delegate.applyDayNight()
                    dialog.dismiss()
                }
                2 -> {
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
                    MyPreferences(this).darkMode = 2
                    delegate.applyDayNight()
                    dialog.dismiss()
                }

            }
        }

        val dialog = builder.create()
        dialog.show()
    }

    private fun checkTheme() {
        when (MyPreferences(this).darkMode) {
            0 -> {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
                delegate.applyDayNight()
            }
            1 -> {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
                delegate.applyDayNight()
            }
            2 -> {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
                delegate.applyDayNight()
            }
        }
    }

    override fun onStart() {
        super.onStart()
        showData()
        Log.i("MainActivity", "onStart")
    }

//    override fun onResume() {
//        super.onResume()
//        Log.i("MainActivity", "onResume")
//    }
//
//    override fun onPause() {
//        super.onPause()
//        Log.i("MainActivity", "onPause")
//    }
//
//    override fun onStop() {
//        super.onStop()
//        Log.i("MainActivity", "onStop")
//    }
//
//    override fun onRestart() {
//        super.onRestart()
//        Log.i("MainActivity", "onRestart")
//    }

    override fun onDestroy() {
        super.onDestroy()
        ////DB.close()
        Log.i("MainActivity", "onDestroy")
    }

}