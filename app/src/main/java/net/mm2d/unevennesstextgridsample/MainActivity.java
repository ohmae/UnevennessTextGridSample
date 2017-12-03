/*
 * Copyright (c) 2017 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.unevennesstextgridsample;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.util.Pair;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author <a href="mailto:ryo@mm2d.net">大前良介 (OHMAE Ryosuke)</a>
 */
public class MainActivity extends AppCompatActivity {
    private volatile boolean mInitialized = false;
    private final List<Pair<String, String>> mDictionary = new ArrayList<>();

    @Override
    protected void onCreate(@Nullable final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        final UnevennessTextGridView view = findViewById(R.id.view);
        view.setOnTextClickListener(text -> Toast.makeText(this, text, Toast.LENGTH_LONG).show());
        final EditText editText = findViewById(R.id.edit);
        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(final CharSequence s, final int start, final int count, final int after) {
            }

            @Override
            public void onTextChanged(final CharSequence s, final int start, final int before, final int count) {
                view.setList(createMatchList(editText.getText().toString()));
            }

            @Override
            public void afterTextChanged(final Editable s) {
            }
        });
        final ProgressBar progressBar = findViewById(R.id.progressBar);
        new Thread(() -> {
            createDictionary();
            runOnUiThread(() -> progressBar.setVisibility(View.GONE));
        }).start();
    }

    private List<String> createMatchList(@NonNull String string) {
        if (!mInitialized || TextUtils.isEmpty(string)) {
            return Collections.emptyList();
        }
        if (string.length() >= 2) {
            char last = string.charAt(string.length() - 1);
            if (last >= 0xFF00 && last <= 0xFFF0) {
                string = string.substring(0, string.length() - 1);
            }
        }
        boolean found = false;
        final List<String> result = new ArrayList<>();
        for (Pair<String, String> pair : mDictionary) {
            if (pair.first.startsWith(string)) {
                found = true;
                result.add(pair.second);
            } else if (found) {
                break;
            }
        }
        return result;
    }

    private void createDictionary() {
        BufferedReader br = null;
        try {
            br = new BufferedReader(new InputStreamReader(getAssets().open("dic.txt")));
            String line;
            while ((line = br.readLine()) != null) {
                String[] words = line.split("\t");
                if (words.length < 2) {
                    continue;
                }
                mDictionary.add(new Pair<>(words[0], words[1]));
            }
            br.close();
        } catch (final IOException e) {
            e.printStackTrace();
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (final IOException ignored) {
                }
            }
            mInitialized = true;
        }
    }
}
