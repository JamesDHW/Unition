package com.jdhaworthwheatman.unition;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Frag_Hub_Profile extends Fragment {

    View myView;
    FirebaseAuth mAuth;
    List<Row_Item_Skills> rowItems;
    long cost_val;

    private void loadImageFromStorage(String path) {
        try {
            File f=new File(path, "my_profile.jpg");
            Bitmap b = BitmapFactory.decodeStream(new FileInputStream(f));
            ImageView iv_prof_pic = myView.findViewById(R.id.iv_profile_pic);
            iv_prof_pic.setImageBitmap(b);
        }
        catch (FileNotFoundException e){e.printStackTrace();}
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        myView = inflater.inflate(R.layout.frame_hub_profile, container, false);

        final SharedPreferences sharedPreferences = PreferenceManager.
                getDefaultSharedPreferences(getContext());
        final String ID = sharedPreferences.getString("ID",null);

        loadImageFromStorage("/data/user/0/com.jdhaworthwheatman.unition/app_imageDir");

        ImageButton ibtn_to_edit_profile = myView.findViewById(R.id.ibtn_edit_profile);
        ibtn_to_edit_profile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getContext(), Activity_Edit_Profile.class);
                getActivity().finish();
                startActivity(intent);
            }
        });
        ImageButton ibtn_to_inbox = myView.findViewById(R.id.ibtn_inbox);
        ibtn_to_inbox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getContext(), Activity_Inbox.class);
                startActivity(intent);
            }
        });
        ImageButton ibtn_to_links = myView.findViewById(R.id.ibtn_links);
        ibtn_to_links.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getContext(), Activity_Links.class);
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
        cost_val = sharedPreferences.getLong("user_cost", 0);
        float rating_val = sharedPreferences.getFloat("my_rating",0);

        //initialise textviews
        TextView tv_my_name = myView.findViewById(R.id.tv_my_name);
        TextView tv_my_degree = myView.findViewById(R.id.tv_my_degree);
        TextView tv_my_cost = myView.findViewById(R.id.tv_my_cost);
        TextView tv_my_bio = myView.findViewById(R.id.tv_my_bio);
        TextView tv_my_uni = myView.findViewById(R.id.tv_my_uni);
        //rating bar
        RatingBar ratingBar = myView.findViewById(R.id.my_rating_bar);
        ratingBar.setRating(rating_val);

        //put shared preferences values in to views
        tv_my_name.setText(name_val);
        tv_my_degree.setText(degree_val);
        tv_my_cost.setText("I charge: £"+String.valueOf(cost_val/100)+"/hr");
        tv_my_bio.setText(bio_val);
        tv_my_uni.setText(uni_val);

        ArrayList<String> my_skill_list = new ArrayList<>();
        ArrayList<Integer> skill_icon_list = new ArrayList<>();

        for(int i=0;i<sharedPreferences.getInt("user_skills_size", 0);i++ ){
            my_skill_list.add(sharedPreferences.getString("skill_val_"+i,null));
        }

        final String[] list_coding = getResources().getStringArray(R.array.skills_coding);
        final String[] list_design = getResources().getStringArray(R.array.skills_design);
        final String[] list_music = getResources().getStringArray(R.array.skills_music);
        final String[] list_language = getResources().getStringArray(R.array.skills_language);
        final String[] list_cooking = getResources().getStringArray(R.array.skills_cooking);

        for(int i=0; i<my_skill_list.size();i++){
            if(Arrays.asList(list_coding).contains(my_skill_list.get(i))){
                skill_icon_list.add(R.mipmap.ic_coding);
            } else if(Arrays.asList(list_design).contains(my_skill_list.get(i))){
                skill_icon_list.add(R.mipmap.ic_design);
            } else if(Arrays.asList(list_language).contains(my_skill_list.get(i))){
                skill_icon_list.add(R.mipmap.ic_language);
            } else if(Arrays.asList(list_music).contains(my_skill_list.get(i))){
                skill_icon_list.add(R.mipmap.ic_music);
            } else if(Arrays.asList(list_cooking).contains(my_skill_list.get(i))){
                skill_icon_list.add(R.mipmap.ic_cooking);
            }
        }

        ExpandableHeightGridView Gv = myView.findViewById(R.id.gv_my_skills);
        Gv.setExpanded(true);
        rowItems = new ArrayList<Row_Item_Skills>();

        for (int i = 0; i < my_skill_list.size(); i++) {
            Row_Item_Skills item = new Row_Item_Skills(my_skill_list.get(i),skill_icon_list.get(i));
            rowItems.add(item);
        }
        Adapter_Custom_Skill adapter = new Adapter_Custom_Skill(getContext(), rowItems);
        Gv.setAdapter(adapter);
    }
}
