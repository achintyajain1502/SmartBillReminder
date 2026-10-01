package com.example.smartbillreminder.database

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import java.util.Calendar

class BillDatabaseHelper(context: Context) :
    SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "SmartBill.db"
        private const val DATABASE_VERSION = 1

        private const val TABLE_BILLS = "bills"

        private const val COLUMN_ID = "id"
        private const val COLUMN_NAME = "name"
        private const val COLUMN_AMOUNT = "amount"
        private const val COLUMN_CATEGORY = "category"
        private const val COLUMN_DATE = "due_date"
        private const val COLUMN_RECURRENCE = "recurrence"
        private const val COLUMN_NOTES = "notes"
    }

    override fun onCreate(db: SQLiteDatabase) {

        val createTable = """
            CREATE TABLE $TABLE_BILLS (
                $COLUMN_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COLUMN_NAME TEXT NOT NULL,
                $COLUMN_AMOUNT REAL NOT NULL,
                $COLUMN_CATEGORY TEXT NOT NULL,
                $COLUMN_DATE TEXT NOT NULL,
                $COLUMN_RECURRENCE TEXT NOT NULL,
                $COLUMN_NOTES TEXT
            )
        """.trimIndent()

        db.execSQL(createTable)
    }

    override fun onUpgrade(
        db: SQLiteDatabase,
        oldVersion: Int,
        newVersion: Int
    ) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_BILLS")
        onCreate(db)
    }

    fun insertBill(
        name: String,
        amount: Double,
        category: String,
        date: String,
        recurrence: String,
        notes: String
    ): Boolean {

        val db = writableDatabase

        val values = ContentValues().apply {
            put(COLUMN_NAME, name)
            put(COLUMN_AMOUNT, amount)
            put(COLUMN_CATEGORY, category)
            put(COLUMN_DATE, date)
            put(COLUMN_RECURRENCE, recurrence)
            put(COLUMN_NOTES, notes)
        }

        val result = db.insert(TABLE_BILLS, null, values)

        db.close()

        return result != -1L
    }

    fun getAllBills(): List<com.example.smartbillreminder.adapter.Bill> {

        val bills = mutableListOf<com.example.smartbillreminder.adapter.Bill>()

        val db = readableDatabase

        val cursor = db.query(
            TABLE_BILLS,
            null,
            null,
            null,
            null,
            null,
            "$COLUMN_ID DESC"
        )

        if (cursor.moveToFirst()) {

            do {

                val bill = com.example.smartbillreminder.adapter.Bill(
                    id = cursor.getInt(
                        cursor.getColumnIndexOrThrow(COLUMN_ID)
                    ),
                    name = cursor.getString(
                        cursor.getColumnIndexOrThrow(COLUMN_NAME)
                    ),
                    amount = cursor.getDouble(
                        cursor.getColumnIndexOrThrow(COLUMN_AMOUNT)
                    ),
                    category = cursor.getString(
                        cursor.getColumnIndexOrThrow(COLUMN_CATEGORY)
                    ),
                    date = cursor.getString(
                        cursor.getColumnIndexOrThrow(COLUMN_DATE)
                    ),
                    recurrence = cursor.getString(
                        cursor.getColumnIndexOrThrow(COLUMN_RECURRENCE)
                    ),
                    notes = cursor.getString(
                        cursor.getColumnIndexOrThrow(COLUMN_NOTES)
                    )
                )

                bills.add(bill)

            } while (cursor.moveToNext())
        }

        cursor.close()
        db.close()

        return bills
    }
    fun updateBill(
        id: Int,
        name: String,
        amount: Double,
        category: String,
        date: String,
        recurrence: String,
        notes: String
    ): Boolean {

        val db = writableDatabase

        val values = ContentValues().apply {
            put(COLUMN_NAME, name)
            put(COLUMN_AMOUNT, amount)
            put(COLUMN_CATEGORY, category)
            put(COLUMN_DATE, date)
            put(COLUMN_RECURRENCE, recurrence)
            put(COLUMN_NOTES, notes)
        }

        val result = db.update(
            TABLE_BILLS,
            values,
            "$COLUMN_ID = ?",
            arrayOf(id.toString())
        )

        db.close()

        return result > 0
    }

    fun deleteBill(id: Int): Boolean {

        val db = writableDatabase

        val result = db.delete(
            TABLE_BILLS,
            "$COLUMN_ID = ?",
            arrayOf(id.toString())
        )

        db.close()

        return result > 0
    }

    fun getMonthlyExpense(): Double {

        val db = readableDatabase

        val cursor = db.rawQuery(
            "SELECT SUM($COLUMN_AMOUNT) FROM $TABLE_BILLS",
            null
        )

        var total = 0.0

        if (cursor.moveToFirst()) {
            total = cursor.getDouble(0)
        }

        cursor.close()
        db.close()

        return total
    }

    fun getDueSoonCount(): Int {

        val bills = getAllBills()

        val today = Calendar.getInstance()

        val sevenDaysLater = Calendar.getInstance()
        sevenDaysLater.add(Calendar.DAY_OF_YEAR, 7)

        var count = 0

        for (bill in bills) {

            try {

                val parts = bill.date.split("/")

                if (parts.size == 3) {

                    val day = parts[0].toInt()
                    val month = parts[1].toInt() - 1
                    val year = parts[2].toInt()

                    val dueDate = Calendar.getInstance()

                    dueDate.set(
                        year,
                        month,
                        day,
                        0,
                        0,
                        0
                    )

                    dueDate.set(
                        Calendar.MILLISECOND,
                        0
                    )

                    val todayDate = Calendar.getInstance()

                    todayDate.set(
                        Calendar.HOUR_OF_DAY,
                        0
                    )

                    todayDate.set(
                        Calendar.MINUTE,
                        0
                    )

                    todayDate.set(
                        Calendar.SECOND,
                        0
                    )

                    todayDate.set(
                        Calendar.MILLISECOND,
                        0
                    )

                    if (
                        !dueDate.before(todayDate) &&
                        !dueDate.after(sevenDaysLater)
                    ) {
                        count++
                    }
                }

            } catch (e: Exception) {
                // Ignore invalid dates
            }
        }

        return count
    }

}