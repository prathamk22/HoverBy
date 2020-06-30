package com.example.pratham.jsoup;

import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

public class SettingsList extends Fragment {

    ListView listView;
    String[] strings = {"Edit Profile", "Your Comments","Log out"};
    EditProfile editProfile;
    OwnComments comments;
    TextView hello;
    FloatingActionButton back;
    int[] drawable = {R.drawable.ic_person_black_24dp,R.drawable.ic_action_comments,R.drawable.ic_lens_black_24dp};

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_settings_list, container, false);
        listView = view.findViewById(R.id.listView);
        hello = view.findViewById(R.id.hello);
        back = view.findViewById(R.id.back);
        editProfile = new EditProfile();
        comments = new OwnComments();
        Typeface reg = Typeface.createFromAsset(getContext().getAssets(), "fonts/segeo_reg.ttf");
        hello.setTypeface(reg);

        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().finish();
            }
        });

        listView.setAdapter(new BaseAdapter() {
            @Override
            public int getCount() {
                return strings.length;
            }

            @Override
            public Object getItem(int i) {
                return strings[i];
            }

            @Override
            public long getItemId(int position) {
                return position;
            }

            @Override
            public View getView(final int i, View convertView, ViewGroup parent) {
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.settings_adapter, parent,false);
                ImageView icon = convertView.findViewById(R.id.icon);
                TextView heading = convertView.findViewById(R.id.heading);
                Typeface reg = Typeface.createFromAsset(getContext().getAssets(), "fonts/segeo_reg.ttf");
                Typeface sb = Typeface.createFromAsset(getContext().getAssets(), "fonts/segeo_sm.ttf");
                Typeface bold = Typeface.createFromAsset(getContext().getAssets(), "fonts/segeo_bold.ttf");
                heading.setTypeface(sb);
                heading.setText(strings[i]);
                icon.setImageDrawable(getContext().getResources().getDrawable(drawable[i]));
                convertView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        switch (i){
                            case 0:Bundle b = new Bundle();
                                b.putInt("frag", -1);
                                fragment(editProfile,b);
                                break;
                            case 1:fragment(comments);
                                break;
                            case 2:logout();
                                break;
                        }
                    }
                });
                return convertView;
            }
        });
        return view;
    }

    void fragment(Fragment fragment){
        FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
        transaction.setCustomAnimations(R.anim.enter_from_left, R.anim.exit_to_right);
        transaction.replace(R.id.frameLayout, fragment);
        transaction.commit();
    }

    void fragment(Fragment fragment, Bundle bundle){
        fragment.setArguments(bundle);
        FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
        transaction.setCustomAnimations(R.anim.enter_from_left, R.anim.exit_to_right);
        transaction.replace(R.id.frameLayout, fragment);
        transaction.commit();
    }

    public void logout(){

    }

}
