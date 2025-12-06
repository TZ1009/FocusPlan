package com.example.focusplan;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.focusplan.ai.AiCategoryClassifier;
import com.example.focusplan.data.AppDatabase;
import com.example.focusplan.data.Record;
import com.example.focusplan.data.RecordDao;
import com.example.focusplan.utils.Prefs;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class MainActivity extends AppCompatActivity {

    private AppDatabase db;
    private RecordAdapter adapter;

    private RecyclerView rvRecords;
    private TextView tvSummary;
    private FloatingActionButton fabAdd;
    private PieChart pieChart;

    // 筛选相关
    private Spinner spMonth, spCategory;
    private List<String> monthList = new ArrayList<>();
    private List<String> categoryList = new ArrayList<>();
    private List<Record> allRecords = new ArrayList<>();

    private String selectedMonth = "全部月份";
    private String selectedCategory = "全部分类";

    private SimpleDateFormat monthFormat =
            new SimpleDateFormat("yyyy-MM", Locale.getDefault());

    private Prefs prefs;
    private static final String PREF_LAST_SELECTED_CATEGORY = "pref_last_selected_category";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        prefs = new Prefs(this);
        db = AppDatabase.getInstance(this);

        // 绑定控件
        spMonth = findViewById(R.id.spMonth);
        spCategory = findViewById(R.id.spCategory);
        rvRecords = findViewById(R.id.rvRecords);
        tvSummary = findViewById(R.id.tvSummary);
        fabAdd = findViewById(R.id.fabAdd);
        pieChart = findViewById(R.id.pieChart);

        // RecyclerView
        adapter = new RecordAdapter();
        rvRecords.setLayoutManager(new LinearLayoutManager(this));
        rvRecords.setAdapter(adapter);

        // 单击任务：弹出操作选择（切换状态 / 开番茄钟）
        adapter.setOnItemClickListener(record -> showTaskActionDialog(record));

// 长按：删除（你原来就有）
        adapter.setOnItemLongClickListener(record -> {
            new AlertDialog.Builder(MainActivity.this)
                    .setTitle("删除任务")
                    .setMessage("确定要删除这条任务吗？")
                    .setPositiveButton("删除", (dialog, which) -> deleteRecord(record))
                    .setNegativeButton("取消", null)
                    .show();
        });

        // 长按删除
        adapter.setOnItemLongClickListener(record -> {
            new AlertDialog.Builder(MainActivity.this)
                    .setTitle("删除任务")
                    .setMessage("确定要删除这条任务吗？")
                    .setPositiveButton("删除", (dialog, which) -> deleteRecord(record))
                    .setNegativeButton("取消", null)
                    .show();
        });

        // 添加任务按钮
        fabAdd.setOnClickListener(v -> showAddRecordDialog());

        // 月份选择监听
        spMonth.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position >= 0 && position < monthList.size()) {
                    selectedMonth = monthList.get(position);
                    applyFiltersAndUpdateUI();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        // 分类选择监听（筛选使用）
        spCategory.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position >= 0 && position < categoryList.size()) {
                    selectedCategory = categoryList.get(position);
                    applyFiltersAndUpdateUI();
                }
                // 记住上一次筛选用的分类
                getSharedPreferences("expense_prefs", MODE_PRIVATE)
                        .edit()
                        .putString(PREF_LAST_SELECTED_CATEGORY, selectedCategory)
                        .apply();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        // 初始化饼图外观
        initPieChart();

        // 恢复上次筛选的分类
        String savedCategory = getSharedPreferences("expense_prefs", MODE_PRIVATE)
                .getString(PREF_LAST_SELECTED_CATEGORY, "全部分类");
        selectedCategory = savedCategory;

        // 初次加载数据
        loadRecords();
    }

    /**
     * 从数据库加载所有任务，刷新内存列表、筛选列表、UI
     */
    private void loadRecords() {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            RecordDao dao = db.recordDao();
            List<Record> records = dao.getAllOrderByDeadlineDesc();

            // 更新内存中的总列表
            allRecords.clear();
            allRecords.addAll(records);

            // 统计月份和分类集合
            Set<String> monthSet = new HashSet<>();
            Set<String> categorySet = new HashSet<>();

            for (Record r : allRecords) {
                monthSet.add(monthFormat.format(new Date(r.deadline)));
                if (r.category != null && !r.category.trim().isEmpty()) {
                    categorySet.add(r.category.trim());
                }
            }

            // 生成 Spinner 数据源
            List<String> newMonthList = new ArrayList<>();
            newMonthList.add("全部月份");
            newMonthList.addAll(monthSet);

            List<String> newCategoryList = new ArrayList<>();
            newCategoryList.add("全部分类");
            newCategoryList.addAll(categorySet);

            runOnUiThread(() -> {
                // 更新月份 Spinner
                monthList.clear();
                monthList.addAll(newMonthList);
                ArrayAdapter<String> monthAdapter = new ArrayAdapter<>(
                        MainActivity.this,
                        android.R.layout.simple_spinner_item,
                        monthList
                );
                monthAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spMonth.setAdapter(monthAdapter);

                // 更新分类 Spinner
                categoryList.clear();
                categoryList.addAll(newCategoryList);
                ArrayAdapter<String> categoryAdapter = new ArrayAdapter<>(
                        MainActivity.this,
                        android.R.layout.simple_spinner_item,
                        categoryList
                );
                categoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spCategory.setAdapter(categoryAdapter);

                // 尽量保持之前选中的筛选条件
                int monthIndex = monthList.indexOf(selectedMonth);
                if (monthIndex < 0) monthIndex = 0;
                spMonth.setSelection(monthIndex, false);

                int categoryIndex = categoryList.indexOf(selectedCategory);
                if (categoryIndex < 0) categoryIndex = 0;
                spCategory.setSelection(categoryIndex, false);

                // 应用筛选并更新 UI + 图表
                applyFiltersAndUpdateUI();
            });
        });
    }

    /**
     * 根据当前选中的月份 + 分类，对 allRecords 做过滤，并更新列表和统计和饼图（Todo 版本）
     */
    private void applyFiltersAndUpdateUI() {
        List<Record> filtered = new ArrayList<>();

        for (Record r : allRecords) {
            boolean matchMonth = true;
            boolean matchCategory = true;

            // 月份筛选（使用 deadline）
            if (!"全部月份".equals(selectedMonth)) {
                String ym = monthFormat.format(new Date(r.deadline));
                matchMonth = selectedMonth.equals(ym);
            }

            // 分类（标签）筛选
            if (!"全部分类".equals(selectedCategory)) {
                String cat = (r.category == null) ? "" : r.category.trim();
                matchCategory = selectedCategory.equals(cat);
            }

            if (matchMonth && matchCategory) {
                filtered.add(r);
            }
        }

        // 统计任务完成情况
        int finished = 0;
        int unfinished = 0;
        for (Record r : filtered) {
            boolean done = "已完成".equals(r.status); // status: "已完成" / "未完成"
            if (done) finished++;
            else unfinished++;
        }

        adapter.setData(filtered);

        // 顶部汇总显示
        String filterDesc = "";
        if (!"全部月份".equals(selectedMonth)) {
            filterDesc += selectedMonth + " ";
        }
        if (!"全部分类".equals(selectedCategory)) {
            filterDesc += selectedCategory + " ";
        }
        if (filterDesc.isEmpty()) filterDesc = "全部任务";

        tvSummary.setText(String.format(Locale.getDefault(),
                "%s：已完成 %d 条 · 未完成 %d 条",
                filterDesc.trim(), finished, unfinished));

        // 更新饼图（Todo 版本）
        updatePieChart(finished, unfinished);
    }

    /**
     * 初始化饼图的基本样式
     */
    private void initPieChart() {
        pieChart.setUsePercentValues(false);
        pieChart.setDrawHoleEnabled(true);
        pieChart.setHoleRadius(45f);
        pieChart.setTransparentCircleRadius(50f);
        pieChart.setRotationEnabled(true);
        pieChart.setHighlightPerTapEnabled(true);

        Description desc = new Description();
        desc.setText("任务完成情况");
        pieChart.setDescription(desc);

        Legend l = pieChart.getLegend();
        l.setEnabled(true);
    }

    /**
     * 更新饼图数据（Todo版：已完成 vs 未完成）
     */
    private void updatePieChart(int finished, int unfinished) {

        List<PieEntry> entries = new ArrayList<>();

        if (finished > 0) {
            entries.add(new PieEntry(finished, "已完成"));
        }
        if (unfinished > 0) {
            entries.add(new PieEntry(unfinished, "未完成"));
        }

        if (entries.isEmpty()) {
            pieChart.clear();
            pieChart.invalidate();
            return;
        }

        PieDataSet dataSet = new PieDataSet(entries, "");
        dataSet.setSliceSpace(3f);
        dataSet.setValueTextSize(12f);

        List<Integer> colors = new ArrayList<>();
        colors.add(0xFF81C784); // 完成：绿色
        colors.add(0xFFE57373); // 未完成：红色
        dataSet.setColors(colors);

        PieData data = new PieData(dataSet);
        pieChart.setData(data);
        pieChart.invalidate();
    }

    /**
     * 显示添加任务的对话框（沿用 dialog_add_record.xml）
     */
    private void showAddRecordDialog() {
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_add_record, null, false);

        // 原来的字段
        EditText etTitle = view.findViewById(R.id.etAmount);     // 当标题
        EditText etCategory = view.findViewById(R.id.etCategory);
        EditText etNote = view.findViewById(R.id.etNote);

        // ========== 新增的控件 ==========
        Button btnPickDate = view.findViewById(R.id.btnPickDate);
        TextView tvSelectedDate = view.findViewById(R.id.tvSelectedDate);

        // 存储用户选择的 deadline（默认：今天的 23:59）
        final long[] pickedDeadline = {System.currentTimeMillis()};
        {
            Calendar cal = Calendar.getInstance();
            cal.set(Calendar.HOUR_OF_DAY, 23);
            cal.set(Calendar.MINUTE, 59);
            cal.set(Calendar.SECOND, 59);
            cal.set(Calendar.MILLISECOND, 0);
            pickedDeadline[0] = cal.getTimeInMillis();
        }

        // 日期选择器
        btnPickDate.setOnClickListener(v -> {
            Calendar cal = Calendar.getInstance();
            new DatePickerDialog(
                    MainActivity.this,
                    (view1, year, month, dayOfMonth) -> {
                        Calendar c = Calendar.getInstance();
                        c.set(year, month, dayOfMonth, 23, 59, 59);
                        pickedDeadline[0] = c.getTimeInMillis();

                        tvSelectedDate.setText("截止日期：" + year + "-" + (month + 1) + "-" + dayOfMonth);
                    },
                    cal.get(Calendar.YEAR),
                    cal.get(Calendar.MONTH),
                    cal.get(Calendar.DAY_OF_MONTH)
            ).show();
        });


        new AlertDialog.Builder(this)
                .setTitle("添加任务")
                .setView(view)
                .setPositiveButton("保存", (dialog, which) -> {

                    // 标题
                    String title = etTitle.getText().toString().trim();
                    if (title.isEmpty()) {
                        Toast.makeText(MainActivity.this, "标题不能为空", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    // 标签
                    String category = etCategory.getText().toString().trim();
                    if (category.isEmpty()) category = "未分类";

                    // 备注
                    String note = etNote.getText().toString().trim();

                    // 状态
                    String status = "未完成";

                    // deadline = 用户选择的日期
                    long deadline = pickedDeadline[0];

                    // 保存任务
                    Record record = new Record(
                            title,
                            status,
                            category,
                            deadline,
                            note
                    );
                    saveRecord(record);
                })
                .setNegativeButton("取消", null)
                .show();
    }


    /**
     * 保存任务到数据库
     */
    private void saveRecord(Record record) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            db.recordDao().insert(record);
            loadRecords();
        });
    }

    /**
     * 删除任务
     */
    private void deleteRecord(Record record) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            db.recordDao().delete(record);
            loadRecords();
        });
    }
    /**
     * 点击任务时：弹出操作选择对话框
     */
    private void showTaskActionDialog(Record record) {
        String[] actions = {"切换完成状态", "为该任务启动番茄钟"};

        new AlertDialog.Builder(MainActivity.this)
                .setTitle(record.title)
                .setItems(actions, (dialog, which) -> {
                    if (which == 0) {
                        // 切换任务完成状态
                        toggleRecordStatus(record);
                    } else if (which == 1) {
                        // 启动番茄钟，并绑定此任务
                        startPomodoroForRecord(record);
                    }
                })
                .show();
    }
    /**
     * 点击任务条目时：在“未完成”和“已完成”之间切换
     */
    private void toggleRecordStatus(Record record) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            // 翻转状态
            if ("已完成".equals(record.status)) {
                record.status = "未完成";
            } else {
                record.status = "已完成";
            }

            // 更新数据库
            db.recordDao().update(record);

            // 重新加载列表（会自动刷新统计和饼图）
            loadRecords();
        });
    }

    /**
     * 为指定任务启动番茄钟
     */
    private void startPomodoroForRecord(Record record) {
        Intent intent = new Intent(MainActivity.this, PomodoroActivity.class);
        intent.putExtra(PomodoroActivity.EXTRA_TASK_ID, record.id);
        intent.putExtra(PomodoroActivity.EXTRA_TASK_TITLE, record.title);
        startActivity(intent);
    }


}
