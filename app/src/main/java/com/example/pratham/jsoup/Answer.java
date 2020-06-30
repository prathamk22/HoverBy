package com.example.pratham.jsoup;

import android.app.Dialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Handler;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SnapHelper;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.text.TextUtils;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.pratham.jsoup.Packages.Ans;
import com.example.pratham.jsoup.Packages.NotificationClass;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.UUID;

import de.hdodenhof.circleimageview.CircleImageView;

public class Answer extends AppCompatActivity {

    CardView cardView;
    String ques, color, id, user, pic,time;
    TextView textView, userID,date;
    CircleImageView profilepic, userPic, circle;
    View view;
    ImageView anss,commentAdded,option;
    DatabaseReference reference;
    EditText editText;
    RecyclerView recyclerView;
    ArrayList<Ans> answer;
    ArrayList<String> streamId;
    AnswerAdapter answerAdapter;
    RelativeLayout relative, comments,animationLayout;
    boolean comment = false,show = false;
    SharedPreferences userData;
    SwipeHeler swipeHeler;
    SharedPreferences answerData;
    SharedPreferences.Editor editor;
    ProgressBar progress;
    public static boolean isAnswer = false;
    public static String currentTime;
    private String[] options = {"Copy Question","Report"};
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_answer);
        isAnswer = true;
        cardView = findViewById(R.id.cardView);
        textView = findViewById(R.id.textView);
        progress = findViewById(R.id.progress);
        date = findViewById(R.id.date);
        commentAdded = findViewById(R.id.commentAdded);
        option = findViewById(R.id.option);
        userData = getSharedPreferences("User", MODE_PRIVATE);
        answerData = getSharedPreferences("answer", MODE_PRIVATE);
        editor = answerData.edit();
        streamId = new ArrayList<>();
        animationLayout = findViewById(R.id.animationLayout);
        relative = findViewById(R.id.relative);
        comments = findViewById(R.id.comments);
        profilepic = findViewById(R.id.profilepic);
        userID = findViewById(R.id.userID);
        userPic = findViewById(R.id.userPic);
        circle = findViewById(R.id.circle);
        view = findViewById(R.id.view);
        editText = findViewById(R.id.editText);
        recyclerView = findViewById(R.id.recyclerview);
        anss = findViewById(R.id.ans);
        answer = new ArrayList<>();
        reference = FirebaseDatabase.getInstance().getReference();
        ques = getIntent().getStringExtra("ques");
        color = getIntent().getStringExtra("color");
        id = getIntent().getStringExtra("id");
        user = getIntent().getStringExtra("user");
        time = getIntent().getStringExtra("time");
        pic = getIntent().getStringExtra( "pic");
        show = getIntent().getBooleanExtra("comment",false);
        getAnswer();
        answerAdapter = new AnswerAdapter(getApplicationContext(), answer,streamId);
        answerAdapter.isOwnComment(false);
        Typeface tf = Typeface.createFromAsset(getAssets(), "fonts/futura.ttf");

        if(show)
            commentAdded.setVisibility(View.VISIBLE);
        else
            commentAdded.setVisibility(View.INVISIBLE);

        date.setText(time);
        userID.setTypeface(tf);
        recyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
        recyclerView.setAdapter(answerAdapter);
        textView.setTextColor(Color.parseColor(color));
        view.setBackgroundColor(Color.parseColor(color));
        textView.setText(ques);
        userID.setText(user);
        getAnswerData();
        Glide.with(getApplicationContext()).load(pic).into(profilepic);
        anss.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                anss.setVisibility(View.INVISIBLE);
                progress.setVisibility(View.VISIBLE);
                if(!TextUtils.isEmpty(editText.getText())){
                    newAnswer(editText.getText().toString());
                    editText.setText("");
                    View view = Answer.this.getCurrentFocus();
                    if (view != null) {
                        InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
                    }
                }else{
                    progress.setVisibility(View.INVISIBLE);
                    anss.setVisibility(View.VISIBLE);
                    Toast.makeText(Answer.this, "Enter text to add a comment", Toast.LENGTH_SHORT).show();
                }

            }
        });
        swipeHeler = new SwipeHeler(answerAdapter,getApplicationContext(),streamId);
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(swipeHeler);
        itemTouchHelper.attachToRecyclerView(recyclerView);
        relative.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                comments.setVisibility(View.VISIBLE);
                comments.setAlpha(0f);
                comments.animate().alphaBy(1f).setDuration(300).start();
                relative.setVisibility(View.GONE);
                comment = true;
            }
        });

        option.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                final Dialog dialog = new Dialog(Answer.this);
                dialog.setContentView(R.layout.dialog_layout);

                ListView listView = dialog.findViewById(R.id.listview);

                listView.setAdapter(new ArrayAdapter<String>(Answer.this,android.R.layout.simple_list_item_1,options));

                listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long i) {
                        switch (position){
                            case 0:
                                ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                                ClipData clip = ClipData.newPlainText("ques", ques);
                                clipboard.setPrimaryClip(clip);
                                dialog.dismiss();
                                break;
                            case 1:
                                Intent intent = new Intent(getApplicationContext(), Report.class);
                                intent.putExtra("id",id);
                                startActivity(intent);
                                dialog.dismiss();
                                break;
                        }
                    }
                });
                dialog.show();
            }
        });

        animationLayout.setVisibility(View.VISIBLE);
        Animation animation = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.ans_animation);
        animationLayout.startAnimation(animation);

    }

    @Override
    public void onBackPressed() {
        if (comment) {
            comments.animate().alphaBy(0f).setDuration(300).start();
            comments.setVisibility(View.GONE);
            relative.setVisibility(View.VISIBLE);
            comment = false;
        } else{
            super.onBackPressed();
            isAnswer = false;
        }

    }

    public void newAnswer(final String ans) {
        Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            v.vibrate(VibrationEffect.createOneShot(50, 10));
        } else {
            v.vibrate(50);
        }
        final TimeAsyncTask asyncTask = new TimeAsyncTask(getApplicationContext());
        asyncTask.execute();

        final Handler handler = new Handler();
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                if(asyncTask.getStatus()==TimeAsyncTask.Status.FINISHED){
                    int totalAns = answerData.getInt("total",0);
                    totalAns++;
                    DatabaseReference post = reference.child("post").child(id).child("Answer");
                    String rand = UUID.randomUUID().toString();
                    editor.putString(String.valueOf(totalAns), rand);
                    editor.putInt("total",totalAns);
                    DatabaseReference userss = post.child(userData.getString("Name", "Anonymous")).child(rand);
                    userss.child("Time").setValue(currentTime);
                    userss.child("Ans").setValue(ans);
                    userss.child("upvote").setValue("0");
                    userss.child("downvote").setValue("0");
                    pic = userData.getString("ProfilePic", "https://firebasestorage.googleapis.com/v0/b/birthdayexchange.appspot.com/o/user.png?alt=media&token=92b9269c-4136-458a-b465-d8229b68c744");
                    editor.apply();
                    userss.child("ProfilePic").setValue(pic).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            DatabaseReference notification = reference.child("Notification");
                            DatabaseReference notificationid = notification.child(id).child(UUID.randomUUID().toString());
                            NotificationClass notification1 = new NotificationClass();
                            notification1.setTime(currentTime);
                            notification1.setAns(ans);
                            notification1.setDownvote("0");
                            notification1.setUpvote("0");
                            notification1.setName(userData.getString("Name", "Anonymous"));
                            notification1.setProfilePic(pic);
                            notificationid.setValue(notification1);
                            progress.setVisibility(View.INVISIBLE);
                            anss.setVisibility(View.VISIBLE);
                            onBackPressed();
                        }
                    });
                }else
                    handler.postDelayed(this, 500);
            }
        };

        handler.post(runnable);
    }

    public void getAnswerData(){
        streamId.clear();
        int totalAns = answerData.getInt("streamcount",0);
        for(int i=1; i<=totalAns; i++)
            streamId.add(answerData.getString("Stream"+String.valueOf(i), ""));
    }

    public void getAnswer() {
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                answer.clear();
                pic = userData.getString("ProfilePic", "https://firebasestorage.googleapis.com/v0/b/birthdayexchange.appspot.com/o/user.png?alt=media&token=92b9269c-4136-458a-b465-d8229b68c744");
                Glide.with(getApplicationContext()).load(pic).into(userPic);
                Glide.with(getApplicationContext()).load(pic).into(circle);
                DataSnapshot user = dataSnapshot.child("post").child(id).child("Answer");
                for (DataSnapshot userId : user.getChildren()) {
                    for(DataSnapshot userChild: userId.getChildren()){
                        DataSnapshot ans = userChild.child("Ans");
                        DataSnapshot time = userChild.child("Time");
                        DataSnapshot upvote = userChild.child("upvote");
                        DataSnapshot downvote = userChild.child("downvote");
                        DataSnapshot userpic = userChild.child("ProfilePic");
                        if (time.getValue() != null &&
                                ans.getValue() != null &&
                                upvote.getValue() != null &&
                                downvote.getValue() != null &&
                                userpic.getValue() != null
                        ) {
                            Ans anss = new Ans(time.getValue().toString(),
                                    ans.getValue().toString(),
                                    userId.getKey(),
                                    upvote.getValue().toString(),
                                    downvote.getValue().toString(),
                                    id,
                                    userpic.getValue().toString(),
                                    userChild.getKey());
                            answer.add(anss);
                        }
                    }
                }
                answerAdapter.notifyDataSetChanged();
                swipeHeler.getAns(answer);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
}

