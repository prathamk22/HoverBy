package  com.example.pratham.jsoup;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.widget.Toast;

import com.example.pratham.jsoup.Packages.Ans;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.Random;

import static android.content.Context.MODE_PRIVATE;


public class SwipeHeler extends ItemTouchHelper.SimpleCallback {
    private AnswerAdapter answerAdapter;
    Context context;
    private ArrayList<Ans> ans;
    private ArrayList<String> ansId;
    DatabaseReference reference;
    private SharedPreferences answerData,userData;
    private SharedPreferences.Editor ansDataEditor,userDataEditor;
    private int upvote,downvote,reputation;
    boolean isMatched = false;

    public SwipeHeler(AnswerAdapter answerAdapter, Context context,ArrayList<String> ansId) {
        super(0,ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT);
        this.answerAdapter = answerAdapter;
        this.context = context;
        this.ansId = ansId;
        answerData = context.getSharedPreferences("answer", MODE_PRIVATE);
        userData = context.getSharedPreferences("User",MODE_PRIVATE);
        reputation = userData.getInt("reputation", 0);
        reference = FirebaseDatabase.getInstance().getReference();
    }

    public void getAns(ArrayList<Ans> arrayList){
        ans = arrayList;
    }

    @Override
    public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder viewHolder1) {
        return false;
    }

    @Override
    public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int p) {
        ansDataEditor = answerData.edit();
        userDataEditor = userData.edit();
        Random random = new Random();
        int rand = random.nextInt(2);
        reputation += Math.pow(2.67, rand);
        int totalStream = answerData.getInt("streamcount",0);
        int i = viewHolder.getAdapterPosition();
        upvote = Integer.parseInt(ans.get(i).upvote);
        downvote = Integer.parseInt(ans.get(i).downvote);
        Vibrator v = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
        if(p==ItemTouchHelper.LEFT){
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                v.vibrate(VibrationEffect.createOneShot(50, 10));
            } else {
                v.vibrate(50);
            }
            for(int z=0; z<ansId.size(); z++){
                if(ans.get(i).ansId.matches(ansId.get(z))){
                    isMatched = true;
                }
            }
            if(!isMatched){
                totalStream++;
                downvote++;
                reference.child("post")
                        .child(ans.get(i).id)
                        .child("Answer")
                        .child(ans.get(i).user)
                        .child(ans.get(i).ansId)
                        .child("downvote")
                        .setValue(String.valueOf(downvote));
                ansDataEditor.putString("Stream"+String.valueOf(totalStream), ans.get(i).ansId);
                ansDataEditor.putInt("streamcount",totalStream);
                getAnswerData();
                Toast.makeText(context, "Down Streamed", Toast.LENGTH_SHORT).show();
                isMatched = false;
            }else{
                Toast.makeText(context, "Already streamed", Toast.LENGTH_SHORT).show();
                isMatched = false;
            }
            answerAdapter.notifyItemChanged(viewHolder.getAdapterPosition());
        }else{
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                v.vibrate(VibrationEffect.createOneShot(50, 10));
            } else {
                v.vibrate(50);
            }
            for(int z=0; z<ansId.size(); z++){
                if(ans.get(i).ansId.matches(ansId.get(z))){
                    isMatched = true;
                }
            }
            if(!isMatched){
                totalStream++;
                upvote++;
                reference.child("post")
                        .child(ans.get(i).id)
                        .child("Answer")
                        .child(ans.get(i).user)
                        .child(ans.get(i).ansId)
                        .child("upvote")
                        .setValue(String.valueOf(upvote));
                ansDataEditor.putString("Stream"+String.valueOf(totalStream), ans.get(i).ansId);
                ansDataEditor.putInt("streamcount",totalStream);
                getAnswerData();
                Toast.makeText(context, "Up Streamed", Toast.LENGTH_SHORT).show();
                isMatched = false;
            }else{
                Toast.makeText(context, "Already streamed", Toast.LENGTH_SHORT).show();
                isMatched = false;
            }
            answerAdapter.notifyItemChanged(viewHolder.getAdapterPosition());
        }
        ansDataEditor.apply();
        ansDataEditor.commit();
        userDataEditor.apply();
        userDataEditor.commit();
    }

    public void getAnswerData(){
        ansId.clear();
        int totalAns = answerData.getInt("streamcount",0);
        for(int i=1; i<=totalAns; i++)
            ansId.add(answerData.getString("Stream"+String.valueOf(i), ""));
    }

    @Override
    public void onChildDraw(@NonNull Canvas c, @NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {
        super.onChildDraw(c, recyclerView, viewHolder, dX/4, dY, actionState, isCurrentlyActive);
    }
}
