package com.hankarun.gevrek;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
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
import java.util.ArrayList;
import java.util.List;

public class Login extends FragmentActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, new LoginFragment())
                    .commit();
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig){
        super.onConfigurationChanged(newConfig);
    }


    public static class LoginFragment extends Fragment {

        LinearLayout greetings;
        LinearLayout loginform;
        LinearLayout progressbar;

        //texedit for username
        EditText username;
        EditText password;
        CheckBox savestate;
        Button loginbutton;
        //textedit for password
        //checkbox for save password
        //login button for clicking

        String uname;
        String upassword;

        //function for init variables
        private void init(View rootView){
            greetings = (LinearLayout) rootView.findViewById(R.id.greetings);
            loginform = (LinearLayout) rootView.findViewById(R.id.loginform);
            progressbar = (LinearLayout) rootView.findViewById(R.id.progress);

            password = (EditText) rootView.findViewById(R.id.passwordedittext);
            username = (EditText) rootView.findViewById(R.id.usernameedittext);
            savestate = (CheckBox) rootView.findViewById(R.id.savestatecheckbox);
            loginbutton = (Button) rootView.findViewById(R.id.loginbutton);

            loginbutton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    hideForm();
                    new Cowcreds().execute();
                }
            });
        }

        //function for getting username and password return false if not
        private boolean checkcreds(){
            SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(getActivity());

            uname = settings.getString("user_name", "");
            upassword = settings.getString("user_password", "");

            username.setText(uname);


            savestate.setChecked(settings.getBoolean("save_state",false));
            if(settings.getBoolean("save_state",false))
                password.setText(upassword);
            else
                password.setText("");

            return settings.getBoolean("save_state",false);
        }

        //show login form
        private void showForm(){
            progressbar.setVisibility(View.GONE);
            loginform.setVisibility(View.VISIBLE);


            RelativeLayout.LayoutParams layoutParams =
                    (RelativeLayout.LayoutParams) greetings.getLayoutParams();

            layoutParams.addRule(RelativeLayout.CENTER_HORIZONTAL, RelativeLayout.TRUE);
            layoutParams.addRule(RelativeLayout.CENTER_IN_PARENT,0);

            greetings.setLayoutParams(layoutParams);
        }

        private void hideForm(){
            progressbar.setVisibility(View.VISIBLE);
            loginform.setVisibility(View.GONE);


            RelativeLayout.LayoutParams layoutParams =
                    (RelativeLayout.LayoutParams)greetings.getLayoutParams();

            layoutParams.addRule(RelativeLayout.CENTER_VERTICAL, RelativeLayout.TRUE);

            greetings.setLayoutParams(layoutParams);

        }

        //check for username password from cow 1 for true 2 for wrong creditentials 3 for network connection
        private void checkcow(int result){
            switch (result){
                case 1:
                    savecreds();
                    //Load activity
                    Toast.makeText(getActivity().getApplicationContext(), R.string.login_success, Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(getActivity(), MainActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);
                    getActivity().finish();

                    //Check for name
                    break;
                case 2:
                    //Toast to show things and login form
                    Toast.makeText(getActivity().getApplicationContext(),R.string.check_creds,Toast.LENGTH_SHORT).show();
                    showForm();

                    break;
                case 3:
                    //Toast to show network connection failure
                    Toast.makeText(getActivity().getApplicationContext(),R.string.network_problem,Toast.LENGTH_SHORT).show();
                    getActivity().finish();
            }
        }

        //save password and username to the preferences
        private void savecreds(){
            SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getActivity());
            SharedPreferences.Editor edit = sp.edit();

            edit.putString("user_name", username.getText().toString());
            edit.putString("user_password", password.getText().toString());

            if(savestate.isChecked()){
                edit.putBoolean("save_state",true);
            }else{
                edit.putBoolean("save_state",false);
            }

            edit.commit();
        }

        public LoginFragment() {
        }



        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {

            View rootView;
            if (Build.VERSION.SDK_INT >= 17){
                rootView = inflater.inflate(R.layout.fragment_login, container, false);
            }else{
                rootView = inflater.inflate(R.layout.fragment_login_legacy, container, false);
            }

            init(rootView);
            if(!checkcreds()){
                new WaitSplash().execute();
            }else{
                new Cowcreds().execute();
            }

            return rootView;
        }


        private class Cowcreds extends AsyncTask<String, String, String> {

            @Override
            protected String doInBackground(String... args) {
                HttpClient client = new DefaultHttpClient();
                HttpPost post = new HttpPost("https://cow.ceng.metu.edu.tr/User/index.php");

                try {
                    List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(1);
                    nameValuePairs.add(new BasicNameValuePair("cow_username", username.getText().toString()));
                    nameValuePairs
                            .add(new BasicNameValuePair("cow_password", password.getText().toString()));
                    nameValuePairs.add(new BasicNameValuePair("cow_login", "login"));

                    post.setEntity(new UrlEncodedFormEntity(nameValuePairs));
                    HttpResponse response = client.execute(post);

                    String html = EntityUtils.toString(response.getEntity());
                    return html;


                } catch (IOException e) {
                    e.printStackTrace();
                }


                return "";
            }
            @Override
            protected void onPostExecute(String html) {
                if(!html.equals("")){
                    if(html.contains("Wrong login data!"))
                        checkcow(2);
                    else{
                        Document doc = Jsoup.parse(html);
                        Element loginform = doc.getElementById("edit_auth");

                        Elements inputElements = loginform.select("tr");

                        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getActivity());
                        SharedPreferences.Editor edit = sp.edit();

                        edit.putString("name_user", inputElements.get(5).select("td").text());
                        edit.putString("email_user", inputElements.get(6).select("td").text());
                        edit.putString("avatar", inputElements.get(20).select("a").attr("abs:href"));
                        edit.commit();


                        checkcow(1);

                    }
                }else
                    checkcow(3);

            }
        }

        class WaitSplash extends AsyncTask<Void, Void, Void> {
            @Override
            protected Void doInBackground(Void... params) {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                return null;
            }

            @Override
            protected void onPostExecute(Void a) {
                showForm();
            }
        }
    }

}
