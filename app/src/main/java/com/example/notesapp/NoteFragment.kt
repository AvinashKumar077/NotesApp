package com.example.notesapp

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.example.notesapp.databinding.FragmentNoteBinding
import com.example.notesapp.models.NoteRequest
import com.example.notesapp.models.NoteResponse
import com.example.notesapp.utils.NetworkResult
import com.example.notesapp.viewmodel.NoteViewmodel
import com.google.gson.Gson
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class NoteFragment : Fragment() {

    private var _binding: FragmentNoteBinding? = null
    private val binding get() = _binding!!
    private var note : NoteResponse?=null
    private val noteViewModel by viewModels<NoteViewmodel>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentNoteBinding.inflate(inflater, container, false)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setInitialData()
        bindHandler()
        bindObserver()
    }


    private fun setInitialData() {
        val jsonNote = arguments?.getString("note")
        if (jsonNote!=null){
            note = Gson().fromJson(jsonNote,NoteResponse::class.java)
            note?.let {
                binding.txtTitle.setText(it.title)
                binding.txtDescription.setText(it.description)
            }

        }else{
            binding.addEditText.text = "Add Note"
        }
    }

    private fun bindHandler() {
        binding.btnDelete.setOnClickListener{
            note?.let { noteViewModel.deleteNote(it._id) }
        }
        binding.btnSubmit.setOnClickListener {
            val title = binding.txtTitle.text.toString()
            val description = binding.txtDescription.text.toString()
            val noteRequest = NoteRequest(description,title)
            if (note==null){
                noteViewModel.createNote(noteRequest)
            }else{
                noteViewModel.updateNote(note!!._id,noteRequest)
            }
        }

    }
    private fun bindObserver() {
        noteViewModel.statusLiveData.observe(viewLifecycleOwner, Observer {
            when(it){
                is NetworkResult.Success -> {
                    findNavController().popBackStack()
                }
                is NetworkResult.Error -> {}
                is NetworkResult.Loading -> {}
            }
        })

    }



    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}