package com.gonpas.nasaapis

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.provider.DocumentsContract
import android.provider.OpenableColumns
import android.util.Log
import android.view.MenuItem
import androidx.appcompat.app.AppCompatDelegate
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
import com.gonpas.nasaapis.database.ApodDb
import com.gonpas.nasaapis.databinding.ActivityMainBinding
import com.gonpas.nasaapis.worker.RefreshDataWorker
import com.google.android.material.navigation.NavigationView
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

    private const val TAG = "xxMa"
    private const val CREATE_FILE_APODS =1
    private const val CREATE_FILE_MARS_FOTOS =2
    private const val CREATE_FILE_MARS_FECHAS =3
    private const val PICK_DB_FILE = 7
    private const val OPEN_DOWNLOAD_DIR = 0

class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    private lateinit var drawerLayout: DrawerLayout
    private val applicationScope = CoroutineScope(Dispatchers.Default)
    private val backupScope = CoroutineScope(Dispatchers.IO)
    private lateinit var openUri: Uri
    var jsonExample: String = "{\"apodId\":15,\"copyright\":\"Nasa\",\"date\":\"2022-12-08\",\"explanation\":\"A camera on board the uncrewed Orion spacecraft captured this view on December 5 as Orion approached its return powered flyby of the Moon.  Below one of Orion's extended solar arrays lies dark, smooth, terrain along the western edge of the Oceanus Procellarum. Prominent on the lunar nearside Oceanus Procellarum, the Ocean of Storms, is the largest of the Moon's lava-flooded maria. The lunar terminator, shadow line between lunar night and day, runs along the left of the frame. The 41 kilometer diameter crater Marius is top center, with ray crater Kepler peeking in at the edge, just right of the solar array wing. Kepler's bright rays extend to the north and west, reaching the dark-floored Marius. Of course the Orion spacecraft is now headed toward a December 11 splashdown in planet Earth's water-flooded Pacific Ocean.\",\"hdurl\":\"https://apod.nasa.gov/apod/image/2212/art001e002132.jpg\",\"mediaType\":\"image\",\"serviceVersion\":\"v1\",\"title\":\"Orion and the Ocean of Storms\",\"url\":\"https://apod.nasa.gov/apod/image/2212/art001e002132_apod1024.jpg\"}"

    val viewModel: MainActivityViewModel by lazy {
        Log.d(TAG,"Instanciando mainactivity viewmodel")
        val viewModelFactory = MainActivityViewModelFactory(this.application)
        ViewModelProvider(this, viewModelFactory).get(MainActivityViewModel::class.java)
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

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
//        Log.d(TAG, "data.data: ${data?.data}")

        val documentFolder = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
        val contentResolver = applicationContext.contentResolver
        val takeFlags: Int = Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION

        if (resultCode == RESULT_OK) {
            when (requestCode) {
                CREATE_FILE_APODS -> {
                    data?.data?.also { uri ->
                        Log.d(TAG,"uri: $uri")
                        contentResolver.takePersistableUriPermission(uri, takeFlags)

                        Log.d(TAG, "copiando archivo NasaApodsBackup.json")
                        val jsonString = viewModel.dataToJson(viewModel.apods.value as List<Any>, "ApodDb")
                        viewModel.editFile(uri, jsonString)
                    }
                }

                CREATE_FILE_MARS_FOTOS -> {
                    data?.data?.also { uri ->
                        contentResolver.takePersistableUriPermission(uri, takeFlags)

                        Log.d(TAG, "copiando archivo NasaMarsFotosBackup.json")
                        val jsonString = viewModel.dataToJson(viewModel.marsFotos.value as List<Any>, "MarsPhotoDb")
                        viewModel.editFile(uri, jsonString)
                    }
                }

                CREATE_FILE_MARS_FECHAS -> {
                    data?.data?.also { uri ->
                        contentResolver.takePersistableUriPermission(uri, takeFlags)

                        Log.d(TAG, "copiando archivo NasaMarsFechasBackup.json")
                        val jsonString = viewModel.dataToJson(viewModel.fechasMarte.value as List<Any>,"FechaVista")
                        viewModel.editFile(uri, jsonString)
                    }
                }

                PICK_DB_FILE -> {
                    data?.data?.also { uri ->
                        val filename = getFileNameFromUri(this, uri)
                        Log.d(TAG,"filename: $filename")
                        Log.d(TAG,"data: $data")
//                        Log.d(TAG,"filename: ${uri.lastPathSegment}")
//                        val path = uri.path?.split("/")
//                        Log.d(TAG, "Path: ${ path.toString() }")
//                        val name = path?.get(path.size -1)
//                        Log.d(TAG, name?: "nulo")
                        viewModel.rebuildData(uri)
                    }
                }
            }
        }else{
            Log.e(TAG, "result code no OK: $resultCode")
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

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        val navController = this.findNavController(R.id.nav_host_fragment)
        when(item.itemId){
            R.id.restore -> {
                val documentFolder = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).absolutePath
                val uri = Uri.parse(documentFolder)
                val jsonArray = openFile(uri)

//
//                Toast.makeText(this, "Datos recuperados", Toast.LENGTH_LONG).show()
//                val externalStorageVolumes: Array<out File> = ContextCompat.getExternalFilesDirs(applicationContext, null)
//                for (item in externalStorageVolumes)    Log.d(TAG, item.absolutePath)
              //  Log.d(TAG,"permiso manage external storage: ${Environment.isExternalStorageManager()}")


                Log.d(TAG, "Clickado Restore")
            }
            R.id.backup  -> {
                Log.d(TAG, "Clickado Backup")
              //  openDirectory()
                viewModel.obtenerDatos()
                val documentFolder = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).absolutePath
//                Log.d(TAG, "documentFolder: ${Uri.parse(documentFolder)}")
//                Log.d(TAG,"estado storage: ${Environment.getExternalStorageState()}")
                /*viewModel.apods.observe(this){
                    Log.d(TAG, "Recibidos apods")
                    createFile(Uri.parse(documentFolder), "NasaApodsBackup.json")
                }*/
                viewModel.apods.observerOnce(this) {
                    Log.d(TAG, "Recibidos apods")
                    createFile(Uri.parse(documentFolder), "NasaApodsBackup.json")
                }

                viewModel.marsFotos.observerOnce(this){
                    createFile(Uri.parse(documentFolder), "NasaMarsFotosBackup.json")
                    Log.d(TAG, "Recibidas fotos de marte")
                }

                viewModel.fechasMarte.observerOnce(this){
                    createFile(Uri.parse(documentFolder), "NasaMarsFechasBackup.json")
                    Log.d(TAG, "Recibidas fechas vistas de marte")
                }
//                Log.d(TAG,"documentFolder: $documentFolder")


//                Toast.makeText(this, "Datos guardados en $documentFolder", Toast.LENGTH_LONG).show()

            }
            R.id.apodsFragment -> navController.navigate(R.id.apodsFragment)
            R.id.epicThumsFragment -> navController.navigate(R.id.epicThumsFragment)
            R.id.marsRoverPhotosFragment -> navController.navigate(R.id.marsRoverPhotosFragment)

        }
        drawerLayout.closeDrawer(GravityCompat.START,true)
        return true
    }

    // extensión para la auto eliminacion del observador despues de una observación
    private fun <T> LiveData<T>.observerOnce(lifecycleOwner: LifecycleOwner, observer: Observer<T>) {
        observe(lifecycleOwner, object: Observer<T>{
            override fun onChanged(t: T) {
                observer.onChanged(t)
                removeObserver(this)
            }

        })
    }

    private fun createFile(pickerInitialUri: Uri, title: String){
//        Log.d(TAG,"createFile pickerInitialUri: $pickerInitialUri")
        val intent = Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "application/json"

            putExtra(Intent.EXTRA_TITLE, title)

            // opcionalmente se puede especificar la URI del directorio inicial que abre el file picker
            // antes de crear el archivo. Para Api level >=26
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
                putExtra(DocumentsContract.EXTRA_INITIAL_URI, pickerInitialUri)
            }

        }
        when(title){
            "NasaApodsBackup.json" -> startActivityForResult(intent, CREATE_FILE_APODS)
            "NasaMarsFotosBackup.json" -> startActivityForResult(intent, CREATE_FILE_MARS_FOTOS)
            "NasaMarsFechasBackup.json" -> startActivityForResult(intent, CREATE_FILE_MARS_FECHAS)
        }
    }

    private fun openFile(pickerInitialUri: Uri){
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "application/json"

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
                putExtra(DocumentsContract.EXTRA_INITIAL_URI, pickerInitialUri)
            }
        }
        startActivityForResult(intent, PICK_DB_FILE)
    }

    private fun getFileNameFromUri(context: Context, uri: Uri): String? {
        val fileName: String?
        val cursor = context.contentResolver.query(uri, null, null, null, null)
        cursor?.moveToFirst()
        var indice = cursor?.getColumnIndex(OpenableColumns.DISPLAY_NAME)
        if(indice == -1) indice = 0
        fileName = cursor?.getString(indice!!)
        cursor?.close()
        return fileName
    }
}