package com.jdhaworthwheatman.unition;

import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.PersistableBundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
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
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Activity_User_Profile extends AppCompatActivity implements AdapterView.OnItemClickListener {

    FirebaseAuth mAuth;
    StorageReference mStorageRef;
    String message_id_val = null;
    String users_name_val;
    long users_cost_val;
    List<Long> rating_list;
    ExpandableHeightGridView Gv;
    Bitmap user_profile_pic;

    List<Row_Item_Skills> rowItems;
    File localFile = new File("/data/user/0/com.jdhaworthwheatman.unition/app_imageDir", "Profile.jpg");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity__user__profile);

        //get info out of intent from search activity
        Bundle extras = getIntent().getExtras();
        final String user_id_val = (String) extras.get("users_id");
        mStorageRef = FirebaseStorage.getInstance().getReferenceFromUrl("gs://unition-7c4a8.appspot.com/"+user_id_val+"/profile_pic");

        //initialise authentication variables
        mAuth = FirebaseAuth.getInstance();
        final FirebaseUser currentUser = mAuth.getCurrentUser();
        final String ID = currentUser.getUid();

        //Initialise TextViews
        final TextView tv_to_teach = findViewById(R.id.tv_to_teach);
        final TextView tv_users_name = findViewById(R.id.tv_user_name);
        final TextView tv_users_degree = findViewById(R.id.tv_user_degree);
        final TextView tv_users_bio = findViewById(R.id.tv_user_bio);
        final TextView tv_users_cost = findViewById(R.id.tv_user_cost);
        final TextView tv_users_uni = findViewById(R.id.tv_user_uni);
        //rating bar
        final RatingBar ratingBar = findViewById(R.id.user_rating_bar);

        //initialise sharedPreferences
        final SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        final SharedPreferences.Editor editor = sharedPreferences.edit();

        //set uni
        final String uni_val = sharedPreferences.getString("user_uni",null);
        tv_users_uni.setText(uni_val);

        //initialise database references
        final FirebaseFirestore db = FirebaseFirestore.getInstance();
        final DocumentReference users_document = db.collection("Unition").document(uni_val).collection("Users").document(user_id_val);

        final ImageButton btn_link_up = findViewById(R.id.btn_link_up);
        final ImageButton btn_de_link = findViewById(R.id.btn_de_link);
        final LinearLayout ll_de_link = findViewById(R.id.ll_de_link);
        final LinearLayout ll_link_up = findViewById(R.id.ll_link_up);
        final LinearLayout ll_message = findViewById(R.id.ll_message);
        final LinearLayout ll_to_teach = findViewById(R.id.ll_to_teach);
        final ImageButton btn_to_teach = findViewById(R.id.btn_to_teach);
        final LinearLayout ll_progress = findViewById(R.id.progressBar_user);

        Gv = findViewById(R.id.gv_user_skills);
        Gv.setOnItemClickListener(this);

        final ImageView iv_profile = findViewById(R.id.iv_profile_pic);

        mStorageRef.getFile(localFile).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                try {
                    user_profile_pic = BitmapFactory.decodeStream(new FileInputStream(localFile));
                    iv_profile.setImageBitmap(user_profile_pic);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                ll_progress.setVisibility(View.GONE);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                ll_progress.setVisibility(View.GONE);
            }
        });

        users_document.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {

                users_name_val = documentSnapshot.getString("name");
                String users_degree_val = documentSnapshot.getString("degree");
                String users_bio_val = documentSnapshot.getString("bio");
                users_cost_val = documentSnapshot.getLong("cost");

                tv_users_name.setText(users_name_val);

                //set user rating
                try{
                    rating_list = (List<Long>) documentSnapshot.get("rating");
                    float rating_val = rating_list.get(0)/rating_list.get(1);
                    ratingBar.setRating(rating_val);
                    for (int i = 0; i < 2; i++) {
                        editor.putLong("users_rating"+i,rating_list.get(i)).
                                apply();
                    }
                } catch(Exception e){
                    ratingBar.setRating(0);
                }

                try{
                    ArrayList<Integer> skill_icon_list = new ArrayList<>();
                    List<String> skills_array_list = (List<String>) documentSnapshot.get("skill_list");
                    rowItems = new ArrayList<Row_Item_Skills>();

                    final String[] list_coding = getResources().getStringArray(R.array.skills_coding);
                    final String[] list_design = getResources().getStringArray(R.array.skills_design);
                    final String[] list_music = getResources().getStringArray(R.array.skills_music);
                    final String[] list_language = getResources().getStringArray(R.array.skills_language);
                    final String[] list_cooking = getResources().getStringArray(R.array.skills_cooking);

                    for(int i=0; i<skills_array_list.size();i++){
                        if(Arrays.asList(list_coding).contains(skills_array_list.get(i))){
                            skill_icon_list.add(R.mipmap.ic_coding);
                        } else if(Arrays.asList(list_design).contains(skills_array_list.get(i))){
                            skill_icon_list.add(R.mipmap.ic_design);
                        } else if(Arrays.asList(list_language).contains(skills_array_list.get(i))){
                            skill_icon_list.add(R.mipmap.ic_language);
                        } else if(Arrays.asList(list_music).contains(skills_array_list.get(i))){
                            skill_icon_list.add(R.mipmap.ic_music);
                        } else if(Arrays.asList(list_cooking).contains(skills_array_list.get(i))){
                            skill_icon_list.add(R.mipmap.ic_cooking);
                        } else {
                            skill_icon_list.add(R.mipmap.ic_course);
                        }
                    }

                    for (int i = 0; i < skills_array_list.size(); i++) {
                        Row_Item_Skills item = new Row_Item_Skills(skills_array_list.get(i),skill_icon_list.get(i));
                        rowItems.add(item);
                    }
                    Adapter_Custom_Skill adapter = new Adapter_Custom_Skill(getBaseContext(), rowItems);
                    Gv.setAdapter(adapter);
                    Gv.setExpanded(true);
                } catch(Exception e){
                    final TextView tv_users_skills = findViewById(R.id.tv_user_skills);
                    tv_users_skills.setVisibility(View.VISIBLE);
                    tv_users_skills.setText(users_name_val+" hasn't set any skills yet");
                }

                tv_users_degree.setText(users_degree_val);
                tv_users_cost.setText("£"+String.valueOf(users_cost_val/100)+"/hr");
                tv_users_uni.setText(uni_val);
                tv_users_bio.setText(users_bio_val);

                ImageButton btn_send_msg = findViewById(R.id.btn_send_msg);
                btn_send_msg.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        final CollectionReference collection_my_messages = db.collection("Unition").
                                document(uni_val).collection("Messages");
                        final String my_name = sharedPreferences.getString("user_name",null);

                        //get instant messages from the database
                        collection_my_messages.whereArrayContains("included_ids",user_id_val).get().
                                addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                    @Override
                                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                        for(QueryDocumentSnapshot document : task.getResult()){
                                            if(task.isSuccessful()){
                                                List<String> included_users = (List<String>) document.get("included_ids");
                                                if(included_users.size()==2){
                                                    message_id_val = document.getId();
                                                }
                                            }
                                        }

                                        if(message_id_val==null){
                                            List<String> included_ids = Lists.newArrayList();
                                            List<String> included_names = Lists.newArrayList();

                                            included_ids.add(ID);
                                            included_ids.add(user_id_val);

                                            included_names.add(my_name);
                                            included_names.add(users_name_val);

                                            Map<String,Object> dataToSave = new HashMap<String, Object>();
                                            //Define data to save
                                            dataToSave.put("included_ids",included_ids);
                                            dataToSave.put("included_names",included_names);

                                            DocumentReference my_doc_ref = collection_my_messages.document();
                                            message_id_val = my_doc_ref.getId();
                                            my_doc_ref.set(dataToSave);
                                        }

                                        Intent msg_intent = new Intent(Activity_User_Profile.this,Activity_Messaging.class);
                                        msg_intent.putExtra("message_title",users_name_val);
                                        msg_intent.putExtra("message_id",message_id_val);
                                        startActivity(msg_intent);
                                    }
                                });
                    }
                });

                if(ID.equals(user_id_val)){
                    ll_link_up.setVisibility(View.GONE);
                    ll_message.setVisibility(View.GONE);
                }

                int all_links_size = sharedPreferences.getInt("Links_length_confirmed",0);
                for(int i=0;i<all_links_size;i++){
                    if(user_id_val.equals(sharedPreferences.getString("Link_id_confirmed_"+i,""))){
                        ll_de_link.setVisibility(View.VISIBLE);
                        ll_link_up.setVisibility(View.GONE);
                        ll_to_teach.setVisibility(View.VISIBLE);
                        tv_to_teach.setText("Begin Lesson"  + System.getProperty("line.separator") + "with "+users_name_val);
                    }
                }
            }
        });

        //button to link up with people
        btn_link_up.setOnClickListener(new View.OnClickListener() {
            boolean is_linked = false;
            @Override
            public void onClick(View v) {
                final CollectionReference users_links_collection_ref = users_document.collection("Links");
                final CollectionReference my_links_collection_ref = db.collection("Unition").document(uni_val).collection("Users").document(ID).collection("Links");
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
                                ll_de_link.setVisibility(View.GONE);
                                tv_to_teach.setVisibility(View.GONE);
                                ll_link_up.setVisibility(View.VISIBLE);
                                Toast.makeText(getApplicationContext(), "Link removed",Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                });
            }
        });

        btn_to_teach.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getBaseContext(), Activity_Lesson_Timer.class);
                intent.putExtra("users_name_val",users_name_val);
                intent.putExtra("users_cost_val",users_cost_val);
                intent.putExtra("users_id_val",user_id_val);
                startActivity(intent);
            }
        });
    }

    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

        String skill_name = rowItems.get(position).getSkill_name();
        Intent skill_intent = new Intent(getBaseContext(),Activity_Search.class);
        skill_intent.putExtra("skill",skill_name);
        startActivity(skill_intent);

    }

}
