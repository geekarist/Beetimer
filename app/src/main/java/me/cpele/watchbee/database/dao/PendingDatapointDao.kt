package me.cpele.watchbee.database.dao

import android.arch.lifecycle.LiveData
import android.arch.persistence.room.Dao
import android.arch.persistence.room.Delete
import android.arch.persistence.room.Insert
import android.arch.persistence.room.Query
import me.cpele.watchbee.domain.DatapointBo

@Dao
interface PendingDatapointDao {

    @Insert
    fun insertOne(datapoint: DatapointBo)

    @Query("SELECT * FROM PendingDatapoint WHERE userName = :userName")
    fun findAll(userName: String): List<DatapointBo>

    @Delete
    fun deleteOne(datapoint: DatapointBo)

    @Query("SELECT * FROM PendingDatapoint WHERE userName = :userName AND goalSlug = :slug")
    fun findBySlug(userName: String, slug: String): LiveData<List<DatapointBo>>
}