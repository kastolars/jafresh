package com.kastolars.expirationreminderproject

import android.app.Activity
import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.DatePicker
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import java.util.*

class ItemActivity : AppCompatActivity(), DatePickerDialog.OnDateSetListener {

    private lateinit var mEditTextView: EditText
    private lateinit var mExpirationDateTextView: TextView
    private val tag = "exprem" + ItemActivity::class.java.simpleName
    private val cal: Calendar = Calendar.getInstance(TimeZone.getDefault())

    private var year = cal.get(Calendar.YEAR)
    private var month = cal.get(Calendar.MONTH)
    private var dayOfMonth = cal.get(Calendar.DAY_OF_MONTH)

    override fun onCreate(savedInstanceState: Bundle?) {
        Log.v(tag, "onCreate called")
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_item)

        mEditTextView = findViewById(R.id.edit_text)
        mExpirationDateTextView = findViewById(R.id.current_expiration_date)
        mExpirationDateTextView.text = String.format("%d-%d-%d", month + 1, dayOfMonth, year)

        val mExpirationDateButton: Button = findViewById(R.id.set_expiration_date_button)

        mExpirationDateButton.setOnClickListener {
            DatePickerDialog(
                this@ItemActivity,
                this@ItemActivity,
                year,
                month,
                dayOfMonth
            ).show()
        }

        val mDoneButton: Button = findViewById(R.id.done_button)
        mDoneButton.setOnClickListener {
            val intent = Intent()
            intent.putExtra("name", mEditTextView.text.toString())
            cal.set(year, month, dayOfMonth)
            intent.putExtra("date", cal.time.time)
            setResult(Activity.RESULT_OK, intent)
            finish()
        }
    }

    override fun onDateSet(view: DatePicker?, year: Int, month: Int, dayOfMonth: Int) {
        Log.v(tag, "onDateSet called")
        this.year = year
        this.month = month
        this.dayOfMonth = dayOfMonth
        mExpirationDateTextView.text = String.format("%d-%d-%d", month + 1, dayOfMonth, year)
    }
}
