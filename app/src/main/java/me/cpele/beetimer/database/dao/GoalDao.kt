package me.cpele.beetimer.database.dao

import android.arch.lifecycle.LiveData
import android.arch.persistence.room.Dao
import android.arch.persistence.room.Insert
import android.arch.persistence.room.OnConflictStrategy
import android.arch.persistence.room.Query
import me.cpele.beetimer.api.Goal

@Dao
interface GoalDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(list: List<Goal>)

    @Query("SELECT * FROM goal")
    fun findAllGoals(): LiveData<List<Goal>>
}