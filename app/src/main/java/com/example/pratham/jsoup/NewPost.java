package com.example.pratham.jsoup;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Handler;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Random;
import java.util.UUID;

public class NewPost extends AppCompatActivity {

    FloatingActionButton share,back;
    DatabaseReference reference;
    EditText editText;
    FirebaseAuth auth;
    TextView userId;
    ImageView userPic;
    int totalPost;
    View view;
    int reputation;
    String pic,name;
    public static String TIME;
    String[] colors = {"#fa7268","#a8c55c","#0d243f","#ec8a04","#a8c55c","#e41749","#6fc2d0","#ff8a5c","#33313b","#8e2e6a"};
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_post);


        final TimeAsyncTask asyncTask = new TimeAsyncTask(getApplicationContext());
        auth = FirebaseAuth.getInstance();
        editText = findViewById(R.id.editText);
        userId = findViewById(R.id.userId);
        back = findViewById(R.id.back);
        share = findViewById(R.id.share);
        view = findViewById(R.id.view);
        reference = FirebaseDatabase.getInstance().getReference();
        userPic = findViewById(R.id.userPic);
        Typeface tf = Typeface.createFromAsset(getAssets(), "fonts/futura.ttf");

        final Random random = new Random();
        int randColor = random.nextInt(10);
        view.setBackgroundColor(Color.parseColor(colors[randColor]));
        editText.setTextColor(Color.parseColor(colors[randColor]));
        userId.setTextColor(Color.parseColor(colors[randColor]));
        final SharedPreferences postData = getSharedPreferences("Post", MODE_PRIVATE);
        final SharedPreferences userData = getSharedPreferences("User", MODE_PRIVATE);
        totalPost = userData.getInt("TotalPost", 0);
        pic = userData.getString("ProfilePic", "https://firebasestorage.googleapis.com/v0/b/birthdayexchange.appspot.com/o/user.png?alt=media&token=92b9269c-4136-458a-b465-d8229b68c744");
        Glide.with(getApplicationContext()).load(pic).into(userPic);
        name = userData.getString("Name", "Anonymous");
        reputation = userData.getInt("reputation", 0);
        userId.setText(name);
        userId.setTypeface(tf);
        final SharedPreferences.Editor editor = postData.edit();
        final SharedPreferences.Editor editor2 = userData.edit();
        share.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(editText.length() > 0 && !editText.getText().toString().isEmpty()){
                    Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        v.vibrate(VibrationEffect.createOneShot(50, 10));
                    } else {
                        v.vibrate(50);
                    }
                    asyncTask.execute();

                    final Handler handler = new Handler();
                    Runnable runnable = new Runnable() {
                        @Override
                        public void run() {
                            if(asyncTask.getStatus()== AsyncTask.Status.FINISHED){
                                totalPost++;
                                int rand = random.nextInt(3);
                                reputation += Math.pow(2.67, rand);
                                String random = UUID.randomUUID().toString();
                                DatabaseReference post = reference.child("post").child(random);
                                post.child("Question").setValue(editText.getText().toString());
                                post.child("Time").setValue(TIME);
                                post.child("Answer").setValue(0);
                                post.child("ProfilePic").setValue(pic);
                                post.child("Name").setValue(name);
                                editor.putString("id"+String.valueOf(totalPost),random);
                                editor2.putInt("TotalPost", totalPost);
                                editor2.putInt("reputation", reputation);
                                editor.apply();
                                editor2.apply();
                                handler.removeCallbacks(this);
                                finish();
                            }else
                                handler.postDelayed(this, 500);
                        }
                    };
                    handler.postDelayed(runnable,500);
                }else{
                    Toast.makeText(NewPost.this, "Ask a Question to continue", Toast.LENGTH_SHORT).show();
                }
            }
        });
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

    }
}
