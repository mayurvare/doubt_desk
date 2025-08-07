package com.example.firstapp

import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.example.firstapp.databinding.ActivityAddNoteBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class AddNote : AppCompatActivity() {
    private lateinit var binding: ActivityAddNoteBinding
    private lateinit var databaseReference: DatabaseReference
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityAddNoteBinding.inflate(layoutInflater)
        enableEdgeToEdge()
        setContentView(binding.root)

        databaseReference = FirebaseDatabase.getInstance().reference
        auth = FirebaseAuth.getInstance()

        binding.saveNoteButton.setOnClickListener {
            val name = binding.etName.text.toString()
            val studentClass = binding.etClass.text.toString()
            val title = binding.etTitle.text.toString()
            val description = binding.etDescription.text.toString()

            if (title.isEmpty() || description.isEmpty() || studentClass.isEmpty()) {
                Toast.makeText(this, "please fill the all field", Toast.LENGTH_SHORT).show()
            } else {

                val currentUser = auth.currentUser
                currentUser?.let { user ->
                    // Generate the unique key for the note
                    val noteKey =
                        databaseReference.child("users").child(user.uid).child("notes").push().key
                    val userId = user.uid
                    // note item instance

                    val noteItem =
                        NoteItem(name, studentClass, title, description, noteKey ?: "", userId)
                    if (noteKey != null) {
                        // add notes to the user notes
                        databaseReference.child("users").child(user.uid).child("notes")
                            .child(noteKey).setValue(noteItem)
                            .addOnCompleteListener { task ->
                                if (task.isSuccessful) {
                                    Toast.makeText(this, "Note Save Successful", Toast.LENGTH_SHORT)
                                        .show()
                                    finish()
                                } else {
                                    Toast.makeText(this, "Failed to save Note ", Toast.LENGTH_SHORT)
                                        .show()
                                }
                            }
                    }
                }
            }
        }
    }
}