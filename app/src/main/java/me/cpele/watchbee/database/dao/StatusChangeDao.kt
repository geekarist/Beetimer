package me.cpele.watchbee.database.dao

import android.arch.lifecycle.LiveData
import android.arch.persistence.room.Dao
import android.arch.persistence.room.Insert
import android.arch.persistence.room.Query
import me.cpele.watchbee.domain.StatusChange

@Dao
interface StatusChangeDao {
    @Query("SELECT * FROM StatusChange ORDER BY date DESC LIMIT 1")
    fun findLatestStatus(): LiveData<StatusChange>

    @Insert
    fun insert(status: StatusChange)
}

