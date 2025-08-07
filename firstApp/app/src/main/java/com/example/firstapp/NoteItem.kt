package com.example.firstapp

data class NoteItem(
    val name: String = "",
    val studentClass: String = "",
    val title: String = "",
    val description: String = "",
    val noteId: String = "",
    val userId: String = "",
    var answer: String ="",
    var solved: Boolean = false,
    var youtubeLink: String = ""
)