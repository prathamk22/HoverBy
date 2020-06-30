package com.example.pratham.jsoup;

import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.example.pratham.jsoup.Packages.Ans;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

import static android.content.Context.MODE_PRIVATE;

public class OwnComments extends Fragment {
    RecyclerView recyclerView;
    EditProfile editProfile;
    TextView hello;
    FloatingActionButton back;
    SharedPreferences userData,answerData;
    ArrayList<String> postId;
    DatabaseReference reference;
    ArrayList<Ans> ansId;
    AnswerAdapter adapter;
    ArrayList<String> streamId;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_own_comments, container, false);
        recyclerView = view.findViewById(R.id.recyclerview);
        hello = view.findViewById(R.id.hello);
        back = view.findViewById(R.id.back);
        editProfile = new EditProfile();
        postId = new ArrayList<>();
        streamId = new ArrayList<>();
        ansId = new ArrayList<>();
        reference = FirebaseDatabase.getInstance().getReference();
        userData = getActivity().getSharedPreferences("User", MODE_PRIVATE);
        answerData = getActivity().getSharedPreferences("answer", MODE_PRIVATE);
        Typeface tf = Typeface.createFromAsset(getContext().getAssets(), "fonts/futura.ttf");
        hello.setTypeface(tf);

        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SettingsList list = new SettingsList();
                FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
                transaction.setCustomAnimations(R.anim.enter_from_right, R.anim.exit_to_left);
                transaction.replace(R.id.frameLayout, list);
                transaction.commit();
            }
        });

        getAnswerData();
        getData();
        adapter = new AnswerAdapter(getContext(),ansId,streamId);
        adapter.isOwnComment(true);
        CustomGridLayoutManager customGridLayoutManager = new CustomGridLayoutManager(getContext());
        recyclerView.setLayoutManager(customGridLayoutManager);
        recyclerView.setAdapter(adapter);

        return view;
    }

    public void getData(){
        int totalAns = answerData.getInt("total",0);
        for(int i=1; i<=totalAns; i++){
            String id = answerData.getString(String.valueOf(i),null);
            if(id!=null)
                postId.add(id);
        }

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                DataSnapshot p = dataSnapshot.child("post");
                for(DataSnapshot posts : p.getChildren()){
                    DataSnapshot answers = posts.child("Answer");
                    for(DataSnapshot name : answers.getChildren()){
                            for(DataSnapshot id : name.getChildren()){
                                for (int i=0; i<postId.size(); i++){
                                    if(id.getKey().matches(postId.get(i))){
                                        Ans ans = new Ans(id.child("Time").getValue().toString(),
                                                id.child("Ans").getValue().toString(),
                                                name.getKey(),
                                                id.child("upvote").getValue().toString(),
                                                id.child("downvote").getValue().toString(),
                                                posts.child("Question").getValue().toString(),
                                                id.child("ProfilePic").getValue().toString(),
                                                id.getKey());
                                        setId(ans);
                                        break;
                                    }
                                }
                            }
                    }
                }
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(getContext(), databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void setId(Ans ans){
        ansId.add(ans);
    }

    public void getAnswerData(){
        streamId.clear();
        int totalAns = answerData.getInt("streamcount",0);
        for(int i=1; i<=totalAns; i++)
            streamId.add(answerData.getString("Stream"+String.valueOf(i), ""));
    }
}
