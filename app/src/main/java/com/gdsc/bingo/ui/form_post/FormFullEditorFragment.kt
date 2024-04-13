package com.gdsc.bingo.ui.form_post

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
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


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        // Inflate the layout for this fragment
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
            wysiwygEditor.setPlaceholder("Insert your notes here...")

//        val mPreview = preview as TextView
//        wysiwygEditor.setOnTextChangeListener(OnTextChangeListener { text -> mPreview.setText(text) })

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

            actionTxtColor.setOnClickListener(object : View.OnClickListener {
                private var isChanged = false
                override fun onClick(v: View) {
                    wysiwygEditor.setTextColor(if (isChanged) Color.BLACK else Color.RED)
                    isChanged = !isChanged
                }
            })

            actionBgColor.setOnClickListener(object : View.OnClickListener {
                private var isChanged = false
                override fun onClick(v: View) {
                    wysiwygEditor.setTextBackgroundColor(if (isChanged) Color.TRANSPARENT else Color.YELLOW)
                    isChanged = !isChanged
                }
            })

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

            var visible = false

            preview.setOnClickListener {
                if(!visible){
                    wysiwygEditor.setInputEnabled(false)
                    preview.setImageResource(com.github.onecode369.wysiwyg.R.drawable.visibility_off)
                }else{
                    wysiwygEditor.setInputEnabled(true)
                    preview.setImageResource(com.github.onecode369.wysiwyg.R.drawable.visibility)
                }
                visible = !visible
            }

            insertLatex.setOnClickListener {
                if(latextEditor.visibility == View.GONE) {
                    latextEditor.visibility = View.VISIBLE
                    submitLatex.setOnClickListener {
                        wysiwygEditor.insertLatex(latexEquation.text.toString())
                    }
                }else{
                    latextEditor.visibility = View.GONE
                }
            }

            insertCode.setOnClickListener{ wysiwygEditor.setCode() }


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





