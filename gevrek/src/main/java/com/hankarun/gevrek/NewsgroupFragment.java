package com.hankarun.gevrek;

import android.support.v4.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ExpandableListView;
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
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


public class NewsgroupFragment extends Fragment{
    private ExpandableListView listview;
    private ExpandableListAdapter adapter;
    private Loadgroups task;

    @Override
    public void onPause(){
        super.onPause();
        if(task != null)
            task.cancel(true);
        if(task != null)
            task.cancel(true);
    }

    private ProgressBar bar;


    public NewsgroupFragment() {
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        task = new Loadgroups();
        task.execute();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.news_group_fragment, container, false);

        listview = (ExpandableListView) rootView.findViewById(R.id.expandableListView);
        bar = (ProgressBar) rootView.findViewById(R.id.progressBar);

        return rootView;
    }

    public class Newsgroup {
        public String name;
        public final List<Urls> groups = new ArrayList<Urls>();

        @Override
        public String toString(){
            return name;
        }

        public int getSize() { return groups.size();}

        public void addUrl(String _name, String _url, String _count, String _color){
            groups.add(new Urls(_name,_url, _count, _color));
        }

        public Urls getUrl(int i) { return groups.get(i);}


    }
    public class Urls {
        public final String name;
        public final String url;
        public final String count;
        public final String color;

        @Override
        public String toString(){return name + " <font color=\""+ color +"\">" +count + "</font>";}

        Urls(String _name, String _url, String _count, String _color){
            name = _name;
            count = _count;
            url = _url;
            color = _color;
        }
    }

    private class Loadgroups extends AsyncTask<String, String, String> {

        @Override
        protected String doInBackground(String... args) {
            SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(getActivity());

            String uname = settings.getString("user_name", "");
            String upassword = settings.getString("user_password", "");

            HttpClient client = new DefaultHttpClient();
            HttpPost post = new HttpPost("https://cow.ceng.metu.edu.tr/News/cowNews_left.php");

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
        List<Newsgroup> groups;
        @Override
        protected void onPostExecute(String html) {
            if(!html.equals("")){
                groups = new ArrayList<Newsgroup>();

                Document doc = Jsoup.parse(html);
                Elements groupblock = doc.select(".np_index_groupblock:not(:has(div))");
                Elements grouphead = doc.select("div.np_index_grouphead");
                int a = 0;
                for (Element div : groupblock) {
                    Newsgroup temp = new Newsgroup();
                    temp.name = grouphead.get(a++).text();
                    Elements rews = div.select("a");
                    Elements smalls = div.select("small");
                    int b = 0;
                    for (Element link : rews){
                        String color = "";
                        if(smalls.get(b).select("font").size()>0)
                            color = smalls.get(b).select("font").attr("color");
                        temp.addUrl(link.text(), link.attr("href"),smalls.get(b++).text(),color);
                    }
                    groups.add(temp);

                }
                Log.d("size", groups.size()+"");
                adapter = new ExpandableListAdapter(getActivity().getApplicationContext(),groups);
                listview.setAdapter(adapter);
                for(int x = 0; x < groups.size(); x++)
                    listview.expandGroup(x);
                listview.setGroupIndicator(null);
                listview.setVisibility(View.VISIBLE);
                bar.setVisibility(View.GONE);
                listview.setOnChildClickListener(new ExpandableListView.OnChildClickListener()
                {
                    @Override
                    public boolean onChildClick(ExpandableListView parent, View v, int group_position, int child_position, long id)
                    {
                        //Toast.makeText(getActivity().getApplicationContext(), groups.get(group_position).getUrl(child_position).url, Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(getActivity(), GroupMessages.class);
                        intent.putExtra("name",groups.get(group_position).getUrl(child_position).name);
                        intent.putExtra("link",groups.get(group_position).getUrl(child_position).url);
                        startActivity(intent);
                        return false;
                    }
                });
            }else{
                Toast.makeText(getActivity().getApplicationContext(),R.string.network_problem,Toast.LENGTH_SHORT).show();
                getActivity().finish();
            }

        }


    }

    public class ExpandableListAdapter extends BaseExpandableListAdapter {

        private final Context _context;
        private final List<Newsgroup> groups;

        public ExpandableListAdapter(Context context, List<Newsgroup> _groups) {
            this._context = context;
            this.groups = _groups;
        }

        @Override
        public Object getChild(int groupPosition, int childPosititon) {
            return this.groups.get(groupPosition).getUrl(childPosititon);
        }

        @Override
        public long getChildId(int groupPosition, int childPosition) {
            return childPosition;
        }

        @Override
        public View getChildView(int groupPosition, final int childPosition,
                                 boolean isLastChild, View convertView, ViewGroup parent) {

            final String childText = getChild(groupPosition, childPosition).toString();

            if (convertView == null) {
                LayoutInflater infalInflater = (LayoutInflater) this._context
                        .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                convertView = infalInflater.inflate(R.layout.list_item, null);
            }

            TextView txtListChild = (TextView) convertView
                    .findViewById(R.id.lblist_item);

            txtListChild.setText(Html.fromHtml(childText));
            return convertView;
        }

        @Override
        public int getChildrenCount(int groupPosition) {
            return this.groups.get(groupPosition)
                    .getSize();
        }

        @Override
        public Object getGroup(int groupPosition) {
            return this.groups.get(groupPosition);
        }

        @Override
        public int getGroupCount() {
            return this.groups.size();
        }

        @Override
        public long getGroupId(int groupPosition) {
            return groupPosition;
        }

        @Override
        public View getGroupView(int groupPosition, boolean isExpanded,
                                 View convertView, ViewGroup parent) {
            String headerTitle = getGroup(groupPosition).toString();
            if (convertView == null) {
                LayoutInflater infalInflater = (LayoutInflater) this._context
                        .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                convertView = infalInflater.inflate(R.layout.list_group, null);
            }

            TextView lblListHeader = (TextView) convertView
                    .findViewById(R.id.lblistgroup);
            lblListHeader.setTypeface(null, Typeface.BOLD);
            lblListHeader.setText(headerTitle);

            return convertView;
        }

        @Override
        public boolean hasStableIds() {
            return false;
        }

        @Override
        public boolean isChildSelectable(int groupPosition, int childPosition) {
            return true;
        }

    }

}
