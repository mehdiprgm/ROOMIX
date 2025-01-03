package org.zendev.roomix.activity

import android.R.attr.data
import android.animation.LayoutTransition
import android.app.ProgressDialog
import android.content.Context
import android.content.SharedPreferences
import android.os.AsyncTask
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.PopupMenu
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import org.zendev.roomix.R
import org.zendev.roomix.adapter.RandomElementsAdapter
import org.zendev.roomix.databinding.ActivityMainBinding
import org.zendev.roomix.dialog.AboutDialog
import org.zendev.roomix.request.NumberRequest
import org.zendev.roomix.request.UUIDRequest
import org.zendev.roomix.request.basics.BaseRequest
import org.zendev.roomix.request.basics.RequestType
import org.zendev.roomix.tools.isDarkThemeEnabled
import org.zendev.roomix.tools.isInternetConnected
import java.io.BufferedReader
import java.io.File
import java.io.InputStream
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.nio.file.Files
import java.nio.file.Path


class RandomAsyncRequest(
    private val activity: MainActivity,
    var baseRequest: BaseRequest, var requestType: RequestType
) : AsyncTask<Void, Void, String>() {

    private lateinit var progressDialog: ProgressDialog

    private lateinit var stringRequest: StringRequest
    private lateinit var requestQue: RequestQueue

    var response = "error"

    @Deprecated("Deprecated in Java", ReplaceWith("super.onPreExecute()", "android.os.AsyncTask"))
    override fun onPreExecute() {
        super.onPreExecute()

        progressDialog = ProgressDialog(activity)
        progressDialog.setMessage("Sending request...")
        progressDialog.setCancelable(false)
        progressDialog.show()
    }

    @Deprecated("Deprecated in Java")
    override fun doInBackground(vararg params: Void?): String {
        var url = ""

        var numberRequest: NumberRequest
        var stringRequest: org.zendev.roomix.request.StringRequest
        var uuidRequest: UUIDRequest

        when (requestType) {
            RequestType.NUMBER -> {
                numberRequest = baseRequest as NumberRequest
                url =
                    "https://www.randomnumberapi.com/api/v1.0/random?min=${numberRequest.min}&max=${numberRequest.max}&count=${numberRequest.count}"
            }

            RequestType.STRING -> {
                stringRequest = baseRequest as org.zendev.roomix.request.StringRequest
                url =
                    "https://www.randomnumberapi.com/api/v1.0/randomstring?min=${stringRequest.min}&max=${stringRequest.max}&count=${stringRequest.count}&all=${stringRequest.includeSymbols}"
            }

            RequestType.UUID -> {
                uuidRequest = baseRequest as UUIDRequest
                url = "https://www.randomnumberapi.com/api/v1.0/uuid?count=${uuidRequest.count}"

            }
        }

        this.stringRequest = StringRequest(
            Request.Method.GET,
            url,
            { response ->
                val elements: ArrayList<String> =
                    Gson().fromJson(response, object : TypeToken<List<String>>() {}.type)

                activity.printResults(elements)
                activity.writeRandomItemsToTextFile(elements)

                progressDialog.dismiss()
            },
            { error ->
                val handler = Handler(Looper.getMainLooper())
                handler.post {
                    Toast.makeText(activity, "Request failed", Toast.LENGTH_SHORT).show()
                }

                progressDialog.dismiss()
            }
        )

        requestQue = Volley.newRequestQueue(activity)
        requestQue.add(this.stringRequest)
        return "ok"
    }

    @Deprecated(
        "Deprecated in Java",
        ReplaceWith("super.onPostExecute(result)", "android.os.AsyncTask")
    )
    override fun onPostExecute(result: String?) {
        super.onPostExecute(result)
    }
}

class MainActivity : AppCompatActivity(), View.OnClickListener {
    private lateinit var b: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        b = ActivityMainBinding.inflate(layoutInflater)

        enableEdgeToEdge()
        setContentView(b.root)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        /* load settings from shared preferences */
        loadSettings()

        /* set buttons click listener */
        addButtonClickListeners()

        /* set cardview layout transitions */
        enableCardViewLayoutsTransitions()

