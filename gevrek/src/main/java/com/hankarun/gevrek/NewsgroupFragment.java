package com.hankarun.gevrek;

import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.http.NameValuePair;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.List;


public class NewsgroupFragment extends Fragment implements JavaAsyncCompleteListener{
    private ExpandableListView listview;
    private ExpandableListAdapter adapter;

    @Override
    public void onPause(){
        super.onPause();
        //Something must be done.
        /*if(task != null)
            task.cancel(true);
        if(task != null)
            task.cancel(true);*/
    }

    private ProgressBar bar;

    public void reload(){
        listview.setVisibility(View.GONE);
        bar.setVisibility(View.VISIBLE);
        startTask();
    }


    public NewsgroupFragment() {
    }

    public void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.group_messages, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_settings) {
            //if(task.getStatus() == AsyncTask.Status.FINISHED){
                Intent intent = new Intent(getActivity(), NewsGroupEdit.class);
                getActivity().startActivityForResult(intent, 1);
                return true;
            //}
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        startTask();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.news_group_fragment, container, false);

        listview = (ExpandableListView) rootView.findViewById(R.id.expandableListView);
        bar = (ProgressBar) rootView.findViewById(R.id.progressBar);

        return rootView;
    }

    private List<Newsgroup> groups;

    @Override
    public void onTaskComplete(String html) {
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
    //Async Task for url fetch
    private void startTask(){
        List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(1);
        new PageFetchAsync(this,HttpPages.left_page,getActivity()).execute(nameValuePairs);
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
