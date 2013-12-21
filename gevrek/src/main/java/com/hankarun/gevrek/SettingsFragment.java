package com.hankarun.gevrek;


import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

public class SettingsFragment extends Fragment{
    private Button deleteButton;
    private TextView deleteUserName;

    public SettingsFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_settings, container, false);

        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getActivity());

        deleteUserName = (TextView) rootView.findViewById(R.id.delete_user_name);
        deleteUserName.setText(sp.getString("name_user",""));

        deleteButton = (Button) rootView.findViewById(R.id.delete_user);
        deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                deleteUser();
                getActivity().finish();
            }
        });

        return rootView;
    }

    private void deleteUser(){
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getActivity());
        SharedPreferences.Editor edit = sp.edit();

        edit.putString("user_name", "");
        edit.putString("user_password", "");

        edit.putString("name_user","");
        edit.putString("email_user","");


        edit.putBoolean("save_state",false);
        edit.commit();
    }
}
