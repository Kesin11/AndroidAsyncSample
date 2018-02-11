package com.example.kesin.asyncsample

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.functions.BiFunction
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.experimental.CommonPool
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.async
import kotlinx.coroutines.experimental.launch
import kotlinx.coroutines.experimental.newSingleThreadContext
import java.lang.System.currentTimeMillis

class MainActivity : AppCompatActivity() {
    lateinit var callbackButton: Button
    lateinit var callbackSerialButton: Button
    lateinit var rxButton: Button
    lateinit var rxSerialButton: Button
    lateinit var rxConcurrentButton: Button
    lateinit var asyncButton: Button
    lateinit var asyncSerialButton: Button
    lateinit var asyncConcurrentButton: Button
    lateinit var suspendButton: Button
    lateinit var progressBar: ProgressBar
    lateinit var message: TextView
    var timerStarted: Long? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        callbackButton = findViewById<Button>(R.id.callback_button)
        callbackSerialButton = findViewById<Button>(R.id.callback_s_button)
        rxButton = findViewById<Button>(R.id.rx_button)
        rxSerialButton = findViewById<Button>(R.id.rx_s_button)
        rxConcurrentButton = findViewById<Button>(R.id.rx_c_button)
        asyncButton = findViewById<Button>(R.id.async_button)
        asyncSerialButton = findViewById<Button>(R.id.async_s_button)
        asyncConcurrentButton = findViewById<Button>(R.id.async_c_button)
        suspendButton = findViewById<Button>(R.id.suspend_button)
        progressBar = findViewById<ProgressBar>(R.id.progressBar)
        message = findViewById<TextView>(R.id.message)

        callbackButton.setOnClickListener {
            renderBegin()
            CallbackStyle.single {
                runOnUiThread {
                    renderFinish(it)
                }
            }
        }

        callbackSerialButton.setOnClickListener {
            renderBegin()
            CallbackStyle.serial {
                runOnUiThread {
                    renderFinish(it)
                }
            }
        }

        rxButton.setOnClickListener {
            // fetchOneは別スレッドで実行される
            RxStyle.fetchOne("Rx")
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe  { result ->
                    // こっちはmainスレッドで実行される
                    renderFinish(result)
                }

            renderBegin()
        }

        rxSerialButton.setOnClickListener {
            RxStyle.fetchOne("Rx")
                .flatMap {
                    RxStyle.fetchTwo("Rx")
                }
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe  { result ->
                    renderFinish(result)
                }

            renderBegin()
        }

        rxConcurrentButton.setOnClickListener {
            // 単にsubscribeOn(Schedulers.io())とzipの組み合わせでは2つが別々のスレッドで実行されない
            // https://medium.com/@Joseph82/concurrent-tasks-with-zip-operator-99773466039f
            Observable.zip<String, String, String>(
                RxStyle.fetchOne("Rx").subscribeOn(Schedulers.newThread()),
                RxStyle.fetchTwo("Rx2").subscribeOn(Schedulers.newThread()),
                BiFunction { message1, message2 ->
                    message1
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe  { result ->
                    renderFinish(result)
                }

            renderBegin()
        }

        asyncButton.setOnClickListener {
            renderBegin()
            launch(UI) {
                // asyncはawaitが必要
                val task = async {
                    return@async DummyApi.fetchOne("async")
                }
                val result = task.await()
                renderFinish(result)
            }
        }

        asyncSerialButton.setOnClickListener {
            renderBegin()
            launch(UI) {
                val task1 = async {
                    return@async DummyApi.fetchOne("async")
                }
                val task2 = async {
                    return@async DummyApi.fetchTwo("async")
                }
                val result = task1.await() + task2.await()
                renderFinish(result)
            }
        }

        asyncConcurrentButton.setOnClickListener {
            renderBegin()
            launch(UI) {
                // DummyApi.fetch**はThread.sleepなのでそれぞれ別スレッドにしないと並列に動かない
                val task1 = async(newSingleThreadContext("task1")){
                    Log.d("DEBUG", "task1 start ${Thread.currentThread().name}")
                    return@async DummyApi.fetchOne("async")
                }
                val task2 = async(newSingleThreadContext("task2")) {
                    Log.d("DEBUG", "task2 start ${Thread.currentThread().name}")
                    return@async DummyApi.fetchTwo("async")
                }
                // asyncは自動的にスタートしているので同時にawait()すると一番時間がかかるものに引きづられることになる
                val result = task1.await() + task2.await()
                renderFinish(result)
            }
        }

        suspendButton.setOnClickListener {
            renderBegin()
            launch(UI) {
                // suspendの場合はawait不要
                val result = CoroutineStyle.fetchOneSuspend()
                renderFinish(result)
            }
        }
    }

    fun renderBegin() {
        timerStarted = currentTimeMillis()
        message.text = "progress..."
        progressBar.visibility = View.VISIBLE
    }

    fun renderFinish(result: String) {
        var timeCount = (currentTimeMillis() - timerStarted!!) / 1000.0
        message.text = "$result ${timeCount}s"
        progressBar.visibility = View.INVISIBLE
    }

}
