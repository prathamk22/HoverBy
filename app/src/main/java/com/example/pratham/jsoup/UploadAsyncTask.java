package com.example.pratham.jsoup;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.UUID;

public class UploadAsyncTask extends AsyncTask<Void,Void,Void> {

    Uri image;
    Context context;
    DatabaseReference firebaseDatabase;
    StorageReference storage;
    public static String id;
    SharedPreferences preferences;
    SharedPreferences.Editor editor;

    public UploadAsyncTask(Uri URI_PATH, Context context) {
        this.image= URI_PATH;
        this.context = context;
        firebaseDatabase = FirebaseDatabase.getInstance().getReference();
        storage = FirebaseStorage.getInstance().getReference();
        preferences = context.getSharedPreferences("User",Context.MODE_PRIVATE);
        editor = preferences.edit();
        id = UUID.randomUUID ().toString ();
    }

    @Override
    protected Void doInBackground(Void... voids) {
        storage.child("ProfilePhoto").child(id).putFile(image).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onComplete(@NonNull final Task<UploadTask.TaskSnapshot> task) {
                storage.child("ProfilePhoto/"+id).getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        String downloadUri = uri.toString();
                        editor.putString("ProfilePic", downloadUri);
                        editor.commit();
                        editor.apply();
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(context, e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
                Toast.makeText(context, "Saved", Toast.LENGTH_SHORT).show();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(context, e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
            }
        });
        return null;
    }

}
