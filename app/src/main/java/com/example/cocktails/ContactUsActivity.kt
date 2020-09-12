package com.example.cocktails

import android.annotation.SuppressLint
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.addTextChangedListener
import com.google.android.material.textfield.TextInputLayout


class ContactUsActivity: AppCompatActivity() {

    private val ACTIVITY_TITLE: String = "CONTACT US"
    private val MAX_CHARACTERS: Int = 150

    private lateinit var content: TextInputLayout
    private lateinit var contentText: EditText
    private lateinit var subjectText: EditText
    private lateinit var submitButton: Button

    @SuppressLint("InflateParams")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.contact_us_layout)

        supportActionBar?.title = ACTIVITY_TITLE

        initViews()
    }

    private fun initViews() {
        content = findViewById(R.id.content)
        content.counterMaxLength = MAX_CHARACTERS
        contentText = content.editText!!

        contentText.addTextChangedListener(contentTextWatcher())

        subjectText = findViewById(R.id.subject_input)

        submitButton = findViewById(R.id.submit_button)
        submitButton.setOnClickListener{
            // todo send email
        }
    }

    private fun contentTextWatcher(): TextWatcher {
        return object : TextWatcher {
            override fun afterTextChanged(arg0: Editable) {
                val enableButton: Boolean = contentText.text.length <= MAX_CHARACTERS
                submitButton.isEnabled = enableButton
            }

            override fun beforeTextChanged(
                s: CharSequence,
                start: Int,
                count: Int,
                after: Int
            ) {
            }

            override fun onTextChanged(
                s: CharSequence,
                start: Int,
                before: Int,
                count: Int
            ) {
            }
        }
    }
}
