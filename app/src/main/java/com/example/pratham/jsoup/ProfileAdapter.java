package com.example.pratham.jsoup;

import android.app.Dialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.support.annotation.NonNull;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
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
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Random;

import de.hdodenhof.circleimageview.CircleImageView;

import static android.content.Context.MODE_PRIVATE;

public class ProfileAdapter extends RecyclerView.Adapter<ProfileAdapter.ViewHolder> {
    Context context;
    ArrayList<Post> list;
    int ran=0;
    private boolean isDelete = false;
    private String[] colors = {"#fa7268","#a8c55c","#0d243f","#ec8a04","#a8c55c","#e41749","#6fc2d0","#ff8a5c","#33313b","#8e2e6a"};
    private String[] options = {"Copy Question","Report","Answer","Delete"};

    public ProfileAdapter(Context context, ArrayList<Post> arrayList) {
        this.context = context;
        list = arrayList;
    }

    @NonNull
    @Override
    public ProfileAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View itemView = LayoutInflater.from(context)
                .inflate(R.layout.profile_recycler_view,null,false);
        return new ProfileAdapter.ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull final ProfileAdapter.ViewHolder viewHolder, final int i) {
        isDelete = false;
        viewHolder.textView.setText(list.get(i).Question);
        Glide.with(context).load(list.get(i).profilepic).into(viewHolder.profilepic);
        viewHolder.userID.setText(list.get(i).User);
        Typeface tf = Typeface.createFromAsset(context.getAssets(), "fonts/futura.ttf");
        viewHolder.userID.setTypeface(tf);
        ran++;
        if(ran==colors.length)
            ran=0;
        viewHolder.textView.setTextColor(Color.parseColor(colors[ran]));
        viewHolder.view.setBackgroundColor(Color.parseColor(colors[ran]));
        final int finalRan = ran;
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
                context.startActivity(intent);
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
                            case 0:
                                ClipboardManager clipboard = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
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
                            case 3:isDelete = true;
                            deletePost(list.get(i).id);
                                dialog.dismiss();
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
        ImageView option;
        CircleImageView profilepic;
        View view;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            textView = itemView.findViewById(R.id.textView);
            date = itemView.findViewById(R.id.date);
            userID = itemView.findViewById(R.id.userID);
            cardView = itemView.findViewById(R.id.cardView);
            view = itemView.findViewById(R.id.view);
            profilepic = itemView.findViewById(R.id.profilepic);
            option = itemView.findViewById(R.id.option);
        }
    }

    private String getDateback(String s) throws ParseException {
        int l = s.indexOf('T');
        String a = s.substring(0,l);
        DateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd");
        DateFormat outputFormat = new SimpleDateFormat("dd/MMM/yyyy");
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

    private void deletePost(final String id){
        final DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
        reference.child("post").child(id).getRef().removeValue();
        final SharedPreferences postData = context.getSharedPreferences("Post", MODE_PRIVATE);
        final SharedPreferences userData = context.getSharedPreferences("User", MODE_PRIVATE);
        final SharedPreferences.Editor userDataEditor = userData.edit();
        final SharedPreferences.Editor postDataEditor = postData.edit();

        int totalPost = userData.getInt("TotalPost",0);
        for(int i=1; i<=totalPost; i++) {
            String postId = postData.getString("id" + String.valueOf(i), null);
            if (postId != null) {
                if(postId.matches(id)){
                    postDataEditor.remove("id"+String.valueOf(i));
                    totalPost--;
                }
            }
        }
        userDataEditor.putInt("TotalPost", totalPost);
        postDataEditor.apply();
        postDataEditor.commit();
        userDataEditor.apply();
        userDataEditor.commit();
    }
}
