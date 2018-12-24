package com.example.pc_asus.nguoimu.CheckOpenApp;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class CheckPressVolumReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {

        Log.e("abc","zô Receiver.....");
        Intent i = new Intent();
        i.setClassName(context.getPackageName(), CheckOpenAppService.class.getName());
        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startService(i);

    }
}