package org.zendev.roomix.dialog

import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.core.content.ContextCompat.startActivity
import org.zendev.roomix.R

class AboutDialog(context: Context) : Dialog(context), View.OnClickListener {

    private lateinit var btnCloseAboutDialog: Button

    private lateinit var tvGithubRepo: TextView
    private lateinit var tvEmailAddress: TextView
    private lateinit var tvTelegramAccount: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.about_dialog)

        initViews()

        btnCloseAboutDialog.setOnClickListener(this)

        tvGithubRepo.setOnClickListener(this)
        tvEmailAddress.setOnClickListener(this)
        tvTelegramAccount.setOnClickListener(this)
    }

    private fun initViews() {
        btnCloseAboutDialog = findViewById(R.id.btnCloseAboutDialog)

        tvGithubRepo = findViewById(R.id.tvGithubRepo)
        tvEmailAddress = findViewById(R.id.tvEmailAddress)
        tvTelegramAccount = findViewById(R.id.tvTelegramAccount)
    }

    override fun onClick(v: View?) {
        when(v?.id) {
            R.id.btnCloseAboutDialog -> {
                dismiss()
            }

            R.id.tvGithubRepo -> {
                startActivity(
                    context,
                    Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/mehdiprgm")), Bundle()
                )
            }

            R.id.tvEmailAddress -> {
                val emailIntent = Intent(Intent.ACTION_SEND)
                emailIntent.type = "plain/text"
                emailIntent.putExtra(Intent.EXTRA_EMAIL, arrayOf("zendevmehdi@gmail.com")) // Replace with recipient email
                emailIntent.putExtra(Intent.EXTRA_SUBJECT, "Subject")
                emailIntent.putExtra(Intent.EXTRA_TEXT, "Text")

                startActivity(context, Intent.createChooser(emailIntent, "Send email..."), Bundle())
            }

            R.id.tvTelegramAccount -> {
                startActivity(
                    context,
                    Intent(Intent.ACTION_VIEW, Uri.parse("https://t.me/zenDEv2")), Bundle()
                )
            }
        }
    }
}