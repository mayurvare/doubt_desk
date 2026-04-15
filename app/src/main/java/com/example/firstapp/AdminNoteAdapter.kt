package com.example.firstapp

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.example.firstapp.databinding.NotesItemAdminBinding
import com.google.firebase.database.FirebaseDatabase
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import android.content.Intent
import android.net.Uri


class AdminNoteAdapter(
    private var notes: List<NoteItem>
) : RecyclerView.Adapter<AdminNoteAdapter.NoteViewHolder>() {

    class NoteViewHolder(val binding: NotesItemAdminBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(note: NoteItem, isExpanded: Boolean) {
            binding.textViewName.text = "Name : ${note.name}"
            binding.textViewClass.text = "Class : ${note.studentClass}"
            binding.titleTextView.text = "Subject : ${note.title}"
            binding.descriptionTextView.text = "Question : ${note.description}"
            binding.youtubeLinkTextView.visibility = View.GONE

            // Handle solved visibility
            if (note.solved) {
                binding.statusText.visibility = View.VISIBLE
                binding.answerTextView.visibility = if (isExpanded) View.VISIBLE else View.GONE
                binding.answerTextView.text = "Answer:- ${note.answer}"
                binding.solvebutton.visibility = View.GONE
                binding.etAnswer.visibility = View.GONE
                binding.btnAskAI.visibility = View.GONE
                binding.etYoutubeLink.visibility = View.GONE

                //binding.youtubeLinkTextView.visibility = if (!note.youtubeLink.isNullOrEmpty()) View.VISIBLE else View.GONE
                binding.youtubeLinkTextView.visibility =
                    if (!note.youtubeLink.isNullOrEmpty() && isExpanded) View.VISIBLE else View.GONE
                binding.youtubeLinkTextView.text = note.youtubeLink
                binding.youtubeLinkTextView.setOnClickListener {
                    try {
                        val link = note.youtubeLink?.trim()
                        if (!link.isNullOrEmpty() && (link.startsWith("http://") || link.startsWith(
                                "https://"
                            ))
                        ) {
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(link))
                            binding.root.context.startActivity(intent)
                        } else {
                            Toast.makeText(
                                binding.root.context,
                                binding.root.context.getString(R.string.invalid_link),
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    } catch (e: Exception) {
                        Toast.makeText(
                            binding.root.context,
                            binding.root.context.getString(R.string.link_error, e.message),
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            } else {
                binding.statusText.visibility = View.GONE
                binding.answerTextView.visibility = View.GONE
                binding.solvebutton.visibility = if (isExpanded) View.VISIBLE else View.GONE
                binding.etAnswer.visibility = if (isExpanded) View.VISIBLE else View.GONE
                binding.btnAskAI.visibility = if (isExpanded) View.VISIBLE else View.GONE
                binding.etYoutubeLink.visibility = if (isExpanded) View.VISIBLE else View.GONE

                binding.youtubeLinkTextView.visibility = View.GONE
            }

            binding.etAnswer.setText(note.answer ?: "")
        }
    }

    private val expandedPositions = mutableSetOf<Int>()

    fun updateList(newList: List<NoteItem>) {
        notes = newList
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NoteViewHolder {
        val binding =
            NotesItemAdminBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return NoteViewHolder(binding)
    }

    override fun onBindViewHolder(holder: NoteViewHolder, position: Int) {
        val note = notes[position]
        val isExpanded = expandedPositions.contains(position)
        holder.bind(note, isExpanded)

        holder.binding.cardView.setOnClickListener {
            if (expandedPositions.contains(position))
                expandedPositions.remove(position)
            else
                expandedPositions.add(position)
            notifyItemChanged(position)
        }

        holder.binding.solvebutton.setOnClickListener {
            val answerText = holder.binding.etAnswer.text.toString().trim()
            val youtubeLink = holder.binding.etYoutubeLink.text.toString().trim()

            if (answerText.isEmpty()) {
                Toast.makeText(
                    holder.itemView.context,
                    holder.itemView.context.getString(R.string.answer_empty),
                    Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }

            holder.binding.etYoutubeLink.visibility = View.GONE

            val mainRef = FirebaseDatabase.getInstance().getReference()

            val updates = mapOf(
                "solved" to true,
                "answer" to answerText,
                "youtubeLink" to youtubeLink
            )

            mainRef.child("users").child(note.userId).child("notes").child(note.noteId)
                .updateChildren(updates)
                .addOnSuccessListener {
                    note.solved = true
                    note.answer = answerText
                    notifyItemChanged(position)
                    holder.binding.etYoutubeLink.visibility = View.GONE
                    Toast.makeText(holder.itemView.context, holder.itemView.context.getString(R.string.answer_submitted), Toast.LENGTH_SHORT)
                        .show()
                }
                .addOnFailureListener {
                    Toast.makeText(
                        holder.itemView.context,
                        holder.itemView.context.getString(R.string.answer_submit_failed, it.message),
                        Toast.LENGTH_SHORT
                    ).show()
                }
        }

        val question = note.description
        val request = OpenRouterRequest(
//            model = "mistralai/mistral-7b-instruct",
            model = "llama-3.1-8b-instant",
            messages = listOf(Message("user", question)),
            max_tokens = 200
        )

        holder.binding.btnAskAI.setOnClickListener {
            holder.binding.btnAskAI.isEnabled = false
            holder.binding.btnAskAI.text = holder.itemView.context.getString(R.string.ai_loading)

            val apiKey = ApiConfig.getAiApiKey()
            OpenRouterClient.instance.getAIAnswer(
                apiKey = apiKey,
                request = request
            ).enqueue(object : Callback<OpenRouterResponse> {
                override fun onResponse(
                    call: Call<OpenRouterResponse>,
                    response: Response<OpenRouterResponse>
                ) {
                    holder.binding.btnAskAI.isEnabled = true
                    holder.binding.btnAskAI.text = holder.itemView.context.getString(R.string.ask_ai)

                    if (response.isSuccessful) {
                        val answer = response.body()?.choices?.firstOrNull()?.message?.content
                        if (!answer.isNullOrEmpty()) {
                            holder.binding.etAnswer.setText(answer)
                        } else {
                            Toast.makeText(
                                holder.itemView.context,
                                holder.itemView.context.getString(R.string.ai_no_answer),
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    } else {
                        Toast.makeText(
                            holder.itemView.context,
                            holder.itemView.context.getString(R.string.ai_error, response.code().toString()),
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }

                override fun onFailure(call: Call<OpenRouterResponse>, t: Throwable) {
                    holder.binding.btnAskAI.isEnabled = true
                    holder.binding.btnAskAI.text = holder.itemView.context.getString(R.string.ask_ai)
                    Toast.makeText(
                        holder.itemView.context,
                        holder.itemView.context.getString(R.string.ai_network_error, t.message),
                        Toast.LENGTH_SHORT
                    ).show()
                }
            })
        }
    }

    override fun getItemCount(): Int = notes.size
}