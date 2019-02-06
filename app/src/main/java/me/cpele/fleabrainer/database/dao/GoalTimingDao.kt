package me.cpele.fleabrainer.database.dao

import android.arch.lifecycle.LiveData
import android.arch.persistence.room.Dao
import android.arch.persistence.room.Insert
import android.arch.persistence.room.OnConflictStrategy
import android.arch.persistence.room.Query
import me.cpele.fleabrainer.domain.GoalTiming

@Dao
interface GoalTimingDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(list: List<GoalTiming>)

    @Query("SELECT * FROM GoalTiming ORDER BY running DESC, losedate ASC")
    fun findAll(): LiveData<List<GoalTiming>>

    @Query("SELECT * FROM GoalTiming")
    fun findAllSync(): List<GoalTiming>

    @Query("SELECT * FROM GoalTiming WHERE slug = :slug")
    fun findOneBySlug(slug: String): GoalTiming?

    @Query("SELECT * FROM GoalTiming WHERE slug = :slug")
    fun asyncFindOneBySlug(slug: String): LiveData<GoalTiming>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertOne(goalTiming: GoalTiming)
}