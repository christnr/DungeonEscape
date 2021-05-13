package com.example.dungeonescape

import android.content.Intent
import android.content.SharedPreferences
import android.content.res.Resources
import android.graphics.drawable.AnimationDrawable
import android.media.MediaPlayer
import android.os.Bundle
import android.os.Handler
import android.view.animation.AnimationUtils
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import com.example.dungeonescape.model.Actor
import com.example.dungeonescape.model.Enemy
import com.example.dungeonescape.model.Player
import com.example.dungeonescape.model.Vector2
import kotlinx.android.synthetic.main.activity_dungeon.*
import kotlin.collections.HashMap

class DungeonActivity : AppCompatActivity() {


    /** Positional variables */
    //locations of walls
    private val wallLocations = listOf(Vector2(3, 1), Vector2(3, 2), Vector2(5, 1), Vector2(5, 2))

    //positional limits
    private val minPosition = 1
    private val maxPosition = 7

    //enemy starting points
    private val enemyStartPos = listOf(Vector2(1, 5), Vector2(4, 7), Vector2(7, 5))
    private var spawnIndex = 0
    private var spawnOffset = Vector2()
    private var enemyStartPosPixels = mutableListOf<FloatArray>()

    //boss starting point
    private var bossStartPos = Vector2(4, 2)

    //door position
    private val doorPosition = Vector2(4, 1)


    /** Music variables */
    //Mute flags
    private var musicOn = false
    private var sfxOn = false

    //Define sound effect players
    private var sfxStep: MediaPlayer? = null
    private var sfxBump: MediaPlayer? = null
    private var sfxClick: MediaPlayer? = null
    private var sfxDie: MediaPlayer? = null
    private var sfxAttack: MediaPlayer? = null
    private var sfxLevel: MediaPlayer? = null

    //Define music player
    private var musicMP: MediaPlayer? = null

    //Define flags for game play
    private var isTakingTurn = false
    private var playerDead = false
    private var dungeonCleared = false


    /** Actor object variables */
    //Define objects for game
    private var playerCharacter: Actor? = null

    private var enemies = mutableListOf<Actor>()
    private val enemyClasses = arrayListOf("Barbarian", "Ranger", "Wizard")
    private val numEnemies = 3
    private var enemySprites: Array<ImageView>? = null

    private var bossCharacter: Actor? = null
    private var blockingEnemy: Actor? = null

    private var respawnCounter = arrayListOf(-1, -1, -1)

    private val directionMap = HashMap<String, Vector2>()


