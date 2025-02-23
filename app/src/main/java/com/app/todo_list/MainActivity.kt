package com.app.todo_list

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.DatePickerDialog
import android.app.Dialog
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.Rect
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.util.Log
import android.view.View
import android.view.WindowInsets
import android.view.WindowInsetsController
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.SearchView
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.getbase.floatingactionbutton.FloatingActionButton
import com.google.android.material.bottomnavigation.BottomNavigationView
import java.text.ParseException
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Calendar
import java.util.Date
import java.util.Locale

class MainActivity : AppCompatActivity() {

    private lateinit var txtCurrentTime: TextView
    private lateinit var txtCurrentDate: TextView
    private lateinit var txtCurrentView: TextView
    private lateinit var fabMain: FloatingActionButton
    private lateinit var linearLayout: LinearLayout
    private lateinit var searchView: SearchView
    private lateinit var recyclerView: RecyclerView
    private lateinit var bottomNavigationView: BottomNavigationView
    private lateinit var noteAdapter: NoteAdapter
    private lateinit var taskAdapter: TaskAdapter
    private lateinit var notesList: MutableList<Note>
    private lateinit var tasksList: MutableList<Task>
    private lateinit var db: Database

    private var isNotesSelected: Boolean = true
    private var isFabMenuOpen = false
    private val handler = Handler(Looper.getMainLooper())

    @RequiresApi(Build.VERSION_CODES.S)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        hideSystemUI()

        db = Database(this)
        txtCurrentDate = findViewById(R.id.txtCurrentDate)
        txtCurrentTime = findViewById(R.id.txtCurrentTime)
        txtCurrentView = findViewById(R.id.txtCurrentView)
        searchView = findViewById(R.id.searchView)

        updateDate()
        updateTime()

