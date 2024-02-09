

package com.example.volumemanager

import android.app.*
import android.content.Context
import android.content.Context.NOTIFICATION_SERVICE
import android.content.Intent
import android.database.Cursor
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.transition.AutoTransition
import android.transition.TransitionManager
import android.util.Log
import android.view.*
import android.view.WindowManager.LayoutParams.FLAG_DIM_BEHIND
import android.widget.*
import androidx.cardview.widget.CardView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputEditText
import com.nex3z.togglebuttongroup.MultiSelectToggleGroup
import com.nex3z.togglebuttongroup.button.CircularToggle
import java.text.SimpleDateFormat
import java.util.*
import java.util.Calendar.*
import kotlin.time.measureTimedValue


class Adapter(private var context: Context, data: ArrayList<custom_class>) : RecyclerView.Adapter<Adapter.ViewHolder>(), LifecycleOwner {
    private lateinit var lifecycleRegistry: LifecycleRegistry
    var data:ArrayList<custom_class> = data
    //lateinit var alarmManager: AlarmManager

    lateinit var title_name:String
    //lateinit var dayS:String
    var  LAYOUT_FLAG: Int = 0
    var setEnabled=false
    init {
        this.context = context
    }

    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    val notificationManager = context.getSystemService(NOTIFICATION_SERVICE) as NotificationManager

    val setDayD = mutableSetOf("Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat")
    //val setDay= mutableSetOf<String>()
    //setDay.addAll(listOf("Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat"))

    lateinit var db:SQLDB
    lateinit var cur: Cursor
    
    fun View.forEachChildView(closure: (View) -> Unit) {
        closure(this)
        val groupView = this as? ViewGroup ?: return
        val size = groupView.childCount - 1
        for (i in 0..size) {
            groupView.getChildAt(i).forEachChildView(closure)
        }
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                LAYOUT_FLAG = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
            } else {
                LAYOUT_FLAG = WindowManager.LayoutParams.TYPE_PHONE
            }

           var id1 =data[position].id
           var titlename =data[position].title
           var dayS:String=data[position].dayS

           val setDay= mutableSetOf<String>()

            db= SQLDB(context.applicationContext)

           //val setDay = mutableSetOf("Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat")

           holder.mediaSbar.progress= data[position].mbar
           holder.ringSbar.progress=data[position].rbar
           holder.alarmSbar.progress=data[position].abar
           holder.labelBtn.text=titlename
           holder.dontDisturbTB.isChecked=data[position].dontdisturb
           holder.airplaneTB.isChecked=data[position].airplane
           holder.vibrateSW.isChecked=data[position].vibrate
           holder.justVibrateSW.isChecked=data[position].just_vib
           holder.repeatCB.isChecked=data[position].repeat
           holder.itemView.findViewById<Switch>(R.id.setting_enable_sw).isChecked=data[position].setting_enabled
           holder.itemView.findViewById<CircularToggle>(R.id.sun).isChecked=data[position].sun
           holder.itemView.findViewById<CircularToggle>(R.id.mon).isChecked=data[position].mon
           holder.itemView.findViewById<CircularToggle>(R.id.tue).isChecked=data[position].tue
           holder.itemView.findViewById<CircularToggle>(R.id.wed).isChecked=data[position].wed
           holder.itemView.findViewById<CircularToggle>(R.id.thu).isChecked=data[position].thu
           holder.itemView.findViewById<CircularToggle>(R.id.fri).isChecked=data[position].fri
           holder.itemView.findViewById<CircularToggle>(R.id.sat).isChecked=data[position].sat
           holder.startBtn.text=data[position].s_time
           holder.endBtn.text=data[position].e_time

        
            Log.d("RPT", "setDay.bgin" + setDay.toString())
            if(data[position].sun&&data[position].mon&&data[position].tue&&data[position].wed&&data[position].thu&&data[position].fri&&data[position].sat) {
                setDay.addAll(setDayD)
                Log.d("RPT", "setDay.isEmpty" + setDay.toString())
            }

