package com.hankarun.gevrek;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.preference.PreferenceManager;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.util.List;


public class PageFetchAsync extends AsyncTask<List<NameValuePair>, String, String> {
    private JavaAsyncCompleteListener callback;

    private String currentUrl;
    private Activity activity;

    public PageFetchAsync(JavaAsyncCompleteListener listener, String _currentPage, Activity _activity){
        activity = _activity;
        callback = listener;
        currentUrl = _currentPage;
    }

    @Override
    protected String doInBackground(List<NameValuePair>... nameValuePairs) {
        HttpClient client = new DefaultHttpClient();
        HttpPost post = new HttpPost(currentUrl);
        if(!currentUrl.equals(HttpPages.login_page)){
            SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(activity);

            nameValuePairs[0].add(new BasicNameValuePair("cow_username", settings.getString("user_name", "")));
            nameValuePairs[0]
                    .add(new BasicNameValuePair("cow_password", settings.getString("user_password", "")));
            nameValuePairs[0].add(new BasicNameValuePair("cow_login", "login"));
        }
        try {
            post.setEntity(new UrlEncodedFormEntity(nameValuePairs[0]));
            HttpResponse response = client.execute(post);

            return  EntityUtils.toString(response.getEntity(), "ISO-8859-9");
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "";
    }

    @Override
    protected void onPostExecute(String result){
        callback.onTaskComplete(result);
    }
}
