package com.example.focusplan;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.focusplan.data.Record;
import com.example.focusplan.R;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class RecordAdapter extends RecyclerView.Adapter<RecordAdapter.RecordViewHolder> {

    public interface OnItemLongClickListener {
        void onItemLongClick(Record record);
    }

    public interface OnItemClickListener {
        void onItemClick(Record record);
    }

    private OnItemClickListener clickListener;

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.clickListener = listener;
    }

    private final List<Record> data = new ArrayList<>();
    private final SimpleDateFormat dateFormat =
            new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());

    private OnItemLongClickListener longClickListener;

    public void setOnItemLongClickListener(OnItemLongClickListener listener) {
        this.longClickListener = listener;
    }

    public void setData(List<Record> records) {
        data.clear();
        if (records != null) {
            data.addAll(records);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public RecordViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_record, parent, false);
        return new RecordViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecordViewHolder holder, int position) {
        Record task = data.get(position);

        // status: "已完成" / "未完成"
        boolean done = "已完成".equals(task.status);
        String statusIcon = done ? "✔ " : "○ ";

        // 第一行：状态 + 标题
        holder.tvTopLine.setText(statusIcon + task.title);

        // 第二行：截止时间
        String deadlineStr = dateFormat.format(new Date(task.deadline));
        holder.tvTime.setText("截止：" + deadlineStr);

        // 第三行：标签 + 是否完成
        String cat = (task.category == null) ? "未分类" : task.category;
        holder.tvNote.setText("标签：" + cat + (done ? " · 已完成" : " · 未完成"));

        holder.itemView.setOnClickListener(v -> {
            if (clickListener != null) {
                clickListener.onItemClick(task);
            }
        });

        // 长按删除
        holder.itemView.setOnLongClickListener(v -> {
            if (longClickListener != null) {
                longClickListener.onItemLongClick(task);
            }
            return true;
        });
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    static class RecordViewHolder extends RecyclerView.ViewHolder {
        TextView tvTopLine, tvTime, tvNote;

        public RecordViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTopLine = itemView.findViewById(R.id.tvTopLine);
            tvTime = itemView.findViewById(R.id.tvTime);
            tvNote = itemView.findViewById(R.id.tvNote);
        }
    }
}
