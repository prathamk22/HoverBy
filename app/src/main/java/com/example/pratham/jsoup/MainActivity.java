package com.example.pratham.jsoup;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Toast;

import com.developers.imagezipper.ImageZipper;
import com.yalantis.ucrop.UCrop;
import com.zhihu.matisse.Matisse;

import java.io.File;
import java.io.IOException;
import java.util.List;

import static com.example.pratham.jsoup.NewLoginFragment.PHOTO;
import static com.example.pratham.jsoup.NewLoginFragment.userProfile;

public class MainActivity extends AppCompatActivity {

    SplashScreen splashScreen;
    NoInternetConnection noInternetConnection;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        splashScreen = new SplashScreen();
        noInternetConnection = new NoInternetConnection();
        if (CheckInternetConnection.isConnectedToNetwork(MainActivity.this)) {
            fragment(splashScreen);
            startService(new Intent(this, NotificationService.class));
        } else {
            fragment(noInternetConnection);
        }
    }

    void fragment(Fragment fragment){
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.setCustomAnimations(R.anim.alpha_up, R.anim.anim_down);
        transaction.replace(R.id.frameLayout, fragment);
        transaction.commit();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
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

            try{
                UCrop.Options options = new UCrop.Options();
                options.setFreeStyleCropEnabled(false);
                UCrop.of(photoUri,photoUri)
                        .withOptions(options)
                        .withAspectRatio(1, 1)
                        .start(MainActivity.this);
            }catch (Exception e){
                e.printStackTrace();
            }
        }

        if(resultCode==RESULT_OK && requestCode==UCrop.REQUEST_CROP && data!= null){
            Uri resultData = UCrop.getOutput(data);
            NewLoginFragment.ImageUri = resultData;
            userProfile.setImageURI(resultData);
            NewLoginFragment.isImageUpdated = true;
        }
    }
}
