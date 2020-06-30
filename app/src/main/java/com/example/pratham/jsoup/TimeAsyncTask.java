package com.example.pratham.jsoup;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.example.pratham.jsoup.Packages.Ans;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;

import static com.example.pratham.jsoup.NewPost.TIME;

public class TimeAsyncTask extends AsyncTask<Void,Void,String> {

    private Context context;

    public TimeAsyncTask(Context context) {
        this.context = context;
    }

    @Override
    protected String doInBackground(Void... voids) {

        String str="http://worldtimeapi.org/api/timezone/Asia/Kolkata";
        URLConnection urlConn;
        BufferedReader bufferedReader = null;
        try
        {
            URL url = new URL(str);
            urlConn = url.openConnection();
            bufferedReader = new BufferedReader(new InputStreamReader(urlConn.getInputStream()));

            StringBuffer stringBuffer = new StringBuffer();
            String line;
            while ((line = bufferedReader.readLine()) != null)
            {
                stringBuffer.append(line);
            }

            return stringBuffer.toString();
        }
        catch(Exception ex)
        {
            Log.e("App", "yourDataTask", ex);
            return null;
        }
        finally
        {
            if(bufferedReader != null)
            {
                try {
                    bufferedReader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @Override
    protected void onPostExecute(String s) {
        super.onPostExecute(s);
        try {
            JSONObject jObject = new JSONObject(s);
            TIME = jObject.getString("datetime");
            Answer.currentTime = jObject.getString("datetime");
            Explore.Today= jObject.getString("datetime");
            AnswerAdapter.TimeNow = jObject.getString("datetime");
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}
