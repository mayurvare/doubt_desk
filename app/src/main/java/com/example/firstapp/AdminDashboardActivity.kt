package com.example.firstapp

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.firstapp.databinding.ActivityAdminDashboardBinding
import com.google.android.material.tabs.TabLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import androidx.core.content.ContextCompat

class AdminDashboardActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAdminDashboardBinding
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var dbRef: DatabaseReference
    private lateinit var adapter: AdminNoteAdapter
    private lateinit var noteList: ArrayList<NoteItem>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAdminDashboardBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)

        firebaseAuth = FirebaseAuth.getInstance()

        noteList = ArrayList()
        adapter = AdminNoteAdapter(noteList)
        binding.recyclerViewNotes.layoutManager = LinearLayoutManager(this)
        binding.recyclerViewNotes.adapter = adapter

        dbRef = FirebaseDatabase.getInstance().getReference("users")

        loadAllusersNotes()

        binding.classTabLayout.addTab(binding.classTabLayout.newTab().setText("All"))
        binding.classTabLayout.addTab(binding.classTabLayout.newTab().setText("FY"))
        binding.classTabLayout.addTab(binding.classTabLayout.newTab().setText("SY"))
        binding.classTabLayout.addTab(binding.classTabLayout.newTab().setText("TY"))
        binding.classTabLayout.addTab(binding.classTabLayout.newTab().setText("Solved"))
        binding.classTabLayout.addTab(binding.classTabLayout.newTab().setText("Unsolved"))


        binding.classTabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                filterAndSearchNotes("")
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {
                filterAndSearchNotes("")
            }
        })

        //swipe Left or Right to delete note


    }


    private fun filterAndSearchNotes(queryText: String?) {
        val query = queryText?.trim()?.lowercase() ?: ""
        val selectedTab = binding.classTabLayout.selectedTabPosition
        val selectedClass = when (selectedTab) {
            1 -> "FY"
            2 -> "SY"
            3 -> "TY"
            else -> "All"
        }

        val filtered = noteList.filter { note ->
            val matchName = note.name.lowercase().contains(query)
            val matchTitle = note.title.lowercase().contains(query)
            val matchClass =
                selectedClass.lowercase() == "all" || note.studentClass.lowercase() == selectedClass.lowercase()

            //New Solved/Unsolved filter
            val matchSolved = when (selectedTab) {
                4 -> note.solved == true    // Solved tab
                5 -> note.solved == false   // Unsolved tab
                else -> true                // other tabs
            }

            (matchName || matchTitle) && matchClass && matchSolved
        }

        adapter.updateList(filtered)
        binding.emptyMessage.visibility = if (filtered.isEmpty()) View.VISIBLE else View.GONE
    }

    private fun loadAllusersNotes() {
        // Show progress bar before fetching
        binding.progressBar.visibility = View.VISIBLE

        dbRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                noteList.clear()
                for (userSnapshot in snapshot.children) {
                    val notesSnapshot = userSnapshot.child("notes")
                    for (noteSnap in notesSnapshot.children) {
                        val note = noteSnap.getValue(NoteItem::class.java)
                        note?.let { noteList.add(it) }
                    }
                }
                filterAndSearchNotes("")

                // Hide progress bar after loading
                binding.progressBar.visibility = View.GONE
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("TAG", "onCancelled: database Error")
                Toast.makeText(
                    this@AdminDashboardActivity, "Error: ${error.message}", Toast.LENGTH_SHORT
                ).show()

                // Hide progress bar if error
                binding.progressBar.visibility = View.GONE
            }
        })
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.search_menu, menu)
        val searchItem = menu?.findItem(R.id.action_search)
        val searchView = searchItem?.actionView as? SearchView

        searchView?.queryHint = "Search by name or subject"
        searchView?.setBackgroundColor(resources.getColor(R.color.colorBackground, theme))
        searchView?.setBackgroundResource(R.drawable.search_view_bg)

        // Access the EditText inside SearchView
        val searchEditText = searchView?.findViewById<android.widget.EditText>(
            androidx.appcompat.R.id.search_src_text
        )
        // Set hint color
        searchEditText?.setHintTextColor(ContextCompat.getColor(this, R.color.colorHint))

        // Set text color
        searchEditText?.setTextColor(ContextCompat.getColor(this, R.color.black))

        searchView?.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                filterAndSearchNotes(query)
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                filterAndSearchNotes(newText)
                return true
            }
        })
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_logout -> {
                logoutUser()
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