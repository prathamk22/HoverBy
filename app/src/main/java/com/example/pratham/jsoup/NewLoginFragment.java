package com.example.pratham.jsoup;

import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Spinner;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.zhihu.matisse.Matisse;
import com.zhihu.matisse.MimeType;
import com.zhihu.matisse.engine.impl.PicassoEngine;

import de.hdodenhof.circleimageview.CircleImageView;

public class NewLoginFragment extends Fragment {

    Button fab;
    Spinner spinner;
    String gender;
    Context context;
    EditText nameText,phoneNumberText, bio;
    private static final String[] paths = {"Male", "Female", "Transgender"};
    private static final String[] photo = {"New Photo", "Remove Photo"};
    public static Uri ImageUri;
    DatabaseReference reference;
    StorageReference storage;
    public static boolean isImageUpdated = false;
    boolean setup = false;
    public static CircleImageView userProfile;
    public static final int PHOTO = 900;
    public static final int PHOTO_PERMISSION = 501;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_new_login, container, false);
        fab = view.findViewById(R.id.fab);
        spinner = view.findViewById(R.id.spinners);
        storage = FirebaseStorage.getInstance().getReference();
        nameText = view.findViewById(R.id.nameText);
        reference = FirebaseDatabase.getInstance().getReference();
        userProfile = view.findViewById(R.id.userProfile);
        bio = view.findViewById(R.id.bio);
        phoneNumberText = view.findViewById(R.id.phoneNumber);

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(getContext(),
                android.R.layout.simple_spinner_item,paths);
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
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(setup){
                    Intent intent = new Intent(getContext(), WallActivity.class);
                    startActivity(intent);
                    getActivity().finish();
                }else{
                    if(!TextUtils.isEmpty(nameText.getText())){
                        if(!TextUtils.isEmpty(phoneNumberText.getText()) && phoneNumberText.getText().length()==10){
                            if(!TextUtils.isEmpty(bio.getText())){
                                UpdateProfile();
                            }else
                                bio.setError("* Field Required");
                        }else{
                            phoneNumberText.setError("* Field Required");
                        }
                    }else{
                        nameText.setError("* Field Required");
                    }
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
        final ProgressDialog dialog = new ProgressDialog(getContext());
        dialog.setTitle("Creating Profile");
        dialog.setMessage("Setting up Profile");
        dialog.setCancelable(false);
        dialog.show();
        SharedPreferences userData = getContext().getSharedPreferences("User", Context.MODE_PRIVATE);
        final SharedPreferences.Editor editor = userData.edit();

        editor.putBoolean("isNewUser", false);
        editor.putString("Name",nameText.getText().toString());
        editor.putString("PhoneNumber",phoneNumberText.getText().toString());
        editor.putString("Gender",gender);
        editor.putString("Bio",bio.getText().toString());
        if(isImageUpdated){
            final UploadAsyncTask asyncTask = new UploadAsyncTask(ImageUri,getContext());
            asyncTask.execute();
            final Handler handler = new Handler();
            Runnable runnable = new Runnable() {
                @Override
                public void run() {
                    if(asyncTask.getStatus()==AsyncTask.Status.FINISHED){
                        editor.apply();
                        editor.commit();
                        setup = true;
                        dialog.dismiss();
                        isImageUpdated = false;
                        try {
                            Intent intent = new Intent(getActivity(), WallActivity.class);
                            startActivity(intent);
                            getActivity().finish();
                        }catch (Exception e){
                            Log.e(getClass().toString(), e.getMessage());
                        }
                        handler.removeCallbacks(this);
                    }else
                        handler.postDelayed(this, 1000);
                }
            };
            handler.postDelayed(runnable,1000);
        }else{
            editor.apply();
            editor.commit();
            dialog.dismiss();
            setup = true;
            Intent intent = new Intent(getContext(), WallActivity.class);
            startActivity(intent);
            getActivity().finish();
        }

    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        this.context = context;
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
