package com.gdsc.bingo.services.database.manager

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface VersionControlDao {
    // select all
    @Query("SELECT * FROM version_control")
    fun getAll(): List<VersionControlTable>

    // select by name
    @Query("SELECT * FROM version_control WHERE name = :name")
    fun getByName(name: String): VersionControlTable

    // insert
    @Insert
    fun insert(versionControlTable: VersionControlTable)

    // delete
    @Query("DELETE FROM version_control WHERE name = :name")
    fun deleteByName(name: String)

    // update
    @Query("UPDATE version_control SET version = :version WHERE name = :name")
    fun updateVersionByName(name: String, version: String)

    // delete all
    @Query("DELETE FROM version_control")
    fun deleteAll()
}