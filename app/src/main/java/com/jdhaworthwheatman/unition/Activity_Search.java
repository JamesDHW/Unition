package com.jdhaworthwheatman.unition;

import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.common.primitives.Ints;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

public class Activity_Search extends AppCompatActivity implements AdapterView.OnItemClickListener{

    List<Row_Item_User_List> rowItems;
    FirebaseAuth mAuth;
    private DatabaseReference mDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity__search);

        final ListView mylistview = findViewById(R.id.lv_search);

        //Initialise list arrays
        rowItems = new ArrayList<Row_Item_User_List>();

        //initialise sharedPreferences
        final SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        final String uni_val = sharedPreferences.getString("user_uni",null);

        //initialise database variables
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        CollectionReference users_collection = db.collection("Unition").document(uni_val).collection("Users");

        //get skill from intent
        Intent intent = getIntent();
        String skill_val = intent.getStringExtra("skill");

        TextView tv_skill_title = findViewById(R.id.tv_search_title);
        tv_skill_title.setText(skill_val);

        users_collection.whereArrayContains("skill_list",skill_val).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            ArrayList<String> all_usernames = new ArrayList<String>();
                            ArrayList<String> all_user_ids = new ArrayList<String>();
                            ArrayList<Integer> all_user_costs = new ArrayList<Integer>();
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                String name_val = document.getString("name");
                                String ID_val = document.getId().toString();
                                int cost_val = Ints.checkedCast(document.getLong("cost"));
                                all_usernames.add(name_val);
                                all_user_ids.add(ID_val);
                                all_user_costs.add(cost_val);
                            }

                            for (int i = 0; i < all_usernames.size(); i++) {
                                Row_Item_User_List item = new Row_Item_User_List(all_usernames.get(i),all_user_ids.get(i), all_user_costs.get(i));
                                rowItems.add(item);
                            }
                            Adapter_Custom_Users_List adapter = new Adapter_Custom_Users_List(getBaseContext(), rowItems);
                            mylistview.setAdapter(adapter);
                            LinearLayout ll_progress = findViewById(R.id.progressBar_search);
                            ll_progress.setVisibility(View.GONE);
                        }
                    }
        });

        mylistview.setOnItemClickListener(this);
    }

    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        String users_id = rowItems.get(position).getLinks_user_id();
        Intent users_intent = new Intent(Activity_Search.this,Activity_User_Profile.class);
        users_intent.putExtra("users_id",users_id);
        startActivity(users_intent);
    }
}
