package com.example.dungeonescape

import android.content.Intent
import android.content.SharedPreferences
import android.media.MediaPlayer
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.fragment_nav_buttons.*
import kotlin.system.exitProcess

class MainActivity : AppCompatActivity() {

    //Create music player and click sound effect player
    private var introMusic: MediaPlayer? = null
    private var clickSFX: MediaPlayer? = null

    //Flags to mute and un-mute music and sound effects
    private var musicOn = false
    private var sfxOn = false


    /** onCreate set up all music, sounds and button controls*/
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        //Load sound preferences
        loadPreferences()

        //Set music
        if (introMusic == null) {
            introMusic = MediaPlayer.create(this, R.raw.dungeon_escape_music_title)
        }

        //Set click sound effect
        if (clickSFX == null)
            clickSFX = MediaPlayer.create(this, R.raw.dungeon_escape_sfx_click)


        //Set up "Start Game" button
        startGameButton.setOnClickListener {
            if (sfxOn) clickSFX?.start()
            val selectIntent = Intent(this, PlayerSelect::class.java)
            startActivity(selectIntent)
            finish()
        }

        //Set up "Quit" button
        quitButton.setOnClickListener { exitProcess(1) }

        //Set up "Help" button
        helpButton.setOnClickListener {
            if (sfxOn) clickSFX?.start()
            val helpIntent = Intent(this, HelpActivity::class.java)
            startActivity(helpIntent)
        }

        //Set up "Settings" button
        settingsButton.setOnClickListener {
            if (sfxOn) clickSFX?.start()
            val settingsIntent = Intent(this, SettingsActivity::class.java)
            startActivity(settingsIntent)
        }
    }


    /** onPause will pause music when going to another Activity */
    override fun onPause() {
        super.onPause()
        if (musicOn) introMusic?.pause()
    }


    /** onResume will reload preferences and resume music when returning to main screen */
    override fun onResume() {
        super.onResume()
        loadPreferences()
        if (musicOn) introMusic?.start()
    }


    /** onDestroy will release sounds and music when activity finishes */
    override fun onDestroy() {
        super.onDestroy()
        introMusic?.release()
        clickSFX?.release()
    }


    /** Load sound preferences */
    private fun loadPreferences() {
        val prefName = "soundPreferences"
        val dataBack: SharedPreferences = getSharedPreferences(prefName, 0)
        if (dataBack.contains("musicIsOn") || dataBack.contains("sfxIsOn")){
            musicOn = dataBack.getBoolean("musicIsOn", false)
            sfxOn = dataBack.getBoolean("sfxIsOn", false)
        }
    }
}