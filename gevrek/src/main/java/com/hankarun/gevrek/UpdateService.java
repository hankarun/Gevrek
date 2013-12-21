package com.hankarun.gevrek;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

public class UpdateService extends Service {

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

        @Override
    public IBinder onBind(Intent ıntent) {
        return null;
    }

    private void notificationToggle(boolean state){
        /*if(state){
            Intent intent = new Intent(this, ToggleClass.class);
            PendingIntent pIntent = PendingIntent.getActivity(this, 0, intent, 0);

            // Build notification
            // Actions are just fake
            Notification noti = new Notification.Builder(this)
                    .setContentTitle(enabled ? "Night Mode (Enabled)":"Night Mode (Disabled)")
                    .setContentText("Click to quick toggle.")
                    .setSmallIcon(R.drawable.ic_launcher)
                    .setContentIntent(pIntent).build();

            noti.flags |= Notification.FLAG_NO_CLEAR;

            notificationManager.notify(0, noti);
        }else{
            notificationManager.cancelAll();
        }*/
    }
}
