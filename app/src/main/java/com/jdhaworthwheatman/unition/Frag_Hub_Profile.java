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
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

public class Frag_Hub_Profile extends Fragment {

    View myView;
    FirebaseAuth mAuth;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        myView = inflater.inflate(R.layout.frame_hub_profile, container, false);

        //initialise authentication variables
        mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();

        Button btn_to_edit_profile = myView.findViewById(R.id.btn_edit_profile);
        btn_to_edit_profile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getContext(), Activity_Edit_Profile.class);
                startActivity(intent);
            }
        });

        return myView;
    }

    @Override
    public void onResume()
    {  // After a pause OR at startup
        super.onResume();

        //initialise shared preferences
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());

        String uni_val = sharedPreferences.getString("user_uni", null);

        //initialise authentication variables
        mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        String ID = currentUser.getUid();
        final DocumentReference mDocRef = FirebaseFirestore.getInstance().document("Unition/"+uni_val+"/Users/"+ID);

        //get values from shared preferences
        String name_val = sharedPreferences.getString("user_name", "YourName");
        String degree_val = sharedPreferences.getString("user_degree", "YourDegree");
        String bio_val = sharedPreferences.getString("user_bio", "-");
        long cost_val = sharedPreferences.getLong("user_cost", 0);

        //initialise textviews
        TextView my_name = myView.findViewById(R.id.tv_my_name);
        TextView my_degree = myView.findViewById(R.id.tv_my_degree);
        TextView my_cost = myView.findViewById(R.id.tv_my_cost);
        TextView my_skills = myView.findViewById(R.id.tv_my_skills);
        TextView my_bio = myView.findViewById(R.id.tv_my_bio);
        TextView my_uni = myView.findViewById(R.id.tv_my_uni);

        //put shared preferences values in to views
        my_name.setText(name_val);
        my_degree.setText("I study: "+degree_val);
        my_cost.setText("I charge: £"+String.valueOf(cost_val)+"/hr");
        my_bio.setText(bio_val);
        my_uni.setText(uni_val);

        String skills_string = "";
        for(int i=0;i<sharedPreferences.getInt("user_skills_size", 0);i++ ){
            skills_string += sharedPreferences.getString("skill_val_"+i,"")+", ";
        }
        skills_string = skills_string.substring(0,skills_string.length()-2);
        my_skills.setText("My Skills: "+skills_string);
    }
}
