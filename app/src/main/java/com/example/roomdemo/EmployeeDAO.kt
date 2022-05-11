package com.example.roomdemo

import androidx.room.*
import java.util.concurrent.Flow


@Dao
interface EmployeeDAO {

    @Insert
    suspend fun insert(employeeEntity: EmployeeEntity)

    @Update
    suspend fun update(employeeEntity: EmployeeEntity)

    @Delete
    suspend fun delete(employeeEntity: EmployeeEntity)

    @Query("SELECT * FROM `employee-table`")
    fun fetchAllEmployees():kotlinx.coroutines.flow.Flow<List<EmployeeEntity>>

    @Query("SELECT * FROM `employee-table` WHERE id=:id")
    fun fetchEmployeeById(id:Int):kotlinx.coroutines.flow.Flow<EmployeeEntity>



}