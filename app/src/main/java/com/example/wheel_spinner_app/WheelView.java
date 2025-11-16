package com.example.wheel_spinner_app;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import java.util.ArrayList;
import java.util.List;

public class WheelView extends View {

    private Paint paint;
    private RectF rectF;
    private List<String> options;
    private List<Integer> colors;

    // 自定义旋转属性
    private float wheelRotation = 0f;

    // 12个漂亮的颜色
    private int[] wheelColors = {
            Color.parseColor("#FF6B9D"), Color.parseColor("#C44569"),
            Color.parseColor("#4ECDC4"), Color.parseColor("#45B7D1"),
            Color.parseColor("#96CEB4"), Color.parseColor("#FECA57"),
            Color.parseColor("#FD79A8"), Color.parseColor("#E17055"),
            Color.parseColor("#74B9FF"), Color.parseColor("#A29BFE"),
            Color.parseColor("#6C5CE7"), Color.parseColor("#FF7675")
    };

    public WheelView(Context context) {
        super(context);
        init();
    }

    public WheelView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public WheelView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        rectF = new RectF();
        options = new ArrayList<>();
        colors = new ArrayList<>();
        for (int i = 1; i <= 12; i++) {
            options.add("选项" + i);
            colors.add(wheelColors[i - 1]);
        }
    }

    public void setWheelRotation(float rotation) {
        this.wheelRotation = rotation;
        invalidate();
    }

    public float getWheelRotation() {
        return wheelRotation;
    }

    public void setOptions(List<String> newOptions) {
        if (newOptions == null) return;
        this.options.clear();
        this.options.addAll(newOptions);
        while (this.options.size() < 12) {
            this.options.add("选项" + (this.options.size() + 1));
        }
        invalidate();
    }

    /**
     * 获取指针指向的扇区数字（1-12）
     * 指针在12点方向，我们需要知道这个位置对应哪个数字
     */
    public int getSelectedNumber() {
        // 每个扇区30度
        float sectorAngle = 30f;

        // 获取当前旋转角度，标准化到0-360度
        float currentAngle = ((wheelRotation % 360f) + 360f) % 360f;

        // 指针在12点方向（0度位置）
        // 由于我们的扇区是从12点开始按顺时针排列的
        // 第1个扇区在12点位置（-15度到15度）
        // 我们需要计算当前角度对应哪个扇区

        // 加15度偏移，让扇区边界对齐
        float adjustedAngle = (currentAngle + 15f) % 360f;

        // 计算扇区索引（0-11）
        int sectorIndex = (int)(adjustedAngle / sectorAngle) % 12;

        // 由于转盘是逆时针旋转的，我们需要反向计算
        // 指针不动，转盘转动，所以实际指向的是相反方向的扇区
        int selectedNumber = (12 - sectorIndex) % 12;
        if (selectedNumber == 0) selectedNumber = 12;

        Log.d("WheelView", "currentAngle: " + currentAngle +
                ", adjustedAngle: " + adjustedAngle +
                ", sectorIndex: " + sectorIndex +
                ", selectedNumber: " + selectedNumber);

        return selectedNumber;
    }

    /**
     * 获取选中数字对应的选项文字
     */
    public String getSelectedOptionText() {
        int number = getSelectedNumber();
        int index = number - 1; // 转换为数组索引（0-11）

        if (index >= 0 && index < options.size()) {
            String selectedText = options.get(index);
            Log.d("WheelView", "Selected number: " + number + ", text: " + selectedText);
            return selectedText;
        }

        Log.w("WheelView", "Invalid number: " + number);
        return "未知选项";
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        int w = getWidth(), h = getHeight();
        int radius = Math.min(w, h) / 2 - 20;
        int cx = w / 2, cy = h / 2;
        rectF.set(cx - radius, cy - radius, cx + radius, cy + radius);

        canvas.save();
        canvas.rotate(wheelRotation, cx, cy);

        float sweepAngle = 30f; // 每个扇区30度

        // 从12点方向开始绘制，第1个扇区在最上方
        for (int i = 0; i < 12; i++) {
            // 从-90度开始（12点方向），按顺时针绘制
            float startAngle = -90f + i * sweepAngle;

            // 绘制扇区背景
            paint.setColor(colors.get(i));
            paint.setStyle(Paint.Style.FILL);
            canvas.drawArc(rectF, startAngle, sweepAngle, true, paint);

            // 绘制扇区边框
            paint.setColor(Color.WHITE);
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth(3f);
            canvas.drawArc(rectF, startAngle, sweepAngle, true, paint);

            // 计算文字位置（扇区中心）
            float textAngle = startAngle + sweepAngle / 2f;
            float textRadius = radius * 0.7f;
            float tx = (float)(cx + textRadius * Math.cos(Math.toRadians(textAngle)));
            float ty = (float)(cy + textRadius * Math.sin(Math.toRadians(textAngle)) + 8);

            // 绘制选项标号文字（只显示"选项1"、"选项2"等）
            paint.setColor(Color.WHITE);
            paint.setStyle(Paint.Style.FILL);
            paint.setTextSize(20f); // 稍微增大字体，因为文字更短了
            paint.setTextAlign(Paint.Align.CENTER);
            String displayText = "选项" + (i + 1);
            canvas.drawText(displayText, tx, ty, paint);

            // 绘制数字标签
            paint.setTextSize(14f);
            paint.setColor(Color.parseColor("#80FFFFFF"));
            float numberRadius = radius * 0.85f;
            float nx = (float)(cx + numberRadius * Math.cos(Math.toRadians(textAngle)));
            float ny = (float)(cy + numberRadius * Math.sin(Math.toRadians(textAngle)) + 6);
            canvas.drawText(String.valueOf(i + 1), nx, ny, paint);
        }

        // 绘制中心圆
        paint.setColor(Color.parseColor("#4A90E2"));
        paint.setStyle(Paint.Style.FILL);
        canvas.drawCircle(cx, cy, 40f, paint);
        paint.setColor(Color.WHITE);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(4f);
        canvas.drawCircle(cx, cy, 40f, paint);

        canvas.restore();

        // 在转盘上方绘制指针指示（调试用，可以看到指针指向的数字）
        paint.setColor(Color.RED);
        paint.setStyle(Paint.Style.FILL);
        paint.setTextSize(20f);
        paint.setTextAlign(Paint.Align.CENTER);
        canvas.drawText("↓", cx, cy - radius - 30, paint);
    }
}