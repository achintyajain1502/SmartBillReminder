package com.example.smartbillreminder

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.smartbillreminder.database.BillDatabaseHelper

class BillDetailActivity : AppCompatActivity() {

    private lateinit var databaseHelper: BillDatabaseHelper

    private var billId = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_bill_detail)

        databaseHelper = BillDatabaseHelper(this)

        billId = intent.getIntExtra("bill_id", 0)

        val tvName = findViewById<TextView>(R.id.tvDetailName)
        val tvAmount = findViewById<TextView>(R.id.tvDetailAmount)
        val tvCategory = findViewById<TextView>(R.id.tvDetailCategory)
        val tvDate = findViewById<TextView>(R.id.tvDetailDate)
        val tvRecurrence = findViewById<TextView>(R.id.tvDetailRecurrence)
        val tvNotes = findViewById<TextView>(R.id.tvDetailNotes)

        val btnBack = findViewById<Button>(R.id.btnBack)
        val btnEdit = findViewById<Button>(R.id.btnEdit)
        val btnDelete = findViewById<Button>(R.id.btnDelete)

        val billName = intent.getStringExtra("bill_name") ?: ""
        val billAmount = intent.getDoubleExtra("bill_amount", 0.0)
        val billCategory = intent.getStringExtra("bill_category") ?: ""
        val billDate = intent.getStringExtra("bill_date") ?: ""
        val billRecurrence = intent.getStringExtra("bill_recurrence") ?: ""
        val billNotes = intent.getStringExtra("bill_notes") ?: ""

        tvName.text = billName
        tvAmount.text = "₹$billAmount"
        tvCategory.text = "Category: $billCategory"
        tvDate.text = "Due Date: $billDate"
        tvRecurrence.text = "Recurrence: $billRecurrence"

        if (billNotes.isEmpty()) {
            tvNotes.text = "Notes: No notes added"
        } else {
            tvNotes.text = "Notes: $billNotes"
        }

        btnBack.setOnClickListener {
            finish()
        }

        btnEdit.setOnClickListener {

            val intent = Intent(
                this,
                EditBillActivity::class.java
            )

            intent.putExtra("bill_id", billId)
            intent.putExtra("bill_name", billName)
            intent.putExtra("bill_amount", billAmount)
            intent.putExtra("bill_category", billCategory)
            intent.putExtra("bill_date", billDate)
            intent.putExtra("bill_recurrence", billRecurrence)
            intent.putExtra("bill_notes", billNotes)

            startActivity(intent)
        }

        btnDelete.setOnClickListener {
            showDeleteConfirmation()
        }
    }

    private fun showDeleteConfirmation() {

        AlertDialog.Builder(this)
            .setTitle("Delete Bill")
            .setMessage("Are you sure you want to delete this bill?")
            .setNegativeButton("Cancel", null)
            .setPositiveButton("Delete") { _, _ ->

                val success = databaseHelper.deleteBill(billId)

                if (success) {

                    Toast.makeText(
                        this,
                        "Bill deleted",
                        Toast.LENGTH_SHORT
                    ).show()

                    finish()

                } else {

                    Toast.makeText(
                        this,
                        "Failed to delete bill",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
            .show()
    }
}