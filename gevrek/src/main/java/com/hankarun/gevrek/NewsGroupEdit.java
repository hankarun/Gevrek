package com.hankarun.gevrek;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.os.Build;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.BaseExpandableListAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ExpandableListView;
import android.widget.Filter;
import android.widget.Filterable;
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
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class NewsGroupEdit extends FragmentActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_news_group_edit);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            getActionBar().setBackgroundDrawable(new ColorDrawable(Color.parseColor("#2f6699")));
        }

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, new GroupListFragment())
                    .commit();
        }
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




    public static class GroupListFragment extends Fragment {
        private ListView listview;
        Loadgroups1 task;
        SendGroups task1;
        GroupListAdapter adapter;
        EditText filterEditText;
        Button send;
        Button cancel;
        ProgressBar bar;

        public GroupListFragment() {
        }

        public class GroupName{
            String name;
            Boolean checked;
            Boolean disabled;
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_news_group_edit, container, false);
            listview = (ListView) rootView.findViewById(R.id.allGroupsList);
            bar = (ProgressBar) rootView.findViewById(R.id.editProgress);

            listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                    GroupName tmp = (GroupName) adapter.getItem(i);

                    if (adapter.names.get(adapter.names.indexOf(tmp)).checked)
                        adapter.names.get(adapter.names.indexOf(tmp)).checked = false;
                    else
                        adapter.names.get(adapter.names.indexOf(tmp)).checked = true;
                    adapter.notifyDataSetChanged();
                }
            });

            send = (Button) rootView.findViewById(R.id.groupsave);
            send.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    task1 = new SendGroups();
                    task1.execute();
                }
            });
            cancel = (Button) rootView.findViewById(R.id.groupcancel);
            cancel.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if(task1 != null)
                        task1.cancel(true);
                    Intent returnIntent = new Intent();
                    getActivity().setResult(RESULT_CANCELED, returnIntent);
                    getActivity().finish();
                }
            });

            filterEditText = (EditText) rootView.findViewById(R.id.groupSearchEdit);

            // Add Text Change Listener to EditText
            filterEditText.addTextChangedListener(new TextWatcher()
            {

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count)
                {
                    // Call back the Adapter with current character to Filter
                    adapter.getFilter().filter(s.toString());
                }

                @Override
                public void beforeTextChanged(CharSequence s, int start, int count,int after)
                {
                }

                @Override
                public void afterTextChanged(Editable s)
                {
                }
            });

            filterEditText.setOnTouchListener(new View.OnTouchListener() {
                final Drawable imgX = getResources().getDrawable(R.drawable.ic_action_remove );
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    // Is there an X showing?
                    if (filterEditText.getCompoundDrawables()[2] == null) return false;
                    // Only do this for up touches
                    if (event.getAction() != MotionEvent.ACTION_UP) return false;
                    // Is touch on our clear button?
                    if (event.getX() > filterEditText.getWidth() - filterEditText.getPaddingRight() - imgX.getIntrinsicWidth()) {
                        filterEditText.setText("");

                        InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Activity.INPUT_METHOD_SERVICE);
                        imm.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0);
                    }
                    return false;
                }
            });

            return rootView;
        }

        public void show(){
            listview.setVisibility(View.VISIBLE);
            filterEditText.setVisibility(View.VISIBLE);
            send.setVisibility(View.VISIBLE);
            cancel.setVisibility(View.VISIBLE);
            bar.setVisibility(View.GONE);
        }

        public void hide(){
            listview.setVisibility(View.GONE);
            filterEditText.setVisibility(View.GONE);
            send.setVisibility(View.GONE);
            cancel.setVisibility(View.GONE);
            bar.setVisibility(View.VISIBLE);
        }


        @Override
        public void onActivityCreated(Bundle savedInstanceState) {
            super.onActivityCreated(savedInstanceState);
            task = new Loadgroups1();
            task.execute();
        }

        private class SendGroups extends AsyncTask<String, String, String> {
            @Override
            protected void onPreExecute(){
                hide();
            }

            @Override
            protected String doInBackground(String... strings) {
                SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(getActivity());

                String uname = settings.getString("user_name", "");
                String upassword = settings.getString("user_password", "");

                HttpClient client = new DefaultHttpClient();
                HttpPost post = new HttpPost("https://cow.ceng.metu.edu.tr/News/setOptions.php");

                try {

                    List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(1);
                    nameValuePairs.add(new BasicNameValuePair("cow_username", uname));
                    nameValuePairs
                            .add(new BasicNameValuePair("cow_password", upassword));
                    nameValuePairs.add(new BasicNameValuePair("cow_login", "login"));

                    nameValuePairs.add(new BasicNameValuePair("submitOptions", "save Options"));



                    String collecs = "";
                    for(GroupName a: adapter.names){
                        if(a.checked)
                            nameValuePairs.add(new BasicNameValuePair("mygroups["+adapter.names.indexOf(a)+"]",a.name));
                            //collecs = collecs + a.name + ",";
                    }
                    //nameValuePairs.add(new BasicNameValuePair("colours[0]","red"));
                    //nameValuePairs.add(new BasicNameValuePair("colours[1]","white"));
                    //nameValuePairs.add(new BasicNameValuePair("colours[2]","black"));
                    //nameValuePairs.add(new BasicNameValuePair("colours[3]","brown"));
                    //nameValuePairs.add(new BasicNameValuePair("mygroups[]",collecs));

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
                Intent returnIntent = new Intent();
                returnIntent.putExtra("type","group");
                getActivity().setResult(RESULT_OK, returnIntent);
                getActivity().finish();
            }
        }


        private class Loadgroups1 extends AsyncTask<String, String, String> {
            @Override
            protected void onPreExecute(){
                hide();
            }


            @Override
            protected String doInBackground(String... args) {
                SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(getActivity());

                String uname = settings.getString("user_name", "");
                String upassword = settings.getString("user_password", "");

                HttpClient client = new DefaultHttpClient();
                HttpPost post = new HttpPost("https://cow.ceng.metu.edu.tr/News/setOptions.php");

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
                if(!html.equals("")){
                    Document doc = Jsoup.parse(html);
                    Elements test = doc.select("input");
                    ArrayList<GroupName> groups = new ArrayList<GroupName>();

                    for(Element e: test){
                        if(e.attr("name").equals("mygroups[]")){
                            GroupName tmp = new GroupName();

                            tmp.name = e.attr("value");
                            tmp.checked = e.hasAttr("checked");
                            tmp.disabled = e.hasAttr("disabled");

                            groups.add(tmp);
                        }
                    }
                    adapter = new GroupListAdapter(getActivity().getApplicationContext(), groups);
                    listview.setAdapter(adapter);
                    show();

                }else{
                    Toast.makeText(getActivity().getApplicationContext(), R.string.network_problem, Toast.LENGTH_SHORT).show();
                    getActivity().finish();
                }

            }
        }



        public class GroupListAdapter extends BaseAdapter implements Filterable {
            Context context;
            public ArrayList<GroupName> names;
            private ArrayList<GroupName> filteredModelItemsArray;
            CustomFilter filter;

            public GroupListAdapter(Context _context, ArrayList<GroupName> _names){
                context = _context;
                names = _names;
                filteredModelItemsArray = new ArrayList<GroupName>();
                filteredModelItemsArray.addAll(names);
            }

            @Override
            public int getCount() {
                return filteredModelItemsArray.size();
            }

            @Override
            public Object getItem(int i) {
                return filteredModelItemsArray.get(i);
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
                    view = infalInflater.inflate(R.layout.group_edit_item, null);
                }

                TextView groupName = (TextView) view.findViewById(R.id.groupNameText);
                CheckBox groupCheck = (CheckBox) view.findViewById(R.id.groupCheckBox);

                GroupName tmp = (GroupName) filteredModelItemsArray.get(i);

                groupName.setText(tmp.name);
                groupCheck.setChecked(tmp.checked);
                groupCheck.setEnabled(!tmp.disabled);

                return view;
            }

            @Override
            public Filter getFilter() {
                if (filter == null){
                    filter = new CustomFilter();
                }
                return filter;
            }


            private class CustomFilter extends Filter {

                @SuppressLint("DefaultLocale")
                @Override
                protected FilterResults performFiltering(CharSequence constraint) {
                    constraint = constraint.toString().toLowerCase();
                    FilterResults result = new FilterResults();
                    if(constraint != null && constraint.toString().length() > 0)
                    {
                        ArrayList<GroupName> filteredItems = new ArrayList<GroupName>();

                        for(int i = 0, l = names.size(); i < l; i++)
                        {
                            String m = names.get(i).name;
                            if(m.contains(constraint))
                                filteredItems.add(names.get(i));
                        }
                        result.count = filteredItems.size();
                        result.values = filteredItems;
                    }
                    else
                    {
                        synchronized(this)
                        {
                            result.values = names;
                            result.count = names.size();
                        }
                    }
                    return result;
                }

                @SuppressWarnings("unchecked")
                @Override
                protected void publishResults(CharSequence constraint, FilterResults results) {
                    filteredModelItemsArray.clear();
                    filteredModelItemsArray.addAll((ArrayList<GroupName>) results.values);
                    notifyDataSetChanged();
                }

            }
        }
    }

}
