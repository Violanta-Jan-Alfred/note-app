package com.app.todo_list

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class NoteAdapter(
    private var notesList: MutableList<Note>,
    private val itemClickListener: (Note) -> Unit
) : RecyclerView.Adapter<NoteAdapter.NoteViewHolder>() {

    private var notesListFiltered: MutableList<Note> = notesList

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NoteViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_note, parent, false)
        return NoteViewHolder(view)
    }

    override fun onBindViewHolder(holder: NoteViewHolder, position: Int) {
        val note = notesListFiltered[position]
        holder.bind(note)
    }

    override fun getItemCount(): Int {
        return notesListFiltered.size
    }

    @SuppressLint("NotifyDataSetChanged")
    fun filter(query: String) {
        notesListFiltered = if (query.isEmpty()) {
            notesList
        } else {
            notesList.filter { note ->
                note.title.contains(query, ignoreCase = true) || note.content.contains(query, ignoreCase = true)
            }.toMutableList()
        }
        notifyDataSetChanged()
    }

    inner class NoteViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val txtTitle: TextView = itemView.findViewById(R.id.noteTitle)
        private val txtContent: TextView = itemView.findViewById(R.id.noteContent)

        fun bind(note: Note) {
            txtTitle.text = note.title
            txtContent.text = note.content
            itemView.setOnClickListener { itemClickListener(note) }
        }
    }
}
