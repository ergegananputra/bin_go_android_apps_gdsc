package com.gdsc.bingo.ui.form_post

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.gdsc.bingo.databinding.FragmentFormFullEditorBinding
import com.gdsc.bingo.ui.form_post.viewmodel.FormPostViewModel
import com.github.onecode369.wysiwyg.WYSIWYG

class FormFullEditorFragment : Fragment() {

    private val binding by lazy {
        FragmentFormFullEditorBinding.inflate(layoutInflater)
    }

    private val formViewModel by lazy {
        ViewModelProvider(requireActivity())[FormPostViewModel::class.java]
    }

    private val currentExpandableTools : MutableLiveData<View?> = MutableLiveData()
    private var previousExpandableTools : View? = null


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        // Inflate the layout for this fragment
        binding.editorHeadingOptions.isVisible = false
        binding.editorJustifyOptions.isVisible = false
        binding.editorMoreOptions.isVisible = false

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupCancelButton()
        setupSaveButton()

        with(binding) {

            val wysiwygEditor = editor as WYSIWYG
            wysiwygEditor.setEditorHeight(200)
            wysiwygEditor.setEditorFontSize(16)
            wysiwygEditor.setPadding(10, 10, 10, 10)
            wysiwygEditor.setPlaceholder("Apa yang ingin Anda bagikan? ...")

            setupWYSIWYGListeners(wysiwygEditor)
            setupExpandableButtons()

        }
    }

    private fun setupExpandableButtons() {


        with(binding) {
            actionHeadingOptions.setOnClickListener { currentExpandableTools.value = it }
            actionAlignOptions.setOnClickListener { currentExpandableTools.value = it }
            actionMoreOptions.setOnClickListener { currentExpandableTools.value = it }
        }

        currentExpandableTools.observe(viewLifecycleOwner) {
            with(binding) {
                editorHeadingOptions.isVisible = it == actionHeadingOptions
                editorJustifyOptions.isVisible = it == actionAlignOptions
                editorMoreOptions.isVisible = it == actionMoreOptions

                if (previousExpandableTools != null && previousExpandableTools == it) {
                    currentExpandableTools.value = null
                } else {
                    previousExpandableTools = it
                }
            }
        }


    }

    private fun setupWYSIWYGListeners(wysiwygEditor: WYSIWYG) {
        with(binding) {
            actionUndo.setOnClickListener{ wysiwygEditor.undo() }

            actionRedo.setOnClickListener{ wysiwygEditor.redo() }

            actionBold.setOnClickListener{ wysiwygEditor.setBold() }

            actionItalic.setOnClickListener{ wysiwygEditor.setItalic() }

            actionSubscript.setOnClickListener{ wysiwygEditor.setSubscript() }

            actionSuperscript.setOnClickListener{ wysiwygEditor.setSuperscript() }

            actionStrikethrough.setOnClickListener{ wysiwygEditor.setStrikeThrough() }

            actionUnderline.setOnClickListener { wysiwygEditor.setUnderline() }

            actionHeading1.setOnClickListener{
                wysiwygEditor.setHeading(
                    1
                )
            }

            actionHeading2.setOnClickListener{
                wysiwygEditor.setHeading(
                    2
                )
            }

            actionHeading3.setOnClickListener{
                wysiwygEditor.setHeading(
                    3
                )
            }

            actionHeading4.setOnClickListener{
                wysiwygEditor.setHeading(
                    4
                )
            }

            actionHeading5.setOnClickListener{
                wysiwygEditor.setHeading(
                    5
                )
            }

            actionHeading6.setOnClickListener{
                wysiwygEditor.setHeading(
                    6
                )
            }

            actionIndent.setOnClickListener{wysiwygEditor.setIndent() }

            actionOutdent.setOnClickListener { wysiwygEditor.setOutdent() }

            actionAlignLeft.setOnClickListener{ wysiwygEditor.setAlignLeft() }

            actionAlignCenter.setOnClickListener{ wysiwygEditor.setAlignCenter() }

            actionAlignRight.setOnClickListener { wysiwygEditor.setAlignRight() }

            actionAlignJustify.setOnClickListener { wysiwygEditor.setAlignJustifyFull() }

            actionBlockquote.setOnClickListener { wysiwygEditor.setBlockquote() }

            actionInsertBullets.setOnClickListener { wysiwygEditor.setBullets() }

            actionInsertNumbers.setOnClickListener { wysiwygEditor.setNumbers() }

            actionInsertCheckbox.setOnClickListener{ wysiwygEditor.insertTodo() }
        }

    }

    private fun setupSaveButton() {
        binding.formFullEditorHeaderButtonSave.setOnClickListener {
            val editor = binding.editor as WYSIWYG
            formViewModel.description.value = editor.html

            findNavController().navigateUp()
        }
    }

    private fun setupCancelButton() {
        binding.formFullEditorHeaderButtonCancel.setOnClickListener {
            findNavController().navigateUp()
        }
    }




}





