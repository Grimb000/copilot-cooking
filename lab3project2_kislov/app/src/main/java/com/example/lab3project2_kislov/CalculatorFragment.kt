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
import android.widget.RadioGroup
import android.widget.TableLayout
import android.widget.TextView
import androidx.appcompat.widget.SwitchCompat
import androidx.fragment.app.Fragment
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class CalculatorFragment : Fragment(R.layout.fragment_calculator) {

    private lateinit var rgMode: RadioGroup
    private lateinit var typeModeContainer: View
    private lateinit var basicModeContainer: View
    private lateinit var etInput: EditText
    private lateinit var tvResultType: TextView
    private lateinit var tvError: TextView
    private lateinit var btnCheck: Button
    private lateinit var progressCheck: ProgressBar
    private lateinit var cbShowTable: CheckBox
    private lateinit var swAnimations: SwitchCompat
    private lateinit var tableTypes: TableLayout
    private lateinit var tvCalcDisplay: TextView
    private lateinit var tvCalcError: TextView

    private val handler = Handler(Looper.getMainLooper())
    private val executor: ExecutorService = Executors.newSingleThreadExecutor()

    private var calcStoredValue: Double? = null
    private var calcPendingOperator: CalcOperator? = null
    private var calcCurrentInput: String = ""
    private var calcJustEvaluated = false

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        rgMode = view.findViewById(R.id.rgMode)
        typeModeContainer = view.findViewById(R.id.typeModeContainer)
        basicModeContainer = view.findViewById(R.id.basicModeContainer)
        etInput = view.findViewById(R.id.etInput)
        tvResultType = view.findViewById(R.id.tvResultType)
        tvError = view.findViewById(R.id.tvError)
        btnCheck = view.findViewById(R.id.btnCheck)
        progressCheck = view.findViewById(R.id.progressCheck)
        cbShowTable = view.findViewById(R.id.cbShowTable)
        swAnimations = view.findViewById(R.id.swAnimations)
        tableTypes = view.findViewById(R.id.tableTypes)
        tvCalcDisplay = view.findViewById(R.id.tvCalcDisplay)
        tvCalcError = view.findViewById(R.id.tvCalcError)

        etInput.showSoftInputOnFocus = false
        etInput.keyListener = null
        etInput.isCursorVisible = false

        cbShowTable.setOnCheckedChangeListener { _, isChecked ->
            tableTypes.visibility = if (isChecked) View.VISIBLE else View.GONE
        }

        btnCheck.setOnClickListener { performCheck() }

        setupTypeKeypad(view)
        setupCalcKeypad(view)

        rgMode.setOnCheckedChangeListener { _, checkedId -> updateMode(checkedId) }
        updateMode(rgMode.checkedRadioButtonId)

        resetCalcState()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        executor.shutdownNow()
    }

    private fun updateMode(checkedId: Int) {
        val showTypeMode = checkedId == R.id.rbModeTypes
        typeModeContainer.visibility = if (showTypeMode) View.VISIBLE else View.GONE
        basicModeContainer.visibility = if (showTypeMode) View.GONE else View.VISIBLE
    }

    private fun setupTypeKeypad(view: View) {
        val digitButtons = mapOf(
            R.id.btnTypeDigit0 to "0",
            R.id.btnTypeDigit1 to "1",
            R.id.btnTypeDigit2 to "2",
            R.id.btnTypeDigit3 to "3",
            R.id.btnTypeDigit4 to "4",
            R.id.btnTypeDigit5 to "5",
            R.id.btnTypeDigit6 to "6",
            R.id.btnTypeDigit7 to "7",
            R.id.btnTypeDigit8 to "8",
            R.id.btnTypeDigit9 to "9",
        )
        digitButtons.forEach { (id, value) ->
            view.findViewById<Button>(id).setOnClickListener { appendTypeInput(value) }
        }
        view.findViewById<Button>(R.id.btnTypeDot).setOnClickListener { appendTypeInput(".") }
        view.findViewById<Button>(R.id.btnTypeMinus).setOnClickListener { appendTypeInput("-") }
        view.findViewById<Button>(R.id.btnTypeClear).setOnClickListener { clearTypeInput() }
        view.findViewById<Button>(R.id.btnTypeBackspace).setOnClickListener { backspaceTypeInput() }
    }

    private fun setupCalcKeypad(view: View) {
        val digitButtons = mapOf(
            R.id.btnCalcDigit0 to "0",
            R.id.btnCalcDigit1 to "1",
            R.id.btnCalcDigit2 to "2",
            R.id.btnCalcDigit3 to "3",
            R.id.btnCalcDigit4 to "4",
            R.id.btnCalcDigit5 to "5",
            R.id.btnCalcDigit6 to "6",
            R.id.btnCalcDigit7 to "7",
            R.id.btnCalcDigit8 to "8",
            R.id.btnCalcDigit9 to "9",
        )
        digitButtons.forEach { (id, value) ->
            view.findViewById<Button>(id).setOnClickListener { appendCalcDigit(value) }
        }
        view.findViewById<Button>(R.id.btnCalcDot).setOnClickListener { appendCalcDot() }
        view.findViewById<Button>(R.id.btnCalcAdd).setOnClickListener { onCalcOperator(CalcOperator.ADD) }
        view.findViewById<Button>(R.id.btnCalcSub).setOnClickListener { onCalcOperator(CalcOperator.SUB) }
        view.findViewById<Button>(R.id.btnCalcMul).setOnClickListener { onCalcOperator(CalcOperator.MUL) }
        view.findViewById<Button>(R.id.btnCalcDiv).setOnClickListener { onCalcOperator(CalcOperator.DIV) }
        view.findViewById<Button>(R.id.btnCalcEquals).setOnClickListener { onCalcEquals() }
        view.findViewById<Button>(R.id.btnCalcClear).setOnClickListener { resetCalcState() }
        view.findViewById<Button>(R.id.btnCalcBackspace).setOnClickListener { backspaceCalcInput() }
    }

    private fun appendTypeInput(symbol: String) {
        val current = etInput.text.toString()
        val updated = when (symbol) {
            "-" -> if (current.isEmpty()) "-" else current
            "." -> when {
                current.contains(".") -> current
                current.isEmpty() -> "0."
                current == "-" -> "-0."
                else -> "$current."
            }
            else -> if (current == "0") symbol else current + symbol
        }
        if (updated != current) {
            etInput.setText(updated)
            resetTypeResult()
        }
    }

    private fun clearTypeInput() {
        etInput.setText("")
        resetTypeResult()
    }

    private fun backspaceTypeInput() {
        val current = etInput.text.toString()
        if (current.isNotEmpty()) {
            etInput.setText(current.dropLast(1))
            resetTypeResult()
        }
    }

    private fun resetTypeResult() {
        tvError.text = ""
        tvResultType.setText(R.string.result_type_placeholder)
    }

    private fun appendCalcDigit(digit: String) {
        clearCalcError()
        if (calcJustEvaluated && calcPendingOperator == null) {
            resetCalcState()
        }
        calcCurrentInput = if (calcCurrentInput == "0") digit else calcCurrentInput + digit
        updateCalcDisplay(calcCurrentInput)
    }

    private fun appendCalcDot() {
        clearCalcError()
        if (calcJustEvaluated && calcPendingOperator == null) {
            resetCalcState()
        }
        if (calcCurrentInput.contains(".")) return
        calcCurrentInput = if (calcCurrentInput.isEmpty()) "0." else "$calcCurrentInput."
        updateCalcDisplay(calcCurrentInput)
    }

    private fun backspaceCalcInput() {
        clearCalcError()
        if (calcJustEvaluated && calcPendingOperator == null) {
            resetCalcState()
            return
        }
        if (calcCurrentInput.isNotEmpty()) {
            calcCurrentInput = calcCurrentInput.dropLast(1)
            updateCalcDisplay(calcCurrentInput.ifEmpty { getString(R.string.calc_display_default) })
        }
    }

    private fun onCalcOperator(operator: CalcOperator) {
        clearCalcError()
        val currentValue = if (calcCurrentInput.isNotEmpty()) parseCalcInput() else null
        if (calcCurrentInput.isNotEmpty() && currentValue == null) return

        when {
            calcStoredValue == null -> {
                calcStoredValue = currentValue ?: 0.0
            }
            calcPendingOperator != null && currentValue != null -> {
                val result = calculateOperation(calcStoredValue!!, currentValue, calcPendingOperator!!)
                    ?: return
                calcStoredValue = result
            }
            currentValue != null -> {
                calcStoredValue = currentValue
            }
        }

        calcPendingOperator = operator
        calcCurrentInput = ""
        calcJustEvaluated = false
        updateCalcDisplay(formatNumber(calcStoredValue ?: 0.0))
    }

    private fun onCalcEquals() {
        clearCalcError()
        val stored = calcStoredValue ?: return
        val operator = calcPendingOperator ?: return
        val current = parseCalcInput() ?: return
        val result = calculateOperation(stored, current, operator) ?: return

        calcStoredValue = result
        calcPendingOperator = null
        calcCurrentInput = formatNumber(result)
        calcJustEvaluated = true
        updateCalcDisplay(calcCurrentInput)
    }

    private fun resetCalcState() {
        calcStoredValue = null
        calcPendingOperator = null
        calcCurrentInput = ""
        calcJustEvaluated = false
        tvCalcError.text = ""
        updateCalcDisplay(getString(R.string.calc_display_default))
    }

    private fun clearCalcError() {
        if (tvCalcError.text.isNotEmpty()) {
            tvCalcError.text = ""
        }
    }

    private fun updateCalcDisplay(value: String) {
        tvCalcDisplay.text = value
    }

    private fun parseCalcInput(): Double? {
        val value = calcCurrentInput.toDoubleOrNull()
        if (value == null) {
            handleCalcError(R.string.error_calc_invalid)
            return null
        }
        return value
    }

    private fun calculateOperation(left: Double, right: Double, operator: CalcOperator): Double? {
        val result = when (operator) {
            CalcOperator.ADD -> left + right
            CalcOperator.SUB -> left - right
            CalcOperator.MUL -> left * right
            CalcOperator.DIV -> if (right == 0.0) {
                handleCalcError(R.string.error_div_zero)
                return null
            } else {
                left / right
            }
        }

        if (result.isInfinite() || result.isNaN()) {
            handleCalcError(R.string.error_calc_overflow)
            return null
        }

        return result
    }

    private fun handleCalcError(errorRes: Int) {
        tvCalcError.setText(errorRes)
        calcStoredValue = null
        calcPendingOperator = null
        calcCurrentInput = ""
        calcJustEvaluated = false
        updateCalcDisplay(getString(R.string.calc_display_default))
    }

    private fun formatNumber(value: Double): String {
        val longValue = value.toLong()
        return if (value == longValue.toDouble()) {
            longValue.toString()
        } else {
            value.toString()
        }
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
                    tvResultType.text = getString(R.string.result_type, result.typeLabel)
                    maybeAnimate(tvResultType, R.anim.alpha_pulse)
                }
            }
        }
    }

    private fun showError(errorRes: Int) {
        tvError.setText(errorRes)
        tvResultType.setText(R.string.result_type_placeholder)
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

    private enum class CalcOperator {
        ADD,
        SUB,
        MUL,
        DIV,
    }

    private data class TypeResult(
        val typeLabel: String? = null,
        val errorRes: Int? = null,
    )
}
