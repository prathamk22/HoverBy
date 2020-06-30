package com.example.pratham.jsoup;

import android.Manifest;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.zhihu.matisse.Matisse;
import com.zhihu.matisse.MimeType;
import com.zhihu.matisse.engine.impl.PicassoEngine;


import de.hdodenhof.circleimageview.CircleImageView;


public class EditProfile extends Fragment {

    Spinner spinner;
    EditText nameText,phoneNumberText,bio,email;
    private static final String[] paths = {"Male", "Female", "Transgender"};
    private static final String[] photo = {"New Photo", "Remove Photo"};
    String gender,pic;
    FloatingActionButton save,back;
    public static Uri ImageUri;
    DatabaseReference reference;
    StorageReference storage;
    public static boolean isImageUpdated = false;
    public static CircleImageView userProfile;
    public static final int PHOTO = 900;
    public static final int PHOTO_PERMISSION = 501;
    public static final int CROP = 400;
    SharedPreferences userData;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_edit_profile, container, false);
        spinner = view.findViewById(R.id.spinners);
        save = view.findViewById(R.id.save);
        back = view.findViewById(R.id.back);
        email = view.findViewById(R.id.email);
        storage = FirebaseStorage.getInstance().getReference();
        nameText = view.findViewById(R.id.nameText);
        reference = FirebaseDatabase.getInstance().getReference();
        userProfile = view.findViewById(R.id.userProfile);
        phoneNumberText = view.findViewById(R.id.phoneNumber);
        bio = view.findViewById(R.id.bio);
        userData = getContext().getSharedPreferences("User", Context.MODE_PRIVATE);
        ArrayAdapter<String>adapter = new ArrayAdapter<String>(getContext(),
                android.R.layout.simple_spinner_item,paths);

        final int frag = getArguments().getInt("frag");
        nameText.setText(userData.getString("Name", ""));
        phoneNumberText.setText(userData.getString("PhoneNumber", ""));
        bio.setText(userData.getString("Bio", ""));
        gender = userData.getString("Gender", "");
        pic = userData.getString("ProfilePic", "");
        if(!pic.matches(""))
            Glide.with(getContext()).load(pic).into(userProfile);
        switch (gender){
            case "Male":spinner.setSelection(1);
                break;
            case "Female":spinner.setSelection(2);
                break;
            case "Transgender":spinner.setSelection(3);
                break;
        }
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                gender = paths[position];
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(!TextUtils.isEmpty(nameText.getText())){
                    if(!TextUtils.isEmpty(phoneNumberText.getText())  && phoneNumberText.getText().length()==10){
                        if(!TextUtils.isEmpty(bio.getText())){
                            UpdateProfile();
                        }else{
                            bio.setError("* Required Field"); }
                    }else{
                        phoneNumberText.setError("* Required Field"); }
                }else{
                    nameText.setError("* Required Field"); }
            }
        });

        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(frag==0){
                    getActivity().finish();
                }else{
                    SettingsList list = new SettingsList();
                    FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
                    transaction.setCustomAnimations(R.anim.enter_from_right, R.anim.exit_to_left);
                    transaction.replace(R.id.frameLayout, list);
                    transaction.commit();
                }
            }
        });

        userProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Dialog dialog = new Dialog(getActivity());
                dialog.setContentView(R.layout.dialog_layout);

                ListView listView = dialog.findViewById(R.id.listview);

                listView.setAdapter(new ArrayAdapter<String>(getContext(),android.R.layout.simple_list_item_1,photo));

                listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        switch (position){
                            case 0:newPhoto();
                                dialog.dismiss();
                                break;
                            case 1: dialog.dismiss();
                                break;
                        }
                    }
                });

                dialog.show();
            }
        });
        return view;
    }



    public void UpdateProfile(){
        final SharedPreferences.Editor editor = userData.edit();

        editor.putString("Name",nameText.getText().toString());
        editor.putString("PhoneNumber",phoneNumberText.getText().toString());
        editor.putString("Gender",gender);
        editor.putString("Bio",bio.getText().toString());
        editor.putString("Email",email.getText().toString());
        editor.apply();
        editor.commit();
        final ProgressDialog dialog = new ProgressDialog(getContext());
        dialog.setTitle("Profile");
        dialog.setMessage("Updating Profile");
        dialog.setCancelable(false);
        dialog.show();
        if(isImageUpdated){
            final UploadAsyncTask asyncTask = new UploadAsyncTask(ImageUri,getContext());
            asyncTask.execute();
            final Handler handler = new Handler();
            Runnable runnable = new Runnable() {
                @Override
                public void run() {
                    if(asyncTask.getStatus()==AsyncTask.Status.FINISHED){
                        handler.removeCallbacks(this);
                        dialog.dismiss();
                    }else
                        handler.postDelayed(this, 1000);
                }
            };
            handler.post(runnable);
        }else{
            dialog.dismiss();
            Toast.makeText(getContext(), "Profile Updated", Toast.LENGTH_SHORT).show();
        }
    }

    public void newPhoto(){
        if (ContextCompat.checkSelfPermission(getActivity(),
                android.Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(getActivity(),
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                    PHOTO_PERMISSION);
        } else {
            Matisse.from(getActivity())
                    .choose(MimeType.ofImage())
                    .imageEngine(new PicassoEngine())
                    .countable(false)
                    .maxSelectable(1)
                    .forResult(PHOTO);
        }
    }

}
