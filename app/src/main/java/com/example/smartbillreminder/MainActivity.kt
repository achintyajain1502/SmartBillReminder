package com.example.smartbillreminder

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.smartbillreminder.adapter.BillAdapter
import com.example.smartbillreminder.database.BillDatabaseHelper
import android.Manifest
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
class MainActivity : AppCompatActivity() {

    private lateinit var recyclerBills: RecyclerView
    private lateinit var billAdapter: BillAdapter
    private lateinit var databaseHelper: BillDatabaseHelper

    private lateinit var tvTotalExpense: TextView
    private lateinit var tvDueSoon: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_main)

        if (
            android.os.Build.VERSION.SDK_INT >=
            android.os.Build.VERSION_CODES.TIRAMISU
        ) {

            if (
                ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {

                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                    100
                )
            }
        }

        databaseHelper = BillDatabaseHelper(this)

        recyclerBills = findViewById(R.id.recyclerBills)

        tvTotalExpense = findViewById(R.id.tvTotalExpense)
        tvDueSoon = findViewById(R.id.tvDueSoon)

        recyclerBills.layoutManager =
            LinearLayoutManager(this)

        billAdapter = BillAdapter(emptyList())

        val etSearchBill =
            findViewById<EditText>(R.id.etSearchBill)

        etSearchBill.addTextChangedListener(
            object : android.text.TextWatcher {

                override fun beforeTextChanged(
                    s: CharSequence?,
                    start: Int,
                    count: Int,
                    after: Int
                ) {
                }

                override fun onTextChanged(
                    s: CharSequence?,
                    start: Int,
                    before: Int,
                    count: Int
                ) {

                    billAdapter.filterBills(
                        s.toString().trim()
                    )
                }

                override fun afterTextChanged(
                    s: android.text.Editable?
                ) {
                }
            }
        )

        recyclerBills.adapter = billAdapter

        val btnAddBill =
            findViewById<Button>(R.id.btnAddBill)

        btnAddBill.setOnClickListener {

            val intent =
                Intent(this, AddBillActivity::class.java)

            startActivity(intent)
        }
        val btnTestNotification =
            findViewById<Button>(R.id.btnTestNotification)

        btnTestNotification.setOnClickListener {

            val intent = Intent(
                this,
                BillReminderReceiver::class.java
            )

            intent.putExtra("bill_name", "Netflix")
            intent.putExtra("bill_amount", 649.0)

            sendBroadcast(intent)

            Toast.makeText(
                this,
                "Test notification sent!",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    override fun onResume() {
        super.onResume()

        loadBills()
        updateDashboard()
    }

    private fun loadBills() {

        val bills =
            databaseHelper.getAllBills()

        billAdapter.updateBills(bills)
    }

    private fun updateDashboard() {

        val totalExpense =
            databaseHelper.getMonthlyExpense()

        val dueSoonCount =
            databaseHelper.getDueSoonCount()

        tvTotalExpense.text =
            "₹${String.format("%.2f", totalExpense)}"

        tvDueSoon.text =
            dueSoonCount.toString()
    }
}