package com.jdhaworthwheatman.unition;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
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

public class Activity_Unition_Hub extends AppCompatActivity
        implements BottomNavigationView.OnNavigationItemSelectedListener{


    FirebaseAuth mAuth;

    //fragment loader
    private boolean loadFagment(Fragment fragment){
        if(fragment!=null){
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container_hub,fragment).commit();
            return true;
        }
        return false;
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
        Fragment fragment = null;

        switch (menuItem.getItemId()) {
            case R.id.navigation_connections:
                fragment = new Frag_Hub_Links();
                menuItem.setChecked(true);
                break;
            case R.id.navigation_inbox:
                fragment = new Frag_Hub_Inbox();
                menuItem.setChecked(true);
                break;
            case R.id.navigation_home:
                fragment = new Frag_Hub_Home();
                menuItem.setChecked(true);
                break;
            case R.id.navigation_profile:
                fragment = new Frag_Hub_Profile();
                menuItem.setChecked(true);
                break;
            case R.id.navigation_payment:
                fragment = new Frag_Hub_Payment();
                menuItem.setChecked(true);
                break;
        }
        loadFagment(fragment);
        return false;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTheme(R.style.AppTheme);
        setContentView(R.layout.activity__unition__hub);

    }

    @Override
    public void onResume(){
        super.onResume();

        //set navigation bar
        BottomNavigationView navigation = findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(this);
        navigation.setSelectedItemId(R.id.navigation_home);

        //initialise authentication variables
        mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        final String ID = currentUser.getUid().toString();
        //get shared preferences
        final SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        final String uni_val = sharedPreferences.getString("user_uni",null);
        final SharedPreferences.Editor editor = sharedPreferences.edit();

        //Initialise FireBase ref for links/ connections
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        CollectionReference collection_my_links = db.collection("Unition").document(uni_val).
                collection("Users").document(ID).collection("Links");

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

        //load fragment
        loadFagment(new Frag_Hub_Home());
    }

}
