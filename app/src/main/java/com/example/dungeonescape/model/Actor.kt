package com.example.dungeonescape.model

import android.widget.ImageView
import androidx.core.view.isVisible
import com.example.dungeonescape.R
import kotlin.math.ln
import kotlin.math.pow

abstract class Actor {

    //Declare variables for all Actors

    //Base variables
    protected var characterType: String? = null
    protected var sprite: ImageView? = null

    //Stat variables
    protected var level = 1
    protected var xp = 1
    protected var currentHP = 1
    protected var maxHP = 1
    protected var attack = 1
    protected var defense = 1
    private val maxLevel = 10
    protected var alive = true

    //Positional variables
    protected var currentPosition = Vector2(0, 0)

    //Level Up Bonus variables
    protected var levelUpBonusRange = 1
    protected var levelUpBonusFloor = 0


    //When attacked, subtract defense points, then subtract damage from currentHP, return damageTaken
    fun takeDamage(attackTaken: Int): Int {
        var damageTaken = attackTaken - defense
        if (damageTaken <= 0) { damageTaken = 1}
        currentHP -= damageTaken
        return damageTaken
    }

    //Level Up function, increases level and stats by one level
    fun levelUp() {
        if (level < maxLevel) {
            level++
            maxHP += 5 + generateBonus()
            attack += 3 + generateBonus()
            defense += 3 + generateBonus()
            currentHP = maxHP  //refill life
        }
    }

    //Calculate random level up bonus
    private fun generateBonus(): Int {
        return (Math.random() * levelUpBonusRange + levelUpBonusFloor).toInt()
    }

    //Set new position
    @JvmName("setCurrentPosition1")
    fun setCurrentPosition(newPosition: Vector2) {
        currentPosition = newPosition
    }

    //Get current position
    @JvmName("getCurrentPosition1")
    fun getCurrentPosition(): Vector2 {
        return currentPosition
    }

    //Get current HP
    @JvmName("getCurrentHP1")
    fun getCurrentHP(): Int {
        return currentHP
    }

    //Get current Level
    @JvmName("getLevel1")
    fun getLevel(): Int {
        return level
    }

    //Get attack with variation of +-2
    @JvmName("getAttack1")
    fun getAttack(): Int {
        return attack + (Math.random() * 5 - 2).toInt()
    }

    //Get sprite
    @JvmName("getSprite1")
    fun getSprite(): ImageView? {
        return sprite
    }

    //Return XP
    fun getXP(): Int {
        return xp
    }

    //Return character type name
    fun getType(): String {
        return characterType!!
    }

    //Return status of alive or dead
    fun isAlive(): Boolean {
        return alive
    }

    //Function for instructions to perform on death
    abstract fun die()

}


//Define Player class as child of ActorClass class
class Player(): Actor() {

    //Declare variables used by player
    private var nextLevel = 1.0

    //Construct a new player
    constructor(characterType: String, sprite: ImageView) : this() {

        //Set general variables
        this.characterType = characterType
        this.sprite = sprite
        this.currentPosition = Vector2(7, 1)
        this.nextLevel = calculateNextLevel()

        //Set character type specific attributes
        when (characterType) {

            //Set stats for Golem monster type
            "Golem" -> {
                this.sprite!!.setImageResource(R.drawable.golem_anim)
                this.maxHP = 20
                this.attack = 10
                this.defense = 5
            }

            //Set stats for Daemon monster type
            "Daemon" -> {
                this.sprite!!.setImageResource(R.drawable.daemon_anim)
                this.maxHP = 15
                this.attack = 7
                this.defense = 3
                this.levelUpBonusRange = 2
            }

            //Set stats for Slime monster type
            "Slime" -> {
                this.sprite!!.setImageResource(R.drawable.slime_anim)
                this.maxHP = 10
                this.attack = 5
                this.defense = 2
                this.levelUpBonusRange = 3
                this.levelUpBonusFloor = 1
            }
        }

        //Fill HP to max
        this.currentHP = this.maxHP
    }

    //Calculate XP threshold to reach next level
    private fun calculateNextLevel(): Double {
        return (ln((level + 1).toDouble()) * (level + 1).toDouble().pow(2.4) + 4)
    }

    //Add earned XP to current XP, check for level up
    fun addXP(earnedXP: Int) {
        xp += earnedXP
        if (xp >= nextLevel) {
            levelUp()
            nextLevel = calculateNextLevel()
        }
    }

    //If dead, set HP to zero and change character sprite to tombstone
    override fun die(){
        currentHP = 0
        sprite!!.setImageResource(R.drawable.monster_tombstone)
    }
}

//Enemy class for Adventurers wandering around the dungeon
class Enemy(): Actor() {

    //Index of enemy in DungeonActivity
    private var index = 0

    //Construct an enemy of chosen type
    constructor(characterType: String, sprite: ImageView, startPosition: Vector2, level: Int, index: Int) : this() {

        //Set general variables for enemy
        this.characterType = characterType
        this.sprite = sprite
        this.currentPosition = startPosition
        this.index = index

        //Set character type specific attributes
        when (characterType) {

            //Set stats for Barbarian adventurer type
            "Barbarian" -> {
                this.sprite!!.setImageResource(R.drawable.adventurer_barbarian)
                this.maxHP = 10
                this.attack = 5
                this.defense = 5
            }

            //Set stats for Ranger adventurer type
            "Ranger" -> {
                this.sprite!!.setImageResource(R.drawable.adventurer_ranger)
                this.maxHP = 7
                this.attack = 3
                this.defense = 3
            }

            //Set stats for Wizard adventurer type
            "Wizard" -> {
                this.sprite!!.setImageResource(R.drawable.adventurer_wizard)
                this.maxHP = 5
                this.attack = 2
                this.defense = 1
                this.levelUpBonusRange = 2
            }

            //Set stats for Dungeon Boss type
            "Boss" -> {
                this.sprite!!.setImageResource(R.drawable.dungeon_boss)
                this.maxHP = 55
                this.attack = 8
                this.defense = 3
            }
        }

        //Level up enemy to given level
        while (this.level < level)
            levelUp()

        //Set enemy XP equal to their maximum HP
        this.xp = this.maxHP

        //Set current HP to max HP
        this.currentHP = this.maxHP
    }

    //Get enemy index
    @JvmName("getIndex1")
    fun getIndex(): Int {
        return index
    }

    //If enemy dies, hide sprite, set position outside of dungeon, and set alive flag to false
    override fun die() {
        sprite!!.isVisible = false
        currentPosition = Vector2(0, 0)
        alive = false
    }
}
