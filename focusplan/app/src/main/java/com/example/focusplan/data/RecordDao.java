package com.example.focusplan.data;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;


import java.util.List;

@Dao
public interface RecordDao {

    @Insert
    void insert(Record record);

    @Delete
    void delete(Record record);

    @Update
    void update(Record record);

    // 按 deadline 降序排序（Todo 的截止时间）
    @Query("SELECT * FROM Record ORDER BY deadline DESC")
    List<Record> getAllOrderByDeadlineDesc();

    // 查询某个月份的任务（使用 deadline）
    @Query("SELECT * FROM Record WHERE deadline BETWEEN :start AND :end ORDER BY deadline DESC")
    List<Record> getRecordsBetween(long start, long end);
}
