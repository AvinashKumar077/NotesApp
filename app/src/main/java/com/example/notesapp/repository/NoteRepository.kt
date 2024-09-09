package com.example.notesapp.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.notesapp.api.NotesAPI
import com.example.notesapp.models.NoteRequest
import com.example.notesapp.models.NoteResponse
import com.example.notesapp.utils.NetworkResult
import retrofit2.Response
import javax.inject.Inject

class NoteRepository @Inject constructor(private val notesAPI: NotesAPI) {

    private val _notes = MutableLiveData<NetworkResult<List<NoteResponse>>>()
    val notesLiveData : LiveData<NetworkResult<List<NoteResponse>>>
    get() = _notes

    private val _status = MutableLiveData<NetworkResult<String>>()
    val statusLiveData : LiveData<NetworkResult<String>>
    get() = _status

    suspend fun getNotes(){

        _notes.postValue(NetworkResult.Loading())
        val response = notesAPI.getNotes()
        if(response.isSuccessful && response.body() != null){
            _notes.postValue(NetworkResult.Success(response.body()!!))
        }
        else if(response.errorBody() != null){
            _notes.postValue(NetworkResult.Error("Something went wrong"))
        }
        else {
            _notes.postValue(NetworkResult.Error("Something went wrong"))

        }
    }
    suspend fun createNote(noteRequest: NoteRequest){

        _status.postValue(NetworkResult.Loading())
        val response = notesAPI.createNote(noteRequest)
        handleResponse(response,"Note Created")
    }



    suspend fun updateNote(noteId:String,noteRequest: NoteRequest){
        _status.postValue(NetworkResult.Loading())
        val response = notesAPI.updateNote(noteId,noteRequest)
        handleResponse(response,"Note Updated")

    }
    suspend fun deleteNote(noteId:String){
        _status.postValue(NetworkResult.Loading())
        val response = notesAPI.deleteNote(noteId)
        handleResponse(response,"Note Deleted")

    }

    private fun handleResponse(response: Response<NoteResponse>, message: String) {
        if (response.isSuccessful && response.body() != null) {
            _status.postValue(NetworkResult.Success(message))
        } else if (response.errorBody() != null) {
            _status.postValue(NetworkResult.Error("Something went wrong"))
        } else {
            _status.postValue(NetworkResult.Error("Something went wrong"))

        }
    }


}