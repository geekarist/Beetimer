package me.cpele.watchbee.database.dao

import android.arch.lifecycle.LiveData
import android.arch.persistence.room.Dao
import android.arch.persistence.room.Insert
import android.arch.persistence.room.OnConflictStrategy
import android.arch.persistence.room.Query
import me.cpele.watchbee.domain.DatapointBo

@Dao
interface PendingDatapointDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertOne(datapoint: DatapointBo)

    @Query("SELECT * FROM PendingDatapoint WHERE userName = :userName AND pending = 1")
    fun findPendingByUser(userName: String): List<DatapointBo>

    @Query("SELECT * FROM PendingDatapoint WHERE userName = :userName AND goalSlug = :slug")
    fun findBySlug(userName: String, slug: String): LiveData<List<DatapointBo>>
}