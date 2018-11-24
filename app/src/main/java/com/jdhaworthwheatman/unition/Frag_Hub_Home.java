package com.jdhaworthwheatman.unition;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;

public class Frag_Hub_Home extends Fragment {

    View myView;
    FirebaseAuth mAuth;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        myView = inflater.inflate(R.layout.frame_hub_home, container, false);
        mAuth = FirebaseAuth.getInstance();
        final String[] all_skills_list = getResources().getStringArray(R.array.skills_list);


        Button btn_log_out = myView.findViewById(R.id.btn_logout);
        btn_log_out.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mAuth.signOut();
                Intent intent = new Intent(myView.getContext(), Activity_Login.class);
                //finish();
                startActivity(intent);

                Toast.makeText(getContext(), "Signed Out",
                        Toast.LENGTH_SHORT).show();
            }
        });


        ArrayAdapter<String> adapter = new ArrayAdapter<String>(
                getContext(),
                android.R.layout.simple_list_item_1,
                all_skills_list);

        ListView Lv = (ListView) myView.findViewById(R.id.lv_search_skills);
        Lv.setAdapter(adapter);
        Lv.setOnItemClickListener(
                new AdapterView.OnItemClickListener(){
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        String skill_selected = String.valueOf(parent.getItemAtPosition(position));
                        Intent users_intent = new Intent(getContext(),Activity_Search.class);
                        users_intent.putExtra("skill",skill_selected);
                        startActivity(users_intent);

                    }
                }
        );


        return myView;
    }

}
