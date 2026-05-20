package com.example.task76

import android.database.Cursor
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {
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
            executeSelectedOperation()
        }

        operationSpinner.setSelection(0)
        updateVisibility()
        operationSpinner.onItemSelectedListener = SimpleItemSelectedListener { updateVisibility() }

        executeSelectedOperation()
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

    private fun executeSelectedOperation() {
        when (operationSpinner.selectedItemPosition) {
            0 -> showAllRecords()
            1 -> showAggregate()
            2 -> showFilteredRecords()
            3 -> showGroupedRecords()
            4 -> showSortedRecords()
        }
    }

    private fun showAllRecords() {
        contentResolver.query(PizzeriaContract.URI_ORDER_DETAILS, null, null, null, null).use { cursor ->
            resultsView.text = formatOrderRows(cursor)
        }
    }

    private fun showAggregate() {
        val function = aggregateFunctions[aggregateSpinner.selectedItemPosition]
        val uri = PizzeriaContract.URI_ANALYTICS.buildUpon()
            .appendQueryParameter("mode", "aggregate")
            .appendQueryParameter("function", function)
            .build()

        contentResolver.query(uri, null, null, null, null).use { cursor ->
            resultsView.text = if (cursor != null && cursor.moveToFirst()) {
                val value = cursor.getDouble(cursor.getColumnIndexOrThrow("aggregate_value"))
                "$function(total_cost) = $value"
            } else {
                getString(R.string.no_results)
            }
        }
    }

    private fun showFilteredRecords() {
        val threshold = thresholdInput.text.toString().toDoubleOrNull()
        if (threshold == null) {
            Toast.makeText(this, R.string.enter_threshold, Toast.LENGTH_SHORT).show()
            return
        }

        val uri = PizzeriaContract.URI_ANALYTICS.buildUpon()
            .appendQueryParameter("mode", "filter")
            .appendQueryParameter("threshold", threshold.toString())
            .build()

        contentResolver.query(uri, null, null, null, null).use { cursor ->
            resultsView.text = formatOrderRows(cursor)
        }
    }

    private fun showGroupedRecords() {
        val field = groupFields[groupSpinner.selectedItemPosition]
        val uri = PizzeriaContract.URI_ANALYTICS.buildUpon()
            .appendQueryParameter("mode", "group")
            .appendQueryParameter("field", field)
            .build()

        contentResolver.query(uri, null, null, null, null).use { cursor ->
            resultsView.text = formatGroupRows(cursor, field)
        }
    }

    private fun showSortedRecords() {
        val field = sortFields[sortFieldSpinner.selectedItemPosition]
        val direction = sortDirections[sortDirectionSpinner.selectedItemPosition]
        val uri = PizzeriaContract.URI_ANALYTICS.buildUpon()
            .appendQueryParameter("mode", "sort")
            .appendQueryParameter("field", field)
            .appendQueryParameter("direction", direction)
            .build()

        contentResolver.query(uri, null, null, null, null).use { cursor ->
            resultsView.text = formatOrderRows(cursor)
        }
    }

    private fun formatOrderRows(cursor: Cursor?): String {
        if (cursor == null || !cursor.moveToFirst()) {
            return getString(R.string.no_results)
        }

        return buildString {
            do {
                appendLine(
                    "Заказ #${cursor.getInt(cursor.getColumnIndexOrThrow("order_id"))}: " +
                        "${cursor.getString(cursor.getColumnIndexOrThrow("dish_name"))} " +
                        "(${cursor.getString(cursor.getColumnIndexOrThrow("category"))})"
                )
                appendLine(
                    "Цена: ${cursor.getDouble(cursor.getColumnIndexOrThrow("price"))}, " +
                        "кол-во: ${cursor.getInt(cursor.getColumnIndexOrThrow("quantity"))}, " +
                        "сумма: ${cursor.getDouble(cursor.getColumnIndexOrThrow("total_cost"))}"
                )
                appendLine(
                    "Акция: ${cursor.getString(cursor.getColumnIndexOrThrow("promotion_title"))}, " +
                        "оплата: ${cursor.getString(cursor.getColumnIndexOrThrow("payment_method"))}"
                )
                appendLine(
                    "Курьер: ${cursor.getString(cursor.getColumnIndexOrThrow("courier_name"))}, " +
                        "способ доставки: ${cursor.getString(cursor.getColumnIndexOrThrow("delivery_method"))}"
                )
                appendLine("Адрес: ${cursor.getString(cursor.getColumnIndexOrThrow("delivery_address"))}")
                appendLine("Дата: ${cursor.getString(cursor.getColumnIndexOrThrow("order_date"))}")
                appendLine()
            } while (cursor.moveToNext())
        }.trim()
    }

    private fun formatGroupRows(cursor: Cursor?, field: String): String {
        if (cursor == null || !cursor.moveToFirst()) {
            return getString(R.string.no_results)
        }

        return buildString {
            do {
                appendLine("Группа ($field): ${cursor.getString(cursor.getColumnIndexOrThrow("group_value"))}")
                appendLine("COUNT: ${cursor.getInt(cursor.getColumnIndexOrThrow("order_count"))}")
                appendLine("SUM: ${cursor.getDouble(cursor.getColumnIndexOrThrow("sum_total_cost"))}")
                appendLine("AVG: ${cursor.getDouble(cursor.getColumnIndexOrThrow("avg_total_cost"))}")
                appendLine("MIN: ${cursor.getDouble(cursor.getColumnIndexOrThrow("min_total_cost"))}")
                appendLine("MAX: ${cursor.getDouble(cursor.getColumnIndexOrThrow("max_total_cost"))}")
                appendLine()
            } while (cursor.moveToNext())
        }.trim()
    }
}
