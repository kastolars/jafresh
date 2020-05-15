package com.kastolars.expirationreminderproject.activities

import android.app.Activity
import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.animation.Animation
import android.view.animation.TranslateAnimation
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.kastolars.expirationreminderproject.R
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
        mExpirationDateTextView.animate().alpha(1f).duration = 250

        val mExpirationDateButton: Button = findViewById(R.id.set_expiration_date_button)
        val expirationButtonAnim = TranslateAnimation(
            Animation.RELATIVE_TO_SELF, -1.0f, Animation.RELATIVE_TO_SELF, 0.0f,
            Animation.RELATIVE_TO_SELF, 1.0f, Animation.RELATIVE_TO_SELF, 0.0f
        )
        expirationButtonAnim.duration = 250
        mExpirationDateButton.animation = expirationButtonAnim

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
        var doneButtonAnim = TranslateAnimation(
            Animation.RELATIVE_TO_SELF, 1.0f, Animation.RELATIVE_TO_SELF, 0.0f,
            Animation.RELATIVE_TO_SELF, 1.0f, Animation.RELATIVE_TO_SELF, 0.0f
        )
        doneButtonAnim.duration = 250
        mDoneButton.animation = doneButtonAnim
        mDoneButton.setOnClickListener {
            Log.v(tag, "Done button clicked")
            val intent = Intent()
            intent.putExtra("name", mEditTextView.text.toString())
            cal.set(year, month, dayOfMonth, 6, 0)
            // TODO: also make sure the item has a name
            if (!cal.time.before(Calendar.getInstance(TimeZone.getDefault()).time)) {
                intent.putExtra("date", cal.time.time)
                setResult(Activity.RESULT_OK, intent)
                finish()
            } else {
                Toast.makeText(this, "Can't use that expiration date", Toast.LENGTH_SHORT).show()
            }
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
