package com.example.task77

import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    private lateinit var repository: PizzeriaRepository
    private lateinit var operationSpinner: Spinner
    private lateinit var aggregateSpinner: Spinner
    private lateinit var groupSpinner: Spinner
    private lateinit var sortFieldSpinner: Spinner
    private lateinit var sortDirectionSpinner: Spinner
    private lateinit var thresholdInput: EditText
    private lateinit var resultsView: TextView

    private val operations = listOf(
        "Все записи",
        "Агрегирующая функция",
        "Фильтр total_cost > X",
        "Группировка",
        "Сортировка"
    )
    private val aggregateFunctions = listOf("SUM", "MIN", "MAX", "COUNT", "AVG")
    private val groupFields = listOf("category", "courier_name", "payment_method", "delivery_method")
    private val sortFields = listOf("dish_name", "price", "total_cost", "order_date")
    private val sortDirections = listOf("ASC", "DESC")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        repository = PizzeriaRepository(applicationContext)
        operationSpinner = findViewById(R.id.operationSpinner)
        aggregateSpinner = findViewById(R.id.aggregateSpinner)
        groupSpinner = findViewById(R.id.groupSpinner)
        sortFieldSpinner = findViewById(R.id.sortFieldSpinner)
        sortDirectionSpinner = findViewById(R.id.sortDirectionSpinner)
        thresholdInput = findViewById(R.id.thresholdInput)
        resultsView = findViewById(R.id.resultsView)

        operationSpinner.adapter = spinnerAdapter(operations)
        aggregateSpinner.adapter = spinnerAdapter(aggregateFunctions)
        groupSpinner.adapter = spinnerAdapter(groupFields)
        sortFieldSpinner.adapter = spinnerAdapter(sortFields)
        sortDirectionSpinner.adapter = spinnerAdapter(sortDirections)

        findViewById<Button>(R.id.executeButton).setOnClickListener {
            lifecycleScope.launch { executeSelectedOperation() }
        }

        operationSpinner.onItemSelectedListener = SimpleItemSelectedListener { updateVisibility() }
        updateVisibility()

        lifecycleScope.launch {
            repository.ensureSeedData()
            executeSelectedOperation()
        }
    }

    private fun spinnerAdapter(items: List<String>): ArrayAdapter<String> {
        return ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, items)
    }

    private fun updateVisibility() {
        val selected = operationSpinner.selectedItemPosition
        aggregateSpinner.visibility = if (selected == 1) View.VISIBLE else View.GONE
        thresholdInput.visibility = if (selected == 2) View.VISIBLE else View.GONE
        groupSpinner.visibility = if (selected == 3) View.VISIBLE else View.GONE
        sortFieldSpinner.visibility = if (selected == 4) View.VISIBLE else View.GONE
        sortDirectionSpinner.visibility = if (selected == 4) View.VISIBLE else View.GONE
    }

    private suspend fun executeSelectedOperation() {
        when (operationSpinner.selectedItemPosition) {
            0 -> resultsView.text = formatOrderRows(repository.getAllOrders())
            1 -> {
                val function = aggregateFunctions[aggregateSpinner.selectedItemPosition]
                val result = repository.getAggregate(function)
                resultsView.text = if (result == null) {
                    getString(R.string.no_results)
                } else {
                    "${result.aggregate_function}(total_cost) = ${result.aggregate_value}"
                }
            }

            2 -> {
                val threshold = thresholdInput.text.toString().toDoubleOrNull()
                if (threshold == null) {
                    Toast.makeText(this, R.string.enter_threshold, Toast.LENGTH_SHORT).show()
                    return
                }
                resultsView.text = formatOrderRows(repository.getFilteredOrders(threshold))
            }

            3 -> {
                val field = groupFields[groupSpinner.selectedItemPosition]
                resultsView.text = formatGroupRows(repository.getGrouped(field), field)
            }

            4 -> {
                val field = sortFields[sortFieldSpinner.selectedItemPosition]
                val direction = sortDirections[sortDirectionSpinner.selectedItemPosition]
                resultsView.text = formatOrderRows(repository.getSorted(field, direction))
            }
        }
    }

    private fun formatOrderRows(rows: List<OrderDetailRow>): String {
        if (rows.isEmpty()) return getString(R.string.no_results)

        return buildString {
            rows.forEach { row ->
                appendLine("Заказ #${row.order_id}: ${row.dish_name} (${row.category})")
                appendLine("Цена: ${row.price}, кол-во: ${row.quantity}, сумма: ${row.total_cost}")
                appendLine("Акция: ${row.promotion_title}, оплата: ${row.payment_method}")
                appendLine("Курьер: ${row.courier_name}, способ доставки: ${row.delivery_method}")
                appendLine("Адрес: ${row.delivery_address}")
                appendLine("Дата: ${row.order_date}")
                appendLine()
            }
        }.trim()
    }

    private fun formatGroupRows(rows: List<GroupResultRow>, field: String): String {
        if (rows.isEmpty()) return getString(R.string.no_results)

        return buildString {
            rows.forEach { row ->
                appendLine("Группа ($field): ${row.group_value}")
                appendLine("COUNT: ${row.order_count}")
                appendLine("SUM: ${row.sum_total_cost}")
                appendLine("AVG: ${row.avg_total_cost}")
                appendLine("MIN: ${row.min_total_cost}")
                appendLine("MAX: ${row.max_total_cost}")
                appendLine()
            }
        }.trim()
    }
}
