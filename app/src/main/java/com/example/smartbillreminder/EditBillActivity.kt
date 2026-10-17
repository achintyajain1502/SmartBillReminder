package com.example.smartbillreminder

import android.app.DatePickerDialog
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.smartbillreminder.database.BillDatabaseHelper
import java.util.Calendar

class EditBillActivity : AppCompatActivity() {

    private lateinit var etAccountNumber: EditText
    private lateinit var etBillName: EditText
    private lateinit var etAmount: EditText
    private lateinit var etNotes: EditText

    private lateinit var spinnerCategory: Spinner
    private lateinit var spinnerRecurrence: Spinner

    private lateinit var btnDate: Button
    private lateinit var btnUpdateBill: Button

    private lateinit var databaseHelper: BillDatabaseHelper

    private var originalAccountNumber = ""
    private var selectedDate = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_bill)

        databaseHelper = BillDatabaseHelper(this)

        originalAccountNumber =
            intent.getStringExtra("bill_account_number") ?: ""

        etAccountNumber = findViewById(R.id.etEditAccountNumber)
        etBillName = findViewById(R.id.etEditBillName)
        etAmount = findViewById(R.id.etEditAmount)
        etNotes = findViewById(R.id.etEditNotes)

        spinnerCategory = findViewById(R.id.spinnerEditCategory)
        spinnerRecurrence = findViewById(R.id.spinnerEditRecurrence)

        btnDate = findViewById(R.id.btnEditDate)
        btnUpdateBill = findViewById(R.id.btnUpdateBill)

        setupSpinners()
        loadBillData()

        btnDate.setOnClickListener {
            showDatePicker()
        }

        btnUpdateBill.setOnClickListener {
            updateBill()
        }
    }

    private fun setupSpinners() {
        val categories = arrayOf(
            "Utilities",
            "Entertainment",
            "Education",
            "Shopping",
            "Internet",
            "Mobile",
            "Other"
        )

        spinnerCategory.adapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_dropdown_item,
            categories
        )

        val recurrence = arrayOf(
            "One Time",
            "Monthly",
            "Yearly"
        )

        spinnerRecurrence.adapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_dropdown_item,
            recurrence
        )
    }

    private fun loadBillData() {
        etAccountNumber.setText(originalAccountNumber)
        etAccountNumber.isEnabled = false

        etBillName.setText(
            intent.getStringExtra("bill_name") ?: ""
        )

        val amount = intent.getDoubleExtra("bill_amount", 0.0)
        etAmount.setText(amount.toString())

        etNotes.setText(
            intent.getStringExtra("bill_notes") ?: ""
        )

        val category = intent.getStringExtra("bill_category") ?: ""
        val recurrence = intent.getStringExtra("bill_recurrence") ?: ""

        selectedDate = intent.getStringExtra("bill_date") ?: ""
        btnDate.text = selectedDate

        // Find the saved category without using ArrayAdapter<*>
        val categoryPosition = (0 until spinnerCategory.count)
            .firstOrNull {
                spinnerCategory.getItemAtPosition(it).toString() == category
            } ?: -1

        if (categoryPosition >= 0) {
            spinnerCategory.setSelection(categoryPosition)
        }

        // Find the saved recurrence without using ArrayAdapter<*>
        val recurrencePosition = (0 until spinnerRecurrence.count)
            .firstOrNull {
                spinnerRecurrence.getItemAtPosition(it).toString() == recurrence
            } ?: -1

        if (recurrencePosition >= 0) {
            spinnerRecurrence.setSelection(recurrencePosition)
        }
    }
    private fun showDatePicker() {
        val calendar = Calendar.getInstance()

        DatePickerDialog(
            this,
            { _, year, month, day ->
                selectedDate = "$day/${month + 1}/$year"
                btnDate.text = selectedDate
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    private fun updateBill() {
        val name = etBillName.text.toString().trim()
        val amountText = etAmount.text.toString().trim()
        val notes = etNotes.text.toString().trim()

        if (name.isEmpty()) {
            etBillName.error = "Enter bill name"
            etBillName.requestFocus()
            return
        }

        val amount = amountText.toDoubleOrNull()

        if (amount == null || amount <= 0) {
            etAmount.error = "Enter a valid amount"
            etAmount.requestFocus()
            return
        }

        if (selectedDate.isEmpty()) {
            Toast.makeText(
                this,
                "Please select a due date",
                Toast.LENGTH_SHORT
            ).show()
            return
        }

        if (originalAccountNumber.isBlank()) {
            Toast.makeText(
                this,
                "Account number is missing",
                Toast.LENGTH_SHORT
            ).show()
            return
        }

        val success = databaseHelper.updateBill(
            accountNumber = originalAccountNumber,
            name = name,
            amount = amount,
            category = spinnerCategory.selectedItem.toString(),
            date = selectedDate,
            recurrence = spinnerRecurrence.selectedItem.toString(),
            notes = notes
        )

        if (success) {
            Toast.makeText(
                this,
                "Bill updated successfully!",
                Toast.LENGTH_SHORT
            ).show()

            BillAlarmScheduler.cancelReminder(
                context = this,
                accountNumber = originalAccountNumber
            )

            BillAlarmScheduler.scheduleReminder(
                context = this,
                accountNumber = originalAccountNumber,
                billName = name,
                amount = amount,
                dueDate = selectedDate
            )

            finish()
        } else {
            Toast.makeText(
                this,
                "Failed to update bill",
                Toast.LENGTH_SHORT
            ).show()
        }
    }
}