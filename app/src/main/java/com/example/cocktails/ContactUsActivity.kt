package com.example.cocktails

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.textfield.TextInputLayout


class ContactUsActivity: AppCompatActivity() {

    private val ACTIVITY_TITLE: String = "CONTACT US"
    private val EMAIL_ADDRESS: Array<String> = arrayOf("cocktails.ppc@gmail.com")
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
        composeEmail(EMAIL_ADDRESS, subjectText.text.toString(), contentText.text.toString())
        clearViews()
    }

    /**
     * Compose an email message, aimed for 'addresses', with 'subject' and 'body'.
     */
    private fun composeEmail(addresses: Array<String>, subject: String, body: String) {
        val intent = Intent(Intent.ACTION_SENDTO).apply {
//            type = "message/rfc822"  // Use this to allow message based apps
            data = Uri.parse("mailto:") // Only email apps should handle this
            putExtra(Intent.EXTRA_EMAIL, addresses)
            putExtra(Intent.EXTRA_SUBJECT, subject)
            putExtra(Intent.EXTRA_TEXT, body)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        if (intent.resolveActivity(packageManager) != null) {
            startActivity(intent)
        }
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
