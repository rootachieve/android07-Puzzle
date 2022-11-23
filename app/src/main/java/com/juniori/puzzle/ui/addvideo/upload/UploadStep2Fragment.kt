package com.juniori.puzzle.ui.addvideo.upload

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.juniori.puzzle.R
import com.juniori.puzzle.data.Resource
import com.juniori.puzzle.databinding.FragmentUploadStep2Binding
import com.juniori.puzzle.ui.addvideo.AddVideoViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.io.File

class UploadStep2Fragment : Fragment() {

    private var _binding: FragmentUploadStep2Binding? = null
    private val binding get() = _binding!!
    private val viewModel: AddVideoViewModel by activityViewModels()

    private val saveDialog: AlertDialog by lazy {
        createSaveDialog()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentUploadStep2Binding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val filePath = "${requireContext().cacheDir.path}/${viewModel.videoName.value}.mp4"

        binding.buttonSave.setOnClickListener {
            saveDialog.show()
        }
        binding.buttonGoback.setOnClickListener {
            findNavController().navigateUp()
        }
        binding.containerRadiogroup.setOnCheckedChangeListener { radioGroup, checkedId ->
            if (checkedId == binding.radiobuttonSetPublic.id) {
            } else if (checkedId == binding.radiobuttonSetPrivate.id) {
            }
        }

        lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uploadFlow.collectLatest { resource ->
                    resource?.let {
                        when (it) {
                            is Resource.Success -> {
                                File(filePath).delete()
                                arguments?.let { bundle ->
                                    findNavController().navigate(
                                        bundle.getInt(
                                            "previousFragment"
                                        )
                                    )
                                }
                            }
                            is Resource.Failure -> {
                                /** upload video가 실패했을때의 ui 처리 */
                            }
                            is Resource.Loading -> {
                                /** video upload 중일때의 ui 처리 */
                            }
                        }
                    }
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun createSaveDialog(): AlertDialog {
        return MaterialAlertDialogBuilder(requireContext(), R.style.Theme_Puzzle_Dialog)
            .setTitle(R.string.upload_savedialog_title)
            .setMessage(
                if (binding.radiobuttonSetPrivate.isChecked) {
                    R.string.upload_savedialog_supporting_text_private
                } else {
                    R.string.upload_savedialog_supporting_text_private
                }
            )
            .setPositiveButton(R.string.all_yes) { _, _ ->
                val filePath = "${requireContext().cacheDir.path}/${viewModel.videoName.value}.mp4"
                viewModel.uploadVideo(filePath)
            }
            .setNegativeButton(R.string.all_no) { _, _ ->
                saveDialog.dismiss()
            }
            .create()
    }
}
