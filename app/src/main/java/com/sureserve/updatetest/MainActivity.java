package com.sureserve.updatetest;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.sureserve.update.UpdateUtil;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initMain();
    }


    void initMain() {
        Button button = (Button) findViewById(R.id.btn_update);


        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkUpdate();
            }
        });
    }


    void checkUpdate() {
        UpdateUtil.getInstance().setContext(this)
                .checkUpdate(false);
    }

}
