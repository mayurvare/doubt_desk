package com.example.firstapp

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.firstapp.NoteAdapter.NoteViewHolder
import com.example.firstapp.databinding.NotesItemBinding
import android.graphics.Color
import android.net.Uri
import android.widget.Toast

class NoteAdapter(
    private val notes: List<NoteItem>,
    private val onSelectionChanged: () -> Unit
) : RecyclerView.Adapter<NoteViewHolder>() {

    private val selectedNotes = mutableSetOf<String>()
    private val expandedNotes = mutableSetOf<String>() // New for expand/collapse

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NoteViewHolder {
        val binding = NotesItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return NoteViewHolder(binding)
    }

    override fun onBindViewHolder(holder: NoteViewHolder, position: Int) {
        val note = notes[position]
        val isSelected = selectedNotes.contains(note.noteId)
        val isExpanded = expandedNotes.contains(note.noteId)

        holder.bind(note, isSelected, isExpanded)

        holder.itemView.setOnLongClickListener {
            toggleSelection(note)
            true
        }

        holder.itemView.setOnClickListener {
            if (selectedNotes.isNotEmpty()) {
                toggleSelection(note)
            } else {
                toggleExpansion(note)
            }
        }
    }

    override fun getItemCount(): Int = notes.size

    fun getSelectedNotes(): List<NoteItem> {
        return notes.filter { selectedNotes.contains(it.noteId) }
    }

    fun clearSelection() {
        selectedNotes.clear()
        notifyDataSetChanged()
        onSelectionChanged()
    }

    fun selectAll() {
        selectedNotes.clear()
        selectedNotes.addAll(notes.map { it.noteId })
        notifyDataSetChanged()
        onSelectionChanged()
    }

    fun isAllSelected(): Boolean {
        return selectedNotes.size == notes.size
    }

    private fun toggleSelection(note: NoteItem) {
        if (selectedNotes.contains(note.noteId)) {
            selectedNotes.remove(note.noteId)
        } else {
            selectedNotes.add(note.noteId)
        }
        notifyDataSetChanged()
        onSelectionChanged()
    }

    private fun toggleExpansion(note: NoteItem) {
        if (expandedNotes.contains(note.noteId)) {
            expandedNotes.remove(note.noteId)
        } else {
            expandedNotes.add(note.noteId)
        }
        notifyItemChanged(notes.indexOf(note))
    }

    class NoteViewHolder(val binding: NotesItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(note: NoteItem, isSelected: Boolean, isExpanded: Boolean) {
            binding.textViewName.text = "Name : ${note.name}"
            binding.textViewClass.text = "Class: ${note.studentClass}"
            binding.titleTextView.text = "Subject : ${note.title}"
            binding.descriptionTextView.text = "Question : ${note.description}"

            // Show solved status if note is solved
            if (note.solved) {
                binding.statusText.visibility = View.VISIBLE
            } else {
                binding.statusText.visibility = View.GONE
            }

            // Show answer only when expanded and solved
            if (isExpanded && note.solved) {
                binding.answerTextView.visibility = View.VISIBLE
                binding.answerTextView.text = "Answer:- ${note.answer}"

                if (note.youtubeLink.isNotEmpty()) {
                    binding.youtubeLinkTextView.visibility = View.VISIBLE
                    binding.youtubeLinkTextView.text = "Open Related Link"

                    binding.youtubeLinkTextView.setOnClickListener {
                        try {
                            val url = note.youtubeLink.trim()
                            val uri = Uri.parse(url)
                            val intent = Intent(Intent.ACTION_VIEW, uri)

                            val chooser = Intent.createChooser(intent, "Open with")
                            it.context.startActivity(chooser)

                        } catch (e: Exception) {
                            e.printStackTrace()
                            Toast.makeText(it.context, "Invalid or unsupported link", Toast.LENGTH_SHORT).show()
                        }
                    }
                } else {
                    binding.youtubeLinkTextView.visibility = View.GONE
                }
            } else {
                binding.answerTextView.visibility = View.GONE
                binding.youtubeLinkTextView.visibility = View.GONE
            }

            // Selection color logic
            if (isSelected) {
                binding.root.setBackgroundColor(Color.LTGRAY)
            } else {
                val defaultColor = binding.root.context.getColor(R.color.colorBackground)
                binding.root.setBackgroundColor(defaultColor)
            }
        }
    }
}