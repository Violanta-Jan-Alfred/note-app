package com.app.todo_list

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Rect
import android.media.Image
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.WindowInsets
import android.view.WindowInsetsController
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity

class ModifyNote : AppCompatActivity() {

    private lateinit var btnBack: ImageButton
    private lateinit var btnDelete: ImageButton
    private lateinit var btnClear: ImageButton
    private lateinit var txtTitle: EditText
    private lateinit var txtContent: EditText
    private lateinit var db: Database
    private var noteId: Int? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_note)

        hideSystemUI()
        setupDatabase()
        setupButtons()
        setupContent()
        setupOnBackPressedDispatcher()
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

    private fun setupContent() {
        noteId = intent.getIntExtra("noteId", -1)
        if (noteId != -1) {
            val noteTitle = intent.getStringExtra("noteTitle")
            val noteContent = intent.getStringExtra("noteContent")
            txtTitle.setText(noteTitle)
            txtContent.setText(noteContent)
        }

        txtTitle.setupKeyboardListener()
        txtContent.setupKeyboardListener()

    }

    @SuppressLint("SetTextI18n")
    private fun setupButtons() {
        btnBack = findViewById(R.id.btnBack)
        btnDelete = findViewById(R.id.btnDelete)
        btnClear = findViewById(R.id.btnClear)

        btnBack.setOnClickListener {
            saveNote()
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        }

        btnDelete.setOnClickListener {
            deleteNote()
        }

        btnClear.setOnClickListener {
            txtContent.setText("")
        }
    }

    private fun setupDatabase() {
        db = Database(this)
        txtTitle = findViewById(R.id.editTextTitle)
        txtContent = findViewById(R.id.editTextContent)
    }

    private fun addNote() {
        db.addNote(txtTitle.text.toString(), txtContent.text.toString())
    }

    private fun updateNote() {
        noteId?.let {
            db.updateNote(it, txtTitle.text.toString(), txtContent.text.toString())
        }
    }

    private fun deleteNote() {
        noteId?.let {
            val rowsDeleted = db.deleteNote(it)
            if (rowsDeleted > 0) {
                val intent = Intent(this, MainActivity::class.java)
                startActivity(intent)
                finish()
            }
        }
    }

    private fun saveNote() {
        val title = txtTitle.text.toString().trim()
        val content = txtContent.text.toString().trim()

        if (title.isNotEmpty() || content.isNotEmpty()) {
            if (noteId != -1) {
                updateNote()
            } else {
                addNote()
            }
        }
    }

    private fun setupOnBackPressedDispatcher() {
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                saveNote()
                val intent = Intent(this@ModifyNote, MainActivity::class.java)
                startActivity(intent)
                finish()
            }
        })
    }
}
