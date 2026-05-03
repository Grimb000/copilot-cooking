package com.example.lab3project2_kislov

import android.os.Bundle
import android.view.View
import android.view.animation.AnimationUtils
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment

class ResultFragment : Fragment(R.layout.fragment_result) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val badge = view.findViewById<ImageView>(R.id.ivResultBadge)
        val tvInput = view.findViewById<TextView>(R.id.tvInputValue)
        val tvResult = view.findViewById<TextView>(R.id.tvResultValue)
        val btnClose = view.findViewById<Button>(R.id.btnClose)

        val input = requireArguments().getString(ARG_INPUT).orEmpty()
        val result = requireArguments().getString(ARG_RESULT).orEmpty()

        tvInput.text = getString(R.string.input_label, input)
        tvResult.text = getString(R.string.result_type, result)

        badge.startAnimation(AnimationUtils.loadAnimation(requireContext(), R.anim.scale_pop))
        tvResult.startAnimation(AnimationUtils.loadAnimation(requireContext(), R.anim.alpha_pulse))

        btnClose.setOnClickListener { requireActivity().finish() }
    }

    companion object {
        private const val ARG_INPUT = "arg_input"
        private const val ARG_RESULT = "arg_result"

        fun newInstance(input: String, result: String): ResultFragment {
            return ResultFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_INPUT, input)
                    putString(ARG_RESULT, result)
                }
            }
        }
    }
}
