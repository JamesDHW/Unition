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
        final String[] all_skills_list = getResources().getStringArray(R.array.skills_list);

        //shared prefs
        final SharedPreferences sharedPreferences = PreferenceManager.
                getDefaultSharedPreferences(getContext());
        final SharedPreferences.Editor editor = sharedPreferences.edit();
        final String ID = sharedPreferences.getString("ID",null);
        final String my_degree_val = sharedPreferences.getString("user_degree","No Degree Specified");

        final DocumentReference suggestioinRef = db.collection("Unition").
                document("Skill Suggestions");


        ImageButton ib_suggestion = myView.findViewById(R.id.ib_suggestion);
        ib_suggestion.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                final LinearLayout ll_suggestion = myView.findViewById(R.id.ll_suggestion);
                ll_suggestion.setVisibility(View.VISIBLE);

                ImageButton ib_close_suggestion = myView.findViewById(R.id.btn_close_suggestion);
                ib_close_suggestion.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        ll_suggestion.setVisibility(View.GONE);
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
                        Toast.makeText(getContext(),"Suggestion made (1 per user)",Toast.LENGTH_SHORT);
                        getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

                    }
                });
            }
        });

        //Initialise list arrays
        rowItems = new ArrayList<Row_Item_Skills>();
        ArrayList<Integer> skill_icon_list = new ArrayList<Integer>();
        GridView Gv = myView.findViewById(R.id.gv_search_skills);

        for (int i = 0; i < all_skills_list.length; i++) {
//            skill_icon_list.add(getResources().getIdentifier("icon"+all_skills_list[i].toLowerCase(),"mipmap","com.jdhaworthwheatman.unition"));
//            if(skill_icon_list.get(skill_icon_list.size()-1)==null && !skill_icon_list.isEmpty()){
//                skill_icon_list.remove(skill_icon_list.size()-1);
//                skill_icon_list.add(R.mipmap.iconskills);
//            }
            skill_icon_list.add(R.mipmap.iconskills);
        }

        for (int i = 0; i < all_skills_list.length; i++) {
            Row_Item_Skills item = new Row_Item_Skills(all_skills_list[i],skill_icon_list.get(i));
            rowItems.add(item);
        }
        Adapter_Custom_Skill adapter = new Adapter_Custom_Skill(getContext(), rowItems);
        Gv.setAdapter(adapter);
        Gv.setOnItemClickListener(this);

        Button btn_my_degree_search = myView.findViewById(R.id.btn_search_my_degree);
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
