package com.gonpas.nasaapis

import android.Manifest
import android.app.AlertDialog
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import androidx.databinding.DataBindingUtil
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import androidx.navigation.ui.NavigationUI
import androidx.work.*
import com.gonpas.nasaapis.databinding.ActivityMainBinding
import com.gonpas.nasaapis.util.cancelNotification
import com.gonpas.nasaapis.worker.RefreshDataWorker
import com.google.android.material.navigation.NavigationView
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

private const val TAG = "xxMa"

    private const val FILE_NAME = "NasaApisBackup.json"
    private const val MIME_TYPE = "application/json"
    private const val NOTIFICATION_PERMISSION_REQUEST = 2

class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    private lateinit var drawerLayout: DrawerLayout
    private val applicationScope = CoroutineScope(Dispatchers.Default)
    private lateinit var preferences: SharedPreferences


    val viewModel: MainActivityViewModel by lazy {
        val viewModelFactory = MainActivityViewModelFactory(this.application)
        ViewModelProvider(this, viewModelFactory)[MainActivityViewModel::class.java]
    }

    private val createBackupFile = registerForActivityResult(CreateFileContract()) { uri ->
        if (uri != null) {
            createFile(uri)
        }
    }

    private val retrieveBackupFile = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null){
            openFile(uri)
        }
    }

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
        navView.setNavigationItemSelectedListener(this)

        preferences = getSharedPreferences("preferences", Context.MODE_PRIVATE)
        val firstTime = preferences.getBoolean("firstTime", true)

        delayedInit(firstTime)
//        if (firstTime){
//            val requestPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()){ isGranted ->
//                if (!isGranted) {
//                    Toast.makeText(this, getText(R.string.info_permiso), Toast.LENGTH_LONG).show()
//                }
//            }
//
//        }
        //fijar el modo noche como el actual
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = this.findNavController(R.id.nav_host_fragment)
        return NavigationUI.navigateUp(navController, drawerLayout)
    }

    private fun isNotificationGranted(): Boolean {
        if (Build.VERSION.SDK_INT < 33)     return true
        else {
           if ( ContextCompat.checkSelfPermission(
                   this,
                   Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) {
               return true
           } else {
               if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.POST_NOTIFICATIONS)){
                   Log.d(TAG,"requiere explicacion")

                   val builder = AlertDialog.Builder(this)
                   builder.setMessage(R.string.explicacion_permiso)
                       .setTitle(R.string.info)
                   builder.setPositiveButton(
                       android.R.string.ok
                   ) { _, _ ->
                       ActivityCompat.requestPermissions(
                           this,
                           arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                           NOTIFICATION_PERMISSION_REQUEST
                       )
                   }
                   builder.setNegativeButton(
                       android.R.string.cancel
                   ) { _, _ -> // User cancelled the dialog
                       Toast.makeText(
                           applicationContext,
                           getText(R.string.denied_permiso),
                           Toast.LENGTH_LONG
                       ).show()
                   }
                   val dialog = builder.create()
                   dialog.show()
               } else {
                   Log.d(TAG,"NO requiere explicacion")
                   // No explanation needed, we can request the permission.
                   ActivityCompat.requestPermissions(
                       this,
                       arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                       NOTIFICATION_PERMISSION_REQUEST
                   )
               }
           }
        }
        return false
    }

    private fun delayedInit(firstTime: Boolean){
        if (firstTime){
            Log.d(TAG,"firstTime")
            with(preferences.edit()){
                putBoolean("firstTime", false)
                apply()
            }
            val isGranted = isNotificationGranted()
            Log.d(TAG,"is notification granted = $isGranted")
        }
        applicationScope.launch {
            setupRecurringWork()
        }
    }

    private fun setupRecurringWork(){

        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.UNMETERED)
            .setRequiresBatteryNotLow(true)
            .setRequiresStorageNotLow(true)
            .build()

        val repeatingRequest = PeriodicWorkRequestBuilder<RefreshDataWorker>(1, TimeUnit.DAYS)
            .setConstraints(constraints)
            .build()

        Log.d(TAG,"Programada petición de sincronicacion con apod del dia")
        val workManager = WorkManager.getInstance(application)
        workManager
            .enqueueUniquePeriodicWork(
            RefreshDataWorker.WORK_NAME,
            ExistingPeriodicWorkPolicy.REPLACE,
            repeatingRequest
        )
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        val navController = this.findNavController(R.id.nav_host_fragment)
        when(item.itemId){
            R.id.restore -> {
                retrieveBackupFile.launch(MIME_TYPE)
            }

            R.id.backup  -> {
                viewModel.obtenerDatos()

                viewModel.datosBackupRecibidos.observeUntilTrue(this) {
                    if (it){
//                        Log.d(TAG,"datos recibidos")
                        viewModel.datosBackupRecibidos.value = false
                        createBackupFile.launch(MIME_TYPE)
                    }
                }
            }

            R.id.apodsFragment -> {
                val notificationManager =ContextCompat.getSystemService(
                    this,
                    NotificationManager::class.java
                ) as NotificationManager
                notificationManager.cancelNotification()
                navController.navigate(R.id.apodsFragment)
            }
            R.id.epicThumsFragment -> navController.navigate(R.id.epicThumsFragment)
            R.id.marsRoverPhotosFragment -> navController.navigate(R.id.marsRoverPhotosFragment)

        }
        drawerLayout.closeDrawer(GravityCompat.START,true)
        return true
    }

    // extensión para la auto eliminacion del observador despues de observar true
    private fun LiveData<Boolean>.observeUntilTrue(lifecycleOwner: LifecycleOwner, observer: Observer<Boolean>) {
        observe(lifecycleOwner, object: Observer<Boolean>{
            override fun onChanged(t: Boolean) {
                observer.onChanged(t)
                if (t)                removeObserver(this)
            }
        })
    }

    private fun createFile(uri: Uri) {
        viewModel.editFile(uri)
        viewModel.showBackupInfo.observe(this){
            if (it) {
                val msg = resources.getString(R.string.backupInfo).format(viewModel.apodsCount, viewModel.marsPhotosCount, viewModel.fechasVistasCount)
                Toast.makeText(this, msg, Toast.LENGTH_LONG).show()
                viewModel.resetCountData()
            }
        }
    }
    private fun openFile(uri: Uri) {
        viewModel.rebuildData(uri)
        viewModel.showRestoreInfo.observe(this) {
            if (it) {
                val msg = resources.getString(R.string.restoreInfo).format(viewModel.apodsCount, viewModel.marsPhotosCount, viewModel.fechasVistasCount)
                Toast.makeText(this, msg, Toast.LENGTH_LONG).show()
                viewModel.resetCountData()
            }
        }
    }
}

class CreateFileContract: ActivityResultContracts.CreateDocument(MIME_TYPE) {
    override fun createIntent(context: Context, input: String): Intent {
        val intent = super.createIntent(context, input)
        intent.putExtra(Intent.EXTRA_TITLE, FILE_NAME)
        return intent
    }
}