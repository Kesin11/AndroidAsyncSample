package com.example.kesin.asyncsample

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import io.reactivex.Observable
import io.reactivex.ObservableOnSubscribe
import io.reactivex.Scheduler
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers

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
            processCallback { result ->
                runOnUiThread {
                    renderFinish(result)
                }
            }
        }

        rxButton.setOnClickListener {
            Observable.create( ObservableOnSubscribe<String> { subscriber ->
                // ここがioスレッドで実行される
                val result = api.fetchFive("Rx")
                subscriber.onNext(result)
            })
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe  { result ->
                // こっちがmainスレッドで実行される
                renderFinish(result)
            }

            renderBegin()
        }
    }

    fun renderBegin() {
        message.text = "progress..."
        progressBar.visibility = View.VISIBLE
    }

    fun renderFinish(result: String) {
        message.text = result
        progressBar.visibility = View.INVISIBLE
    }

    fun processCallback(callback: (String) -> Unit) {
        val thread = Thread({
            val result = api.fetchThree("callback")
            callback.invoke(result)
        })
        thread.start()
    }
}
