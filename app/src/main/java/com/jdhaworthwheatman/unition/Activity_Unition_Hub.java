package com.jdhaworthwheatman.unition;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.common.collect.Lists;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

public class Activity_Unition_Hub extends AppCompatActivity {

    FirebaseAuth mAuth;
    FirebaseFirestore db = FirebaseFirestore.getInstance();

    //ArrayList of ArrayLists
    List<String> message_ids = Lists.newArrayList();
    List<String> message_titles = Lists.newArrayList();

    //find the help frame
    FrameLayout fl_help;
    FrameLayout fl_settings;

    String message_title;

    //fragment loader
    private boolean loadFagment(Fragment fragment){
        if(fragment!=null){
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container_hub,fragment).commit();
            return true;
        }
        return false;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTheme(R.style.Laura);
        setContentView(R.layout.activity__unition__hub);

        loadFagment(new Frag_Hub_Home());

        fl_help = findViewById(R.id.frame_layout_help);
        fl_settings = findViewById(R.id.frame_layout_settings);

        //get fragment manager
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        //add fragment to frame
        ft.add(R.id.frame_layout_help, new Frag_Help());
        ft.add(R.id.frame_layout_settings, new Frag_Settings());
        ft.commit();

        //documentref for my account + shared prefs
        final SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        final SharedPreferences.Editor editor = sharedPreferences.edit();
        final String uni_val = sharedPreferences.getString("user_uni",null);
        final String ID = sharedPreferences.getString("ID",null);
        editor.putInt("page_index",0).apply();

        DocumentReference my_docRef = db.collection("Unition").
                document(uni_val).collection("Users").document(ID);

