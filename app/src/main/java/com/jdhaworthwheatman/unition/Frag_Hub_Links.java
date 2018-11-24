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
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;
import java.util.List;

public class Frag_Hub_Links extends Fragment implements AdapterView.OnItemClickListener {

    View myView;
    FirebaseAuth mAuth;
    List<Row_Item_Links> rowItems_pen;
    List<Row_Item_Links> rowItems_con;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        myView = inflater.inflate(R.layout.frame_hub_links, container, false);

        //initialise authentication variables
        mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        final String ID = currentUser.toString();
        //get shared preferences
        final SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());

        //Initialise list arrays
        rowItems_pen = new ArrayList<Row_Item_Links>();
        rowItems_con = new ArrayList<Row_Item_Links>();


        //Initialise an array of links for current user
        ArrayList<String> all_my_links_ids_confirmed = new ArrayList<String>();
        ArrayList<String> all_my_links_names_confirmed = new ArrayList<String>();
        ArrayList<Integer> all_my_links_costs_confirmed = new ArrayList<Integer>();
        ArrayList<String> all_my_links_ids_pending = new ArrayList<String>();
        ArrayList<String> all_my_links_names_pending = new ArrayList<String>();
        ArrayList<Integer> all_my_links_costs_pending = new ArrayList<Integer>();


        //get the number of confirmed and pending links
        int size_pen = sharedPreferences.getInt("Links_length_pending", 0);
        int size_con = sharedPreferences.getInt("Links_length_confirmed", 0);

        //define textviews
        TextView tv_links_pending = myView.findViewById(R.id.tv_pending_links);
        TextView tv_links_confirmed = myView.findViewById(R.id.tv_confirmed_links);
        if(size_con==0 && size_pen==0){
            TextView tv_no_links_yet = myView.findViewById(R.id.tv_no_links_yet);
            tv_no_links_yet.setVisibility(View.VISIBLE);
            tv_links_pending.setVisibility(View.GONE);
            tv_links_confirmed.setVisibility(View.GONE);
        } else if(size_pen==0){
            tv_links_pending.setVisibility(View.GONE);
        } else if(size_con==0){
            tv_links_confirmed.setVisibility(View.GONE);
        }

        for(int i=0;i<size_pen;i++) {
            if(ID!=sharedPreferences.getString("Link_id_pending_" + i, null)){
                all_my_links_ids_pending.add(sharedPreferences.getString
                        ("Link_id_pending_" + i, null));
                all_my_links_names_pending.add(sharedPreferences.getString
                        ("Link_name_pending_" + i, null));
                all_my_links_costs_pending.add((int) sharedPreferences.getLong
                        ("Link_cost_pending_" + i, 0));
            }
        }
        for(int i=0;i<size_con;i++) {
            if(ID!=sharedPreferences.getString("Link_id_confirmed_" + i, null)){
                all_my_links_ids_confirmed.add(sharedPreferences.getString
                        ("Link_id_confirmed_" + i, null));
                all_my_links_names_confirmed.add(sharedPreferences.getString
                        ("Link_name_confirmed_" + i, null));
                all_my_links_costs_confirmed.add((int) sharedPreferences.getLong
                        ("Link_cost_confirmed_" + i, 0));
            }
        }

        //find lists
        ListView mylistview_pen = myView.findViewById(R.id.lv_pending_links);
        ListView mylistview_con = myView.findViewById(R.id.lv_confirmed_links);

        for (int i = 0; i < all_my_links_ids_pending.size(); i++) {
            Row_Item_Links item_pen = new Row_Item_Links( all_my_links_names_pending.get(i),
                    all_my_links_ids_pending.get(i), all_my_links_costs_pending.get(i));
            rowItems_pen.add(item_pen);
        }
        Adapter_Custom_Links adapter_pen = new Adapter_Custom_Links(getContext(), rowItems_pen);
        mylistview_pen.setAdapter(adapter_pen);


        for (int i = 0; i < all_my_links_ids_confirmed.size(); i++) {
            Row_Item_Links item_con = new Row_Item_Links( all_my_links_names_confirmed.get(i),
                    all_my_links_ids_confirmed.get(i), all_my_links_costs_confirmed.get(i));
            rowItems_con.add(item_con);
        }
        Adapter_Custom_Links adapter_con = new Adapter_Custom_Links(getContext(), rowItems_con);
        mylistview_con.setAdapter(adapter_con);


        mylistview_con.setOnItemClickListener(this);
        mylistview_pen.setOnItemClickListener(this);
        return myView;
    }

    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

        if(parent.getId()==R.id.lv_confirmed_links){
            String users_id = rowItems_con.get(position).getLinks_user_id();
            Intent users_intent = new Intent(getContext(),Activity_User_Profile.class);
            users_intent.putExtra("users_id",users_id);
            startActivity(users_intent);
        } else if(parent.getId()==R.id.lv_pending_links){
            String users_id = rowItems_pen.get(position).getLinks_user_id();
            Intent users_intent = new Intent(getContext(),Activity_User_Profile.class);
            users_intent.putExtra("users_id",users_id);
            startActivity(users_intent);
        }


    }
}
