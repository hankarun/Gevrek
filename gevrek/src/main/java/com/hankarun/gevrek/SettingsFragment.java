package com.hankarun.gevrek;


import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.Spinner;
import android.widget.TextView;

public class SettingsFragment extends Fragment implements AdapterView.OnItemSelectedListener {
    private Button deleteButton;
    private TextView deleteUserName;
    private CheckBox wifiCheck;
    private CheckBox gCheck;
    private CheckBox edgeCheck;

    public SettingsFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_settings, container, false);

        Spinner spinner = (Spinner) rootView.findViewById(R.id.spinner);

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getActivity(),
                R.array.data_status, android.R.layout.simple_spinner_item);

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(this);

        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getActivity());

        spinner.setSelection(sp.getInt("avatar_method",0));

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

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int pos, long l) {
        // An item was selected. You can retrieve the selected item using
        // parent.getItemAtPosition(pos)
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getActivity());
        SharedPreferences.Editor editor = sp.edit();
        editor.putInt("avatar_method",pos);
        editor.commit();
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {

    }
}
