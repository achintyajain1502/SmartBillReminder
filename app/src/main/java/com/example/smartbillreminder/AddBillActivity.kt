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

class AddBillActivity : AppCompatActivity() {

    private lateinit var etBillName: EditText
    private lateinit var etAmount: EditText
    private lateinit var etNotes: EditText

    private lateinit var spinnerCategory: Spinner
    private lateinit var spinnerRecurrence: Spinner

    private lateinit var btnDate: Button
    private lateinit var btnSaveBill: Button

    private var selectedDate = ""

    private lateinit var databaseHelper: BillDatabaseHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_add_bill)

        databaseHelper = BillDatabaseHelper(this)

        etBillName = findViewById(R.id.etBillName)
        etAmount = findViewById(R.id.etAmount)
        etNotes = findViewById(R.id.etNotes)

        spinnerCategory = findViewById(R.id.spinnerCategory)
        spinnerRecurrence = findViewById(R.id.spinnerRecurrence)

        btnDate = findViewById(R.id.btnDate)
        btnSaveBill = findViewById(R.id.btnSaveBill)

        setupSpinners()

        btnDate.setOnClickListener {
            showDatePicker()
        }

        btnSaveBill.setOnClickListener {
            saveBill()
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

    private fun saveBill() {

        val billName = etBillName.text.toString().trim()
        val amountText = etAmount.text.toString().trim()
        val notes = etNotes.text.toString().trim()

        if (billName.isEmpty()) {
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

        val success = databaseHelper.insertBill(
            name = billName,
            amount = amount,
            category = category,
            date = selectedDate,
            recurrence = recurrence,
            notes = notes
        )

        if (success) {

            Toast.makeText(
                this,
                "Bill saved successfully!",
                Toast.LENGTH_SHORT
            ).show()

            BillAlarmScheduler.scheduleReminder(
                context = this,
                billId = databaseHelper.getAllBills().first().id,
                billName = billName,
                amount = amount,
                dueDate = selectedDate
            )
            finish()

        } else {

            Toast.makeText(
                this,
                "Failed to save bill",
                Toast.LENGTH_SHORT
            ).show()
        }
    }
}