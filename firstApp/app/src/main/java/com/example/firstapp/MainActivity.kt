package com.example.firstapp

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.View
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.firstapp.databinding.ActivityMainBinding
import com.example.firstapp.databinding.DialogUpdateNoteBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.drawable.Drawable
import androidx.appcompat.widget.SearchView
import androidx.core.content.ContextCompat


class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var dbRef: DatabaseReference
    private lateinit var adapter: NoteAdapter
    private lateinit var noteList: ArrayList<NoteItem>
    private var recentlyDeletedNote: NoteItem? = null
    private var recentlyDeletedPosition: Int = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)

        firebaseAuth = FirebaseAuth.getInstance()
        dbRef = FirebaseDatabase.getInstance().getReference("users")
        noteList = ArrayList()

//        adapter = NoteAdapter(noteList)
        adapter = NoteAdapter(noteList) {
            invalidateOptionsMenu()
        }

        binding.recyclerViewNotes.layoutManager = LinearLayoutManager(this)
        binding.recyclerViewNotes.adapter = adapter

        loadUserNotes()

        binding.createNoteButton.setOnClickListener {
            startActivity(Intent(this, AddNote::class.java))
        }

        // Right swipe to (Edit) edit note and Left swipe to (Delete) delete note
        val itemTouchHelper = ItemTouchHelper(object :
            ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT) {

            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean = false

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val position = viewHolder.adapterPosition
                val note = noteList[position]

                if (direction == ItemTouchHelper.LEFT) {
                    // Delete logic
                    recentlyDeletedNote = note
                    recentlyDeletedPosition = position

                    firebaseAuth.currentUser?.uid?.let { uid ->
                        dbRef.child(uid).child("notes").child(note.noteId).removeValue()
                    }

                    noteList.removeAt(position)
                    adapter.notifyItemRemoved(position)
                    showUndoSnackbar()

                } else if (direction == ItemTouchHelper.RIGHT) {
                    // Open update dialog
                    adapter.notifyItemChanged(position) // prevent it from disappearing
                    showUpdateDialog(
                        note.noteId, note.name, note.studentClass,
                        note.title, note.description
                    )
                }
            }

            //Draw colored background during swipe
            override fun onChildDraw(
                c: Canvas,
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                dX: Float,
                dY: Float,
                actionState: Int,
                isCurrentlyActive: Boolean
            ) {
                val itemView = viewHolder.itemView
                val paint = Paint()
                val textPaint = Paint().apply {
                    color = Color.WHITE
                    textSize = 40f
                    isAntiAlias = true
                }

                val icon: Drawable?
                val text: String
                val iconMargin = 32

                if (dX > 0) {
                    // Right swipe (Edit)
                    paint.color = Color.parseColor("#2196F3")
                    c.drawRect(
                        itemView.left.toFloat(), itemView.top.toFloat(),
                        itemView.left + dX, itemView.bottom.toFloat(), paint
                    )

                    icon = ContextCompat.getDrawable(this@MainActivity, R.drawable.note_edit_icon)
                    text = "Edit"

                    icon?.let {
                        val iconTop = itemView.top + (itemView.height - it.intrinsicHeight) / 2
                        val iconLeft = itemView.left + iconMargin
                        val iconRight = iconLeft + it.intrinsicWidth
                        val iconBottom = iconTop + it.intrinsicHeight

                        it.setBounds(iconLeft, iconTop, iconRight, iconBottom)
                        it.draw(c)

                        // Draw Text to the right of icon
                        c.drawText(
                            text,
                            (iconRight + 30).toFloat(),
                            (iconBottom - 10).toFloat(),
                            textPaint
                        )
                    }

                } else if (dX < 0) {
                    // Left swipe (Delete)
                    paint.color = Color.parseColor("#f44336")
                    c.drawRect(
                        itemView.right + dX, itemView.top.toFloat(),
                        itemView.right.toFloat(), itemView.bottom.toFloat(), paint
                    )

                    icon = ContextCompat.getDrawable(this@MainActivity, R.drawable.note_delete_icon)
                    text = "Delete"

                    icon?.let {
                        val iconTop = itemView.top + (itemView.height - it.intrinsicHeight) / 2
                        val iconRight = itemView.right - iconMargin
                        val iconLeft = iconRight - it.intrinsicWidth
                        val iconBottom = iconTop + it.intrinsicHeight

                        it.setBounds(iconLeft, iconTop, iconRight, iconBottom)
                        it.draw(c)

                        // Draw text to the left of icon
                        val textWidth = textPaint.measureText(text)
                        c.drawText(
                            text,
                            iconLeft - textWidth - 30,
                            (iconBottom - 10).toFloat(),
                            textPaint
                        )
                    }
                }

                super.onChildDraw(
                    c,
                    recyclerView,
                    viewHolder,
                    dX,
                    dY,
                    actionState,
                    isCurrentlyActive
                )
            }

        })
        itemTouchHelper.attachToRecyclerView(binding.recyclerViewNotes)

    }

    override fun onPrepareOptionsMenu(menu: Menu?): Boolean {
        val selectedCount = adapter.getSelectedNotes().size
        val totalCount = adapter.itemCount

        menu?.findItem(R.id.action_delete)?.isVisible = selectedCount > 0

        menu?.findItem(R.id.menu_select_all)?.title =
            if (selectedCount == totalCount && totalCount > 0) "Unselect All" else "Select All"

        return super.onPrepareOptionsMenu(menu)
    }


    private fun deleteSelectedNotes() {
        val selectedNotes = adapter.getSelectedNotes()
        if (selectedNotes.isEmpty()) {
            Toast.makeText(this, "No notes selected", Toast.LENGTH_SHORT).show()
            return
        }

        AlertDialog.Builder(this)
            .setTitle("Delete Notes")
            .setMessage("Are you sure you want to delete ${selectedNotes.size} note(s)?")
            .setPositiveButton("Yes") { dialog, _ ->
                val currentUserId = firebaseAuth.currentUser?.uid ?: return@setPositiveButton

                for (note in selectedNotes) {
                    dbRef.child(currentUserId).child("notes").child(note.noteId).removeValue()
                }

                Toast.makeText(this, "${selectedNotes.size} note(s) deleted", Toast.LENGTH_SHORT)
                    .show()
                adapter.clearSelection()
                dialog.dismiss()
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    override fun onBackPressed() {
        if (adapter.getSelectedNotes().isNotEmpty()) {
            adapter.clearSelection()
        } else {
            super.onBackPressed()
        }
    }


    private fun showUndoSnackbar() {
        val snackbar = com.google.android.material.snackbar.Snackbar.make(
            binding.root,
            "Note deleted",
            com.google.android.material.snackbar.Snackbar.LENGTH_LONG
        )
        snackbar.setAction("UNDO") {
            recentlyDeletedNote?.let { note ->
                firebaseAuth.currentUser?.uid?.let { uid ->
                    dbRef.child(uid).child("notes").child(note.noteId).setValue(note)
                }

                noteList.add(recentlyDeletedPosition, note)
                adapter.notifyItemInserted(recentlyDeletedPosition)
            }
        }
        snackbar.show()
    }

    private fun loadUserNotes() {
        val currentUserId = firebaseAuth.currentUser?.uid ?: return

        dbRef.child(currentUserId).child("notes")
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    noteList.clear()
                    for (noteSnap in snapshot.children) {
                        val note = noteSnap.getValue(NoteItem::class.java)
                        note?.let { noteList.add(it) }
                    }
                    adapter.notifyDataSetChanged()

                    if (noteList.isEmpty()) {
                        binding.emptyMessage.visibility = View.VISIBLE
                    } else {
                        binding.emptyMessage.visibility = View.GONE
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                }
            })
    }

    private fun showUpdateDialog(
        noteId: String,
        currentName: String,
        currentClass: String,
        currentTitle: String,
        currentDescription: String
    ) {
        val dialogBinding = DialogUpdateNoteBinding.inflate(layoutInflater)
        val dialog = androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Update Note")
            .setView(dialogBinding.root)
            .setPositiveButton("Update") { dialog, _ ->
                val newTitle = dialogBinding.updatenotetitle.text.toString()
                val newDescription = dialogBinding.updatenotedescription.text.toString()
                updateNote(noteId, currentName, currentClass, newTitle, newDescription)
                dialog.dismiss()
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }
            .create()

        dialogBinding.updatenotetitle.setText(currentTitle)
        dialogBinding.updatenotedescription.setText(currentDescription)

        dialog.show()
    }

    private fun updateNote(
        noteId: String,
        name: String,
        studentClass: String,
        newTitle: String,
        newDescription: String
    ) {
        val currentUserId = firebaseAuth.currentUser?.uid ?: return
        val updatedNote =
            NoteItem(name, studentClass, newTitle, newDescription, noteId, currentUserId)

        dbRef.child(currentUserId).child("notes").child(noteId).setValue(updatedNote)
            .addOnSuccessListener {
                Toast.makeText(this, "Note updated successfully", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Update failed", Toast.LENGTH_SHORT).show()
            }
    }


    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_logout -> {
                logoutUser()
                true
            }

            R.id.menu_select_all -> {
                if (adapter.isAllSelected()) {
                    adapter.clearSelection()
                } else {
                    adapter.selectAll()
                }
                true
            }

            R.id.action_delete -> {
                deleteSelectedNotes()
                true
            }


            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun logoutUser() {
        firebaseAuth.signOut()
        // Logout flag
        val sharedPref = getSharedPreferences("MyAppPrefs", MODE_PRIVATE)
        sharedPref.edit().putBoolean("isUserLoggedOut", true).apply()

        // LoginActivity  redirect
        val intent = Intent(this, login_Activity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}