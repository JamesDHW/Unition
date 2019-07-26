package com.jdhaworthwheatman.unition;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Activity_Login extends AppCompatActivity {

    //get FireBase authentication object
    private FirebaseAuth mAuth;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTheme(R.style.Laura);
        setContentView(R.layout.activity__login);

        //initialise authentication variables
        mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();

        //find the two frames for registering and logging in
        final FrameLayout fl_loading = findViewById(R.id.frame_layout_loading);
        //get fragment manager
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        //add fragment to frame
        ft.add(R.id.frame_layout_loading, new Frag_Loading());
        ft.commit();
        //set initial visibility of views
        fl_loading.setVisibility(View.GONE);

        //check to see if the user is already logged in
        if (currentUser != null){
            Intent login_intent = new Intent(getBaseContext(), Activity_Unition_Hub.class);
            finish();
            startActivity(login_intent);
        }

    }

    @Override
    public void onResume(){
        super.onResume();

        //initialise authentication variables
        mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();

        //Don't want this activity if already logged in
        if(currentUser!=null){
            finish();
        }

        //get shared preferences
        final SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        final SharedPreferences.Editor editor = sharedPreferences.edit();

        //find the two frames for registering and logging in
        final FrameLayout fl_loading = findViewById(R.id.frame_layout_loading);

        //Initialise buttons
        final Button btn_to_register = findViewById(R.id.btn_register);
        //Initialise login button
        final Button btn_login = findViewById(R.id.btn_login);

        //Initialise register EditTexts
        final EditText etxt_r_email = findViewById(R.id.etxt_r_email);
        final EditText etxt_r_pass1 = findViewById(R.id.etxt_r_pass1);
        final EditText etxt_r_pass2 = findViewById(R.id.etxt_r_pass2);
        final EditText etxt_r_name = findViewById(R.id.etxt_r_name);
        final EditText etxt_r_degree = findViewById(R.id.etxt_r_degree);
        //Initialise login EditTexts
        final EditText etxt_l_pass = findViewById(R.id.etxt_l_pass);
        final EditText etxt_l_email = findViewById(R.id.etxt_l_email);

        //Initialise uni_list resource
        final String[] uni_list = getResources().getStringArray(R.array.uni_list);
        //Initialise spinner objects
        final Spinner spnr_r_uni = findViewById(R.id.spnr_r_uni);
        final Spinner spnr_l_uni = findViewById(R.id.spnr_l_uni);
        final ArrayAdapter<String> adapter;
        adapter = new ArrayAdapter<String>(getBaseContext(), android.R.layout.simple_list_item_1,uni_list);
        spnr_l_uni.setAdapter(adapter);

        //Initialise intent to open hub activity
        final Intent login_intent = new Intent(getBaseContext(), Activity_Unition_Hub.class);

        //Button to log in an existing user
        btn_login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fl_loading.setVisibility(View.VISIBLE);
                final String email_val = etxt_l_email.getText().toString();
                String pass_val = etxt_l_pass.getText().toString();

                try {
                    if (!email_val.isEmpty() && !pass_val.isEmpty() && email_val != null && pass_val != null &&
                            spnr_l_uni.getSelectedItemPosition() != 0) {

                        mAuth.signInWithEmailAndPassword(email_val, pass_val).addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                            @Override
                            public void onSuccess(AuthResult authResult) {

                                //initialise FireBase doc ref
                                FirebaseUser currentUser = mAuth.getCurrentUser();
                                final String ID = currentUser.getUid();
                                editor.putString("user_email",email_val).apply();
                                final DocumentReference mUserDocRef = FirebaseFirestore.getInstance().
                                        document("Unition/"+spnr_l_uni.getSelectedItem().toString()+"/Users/"+ID);

                                //try update shared prefs whilst logging in or registering -
                                //if not possible then user is not at the right uni
                                mUserDocRef.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                                    @Override
                                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                                        if(documentSnapshot.exists()){

                                            String name_val = documentSnapshot.getString("name");
                                            String degree_val = documentSnapshot.getString("degree");
                                            String uni_val = documentSnapshot.getString("uni");
                                            String bio_val = documentSnapshot.getString("bio");
                                            long cost_val = documentSnapshot.getLong("cost");
                                            long no_of_skills = documentSnapshot.getLong("no_of_skills");
                                            List<String> skills_list = (List<String>) documentSnapshot.get("skill_list");

                                            try{
                                                //skills_array = documentSnapshot.getData("skill_list",);
                                                for(int i=0;i<no_of_skills;i++){
                                                    editor.putString("skill_val_"+i,skills_list.get(i)).apply();
                                                }
                                                editor.putInt("user_skills_size", (int) no_of_skills).apply();
                                            } catch(Exception e){
                                                editor.putInt("user_skills_size",0).apply();
                                            }

                                            //update shared prefs from database
                                            editor.putString("user_name", name_val).
                                                    putString("user_degree", degree_val).
                                                    putString("user_uni", uni_val).
                                                    putString("user_bio", bio_val).
                                                    putString("ID",ID).
                                                    putLong("user_cost", cost_val).
                                                    apply();

                                            editor.putString("user_uni", spnr_l_uni.getSelectedItem().toString()).
                                                    putInt("user_uni_pos",spnr_l_uni.getSelectedItemPosition()).
                                                    putInt("nav_index",0).
                                                    apply();
                                            fl_loading.setVisibility(View.GONE);
                                            finish();
                                            startActivity(login_intent);
                                        }else{
                                            mAuth.signOut();
                                            Toast.makeText(getApplicationContext(),
                                                    "Sorry, we couldn't find you at this university.",
                                                    Toast.LENGTH_SHORT).show();
                                            fl_loading.setVisibility(View.GONE);
                                        }
                                    }
                                });


                            }
                        });
                        mAuth.signInWithEmailAndPassword(email_val, pass_val).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                fl_loading.setVisibility(View.GONE);
                                Toast.makeText(getApplicationContext(),
                                        "Could not sign in.", Toast.LENGTH_SHORT).show();
                            }
                        });
                    } else {
                        fl_loading.setVisibility(View.GONE);
                        Toast.makeText(getApplicationContext(),
                                "Please fill in all your details.", Toast.LENGTH_SHORT).show();
                    }
                } catch(Exception e){
                    fl_loading.setVisibility(View.GONE);
                    Toast.makeText(getApplicationContext(),
                            "Sorry, we couldn't find your profile at this location.",
                            Toast.LENGTH_SHORT).show();
                }
            }
        });

        //Button to register activity
        btn_to_register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent reg_intent = new Intent(getBaseContext(),Activity_Register.class);
                startActivity(reg_intent);
            }
        });

    }

}
