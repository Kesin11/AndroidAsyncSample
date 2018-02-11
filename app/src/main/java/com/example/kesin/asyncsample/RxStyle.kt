package com.example.kesin.asyncsample

import android.util.Log
import io.reactivex.Observable
import io.reactivex.ObservableOnSubscribe

/**
 * Created by kesin on 2018/02/11.
 */
class RxStyle {
    companion object {
        fun fetchOne(message: String): Observable<String> {
            return Observable.create( ObservableOnSubscribe<String> { subscriber ->
                val start = System.currentTimeMillis()
                Log.d("DEBUG", "fetchOne start ${Thread.currentThread().name}")

                val result = DummyApi.fetchOne(message)
                subscriber.onNext(result)

                val timer = (System.currentTimeMillis() - start) / 1000.0
                Log.d("DEBUG", "fetchOne end ${timer}")
            })
        }

        fun fetchTwo(message: String): Observable<String> {
            return Observable.create( ObservableOnSubscribe<String> { subscriber ->
                val start = System.currentTimeMillis()
                Log.d("DEBUG", "fetchTwo start ${Thread.currentThread().name}")

                val result = DummyApi.fetchTwo(message)
                subscriber.onNext(result)

                val timer = (System.currentTimeMillis() - start) / 1000.0
                Log.d("DEBUG", "fetchTwo end ${timer}")
            })
        }
    }
}