package com.example.pratham.jsoup;

import android.app.Activity;
import android.app.Dialog;
import android.app.Notification;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.CountDownTimer;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.pratham.jsoup.Packages.Post;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Random;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;

class DeckAdapter extends RecyclerView.Adapter<DeckAdapter.ViewHolder> {


    private String[] colors = {"#fa7268","#a8c55c","#0d243f","#ec8a04","#a8c55c","#e41749","#6fc2d0","#ff8a5c","#33313b","#8e2e6a"};
    private String[] options = {"Copy Question","Report","Answer"};
    private ArrayList<Post> list;
    private ArrayList<String> ansId;
    private ArrayList<String> FirebaseId;
    Context context;
    private int ran = 0;
    private boolean isMatched = false;
    public DeckAdapter(@NonNull Context context, ArrayList<Post> list,ArrayList<String> ansId,ArrayList<String> FirebaseId) {
        super();
        this.list = list;
        this.context = context;
        this.ansId = ansId;
        this.FirebaseId = FirebaseId;
    }


    @NonNull
    @Override
    public DeckAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View itemView = LayoutInflater.from(context)
                .inflate(R.layout.adapter_layout,null,false);
        return new DeckAdapter.ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull final DeckAdapter.ViewHolder viewHolder, final int i) {
        isMatched = false;
        viewHolder.textView.setText(list.get(i).Question);
        Glide.with(context).load(list.get(i).profilepic).into(viewHolder.profilepic);
        viewHolder.userID.setText(list.get(i).User);
        Typeface reg = Typeface.createFromAsset(context.getAssets(), "fonts/segeo_reg.ttf");
        Typeface sm = Typeface.createFromAsset(context.getAssets(), "fonts/segeo_sm.ttf");
        Typeface bold = Typeface.createFromAsset(context.getAssets(), "fonts/segeo_bold.ttf");
        viewHolder.userID.setTypeface(sm);
        viewHolder.textView.setTypeface(bold);
        viewHolder.date.setTypeface(reg);
        ran++;
        if(ran==colors.length)
            ran=0;
        viewHolder.textView.setTextColor(Color.parseColor(colors[ran]));
        viewHolder.view.setBackgroundColor(Color.parseColor(colors[ran]));
        final int finalRan = ran;

        for(int z=0; z<ansId.size(); z++){
            String[] words=FirebaseId.get(i).split(",");
            for(String w : words){
                if(w.matches(ansId.get(z)))
                    isMatched = true;
            }
        }
        if(isMatched){
            viewHolder.commentAdded.setVisibility(View.VISIBLE);
        }else
            viewHolder.commentAdded.setVisibility(View.INVISIBLE);

        viewHolder.cardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Intent intent = new Intent(context, Answer.class);
                try {
                    intent.putExtra("ques",list.get(i).Question);
                    intent.putExtra("color",colors[finalRan]);
                    intent.putExtra("id",list.get(i).id);
                    intent.putExtra("time",getDateback(list.get(i).time));
                    intent.putExtra("user",list.get(i).User);
                    intent.putExtra("pic",list.get(i).profilepic);
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                if(isMatched)
                    intent.putExtra("comment",true);
                else
                    intent.putExtra("comment",false);
                final ActivityOptionsCompat options = ActivityOptionsCompat.
                        makeSceneTransitionAnimation((Activity) context,
                                viewHolder.cardView,
                                "simple_activity_transition");
                WallActivity.navigationDown();
                new CountDownTimer(100,200) {
                    @Override
                    public void onTick(long millisUntilFinished) {

                    }

                    @Override
                    public void onFinish() {
                        context.startActivity(intent,options.toBundle());
                    }
                }.start();
            }
        });
        viewHolder.option.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Dialog dialog = new Dialog(context);
                dialog.setContentView(R.layout.dialog_layout);

                ListView listView = dialog.findViewById(R.id.listview);

                listView.setAdapter(new ArrayAdapter<String>(context,android.R.layout.simple_list_item_1,options));

                listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        switch (position){
                            case 0: ClipboardManager clipboard = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
                                ClipData clip = ClipData.newPlainText("ques", list.get(i).Question);
                                clipboard.setPrimaryClip(clip);
                                dialog.dismiss();
                                break;
                            case 1:Intent intent = new Intent(context, Report.class);
                                    intent.putExtra("id",list.get(i).id);
                                context.startActivity(intent);
                                dialog.dismiss();
                                break;
                            case 2:dialog.dismiss();
                                break;
                        }
                    }
                });
                dialog.show();
            }
        });

        try {
            viewHolder.date.setText(getDateback(list.get(i).time));
        } catch (ParseException e) {
            e.printStackTrace();
        }

    }

    @Override
    public int getItemCount() {
        return list.size();
    }


    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView textView,userID,date;
        CardView cardView;
        ImageView profilepic,option,commentAdded;
        View view;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            date = itemView.findViewById(R.id.date);
            textView = itemView.findViewById(R.id.textView);
            userID = itemView.findViewById(R.id.userID);
            cardView = itemView.findViewById(R.id.cardView);
            view = itemView.findViewById(R.id.view);
            profilepic = itemView.findViewById(R.id.profilepic);
            option = itemView.findViewById(R.id.option);
            commentAdded = itemView.findViewById(R.id.commentAdded);
        }
    }

    private String getDateback(String s) throws ParseException {
        int l = s.indexOf('T');
        String a = s.substring(0,l);
        DateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd");
        DateFormat outputFormat = new SimpleDateFormat("dd MMM yyyy");
        Date date = inputFormat.parse(a);
        Date today = inputFormat.parse(Explore.Today);
        long difference = Math.abs(date.getTime() - today.getTime());
        long differenceDates = difference / (24 * 60 * 60 * 1000);
        int dayDifference = Integer.parseInt(Long.toString(differenceDates));
        if(dayDifference==0){
            return "Today";
        }else if(dayDifference==1){
            return "Yesterday";
        }else
            return outputFormat.format(date);
    }
}
