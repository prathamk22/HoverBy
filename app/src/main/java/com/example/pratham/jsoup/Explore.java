package com.example.pratham.jsoup;

import android.animation.Animator;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.widget.CardView;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.pratham.jsoup.Packages.Post;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.yarolegovich.discretescrollview.DiscreteScrollView;
import com.yarolegovich.discretescrollview.transform.Pivot;
import com.yarolegovich.discretescrollview.transform.ScaleTransformer;

import java.util.ArrayList;

import static com.example.pratham.jsoup.Answer.isAnswer;

public class Explore extends Fragment {
    FloatingActionButton newPost;
    public static final int imageRequest = 200;
    DiscreteScrollView rossDeck;
    LinearLayout notFound;
    ArrayList<Post> mItems;
    ImageView move;
    CardView start;
    ArrayList<String> answer,ansId,FirebaseAnsId;
    DeckAdapter adapter;
    TextView load,found,four,wb,name,have;
    DatabaseReference reference;
    SharedPreferences answerData,userData;
    public static String Today;
    boolean open = false;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_explore, container, false);
        newPost = view.findViewById(R.id.newPost);
        found = view.findViewById(R.id.found);
        wb = view.findViewById(R.id.wb);
        have = view.findViewById(R.id.have);
        name = view.findViewById(R.id.name);
        start = view.findViewById(R.id.start);
        notFound = view.findViewById(R.id.notFound);
        four = view.findViewById(R.id.four);
        load = view.findViewById(R.id.load);
        move = view.findViewById(R.id.move);
        rossDeck = view.findViewById(R.id.picker);
        mItems = new ArrayList<>();
        answerData = getContext().getSharedPreferences("answer", Context.MODE_PRIVATE);
        userData = getContext().getSharedPreferences("User", Context.MODE_PRIVATE);
        answer = new ArrayList<>();
        ansId = new ArrayList<>();
        FirebaseAnsId = new ArrayList<>();
        reference = FirebaseDatabase.getInstance().getReference();
        Typeface sm = Typeface.createFromAsset(getContext().getAssets(), "fonts/segeo_sm.ttf");
        Typeface bold = Typeface.createFromAsset(getContext().getAssets(), "fonts/segeo_bold.ttf");
        final TimeAsyncTask asyncTask = new TimeAsyncTask(getContext());

        asyncTask.execute();
        adapter = new DeckAdapter(getContext(),mItems,ansId,FirebaseAnsId);
        rossDeck.bringToFront();
        rossDeck.setItemTransformer(new ScaleTransformer.Builder()
                .setMaxScale(1.05f)
                .setMinScale(0.8f)
                .setPivotX(Pivot.X.CENTER) // CENTER is a default one
                //.setPivotY(Pivot.Y.BOTTOM) // CENTER is a default one
                .build());
        rossDeck.setAdapter(adapter);
        rossDeck.setItemTransitionTimeMillis(200);
        load.setTypeface(bold);
        four.setTypeface(sm);
        wb.setTypeface(bold);
        name.setTypeface(bold);
        have.setTypeface(bold);
        name.setText(userData.getString("Name",""));
        found.setText("CONTENT NOT FOUND");
        found.setTypeface(sm);
        newPost.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivityForResult(new Intent(getContext(), NewPost.class), imageRequest);
                getActivity().overridePendingTransition(R.anim.slide_up,R.anim.empty);
            }
        });

        final Handler handler = new Handler();
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                if(asyncTask.getStatus()== AsyncTask.Status.FINISHED){
                    getData();
                    getAnswerData();
                    handler.removeCallbacks(this);
                }else
                    handler.postDelayed(this,500);
            }
        };
        handler.postDelayed(runnable,500);

        move.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(open){
                    move.setImageDrawable(getResources().getDrawable(R.drawable.ic_keyboard_arrow_left_black_24dp));
                    start.animate().translationX(30f).setDuration(300).setInterpolator(new AccelerateInterpolator()).start();
                    open = false;
                }else{
                    move.setImageDrawable(getResources().getDrawable(R.drawable.ic_keyboard_arrow_right_black_24dp));
                    start.animate().translationX(-170f).setDuration(300).setInterpolator(new AccelerateInterpolator()).start();
                    open = true;
                }
            }
        });

        return view;
    }

    public void getAnswerData(){
        ansId.clear();
        int totalAns = answerData.getInt("total",0);
        for(int i=1; i<=totalAns; i++)
            ansId.add(answerData.getString(String.valueOf(i), ""));
    }

    public void getData(){
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(!isAnswer){
                    FirebaseAnsId.clear();
                    mItems.clear();
                    DataSnapshot posts = dataSnapshot.child("post");
                    for(DataSnapshot child : posts.getChildren()){
                        Post post= new Post();
                        DataSnapshot time = child.child("Time");
                        DataSnapshot ques = child.child("Question");
                        DataSnapshot pic = child.child("ProfilePic");
                        DataSnapshot name = child.child("Name");
                        if(time.getValue()!=null
                                && ques.getValue()!=null
                                && pic.getValue()!=null
                                && name.getValue()!=null){
                            post.setUser(name.getValue().toString());
                            post.setTime(time.getValue().toString());
                            post.setQuestion(ques.getValue().toString());
                            post.setId(child.getKey());
                            post.setProfilepic(pic.getValue().toString());
                            setPost(post);
                        }
                    }
                    load.animate().alphaBy(0f).setDuration(500).setListener(new Animator.AnimatorListener() {
                        @Override
                        public void onAnimationStart(Animator animation) {

                        }

                        @Override
                        public void onAnimationEnd(Animator animation) {
                            load.setVisibility(View.GONE);
                            if(mItems.size()==0){
                                notFound.setVisibility(View.VISIBLE);
                            }else{
                                rossDeck.setVisibility(View.VISIBLE);
                                rossDeck.setAlpha(0f);
                                rossDeck.animate().alpha(1f).setDuration(500).start();
                            }
                        }

                        @Override
                        public void onAnimationCancel(Animator animation) {

                        }

                        @Override
                        public void onAnimationRepeat(Animator animation) {

                        }
                    }).start();
                    for(DataSnapshot child : posts.getChildren()){
                        DataSnapshot ans = child.child("Answer");
                        String s = "";
                        for(DataSnapshot ansName : ans.getChildren()){
                            for(DataSnapshot ansId : ansName.getChildren()){
                                s = s.concat(ansId.getKey()).concat(",");
                            }
                        }
                        setFirebaseAnsId(s);
                        adapter.notifyDataSetChanged();
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(getContext(), databaseError.getDetails(), Toast.LENGTH_SHORT).show();
            }
        });

    }

    public void setPost(Post post){
        mItems.add(post);
    }

    public void setFirebaseAnsId(String s){
        FirebaseAnsId.add(s);
    }
}
