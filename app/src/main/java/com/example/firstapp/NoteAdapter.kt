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
import android.util.Log

class NoteAdapter(
    private val notes: List<NoteItem>,
    private val onSelectionChanged: () -> Unit
) : RecyclerView.Adapter<NoteViewHolder>() {

    private val selectedNotes = mutableSetOf<String>()
    private val expandedNotes = mutableSetOf<String>()

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
                // Only expand if note is solved
                if (note.solved) {
                    Log.d("NoteAdapter", "Tapping on solved note: ${note.noteId}, current expanded: $isExpanded")
                    toggleExpansion(note)
                } else {
                    Log.d("NoteAdapter", "Note is not solved, cannot expand")
                }
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
        val wasExpanded = expandedNotes.contains(note.noteId)
        if (wasExpanded) {
            expandedNotes.remove(note.noteId)
            Log.d("NoteAdapter", "Collapsing note: ${note.noteId}")
        } else {
            expandedNotes.add(note.noteId)
            Log.d("NoteAdapter", "Expanding note: ${note.noteId}")
        }
        notifyItemChanged(notes.indexOf(note))
    }

    class NoteViewHolder(val binding: NotesItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(note: NoteItem, isSelected: Boolean, isExpanded: Boolean) {
            Log.d("NoteAdapter", "Binding note: ${note.noteId}, solved: ${note.solved}, expanded: $isExpanded")
            
            binding.textViewName.text = "Name : ${note.name}"
            binding.textViewClass.text = "Class: ${note.studentClass}"
            binding.titleTextView.text = "Subject : ${note.title}"
            binding.descriptionTextView.text = "Question : ${note.description}"

            // Show solved status if note is solved
            if (note.solved) {
                binding.statusText.visibility = View.VISIBLE
                binding.expandIcon.visibility = View.VISIBLE
                
                // Update status text with hint
                if (!isExpanded) {
                    binding.statusText.text = binding.root.context.getString(R.string.solved_tap_to_view)
                    binding.expandIcon.rotation = 0f
                    // Hide answer when collapsed
                    binding.answerTextView.visibility = View.GONE
                    binding.youtubeLinkTextView.visibility = View.GONE
                    Log.d("NoteAdapter", "Note collapsed - hiding answer")
                } else {
                    binding.statusText.text = binding.root.context.getString(R.string.solved)
                    binding.expandIcon.rotation = 180f
                    // Show answer when expanded
                    binding.answerTextView.visibility = View.VISIBLE
                    binding.answerTextView.text = "Answer:- ${note.answer}"
                    Log.d("NoteAdapter", "Note expanded - showing answer: ${note.answer}")
                    
                    // Show YouTube link if available
                    if (note.youtubeLink.isNotEmpty()) {
                        binding.youtubeLinkTextView.visibility = View.VISIBLE
                        binding.youtubeLinkTextView.text = binding.root.context.getString(R.string.open_related_link)
                        
                        binding.youtubeLinkTextView.setOnClickListener {
                            try {
                                val url = note.youtubeLink.trim()
                                val uri = Uri.parse(url)
                                val intent = Intent(Intent.ACTION_VIEW, uri)
                                val chooser = Intent.createChooser(intent, "Open with")
                                it.context.startActivity(chooser)
                            } catch (e: Exception) {
                                e.printStackTrace()
                                Toast.makeText(it.context, it.context.getString(R.string.invalid_unsupported_link), Toast.LENGTH_SHORT).show()
                            }
                        }
                    } else {
                        binding.youtubeLinkTextView.visibility = View.GONE
                    }
                }
            } else {
                binding.statusText.visibility = View.GONE
                binding.expandIcon.visibility = View.GONE
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
