package me.cpele.watchbee.database.dao

import android.arch.lifecycle.LiveData
import android.arch.persistence.room.Dao
import android.arch.persistence.room.Insert
import android.arch.persistence.room.Query
import me.cpele.watchbee.api.User

@Dao
interface UserDao {
    @Query("SELECT * FROM User ORDER BY rowid LIMIT 1")
    fun findLast(): LiveData<User>

    @Insert
    fun insert(user: User)

    @Query("DELETE * FROM User")
    fun deleteAll()
}