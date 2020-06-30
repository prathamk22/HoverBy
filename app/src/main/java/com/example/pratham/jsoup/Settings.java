package com.example.pratham.jsoup;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.developers.imagezipper.ImageZipper;
import com.yalantis.ucrop.UCrop;
import com.yalantis.ucrop.UCropActivity;
import com.zhihu.matisse.Matisse;
import com.zhihu.matisse.MimeType;
import com.zhihu.matisse.engine.impl.PicassoEngine;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.List;

import static com.example.pratham.jsoup.EditProfile.CROP;
import static com.example.pratham.jsoup.EditProfile.PHOTO;
import static com.example.pratham.jsoup.EditProfile.PHOTO_PERMISSION;
import static com.example.pratham.jsoup.EditProfile.userProfile;

public class Settings extends AppCompatActivity {

    EditProfile editProfile;
    SettingsList list;
    int frag;
    String number,name,gender,pic;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        editProfile = new EditProfile();
        list = new SettingsList();
        frag = getIntent().getIntExtra("frag",-1);
        name = getIntent().getStringExtra("name");
        number = getIntent().getStringExtra("number");
        gender = getIntent().getStringExtra("gender");
        pic = getIntent().getStringExtra("pic");
        if(frag==0){
            Bundle bundle = new Bundle();
            bundle.putInt("frag",0);
            fragment(editProfile, bundle);
        }else{
            fragment(list);
        }

    }

    void fragment(Fragment fragment){
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.frameLayout, fragment);
        transaction.commit();
    }

    void fragment(Fragment fragment, Bundle bundle){
        fragment.setArguments(bundle);
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.frameLayout, fragment);
        transaction.commit();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(resultCode==RESULT_OK && requestCode==PHOTO){
            List<String> mSelected = Matisse.obtainPathResult(data);
            Uri ImageUri = Uri.fromFile(new File(mSelected.get(0)));
            File file = new File (ImageUri.getPath ());
            File Compressed=null;
            try {
                Compressed = new ImageZipper(getApplicationContext()).setQuality (75).setMaxWidth (640).setMaxHeight (480).setCompressFormat (Bitmap.CompressFormat.PNG).compressToFile (file);
            } catch (IOException e) {
                Toast.makeText (getApplicationContext(), "Error 407: " + e.getMessage (), Toast.LENGTH_SHORT).show ();
            }
            Uri photoUri = Uri.fromFile (Compressed);

            //UCrop.of(original, myUri).start(Settings.this);

            try{
                UCrop.Options options = new UCrop.Options();
                options.setFreeStyleCropEnabled(false);
                UCrop.of(photoUri,photoUri)
                        .withOptions(options)
                        .withAspectRatio(1, 1)
                        .start(Settings.this);
            }catch (Exception e){
                e.printStackTrace();
            }
        }

        if(resultCode==RESULT_OK && requestCode==UCrop.REQUEST_CROP && data!= null){
            Uri resultData = UCrop.getOutput(data);
            EditProfile.ImageUri = resultData;
            userProfile.setImageURI(resultData);
            EditProfile.isImageUpdated = true;
        }else
            Log.e("Error", String.valueOf(UCrop.getError(data)));
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode){
            case PHOTO_PERMISSION:
                if(grantResults.length > 0  && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    Matisse.from(Settings.this)
                            .choose(MimeType.ofImage())
                            .imageEngine(new PicassoEngine())
                            .countable(false)
                            .maxSelectable(1)
                            .forResult(PHOTO);
                }else{
                    Toast.makeText(this, "Permission not granted. Please grant permission from settings to continue", Toast.LENGTH_SHORT).show();
                }
                break;
        }

    }
}
