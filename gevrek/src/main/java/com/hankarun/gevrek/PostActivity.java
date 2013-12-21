package com.hankarun.gevrek;

import android.app.Activity;
import android.app.ActionBar;
import android.content.res.Configuration;
import android.support.v4.app.Fragment;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.os.Build;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class PostActivity extends FragmentActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post);

        Intent iin= getIntent();
        Bundle b = iin.getExtras();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
            getActionBar().setBackgroundDrawable(new ColorDrawable(Color.parseColor("#2f6699")));

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, new PlaceholderFragment("https://cow.ceng.metu.edu.tr/News/" + b.getString("reply")))
                    .commit();
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig){
        super.onConfigurationChanged(newConfig);
    }

    public static class PlaceholderFragment extends Fragment {
        final String link;
        String quote;
        String newsgroups;
        String newsgroup;
        String references;


        EditText from;
        EditText subject;
        EditText body;
        CheckBox quoteCheck;
        Button sendButton;
        Button cancel;

        LinearLayout postLayout;
        ProgressBar postBar;

        LoadReply task;

        @Override
        public void onPause(){
            super.onPause();
            if(task != null)
                task.cancel(true);
            if(task != null)
                task.cancel(true);
        }


        public PlaceholderFragment(String _link) {
            link = _link;
        }

        @Override
        public void onActivityCreated(Bundle savedInstanceState){
            super.onActivityCreated(savedInstanceState);

            task = new LoadReply();
            task.execute();
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_post, container, false);

            from = (EditText) rootView.findViewById(R.id.fromEdit);
            subject = (EditText) rootView.findViewById(R.id.subjectEdit);
            body = (EditText) rootView.findViewById(R.id.bodyEdit);

            postLayout = (LinearLayout) rootView.findViewById(R.id.postLayout);
            postBar = (ProgressBar) rootView.findViewById(R.id.postBar);

            quoteCheck = (CheckBox) rootView.findViewById(R.id.quoteCheck);
            quoteCheck.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                    if(b){
                        body.setText(body.getText() + quote);
                        quoteCheck.setVisibility(View.GONE);
                    }
                }
            });

            sendButton = (Button) rootView.findViewById(R.id.sendButton);
            cancel = (Button) rootView.findViewById(R.id.cancelButton);


            sendButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if(subject.getText().equals(""))
                        subject.setError(getString(R.string.please_fill));
                    else
                    if(body.getText().equals(""))
                        body.setError(getString(R.string.please_fill));
                    else{
                        postBar.setVisibility(View.VISIBLE);
                        postLayout.setVisibility(View.GONE);
                        new PostReply().execute();
                    }
                }
            });


            cancel.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    getActivity().finish();
                }
            });

            return rootView;
        }

        private class PostReply extends AsyncTask<String, String, String> {

            @Override
            protected String doInBackground(String... strings) {
                SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(getActivity());

                String uname = settings.getString("user_name", "");
                String upassword = settings.getString("user_password", "");

                HttpClient client = new DefaultHttpClient();
                HttpPost post = new HttpPost("https://cow.ceng.metu.edu.tr/News/post.php");

                try {

                    List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(1);
                    nameValuePairs.add(new BasicNameValuePair("cow_username", uname));
                    nameValuePairs
                            .add(new BasicNameValuePair("cow_password", upassword));
                    nameValuePairs.add(new BasicNameValuePair("cow_login", "login"));

                    //Adding other post parameters
                    nameValuePairs.add(new BasicNameValuePair("body",body.getText().toString()));
                    nameValuePairs.add(new BasicNameValuePair("references",references));
                    nameValuePairs.add(new BasicNameValuePair("group",newsgroup));
                    nameValuePairs.add(new BasicNameValuePair("newsgroups",newsgroups));
                    nameValuePairs.add(new BasicNameValuePair("subject",subject.getText().toString()));
                    nameValuePairs.add(new BasicNameValuePair("cc",from.getText().toString()));


                    nameValuePairs.add(new BasicNameValuePair("type","post"));


                    post.setEntity(new UrlEncodedFormEntity(nameValuePairs));
                    HttpResponse response = client.execute(post);


                    String html = EntityUtils.toString(response.getEntity(), "ISO-8859-9");

                    return html;


                } catch (IOException e) {
                    e.printStackTrace();
                }
                return null;
            }

            @Override
            protected  void onPostExecute(String string){
                if(!string.equals("")){
                    //Check for success,
                    //Return for refresh
                    Intent returnIntent = new Intent();
                    returnIntent.putExtra("return","reload");
                    getActivity().setResult(RESULT_OK, returnIntent);
                    getActivity().finish();
                }else{
                    Toast.makeText(getActivity().getApplicationContext(),R.string.network_problem,Toast.LENGTH_SHORT).show();
                    Intent returnIntent = new Intent();
                    getActivity().setResult(RESULT_CANCELED, returnIntent);
                    getActivity().finish();
                }


            }
        }

        private class LoadReply extends AsyncTask<String, String, String> {

            @Override
            protected String doInBackground(String... strings) {
                SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(getActivity());

                String uname = settings.getString("user_name", "");
                String upassword = settings.getString("user_password", "");

                HttpClient client = new DefaultHttpClient();
                HttpPost post = new HttpPost(link);

                try {

                    List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(1);
                    nameValuePairs.add(new BasicNameValuePair("cow_username", uname));
                    nameValuePairs
                            .add(new BasicNameValuePair("cow_password", upassword));
                    nameValuePairs.add(new BasicNameValuePair("cow_login", "login"));

                    post.setEntity(new UrlEncodedFormEntity(nameValuePairs));
                    HttpResponse response = client.execute(post);


                    String html = EntityUtils.toString(response.getEntity(), "ISO-8859-9");

                    return html;


                } catch (IOException e) {
                    e.printStackTrace();
                }
                return null;

            }

            @Override
            protected  void onPostExecute(String string){
                if(string != null){
                    Document doc = Jsoup.parse(string);
                    Elements inputs = doc.select("input");
                    for(Element s: inputs){
                        if(s.attr("name").equals("cc"))
                            from.setText(s.attr("value"));
                        if(s.attr("name").equals("subject"))
                            subject.setText(s.attr("value"));
                        if(s.attr("name").equals("hide"))
                            quote = s.attr("value");
                        if(s.attr("name").equals("newsgroups"))
                            newsgroups = s.attr("value");
                        if(s.attr("name").equals("group"))
                            newsgroup = s.attr("value");
                        if(s.attr("name").equals("references"))
                            references = s.attr("value");

                    }

                }
                postBar.setVisibility(View.GONE);
                postLayout.setVisibility(View.VISIBLE);

            }
        }
    }

}
