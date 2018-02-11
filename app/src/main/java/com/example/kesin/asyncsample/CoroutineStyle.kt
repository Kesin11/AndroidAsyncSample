package com.example.kesin.asyncsample

/**
 * Created by kesin on 2018/02/11.
 */
class CoroutineStyle {
    companion object {
        suspend fun fetchOneSuspend(): String {
            return DummyApi.fetchOne("suspend")
        }
    }
}