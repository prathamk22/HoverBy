package com.example.pratham.jsoup;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;

public class SplashScreen extends Fragment {

    String email,password,idToken;
    FirebaseAuth mAuth;
    NewLoginFragment newLoginFragment;
    TextView name;
    boolean isNewUser;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_splash_screen, container, false);
        SharedPreferences sharedPreferences = getActivity().getSharedPreferences("login", Context.MODE_PRIVATE);
        SharedPreferences preferences = getActivity().getSharedPreferences("User", Context.MODE_PRIVATE);
        email = sharedPreferences.getString("email",null);
        password = sharedPreferences.getString("pass",null);
        idToken = sharedPreferences.getString("idToken",null);
        mAuth = FirebaseAuth.getInstance();
        newLoginFragment = new NewLoginFragment();
        name = view.findViewById(R.id.name);
        isNewUser = preferences.getBoolean("isNewUser", true);
        Typeface tf = Typeface.createFromAsset(getContext().getAssets(), "fonts/cinzel.otf");
        name.setTypeface(tf);
        if(isNewUser){
            new CountDownTimer(1000,3000) {
                @Override
                public void onTick(long millisUntilFinished) {

                }

                @Override
                public void onFinish() {
                    fragment(newLoginFragment);
                }
            }.start();
        }else{
            Intent intent = new Intent(getContext(), WallActivity.class);
            startActivity(intent);
            getActivity().finish();
        }
        return view;
    }

    void fragment(Fragment fragment) {
        FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
        transaction.setCustomAnimations(R.anim.alpha_up, R.anim.anim_down);
        transaction.replace(R.id.frameLayout, fragment);
        transaction.commit();
    }

}
