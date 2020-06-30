package com.example.pratham.jsoup;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.pratham.jsoup.Packages.Ans;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Random;

import de.hdodenhof.circleimageview.CircleImageView;

import static android.content.Context.MODE_PRIVATE;

public class AnswerAdapter extends RecyclerView.Adapter<AnswerAdapter.ViewHolders> {

    Context context;
    private String[] colors = {"#fa7268","#a8c55c","#0d243f","#ec8a04","#a8c55c","#e41749","#6fc2d0","#ff8a5c","#33313b","#8e2e6a"};
    private ArrayList<Ans> ans;
    private int upvote=0,downvote=0;
    DatabaseReference reference;
    private String username;
    SharedPreferences userData;
    private int Height,width = 0,margin = 0,ran =0;
    private ArrayList<String> streamId;
    public static String TimeNow;
    boolean isOwnComment = false;

    public AnswerAdapter(Context context,ArrayList<Ans> arrayList,ArrayList<String> streamId) {
        this.context = context;
        ans = arrayList;
        this.streamId= streamId;
        reference = FirebaseDatabase.getInstance().getReference();
        userData = context.getSharedPreferences("User", MODE_PRIVATE);
        username = userData.getString("Name",null);
    }

    @NonNull
    @Override
    public AnswerAdapter.ViewHolders onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View itemView = LayoutInflater.from(context)
                .inflate(R.layout.answer_layout,null,false);

        return new AnswerAdapter.ViewHolders(itemView);
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public void onBindViewHolder(@NonNull final AnswerAdapter.ViewHolders viewHolder, final int i) {
        width = 0;
        Height = 0;
        ran++;
        if(ran==colors.length)
            ran=0;
        boolean isMatched = false;
        Typeface sm = Typeface.createFromAsset(context.getAssets(), "fonts/segeo_sm.ttf");
        Typeface bold = Typeface.createFromAsset(context.getAssets(), "fonts/segeo_bold.ttf");

        viewHolder.user.setTypeface(bold);
        viewHolder.ansText.setTypeface(sm);

        viewHolder.user.setText(ans.get(i).user);
        viewHolder.ansText.setText(ans.get(i).ans);
        Glide.with(context).load(ans.get(i).pic).into(viewHolder.profilepic);
        viewHolder.design.setCardBackgroundColor(Color.parseColor(colors[ran]));
        viewHolder.user.setTextColor(Color.parseColor(colors[ran]));
        for(int z=0; z<streamId.size(); z++){
            if(ans.get(i).ansId.matches(streamId.get(z))){
                isMatched = true;
            }
        }

        if(isMatched){
            viewHolder.streamed.setVisibility(View.VISIBLE);
        }else
            viewHolder.streamed.setVisibility(View.INVISIBLE);

        upvote = Integer.parseInt(ans.get(i).upvote);
        downvote = Integer.parseInt(ans.get(i).downvote);
        int total = upvote - downvote;
        viewHolder.voting.setText(String.valueOf(total));

        if(isOwnComment){
            viewHolder.user.setText(ans.get(i).id);
        }

        viewHolder.design.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                width = viewHolder.design.getWidth();
                ViewGroup.MarginLayoutParams lp = (ViewGroup.MarginLayoutParams) viewHolder.design.getLayoutParams();
                margin  = lp.rightMargin;

            }
        });

        viewHolder.mainlayout.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                Height = viewHolder.mainlayout.getHeight();
                ViewGroup.LayoutParams params = new RelativeLayout.LayoutParams(width,viewHolder.mainlayout.getHeight());
                //((RelativeLayout.LayoutParams) params).setMarginStart(40);
                ((RelativeLayout.LayoutParams) params).setMarginEnd(margin);
                viewHolder.design.setLayoutParams(params);
            }
        });
    }

    @Override
    public int getItemCount() {
        return ans.size();
    }

    public class ViewHolders extends RecyclerView.ViewHolder {
        TextView user,ansText,voting;
        RelativeLayout relative,root;
        CircleImageView profilepic;
        LinearLayout animationLayout;
        CardView mainlayout,design;
        ImageView streamed;
        public ViewHolders(@NonNull View itemView) {
            super(itemView);
            user = itemView.findViewById(R.id.user);
            ansText = itemView.findViewById(R.id.ans);
            root = itemView.findViewById(R.id.root);
            relative = itemView.findViewById(R.id.relative);
            animationLayout = itemView.findViewById(R.id.animationLayout);
            voting = itemView.findViewById(R.id.voting);
            mainlayout = itemView.findViewById(R.id.mainlayout);
            design = itemView.findViewById(R.id.design);
            profilepic = itemView.findViewById(R.id.profilepic);
            streamed = itemView.findViewById(R.id.streamed);
        }
    }

    public void isOwnComment(boolean t){
        isOwnComment = t;
    }
}
