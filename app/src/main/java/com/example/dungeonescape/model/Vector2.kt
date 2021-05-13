package com.example.dungeonescape.model

//Vector2 class to easily track points on a 2D coordinate plane
class Vector2 {

    //Variables
    var x: Int = 0
    var y: Int = 0

    //Construct new Vector2 with given x and y coordinates
    constructor(x: Int, y: Int): this() {
        this.x = x
        this.y = y
    }

    //Default constructor
    constructor() {
        this.x = 0
        this.y = 0
    }

    //Add two Vector2's
    fun plus(v: Vector2): Vector2 {
        return Vector2(this.x + v.x, this.y + v.y)
    }

    //Subtract two Vector2's
    fun minus(v: Vector2): Vector2 {
        return this.plus(Vector2(v.x * -1, v.y * -1))
    }

    //Scale a Vector2 by multiplying by a float, return floatArray
    fun times(scalar: Int): Vector2 {
        return Vector2(this.x * scalar, this.y * scalar)
    }

    //Test if two Vector2's are equal
    fun equals(v: Vector2): Boolean {
        if (this.x == v.x && this.y == v.y)
            return true
        return false
    }

}