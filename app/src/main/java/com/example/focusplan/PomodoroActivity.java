package com.example.focusplan;

import android.os.Bundle;
import android.os.CountDownTimer;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;


public class PomodoroActivity extends AppCompatActivity {

    // 默认 25 分钟（单位毫秒）
    private static final long WORK_DURATION = 25 * 60 * 1000;
    public static final String EXTRA_TASK_ID = "extra_task_id";
    public static final String EXTRA_TASK_TITLE = "extra_task_title";

    private TextView tvTimer;
    private Button btnStartPause;
    private Button btnReset;

    private CountDownTimer countDownTimer;
    private boolean isRunning = false;
    private long timeLeftInMillis = WORK_DURATION;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pomodoro);

        Button btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> {
            finish(); // 关闭番茄钟页面，返回主界面
        });

        tvTimer = findViewById(R.id.tvTimer);
        btnStartPause = findViewById(R.id.btnStartPause);
        btnReset = findViewById(R.id.btnReset);

        TextView tvTaskTitle = findViewById(R.id.tvTaskTitle);
        int taskId = getIntent().getIntExtra(EXTRA_TASK_ID, -1);
        String taskTitle = getIntent().getStringExtra(EXTRA_TASK_TITLE);
        if (taskTitle == null) taskTitle = "未指定任务";
        tvTaskTitle.setText("当前任务：" + taskTitle);

        updateCountDownText();

        btnStartPause.setOnClickListener(v -> {
            if (isRunning) {
                pauseTimer();
            } else {
                startTimer();
            }
        });

        btnReset.setOnClickListener(v -> resetTimer());
    }

    private void startTimer() {
        countDownTimer = new CountDownTimer(timeLeftInMillis, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                timeLeftInMillis = millisUntilFinished;
                updateCountDownText();
            }

            @Override
            public void onFinish() {
                isRunning = false;
                btnStartPause.setText("开始");
                // 这里可以加震动、声音提醒等
            }
        }.start();

        isRunning = true;
        btnStartPause.setText("暂停");
    }

    private void pauseTimer() {
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
        isRunning = false;
        btnStartPause.setText("继续");
    }

    private void resetTimer() {
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
        timeLeftInMillis = WORK_DURATION;
        updateCountDownText();
        isRunning = false;
        btnStartPause.setText("开始");
    }

    private void updateCountDownText() {
        int minutes = (int) (timeLeftInMillis / 1000) / 60;
        int seconds = (int) (timeLeftInMillis / 1000) % 60;
        String timeFormatted = String.format("%02d:%02d", minutes, seconds);
        tvTimer.setText(timeFormatted);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
    }
}
