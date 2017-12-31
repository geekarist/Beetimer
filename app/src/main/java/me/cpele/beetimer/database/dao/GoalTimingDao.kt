package me.cpele.beetimer.database.dao

import android.arch.lifecycle.LiveData
import android.arch.persistence.room.Dao
import android.arch.persistence.room.Insert
import android.arch.persistence.room.OnConflictStrategy
import android.arch.persistence.room.Query
import me.cpele.beetimer.domain.GoalTiming

@Dao
interface GoalTimingDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(list: List<GoalTiming>)

    @Query("SELECT * FROM GoalTiming")
    fun findAll(): LiveData<List<GoalTiming>>

    @Query("SELECT * FROM GoalTiming WHERE slug = :slug")
    fun findOneBySlug(slug: String): GoalTiming?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertOne(goalTiming: GoalTiming)
}