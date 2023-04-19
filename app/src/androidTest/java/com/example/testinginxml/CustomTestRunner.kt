package com.example.testinginxml

import android.app.Application
import android.content.Context
import androidx.test.runner.AndroidJUnitRunner
import dagger.hilt.android.testing.HiltTestApplication


//https://developer.android.com/training/dependency-injection/hilt-testing


/*
* Next, configure this test runner in your Gradle file
*  as described in the instrumented unit test guide. Make sure you use the full classpath:*/

// A custom runner to set up the instrumented application class for tests.
class CustomTestRunner : AndroidJUnitRunner() {

    override fun newApplication(cl: ClassLoader?, name: String?, context: Context?): Application {
        return super.newApplication(cl, HiltTestApplication::class.java.name, context)
    }
}