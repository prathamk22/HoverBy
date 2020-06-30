package com.example.pratham.jsoup;

import android.graphics.Typeface;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.FrameLayout;

import com.gauravk.bubblenavigation.BubbleNavigationLinearView;
import com.gauravk.bubblenavigation.BubbleToggleView;
import com.gauravk.bubblenavigation.listener.BubbleNavigationChangeListener;

import java.util.ArrayList;

public class WallActivity extends AppCompatActivity {

    FrameLayout frame_layout;
    public static BubbleNavigationLinearView navigation;
    BubbleToggleView toggle1,toggle2;
    Explore explore;
    Profile profile;
    ArrayList<String> postId;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wall);
        frame_layout = findViewById(R.id.frame_layout);
        navigation = findViewById(R.id.navigation);
        toggle1 = findViewById(R.id.toggle1);
        toggle2 = findViewById(R.id.toggle2);
        navigation.setCurrentActiveItem(0);
        navigation.bringToFront();
        explore = new Explore();
        profile = new Profile();
        postId = new ArrayList();
        Typeface sm = Typeface.createFromAsset(getAssets(), "fonts/segeo_sm.ttf");
        toggle1.setTitleTypeface(sm);
        toggle2.setTitleTypeface(sm);
        getSupportFragmentManager().beginTransaction()
                .add(R.id.frame_layout,explore)
                .add(R.id.frame_layout,profile)
                .commit();
        setTabStateFragment(TabState.HOME);
        navigation.setNavigationChangeListener(new BubbleNavigationChangeListener() {
            @Override
            public void onNavigationChanged(View view, int position) {
                switch (position){
                    case 0:setTabStateFragment(TabState.HOME);
                        break;
                    case 1:setTabStateFragment(TabState.PROFILE);
                        break;
                }
            }
        });
    }

    private void setTabStateFragment(TabState state) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        switch (state) {
            case HOME: {
                transaction.setCustomAnimations(R.anim.enter_from_left, R.anim.exit_to_right);
                transaction.show(explore);
                transaction.hide(profile);
                break;
            }
            case PROFILE: {
                transaction.setCustomAnimations(R.anim.enter_from_right, R.anim.exit_to_left);
                transaction.show(profile);
                transaction.hide(explore);
                break;
            }
        }
        transaction.commit();
    }

    enum TabState {
        HOME,
        PROFILE
    }

    public static void navigationDown(){
        navigation.animate().alpha(0f).setDuration(300).start();
    }

    public static void navigationUp(){
        navigation.animate().alpha(1f).setDuration(300).start();
    }

    @Override
    protected void onResume() {
        super.onResume();
        navigationUp();
    }
}
