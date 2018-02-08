package com.example.kesin.asyncsample

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView

class MainActivity : AppCompatActivity() {
    lateinit var callbackButton: Button
    lateinit var rxButton: Button
    lateinit var awaitButton: Button
    lateinit var progressBar: ProgressBar
    lateinit var message: TextView
    val api = DummyApi()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        callbackButton = findViewById<Button>(R.id.callbackButton)
        rxButton = findViewById<Button>(R.id.rxButton)
        awaitButton = findViewById<Button>(R.id.awaitButton)
        progressBar = findViewById<ProgressBar>(R.id.progressBar)
        message = findViewById<TextView>(R.id.message)

        callbackButton.setOnClickListener {
            renderBegin()
            processCallback {
                runOnUiThread {
                    renderFinish()
                }
            }
        }
    }

    fun renderBegin() {
        message.text = "progress..."
        progressBar.visibility = View.VISIBLE
    }

    fun renderFinish() {
        message.text = "Done!"
        progressBar.visibility = View.INVISIBLE
    }

    fun processCallback(callback: () -> Unit) {
        val thread = Thread({
            api.fetchThree()
            callback.invoke()
        })
        thread.start()
    }
}

private operator fun Unit.invoke() {
    TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
}
