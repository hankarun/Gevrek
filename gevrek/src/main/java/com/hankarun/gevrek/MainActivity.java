package com.hankarun.gevrek;

import android.app.ActionBar;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.support.v4.widget.DrawerLayout;

public class MainActivity extends FragmentActivity
        implements NavigationDrawerFragment.NavigationDrawerCallbacks {

    private NavigationDrawerFragment mNavigationDrawerFragment;

    private CharSequence mTitle;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB)
            getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig){
        super.onConfigurationChanged(newConfig);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        int id = item.getItemId();
        if (id == R.id.section1) {
            Fragment fragment = new NewsgroupFragment();
            fragment.setHasOptionsMenu(true);
            fragmentManager.beginTransaction()
                    .replace(R.id.container, fragment, "group").commit();

            return true;
        }
        if (id == R.id.section2) {
            Fragment fragment1 = new CoursesFragment();
            fragment1.setHasOptionsMenu(true);
            fragmentManager.beginTransaction()
                    .replace(R.id.container, fragment1, "lecture").commit();

            return true;
        }
        if(id == R.id.section3) {
            deleteUser();
            finish();
            return true;
        }
        if (id == R.id.section4) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            setContentView(R.layout.activity_main);
            mNavigationDrawerFragment = (NavigationDrawerFragment)
                    getSupportFragmentManager().findFragmentById(R.id.navigation_drawer);
            mTitle =  getString(R.string.title_section1);

            getActionBar().setBackgroundDrawable(new ColorDrawable(Color.parseColor("#2f6699")));

            // Set up the drawer.
            mNavigationDrawerFragment.setUp(
                    R.id.navigation_drawer,
                    (DrawerLayout) findViewById(R.id.drawer_layout));
        }else{
            setContentView(R.layout.activity_main_legacy);
            if (savedInstanceState == null) {
                getSupportFragmentManager().beginTransaction()
                        .add(R.id.container, new NewsgroupFragment())
                        .commit();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 1) {
            if(resultCode == RESULT_OK){
               //Refresh fragment
                NewsgroupFragment tmp = (NewsgroupFragment) getSupportFragmentManager().findFragmentByTag("group");
                tmp.reload();
            }
            if (resultCode == RESULT_CANCELED) {
                //Write your code if there's no result
            }
        }
    }

    @Override
    public void onNavigationDrawerItemSelected(int position) {
        // update the main content by replacing fragments
        FragmentManager fragmentManager = getSupportFragmentManager();
        switch (position){
            case 1:
                Fragment fragment = new NewsgroupFragment();
                fragment.setHasOptionsMenu(true);
                fragmentManager.beginTransaction()
                        .replace(R.id.container, fragment, "group").commit();
                mTitle = getString(R.string.title_section1);
                break;
            case 2:
                Fragment fragment1 = new CoursesFragment();
                fragment1.setHasOptionsMenu(true);
                fragmentManager.beginTransaction()
                        .replace(R.id.container, fragment1, "lecture").commit();
                mTitle = getString(R.string.title_section2);
                break;
            case 3:
                deleteUser();
            case 4:
                finish();
        }

    }

    private void deleteUser(){
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor edit = sp.edit();

        edit.putString("user_name", "");
        edit.putString("user_password", "");

        edit.putString("name_user","");
        edit.putString("email_user","");


        edit.putBoolean("save_state",false);
        edit.commit();
    }

    public void onSectionAttached(int number) {
        switch (number) {
            case 1:
                mTitle = getString(R.string.title_section1);
                break;
            case 2:
                mTitle = getString(R.string.title_section2);
                break;
            case 3:
                mTitle = getString(R.string.title_section3);
                break;
            case 4:
                mTitle = getString(R.string.title_section4);
                break;
        }

    }

    public void restoreActionBar() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB){
            ActionBar actionBar = getActionBar();
            actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
            actionBar.setDisplayShowTitleEnabled(true);
            actionBar.setTitle(mTitle);
        }
    }

    @Override
    public void onBackPressed() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
            mNavigationDrawerFragment.mDrawerLayout.openDrawer(mNavigationDrawerFragment.mFragmentContainerView);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        //No call for super(). Bug on API Level > 11.
    }

}