            val formatter = SimpleDateFormat("hh:mm a", Locale.ENGLISH).parse(data[position].e_time.toUpperCase())
            val cal3 = getInstance().also {
                it.set(HOUR, formatter.hours)
                it.set(MINUTE, formatter.minutes)
                it.set(AM_PM, if ("pm" in data[position].e_time.toUpperCase()){1}else{0})
            }
            val now=Calendar.getInstance()
            if(holder.itemView.findViewById<Switch>(R.id.setting_enable_sw).isChecked && cal3.before(now) && !holder.repeatCB.isChecked){
                holder.itemView.findViewById<Switch>(R.id.setting_enable_sw).isChecked=false
                holder.hiddenView.forEachChildView{  it.isEnabled = true  }
                holder.startBtn.text=data[position].s_time
                holder.endBtn.text=data[position].e_time
            }
            holder.itemView.findViewById<CardView>(R.id.base_cardview).forEachChildView{  it.isEnabled = setEnabled  }
            if(holder.itemView.findViewById<Switch>(R.id.setting_enable_sw).isChecked && setEnabled){
                holder.hiddenView.forEachChildView{  it.isEnabled = false  }
            }
        if(holder.justVibrateSW.isChecked){
            holder.mediaSbar.progress=0
            holder.ringSbar.progress=0
            holder.alarmSbar.progress=0
            holder.vibrateSW.isChecked=true
            holder.mediaSbar.isEnabled=false
            holder.ringSbar.isEnabled=false
            holder.alarmSbar.isEnabled=false
        }
///////////////////////////////////////////////////////<begin> enable custom scheduled setting ////////////////////////////////////////////////////////////////
        holder.itemView.findViewById<Switch>(R.id.setting_enable_sw).setOnCheckedChangeListener{ _, isChecked ->
            //Toast.makeText(context, " setting Turn " + if (isChecked) "ON" else "OFF", Toast.LENGTH_SHORT).show()
            
            var res=0
            db.updateDB(id1, "Setting", isChecked.toString())
            if(isChecked){
                res=runService(id1,holder)
                holder.hiddenView.forEachChildView{  it.isEnabled = false  }
            }else{
                stopService(id1)
                holder.hiddenView.forEachChildView{  it.isEnabled = true  }
            }
            if(res==-1) {
                db.updateDB(id1, "Setting", false.toString())
                holder.itemView.findViewById<Switch>(R.id.setting_enable_sw).isChecked=false
                holder.hiddenView.forEachChildView{  it.isEnabled = true  }
                stopService(id1)
            }
            ////db.close()
        }

/////////////////////////////////////////////////////// enable custom scheduled setting <end>////////////////////////////////////////////////////////////////
        // Name of the new listitem created
           holder.labelBtn.setOnClickListener{
               if(checkSystemDrawOverlay()) {
                   alertTextInput(holder, id1)
               } else{
                   it.snack("Permission Draw Overlay Not Granted!\nPlease Allow App Overlay Permission")
               }

           }

           //Start time selected
           holder.startBtn.setOnClickListener{
               val cal = getInstance()
                           val timeSetListener = TimePickerDialog.OnTimeSetListener { timePicker, hour, minute ->
                               cal.set(HOUR_OF_DAY, hour)
                               cal.set(MINUTE, minute)
                               holder.startBtn.text = SimpleDateFormat("hh:mm a").format(cal.time)

                               val res=db.updateDB(id1, "S_Time", holder.startBtn.text as String)
                           }
                           TimePickerDialog(
                                   holder.startBtn.rootView.context,
                                   timeSetListener,
                                   cal.get(HOUR_OF_DAY),
                                   cal.get(MINUTE),
                                   false
                           ).show()
               //db.close()

           }
           //end time selected
           holder.endBtn.setOnClickListener{
                val cal = getInstance()
                val timeSetListener = TimePickerDialog.OnTimeSetListener { timePicker, hour, minute ->
                    cal.set(HOUR_OF_DAY, hour)
                    cal.set(MINUTE, minute)

                    holder.endBtn.text = SimpleDateFormat("hh:mm a", Locale.ENGLISH).format(cal.time)
                    val res=db.updateDB(id1, "E_Time", holder.endBtn.text as String)
                }
                TimePickerDialog(
                        holder.startBtn.rootView.context,
                        timeSetListener,
                        cal.get(HOUR_OF_DAY),
                        cal.get(MINUTE),
                        false
                ).show()
                //db.close()
            }
////////////////////////////////////////////////<begins>Seekbar listners///////////////////////////////////////////////////////////////////////////////////
            holder.mediaSbar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                    //Toast.makeText(context, "seekbar progress: $progress", Toast.LENGTH_SHORT).show()
                }

                override fun onStartTrackingTouch(seekBar: SeekBar) {
                    //Toast.makeText(context, "seekbar touch started!", Toast.LENGTH_SHORT).show()
                }

                override fun onStopTrackingTouch(seekBar: SeekBar) {
                    val res = db.updateDB(id1, "MBar", seekBar.progress.toString())
                    //db.close()
                }
            })

            holder.ringSbar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(
                        seekBar: SeekBar, progress: Int,
                        fromUser: Boolean
                ) {
                    //Toast.makeText(context, "seekbar progress: $progress", Toast.LENGTH_SHORT).show()
                }

                override fun onStartTrackingTouch(seekBar: SeekBar) {
                   // Toast.makeText(context, "seekbar touch started!", Toast.LENGTH_SHORT).show()
                }

                override fun onStopTrackingTouch(seekBar: SeekBar) {
                    val res = db.updateDB(id1, "RBar", seekBar.progress.toString())
                    //db.close()
                   // Toast.makeText(context, "seekbar touch stopped! at " + seekBar.progress, Toast.LENGTH_SHORT).show()

                }
            })

            holder.alarmSbar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(
                        seekBar: SeekBar, progress: Int,
                        fromUser: Boolean
                ) {
                    //Toast.makeText(context, "seekbar progress: $progress", Toast.LENGTH_SHORT).show()
                }

                override fun onStartTrackingTouch(seekBar: SeekBar) {
                    //Toast.makeText(context, "seekbar touch started!", Toast.LENGTH_SHORT).show()
                }

                override fun onStopTrackingTouch(seekBar: SeekBar) {
                    val res = db.updateDB(id1, "ABar", seekBar.progress.toString())
                    //db.close()
                   //Toast.makeText( context, "seekbar touch stopped! at " + seekBar.progress, Toast.LENGTH_SHORT).show()

                }
            })
