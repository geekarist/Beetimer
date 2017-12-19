package me.cpele.beetimer

import android.arch.persistence.room.Embedded
import android.arch.persistence.room.Entity
import android.arch.persistence.room.PrimaryKey

@Entity
data class User(@PrimaryKey val username: String, @Embedded val goals: ArrayList<String>)