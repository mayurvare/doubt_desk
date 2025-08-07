package com.example.firstapp

import android.os.Bundle
import android.util.Log
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

            Log.e(
                "AddNoteDebug",
                "Name: $name Title: $title, Description: $description, Class: $studentClass"
            )

            if (title.isEmpty() || description.isEmpty() || studentClass.isEmpty()) {
                Log.e("ValidationError", "Fields are missing")
                Toast.makeText(this, "please fill the all field", Toast.LENGTH_SHORT).show()
            } else {

                val currentUser = auth.currentUser
                Log.e("FirebaseUser", "UID: ${currentUser?.uid}")
                currentUser?.let { user ->
                    // Generate the unique key for the note
                    val noteKey =
                        databaseReference.child("users").child(user.uid).child("notes").push().key
                    Log.e("NoteKeyDebug", "Generated noteKey: $noteKey")

                    val userId = user.uid

                    // note item instance
                    val noteItem =
                        NoteItem(name, studentClass, title, description, noteKey ?: "", userId)
                    if (noteKey != null) {
                        Log.e("NoteKeyError", "noteKey======== $noteKey")
                        // add notes to the user notes
                        databaseReference.child("users").child(user.uid).child("notes")
                            .child(noteKey).setValue(noteItem)
                            .addOnCompleteListener { task ->
                                if (task.isSuccessful) {
                                    Log.e("AddNoteSuccess", "Note saved successfully.")
                                    Log.e(
                                        "AddNotePath",
                                        "Saved at path: users/${user.uid}/notes/$noteKey"
                                    )
                                    Log.e(
                                        "AddNoteUser",
                                        "Saved by user: ${user.email ?: "Unknown Email"} (UID: ${user.uid})"
                                    )
                                    Log.e("AddNoteData", "Saved NoteItem: $noteItem")
                                    Toast.makeText(this, "Note Save Successful", Toast.LENGTH_SHORT)
                                        .show()
                                    finish()
                                } else {
                                    Log.e(
                                        "AddNoteError",
                                        "Error saving note: ${task.exception?.message}"
                                    )
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