package com.example.notesapp.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.notesapp.model.Note
import com.google.firebase.firestore.FirebaseFirestore

class NoteViewModel : ViewModel() {
    private val db = FirebaseFirestore.getInstance()
    private val notesCollection = db.collection("notes")

    private val _notes = MutableLiveData<List<Note>>()
    val notes: LiveData<List<Note>> = _notes

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _errorMessage = MutableLiveData<String>()
    val errorMessage: LiveData<String> = _errorMessage

    // Fungsi untuk mengambil semua catatan dari Firestore
    fun fetchNotes() {
        _isLoading.value = true
        notesCollection
            .orderBy("timestamp", com.google.firebase.firestore.Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                _isLoading.value = false
                if (error != null) {
                    _errorMessage.value = "Error: ${error.message}"
                    return@addSnapshotListener
                }

                val notesList = mutableListOf<Note>()
                snapshot?.documents?.forEach { document ->
                    val note = Note(
                        id = document.id,
                        title = document.getString("title") ?: "",
                        content = document.getString("content") ?: "",
                        timestamp = document.getLong("timestamp") ?: 0L
                    )
                    notesList.add(note)
                }
                _notes.value = notesList
            }
    }

    // Fungsi untuk menambah catatan baru
    fun addNote(title: String, content: String, onSuccess: () -> Unit = {}, onError: (String) -> Unit = {}) {
        if (title.isBlank() || content.isBlank()) {
            onError("Judul dan konten tidak boleh kosong")
            return
        }

        val note = hashMapOf(
            "title" to title,
            "content" to content,
            "timestamp" to System.currentTimeMillis()
        )

        notesCollection.add(note)
            .addOnSuccessListener {
                onSuccess()
            }
            .addOnFailureListener { e ->
                onError("Gagal menambah catatan: ${e.message}")
            }
    }

    // Fungsi untuk mengupdate catatan
    fun updateNote(noteId: String, title: String, content: String, onSuccess: () -> Unit = {}, onError: (String) -> Unit = {}) {
        if (title.isBlank() || content.isBlank()) {
            onError("Judul dan konten tidak boleh kosong")
            return
        }

        val updates = hashMapOf(
            "title" to title,
            "content" to content,
            "timestamp" to System.currentTimeMillis()
        )

        notesCollection.document(noteId).update(updates as Map<String, Any>)
            .addOnSuccessListener {
                onSuccess()
            }
            .addOnFailureListener { e ->
                onError("Gagal mengupdate catatan: ${e.message}")
            }
    }

    // Fungsi untuk menghapus catatan
    fun deleteNote(noteId: String, onSuccess: () -> Unit = {}, onError: (String) -> Unit = {}) {
        notesCollection.document(noteId).delete()
            .addOnSuccessListener {
                onSuccess()
            }
            .addOnFailureListener { e ->
                onError("Gagal menghapus catatan: ${e.message}")
            }
    }
}