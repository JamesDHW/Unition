package com.jdhaworthwheatman.unition;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Frag_Hub_Home extends Fragment implements AdapterView.OnItemClickListener{

    View myView;
    FirebaseAuth mAuth;
    List<Row_Item_Skills> rowItems;
    FirebaseFirestore db = FirebaseFirestore.getInstance();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        myView = inflater.inflate(R.layout.frame_hub_home, container, false);
        mAuth = FirebaseAuth.getInstance();

        //shared prefs
        final SharedPreferences sharedPreferences = PreferenceManager.
                getDefaultSharedPreferences(getContext());
        final SharedPreferences.Editor editor = sharedPreferences.edit();
        final String ID = sharedPreferences.getString("ID",null);
        final String my_degree_val = sharedPreferences.getString("user_degree","No Degree Specified");

        final DocumentReference suggestioinRef = db.collection("Unition").
                document("Skill Suggestions");

        final LinearLayout ll_suggestion = myView.findViewById(R.id.ll_suggestion);
        final LinearLayout ll_learn_smth_new = myView.findViewById(R.id.ll_learn_smth_new);


        ImageButton ib_suggestion = myView.findViewById(R.id.ib_suggestion);
        ib_suggestion.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                ll_suggestion.setVisibility(View.VISIBLE);
                ll_learn_smth_new.setVisibility(View.GONE);

                ImageButton ib_close_suggestion = myView.findViewById(R.id.btn_close_suggestion);
                ib_close_suggestion.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        ll_suggestion.setVisibility(View.GONE);
                        ll_learn_smth_new.setVisibility(View.VISIBLE);
                    }
                });

                ImageButton ib_send_suggestion = myView.findViewById(R.id.ib_send_suggestion);
                ib_send_suggestion.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        EditText etxt_suggestion = myView.findViewById(R.id.etxt_suggestion);
                        String suggestion_val = etxt_suggestion.getText().toString();

                        final Map<String,Object> dataToSave = new HashMap<String, Object>();
                        dataToSave.put(ID,suggestion_val);
                        suggestioinRef.update(dataToSave);

                        ll_suggestion.setVisibility(View.GONE);
                        ll_learn_smth_new.setVisibility(View.VISIBLE);
                        Toast.makeText(getContext(),"Suggestion made (1 per user)",Toast.LENGTH_SHORT);
                        getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

                    }
                });
            }
        });

        //Initialise list arrays
        rowItems = new ArrayList<Row_Item_Skills>();
        ArrayList<Integer> skill_icon_list = new ArrayList<Integer>();
        ExpandableHeightGridView Gv = myView.findViewById(R.id.gv_search_skills);

        String [] strings = getResources().getStringArray(R.array.skills_list);
        List<String> all_skills_list = new ArrayList<String>(Arrays.asList(strings));

        final String[] list_coding = getResources().getStringArray(R.array.skills_coding);
        final String[] list_design = getResources().getStringArray(R.array.skills_design);
        final String[] list_music = getResources().getStringArray(R.array.skills_music);
        final String[] list_language = getResources().getStringArray(R.array.skills_language);
        final String[] list_cooking = getResources().getStringArray(R.array.skills_cooking);


        for (int i = 0; i < all_skills_list.size(); i++) {
            if(Arrays.asList(list_coding).contains(all_skills_list.get(i))){
                skill_icon_list.add(R.mipmap.ic_coding);
            } else if(Arrays.asList(list_design).contains(all_skills_list.get(i))){
                skill_icon_list.add(R.mipmap.ic_design);
            } else if(Arrays.asList(list_language).contains(all_skills_list.get(i))){
                skill_icon_list.add(R.mipmap.ic_language);
            } else if(Arrays.asList(list_music).contains(all_skills_list.get(i))){
                skill_icon_list.add(R.mipmap.ic_music);
            } else if(Arrays.asList(list_cooking).contains(all_skills_list.get(i))){
                skill_icon_list.add(R.mipmap.ic_cooking);
            }
        }

        for (int i = 0; i < all_skills_list.size(); i++) {
            Row_Item_Skills item = new Row_Item_Skills(all_skills_list.get(i),skill_icon_list.get(i));
            rowItems.add(item);
        }
        Adapter_Custom_Skill adapter = new Adapter_Custom_Skill(getContext(), rowItems);
        Gv.setAdapter(adapter);
        Gv.setOnItemClickListener(this);
        Gv.setExpanded(true);

        ImageButton btn_my_degree_search = myView.findViewById(R.id.btn_search_my_degree);
        btn_my_degree_search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent skill_intent = new Intent(getContext(),Activity_Search.class);
                skill_intent.putExtra("skill",my_degree_val.toLowerCase().trim());
                startActivity(skill_intent);
            }
        });

        return myView;
    }

    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

        String skill_name = rowItems.get(position).getSkill_name();
        Intent skill_intent = new Intent(getContext(),Activity_Search.class);
        skill_intent.putExtra("skill",skill_name);
        startActivity(skill_intent);

    }

}