        createNotificationChannel()
        checkAndRequestExactAlarmPermission()
        setupRecyclerView()
        setupFabListeners()
        setupDesign()
        setupOnBackPressedDispatcher()
        setupSearch()
        setupBottomNavigationView()
    }

    private fun onFabNotesClick() {
        val intent = Intent(this, ModifyNote::class.java)
        startActivity(intent)
        finish()
    }

    private fun onFabTasksClick() {
        displayPopupDialog(null)
    }

    private fun setupSearch() {
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                if (isNotesSelected) {
                    noteAdapter.filter(newText.orEmpty())
                } else {
                    taskAdapter.filter(newText.orEmpty())
                }
                return true
            }
        })
    }

    private fun setupDesign() {
        Glide.with(this)
            .asGif()
            .load(R.drawable.silve_wolf_honkai)
            .into(findViewById(R.id.imgSilverWolf))
    }

    private fun setupRecyclerView() {
        recyclerView = findViewById(R.id.recyclerNotesTasks)
        recyclerView.layoutManager = LinearLayoutManager(this)
    }

    private fun setupBottomNavigationView() {
        bottomNavigationView = findViewById(R.id.bottomNavigationView)
        bottomNavigationView.setOnNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.navigation_notes -> {
                    txtCurrentView.text = "Notes"
                    searchView.queryHint = "Search notes..."
                    isNotesSelected = true
                    switchRecyclerViewContent(isNotes = true)
                    true
                }
                R.id.navigation_tasks -> {
                    txtCurrentView.text = "Tasks"
                    searchView.queryHint = "Search tasks..."
                    isNotesSelected = false
                    switchRecyclerViewContent(isNotes = false)
                    true
                }
                else -> false
            }
        }

        bottomNavigationView.selectedItemId = R.id.navigation_notes
    }

    private fun switchRecyclerViewContent(isNotes: Boolean) {
        if (isNotes) {
            notesList = getAllNotesFromDb()
            noteAdapter = NoteAdapter(notesList) { note ->
                onNoteItemClick(note)
            }
            recyclerView.adapter = noteAdapter
        } else {
            tasksList = getAllTasksFromDb()
            taskAdapter = TaskAdapter(tasksList) { task ->
                onTaskItemClick(task)
            }
            recyclerView.adapter = taskAdapter
        }
    }


    private fun setupFabListeners() {
        fabMain = findViewById(R.id.fab_main)
        val fabContainer = findViewById<LinearLayout>(R.id.fab_container)

        fabMain.setOnClickListener {
            if (isFabMenuOpen) {
                closeFabMenu(fabContainer)
                rotateFab(fabMain, 0f)
            } else {
                openFabMenu(fabContainer)
                rotateFab(fabMain, -45f)
            }
            isFabMenuOpen = !isFabMenuOpen
        }

        findViewById<FloatingActionButton>(R.id.fab_notes).setOnClickListener {
            onFabNotesClick()
        }

        findViewById<FloatingActionButton>(R.id.fab_tasks).setOnClickListener {
            onFabTasksClick()
        }
    }

    private fun getAllNotesFromDb(): MutableList<Note> {
        val notes = mutableListOf<Note>()
        val cursor = db.getAllNotes()
        if (cursor.moveToFirst()) {
            do {
                val id = cursor.getInt(cursor.getColumnIndexOrThrow("id"))
                val title = cursor.getString(cursor.getColumnIndexOrThrow("title"))
                val content = cursor.getString(cursor.getColumnIndexOrThrow("content"))
                notes.add(Note(id, title, content))
            } while (cursor.moveToNext())
        }
        cursor.close()
        return notes
    }

    private fun getAllTasksFromDb(): MutableList<Task> {
        val tasks = mutableListOf<Task>()
        val cursor = db.getAllTasks()
        if (cursor.moveToFirst()) {
            do {
                val id = cursor.getInt(cursor.getColumnIndexOrThrow("id"))
                val startDate = cursor.getString(cursor.getColumnIndexOrThrow("start_date"))
                val endDate = cursor.getString(cursor.getColumnIndexOrThrow("end_date"))
                val title = cursor.getString(cursor.getColumnIndexOrThrow("title"))
                val isChecked = cursor.getInt(cursor.getColumnIndexOrThrow("is_checked")) == 1
                tasks.add(Task(id, startDate, endDate, title, isChecked))

                val task = Task(id, startDate, endDate, title, isChecked)
                if (!task.isChecked) {
                    scheduleNotification(task, this)
                }

            } while (cursor.moveToNext())
        }
        cursor.close()
        return tasks
    }

    private fun scheduleNotification(task: Task, context: Context) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        val intent = Intent(context, Notification::class.java).apply {
            putExtra("title", task.title)
            putExtra("content", "Your task '${task.title}' is due tomorrow.")
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            task.id,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val endDate: Date? = try {
            SimpleDateFormat("dd MMMM yyyy", Locale.getDefault()).parse(task.endDate)
        } catch (e: ParseException) {
            null
        }

        if (endDate != null) {
            val calendar = Calendar.getInstance().apply {
                timeInMillis = endDate.time
                add(Calendar.DAY_OF_YEAR, -1)
                set(Calendar.HOUR_OF_DAY, 23)
                set(Calendar.MINUTE, 59)
                set(Calendar.SECOND, 0)
            }

            val scheduledTime = SimpleDateFormat("dd MMMM yyyy HH:mm:ss", Locale.getDefault()).format(calendar.time)
            Log.i("Tasks", "Notification scheduled for task '${task.title}' at $scheduledTime")

            try {
                alarmManager.setExact(AlarmManager.RTC_WAKEUP, calendar.timeInMillis, pendingIntent)
            } catch (e: SecurityException) {
                Log.e("Tasks", "Cannot schedule exact alarm: ${e.message}")
            }
        } else {
            Log.e("Tasks", "Invalid end date format for task '${task.title}', notification not scheduled.")
        }
    }


    @RequiresApi(Build.VERSION_CODES.S)
    private fun checkAndRequestExactAlarmPermission() {
        val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        if (!alarmManager.canScheduleExactAlarms()) {
            val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM)
            startActivity(intent)
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun createNotificationChannel() {
        val channelId = "channelOne"
        val channelName = "Task Notifications"
        val channelDescription = "Notifications for task reminders."

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channel = NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_HIGH).apply {
            description = channelDescription
        }

        notificationManager.createNotificationChannel(channel)
    }

    private fun onNoteItemClick(note: Note) {
        val intent = Intent(this, ModifyNote::class.java).apply {
            putExtra("noteId", note.id)
            putExtra("noteTitle", note.title)
            putExtra("noteContent", note.content)
        }
        startActivity(intent)
    }

    private fun onTaskItemClick(task: Task) {
        displayPopupDialog(task)
    }

    @SuppressLint("NewApi")
    private fun updateTime() {
        handler.postDelayed({
            val currentTime = LocalTime.now()
            val formatter = DateTimeFormatter.ofPattern("HH:mm:ss")
            val formattedTime = currentTime.format(formatter)
            txtCurrentTime.text = formattedTime
            updateTime()
        }, 1000)
    }

    @SuppressLint("NewApi")
    private fun updateDate() {
        val currentDate = LocalDate.now()
        val formatter = DateTimeFormatter.ofPattern("dd MMMM yyyy")
        val formattedDate = currentDate.format(formatter)
        txtCurrentDate.text = formattedDate
    }

    private fun hideSystemUI() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.insetsController?.let { controller ->
                controller.hide(WindowInsets.Type.statusBars() or WindowInsets.Type.navigationBars())
                controller.systemBarsBehavior =
                    WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            }
        } else {
            @Suppress("DEPRECATION")
            window.decorView.systemUiVisibility = (
                    View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                            or View.SYSTEM_UI_FLAG_FULLSCREEN
                            or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                    )
        }
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) {
            hideSystemUI()
        }
    }

    private fun openFabMenu(fabContainer: LinearLayout) {
        linearLayout = findViewById(R.id.linearLayout)
        linearLayout.bringToFront()

        fabContainer.visibility = View.VISIBLE
        fabContainer.bringToFront()
        fabContainer.translationX = fabContainer.width.toFloat()
        fabContainer.alpha = 0f

        fabMain.bringToFront()
        val slideIn = ObjectAnimator.ofFloat(fabContainer, "translationX", 0f)
        val fadeIn = ObjectAnimator.ofFloat(fabContainer, "alpha", 1f)

        slideIn.duration = 300
        fadeIn.duration = 300

        slideIn.start()
        fadeIn.start()
    }

    private fun closeFabMenu(fabContainer: LinearLayout) {
        val slideOut =
            ObjectAnimator.ofFloat(fabContainer, "translationX", fabContainer.width.toFloat())
        val fadeOut = ObjectAnimator.ofFloat(fabContainer, "alpha", 0f)

        slideOut.duration = 300
        fadeOut.duration = 300

        slideOut.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator, isReverse: Boolean) {
                fabContainer.visibility = View.GONE
            }
        })

        slideOut.start()
        fadeOut.start()
    }

    private fun rotateFab(fab: FloatingActionButton, angle: Float) {
        ObjectAnimator.ofFloat(fab, "rotation", angle).apply {
            duration = 300
            start()
        }
    }

    private fun setupOnBackPressedDispatcher() {
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                finishAffinity()
            }
        })
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun displayPopupDialog(task: Task?) {
        val popupDialog = Dialog(this)
        popupDialog.setCancelable(false)
        popupDialog.setContentView(R.layout.activity_add_task)
        popupDialog.window?.setBackgroundDrawable(ColorDrawable(Color.DKGRAY))

        val btnClose = popupDialog.findViewById<ImageButton>(R.id.btnClose)
        val btnDelete = popupDialog.findViewById<ImageButton>(R.id.btnDelete)
        val btnSetDueDate = popupDialog.findViewById<Button>(R.id.btnSetDueDate)
        val edtTitle = popupDialog.findViewById<EditText>(R.id.edtTitle)

        edtTitle.setupKeyboardListener()
        btnSetDueDate.text = task?.endDate ?: "Set Due Date~"
        edtTitle.setText(task?.title)

        btnClose.setOnClickListener {
            val title = edtTitle.text.toString()
            val dueDate = btnSetDueDate.text.toString()

            if (task != null) {
                task.title = title
                task.endDate = dueDate
                db.updateTask(task.id, task.startDate, task.endDate, task.title, task.isChecked)
                taskAdapter.notifyDataSetChanged()
            } else {
                val calendar = Calendar.getInstance()
                val dateFormat = SimpleDateFormat("dd MMMM yyyy", Locale.getDefault())
                val currentDate = dateFormat.format(calendar.time)

                db.addTask(
                    title = title,
                    startDate = currentDate,
                    endDate = dueDate,
                    isChecked = false
                )
                tasksList = getAllTasksFromDb()
                taskAdapter = TaskAdapter(tasksList) { task ->
                    onTaskItemClick(task)
                }
                recyclerView.adapter = taskAdapter
                bottomNavigationView.selectedItemId = R.id.navigation_tasks
            }
            popupDialog.dismiss()
        }

        if (task != null) {
            edtTitle.setText(task.title)
            btnSetDueDate.text = task.endDate

            btnDelete.setOnClickListener {
                db.deleteTask(task.id)
                taskAdapter.removeTask(task)
                popupDialog.dismiss()
            }
        } else {
            btnDelete.visibility = View.GONE
        }

        btnSetDueDate.setOnClickListener {
            val calendar = Calendar.getInstance()
            val year = calendar.get(Calendar.YEAR)
            val month = calendar.get(Calendar.MONTH)
            val day = calendar.get(Calendar.DAY_OF_MONTH)

            val datePickerDialog = DatePickerDialog(
                this,
                { _, selectedYear, selectedMonth, selectedDay ->
                    val calendar = Calendar.getInstance()
                    calendar.set(selectedYear, selectedMonth, selectedDay)

                    val dateFormat = SimpleDateFormat("dd MMMM yyyy", Locale.getDefault())
                    val selectedDate = dateFormat.format(calendar.time)
                    btnSetDueDate.text = selectedDate
                },
                year,
                month,
                day
            )

            datePickerDialog.show()
        }

        popupDialog.show()
        hideSystemUI()
    }

    private fun EditText.setupKeyboardListener() {
        this.viewTreeObserver.addOnGlobalLayoutListener {
            val rect = Rect()
            this.getWindowVisibleDisplayFrame(rect)
            val screenHeight = this.rootView.height
            val keypadHeight = screenHeight - rect.bottom

            val isKeyboardShown = keypadHeight > screenHeight * 0.15

            if (!isKeyboardShown) {
                this.clearFocus()
            }
        }
    }
}
