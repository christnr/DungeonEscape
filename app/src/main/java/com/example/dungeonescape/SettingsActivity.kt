package com.example.dungeonescape

import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_settings.*
import kotlinx.android.synthetic.main.fragment_back_button.*

class SettingsActivity : AppCompatActivity() {

    private val prefName = "soundPreferences"
    private var musicOn = false
    private var sfxOn = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        //Retrieve preferences and set current values
        val dataBack: SharedPreferences = getSharedPreferences(prefName, 0)
        if (dataBack.contains("musicIsOn") || dataBack.contains("sfxIsOn")){
            musicOn = dataBack.getBoolean("musicIsOn", false)
            sfxOn = dataBack.getBoolean("sfxIsOn", false)
        } else {
            musicOn = false
            sfxOn = false
        }

        //Set switches in Activity
        musicSwitch.isChecked = musicOn
        sfxSwitch.isChecked = sfxOn

        backBtn.setOnClickListener {
            finish()
        }
    }

    //Automatically save settings when user leaves settings screen
    override fun onPause() {
        super.onPause()

        //Save settings
        val musicPrefs: SharedPreferences? = getSharedPreferences(prefName, 0)
        val editor: SharedPreferences.Editor = (musicPrefs as SharedPreferences).edit()
        editor.putBoolean("musicIsOn", musicSwitch.isChecked)
        editor.putBoolean("sfxIsOn", sfxSwitch.isChecked)
        editor.apply()

        //Notify that settings have been saved
        val musicSetting = if (musicSwitch.isChecked) "On" else "Off"
        val sfxSetting = if (sfxSwitch.isChecked) "On" else "Off"
        Toast.makeText(this, "Music $musicSetting\nSFX $sfxSetting", Toast.LENGTH_SHORT).show()
    }
}