///////////////////////////////////////////////////////Seekbar listner <ends>////////////////////////////////////////////////////////////////////////
//////////////////////////////////////////////////////<begin>Toggle Button lisntner///////////////////////////////////////////////////////////////
        holder.dontDisturbTB.setOnCheckedChangeListener{ _, isChecked ->

            //toast("DONOT DISTURB is turned " + if (isChecked) "ON" else "OFF")
            if (checkNotificationPolicyAccess(holder.dontDisturbTB,notificationManager)) {
                val res = db.updateDB(id1, "DontDisturb", isChecked.toString())
                //db.close()
            }
            if(isChecked) {
                holder.dontDisturbTB.setBackgroundResource(R.drawable.round_button_on)
            } else {
                holder.dontDisturbTB.setBackgroundResource(R.drawable.round_button)
            }
        }
        holder.airplaneTB.setOnCheckedChangeListener{ _, isChecked ->

            //toast("Airplane Mode is turned " + if (isChecked) "ON" else "OFF")
            val res=db.updateDB(id1, "Airplane", isChecked.toString())
            //db.close()
        }
//////////////////////////////////////////////////////Toggle Button lisntner<end>///////////////////////////////////////////////////////////////
//////////////////////////////////////////////////////<begin>switch Button lisntner///////////////////////////////////////////////////////////////////
        holder.vibrateSW.setOnCheckedChangeListener{ _, isChecked ->
            try {
                if (checkSystemWritePermission()) {
                    //Settings.System.putInt(context.getContentResolver(), Settings.System.VIBRATE_WHEN_RINGING, 1)
                    //Toast.makeText(context, "Set as ringtoon successfully ", Toast.LENGTH_SHORT).show()
                    //toast("vibrate Mode is turned " + if (isChecked) "ON" else "OFF")

                    val res=db.updateDB(id1, "Vibrate", isChecked.toString())
                } else {
                    //Toast.makeText(context, "Allow modify system settings ==> ON ", Toast.LENGTH_LONG).show()
                    holder.vibrateSW.snack("Allow modify system settings ==> ON ")
                    holder.vibrateSW.isChecked=false
                    val res=db.updateDB(0, "Vibrate", false.toString())
                }
                //db.close()
            } catch (e: Exception) {
                Log.i("MyAlarmReceiver", e.toString())
                //.makeText(context, "unable to set as Ringtoon ", Toast.LENGTH_SHORT).show()
                holder.vibrateSW.snack("unable to set as Ringtoon ")
            }

        }
        holder.justVibrateSW.setOnCheckedChangeListener{ _, isChecked ->
            //("justVibrateSW Mode is turned " )
            val res=db.updateDB(id1, "JustVibrate", isChecked.toString())
            if(isChecked){
                holder.mediaSbar.progress=0
                holder.ringSbar.progress=0
                holder.alarmSbar.progress=0
                holder.vibrateSW.isChecked=true
                holder.mediaSbar.isEnabled=false
                holder.ringSbar.isEnabled=false
                holder.alarmSbar.isEnabled=false
            }else{
                holder.mediaSbar.progress=7
                holder.ringSbar.progress=4
                holder.alarmSbar.progress=4
                holder.mediaSbar.isEnabled=true
                holder.ringSbar.isEnabled=true
                holder.alarmSbar.isEnabled=true
            }
            //db.close()
        }