    /** onCreate function */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dungeon)

        //catch player selection from selection screen
        val bundle: Bundle? = intent.extras

        //Load sound preferences from saved preferences
        val prefName = "soundPreferences"
        val dataBack: SharedPreferences = getSharedPreferences(prefName, 0)
        if (dataBack.contains("musicIsOn") || dataBack.contains("sfxIsOn")){
            musicOn = dataBack.getBoolean("musicIsOn", false)
            sfxOn = dataBack.getBoolean("sfxIsOn", false)
        }

        //Load Variables to set up game
        loadVariables()

        //Play music
        playMusic()

        //Set up dungeon
        setDungeon(bundle?.get("characterSelection") as String)

        //Update Heads Up Display
        updateHUD()

        //Set up buttons
        setButtons()
    }


    /** onPause function pauses music when not in the foregroud */
    override fun onPause() {
        super.onPause()
        if (musicMP !== null && !dungeonCleared && !playerDead)
            musicMP?.pause()
    }


    /** onResume function resumes music when application comes to the foreground */
    override fun onResume() {
        super.onResume()
        if (musicMP !== null && musicOn)
            musicMP?.start()
    }


    /** onDestroy will release all sounds and music */
    override fun onDestroy() {
        super.onDestroy()
        musicMP?.release()
        sfxStep?.release()
        sfxBump?.release()
        sfxClick?.release()
        sfxDie?.release()
        sfxAttack?.release()
        sfxLevel?.release()
    }

    /** onStart will start the player character animation */
    override fun onStart() {
        super.onStart()
        (player_imageview.drawable as AnimationDrawable).start()
    }


    /** Set up variables referenced in game */
    private fun loadVariables() {
        //Set up directionMap
        directionMap["North"] = Vector2(0, -1)
        directionMap["South"] = Vector2(0, 1)
        directionMap["East"] = Vector2(1, 0)
        directionMap["West"] = Vector2(-1, 0)

        //Set up sound effects
        sfxStep = MediaPlayer.create(this, R.raw.dungeon_escape_sfx_step)
        sfxBump = MediaPlayer.create(this, R.raw.dungeon_escape_sfx_bump)
        sfxClick = MediaPlayer.create(this, R.raw.dungeon_escape_sfx_click)
        sfxDie = MediaPlayer.create(this, R.raw.dungeon_escape_sfx_die)
        sfxAttack = MediaPlayer.create(this, R.raw.dungeon_escape_sfx_attack)
        sfxLevel = MediaPlayer.create(this, R.raw.dungeon_escape_sfx_level_up)

        //Set up enemy Image Views into Array
        enemySprites = arrayOf(adventurer1_imageview, adventurer2_imageview, adventurer3_imageview)

        //Save enemy starting positions in pixels for resetting Image Views after enemy dies.
        enemyStartPosPixels.add(0, floatArrayOf(adventurer1_imageview.translationX, adventurer1_imageview.translationY))
        enemyStartPosPixels.add(1, floatArrayOf(adventurer2_imageview.translationX, adventurer2_imageview.translationY))
        enemyStartPosPixels.add(2, floatArrayOf(adventurer3_imageview.translationX, adventurer3_imageview.translationY))
    }

    /** Set up and play music */
    private fun playMusic() {
        if (musicMP == null) {
            musicMP = MediaPlayer.create(this, R.raw.dungeon_escape_music_dungeon)
            if (musicOn) {
                musicMP?.isLooping = true
                musicMP?.start()
            }
        }
    }


    /** Set up dungeon */
    private fun setDungeon(playerSelection: String) {
        //Create player character object
        playerCharacter = Player(playerSelection, player_imageview)

        //Create three randomly selected class of enemies
        for (i in 0 until numEnemies) {
            val enemySelection = enemyClasses.random()
            enemies.add(Enemy(enemySelection, enemySprites!![i], enemyStartPos[i], 1, i))
        }

        //Create boss
        bossCharacter = Enemy("Boss", boss_imageview, bossStartPos, 5, -1)
    }

    /** Respawn enemy */
    private fun respawn(index: Int) {

        //Choose new random class
        val enemySelection = enemyClasses.random()

        //Generate spawn point
        val spawnPoint = findOpenSpawnPoint()

        //Generate new Enemy
        enemies[index] = Enemy(enemySelection, enemySprites!![index], spawnPoint, playerCharacter!!.getLevel(), index)

        //Set sprite in proper location on screen
        enemies[index].getSprite()!!.translationX = enemyStartPosPixels[spawnIndex][0]
        enemies[index].getSprite()!!.translationY = enemyStartPosPixels[spawnIndex][1]

        //Calculate offset in pixels, one position is 45dp
        val pixelOffset = spawnOffset.times(45.toPx())

        //Adjust sprite X and Y coordinates by offset
        enemies[index].getSprite()!!.translationX += pixelOffset.x
        enemies[index].getSprite()!!.translationY += pixelOffset.y

        //Make sprite visible
        enemies[index].getSprite()!!.isVisible = true

        //Announce spawning
        updateMessages("${enemies[index].getType()} has entered.")
    }

    /** Find an open spawn point to respawn enemy */
    private fun findOpenSpawnPoint(): Vector2 {

        //Variables used to find an open spawn point
        var isValidPoint: Boolean
        var tryThisPoint = Vector2()

        //Shuffle all available indexes to try in random order
        val indexToTry = mutableListOf(0, 1, 2)
        indexToTry.shuffle()

        //Try each index
        for (i in indexToTry){

            //Reset variables
            isValidPoint = true
            tryThisPoint = enemyStartPos[i]
            spawnOffset = Vector2()
            spawnIndex = 0

            //Check for player or enemy in spawn point
            if (isActorInPosition(tryThisPoint))
                isValidPoint = false

            //If no actor is in spawn point, note location index, then break out of loop to return valid location
            if (isValidPoint) {
                spawnIndex = i
                break
            }

            //If spawn point fails, check adjacent locations
            val dirStr = directionMap.keys
            for (direction in dirStr) {
                val adjacentPoint = tryThisPoint.plus(directionMap[direction]!!)

                //Check if adjacent point is a wall, if so try next adjacent location
                if (adjacentPoint.x < minPosition || adjacentPoint.x > maxPosition ||
                        adjacentPoint.y < minPosition || adjacentPoint.y > maxPosition)
                            continue

                //Check for player or enemy is in adjacent point
                if (isActorInPosition(adjacentPoint))
                    isValidPoint = false

                //If no actor is in adjacent point, note location index and offset, then break out of loop to return adjacent location
                if (isValidPoint) {
                    tryThisPoint = adjacentPoint
                    spawnIndex = i
                    spawnOffset = directionMap[direction]!!
                    break
                }
            }

        }

        //Return valid location
        return tryThisPoint
    }

    /** Check if player or enemies are standing in spawn point */
    private fun isActorInPosition (position: Vector2): Boolean {
        //Check if player is in candidate spawn position
        if (playerCharacter!!.getCurrentPosition().equals(position))
            return true

        //Check if any enemies are in candidate spawn position
        for (enemy in enemies)
            if (enemy.getCurrentPosition().equals(position))
                return true

        //If no actors are in position, return false
        return false
    }

    /** Set up buttons */
    private fun setButtons() {
        //Set back button
        back_imagebutton.setOnClickListener {
            if (sfxOn) sfxClick?.start()
            if (!dungeonCleared && !playerDead and !isTakingTurn) {
                returnToMain()
            }
        }

        //Set button listeners for directional buttons
        up_imagebutton.setOnClickListener {
            if (!isTakingTurn && !dungeonCleared && !playerDead)
                mainPhase("North")
        }

        down_imagebutton.setOnClickListener {
            if (!isTakingTurn && !dungeonCleared && !playerDead)
                mainPhase("South")
        }

        right_imagebutton.setOnClickListener {
            if (!isTakingTurn && !dungeonCleared && !playerDead)
                mainPhase("East")
        }

        left_imagebutton.setOnClickListener {
            if (!isTakingTurn && !dungeonCleared && !playerDead)
                mainPhase("West")
        }

    }


    /** Main turn phase */
    private fun mainPhase(direction: String) {
        //Set flag so no other inputs can will be processed while turn is being taken
        isTakingTurn = true

        //Player takes turn
        playerPhase(direction)

        //Each enemy takes turn
        for (i in 0 until numEnemies) {
            enemyPhase(i)
        }

        //Boss takes turn
        bossPhase()

        //Update the Heads Up Display
        updateHUD()

        //Set flag so new turn can be taken
        isTakingTurn = false
    }


    /** Player's turn phase */
    private fun playerPhase(dirStr: String) {

        //set directional vector unit
        val dirIndex = directionMap[dirStr]

        //Check if wall is in the way of chosen direction
        if (isWallInWay(playerCharacter, dirIndex!!)) {
            updateMessages("You cannot go that way.")
            if (sfxOn) sfxBump?.start()
        } else {

            //If enemy is in chosen direction, attack that enemy
            if (isEnemyInWay(playerCharacter, dirIndex)) {
                if (sfxOn) sfxAttack?.start()
                val thisDamage = blockingEnemy!!.takeDamage(playerCharacter!!.getAttack())
                updateMessages("Attack ${blockingEnemy?.getType()} for $thisDamage points!")

                //Check if enemy has died
                if (blockingEnemy!!.getCurrentHP() <= 0) {

                    //Get XP from enemy and add to player score, also check for level up
                    val previousLevel = playerCharacter!!.getLevel()
                    (playerCharacter as Player).addXP(blockingEnemy!!.getXP())
                    if (playerCharacter!!.getLevel() != previousLevel) {
                        if (sfxOn) sfxLevel!!.start()
                        updateMessages("You are now level ${playerCharacter!!.getLevel()}")
                    }

                    //Call enemy function on their death
                    blockingEnemy!!.die()
                    updateMessages("You have defeated ${blockingEnemy!!.getType()}")

                    //If the enemy is not the Boss, set a respawn counter
                    if (blockingEnemy!!.getType() != "Boss")
                        respawnCounter[(blockingEnemy as Enemy).getIndex()] = 2
                }

            } else {
                //If path is clear, move player in chosen direction
                moveActor(playerCharacter, dirStr)
                updateMessages("Moved $dirStr")
                if (sfxOn) sfxStep?.start()
            }
        }

        //if player has reached door, stop game and display win message
        if (playerCharacter!!.getCurrentPosition().equals(doorPosition)) {
            dungeonCleared()
        }
    }


    /** Enemy's turn phase */
    private fun enemyPhase(i: Int) {

        //Take turn if player is not dead yet
        if (!playerDead) {

            //Take turn if enemy is alive
            if (enemies[i].isAlive()){

                //If player is adjacent to current location, attack player
                if (isPlayerAdjacent(enemies[i])) {
                    if (sfxOn) sfxAttack?.start()
                    val thisDamage = playerCharacter!!.takeDamage(enemies[i].getAttack())
                    updateMessages("${enemies[i].getType()} attacks for $thisDamage points!")

                    //Check if player has died
                    if (playerCharacter!!.getCurrentHP() <= 0)
                        gameOver()

                } else {

                    //Move adventurer in random direction
                    val dirStr = directionMap.keys.random()
                    val dirIndex = directionMap[dirStr]

                    //Check if wall is in way, if not then move
                    if (!isWallInWay(enemies[i], dirIndex!!) && !isEnemyInWay(enemies[i], dirIndex))
                        moveActor(enemies[i], dirStr)
                }

            } else {

                //If enemy is dead, count down respawn counter
                respawnCounter[i]--

                //Respawn if counter has reached below zero
                if (respawnCounter[i] <= -1)
                    respawn(i)
            }
        }
    }


    /** Boss's turn phase*/
    private fun bossPhase() {

        //Take turn if Boss is alive
        if (bossCharacter!!.isAlive()){

            //If player is standing in the one spot that the Boss can attack, then attack the player
            if (playerCharacter?.getCurrentPosition()!!.equals(Vector2(4, 3))) {
                if (sfxOn) sfxAttack?.start()
                val thisDamage = playerCharacter!!.takeDamage(bossCharacter!!.getAttack())
                updateMessages("${bossCharacter!!.getType()} attacks for $thisDamage points!")

                //Check if player has died
                if (playerCharacter!!.getCurrentHP() <= 0)
                    gameOver()
            }
        }
    }


    /** Events for when game won */
    private fun dungeonCleared() {

        //Set cleared flag to prevent further player inputs
        dungeonCleared = true

        //Stop player animation
        (player_imageview.drawable as AnimationDrawable).stop()

        //Animation of player leaving dungeon
        val animation = AnimationUtils.loadAnimation(this, R.anim.fade_out)
        playerCharacter!!.getSprite()!!.startAnimation(animation)

        //stop music
        stopMusic()

        //show clear logo
        clear_imageview.isVisible = true

        //display clear message in message box
        updateMessages("Dungeon Clear!")

        //play clear jingle
        val clearJingle = MediaPlayer.create(this, R.raw.dungeon_escape_clear_jingle)
        if (musicOn) clearJingle.start()

        //pause for 2 seconds, then return to main menu
        Handler().postDelayed(
                {
                    clearJingle.stop()
                    clearJingle.release()
                    returnToMain()
                }, 2500
        )
    }

    /** Events if player dies */
    private fun gameOver() {

        //Stop music, tell player they have died, set player death flag
        stopMusic()
        playerCharacter!!.die()
        playerDead = true

        //Output message of player's demise
        updateMessages("")
        updateMessages("You have perished...")
        updateMessages("")

        //Hear the death throes of player
        if (sfxOn) sfxDie?.start()

        //Display "Game Over" and pause, then return to the main menu
        gameOverImageView.isVisible = true
        Handler().postDelayed(
                {
                    sfxDie?.stop()
                    sfxDie?.release()
                    returnToMain()
                }, 2500
        )

    }


    /** Check if wall is blocking chosen direction */
    private fun isWallInWay(actor: Actor?, dirIndex: Vector2): Boolean {

        //Find location of new position
        val newPosition = actor!!.getCurrentPosition().plus(dirIndex)

        //Check if new position is outside the bounds of play
        if (newPosition.x < minPosition || newPosition.x > maxPosition || newPosition.y < minPosition || newPosition.y > maxPosition)
            return true

        //Check if new position is one of the four inner wall locations
        for (wall in wallLocations) {
            if (newPosition.equals(wall))
                return true
        }

        //If no wall is blocking the way, return false
        return false
    }


    /** Check if enemy is blocking chosen direction */
    private fun isEnemyInWay(actor: Actor?, dirIndex: Vector2): Boolean {

        //Find location of new position
        val newPosition = actor!!.getCurrentPosition().plus(dirIndex)

        //Check if any enemies are occupying the new position
        for (enemy in enemies) {
            if (newPosition.equals(enemy.getCurrentPosition())) {
                blockingEnemy = enemy
                return true
            }
        }

        //Check if the boss is occupying the new position
        if (newPosition.equals(bossCharacter!!.getCurrentPosition())) {
            blockingEnemy = bossCharacter
            return true
        }

        //If no enemies are in the way, return false
        return false
    }


    /** Check if player is adjacent to enemy */
    private fun isPlayerAdjacent(actor: Actor?): Boolean {

        //Get current position of the enemy and the player
        val currentPosition = actor?.getCurrentPosition()
        val playerPosition = playerCharacter?.getCurrentPosition()

        //If the player position is N, S, E, or W of the current position return true
        if (playerPosition!!.equals(currentPosition!!.plus(directionMap["North"]!!)) ||
                playerPosition.equals(currentPosition.plus(directionMap["South"]!!)) ||
                playerPosition.equals(currentPosition.plus(directionMap["East"]!!)) ||
                playerPosition.equals(currentPosition.plus(directionMap["West"]!!)))
                    return true

        //If player is not adjacent to current location return false
        return false
    }


    /** Move actor on the screen */
    private fun moveActor(actor: Actor?, dirStr: String) {

        //Get current position of actor moving
        val currentPosition = actor!!.getCurrentPosition()

        //Set new position of actor
        actor.setCurrentPosition(currentPosition.plus(directionMap[dirStr]!!))

        //Calculate movement of actor's sprite in pixels, one position is 45dp
        val dirPixels = directionMap[dirStr]!!.times(45.toPx())

        //Move X and Y coordinates of actor's sprite
        actor.getSprite()!!.translationX += dirPixels.x
        actor.getSprite()!!.translationY += dirPixels.y
    }


    /** Update player's current level and HP at top of screen */
    private fun updateHUD() {
        if  (playerCharacter!!.getLevel() >= 10)
            lvNum_textview.text = getString(R.string.max_string)
        else
            lvNum_textview.text = playerCharacter?.getLevel().toString()
        hpNum_textview.text = playerCharacter?.getCurrentHP().toString()
    }


    /** Add new message and scroll messages down in message box */
    private fun updateMessages(m: String) {
        message1_textview.text = message2_textview.text
        message2_textview.text = message3_textview.text
        message3_textview.text = message4_textview.text
        message4_textview.text = message5_textview.text
        message5_textview.text = m
    }


    /** Function to return to the main menu */
    private fun returnToMain() {
        val mainIntent = Intent(this, MainActivity::class.java)
        startActivity(mainIntent)
        finish()
    }


    /** Function to stop the music playing */
    private fun stopMusic() {
        if (musicOn) musicMP?.stop()
    }


    /** Convert dp to Pixels */
    private fun Int.toPx(): Int = (this * Resources.getSystem().displayMetrics.density).toInt()

}
