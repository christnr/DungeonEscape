package com.example.dungeonescape

import android.content.Intent
import android.content.SharedPreferences
import android.media.MediaPlayer
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import kotlinx.android.synthetic.main.activity_player_select.*

class PlayerSelect : AppCompatActivity() {

    //Flag to know if sound effects are muted orr not
    private var sfxOn = false

    //Set up media player for click sound effect
    private var clickSFX: MediaPlayer? = null

    //onCreate method
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_player_select)

        //Set up click sound effect
        clickSFX = MediaPlayer.create(this, R.raw.dungeon_escape_sfx_click)

        //Load sfx preference from saved preferences
        val prefName = "soundPreferences"
        val dataBack: SharedPreferences = getSharedPreferences(prefName, 0)
        if (dataBack.contains("sfxIsOn")) {
            sfxOn = dataBack.getBoolean("sfxIsOn", false)
        }

        //Set up "Back" button
        backButton.setOnClickListener {
            if (sfxOn) clickSFX?.start()
            val backIntent = Intent(this, MainActivity::class.java)
            startActivity(backIntent)
            finish()
        }

        golemButton.setOnClickListener {
            enterDungeon("Golem")
        }

        daemonButton.setOnClickListener {
            enterDungeon("Daemon")
        }

        slimeButton.setOnClickListener {
            enterDungeon("Slime")
        }
    }

    /** onDestroy method will release click sound effect */
    override fun onDestroy() {
        super.onDestroy()
        clickSFX?.release()
    }

    private fun enterDungeon(characterSelection: String) {
        if (sfxOn) clickSFX?.start()
        val dungeonIntent = Intent(this, DungeonActivity::class.java)
        dungeonIntent.putExtra("characterSelection", characterSelection)
        startActivity(dungeonIntent)
        finish()
    }
}