//////////////////////////////////////////////////////switch Button lisntner<end>///////////////////////////////////////////////////////////////////
//////////////////////////////////////////////////////<begin>checkbox lisntner///////////////////////////////////////////////////////////////////
        if(!(holder.repeatCB.isChecked)){
            TransitionManager.beginDelayedTransition(
                    holder.cardview,
                    AutoTransition()
            )
            holder.hiddenViewWeek.visibility = View.GONE
        }
        holder.repeatCB.setOnCheckedChangeListener { _, isChecked ->
            val res = db.updateDB(id1, "Repeat", isChecked.toString())
            Log.d("MainActivity","Repeat updatd to db")
            //toast("Repeat Mode is turned" + if (isChecked) "ON" else "OFF")
            if (isChecked) {
                TransitionManager.beginDelayedTransition(
                        holder.cardview,
                        AutoTransition()
                )
                if(setDay.isEmpty()){
                    setDay.add("Sun")
                }
                holder.hiddenViewWeek.visibility = View.VISIBLE
            } else {
                TransitionManager.beginDelayedTransition(
                        holder.cardview,
                        AutoTransition()
                )
                holder.hiddenViewWeek.visibility = View.GONE
                dayS="Today"
            }
            //db.close()
        }

        holder.multiTB.setOnCheckedChangeListener { group, checkedId, isChecked ->

            fun checkState():Int{
                if (setDay.isEmpty()){
                    holder.repeatCB.isChecked=false
                    return -1
                }
                return 0
            }

            //var weekDay = arrayOf(false,false,false,false,false,false,false)
            //println("group.siz " + group.size)
            when(checkedId){
                R.id.sun -> {
                    //toast("sun is turnd " + if (isChecked) "ON" else "OFF")
                    if (isChecked) setDay.add("Sun") else setDay.remove("Sun")
                    if (checkState() == -1) {
                        db.updateDB(id1, "Sunday", true.toString())
                        holder.itemView.findViewById<CircularToggle>(R.id.sun).isChecked = true
                        setDay.add("Sun")
                        Log.d("RPT","setDay.Sun.isEmpty"+setDay.toString())
                    } else {
                        //weekDay[0]=isChecked
                        val res = db.updateDB(id1, "Sunday", isChecked.toString())
                    }
                    Log.d("RPT","setDay.Sun"+setDay.toString())
                }
                R.id.mon -> {
                    //toast("Mon is turnd " + if (isChecked) "ON" else "OFF")
                    if (isChecked) setDay.add("Mon") else setDay.remove("Mon")
                    if (checkState() == -1) {
                        db.updateDB(id1, "Monday", true.toString())
                        holder.itemView.findViewById<CircularToggle>(R.id.mon).isChecked = true
                        setDay.add("Mon")
                    } else {
                        val res = db.updateDB(id1, "Monday", isChecked.toString())
                    }
                }
                R.id.tue -> {
                    //toast("tue is turnd " + if (isChecked) "ON" else "OFF")
                    if (isChecked) setDay.add("Tue") else setDay.remove("Tue")
                    if (checkState() == -1) {
                        db.updateDB(id1, "Tuesday", true.toString())
                        holder.itemView.findViewById<CircularToggle>(R.id.tue).isChecked = true
                        setDay.add("Tue")
                    } else {
                        val res = db.updateDB(id1, "Tuesday", isChecked.toString())
                    }
                }
                R.id.wed -> {
                    //toast("wed is turnd " + if (isChecked) "ON" else "OFF")
                    if (isChecked) setDay.add("Wed") else setDay.remove("Wed")
                    if (checkState() == -1) {
                        db.updateDB(id1, "Wednesday", true.toString())
                        holder.itemView.findViewById<CircularToggle>(R.id.wed).isChecked = true
                        setDay.add("Wed")
                    } else {
                        val res = db.updateDB(id1, "Wednesday", isChecked.toString())
                    }
                }
                R.id.thu -> {
                    //toast("thu is turnd " + if (isChecked) "ON" else "OFF")
                    if (isChecked) setDay.add("Thu") else setDay.remove("Thu")
                    if (checkState() == -1) {
                        db.updateDB(id1, "Thursday", true.toString())
                        holder.itemView.findViewById<CircularToggle>(R.id.thu).isChecked = true
                        setDay.add("Thu")
                    } else {
                        val res = db.updateDB(id1, "Thursday", isChecked.toString())
                    }
                }
                R.id.fri -> {
                    //toast("Fri is turnd " + if (isChecked) "ON" else "OFF")
                    if (isChecked) setDay.add("Fri") else setDay.remove("Fri")
                    if (checkState() == -1) {
                        db.updateDB(id1, "Friday", true.toString())
                        holder.itemView.findViewById<CircularToggle>(R.id.fri).isChecked = true
                        setDay.add("Fri")
                    } else {
                        val res = db.updateDB(id1, "Friday", isChecked.toString())
                    }
                }
                R.id.sat -> {
                    //toast("sat is turnd " + if (isChecked) "ON" else "OFF")
                    if (isChecked) setDay.add("Sat") else setDay.remove("Sat")
                    if (checkState() == -1) {
                        db.updateDB(id1, "Saterday", true.toString())
                        holder.itemView.findViewById<CircularToggle>(R.id.sat).isChecked = true
                        setDay.add("Sat")
                    } else {
                        val res = db.updateDB(id1, "Saterday", isChecked.toString())
                    }
                }

            }
            //db.close()
            dayS=setDay.joinToString(" ")
        }
