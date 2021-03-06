package com.hankarun.gevrek;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.ConnectivityManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.ImageView;
import android.widget.LinearLayout;
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
import org.jsoup.select.Elements;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class MessageRead extends FragmentActivity {

    /**
     * The {@link android.support.v4.view.PagerAdapter} that will provide
     * fragments for each of the sections. We use a
     * {@link FragmentPagerAdapter} derivative, which will keep every
     * loaded fragment in memory. If this becomes too memory intensive, it
     * may be best to switch to a
     * {@link android.support.v4.app.FragmentStatePagerAdapter}.
     */
    private SectionsPagerAdapter mSectionsPagerAdapter;

    /**
     * The {@link ViewPager} that will host the section contents.
     */
    private ViewPager mViewPager;

    private int link;
    private ArrayList<CharSequence> tmp;
    private ArrayList<CharSequence> tmp1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message_read);

        Intent iin= getIntent();
        Bundle b = iin.getExtras();
        link = b.getInt("message");


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB){
            getActionBar().setBackgroundDrawable(new ColorDrawable(Color.parseColor("#2f6699")));
            getActionBar().setDisplayHomeAsUpEnabled(true);
            getActionBar().setDisplayShowHomeEnabled(true);
        }
        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        // Set up the ViewPager with the sections adapter.


        tmp = b.getCharSequenceArrayList("list");
        tmp1 = b.getCharSequenceArrayList("headers");



        mViewPager = (ViewPager) findViewById(R.id.pager);
        mViewPager.setAdapter(mSectionsPagerAdapter);
        mViewPager.setOnPageChangeListener(new MyPageChangeListener());
        mViewPager.setCurrentItem(link);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 1) {
            if(resultCode == RESULT_OK){
                //Finish the fragment and refresh the other
                Intent returnIntent = new Intent();
                returnIntent.putExtra("return","reload");
                setResult(RESULT_OK, returnIntent);
                finish();
            }
            if (resultCode == RESULT_CANCELED) {
                //Write your code if there's no result
            }
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

    @Override
    public void onConfigurationChanged(Configuration newConfig){
        super.onConfigurationChanged(newConfig);
    }

    private class MyPageChangeListener extends ViewPager.SimpleOnPageChangeListener {
        @Override
        public void onPageSelected(int position) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
                getActionBar().setTitle(tmp1.get(position));
        }
    }



    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    public class SectionsPagerAdapter extends android.support.v4.app.FragmentPagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            // getItem is called to instantiate the fragment for the given page.
            // Return a PlaceholderFragment (defined as a static inner class below).
            return PlaceholderFragment.newInstance(tmp.get(position).toString());
        }

        @Override
        public int getCount() {
            // Show 3 total pages.
            return tmp.size();
        }


        @Override
        public CharSequence getPageTitle(int position) {
            if(position<mViewPager.getCurrentItem())
                return getString(R.string.previus);
            if(position>mViewPager.getCurrentItem())
                return getString(R.string.next);
            return String.valueOf(position+1) + " / " + String.valueOf(tmp.size());
        }
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment implements JavaAsyncCompleteListener{
        final String link;
        String reply;
        private Bitmap bmps;

        @Override
        public void onPause(){
            super.onPause();

        }


        public void onCreate(Bundle savedInstanceState) {
            // TODO Auto-generated method stub
            super.onCreate(savedInstanceState);
            setHasOptionsMenu(true);
        }

        public static PlaceholderFragment newInstance(String link) {
            PlaceholderFragment fragment = new PlaceholderFragment(link);
            return fragment;
        }

        public PlaceholderFragment(String _link) {
            link = _link;
        }

        public String getReply(){
            return reply;
        }

        @Override
        public void onCreateOptionsMenu(Menu menu,  MenuInflater inflater) {
            inflater.inflate(R.menu.message_read, menu);
            super.onCreateOptionsMenu(menu, inflater);
        }



        @Override
        public boolean onOptionsItemSelected(MenuItem item) {
            // Handle action bar item clicks here. The action bar will
            // automatically handle clicks on the Home/Up button, so long
            // as you specify a parent activity in AndroidManifest.xml.
            int id = item.getItemId();
            if (id == R.id.action_settings) {
                //if(task.getStatus() == AsyncTask.Status.FINISHED){
                    Intent intent = new Intent(getActivity(), PostActivity.class);

                    intent.putExtra("reply", reply);
                    getActivity().startActivityForResult(intent,1);

                    return true;
                //}
            }
            return super.onOptionsItemSelected(item);
        }

        TextView from;
        TextView date;
        WebView body;
        ImageView avatar;
        LinearLayout lm1;
        LinearLayout lm2;
        ProgressBar progressBar;

        String title;

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_message_read, container, false);
            //TextView textView = (TextView) rootView.findViewById(R.id.section_label);
            //textView.setText(Integer.toString(getArguments().getInt(ARG_SECTION_NUMBER)));
            from = (TextView) rootView.findViewById(R.id.from_text);
            date = (TextView) rootView.findViewById(R.id.date_text);
            body = (WebView) rootView.findViewById(R.id.body_view);
            avatar = (ImageView) rootView.findViewById(R.id.authoravatar);

            lm1 = (LinearLayout) rootView.findViewById(R.id.lm1);
            lm2 = (LinearLayout) rootView.findViewById(R.id.lm2);
            progressBar = (ProgressBar) rootView.findViewById(R.id.messageReadProgress);


            startTask();
            //task = new LoadMessages();
            //task.execute();
            return rootView;
        }

        //Async Task for url fetch
        private void startTask(){
            List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(1);
            new PageFetchAsync(this,HttpPages.group_page+link,getActivity()).execute(nameValuePairs);
        }

        private class GetAvatar extends AsyncTask<String, Void, Void>{
            Bitmap bmp;
            @Override
            protected Void doInBackground(String... strings) {
                try {
                    URL url = new URL(strings[0]);
                    bmp = BitmapFactory.decodeStream(url.openConnection().getInputStream());
                }catch (Exception e){

                }
                return null;
            }

            @Override
            protected  void onPostExecute(Void voids){
                avatar.setImageBitmap(bmp);
            }
        }

        private void avatarCheck(Document doc){
            //Check for options
            SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(getActivity().getApplicationContext());

            switch (settings.getInt("avatar_method",0)){
                case 2:
                    if(InternetConnection.isConnected(getActivity().getApplicationContext())){
                        Elements heads = doc.select("tbody").select("td");
                        new GetAvatar().execute(heads.get(0).select("a").attr("href"));
                    }
                    break;
                case 1:
                    if(InternetConnection.isConnectedFast(getActivity().getApplicationContext())){
                        Elements heads = doc.select("tbody").select("td");
                        new GetAvatar().execute(heads.get(0).select("a").attr("href"));
                    }
                    break;
                case 0:
                    if(InternetConnection.isConnectedWifi(getActivity().getApplicationContext())){
                        Elements heads = doc.select("tbody").select("td");
                        new GetAvatar().execute(heads.get(0).select("a").attr("href"));
                    }
                    break;
            }

        }

        @Override
        public void onTaskComplete(String html) {
            if(!html.equals("")){
                Document doc = Jsoup.parse(html);

                avatarCheck(doc);

                reply = doc.select("a.np_button").attr("href");

                String tmp = doc.select("div.np_article_header").text();
                int sbb = tmp.indexOf("Subject:");
                int fbb = tmp.indexOf("From:");
                int dbb = tmp.indexOf("Date:");
                String attach = "";
                if(tmp.indexOf("Attachments:")>0){
                    attach = getString(R.string.attachments) + doc.select("div.np_article_header").select("a").get(1).toString();
                }

                title = tmp.substring(sbb +9, fbb -1);

                String fpps = tmp.substring(fbb,tmp.length());

                from.setText(fpps.substring(6, fpps.indexOf("(") - 1)); //author
                date.setText(tmp.substring(dbb + 6, dbb+20)); //date

                Elements bod  = doc.select("div.np_article_body");
                String start = "<html><head><meta http-equiv='Content-Type' content='text/html' charset='UTF-8' /></head><body>";
                String end = "</body></html>";
                body.loadData(start + attach + bod.toString() + end, "text/html; charset=UTF-8", null);
                body.setBackgroundColor(0x00000000);
                lm1.setVisibility(View.VISIBLE);
                lm2.setVisibility(View.VISIBLE);
                progressBar.setVisibility(View.GONE);
            }else{
                Toast.makeText(getActivity().getApplicationContext(),R.string.network_problem,Toast.LENGTH_SHORT).show();
                getActivity().finish();
            }


        }
    }
}
