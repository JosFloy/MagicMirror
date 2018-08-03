package com.josfloy.magicmirror.activity;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;

import com.josfloy.magicmirror.R;

/**
 * Created by Jos on 2018/8/2 0002.
 * You can copy it anywhere you want
 */
public class HintActivity extends AppCompatActivity {
    private TextView know;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        //不显示状态栏
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hint);
        know = findViewById(R.id.i_know);
        know.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }
}
