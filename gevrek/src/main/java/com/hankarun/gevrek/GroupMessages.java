package com.hankarun.gevrek;

import android.app.Activity;
import android.app.ActionBar;
import android.content.res.Configuration;
import android.support.v4.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentActivity;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.os.Build;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.ImageView;
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
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static android.widget.LinearLayout.LayoutParams;

public class GroupMessages extends FragmentActivity {
    private String group;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_messages);

        Intent iin= getIntent();
        Bundle b = iin.getExtras();
        group = b.getString("link");

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB){
            getActionBar().setTitle(b.getString("name"));

            getActionBar().setDisplayHomeAsUpEnabled(true);
            getActionBar().setBackgroundDrawable(new ColorDrawable(Color.parseColor("#2f6699")));
        }

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, new PlaceholderFragment(group),"groupmessages")
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 1) {
            if(resultCode == RESULT_OK){
                //Refresh fragment
                Bundle b = data.getExtras();
                if(b.getString("reply").equals("reload")){
                    //Refresh the fragment
                    PlaceholderFragment tmp = (PlaceholderFragment) getSupportFragmentManager().findFragmentByTag("groupmessages");
                    tmp.reload();
                }
            }
            if (resultCode == RESULT_CANCELED) {
                //Write your code if there's no result
            }
        }
    }


    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment {
        final String link;
        ListView vies;
        ProgressBar bar;
        String reply;
        LoadMessages task;

        public void reload(){
            bar.setVisibility(View.VISIBLE);
            vies.setVisibility(View.GONE);
            task = new LoadMessages();
            task.execute();
        }

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
                if(task.getStatus() == AsyncTask.Status.FINISHED){
                    Intent intent = new Intent(getActivity(), PostActivity.class);
                    intent.putExtra("reply",reply);
                    getActivity().startActivityForResult(intent,1);
                    return true;
                }
            }
            return super.onOptionsItemSelected(item);
        }

        @Override
        public void onActivityCreated(Bundle savedInstanceState) {
            super.onActivityCreated(savedInstanceState);
            task = new LoadMessages();
            task.execute();
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_group_messages, container, false);
            vies = (ListView) rootView.findViewById(R.id.messagelist);




            bar = (ProgressBar) rootView.findViewById(R.id.messageProgress);

            return rootView;
        }

        private class LoadMessages extends AsyncTask<String, String, String> {


            @Override
            protected String doInBackground(String... args) {
                SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(getActivity());

                String uname = settings.getString("user_name", "");
                String upassword = settings.getString("user_password", "");

                HttpClient client = new DefaultHttpClient();
                HttpPost post = new HttpPost("https://cow.ceng.metu.edu.tr/News/"+link);

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


                return "";
            }

            private List<MessageHeader> array;

            @Override
            protected void onPostExecute(String html) {
                if(!html.equals("")){
                    array = new ArrayList<MessageHeader>();
                    Document doc = Jsoup.parse(html);
                    Elements table = doc.select("table.np_thread_table").select("tr");
                    reply = doc.select("a.np_button").get(0).attr("href");
                    table.remove(0);
                    for(Element s : table){
                        MessageHeader tmp = new MessageHeader();
                        Elements trs = s.select("td");
                        if(s.select("font").size()>0)
                            tmp.color = s.select("font").attr("color");
                        tmp.date = trs.get(0).text();
                        tmp.read = trs.get(1).select("a").attr("class").equals("read");
                        for(Element dd:trs.get(1).select("img"))
                            tmp.images.add(dd.attr("alt"));
                        tmp.header = trs.get(1).text();
                        tmp.href = trs.get(1).select("a").attr("href");
                        tmp.author = trs.get(3).text();
                        array.add(tmp);
                    }
                    MyBaseAdapter adapters = new MyBaseAdapter(getActivity().getApplicationContext(),array);
                    vies.setAdapter(adapters);

                    vies.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                        @Override
                        public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {


                            ArrayList<CharSequence> tmp = new ArrayList<CharSequence>();
                            ArrayList<CharSequence> tmp1 = new ArrayList<CharSequence>();

                            for(int x = 0; x < vies.getAdapter().getCount(); x++){
                                Object o = vies.getItemAtPosition(x);
                                MessageHeader tmps = (MessageHeader) o;
                                tmp.add(tmps.href);
                                tmp1.add(tmps.header);
                            }

                            Intent intent = new Intent(getActivity(), MessageRead.class);

                            intent.putCharSequenceArrayListExtra("list",tmp);
                            intent.putCharSequenceArrayListExtra("headers",tmp1);

                            intent.putExtra("message", i);
                            getActivity().startActivityForResult(intent, 1);

                        }
                    });

                    bar.setVisibility(View.GONE);
                    vies.setVisibility(View.VISIBLE);
                }else{
                    Toast.makeText(getActivity().getApplicationContext(),R.string.network_problem,Toast.LENGTH_SHORT).show();
                    getActivity().finish();
                }

            }


        }

        public class MessageHeader{
            public String header;
            public String date;
            public boolean read;
            public String color;
            public String href;
            public final List<String> images = new ArrayList<String>();
            public String author;
            public String reply;

            public String getImg(){
                String tmp = "";
                for(String a: images)
                    tmp += a;
                return tmp;
            }
        }

        public class MyBaseAdapter extends BaseAdapter{
            final List<MessageHeader> headers;
            final Context context;

            public MyBaseAdapter(Context _context, List<MessageHeader> _headers){
                context = _context;
                headers = _headers;
            }

            @Override
            public int getCount() {
                return headers.size();
            }

            @Override
            public Object getItem(int i) {
                return headers.get(i);
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
                    view = infalInflater.inflate(R.layout.message_item, null);
                }

                TextView body = (TextView) view.findViewById(R.id.body);
                TextView date = (TextView) view.findViewById(R.id.date);
                TextView author = (TextView) view.findViewById(R.id.author);
                LinearLayout layout = (LinearLayout) view.findViewById(R.id.messagelayout);

                String header = "<?xml version=\"1.0\" encoding=\"iso-8859-9\" ?>";
                String imgs = headers.get(i).getImg();
                imgs.replaceAll("/*","-");
                /*for(int x=0; x<imgs.length(); x++){
                    ImageView tmp = new ImageView(context);
                    if(imgs.charAt(x) == ' ')
                        tmp.setImageDrawable(view.getResources().getDrawable(R.drawable.e));
                    else
                    if(imgs.charAt(x) == '*')
                        tmp.setImageDrawable(view.getResources().getDrawable(R.drawable.k2));
                    else
                    if(imgs.charAt(x) == 'o')
                        tmp.setImageDrawable(view.getResources().getDrawable(R.drawable.k1));
                    else
                    if(imgs.charAt(x) == '+')
                        tmp.setImageDrawable(view.getResources().getDrawable(R.drawable.t));
                    else
                    if(imgs.charAt(x) == '-')
                        tmp.setImageDrawable(view.getResources().getDrawable(R.drawable.s));
                    else
                    if(imgs.charAt(x) == '|')
                        tmp.setImageDrawable(view.getResources().getDrawable(R.drawable.l));
                    else
                    if(imgs.charAt(x) == '`')
                        tmp.setImageDrawable(view.getResources().getDrawable(R.drawable.li));

                    LayoutParams params = new LayoutParams(30,55);

                    tmp.setLayoutParams(params);
                    tmp.setFocusable(false);
                    tmp.setFocusableInTouchMode(false);
                    layout.addView(tmp,x+1);
                }*/
                String reads;
                if(headers.get(i).read)
                    reads = "<font color=\"#999900\">"+header + headers.get(i).header+"</font>";
                else
                    reads = "<font color=\"#26598F\">"+header + headers.get(i).header+"</font>";

                if(i % 2 == 0)
                    layout.setBackgroundColor(Color.parseColor("#EEEEEE"));
                else
                    layout.setBackgroundColor(Color.parseColor("#ffffff"));
                body.setText(Html.fromHtml(imgs + " " + reads));
                date.setText(Html.fromHtml("<font color=\""+ headers.get(i).color +"\">" +headers.get(i).date+ "</font>"));
                author.setText(headers.get(i).author);

                return view;
            }
        }
    }


}
