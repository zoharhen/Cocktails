package com.example.cocktails

import android.content.ActivityNotFoundException
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.textfield.TextInputLayout


class ContactUsActivity: AppCompatActivity() {

    private val ACTIVITY_TITLE: String = "CONTACT US"
    private val EMAIL_ADDRESS: String = "cocktails.ppc@gmail.com"
    private val MSG_TO_USER: String = "Contact Us"
    private val MAX_CHARACTERS: Int = 150

    private lateinit var content: TextInputLayout
    private lateinit var contentText: EditText
    private lateinit var subjectText: EditText
    private lateinit var submitButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.contact_us_layout)

        supportActionBar?.title = ACTIVITY_TITLE

        initViews()
    }

    /**
     * Initialize the activity's views.
     */
    private fun initViews() {
        content = findViewById(R.id.content)
        content.counterMaxLength = MAX_CHARACTERS
        contentText = content.editText!!

        contentText.addTextChangedListener(contentTextWatcher())

        subjectText = findViewById(R.id.subject_input)

        submitButton = findViewById(R.id.submit_button)
        submitButton.setOnClickListener{
            submitAction()
        }
    }

    private fun clearViews() {
        contentText.setText("")
        subjectText.setText("")
    }

    /**
     * Define the submit button's functionality
     */
    private fun submitAction() {
        val intent = Intent(Intent.ACTION_SEND)
        intent.type = "message/rfc822"
        intent.putExtra(Intent.EXTRA_EMAIL, arrayOf(EMAIL_ADDRESS))
        intent.putExtra(Intent.EXTRA_SUBJECT, subjectText.text.toString())
        intent.putExtra(Intent.EXTRA_TEXT, contentText.text)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        try {
            startActivity(Intent.createChooser(intent, MSG_TO_USER))
        } catch (ex: ActivityNotFoundException) {
            Toast.makeText(
                this,
                "There are no email clients installed.",
                Toast.LENGTH_SHORT
            ).show()
        }

        clearViews()
    }

    /**
     * Watch the contentText and act accordingly.
     */
    private fun contentTextWatcher(): TextWatcher {
        return object : TextWatcher {
            override fun afterTextChanged(arg0: Editable) {
                val enableButton: Boolean =
                    (contentText.text.length <= MAX_CHARACTERS) && contentText.text.isNotBlank()
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
