package com.gonpas.nasaapis

import android.content.Context
import android.util.Log
import androidx.test.core.app.ApplicationProvider
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.work.Configuration
import com.gonpas.nasaapis.worker.RefreshDataWorker

import androidx.work.ListenableWorker
import androidx.work.impl.utils.SynchronousExecutor
import kotlinx.coroutines.runBlocking
import org.hamcrest.core.Is.`is`

import org.junit.Test
import org.junit.runner.RunWith

import org.junit.Assert.*
import org.junit.Before

/**
 * Instrumented test, which will execute on an Android device.
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
@RunWith(AndroidJUnit4::class)
class WorkManagerTest {
    private lateinit var context: Context

    @Before
    fun setUp(){
        context = ApplicationProvider.getApplicationContext()
        val config = Configuration.Builder()
            .setMinimumLoggingLevel(Log.DEBUG)
            .setExecutor(SynchronousExecutor())
            .build()

        // Initialize WorkManager for instrumentation tests.
//        WorkManagerTestInitHelper.initializeTestWorkManager(context, config)
    }
    @Test
    fun testWorker() {
        // Context of the app under test.
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext
        assertEquals("com.gonpas.apod", appContext.packageName)
     /*   val worker = TestListenableWorkerBuilder<RefreshDataWorker>(context).build()
        runBlocking {
            val result = worker.doWork()
            assertThat(result, `is`(Result.success()))
        }*/
    }
}