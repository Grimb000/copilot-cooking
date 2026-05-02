package com.example.lab3project2_kislov

import android.app.AlertDialog
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.animation.AnimationUtils
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TableLayout
import android.widget.TextView
import androidx.appcompat.widget.SwitchCompat
import androidx.fragment.app.Fragment
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class CalculatorFragment : Fragment(R.layout.fragment_calculator) {

    private lateinit var etInput: EditText
    private lateinit var tvResultType: TextView
    private lateinit var tvError: TextView
    private lateinit var btnCheck: Button
    private lateinit var btnOpenResult: Button
    private lateinit var progressCheck: ProgressBar
    private lateinit var cbShowTable: CheckBox
    private lateinit var swAnimations: SwitchCompat
    private lateinit var tableTypes: TableLayout

    private val handler = Handler(Looper.getMainLooper())
    private val executor: ExecutorService = Executors.newSingleThreadExecutor()

    private var lastInput: String? = null
    private var lastResult: String? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        etInput = view.findViewById(R.id.etInput)
        tvResultType = view.findViewById(R.id.tvResultType)
        tvError = view.findViewById(R.id.tvError)
        btnCheck = view.findViewById(R.id.btnCheck)
        btnOpenResult = view.findViewById(R.id.btnOpenResult)
        progressCheck = view.findViewById(R.id.progressCheck)
        cbShowTable = view.findViewById(R.id.cbShowTable)
        swAnimations = view.findViewById(R.id.swAnimations)
        tableTypes = view.findViewById(R.id.tableTypes)

        cbShowTable.setOnCheckedChangeListener { _, isChecked ->
            tableTypes.visibility = if (isChecked) View.VISIBLE else View.GONE
        }

        btnCheck.setOnClickListener { performCheck() }
        btnOpenResult.setOnClickListener { openResult() }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        executor.shutdownNow()
    }

    private fun performCheck() {
        tvError.text = ""
        val input = etInput.text.toString().trim()
        if (input.isEmpty()) {
            showError(R.string.error_empty)
            return
        }

        progressCheck.visibility = View.VISIBLE
        btnCheck.isEnabled = false

        executor.execute {
            try {
                Thread.sleep(250)
            } catch (e: InterruptedException) {
                Thread.currentThread().interrupt()
                return@execute
            }

            val result = calculateType(input)
            handler.post {
                progressCheck.visibility = View.GONE
                btnCheck.isEnabled = true
                if (result.errorRes != null) {
                    showError(result.errorRes)
                } else {
                    lastInput = input
                    lastResult = result.typeLabel
                    tvResultType.text = getString(R.string.result_type, result.typeLabel)
                    btnOpenResult.isEnabled = true
                    maybeAnimate(tvResultType, R.anim.alpha_pulse)
                    maybeAnimate(btnOpenResult, R.anim.combo_move_scale)
                }
            }
        }
    }

    private fun openResult() {
        val input = lastInput ?: return
        val result = lastResult ?: return
        startActivity(ResultActivity.newIntent(requireContext(), input, result))
    }

    private fun showError(errorRes: Int) {
        tvError.setText(errorRes)
        tvResultType.setText(R.string.result_type_placeholder)
        lastResult = null
        lastInput = null
        btnOpenResult.isEnabled = false
        maybeAnimate(btnCheck, R.anim.rotate)
        AlertDialog.Builder(requireContext())
            .setTitle(R.string.dialog_error_title)
            .setMessage(errorRes)
            .setPositiveButton(R.string.dialog_ok, null)
            .show()
    }

    private fun maybeAnimate(view: View, animRes: Int) {
        if (swAnimations.isChecked) {
            view.startAnimation(AnimationUtils.loadAnimation(requireContext(), animRes))
        }
    }

    private fun calculateType(input: String): TypeResult {
        val intValue = input.toLongOrNull()
        val doubleValue = input.toDoubleOrNull()

        if (doubleValue == null) {
            return TypeResult(errorRes = R.string.error_not_number)
        }

        if (doubleValue.isInfinite() || doubleValue.isNaN()) {
            return TypeResult(errorRes = R.string.error_out_of_range)
        }

        if (doubleValue % 1.0 != 0.0) {
            val floatVal = doubleValue.toFloat()
            val fitsInFloat = floatVal.toDouble() == doubleValue
            return TypeResult(typeLabel = if (fitsInFloat) {
                getString(R.string.type_float)
            } else {
                getString(R.string.type_double)
            })
        }

        if (intValue == null) {
            return TypeResult(errorRes = R.string.error_out_of_range)
        }

        val resType = when {
            intValue in Byte.MIN_VALUE..Byte.MAX_VALUE -> getString(R.string.type_byte)
            intValue in Short.MIN_VALUE..Short.MAX_VALUE -> getString(R.string.type_short)
            intValue in Int.MIN_VALUE..Int.MAX_VALUE -> getString(R.string.type_int)
            else -> getString(R.string.type_long)
        }

        return TypeResult(typeLabel = resType)
    }

    private data class TypeResult(
        val typeLabel: String? = null,
        val errorRes: Int? = null,
    )
}
