package com.gonpas.nasaapis

import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.util.Log
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.view.GravityCompat
import androidx.databinding.DataBindingUtil
import androidx.drawerlayout.widget.DrawerLayout
import androidx.navigation.findNavController
import androidx.navigation.ui.NavigationUI
import androidx.work.*
import com.gonpas.nasaapis.databinding.ActivityMainBinding
import com.gonpas.nasaapis.worker.RefreshDataWorker
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

    private const val TAG = "xxMa"
class MainActivity : AppCompatActivity() {

    private lateinit var drawerLayout: DrawerLayout
    private val applicationScope = CoroutineScope(Dispatchers.Default)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val binding = DataBindingUtil.setContentView<ActivityMainBinding>(this, R.layout.activity_main)

        drawerLayout = binding.drawerLayout
        /*Handler().postDelayed({
            drawerLayout.openDrawer(GravityCompat.START, true)
        }, 2000)*/


        val navController = this.findNavController(R.id.nav_host_fragment)

        NavigationUI.setupActionBarWithNavController(this, navController, drawerLayout)
        NavigationUI.setupWithNavController(binding.navView, navController)

        /*WorkManager.initialize(
            applicationContext,
            Configuration.Builder().setMinimumLoggingLevel(Log.DEBUG).build()
        )*/
        delayedInit()
        //fijar el modo noche como el actual
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = this.findNavController(R.id.nav_host_fragment)
        return NavigationUI.navigateUp(navController, drawerLayout)
    }

    private fun delayedInit(){
        applicationScope.launch {
            setupRecurringWork()
        }
    }

    private fun setupRecurringWork(){

        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.UNMETERED)
            .setRequiresBatteryNotLow(true)
            .setRequiresStorageNotLow(true)
            /*.apply {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
                    setRequiresDeviceIdle(true)
                }
            }*/
            .build()

        val repeatingRequest = PeriodicWorkRequestBuilder<RefreshDataWorker>(1, TimeUnit.DAYS)
            .setConstraints(constraints)
            .build()

        Log.d(TAG,"Programada petición de sincronicacion con apod del dia")
        val workManager = WorkManager.getInstance(application)
        workManager
            .enqueueUniquePeriodicWork(
            RefreshDataWorker.WORK_NAME,
            ExistingPeriodicWorkPolicy.KEEP,
            repeatingRequest
        )
        /*workManager.enqueue(oneRequest)
        workManager.getWorkInfoByIdLiveData(repeatingRequest.id)  //  NO SE PUEDE OBSERVAR EN UN HILO DE BACKGROUND
            .observe(this) {
                if (it.state == WorkInfo.State.ENQUEUED) {
                    // Show the work state in text view
                    Log.d(TAG,"Done")
                } else if (it.state == WorkInfo.State.ENQUEUED) {
                    Log.d(TAG,"Cancelada")
                } else  {
                    Log.d(TAG,"en proceso???")
                }
                Log.d(TAG, it.toString())
            }*/
    }
}