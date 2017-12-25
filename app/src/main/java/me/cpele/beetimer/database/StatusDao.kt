package me.cpele.beetimer.database

import android.arch.lifecycle.LiveData
import android.arch.persistence.room.Dao
import android.arch.persistence.room.Insert
import android.arch.persistence.room.OnConflictStrategy
import android.arch.persistence.room.Query

@Dao
interface StatusDao {
    @Query("SELECT * FROM StatusContainer ORDER BY rowid DESC LIMIT 1")
    fun findLatestStatus(): LiveData<StatusContainer>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun updateLatestStatus(loading: StatusContainer)
}

