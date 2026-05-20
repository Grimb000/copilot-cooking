package com.example.task6

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity

class AddEventActivity : AppCompatActivity() {
    private val viewModel: EventViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_event)

        val titleInput = findViewById<EditText>(R.id.titleInput)
        val descriptionInput = findViewById<EditText>(R.id.descriptionInput)
        val saveButton = findViewById<Button>(R.id.saveButton)

        val eventId = intent.getIntExtra(EXTRA_EVENT_ID, 0)
        val isEditMode = eventId != 0

        if (isEditMode) {
            titleInput.setText(intent.getStringExtra(EXTRA_EVENT_TITLE).orEmpty())
            descriptionInput.setText(intent.getStringExtra(EXTRA_EVENT_DESCRIPTION).orEmpty())
            saveButton.text = getString(R.string.update_event)
        }

        saveButton.setOnClickListener {
            val title = titleInput.text.toString().trim()
            val description = descriptionInput.text.toString().trim()

            if (title.isBlank() || description.isBlank()) {
                Toast.makeText(this, R.string.fill_all_fields, Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (isEditMode) {
                viewModel.updateEvent(Event(eventId, title, description))
                Toast.makeText(this, R.string.event_updated, Toast.LENGTH_SHORT).show()
            } else {
                viewModel.insertEvent(Event(title = title, description = description))
                Toast.makeText(this, R.string.event_saved, Toast.LENGTH_SHORT).show()
            }

            finish()
        }
    }

    companion object {
        const val EXTRA_EVENT_ID = "event_id"
        const val EXTRA_EVENT_TITLE = "event_title"
        const val EXTRA_EVENT_DESCRIPTION = "event_description"
    }
}
