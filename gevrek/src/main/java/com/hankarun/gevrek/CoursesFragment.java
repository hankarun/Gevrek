package com.hankarun.gevrek;

import android.support.v4.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

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
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class CoursesFragment extends Fragment{

    private ListView listView;
    private ProgressBar progressBar;

    public CoursesFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_courses, container, false);
        listView = (ListView) rootView.findViewById(R.id.courselist);
        progressBar = (ProgressBar) rootView.findViewById(R.id.courseProgress);
        new Loadgroups().execute();
        listView.setDivider(null);
        listView.setDividerHeight(10);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Object o = listView.getItemAtPosition(i);

                Intent intent = new Intent(getActivity(), Homeworks.class);
                intent.putExtra("name",((Pairs) o).ccode);
                intent.putExtra("link",((Pairs) o).cname);
                startActivity(intent);
            }
        });
        return rootView;
    }

    private class Loadgroups extends AsyncTask<String, String, String> {

        @Override
        protected String doInBackground(String... args) {
            SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(getActivity());

            String uname = settings.getString("user_name", "");
            String upassword = settings.getString("user_password", "");

            HttpClient client = new DefaultHttpClient();
            HttpPost post = new HttpPost("https://cow.ceng.metu.edu.tr/Courses/");

            try {

                List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(1);
                nameValuePairs.add(new BasicNameValuePair("cow_username", uname));
                nameValuePairs
                        .add(new BasicNameValuePair("cow_password", upassword));
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
            Document doc = Jsoup.parse(html);
            doc.setBaseUri("https://cow.ceng.metu.edu.tr");

            ArrayList<Pairs> lnames = new ArrayList<Pairs>();
            Map<String,String> lcodes = new HashMap<String,String>();

            Elements names = doc.select("div");
            Element divs = null;
            for(Element e:names){
                if(e.attr("id").equals("mtm_menu_horizontal"))
                    divs = e;
            }
            Elements rnamesd = doc.select("td.content").select("tr").select("td");

            int x = 0;
            while(x<rnamesd.size()){
                lcodes.put(rnamesd.get(x).text(), rnamesd.get(x+1).text());
                x += 2;
            }
            Elements courses = divs.select("a");
            courses.remove(0);
            for(Element t: courses){
                lnames.add(new Pairs(t.text(),lcodes.get(t.text()), t.attr("abs:href")));

            }
            MyAdapter adapter = new MyAdapter(getActivity().getApplicationContext(), lnames);
            listView.setAdapter(adapter);
            listView.setVisibility(View.VISIBLE);
            progressBar.setVisibility(View.GONE);
        }


    }

    public class Pairs{
        final String cname;
        final String ccode;
        final String ref;

        public Pairs(String _cname, String _ccode, String _ref){
            cname = _cname;
            ccode = _ccode;
            ref = _ref;
        }
    }

    public class MyAdapter extends BaseAdapter {

        final List<Pairs> cuples;
        final Context context;

        public MyAdapter(Context context, ArrayList<Pairs> p){
            this.context = context;
            cuples = p;
        }

        @Override
        public int getCount() {
            return cuples.size();
        }

        @Override
        public Object getItem(int i) {
            return cuples.get(i);
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
                view = infalInflater.inflate(R.layout.course_item, null);
            }

            TextView ctnames = (TextView) view.findViewById(R.id.cname);
            TextView ctcodes = (TextView) view.findViewById(R.id.ccode);

            ctnames.setText(cuples.get(i).cname);
            ctcodes.setText(cuples.get(i).ccode);

            return view;

        }
    }
}

