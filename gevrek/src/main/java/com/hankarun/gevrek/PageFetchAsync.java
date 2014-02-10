package com.hankarun.gevrek;

import android.os.AsyncTask;
import android.util.Log;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by hankarun on 4.02.2014.
 */
public class PageFetchAsync extends AsyncTask<List<NameValuePair>, String, String> {
    static String cow_page = "https://cow.ceng.metu.edu.tr/";
    static String login_page = cow_page + "User/index.php";
    static String courses_page = cow_page + "Courses/";
    static String group_page = cow_page + "News/";

    private JavaAsyncCompleteListener callback;

    private String currentUrl;

    public PageFetchAsync(JavaAsyncCompleteListener listener, int i){
        switch (i){
            case 1: currentUrl = login_page;
        }
        this.callback = listener;
    }

    @Override
    protected String doInBackground(List<NameValuePair>... nameValuePairs) {
        HttpClient client = new DefaultHttpClient();
        HttpPost post = new HttpPost(currentUrl);
        try {
            post.setEntity(new UrlEncodedFormEntity(nameValuePairs[0]));
            HttpResponse response = client.execute(post);

            return  EntityUtils.toString(response.getEntity());
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
