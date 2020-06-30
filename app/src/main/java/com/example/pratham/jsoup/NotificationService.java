package com.example.pratham.jsoup;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.Icon;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;


public class NotificationService extends Service {
    ArrayList<String> postId;
    boolean isMatched = false;

    ArrayList<String> name;
    ArrayList<String> ans;

    public NotificationService() {
        postId = new ArrayList<>();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        name = new ArrayList<>();
        ans = new ArrayList<>();
        final Handler handler = new Handler();
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                getPosts();
                handler.postDelayed(this, 1000);
            }
        };
        handler.postDelayed(runnable, 1000);
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    public void getPosts(){
        final SharedPreferences postData = getSharedPreferences("Post", MODE_PRIVATE);
        final SharedPreferences userData = getSharedPreferences("User", MODE_PRIVATE);

        int totalPost = userData.getInt("TotalPost",0);
        postId.clear();
        for(int i=1; i<=totalPost; i++) {
            String id = postData.getString("id" + String.valueOf(i), null);
            if (id != null) {
                postId.add(id);
            }
        }
        postId.size();
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                name.clear();
                ans.clear();
                isMatched = false;
                DataSnapshot notification = dataSnapshot.child("Notification");
                for(DataSnapshot id: notification.getChildren()){
                    for(DataSnapshot notifications : id.getChildren()){
                        for(int i=0; i<postId.size(); i++){
                            if(id.getKey().matches(postId.get(i))){
                                setvalue(notifications.child("name").getValue().toString(),notifications.child("ans").getValue().toString());
                                break;
                            }
                        }
                    }
                }
                if(name.size()!=0)
                    loadNotifications(name,ans);
                for(DataSnapshot id: notification.getChildren()){
                    for(DataSnapshot notifications : id.getChildren()){
                        for(int i=0; i<postId.size(); i++){
                            if(id.getKey().matches(postId.get(i))){
                                notifications.getRef().removeValue();
                                break;
                            }
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(getApplicationContext(), databaseError.getDetails(), Toast.LENGTH_SHORT).show();
            }
        });

    }

    public void setvalue(String s1,String s2){
        name.add(s1);
        ans.add(s2);
    }

    public void loadNotifications(ArrayList<String> userName,ArrayList<String> url){
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O){
            int notifyID = 1;
            String CHANNEL_ID = "my_channel_01";
            CharSequence name = "Channel 1";
            NotificationChannel mChannel = new NotificationChannel(CHANNEL_ID, name, NotificationManager.IMPORTANCE_HIGH);

            Notification.InboxStyle style = new Notification.InboxStyle();
            for(int i=0; i<userName.size(); i++){
                style.addLine(userName.get(i).concat(" : ").concat(url.get(i)));
            }
            style.setSummaryText("New Comments");
            style.setBigContentTitle("Have a look at them");

            Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.logo);
            Notification noti = new Notification.Builder(NotificationService.this,CHANNEL_ID)
                    .setSmallIcon(getCircleBitmap(bitmap))
                    .setChannelId(CHANNEL_ID)
                    .setContentTitle("Title")
                    .setContentText("Text")
                    .setStyle( style)
                    .setOnlyAlertOnce(true)
                    .setPriority(Notification.PRIORITY_HIGH)
                    .build();

            noti.flags |= Notification.FLAG_AUTO_CANCEL;
            NotificationManager mNotificationManager =
                    (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            mNotificationManager.createNotificationChannel(mChannel);
            mNotificationManager.notify(notifyID , noti);
        } else{
            String s = null;
            for(int i=0; i<userName.size(); i++){
                s = userName.get(i).concat(" : ").concat(url.get(i));
            }
            if(s==null)
                s="No value";
            NotificationCompat.Builder mBuilder =   new NotificationCompat.Builder(NotificationService.this)
                    .setSmallIcon(R.drawable.main_logo)
                    .setContentTitle("Comments")
                    .setContentText(s)
                    .setOnlyAlertOnce(true)
                    .setPriority(Notification.PRIORITY_HIGH);
            NotificationManager mNotificationManager =
                    (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            mNotificationManager.notify(0, mBuilder.build());
        }
    }

    private Icon getCircleBitmap(Bitmap bitmap) {
        final Bitmap output = Bitmap.createBitmap(bitmap.getWidth(),
                bitmap.getHeight(), Bitmap.Config.ARGB_8888);
        final Canvas canvas = new Canvas(output);

        final int color = Color.RED;
        final Paint paint = new Paint();
        final Rect rect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());
        final RectF rectF = new RectF(rect);

        paint.setAntiAlias(true);
        canvas.drawARGB(0, 0, 0, 0);
        paint.setColor(color);
        canvas.drawOval(rectF, paint);

        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        canvas.drawBitmap(bitmap, rect, rect, paint);

        Icon icon = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            icon = Icon.createWithBitmap(output);
        }

        return icon;
    }

}
