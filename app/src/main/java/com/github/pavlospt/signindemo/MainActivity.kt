package com.github.pavlospt.signindemo

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.widget.TextView

class MainActivity : AppCompatActivity() {

    companion object {
        @JvmField
        val USER_EMAIL_KEY: String = "user-email-key"

        @JvmStatic
        fun startActivity(activity: AppCompatActivity, userEmail: String?) {
            val intent: Intent = Intent(activity, MainActivity::class.java)
            intent.putExtra(USER_EMAIL_KEY, userEmail)
            activity.startActivity(intent)
        }
    }

    private lateinit var userEmailTextView: TextView

    private val userEmail: String by lazy {
        intent.getStringExtra(USER_EMAIL_KEY)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        initViews()
        initData()
    }

    private fun initData() {
        userEmailTextView.text = userEmail
    }

    private fun initViews() {
        userEmailTextView = findViewById(R.id.user_email_text_view) as TextView
    }
}