//////////////////////////////////////////////////////checkbox lisntner<end>///////////////////////////////////////////////////////////////////

        //Deleting the listitem created
           holder.del1.setOnClickListener{

            val item=db.delete_data(id1.toString())
              // Toast.makeText(context,"deleting id "+id,Toast.LENGTH_SHORT).show()
            removeData(position, data)
            notifyItemRemoved(position)
               //db.close()
            //notifyDataSetChanged()
           }
        holder.arrow.setOnClickListener {

            if (holder.repeatCB.isChecked) {
                if (setDay.containsAll(listOf("Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat"))) {
                    dayS = " Every day"
                } else if (setDay.isEmpty()) {
                    dayS = " Today"
                } else if (setDay.containsAll(listOf("Mon", "Tue", "Wed", "Thu", "Fri")) && !(setDay.contains("Sun") || setDay.contains("Sat"))) {
                    dayS = " Weekdays"
                } else if (setDay.containsAll(listOf("Sun", "Sat")) && Collections.disjoint(setDay, listOf("Mon", "Tue", "Wed", "Thu", "Fri"))) {
                    dayS = " Weekend"
                } else {
                    dayS = setDay.joinToString(" ")
                }
            } else if (!(holder.itemView.findViewById<Switch>(R.id.setting_enable_sw).isChecked)) {
                dayS = "Tomorrow"
            } else {
                dayS = "Today"
            }
            db.updateDB(id1, "Day", dayS)
            transitionHV(id1, holder,dayS)
        }
        if(data[position].arrow) transitionHV(id1, holder,dayS)

    }
    private fun transitionHV(id1: Int, holder: ViewHolder,dayS:String="Tommorow"){

        if (holder.hiddenView.visibility == View.VISIBLE) {

            db.updateDB(id1, "Arrow", true.toString())
            // The transition of the hiddenView is carried out
            //  by the TransitionManager class.
            // Here we use an object of the AutoTransition
            // Class to create a default transition.
            TransitionManager.beginDelayedTransition(
                    holder.cardview,
                    AutoTransition()
            )
            holder.hiddenView.visibility = View.GONE
            holder.del1.visibility = View.GONE
            holder.daySet.visibility = View.VISIBLE
            holder.daySet.text=dayS

            val param = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT)
            param.marginEnd=250
            param.marginStart=10
            param.weight=1f
            holder.daySet.layoutParams = param
            val param2 =  LinearLayout.LayoutParams(0, 85)
            param2.weight=0.2f
            param2.marginEnd=5
            param2.topMargin=10
            param2.bottomMargin=5
            holder.arrow.layoutParams=param2
            holder.arrow.setImageResource(R.drawable.ic_baseline_expand_more_24)
        }
        // If the CardView is not expanded, set its visibility
        // to visible and change the expand more icon to expand less.
        else {
            db.updateDB(id1, "Arrow", false.toString())
            TransitionManager.beginDelayedTransition(
                    holder.cardview,
                    AutoTransition()
            )
            holder.hiddenView.visibility = View.VISIBLE
            holder.del1.visibility = View.VISIBLE
            holder.daySet.visibility = View.GONE
            val param2 =  LinearLayout.LayoutParams(0, 85)
            param2.weight=2f
            holder.arrow.layoutParams=param2
            holder.arrow.setImageResource(R.drawable.ic_baseline_expand_less_24)
        }
        //db.close()
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        lifecycleRegistry = LifecycleRegistry(this)
        lifecycleRegistry.markState(Lifecycle.State.CREATED)
        var layout=LayoutInflater.from(parent.context).inflate(R.layout.schedule_items, parent, false)
        return ViewHolder(layout)
    }

    override fun getItemCount(): Int {
        return data.size
    }

    private fun removeData(position: Int, data: ArrayList<custom_class>){
        data.removeAt(position)
        notifyItemRemoved(position)
        notifyDataSetChanged()
    }

    private fun alertTextInput(holder: ViewHolder, id1: Int) {


            val alert = Dialog(this.context)
            //alert.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT));
            alert.window?.setDimAmount(2F);
            alert.window?.clearFlags(FLAG_DIM_BEHIND)
            alert.requestWindowFeature(Window.FEATURE_NO_TITLE)
            alert.setContentView(R.layout.alert_edittext)
            alert.window?.setType(LAYOUT_FLAG)

            val name = alert.findViewById<TextInputEditText>(R.id.text_input)
            val accept = alert.findViewById<Button>(R.id.accept)
            val cancel = alert.findViewById<Button>(R.id.cancel)

            cancel.setOnClickListener {
                alert.dismiss()
            }
            accept.setOnClickListener {
                var titlename=name.text.toString()
                holder.labelBtn.text = titlename
                val res=db.updateDB(id1, "title", titlename)
                alert.dismiss()
            }

            alert.show()
    }


    private fun runService(id1: Int,  holder: ViewHolder) : Int{
        cur=db.data_geter

        var res=0
        var media_sb0=0 ;var ring_sb0=0;var alarm_sb0=0;var vibrate0:Boolean=false;
        var media_sb=0 ;var ring_sb=0;var alarm_sb=0

        var s_time:String="";var e_time:String=""
        var dnd:Boolean=false;var airplane:Boolean=false;
        var vibrate:Boolean=false;var just_vib:Boolean=false
        var repeat:Boolean=false;var sun=false;var mon=false;var tue=false;var wed=false;var thu=false;var fri=false;var sat=false;

        while (cur.moveToNext()){
            if(cur.getInt(1)==0) {
                media_sb0=cur.getInt(3)
                ring_sb0=cur.getInt(4)
                alarm_sb0=cur.getInt(5)
                vibrate0 = cur.getInt(8) != 0
            } else if(cur.getInt(1)==id1){
                media_sb=cur.getInt(3)
                ring_sb=cur.getInt(4)
                alarm_sb=cur.getInt(5)
                s_time=cur.getString(19)
                e_time=cur.getString(20)
                dnd=cur.getInt(6)!= 0
                airplane=cur.getInt(7)!= 0
                vibrate = cur.getInt(8) != 0
                just_vib = cur.getInt(9) != 0
                repeat = cur.getInt(10) != 0
                sun = cur.getInt(11) != 0
                mon = cur.getInt(12) != 0
                tue = cur.getInt(13) != 0
                wed = cur.getInt(14) != 0
                thu = cur.getInt(15) != 0
                fri = cur.getInt(16) != 0
                sat = cur.getInt(17) != 0
                break
            }

        }

        var am_pm= if ("pm" in s_time.toUpperCase()){1}else{0}
        val formatter = SimpleDateFormat("hh:mm a", Locale.ENGLISH).parse(s_time.toUpperCase())
        val cal1 = getInstance().also {
            it.set(HOUR, formatter.hours)
            it.set(MINUTE, formatter.minutes)
            it.set(AM_PM, am_pm)
        }


        var am_pm_2= if ("pm" in e_time.toUpperCase()){1}else{0}
        val formatter_2 = SimpleDateFormat("hh:mm a", Locale.ENGLISH).parse(e_time.toUpperCase())
        val cal2 = getInstance().also {
            it.set(HOUR, formatter_2.hours)
            it.set(MINUTE, formatter_2.minutes)
            it.set(AM_PM, am_pm_2)
        }

        if(just_vib){ media_sb=0; ring_sb=0; alarm_sb=0; vibrate=true }


        val intent = Intent(context, MyAlarmReceiver::class.java)
       // intent.action="com.project.action.ALARM"
        intent.putExtra("Media", media_sb)
        intent.putExtra("Ring", ring_sb)
        intent.putExtra("Alarm", alarm_sb)
        intent.putExtra("DND", dnd)
        intent.putExtra("Airplane", airplane)
        intent.putExtra("Vibrate", vibrate)
        intent.putExtra("E_Time",e_time)
        //intent.putExtra("JustVib", just_vib)



        val intent2 = Intent(context, MyAlarmReceiver::class.java)
       // intent2.action="com.project.action.ALARM"
        intent2.putExtra("Media", media_sb0)
        intent2.putExtra("Ring", ring_sb0)
        intent2.putExtra("Alarm", alarm_sb0)
        intent2.putExtra("DND", false)
        intent2.putExtra("Airplane", false)
        intent2.putExtra("Vibrate", vibrate0)
        intent2.putExtra("E_Time","")
        //intent2.putExtra("JustVib", false)


        val now = getInstance()
        if(repeat) {
            var res1=0
            if (sun) res1=setAlarm(Calendar.SUNDAY, id1, cal1, cal2, intent, intent2,holder)
            if (mon) res1=setAlarm(Calendar.MONDAY, id1, cal1, cal2, intent, intent2,holder)
            if (tue) res1=setAlarm(Calendar.TUESDAY, id1, cal1, cal2, intent, intent2,holder)
            if (wed) res1=setAlarm(Calendar.WEDNESDAY, id1, cal1, cal2, intent, intent2,holder)
            if (thu) res1=setAlarm(Calendar.THURSDAY, id1, cal1, cal2, intent, intent2,holder)
            if (fri) res1=setAlarm(Calendar.FRIDAY, id1, cal1, cal2, intent, intent2,holder)
            if (sat) res1=setAlarm(Calendar.SATURDAY, id1, cal1, cal2, intent, intent2,holder)
            if(res1==-1) return -1
        }else{


            if(cal2.before(cal1)){
                holder.itemView.findViewById<Switch>(R.id.setting_enable_sw).snack("Start time should proceed end time \nCannot set scheduled task")
                return -1
            }
            if (!(now.before(cal1)) ) {
                cal1.add(Calendar.DATE, 1)
                db.updateDB(id1, "Day", "Tomorrow")
            }

            if (now.before(cal2) && !(now.before(cal1))) {
                // it's not 14:00 yet, start today
                   // println("now.before(cal2) && !(now.before(cal1)")
                cal2.add(Calendar.DATE, 1)
            }else if (!(now.before(cal2) )){
                cal2.add(Calendar.DATE, 1)
            }
            val pIntent = PendingIntent.getBroadcast(context, id1, intent, PendingIntent.FLAG_UPDATE_CURRENT)
            Log.d("MyAlarmReceiver", " ID  " + id1.toString() + " \tcreate :" + Date().toString())
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, cal1.timeInMillis, pIntent)

            val pIntent2 = PendingIntent.getBroadcast(context, id1 * MyAlarmReceiver.REQUEST_CODE, intent2, PendingIntent.FLAG_UPDATE_CURRENT)
            Log.d("MyAlarmReceiver", " ID " + (id1 * MyAlarmReceiver.REQUEST_CODE).toString() + " \tcreate :" + Date().toString())
            alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, cal2.timeInMillis, pIntent2)

        }

    return 0
    }

    private fun stopService(id1: Int): Int{
        cur=db.data_geter
        var repeat:Boolean=false;var sun=false;var mon=false;var tue=false;var wed=false;var thu=false;var fri=false;var sat=false;

        while (cur.moveToNext()){
            if(cur.getInt(1)==id1){
                repeat = cur.getInt(10) != 0
                sun = cur.getInt(11) != 0
                mon = cur.getInt(12) != 0
                tue = cur.getInt(13) != 0
                wed = cur.getInt(14) != 0
                thu = cur.getInt(15) != 0
                fri = cur.getInt(16) != 0
                sat = cur.getInt(17) != 0
                break
            }
        }
        if(repeat) {
            if (sun) cancelAlarm(Calendar.SUNDAY, id1)
            if (mon) cancelAlarm(Calendar.MONDAY, id1)
            if (tue) cancelAlarm(Calendar.TUESDAY, id1)
            if (wed) cancelAlarm(Calendar.WEDNESDAY, id1)
            if (thu) cancelAlarm(Calendar.THURSDAY, id1)
            if (fri) cancelAlarm(Calendar.FRIDAY, id1)
            if (sat) cancelAlarm(Calendar.SATURDAY, id1)
        }else {
            alarmManager.cancel(
                    PendingIntent.getBroadcast(
                            context, id1, Intent(context, MyAlarmReceiver::class.java), 0))
            alarmManager.cancel(
                    PendingIntent.getBroadcast(
                            context, id1 * MyAlarmReceiver.REQUEST_CODE, Intent(context, MyAlarmReceiver::class.java), 0))
            Log.d("MyAlarmReceiver","\n  canceling alarmManager srvics \n")
        }

        return 0
    }


    private fun setAlarm(weekno: Int, id1: Int, cal1: Calendar, cal2: Calendar, intent: Intent, intent2: Intent, holder:ViewHolder) :Int{
        var rc1 = 0; var rc2 = 0
        rc1 = id1 * id1 * (weekno *2)
        rc2 = id1 * id1 * MyAlarmReceiver.REQUEST_CODE * (weekno *2)
        val calendar1:Calendar =cal1.clone() as Calendar
        val calendar2=cal2.clone() as Calendar
        calendar1.set(DAY_OF_WEEK, weekno)
        calendar2.set(DAY_OF_WEEK, weekno)

        // Check we aren't setting it in the past which would trigger it to fire instantly
        val now = getInstance()
        if (!(now.before(calendar1))) {
            // it's not 14:00 yet, start today
            calendar1.add(Calendar.DAY_OF_YEAR, 7);
        }
        if (!(now.before(calendar2))) {
            // it's not 14:00 yet, start today
            calendar2.add(Calendar.DAY_OF_YEAR, 7);
        }
        if(calendar2.before(calendar1)){
            holder.itemView.findViewById<Switch>(R.id.setting_enable_sw).snack("Start time should proceed end time \nCannot set scheduled task")
            return -1
        }



        Log.d("MyAlarmReceiver", "rc1 :" + rc1)
        var pIntent = PendingIntent.getBroadcast(context, rc1, intent, PendingIntent.FLAG_UPDATE_CURRENT)
        Log.d("MyAlarmReceiver", " ID  " + rc1.toString() + " \tcreate :" + Date().toString())
        alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, calendar1.timeInMillis, AlarmManager.INTERVAL_DAY * 7, pIntent)
        //alarmManager.setInexactRepeating(AlarmManager.RTC_WAKEUP, cal1.timeInMillis, AlarmManager.INTERVAL_DAY * 7, pIntent)

        var pIntent2 = PendingIntent.getBroadcast(context, rc2, intent2, PendingIntent.FLAG_UPDATE_CURRENT)
        Log.d("MyAlarmReceiver", " ID " + rc2.toString() + " \tcreate :" + Date().toString())
        alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, calendar2.timeInMillis, AlarmManager.INTERVAL_DAY * 7, pIntent2)

        println("now\t:"+now.time)
        println("cal1\t:"+cal1.time)
        println("cal2\t:"+cal2.time)
        println("calendar1\t:"+calendar1.time)
        println("calendar2\t:"+calendar2.time)
        println("weekno\t:"+weekno)
        return 0
    }

    private fun cancelAlarm(weekno: Int, id1: Int){
        var rc1 = 0; var rc2 = 0
        rc1 = id1 * id1 * (weekno *2)
        rc2 = id1 * id1 * MyAlarmReceiver.REQUEST_CODE * (weekno *2)
        alarmManager.cancel(
                PendingIntent.getBroadcast(
                        context, rc1, Intent(context, MyAlarmReceiver::class.java), 0))
        alarmManager.cancel(
                PendingIntent.getBroadcast(
                        context, rc2, Intent(context, MyAlarmReceiver::class.java), 0))
        print("\n  canceling alarmManager srvics for weekno :$weekno  \n")
    }

    private fun checkSystemDrawOverlay(): Boolean {

        if (Settings.canDrawOverlays(context)) {
            return true
        }else {
            val myIntent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION)
            myIntent.setData(Uri.parse("package:" + context.packageName))
            myIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(myIntent)
        }

        return false
    }
    private fun checkSystemWritePermission(): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (Settings.System.canWrite(context)){
                return true
            } else {
                openAndroidPermissionsMenu()
            }
        }
        return false
    }

    private fun openAndroidPermissionsMenu() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val intent = Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS)
            intent.data = Uri.parse("package:" + context.packageName)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent)
        }
    }
    private fun checkNotificationPolicyAccess(view: View,notificationManager: NotificationManager):Boolean{
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (notificationManager.isNotificationPolicyAccessGranted){
                //toast("Notification policy access granted.")
                return true
            }else{
                view.snack("You need to grant notification policy access.")
                //("You need to grant notification policy access.")
                // If notification policy access not granted for this package
                val intent = Intent(Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS)
                //intent.data = Uri.parse("package:" + context.packageName)
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(intent)
            }
        }else{
            toast("Device does not support this feature.")
        }
        return false
    }

    fun toast(message: String) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }
    fun View.snack(message: String, duration: Int = Snackbar.LENGTH_LONG) {
        Snackbar.make(this, message, duration).show()
    }
    class ViewHolder(itemView: View):RecyclerView.ViewHolder(itemView){


        //val id:String
        val cardview:CardView
        val arrow:ImageButton
        val hiddenView:LinearLayout
        val hiddenViewWeek:LinearLayout
        val labelBtn:Button
        val del1:Button
        val startBtn:Button
        val endBtn:Button
        val mediaSbar:SeekBar
        val ringSbar:SeekBar
        val alarmSbar:SeekBar
        val dontDisturbTB:ToggleButton
        val airplaneTB:ToggleButton
        val vibrateSW:Switch
        val justVibrateSW:Switch
        val repeatCB:CheckBox
        val daySet:TextView
        val multiTB:MultiSelectToggleGroup


        init {

            //id="1"//itemView.findViewById(R.id.id_tv)
            del1 = itemView.findViewById(R.id.del_btn)
            labelBtn=itemView.findViewById(R.id.label_btn)
            startBtn=itemView.findViewById(R.id.start_btn)
            endBtn=itemView.findViewById(R.id.end_btn)
            mediaSbar=itemView.findViewById(R.id.media_sbar)
            ringSbar=itemView.findViewById(R.id.ring_sbar)
            alarmSbar=itemView.findViewById(R.id.alarm_sbar)
            dontDisturbTB=itemView.findViewById(R.id.donot_disturb_tb)
            airplaneTB=itemView.findViewById(R.id.airplane_tb)
            vibrateSW=itemView.findViewById(R.id.vibrate_sw)
            justVibrateSW=itemView.findViewById(R.id.just_vibrate_sw)
            repeatCB=itemView.findViewById(R.id.repeat_cb)
            cardview = itemView.findViewById(R.id.base_cardview)
            arrow = itemView.findViewById(R.id.arrow_button)
            hiddenView = itemView.findViewById(R.id.hidden_view)
            hiddenViewWeek = itemView.findViewById(R.id.hiddenView_week)
            daySet=itemView.findViewById(R.id.day_tv)

            multiTB = itemView.findViewById(R.id.group_weekdays)
        }

    }

    override fun getLifecycle(): Lifecycle {
        println("\n \n \n getLifecyclen \n\n\n")
        return lifecycleRegistry

    }

}
