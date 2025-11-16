package com.example.wheel_spinner_app;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class SpinFragment extends Fragment {

    private WheelView wheelView;
    private ImageButton btnStartSpin;
    private TextView resultText;
    private Button btnComplete;
    private LinearLayout resultContainer;

    // 新增：弹窗覆盖层相关视图
    private FrameLayout overlayContainer;
    private LinearLayout dialogResultContainer;
    private TextView dialogTitle;
    private TextView dialogResult;
    private Button btnDialogComplete;

    private boolean isSpinning = false;
    private List<String> wheelOptions;
    private List<String> wheelInstructions;
    private Random random;
    private Handler mainHandler;

    // 标志位：使用哪种显示方式（内嵌结果框 或 弹窗覆盖层）
    private boolean useOverlayMode = true;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_spin, container, false);

        initViews(view);
        setupListeners();
        loadWheelOptions();

        random = new Random();
        mainHandler = new Handler(Looper.getMainLooper());

        return view;
    }

    private void initViews(View view) {
        wheelView = view.findViewById(R.id.wheel_view);
        btnStartSpin = view.findViewById(R.id.btn_start_spin);
        resultText = view.findViewById(R.id.result_text);
        btnComplete = view.findViewById(R.id.btn_complete);
        resultContainer = view.findViewById(R.id.result_container);

        // 新增：初始化弹窗覆盖层视图
        overlayContainer = view.findViewById(R.id.overlay_container);
        dialogResultContainer = view.findViewById(R.id.dialog_result_container);
        dialogTitle = view.findViewById(R.id.dialog_title);
        dialogResult = view.findViewById(R.id.dialog_result);
        btnDialogComplete = view.findViewById(R.id.btn_dialog_complete);

        Log.d("SpinFragment", "initViews: wheelView=" + wheelView
                + ", btnStartSpin=" + btnStartSpin
                + ", resultContainer=" + resultContainer
                + ", overlayContainer=" + overlayContainer);
    }

    private void setupListeners() {
        btnStartSpin.setOnClickListener(v -> {
            if (!isSpinning && wheelOptions != null && !wheelOptions.isEmpty()) {
                startSpin();
            } else if (wheelOptions == null || wheelOptions.isEmpty()) {
                Toast.makeText(getContext(), "请先在设置页面添加选项", Toast.LENGTH_SHORT).show();
            }
        });

        // 内嵌结果框的完成按钮
        if (btnComplete != null) {
            btnComplete.setOnClickListener(v -> {
                Log.d("SpinFragment", "Inline complete button clicked");
                hideResult();
            });
        }

        // 弹窗覆盖层的完成按钮 - 这是唯一的关闭方式
        if (btnDialogComplete != null) {
            btnDialogComplete.setOnClickListener(v -> {
                Log.d("SpinFragment", "Dialog complete button clicked");
                hideResult();
            });
        }

        // 覆盖层背景点击处理 - 阻止事件但不关闭弹窗
        if (overlayContainer != null) {
            overlayContainer.setOnClickListener(v -> {
                Log.d("SpinFragment", "Overlay background touched - ignoring");
                // 什么也不做，只是阻止点击事件传递，防止意外关闭
            });
        }

        // 点击结果对话框内容区域也不关闭，防止误触
        if (dialogResultContainer != null) {
            dialogResultContainer.setOnClickListener(v -> {
                Log.d("SpinFragment", "Dialog content touched - ignoring");
                // 什么也不做，阻止点击事件传递到覆盖层
            });
        }

        // 添加返回键监听（可选）- 如果用户按返回键也可以关闭
        /*
        if (overlayContainer != null) {
            overlayContainer.setFocusableInTouchMode(true);
            overlayContainer.requestFocus();
            overlayContainer.setOnKeyListener((v, keyCode, event) -> {
                if (keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_UP) {
                    hideResult();
                    return true;
                }
                return false;
            });
        }
        */
    }

    private void loadWheelOptions() {
        wheelOptions = new ArrayList<>();
        wheelInstructions = new ArrayList<>();

        SharedPreferences prefs = getActivity().getSharedPreferences("wheel_options", Context.MODE_PRIVATE);

        for (int i = 1; i <= 12; i++) {
            String optionKey = "option_" + i;
            String defaultOption = "选项 " + i;
            String option = prefs.getString(optionKey, defaultOption);
            wheelOptions.add(option.trim().isEmpty() ? defaultOption : option);

            String instructionKey = "instruction_" + i;
            String defaultInstruction = "请完成任务：" + (option.trim().isEmpty() ? defaultOption : option) + "！";
            String instruction = prefs.getString(instructionKey, defaultInstruction);
            wheelInstructions.add(instruction.trim().isEmpty() ? defaultInstruction : instruction);
        }

        Log.d("SpinFragment", "Loaded " + wheelOptions.size() + " options and " + wheelInstructions.size() + " instructions");

        if (wheelView != null) {
            wheelView.setOptions(wheelOptions);
        }
    }

    private void startSpin() {
        if (isSpinning) return;

        isSpinning = true;
        hideResult(); // 确保隐藏之前的结果

        Toast.makeText(getContext(), "🎯 正在旋转中...", Toast.LENGTH_SHORT).show();

        btnStartSpin.setEnabled(false);
        btnStartSpin.setAlpha(0.6f);
        btnStartSpin.setScaleX(0.95f);
        btnStartSpin.setScaleY(0.95f);

        int targetNumber = random.nextInt(12) + 1;
        float targetAngle = -(targetNumber - 1) * 30f;
        int baseRotations = 5 + random.nextInt(6);
        float baseAngle = baseRotations * 360f;

        float currentAngle = wheelView.getWheelRotation();
        float angleDifference = targetAngle - (currentAngle % 360f);
        if (angleDifference <= 0) {
            angleDifference += 360f;
        }

        float finalRotation = currentAngle + baseAngle + angleDifference;

        Log.d("SpinFragment", "Target number: " + targetNumber
                + ", targetAngle: " + targetAngle
                + ", currentAngle: " + currentAngle
                + ", finalRotation: " + finalRotation);

        ObjectAnimator rotateAnimator = ObjectAnimator.ofFloat(
                wheelView,
                "wheelRotation",
                currentAngle,
                finalRotation
        );

        long duration = 3000 + random.nextInt(2000);
        rotateAnimator.setDuration(duration);
        rotateAnimator.setInterpolator(new DecelerateInterpolator(2.5f));

        rotateAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                mainHandler.postDelayed(() -> {
                    isSpinning = false;
                    showResult();
                }, 200); // 稍微延迟一点显示结果
            }
        });

        rotateAnimator.start();
    }

    private void enableStartButton() {
        if (btnStartSpin != null) {
            btnStartSpin.setEnabled(true);
            btnStartSpin.animate()
                    .alpha(1.0f)
                    .scaleX(1.0f)
                    .scaleY(1.0f)
                    .setDuration(200)
                    .start();
        }
    }

    private void showResult() {
        Log.d("SpinFragment", "showResult() called, useOverlayMode=" + useOverlayMode);

        if (wheelView == null || wheelOptions == null || wheelOptions.isEmpty()) {
            Log.e("SpinFragment", "wheelView or wheelOptions is null/empty");
            enableStartButton();
            return;
        }

        try {
            int selectedNumber = wheelView.getSelectedNumber();
            Log.d("SpinFragment", "Selected number: " + selectedNumber);

            int optionIndex = selectedNumber - 1;
            if (optionIndex >= 0 && optionIndex < wheelOptions.size()
                    && optionIndex < wheelInstructions.size()) {

                String selectedOption = wheelOptions.get(optionIndex);
                String selectedInstruction = wheelInstructions.get(optionIndex);

                // 修改结果消息格式：去掉重复的【】内容，直接显示指令
                String resultMessage = String.format(
                        "🎉 抽中数字 %d 🎉\n\n%s",
                        selectedNumber, selectedInstruction);

                // 优先使用弹窗覆盖层显示结果
                if (useOverlayMode && showOverlayResult(resultMessage)) {
                    Log.d("SpinFragment", "Using overlay mode for result display");
                } else if (showInlineResult(resultMessage)) {
                    Log.d("SpinFragment", "Using inline mode for result display");
                } else {
                    // 如果两种方式都失败，使用Toast显示结果
                    Toast.makeText(getContext(),
                            String.format("🎊 恭喜！抽中数字 %d", selectedNumber),
                            Toast.LENGTH_LONG).show();
                    enableStartButton();
                }

            } else {
                Log.e("SpinFragment", "Array index out of bounds");
                Toast.makeText(getContext(), "获取结果时出错", Toast.LENGTH_SHORT).show();
                enableStartButton();
            }

        } catch (Exception e) {
            Log.e("SpinFragment", "Error showing result", e);
            Toast.makeText(getContext(), "显示结果时出错", Toast.LENGTH_SHORT).show();
            enableStartButton();
        }
    }

    // 使用弹窗覆盖层显示结果
    private boolean showOverlayResult(String resultMessage) {
        if (overlayContainer == null || dialogResult == null) {
            Log.w("SpinFragment", "Overlay components not available");
            return false;
        }

        try {
            dialogResult.setText(resultMessage);

            overlayContainer.setVisibility(View.VISIBLE);
            overlayContainer.setAlpha(0f);
            dialogResultContainer.setScaleX(0.7f);
            dialogResultContainer.setScaleY(0.7f);

            overlayContainer.animate()
                    .alpha(1f)
                    .setDuration(300)
                    .start();

            dialogResultContainer.animate()
                    .scaleX(1f)
                    .scaleY(1f)
                    .setDuration(400)
                    .setInterpolator(new DecelerateInterpolator(1.5f))
                    .start();

            Log.d("SpinFragment", "Overlay result displayed successfully");
            return true;

        } catch (Exception e) {
            Log.e("SpinFragment", "Error showing overlay result", e);
            return false;
        }
    }

    // 使用内嵌结果框显示结果
    private boolean showInlineResult(String resultMessage) {
        if (resultContainer == null || resultText == null) {
            Log.w("SpinFragment", "Inline components not available");
            return false;
        }

        try {
            resultText.setText(resultMessage);

            resultContainer.setVisibility(View.VISIBLE);
            resultContainer.setAlpha(0f);
            resultContainer.setScaleX(0.8f);
            resultContainer.setScaleY(0.8f);

            resultContainer.animate()
                    .alpha(1f)
                    .scaleX(1f)
                    .scaleY(1f)
                    .setDuration(400)
                    .setInterpolator(new DecelerateInterpolator(1.5f))
                    .start();

            Log.d("SpinFragment", "Inline result displayed successfully");
            return true;

        } catch (Exception e) {
            Log.e("SpinFragment", "Error showing inline result", e);
            return false;
        }
    }

    private void hideResult() {
        // 隐藏弹窗覆盖层
        if (overlayContainer != null && overlayContainer.getVisibility() == View.VISIBLE) {
            overlayContainer.animate()
                    .alpha(0f)
                    .setDuration(250)
                    .withEndAction(() -> {
                        overlayContainer.setVisibility(View.GONE);
                        enableStartButton();
                    })
                    .start();
            return;
        }

        // 隐藏内嵌结果框
        if (resultContainer != null && resultContainer.getVisibility() == View.VISIBLE) {
            resultContainer.animate()
                    .alpha(0f)
                    .scaleX(0.9f)
                    .scaleY(0.9f)
                    .setDuration(250)
                    .withEndAction(() -> {
                        resultContainer.setVisibility(View.GONE);
                        enableStartButton();
                    })
                    .start();
            return;
        }

        // 如果没有显示的结果框，直接启用按钮
        enableStartButton();
    }

    @Override
    public void onResume() {
        super.onResume();
        loadWheelOptions();

        // 确保没有显示结果时启用按钮
        if (!isSpinning &&
                (overlayContainer == null || overlayContainer.getVisibility() != View.VISIBLE) &&
                (resultContainer == null || resultContainer.getVisibility() != View.VISIBLE)) {
            enableStartButton();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (mainHandler != null) {
            mainHandler.removeCallbacksAndMessages(null);
        }
    }
}