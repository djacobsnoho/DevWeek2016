package com.magnetmessage;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.magnet.max.android.Max;
import com.magnet.max.android.config.MaxAndroidConfig;

/**
 * Created by RITESH on 2/13/2016.
 */
public class Message extends AppCompatActivity{

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate();
        setContentView(R.layout.activity_main);
        Max.init(this, new MaxAndroidConfig.Builder()
                .clientId("445742ce-0ce8-42f8-94fb-c904168a3017")
                .clientSecret("DWiKYwj2L1TX4QUpo-i-RwTv8gpEKFcjUtal0tgGBco")
                .build());

    }
}
}
