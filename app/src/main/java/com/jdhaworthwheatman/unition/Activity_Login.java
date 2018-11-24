package com.jdhaworthwheatman.unition;

import android.app.FragmentManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class Activity_Login extends AppCompatActivity {

    //get FireBase authentication object
    private FirebaseAuth mAuth;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTheme(R.style.AppTheme);
        setContentView(R.layout.activity_login);

        //initialise authentication variables
        mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();

        //find the two frames for registering and logging in
        final FrameLayout fl_login = findViewById(R.id.frame_layout_login);
        final FrameLayout fl_register = findViewById(R.id.frame_layout_register);
        final FrameLayout fl_loading = findViewById(R.id.frame_layout_loading);
        //get fragment manager
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        //add fragment to frame
        ft.add(R.id.frame_layout_login, new Frag_Login());
        ft.add(R.id.frame_layout_register, new Frag_Register());
        ft.add(R.id.frame_layout_loading, new Frag_Loading());
        ft.commit();
        //set initial visibility of views
        fl_login.setVisibility(View.VISIBLE);
        fl_register.setVisibility(View.GONE);
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
        final FrameLayout fl_login = findViewById(R.id.frame_layout_login);
        final FrameLayout fl_register = findViewById(R.id.frame_layout_register);
        final FrameLayout fl_loading = findViewById(R.id.frame_layout_loading);

        //Initialise buttons
        Button btn_to_login = findViewById(R.id.btn_to_login);
        Button btn_to_register = findViewById(R.id.btn_to_register);
        //Initialise login button
        final Button btn_login = findViewById(R.id.btn_login);
        //Initialise Register btn
        final Button btn_register = findViewById(R.id.btn_register);

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

        //Initialise intent to open hub activity
        final Intent login_intent = new Intent(getBaseContext(), Activity_Unition_Hub.class);

        //Button to change the view to login fragment (CONTROLS ALL THE LOGIN FRAGMENT)
        btn_to_login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(fl_login.getVisibility() == View.GONE){
                    editor.putString("user_email",etxt_r_email.getText().toString()).
                            putString("user_name",etxt_r_name.getText().toString()).
                            putInt("user_uni_pos",spnr_r_uni.getSelectedItemPosition()).
                            putString("user_uni",spnr_r_uni.getSelectedItem().toString()).
                            putString("user_degree",etxt_r_degree.getText().toString()).
                            apply();
                }

                // set fragment visibilities (CHANGE FRAGMENT)
                fl_login.setVisibility(View.VISIBLE);
                fl_register.setVisibility(View.GONE);

                //initialise spinner
                spnr_l_uni.setAdapter(adapter);

                //set email on login and spinner pos
                etxt_l_email.setText(sharedPreferences.getString("user_email",null));
                spnr_l_uni.setSelection(sharedPreferences.getInt("user_uni_pos",0));

                //Button to log in an existing user
                btn_login.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        fl_loading.setVisibility(View.VISIBLE);
                        String email_val = etxt_l_email.getText().toString();
                        String pass_val = etxt_l_pass.getText().toString();

                        try {
                            if (!email_val.isEmpty() && !pass_val.isEmpty() && email_val != null && pass_val != null &&
                                    spnr_l_uni.getSelectedItemPosition() != 0) {

                                mAuth.signInWithEmailAndPassword(email_val, pass_val).addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                                    @Override
                                    public void onSuccess(AuthResult authResult) {

                                        //initialise FireBase doc ref
                                        FirebaseUser currentUser = mAuth.getCurrentUser();
                                        String ID = currentUser.getUid();
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
                                                    ArrayList<String> skills_array = new ArrayList<String>();

                                                    try{
                                                        //skills_array = documentSnapshot.getData("skill_list",);
                                                        for(int i=0;i<no_of_skills;i++){
                                                            editor.putString("skill_val_"+i,documentSnapshot.getString("skill_val_"+i)).apply();
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
                                                            putLong("user_cost", cost_val).
                                                            apply();

                                                    editor.putString("user_uni", spnr_l_uni.getSelectedItem().toString()).
                                                            putInt("user_uni_pos",spnr_l_uni.getSelectedItemPosition()).
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
            }
        });

        //Button to change the view to register fragment (CONTROLS ALL THE REGISTER FRAGMENT)
        btn_to_register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //save values from login screen
                if(fl_register.getVisibility() == View.GONE){
                    //save email displayed on one login screen
                    editor.putString("user_email",etxt_l_email.getText().toString()).
                            putString("user_uni",spnr_l_uni.getSelectedItem().toString()).
                            putInt("user_uni_pos",spnr_l_uni.getSelectedItemPosition()).
                            apply();
                }

                // set fragment visibilities (CHANGE FRAGMENT)
                fl_register.setVisibility(View.VISIBLE);
                fl_login.setVisibility(View.GONE);

                //initialise spinner
                spnr_r_uni.setAdapter(adapter);

                //set any saved values for form fields from phone memory
                etxt_r_email.setText(sharedPreferences.getString("user_email",null));
                etxt_r_name.setText(sharedPreferences.getString("user_name",null));
                spnr_r_uni.setSelection(sharedPreferences.getInt("user_uni_pos",0));
                etxt_r_degree.setText(sharedPreferences.getString("user_degree",null));

                btn_register.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        final String email_val = etxt_r_email.getText().toString();
                        final String pass1_val = etxt_r_pass1.getText().toString();
                        String pass2_val = etxt_r_pass2.getText().toString();
                        final String name_val = etxt_r_name.getText().toString();
                        final String uni_val = spnr_r_uni.getSelectedItem().toString();
                        int uni_pos_val = spnr_r_uni.getSelectedItemPosition();
                        final String degree_val = etxt_r_degree.getText().toString();

                        if(pass1_val.equals(pass2_val) && !email_val.isEmpty() && !pass1_val.isEmpty()
                                && !name_val.isEmpty() && !degree_val.isEmpty() && uni_pos_val!=0){

                            fl_loading.setVisibility(View.VISIBLE);
                            mAuth.createUserWithEmailAndPassword(email_val, pass1_val).
                                    addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                                @Override
                                public void onSuccess(AuthResult authResult) {

                                    mAuth.signInWithEmailAndPassword(email_val, pass1_val).
                                            addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                                        @Override
                                        public void onSuccess(AuthResult authResult) {
                                            //Save vals locally
                                            editor.putString("user_uni",spnr_r_uni.getSelectedItem().toString()).
                                                    putString("user_uni",spnr_r_uni.getSelectedItem().toString()).
                                                    putString("user_name",name_val).
                                                    putString("user_degree",degree_val).
                                                    putInt("user_uni_pos",spnr_r_uni.getSelectedItemPosition()).
                                                    apply();
                                            //initialise authentication variables
                                            FirebaseUser currentUser = mAuth.getCurrentUser();
                                            final String ID = currentUser.getUid();
                                            final DocumentReference mDocRef = FirebaseFirestore.getInstance().
                                                    document("Unition/"+uni_val+"/Users/"+ID);
                                            Map<String,Object> dataToSave = new HashMap<String, Object>();
                                            //Define data to save
                                            dataToSave.put("email",email_val);
                                            dataToSave.put("name",name_val);
                                            dataToSave.put("uni",uni_val);
                                            dataToSave.put("degree",degree_val);
                                            dataToSave.put("cost",0);

                                            //Save data to FireBase
                                            mDocRef.set(dataToSave).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @Override
                                                public void onSuccess(Void aVoid) {
                                                    DocumentReference mLinksDocRef = mDocRef.
                                                            collection("Links").document("Myself");
                                                    Map<String,Object> linkDataToSave = new HashMap<String, Object>();
                                                    //Define data to save
                                                    linkDataToSave.put("teaching",false);
                                                    mLinksDocRef.set(linkDataToSave).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                        @Override
                                                        public void onSuccess(Void aVoid) {
                                                            fl_loading.setVisibility(View.GONE);
                                                            finish();
                                                            startActivity(login_intent);
                                                        }
                                                    });
                                                }
                                            });
                                        }
                                    });

                                }
                            });
                        } else if(email_val.isEmpty() || pass1_val.isEmpty() || name_val.isEmpty() || degree_val.isEmpty() || uni_val.isEmpty()){
                            Toast.makeText(getApplicationContext(),
                                    "Please fill in all your details",
                                    Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(getApplicationContext(),
                                    "Passwords do not match",
                                    Toast.LENGTH_SHORT).show();
                        }

                    }
                });
            }
        });

        //Initial button presses to initialise inner buttons
        if(fl_login.getVisibility()==View.VISIBLE){
            btn_to_login.performClick();
        }
        if(fl_register.getVisibility()==View.VISIBLE){
            btn_to_register.performClick();
        }
    }

}
