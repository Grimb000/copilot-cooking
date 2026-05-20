package com.example.task6

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton

class MainActivity : AppCompatActivity() {
    private val viewModel: EventViewModel by viewModels()
    private lateinit var adapter: EventAdapter
    private lateinit var emptyView: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        emptyView = findViewById(R.id.emptyView)
        adapter = EventAdapter(
            onEditClick = { event -> openEditScreen(event) },
            onDeleteClick = { event ->
                viewModel.deleteEvent(event)
                Toast.makeText(this, R.string.event_deleted, Toast.LENGTH_SHORT).show()
            }
        )

        findViewById<RecyclerView>(R.id.eventsRecyclerView).apply {
            layoutManager = LinearLayoutManager(this@MainActivity)
            adapter = this@MainActivity.adapter
        }

        findViewById<FloatingActionButton>(R.id.addButton).setOnClickListener {
            startActivity(Intent(this, AddEventActivity::class.java))
        }

        findViewById<Button>(R.id.clearButton).setOnClickListener {
            viewModel.clearEvents()
            Toast.makeText(this, R.string.all_events_cleared, Toast.LENGTH_SHORT).show()
        }

        viewModel.allEvents.observe(this) { events ->
            adapter.submitList(events)
            emptyView.visibility = if (events.isEmpty()) View.VISIBLE else View.GONE
        }
    }

    private fun openEditScreen(event: Event) {
        val intent = Intent(this, AddEventActivity::class.java).apply {
            putExtra(AddEventActivity.EXTRA_EVENT_ID, event.id)
            putExtra(AddEventActivity.EXTRA_EVENT_TITLE, event.title)
            putExtra(AddEventActivity.EXTRA_EVENT_DESCRIPTION, event.description)
        }
        startActivity(intent)
    }
}
