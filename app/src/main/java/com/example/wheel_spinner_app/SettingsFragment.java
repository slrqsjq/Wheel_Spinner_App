package com.example.wheel_spinner_app;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.fragment.app.Fragment;

public class SettingsFragment extends Fragment {

    private EditText[] editTexts = new EditText[12];
    private Button btnSave;
    private SharedPreferences prefs;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_settings, container, false);

        prefs = getActivity().getSharedPreferences("wheel_options", Context.MODE_PRIVATE);

        initViews(view);
        loadSavedOptions();
        setupTextWatchers();
        setupSaveButton();

        return view;
    }

    private void initViews(View view) {
        editTexts[0] = view.findViewById(R.id.edit_option_1);
        editTexts[1] = view.findViewById(R.id.edit_option_2);
        editTexts[2] = view.findViewById(R.id.edit_option_3);
        editTexts[3] = view.findViewById(R.id.edit_option_4);
        editTexts[4] = view.findViewById(R.id.edit_option_5);
        editTexts[5] = view.findViewById(R.id.edit_option_6);
        editTexts[6] = view.findViewById(R.id.edit_option_7);
        editTexts[7] = view.findViewById(R.id.edit_option_8);
        editTexts[8] = view.findViewById(R.id.edit_option_9);
        editTexts[9] = view.findViewById(R.id.edit_option_10);
        editTexts[10] = view.findViewById(R.id.edit_option_11);
        editTexts[11] = view.findViewById(R.id.edit_option_12);

        btnSave = view.findViewById(R.id.btn_save_settings);
    }

    private void loadSavedOptions() {
        for (int i = 0; i < 12; i++) {
            String defaultOption = "选项 " + (i + 1);
            String savedOption = prefs.getString("option_" + (i + 1), defaultOption);
            editTexts[i].setText(savedOption);
        }
    }

    private void setupTextWatchers() {
        for (int i = 0; i < 12; i++) {
            final int index = i;
            editTexts[i].addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {}

                @Override
                public void afterTextChanged(Editable s) {
                    // 自动保存功能：用户停止输入后自动保存
                    editTexts[index].removeCallbacks(autoSaveRunnable);
                    editTexts[index].postDelayed(autoSaveRunnable, 1000); // 1秒后自动保存
                }

                private final Runnable autoSaveRunnable = () -> {
                    String text = editTexts[index].getText().toString().trim();
                    if (text.isEmpty()) {
                        text = "选项 " + (index + 1);
                    }

                    SharedPreferences.Editor editor = prefs.edit();
                    editor.putString("option_" + (index + 1), text);
                    editor.apply();
                };
            });
        }
    }

    private void setupSaveButton() {
        btnSave.setOnClickListener(v -> {
            saveAllOptions();
            Toast.makeText(getActivity(), "设置已保存", Toast.LENGTH_SHORT).show();
        });
    }

    private void saveAllOptions() {
        SharedPreferences.Editor editor = prefs.edit();

        for (int i = 0; i < 12; i++) {
            String text = editTexts[i].getText().toString().trim();
            if (text.isEmpty()) {
                text = "选项 " + (i + 1);
                editTexts[i].setText(text);
            }
            editor.putString("option_" + (i + 1), text);
        }

        editor.apply();
    }
}