        if (isDarkThemeEnabled(this)) {
            if (File(filesDir, "roomix_data.txt").exists()) {
                printResults(readRandomItemsFromTextFile())
            }
        } else {
            if (File(filesDir, "roomix_data.txt").exists()) {
                printResults(readRandomItemsFromTextFile())
            }
        }
    }

    private fun addButtonClickListeners() {
        b.btnMainMenu.setOnClickListener(this)
        b.btnSendRequest.setOnClickListener(this)

        b.radioBtnNumbers.setOnClickListener(this)
        b.radioBtnStrings.setOnClickListener(this)
        b.radioBtnUUID.setOnClickListener(this)
    }

    private fun enableCardViewLayoutsTransitions() {
        b.layNumbersOptions.layoutTransition.enableTransitionType(LayoutTransition.CHANGING)
        b.layStringsOptions.layoutTransition.enableTransitionType(LayoutTransition.CHANGING)
        b.layUUIDsOptions.layoutTransition.enableTransitionType(LayoutTransition.CHANGING)
    }

    private fun unCheckAllRadioButtons() {
        b.radioBtnNumbers.isChecked = false
        b.radioBtnStrings.isChecked = false
        b.radioBtnUUID.isChecked = false
    }

    fun printResults(data: ArrayList<String>) {
        b.rcRandomItems.layoutManager = LinearLayoutManager(this)
        b.rcRandomItems.adapter = RandomElementsAdapter(this, data)
    }

    fun writeRandomItemsToTextFile(data: ArrayList<String>) {
        val outputStreamWriter = OutputStreamWriter(openFileOutput("roomix_data.txt", MODE_PRIVATE))
        val sb = StringBuilder()

        for (i in data) {
            sb.append(i).append("\n")
        }

        outputStreamWriter.write(sb.toString())
        outputStreamWriter.close()
    }

    private fun readRandomItemsFromTextFile(): ArrayList<String> {
        val data: ArrayList<String> = arrayListOf()

        val inputStreamReader = InputStreamReader(openFileInput("roomix_data.txt"))
        for (i in inputStreamReader.readLines()) {
            data.add(i)
        }

        return data
    }

    private fun showPopupMenu(view: View) {
        val popupMenu = PopupMenu(this, view)
        popupMenu.inflate(R.menu.menu_main)

        popupMenu.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.menuAbout -> {
                    AboutDialog(this).show()
                    true
                }

                else -> false
            }
        }

        popupMenu.show()
    }

    private fun loadSettings() {
        val sp: SharedPreferences = getSharedPreferences("roomix_pref", Context.MODE_PRIVATE)

        if (sp.getString("number_min", "-1") == "-1") {
            resetSettings()

            b.txtNumbersMinimum.setText("0")
            b.txtNumbersMaximum.setText("100")
            b.txtNumbersCount.setText("1")

            b.txtStringsMinimum.setText("10")
            b.txtStringsMaximum.setText("20")
            b.switchStringsAllSymbols.isChecked = false
            b.txtStringsCount.setText("1")

            b.txtUUIDCount.setText("10")
        } else {
            b.txtNumbersMinimum.setText(sp.getString("number_min", "0"))
            b.txtNumbersMaximum.setText(sp.getString("number_max", "100"))
            b.txtNumbersCount.setText(sp.getString("number_count", "1"))

            b.txtStringsMinimum.setText(sp.getString("string_min", "10"))
            b.txtStringsMaximum.setText(sp.getString("string_max", "20"))
            b.switchStringsAllSymbols.isChecked = sp.getBoolean("string_symbol", false)
            b.txtStringsCount.setText(sp.getString("string_count", "1"))

            b.txtUUIDCount.setText(sp.getString("uuid_count", "10"))
        }
    }

    private fun saveSettings() {
        val sharedPreferences: SharedPreferences =
            getSharedPreferences("roomix_pref", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()

        editor.putString("number_min", b.txtNumbersMinimum.text.toString())
        editor.putString("number_max", b.txtNumbersMaximum.text.toString())
        editor.putString("number_count", b.txtNumbersCount.text.toString())

        editor.putString("string_min", b.txtStringsMinimum.text.toString())
        editor.putString("string_max", b.txtStringsMaximum.text.toString())
        editor.putString("string_count", b.txtStringsCount.text.toString())

        editor.putString("uuid_count", b.txtUUIDCount.text.toString())
        editor.apply()
    }

    private fun resetSettings() {
        val sharedPreferences: SharedPreferences =
            getSharedPreferences("roomix_pref", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()

        editor.putString("number_min", "0")
        editor.putString("number_max", "100")
        editor.putString("number_count", "1")

        editor.putString("string_min", "10")
        editor.putString("string_max", "20")
        editor.putBoolean("string_symbol", false)
        editor.putString("string_count", "1")

        editor.putString("uuid_count", "1")
        editor.apply()
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.btnMainMenu -> showPopupMenu(v)
            R.id.btnSendRequest -> {
                if (isInternetConnected(this)) {
                    saveSettings()

                    if (b.radioBtnNumbers.isChecked) {
                        val request = NumberRequest()

                        request.min = b.txtNumbersMinimum.text.toString()
                        request.max = b.txtNumbersMaximum.text.toString()
                        request.count = b.txtNumbersCount.text.toString()

                        val randomRequest = RandomAsyncRequest(this, request, RequestType.NUMBER)
                        randomRequest.execute()
                    } else if (b.radioBtnStrings.isChecked) {
                        val request = org.zendev.roomix.request.StringRequest()

                        request.min = b.txtStringsMinimum.text.toString()
                        request.max = b.txtStringsMaximum.text.toString()
                        request.count = b.txtStringsCount.text.toString()

                        val randomRequest = RandomAsyncRequest(this, request, RequestType.STRING)
                        randomRequest.execute()
                    } else {
                        val request = UUIDRequest()
                        request.count = b.txtUUIDCount.text.toString()

                        val randomRequest = RandomAsyncRequest(this, request, RequestType.UUID)
                        randomRequest.execute()
                    }
                } else {
                    Toast.makeText(
                        this,
                        "The device is not connected to the internet.",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }

            R.id.radioBtnNumbers -> {
                unCheckAllRadioButtons()
                b.radioBtnNumbers.isChecked = true

                val visibility = if (b.tvNumbersOptionsMinimum.visibility == View.GONE)
                    View.VISIBLE else View.GONE

                b.tvNumbersOptionsMinimum.visibility = visibility
                b.txtLayNumbersMinimum.visibility = visibility
                b.txtNumbersMinimum.visibility = visibility

                b.tvNumbersOptionsMaximum.visibility = visibility
                b.txtLayNumbersMaximum.visibility = visibility
                b.txtNumbersMaximum.visibility = visibility

                b.tvNumbersOptionsCount.visibility = visibility
                b.txtLayNumbersCount.visibility = visibility
                b.txtNumbersCount.visibility = visibility
            }

            R.id.radioBtnStrings -> {
                unCheckAllRadioButtons()
                b.radioBtnStrings.isChecked = true

                val visibility = if (b.tvStringsOptionsMinimum.visibility == View.GONE)
                    View.VISIBLE else View.GONE

                b.tvStringsOptionsMinimum.visibility = visibility
                b.txtLayStringsMinimum.visibility = visibility
                b.txtStringsMinimum.visibility = visibility

                b.tvStringsOptionsMaximum.visibility = visibility
                b.txtLayStringsMaximum.visibility = visibility
                b.txtStringsMaximum.visibility = visibility

                b.tvStringsOptionsCount.visibility = visibility
                b.txtLayStringsCount.visibility = visibility
                b.txtStringsCount.visibility = visibility

                b.switchStringsAllSymbols.visibility = visibility
            }

            R.id.radioBtnUUID -> {
                unCheckAllRadioButtons()
                b.radioBtnUUID.isChecked = true

                val visibility =
                    if (b.tvUUIDOptionsCount.visibility == View.GONE) View.VISIBLE else View.GONE

                b.tvUUIDOptionsCount.visibility = visibility
                b.txtLayUUIDCount.visibility = visibility
                b.txtUUIDCount.visibility = visibility
            }
        }
    }

}