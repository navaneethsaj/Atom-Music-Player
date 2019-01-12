package com.blazingapps.asus.atom;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

public class MusicControlBroadCastReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals("play")){
            //Toast.makeText(mainActivity,"PP",Toast.LENGTH_SHORT).show();
        }
    }

}
