package com.yonasoft.handballcourtmanager.fragments.details.dialogs

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.setFragmentResult
import com.yonasoft.handballcourtmanager.R
import com.yonasoft.handballcourtmanager.databinding.FragmentEndMatchDialogBinding

import androidx.fragment.app.DialogFragment

class EndMatchDialogFragment : DialogFragment() {

    private var binding:FragmentEndMatchDialogBinding?=null

    companion object {
        const val REQUEST_KEY_END = "request_key"
        const val BUNDLE_KEY_END = "bundle_key"
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_end_match_dialog, container, false)
        return binding?.root ?: View(context)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupListeners()
    }

    private fun setupListeners() {
        binding?.let {
            it.btnYes.setOnClickListener { setResultAndDismiss(true) }
            it.btnNo.setOnClickListener { setResultAndDismiss(false) }
        }
    }

    private fun setResultAndDismiss(result: Boolean) {
        setFragmentResult(REQUEST_KEY_END, bundleOf(BUNDLE_KEY_END to result))
        dismiss()
    }
}

