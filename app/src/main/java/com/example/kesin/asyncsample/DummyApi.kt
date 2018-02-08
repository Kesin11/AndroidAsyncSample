package com.example.kesin.asyncsample

/**
 * Created by kesin on 2018/02/09.
 */
class DummyApi {
    fun fetchThree(): String {
        Thread.sleep(3000)
        return "Done!"
    }

    fun fetchFive(): String {
        Thread.sleep(5000)
        return "Done!"
    }
}