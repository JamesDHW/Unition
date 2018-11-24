package com.jdhaworthwheatman.unition;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class Activity_Edit_Profile extends AppCompatActivity {

    FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity__edit__profile);

        Button btn_save_profile = (Button) findViewById(R.id.btn_save_my_profile);

        //find edit texts
        final EditText etxt_name = findViewById(R.id.etxt_edit_my_name);
        final EditText etxt_degree = findViewById(R.id.etxt_edit_my_degree);
        final TextView txt_skills = findViewById(R.id.tv_edit_my_skills);
        final EditText etxt_bio = findViewById(R.id.etxt_edit_my_bio);
        final EditText etxt_cost = findViewById(R.id.etxt_edit_my_cost);

        //find the two frames for registering and logging in
        final FrameLayout fl_skills = findViewById(R.id.frame_layout_skills_chooser);
        //get fragment manager
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        //add fragment to frame
        ft.add(R.id.frame_layout_skills_chooser, new Frag_Update_Skills());
        ft.commit();
        //set initial visibility of views
        fl_skills.setVisibility(View.GONE);

        //initialise shared preferences
        final SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        final SharedPreferences.Editor editor = sharedPreferences.edit();

        final String uni_val = sharedPreferences.getString("user_uni",null);

        String name_val = sharedPreferences.getString("user_name", "YourName");
        String degree_val = sharedPreferences.getString("user_degree", "YourDegree");
        String bio_val = sharedPreferences.getString("user_bio", "-");
        final long cost_val = sharedPreferences.getLong("user_cost", 0);
        final int skills_size_val = sharedPreferences.getInt("user_skills_size", 0);

        String skills_string = "My Skills: ";
        for(int i=0;i<skills_size_val;i++ ){
            skills_string += sharedPreferences.getString("skill_val_"+i,null)+", ";
        }
        skills_string = skills_string.substring(0,skills_string.length()-2);

        etxt_name.setText(name_val, TextView.BufferType.EDITABLE);
        etxt_degree.setText(degree_val, TextView.BufferType.EDITABLE);
        txt_skills.setText(skills_string);
        etxt_bio.setText(bio_val, TextView.BufferType.EDITABLE);
        etxt_cost.setText(String.valueOf(cost_val), TextView.BufferType.EDITABLE);

        //SKILLS CHECKBOXES
        final Button btn_begin_update_skills = findViewById(R.id.btn_begin_update_skills);
        btn_begin_update_skills.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //sets frame visible
                fl_skills.setVisibility(View.VISIBLE);

                //put the keyboard away
                try{
                    InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(btn_begin_update_skills.getWindowToken(), 0);
                } catch(Exception e){}

                //get list of possible skills
                final String[] all_skills_list = getResources().getStringArray(R.array.skills_list);
                //find the linear layout
                final LinearLayout sv_skills = findViewById(R.id.ll_skills);
                for(int i=0;i<all_skills_list.length;i++){
                    CheckBox cb = new CheckBox(getBaseContext());
                    cb.setText(all_skills_list[i].toString());
                    cb.setTextColor(getResources().getColor(R.color.white));
                    sv_skills.addView(cb);
                }

                //button to update and exit the view
                Button btn_update_skills = findViewById(R.id.btn_update_skills);
                btn_update_skills.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        //define a variable to find how many items are selected
                        int checked_size=0;
                        for(int i=0;i<all_skills_list.length;i++){
                            View nextChild = sv_skills.getChildAt(i);
                            if(nextChild instanceof CheckBox){
                                if(((CheckBox) nextChild).isChecked()){
                                    String skill_val = ((CheckBox) nextChild).getText().toString();
                                    editor.putString("skill_val_"+checked_size,skill_val).apply();
                                    checked_size ++;
                                }
                            }
                        }
                        //save how many items are checked
                        editor.putInt("user_skills_size",checked_size).apply();
                        //remove the frame
                        fl_skills.setVisibility(View.GONE);

                        //Redraw the skills TV
                        String skills_string = "My Skills: ";
                        for(int i=0;i<sharedPreferences.getInt("user_skills_size", 0);i++ ){
                            skills_string += sharedPreferences.getString("skill_val_"+i,null)+", ";
                        }
                        skills_string = skills_string.substring(0,skills_string.length()-2);
                        txt_skills.setText(skills_string);

                    }
                });
            }
        });

        btn_save_profile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String name_val = etxt_name.getText().toString();
                String degree_val = etxt_degree.getText().toString();
                String bio_val = etxt_bio.getText().toString();
                long cost_val = Long.valueOf(etxt_cost.getText().toString());

                //update shared preferences
                editor.putString("user_name", name_val).
                        putString("user_degree", degree_val).
                        putString("user_bio", bio_val).
                        putLong("user_cost", cost_val).
                        apply();

                //save data to FireBase
                mAuth = FirebaseAuth.getInstance();
                FirebaseUser currentUser = mAuth.getCurrentUser();
                String ID = currentUser.getUid();
                final DocumentReference mDocRef = FirebaseFirestore.getInstance().
                        document("Unition/"+uni_val+"/Users/"+ID);
                final Map<String,Object> dataToSave = new HashMap<String, Object>();
                dataToSave.put("name",name_val);
                dataToSave.put("degree",degree_val);
                dataToSave.put("bio",bio_val);
                dataToSave.put("cost",cost_val);
                dataToSave.put("no_of_skills",sharedPreferences.getInt("user_skills_size",0));
                ArrayList<String> skills_array_list= new ArrayList<String>();
                for(int i=0;i<sharedPreferences.getInt("user_skills_size",0);i++){
                    skills_array_list.add(sharedPreferences.getString("skill_val_"+i,null));
                }
                dataToSave.put("skill_list",skills_array_list);
                mDocRef.set(dataToSave).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Intent intent = new Intent(getBaseContext(), Activity_Unition_Hub.class);
                        Toast.makeText(getBaseContext(), "Profile saved.", Toast.LENGTH_SHORT).show();
                        finish();
                        startActivity(intent);
                    }
                });
            }
        });
    }

    @Override
    public void onBackPressed(){
        FrameLayout fl_skills = findViewById(R.id.frame_layout_skills_chooser);
        if(fl_skills.getVisibility()==View.VISIBLE){
            fl_skills.setVisibility(View.GONE);
        } else{
            finish();
        }
    }
}
