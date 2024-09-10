package com.example.notesapp

import android.app.AlertDialog
import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.example.notesapp.adapter.NoteAdapter
import com.example.notesapp.databinding.FragmentMainBinding
import com.example.notesapp.models.NoteResponse
import com.example.notesapp.models.User
import com.example.notesapp.utils.NetworkResult
import com.example.notesapp.utils.TokenManager
import com.example.notesapp.viewmodel.NoteViewmodel
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.gson.Gson
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainFragment : Fragment() {

    private var _binding: FragmentMainBinding? = null
    private val binding get() = _binding!!
    private val noteViewModel by viewModels<NoteViewmodel>()

    @Inject
    lateinit var tokenManager: TokenManager

    private lateinit var adapter: NoteAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentMainBinding.inflate(inflater, container, false)
        adapter = NoteAdapter(::onNoteClicked)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        noteViewModel.getAllNotes()
        binding.noteList.layoutManager =
            StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)
        binding.noteList.adapter = adapter
        binding.addNote.setOnClickListener {
            findNavController().navigate(R.id.action_mainFragment_to_noteFragment)
        }


        // Sign-out button logic
        binding.profile.setOnClickListener {
            showAccountChooserDialog()
        }

        bindObservers()
    }

    private fun showAccountChooserDialog() {

        val user = getUserFromSharedPreferences()
        Log.d("MainFragment", "User retrieved: $user")
        // Inflate the layout for the BottomSheetDialog
        val dialogView = layoutInflater.inflate(R.layout.dialog_profile, null)

        // Create the BottomSheetDialog
        val bottomSheetDialog = BottomSheetDialog(requireContext(), R.style.BottomSheetDialogTheme)
        bottomSheetDialog.setContentView(dialogView)

        // Setup views in the dialog
        val manageAccountButton = dialogView.findViewById<Button>(R.id.manageAccountButton)
        val userName = dialogView.findViewById<TextView>(R.id.userName)
        val userEmail = dialogView.findViewById<TextView>(R.id.userEmail)
        userName.text = user?.userName ?: "Unknown User"
        userEmail.text = user?.email ?: "unknown@example.com"
        Log.d("info", "$userName")
        manageAccountButton.setOnClickListener {
            tokenManager.saveToken(null)
            bottomSheetDialog.dismiss()
            findNavController().navigate(R.id.action_mainFragment_to_registerFragment)
        }

        // Show the BottomSheetDialog
        bottomSheetDialog.show()
    }
    private fun getUserFromSharedPreferences(): User? {
        val sharedPref = activity?.getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
        val userJson = sharedPref?.getString("user_data", null)
        Log.d("MainFragment", "Retrieved user JSON: $userJson")

        return try {
            if (userJson != null) {
                val user = Gson().fromJson(userJson, User::class.java)
                Log.d("MainFragment", "Parsed User: ${user.userName}, ${user.email}")
                user
            } else {
                Log.d("MainFragment", "No user data found in SharedPreferences")
                null
            }
        } catch (e: Exception) {
            Log.e("MainFragment", "Error parsing user data: ${e.message}")
            null
        }
    }

    private fun bindObservers() {
        noteViewModel.notesLiveData.observe(viewLifecycleOwner, Observer {
            binding.progressBar.isVisible = false
            when (it) {
                is NetworkResult.Success -> {
                    adapter.submitList(it.data)
                }
                is NetworkResult.Error -> {
                    Toast.makeText(requireContext(), it.message.toString(), Toast.LENGTH_SHORT)
                        .show()
                }
                is NetworkResult.Loading -> {
                    binding.progressBar.isVisible = true
                }
            }
        })
    }

    private fun onNoteClicked(noteResponse: NoteResponse){
        val bundle = Bundle()
        bundle.putString("note", Gson().toJson(noteResponse))
        findNavController().navigate(R.id.action_mainFragment_to_noteFragment, bundle)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}