        my_docRef.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                //set user rating
                try{
                    List<Long> rating_list = (List<Long>) documentSnapshot.get("rating");
                    float rating_val = rating_list.get(0)/rating_list.get(1);
                    editor.putFloat("my_rating",rating_val).apply();
                } catch(Exception e){
                    editor.putFloat("my_rating",0).apply();
                }
            }
        });
    }

    @Override
    public void onResume(){
        super.onResume();

        final SharedPreferences sharedPreferences = PreferenceManager.
                getDefaultSharedPreferences(getBaseContext());
        final String uni_val = sharedPreferences.getString("user_uni",null);
        final SharedPreferences.Editor editor = sharedPreferences.edit();
        int page_index = sharedPreferences.getInt("page_index",0);

        if(page_index==1){
            loadFagment(new Frag_Hub_Profile());
        };

        //initialise authentication variables
        mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        final String ID = sharedPreferences.getString("ID",null);

        //Initialise FireBase ref for links/ connections
        CollectionReference collection_my_links = db.collection("Unition").document(uni_val).
                collection("Users").document(ID).collection("Links");
        CollectionReference collection_my_messages = db.collection("Unition").
                document(uni_val).collection("Messages");

        //Find my CONFIRMED Links from FireBase and put them in Shared Prefs
        collection_my_links.whereEqualTo("confirmed",true).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if(task.isSuccessful()){
                    ArrayList<String> all_my_links_ids = new ArrayList<String>();
                    for (QueryDocumentSnapshot document : task.getResult()) {
                        String link_val = document.getId();
                        all_my_links_ids.add(link_val);
                    }
                    editor.putInt("Links_length_confirmed",all_my_links_ids.size()).apply();

                    for(int i=0;i<all_my_links_ids.size();i++){

                        final String link_user_id_val = all_my_links_ids.get(i);
                        DocumentReference link_doc_ref = FirebaseFirestore.getInstance().
                                collection("Unition").document(uni_val).collection("Users").document(link_user_id_val);

                        final int finalI = i;
                        link_doc_ref.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                DocumentSnapshot documentSnapshot = task.getResult();
                                if(documentSnapshot.exists()){
                                    String link_name_val = documentSnapshot.getString("name");
                                    String link_id_val  = documentSnapshot.getId();
                                    editor.putString("Link_id_confirmed_"+ finalI, link_id_val).
                                            putString("Link_name_confirmed_"+ finalI, link_name_val).
                                            putLong("Link_cost_confirmed_"+ finalI, documentSnapshot.getLong("cost")).
                                            apply();
                                }
                            }
                        });
                    }
                }
            }
        });

        //Find PENDING my Links from FireBase and put them in Shared Prefs
        collection_my_links.whereEqualTo("confirmed",false).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if(task.isSuccessful()){
                    ArrayList<String> all_my_links_ids = new ArrayList<String>();
                    for (QueryDocumentSnapshot document : task.getResult()) {
                        String link_val = document.getId().toString();
                        all_my_links_ids.add(link_val);
                    }
                    editor.putInt("Links_length_pending",all_my_links_ids.size()).apply();

                    for(int i=0;i<all_my_links_ids.size();i++){

                        final String link_user_id_val = all_my_links_ids.get(i);
                        DocumentReference link_doc_ref = FirebaseFirestore.getInstance().
                                collection("Unition").document(uni_val).collection("Users").document(link_user_id_val);

                        final int finalI = i;
                        link_doc_ref.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                DocumentSnapshot documentSnapshot = task.getResult();
                                if(documentSnapshot.exists()){
                                    String link_name_val = documentSnapshot.getString("name");
                                    String link_id_val  = documentSnapshot.getId();
                                    editor.putString("Link_id_pending_"+ finalI, link_id_val).
                                            putString("Link_name_pending_"+ finalI, link_name_val).
                                            putLong("Link_cost_pending_"+ finalI, documentSnapshot.getLong("cost")).
                                            apply();
                                }
                            }
                        });
                    }
                }
            }
        });

        //get instant messages from the database
        collection_my_messages.whereArrayContains("included_ids",ID).get().
                addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {

                        message_ids.clear();
                        message_titles.clear();

                        for(QueryDocumentSnapshot document : task.getResult()){
                            if(task.isSuccessful()){
                                List<String> included_ids = (List<String>) document.get("included_ids");
                                List<String> included_names = (List<String>) document.get("included_names");
                                included_ids.remove(ID);
                                included_names.remove(sharedPreferences.getString("user_name",null));
                                //make a message title
                                message_title = "";
                                for(int i=0;i<included_names.size();i++){
                                    String users_id = included_names.get(i);
                                    message_title += users_id+", ";
                                }
                                message_title = message_title.substring(0,message_title.length()-2);
                                message_titles.add(message_title);

                                //have a list of ids of each thread
                                String msg_thrd_id = document.getId();
                                message_ids.add(msg_thrd_id);
                            }
                        }
                        editor.putInt("no_of_threads",message_ids.size());
                        for (int i=0;i<message_ids.size();i++){
                            editor.putString("message_id_"+i, message_ids.get(i)).
                                    putString("message_title_"+i,message_titles.get(i)).
                                    apply();
                        }
                    }
                });

        ImageButton ib_settings = findViewById(R.id.ib_settings);
        ib_settings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fl_settings.setVisibility(View.VISIBLE);
                fl_help.setVisibility(View.GONE);

                ImageButton ib_close_settings = findViewById(R.id.btn_close_settings);
                ib_close_settings.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        fl_settings.setVisibility(View.GONE);
                    }
                });
            }
        });

        //buttons back to home
        ImageButton btn_home1 = findViewById(R.id.btn_home1);
        btn_home1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadFagment(new Frag_Hub_Home());
                editor.putInt("page_index",0).commit();
            }
        });
        Button btn_home = findViewById(R.id.btn_home2);
        btn_home.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadFagment(new Frag_Hub_Home());
                editor.putInt("page_index",0).commit();
            }
        });

        ImageButton btn_my_profile = findViewById(R.id.ib_profile);
        btn_my_profile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                loadFagment(new Frag_Hub_Profile());
                editor.putInt("page_index",1).commit();
            }
        });

        ImageButton ib_help = findViewById(R.id.ib_help);
        ib_help.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                fl_help.setVisibility(View.VISIBLE);

                ImageButton ib_close_help = findViewById(R.id.btn_close_help);
                ib_close_help.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        fl_help.setVisibility(View.GONE);
                    }
                });
            }
        });

        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();

        LinearLayout ll_no_network = findViewById(R.id.ll_no_network);
        if(activeNetworkInfo==null){
            ll_no_network.setVisibility(View.VISIBLE);
        }

    }

    @Override
    public void onBackPressed(){
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        final SharedPreferences.Editor editor = sharedPreferences.edit();
        int page_index = sharedPreferences.getInt("page_index",0);
        FrameLayout fl_settings = findViewById(R.id.frame_layout_settings);
        FrameLayout fl_help = findViewById(R.id.frame_layout_help);
        LinearLayout ll_suggestion = findViewById(R.id.ll_suggestion);

        if(page_index==1){
            loadFagment(new Frag_Hub_Home());
            editor.putInt("page_index",0);
        } else if(fl_settings.getVisibility()==View.VISIBLE){
            fl_settings.setVisibility(View.GONE);
        } else if(fl_help.getVisibility()==View.VISIBLE){
            fl_help.setVisibility(View.GONE);
        } else if (fl_help.getVisibility()==View.GONE &&
                fl_settings.getVisibility()==View.GONE &&
                page_index==0){ finish(); }
        try{
            if (ll_suggestion.getVisibility()==View.VISIBLE){
                ll_suggestion.setVisibility(View.GONE);
            }
        }catch(Exception e){}
    }

}
