package com.example.honeyimhome

import android.app.SearchManager
import android.content.DialogInterface
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.preference.PreferenceManager
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : AppCompatActivity() {

    private val TAG = "MainActivity"

    private var startTrackState = 0

    private val LOCATION_REQ_CODE = 100

    private lateinit var  mFusedLocationClient : FusedLocationProviderClient

    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val info = findViewById<ImageView>(R.id.infoImage)
        info.setOnClickListener{
            val infoPopup = AlertDialog.Builder(this)
            infoPopup.setTitle("INFO")
            infoPopup.setMessage("This app is tracking your location\n\nBy click on START TRACKING LOCATION" +
                    " you will see the current location\n\nIf the accuracy is better then 50 meters" +
                    " you will be able to set the current location as home\n\nIf there is already " +
                    "home location defined you will be able to clear it")
            infoPopup.show()
        }

        val sp : SharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)
        val homeLat = sp.getString("homeLat", null)
        val homeLon = sp.getString("homeLon", null)


        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        val startTrackButton = findViewById<Button>(R.id.startTrack)
        startTrackButton.setOnClickListener{
            // Check for permissions
            val isGranted : Boolean
            if(startTrackState == 0)
            {
                isGranted = ActivityCompat.checkSelfPermission(this,
                    android.Manifest.permission.ACCESS_FINE_LOCATION) ==
                        PackageManager.PERMISSION_GRANTED
                if(isGranted)
                {
                    Log.d(TAG, "already granted")
                    locate()
                }
                else
                {
                    ActivityCompat.requestPermissions(this,
                        arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION), LOCATION_REQ_CODE)
                }
            }
            else
            {
                findViewById<TextView>(R.id.curLat).text = ""
                findViewById<TextView>(R.id.curLon).text = ""
                findViewById<TextView>(R.id.curLocationText).text = ""
                findViewById<TextView>(R.id.accuracy).text = ""
                startTrackButton.text = "Start tracking location"
                startTrackState = 0
                findViewById<Button>(R.id.setHome).visibility = View.INVISIBLE
            }
            //button.visibility = View.INVISIBLE; to make a button invisible
        }

        val clearHomeButton = findViewById<Button>(R.id.clearHome)
        clearHomeButton.visibility = View.INVISIBLE
        clearHomeButton.setOnClickListener{
            Log.d(TAG, "clicked on clear")
            val editor = sp.edit()
            editor.putString("homeLon", "null")
            editor.putString("homeLat", "null")
            editor.apply()
            findViewById<TextView>(R.id.homeLat).text = ""
            findViewById<TextView>(R.id.homeLon).text = ""
            findViewById<TextView>(R.id.homeLocationText).text = ""
            clearHomeButton.visibility = View.INVISIBLE
        }

        val setHomeButton = findViewById<Button>(R.id.setHome)
        setHomeButton.visibility = View.INVISIBLE
        setHomeButton.setOnClickListener{
            Log.d(TAG, "clicked on set")
            val editor = sp.edit()
            editor.putString("homeLat", findViewById<TextView>(R.id.curLat).text.toString())
            editor.putString("homeLon", findViewById<TextView>(R.id.curLon).text.toString())
            editor.apply()
            findViewById<TextView>(R.id.homeLocationText).text = "Home defined as: "
            findViewById<TextView>(R.id.homeLat).text = sp.getString("homeLat", null)
            findViewById<TextView>(R.id.homeLon).text = sp.getString("homeLon", null)
            clearHomeButton.visibility = View.VISIBLE
        }

        if(homeLat != null && homeLon != null)
        {
            if(homeLat != "null")
            {
                findViewById<TextView>(R.id.homeLocationText).text = "Home defined as: "
                findViewById<TextView>(R.id.homeLat).text = homeLat
                findViewById<TextView>(R.id.homeLon).text = homeLon
                clearHomeButton.visibility = View.VISIBLE
            }
        }
    }

    fun locate()
    {
        val button = findViewById<Button>(R.id.startTrack)
        // get location and display on screen
        button.text = "stop tracking"
        startTrackState = 1
        getLocation()
        Log.d(TAG, "locating")
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if(requestCode == LOCATION_REQ_CODE)
        {
            if(grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)
            {
                Log.d(TAG, "granted now")
                locate()
            }
            else
            {
                // Display a UI that explains we need this permission
                val popup = AlertDialog.Builder(this)
                popup.setIcon(R.drawable.popup)
                popup.setTitle("Please grant location services")
                popup.show()
            }
        }
    }

    fun getLocation()
    {
        mFusedLocationClient.lastLocation.addOnCompleteListener(this) {task ->
            val location: Location? = task.result
            if(location != null)
            {
                findViewById<TextView>(R.id.curLat).text = location.latitude.toString() + 0x00B0.toChar()
                findViewById<TextView>(R.id.curLon).text = location.longitude.toString() + 0x00B0.toChar()
                findViewById<TextView>(R.id.curLocationText).text = "Current location:"
                findViewById<TextView>(R.id.accuracy).text = "Accuracy: " +
                        location.accuracy.toString() + " meters"

                if(location.accuracy < 50)
                {
                    setHome.visibility = View.VISIBLE
                }
            }
            else
            {
                Log.d(TAG, "Location not valid")
            }
        }
    }
}
