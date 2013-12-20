package com.hankarun.gevrek;

import android.app.ActionBar;
import android.app.Activity;
import android.content.res.Configuration;
import android.provider.CalendarContract;
import android.support.v4.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.NavUtils;
import android.text.format.Time;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.joda.time.DateTime;
import org.joda.time.Period;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

public class Homeworks extends FragmentActivity {
    String coursename;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_homeworks);

        Intent iin= getIntent();
        Bundle b = iin.getExtras();
        String group = b.getString("link");

        coursename = b.getString("name");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB){
            getActionBar().setDisplayHomeAsUpEnabled(true);
            getActionBar().setBackgroundDrawable(new ColorDrawable(Color.parseColor("#2f6699")));
            getActionBar().setTitle(b.getString("name"));
        }

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, new PlaceholderFragment(group,coursename))
                    .commit();
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig){
        super.onConfigurationChanged(newConfig);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if(id == android.R.id.home){
            finish();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


    public static class PlaceholderFragment extends Fragment {
        ListView listView;

        final String link;
        ProgressBar bar;

        LinearLayout details;
        CheckBox hmwcheck;
        String course;

        public PlaceholderFragment(String link,String _course) {
            this.link = link;
            course = _course;
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_homeworks, container, false);

            listView = (ListView) rootView.findViewById(R.id.hmwlist);

            listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                    final HomeWorks hmw = (HomeWorks) listView.getItemAtPosition(i);

                    details = (LinearLayout) view.findViewById(R.id.hmwdetaillayout);
                    hmwcheck = (CheckBox) view.findViewById(R.id.hmwcheckBox);

                    if(details.getVisibility() == View.GONE)
                        details.setVisibility(View.VISIBLE);
                    else
                        details.setVisibility(View.GONE);

                    TextView deadline = (TextView) view.findViewById(R.id.deadlineText);
                    TextView schedule = (TextView) view.findViewById(R.id.scheduleText);

                    DateTimeFormatter formatter = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss");
                    final DateTime dead = formatter.parseDateTime(hmw.deadline);

                    DateTime today = new DateTime();

                    if(today.isAfter(dead))
                        deadline.setText(getString(R.string.hwm_deadline_passed));
                    else{
                        Period p = new Period(today, dead);
                        String tmp = "";
                        tmp += (p.getMonths()==0)?"":p.getMonths()+ " " + getString(R.string.month) + " ";
                        tmp += (p.getWeeks()==0)?"":p.getWeeks()+ " " + getString(R.string.week) + " ";
                        tmp += (p.getDays()==0)?"":p.getDays() + " " + getString(R.string.day) + " ";
                        tmp += (p.getHours()==0)?"":p.getHours() + " " + getString(R.string.hour);
                        deadline.setText(getString(R.string.still) + " " + tmp);
                        schedule.setVisibility(View.VISIBLE);
                        schedule.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                Toast.makeText(getActivity().getApplicationContext(),"test",Toast.LENGTH_SHORT).show();
                                Intent calIntent = new Intent(Intent.ACTION_INSERT);
                                calIntent.setType("vnd.android.cursor.item/event");
                                calIntent.putExtra(CalendarContract.Events.TITLE, hmw.name + " - " + hmw.course);
                                calIntent.putExtra(CalendarContract.Events.EVENT_LOCATION, "CENG");
                                GregorianCalendar calDate = dead.toGregorianCalendar();
                                calIntent.putExtra(CalendarContract.EXTRA_EVENT_ALL_DAY, true);
                                calIntent.putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME,
                                        calDate.getTimeInMillis());
                                calIntent.putExtra(CalendarContract.EXTRA_EVENT_END_TIME,
                                        calDate.getTimeInMillis());
                                startActivity(calIntent);
                            }
                        });
                    }

                    //if(hmw.greaded.equals("yes"))
                    //    hmwcheck.setVisibility(View.GONE);
                }
            });

            bar = (ProgressBar) rootView.findViewById(R.id.homeworkBar);
            new LoadHomeworks().execute(link);
            return rootView;
        }

        private class LoadHomeworks extends AsyncTask<String,String,String> {

            @Override
            protected String doInBackground(String... strings) {
                SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(getActivity());

                String uname = settings.getString("user_name", "");
                String upassword = settings.getString("user_password", "");

                HttpClient client = new DefaultHttpClient();
                HttpPost post = new HttpPost("https://cow.ceng.metu.edu.tr/Student/homeworks.php");

                try {

                    List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(1);
                    nameValuePairs.add(new BasicNameValuePair("cow_username", uname));
                    nameValuePairs
                            .add(new BasicNameValuePair("cow_password", upassword));
                    nameValuePairs.add(new BasicNameValuePair("cow_login", "login"));
                    nameValuePairs.add(new BasicNameValuePair("task_homeworks","list"));
                    nameValuePairs.add(new BasicNameValuePair("selector_homeworks_course",link));

                    post.setEntity(new UrlEncodedFormEntity(nameValuePairs));
                    HttpResponse response = client.execute(post);

                    String html = EntityUtils.toString(response.getEntity());
                    return html;


                } catch (IOException e) {
                    e.printStackTrace();
                }

                return null;
            }

            @Override
            protected void onPostExecute(String html) {
                if(!html.equals("")){
                    Document doc = Jsoup.parse(html);
                    ArrayList<HomeWorks> hmws = new ArrayList<HomeWorks>();

                    String cours = "";

                    Elements table = doc.select("table.cow");
                    Elements others = table.select("tr");

                    if(!others.toString().contains("The list is empty....")){
                        for(int x=3; x < others.size(); x++){
                            HomeWorks tmp = new HomeWorks();
                            tmp.id = others.get(x).select("td").get(0).text();
                            tmp.name = others.get(x).select("td").get(1).text();
                            tmp.deadline = others.get(x).select("td").get(2).text();
                            tmp.greaded = others.get(x).select("td").get(3).text();
                            tmp.link = others.get(x).select("td").get(4).select("a").attr("href");
                            if(others.get(x).select("td").get(5).text().isEmpty())
                                tmp.greade = "-";
                            else
                                tmp.greade = others.get(x).select("td").get(5).text();
                            tmp.avarage = others.get(x).select("td").get(6).text();
                            tmp.course = course;
                            hmws.add(tmp);
                        }
                    }else{
                        //Show no homework screen
                        Toast.makeText(getActivity().getApplicationContext(), R.string.no_hmw, Toast.LENGTH_SHORT).show();
                        getActivity().finish();

                    }

                    MyOtherAdapter adapter = new MyOtherAdapter(getActivity().getApplicationContext(),hmws);
                    listView.setAdapter(adapter);
                    bar.setVisibility(View.GONE);
                    listView.setVisibility(View.VISIBLE);
                }else{
                    Toast.makeText(getActivity().getApplicationContext(), R.string.network_problem, Toast.LENGTH_SHORT).show();
                    getActivity().finish();
                }
            }
        }
        public class HomeWorks{
            String course;
            String id;
            String name;
            String deadline;
            String greaded;
            String greade;
            String avarage;
            public String link;
        }

        public class MyOtherAdapter extends BaseAdapter {
            final ArrayList<HomeWorks> hmw;
            final Context context;

            public MyOtherAdapter(Context context, ArrayList<HomeWorks> _hmw){
                this.context = context;
                hmw = _hmw;
            }

            @Override
            public int getCount() {
                return hmw.size();
            }

            @Override
            public Object getItem(int i) {
                return hmw.get(i);
            }

            @Override
            public long getItemId(int i) {
                return i;
            }

            @Override
            public View getView(int i, View view, ViewGroup viewGroup) {
                if (view == null) {
                    LayoutInflater infalInflater = (LayoutInflater) this.context
                            .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                    view = infalInflater.inflate(R.layout.hmw_item, null);
                }


                TextView hname = (TextView) view.findViewById(R.id.hmwname);
                TextView grade = (TextView) view.findViewById(R.id.grade);
                TextView agrade = (TextView) view.findViewById(R.id.avarage);

                hname.setText(hmw.get(i).name);
                grade.setText(hmw.get(i).greade);
                agrade.setText(hmw.get(i).avarage);



                return view;
            }
        }
    }

}
