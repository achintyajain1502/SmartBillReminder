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

    private lateinit var etBillName: EditText
    private lateinit var etAmount: EditText
    private lateinit var etNotes: EditText

    private lateinit var spinnerCategory: Spinner
    private lateinit var spinnerRecurrence: Spinner

    private lateinit var btnDate: Button
    private lateinit var btnUpdateBill: Button

    private lateinit var databaseHelper: BillDatabaseHelper

    private var billId = 0
    private var selectedDate = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_edit_bill)

        databaseHelper = BillDatabaseHelper(this)

        billId = intent.getIntExtra("bill_id", 0)

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

        val categoryAdapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_dropdown_item,
            categories
        )

        spinnerCategory.adapter = categoryAdapter

        val recurrence = arrayOf(
            "One Time",
            "Monthly",
            "Yearly"
        )

        val recurrenceAdapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_dropdown_item,
            recurrence
        )

        spinnerRecurrence.adapter = recurrenceAdapter
    }

    private fun loadBillData() {

        val name = intent.getStringExtra("bill_name") ?: ""
        val amount = intent.getDoubleExtra("bill_amount", 0.0)
        val category = intent.getStringExtra("bill_category") ?: ""
        val date = intent.getStringExtra("bill_date") ?: ""
        val recurrence = intent.getStringExtra("bill_recurrence") ?: ""
        val notes = intent.getStringExtra("bill_notes") ?: ""

        etBillName.setText(name)
        etAmount.setText(amount.toString())
        etNotes.setText(notes)

        selectedDate = date
        btnDate.text = date

        val categoryPosition =
            (spinnerCategory.adapter as ArrayAdapter<String>)
                .getPosition(category)

        if (categoryPosition >= 0) {
            spinnerCategory.setSelection(categoryPosition)
        }

        val recurrencePosition =
            (spinnerRecurrence.adapter as ArrayAdapter<String>)
                .getPosition(recurrence)

        if (recurrencePosition >= 0) {
            spinnerRecurrence.setSelection(recurrencePosition)
        }
    }

    private fun showDatePicker() {

        val calendar = Calendar.getInstance()

        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        val datePicker = DatePickerDialog(
            this,
            { _, selectedYear, selectedMonth, selectedDay ->

                selectedDate =
                    "$selectedDay/${selectedMonth + 1}/$selectedYear"

                btnDate.text = selectedDate
            },
            year,
            month,
            day
        )

        datePicker.show()
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

        if (amountText.isEmpty()) {
            etAmount.error = "Enter amount"
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

        val amount = amountText.toDoubleOrNull()

        if (amount == null || amount <= 0) {
            etAmount.error = "Enter a valid amount"
            etAmount.requestFocus()
            return
        }

        val category = spinnerCategory.selectedItem.toString()
        val recurrence = spinnerRecurrence.selectedItem.toString()

        val success = databaseHelper.updateBill(
            id = billId,
            name = name,
            amount = amount,
            category = category,
            date = selectedDate,
            recurrence = recurrence,
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
                billId = billId
            )

            BillAlarmScheduler.scheduleReminder(
                context = this,
                billId = billId,
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