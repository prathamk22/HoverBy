package com.example.pratham.jsoup;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.pratham.jsoup.Packages.Post;
import com.example.pratham.jsoup.Packages.UserComponents;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.yarolegovich.discretescrollview.DiscreteScrollView;
import com.yarolegovich.discretescrollview.transform.Pivot;
import com.yarolegovich.discretescrollview.transform.ScaleTransformer;

import java.util.ArrayList;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

import static android.content.Context.MODE_PRIVATE;

public class Profile extends Fragment {
    DiscreteScrollView recyclerview;
    TextView name,bio,reputation,totalPosts,profile,four,found,tag,hello;
    CircleImageView circleImageView;
    Button editPost;
    LinearLayout notFound;
    FloatingActionButton settings,refresh;
    FirebaseAuth auth;
    DatabaseReference reference;
    ArrayList<Post> mItems;
    ArrayList<String> answer;
    ArrayList<String> postId;
    CustomGridLayoutManager layoutManager;
    ProfileAdapter adapter;
    UserComponents components;
    SharedPreferences preferences;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);
        recyclerview = view.findViewById(R.id.recyclerview);
        settings = view.findViewById(R.id.settings);
        found = view.findViewById(R.id.found);
        tag = view.findViewById(R.id.tag);
        hello = view.findViewById(R.id.hello);
        four = view.findViewById(R.id.four);
        notFound = view.findViewById(R.id.notFound);
        profile = view.findViewById(R.id.profile);
        name = view.findViewById(R.id.name);
        bio = view.findViewById(R.id.bio);
        reputation = view.findViewById(R.id.reputations);
        refresh = view.findViewById(R.id.refresh);
        totalPosts = view.findViewById(R.id.totalPost);
        circleImageView = view.findViewById(R.id.userProfile);
        editPost = view.findViewById(R.id.editPost);
        auth = FirebaseAuth.getInstance();
        reference = FirebaseDatabase.getInstance().getReference();
        mItems = new ArrayList<>();
        layoutManager = new CustomGridLayoutManager(getContext());
        adapter = new ProfileAdapter(getContext(),mItems);
        answer = new ArrayList<>();
        postId = new ArrayList<>();
        components = new UserComponents();
        preferences = getContext().getSharedPreferences("User", MODE_PRIVATE);

        Typeface reg = Typeface.createFromAsset(getContext().getAssets(), "fonts/segeo_reg.ttf");
        Typeface sb = Typeface.createFromAsset(getContext().getAssets(), "fonts/segeo_sm.ttf");
        Typeface bold = Typeface.createFromAsset(getContext().getAssets(), "fonts/segeo_bold.ttf");
        profile.setTypeface(bold);
        name.setTypeface(sb);
        bio.setTypeface(reg);
        four.setTypeface(sb);
        tag.setTypeface(reg);
        found.setText("NO POST AVAILABLE");
        found.setTypeface(bold);
        editPost.setTypeface(bold);
        hello.setTypeface(bold);


        name.setText(preferences.getString("Name","Anonymous"));
        bio.setText(preferences.getString("Bio","At a Glance"));
        reputation.setText(String.valueOf(preferences.getInt("reputation",0)));
        layoutManager.setScrollEnabled(false);
        recyclerview.setNestedScrollingEnabled(true);
        recyclerview.setItemTransformer(new ScaleTransformer.Builder()
                .setMaxScale(1.05f)
                .setMinScale(0.8f)
                .setPivotX(Pivot.X.CENTER)
                .setPivotY(Pivot.Y.BOTTOM)
                .build());
        recyclerview.setAdapter(adapter);
        editPost.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), Settings.class);
                intent.putExtra("frag",0);
                startActivity(intent);
            }
        });

        settings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), Settings.class);
                startActivity(intent);
            }
        });
        getAllPostUser();
        refresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                refresh.startAnimation(AnimationUtils.loadAnimation(getContext(),R.anim.rotate));
                name.setText(preferences.getString("Name","Anonymous"));
                bio.setText(preferences.getString("Bio","At a Glance"));
                reputation.setText(String.valueOf(preferences.getInt("reputation",0)));
                getAllPostUser();
                Toast.makeText(getContext(), "Syncing Profile", Toast.LENGTH_SHORT).show();
            }
        });

        Map<String, ?> allEntries = preferences.getAll();
        for (Map.Entry<String, ?> entry : allEntries.entrySet()) {
            Log.d("map values", entry.getKey() + ": " + entry.getValue().toString());
        }
        return view;
    }

    public void getAllPostUser(){
        String pic = preferences.getString("ProfilePic","https://firebasestorage.googleapis.com/v0/b/birthdayexchange.appspot.com/o/user.png?alt=media&token=92b9269c-4136-458a-b465-d8229b68c744");
        components.setPic(pic);
        Glide.with(getContext()).load(pic).into(circleImageView);

        final SharedPreferences postData = getActivity().getSharedPreferences("Post", MODE_PRIVATE);
        final SharedPreferences userData = getActivity().getSharedPreferences("User", MODE_PRIVATE);

        int totalPost = userData.getInt("TotalPost",0);
        totalPosts.setText(String.valueOf(totalPost));
        postId.clear();
        for(int i=totalPost; i>=1; i--) {
            String id = postData.getString("id" + String.valueOf(i), null);
            if (id != null) {
                postId.add(id);
            }
        }

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                mItems.clear();
                DataSnapshot postHandler = dataSnapshot.child("post");
                for(DataSnapshot eachPost : postHandler.getChildren()){
                    String userId = eachPost.getKey();
                    for(int i=0; i<postId.size(); i++){
                        if(userId.matches(postId.get(i))){
                            Post post= new Post();
                            DataSnapshot child = dataSnapshot.child("post").child(userId);
                            DataSnapshot time = child.child("Time");
                            DataSnapshot ques = child.child("Question");
                            DataSnapshot user = child.child("Name");
                            DataSnapshot ProfilePic = child.child("ProfilePic");
                            if(time.getValue()!=null
                                    && ques.getValue()!=null
                                    && user.getValue()!=null
                                    && ProfilePic.getValue()!=null){
                                post.setTime(time.getValue().toString());
                                post.setQuestion(ques.getValue().toString());
                                post.setUser(user.getValue().toString());
                                post.setId(child.getKey());
                                post.setProfilepic(ProfilePic.getValue().toString());
                                setPost(post);
                            }
                        }
                    }
                }
                adapter.notifyDataSetChanged();
                if(mItems.size()==0){
                    notFound.setVisibility(View.VISIBLE);
                    recyclerview.setVisibility(View.INVISIBLE);
                }else{
                    recyclerview.setVisibility(View.VISIBLE);
                    notFound.setVisibility(View.INVISIBLE);
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
}