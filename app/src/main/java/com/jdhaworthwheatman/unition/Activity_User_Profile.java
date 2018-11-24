package com.jdhaworthwheatman.unition;

import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.common.primitives.Ints;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Activity_User_Profile extends AppCompatActivity {

    FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity__user__profile);

        //find the loading frame
        final FrameLayout fl_loading = findViewById(R.id.frame_layout_loading_user);
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        //add fragment to frame
        ft.add(R.id.frame_layout_loading_user, new Frag_Loading());
        ft.commit();
        //set initial visibility of views
        fl_loading.setVisibility(View.VISIBLE);

        //get info out of intent from search activity
        Bundle extras = getIntent().getExtras();
        final String user_id_val = (String) extras.get("users_id");

        //initialise authentication variables
        mAuth = FirebaseAuth.getInstance();
        final FirebaseUser currentUser = mAuth.getCurrentUser();
        final String ID = currentUser.getUid();

        //Initialise TextViews
        final TextView tv_users_name = findViewById(R.id.tv_user_name);
        final TextView tv_users_degree = findViewById(R.id.tv_user_degree);
        final TextView tv_users_skills = findViewById(R.id.tv_user_skills);
        final TextView tv_users_bio = findViewById(R.id.tv_user_bio);
        final TextView tv_users_cost = findViewById(R.id.tv_user_cost);
        final TextView tv_users_uni = findViewById(R.id.tv_user_uni);

        //initialise sharedPreferences
        final SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

        //set uni
        final String uni_val = sharedPreferences.getString("user_uni",null);
        tv_users_uni.setText(uni_val);

        //initialise database references
        final FirebaseFirestore db = FirebaseFirestore.getInstance();
        final DocumentReference users_document = db.collection("Unition").document(uni_val).collection("Users").document(user_id_val);

        users_document.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                fl_loading.setVisibility(View.GONE);

                String users_name_val = documentSnapshot.getString("name");
                String users_degree_val = documentSnapshot.getString("degree");
                String users_bio_val = documentSnapshot.getString("bio");
                long users_cost_val = documentSnapshot.getLong("cost");

                try{
                    List<String> skills_array_list = (List<String>) documentSnapshot.get("skill_list");
                    String users_skills_val = "Skills: ";
                    for(int i=0;i<skills_array_list.size();i++){
                        users_skills_val.concat(skills_array_list.get(i)+", ");
                    }
                    users_skills_val = users_skills_val.substring(0,users_skills_val.length()-2);
                    tv_users_skills.setText(users_skills_val);
                } catch(Exception e){   }

                tv_users_name.setText(users_name_val);
                tv_users_degree.setText("Studies "+users_degree_val);
                tv_users_cost.setText("£"+String.valueOf(users_cost_val)+"/hr");
                tv_users_uni.setText(uni_val);
                tv_users_bio.setText(users_bio_val);
            }
        });

        Button btn_send_msg = findViewById(R.id.btn_send_msg);
        btn_send_msg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent msg_intent = new Intent(Activity_User_Profile.this,Activity_Messaging.class);
                startActivity(msg_intent);
            }
        });

        final Button btn_link_up = findViewById(R.id.btn_link_up);
        final Button btn_de_link = findViewById(R.id.btn_de_link);
        if(ID.equals(user_id_val)){
            btn_link_up.setVisibility(View.GONE);
            btn_send_msg.setVisibility(View.GONE);
        }

        int all_links_size = sharedPreferences.getInt("Links_length_confirmed",0);
        for(int i=0;i<all_links_size;i++){
            if(user_id_val.equals(sharedPreferences.getString("Link_id_confirmed_"+i,""))){
                btn_de_link.setVisibility(View.VISIBLE);
                btn_link_up.setVisibility(View.GONE);
            }
        }

        //button to link up with people
        btn_link_up.setOnClickListener(new View.OnClickListener() {
            boolean is_linked = false;
            @Override
            public void onClick(View v) {
                final CollectionReference users_links_collection_ref = users_document.collection("Links");
                final CollectionReference my_links_collection_ref = db.collection("Unition").document(uni_val).
                        collection("Users").document(ID).collection("Links");
                users_links_collection_ref.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    //check to see if this user is linked to me
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if(task.isSuccessful()){
                            //if I am in the user's links already
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                String links_id_val = document.getId();
                                if(links_id_val.equals(ID)){
                                    is_linked = true;
                                    break;
                                }
                            }
                            Map<String,Object> dataToSave = new HashMap<String, Object>();
                            //Define data to save
                            dataToSave.put("confirmed",is_linked);
                            dataToSave.put("teaching",false);
                            my_links_collection_ref.document(user_id_val).set(dataToSave).
                                    addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            Toast.makeText(getApplicationContext(), "Link Sent",Toast.LENGTH_SHORT).show();
                                        }
                                    });
                            users_links_collection_ref.document(ID).set(dataToSave).
                                    addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            Toast.makeText(getApplicationContext(), "Link Sent",Toast.LENGTH_SHORT).show();
                                        }
                                    });
                        }
                    }
                });
            }
        });

        //button to de-link
        btn_de_link.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final CollectionReference users_links_collection_ref = users_document.collection("Links");
                final CollectionReference my_links_collection_ref = db.collection("Unition").document(uni_val).
                        collection("Users").document(ID).collection("Links");
                users_links_collection_ref.document(ID).delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        my_links_collection_ref.document(user_id_val).delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                btn_de_link.setVisibility(View.GONE);
                                btn_link_up.setVisibility(View.VISIBLE);
                                Toast.makeText(getApplicationContext(), "Link removed",Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                });



            }
        });



    }
}
