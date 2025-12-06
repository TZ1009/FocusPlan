package com.example.focusplan.data;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

/**
 * Todo List 使用的 Record 实体（保持类名不变）
 *
 * 字段说明：
 * id        —— 主键，自增
 * title     —— 任务标题
 * status    —— "未完成" / "已完成"
 * category  —— 任务分类（标签）
 * deadline  —— 截止时间（毫秒时间戳）
 * note      —— 备注
 */
@Entity
public class Record {

    @PrimaryKey(autoGenerate = true)
    public int id;

    // ========== Todo List 字段 ==========
    public String title;      // 任务标题
    public String status;     // "未完成" 或 "已完成"
    public String category;   // 标签
    public long deadline;     // 截止时间（时间戳）
    public String note;       // 任务备注（可选）

    // ========== 构造函数 ==========
    public Record(String title, String status, String category, long deadline, String note) {
        this.title = title;
        this.status = status;
        this.category = category;
        this.deadline = deadline;
        this.note = note;
    }
}
