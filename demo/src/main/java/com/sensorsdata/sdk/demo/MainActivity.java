/*
 * Created by dengshiwei on 2022/06/28.
 * Copyright 2015－2021 Sensors Data Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.sensorsdata.sdk.demo;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.sensorsdata.analytics.android.sdk.SensorsDataAPI;
import com.sensorsdata.analytics.android.sdk.pantumcontant.ActionType;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initLambdaButton();
        initButton();

        Button button = (Button) findViewById(R.id.btn_make_event);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                JSONObject properties = new JSONObject();
                try {
                    properties.put("custom1", "1");
                    properties.put("custom2", "2");
                    properties.put("custom3", "3");
                    JSONArray array = new JSONArray();
                    array.put("加班申请表.docx");
                    array.put("发票1.png");
                    array.put("出差报销.pdf");
                    properties.put("fileName", array);
                    // properties.put("extra", null);
                } catch (Exception e) {

                }
                 SensorsDataAPI.sharedInstance().pantumTrack("PRACTICE", "", "", 13L, "CLICK", "", "", properties);
//                SensorsDataAPI.sharedInstance().pantumTrack("PRACTICE", "123", "", "CLICK", properties);
            }
        });
    }

    public void onViewClick(View view) {

    }

    private void initLambdaButton() {
        Button button = (Button) findViewById(R.id.lambdaButton);
        button.setOnClickListener(v -> {

        });
    }

    private void initButton() {
        findViewById(R.id.button).setOnClickListener(v -> {

        });

        findViewById(R.id.btn_time_start).setOnClickListener(v -> SensorsDataAPI.sharedInstance().trackTimerStart("PT_TIME_EVENT"));

        findViewById(R.id.btn_time_end).setOnClickListener(v -> {
            long userId = 1L;
            JSONObject extra = new JSONObject();
            try {
                extra.put("from", "1");
            } catch (JSONException e) {
                throw new RuntimeException(e);
            }
            SensorsDataAPI.sharedInstance().pantumTrack(
                    "PT_TIME_EVENT", "", "",
                    userId,
                    ActionType.TIME, "", "",
                    extra
            );
        });
    }
}
