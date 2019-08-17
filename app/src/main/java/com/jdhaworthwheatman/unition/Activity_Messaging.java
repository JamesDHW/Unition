package com.jdhaworthwheatman.unition;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;

import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;

public class Activity_Messaging extends AppCompatActivity {

    FirebaseAuth mAuth;
    String message_id_val = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity__messaging);
        //hide keyboard
        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);


        //initialise authentication variables
        mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        final SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        final String uni_val = sharedPreferences.getString("user_uni",null);
        final SharedPreferences.Editor editor = sharedPreferences.edit();
        final String ID = currentUser.getUid();
        final String my_name = sharedPreferences.getString("user_name",null);

        //get info out of intent from search activity
        Bundle extras = getIntent().getExtras();
        message_id_val = (String) extras.get("message_id");
        final String message_title_val = (String) extras.get("message_title");

        final TextView tv_title = findViewById(R.id.tv_message_title);
        tv_title.setText(message_title_val);
        final EditText etxt_message = findViewById(R.id.etxt_message);

        //Initialise FireBase ref
        final FirebaseFirestore db = FirebaseFirestore.getInstance();
        final CollectionReference collection_my_messages = db.collection("Unition").
                document(uni_val).collection("Messages");

        collection_my_messages.document(message_id_val).addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {
                List<Map<String,Object>> message_array = (List<Map<String,Object>>)
                        documentSnapshot.get("messages");
                LinearLayout ll_messages = findViewById(R.id.ll_messages);
                ll_messages.removeAllViews();

                if(message_array!=null){
                    for(int i=0;i<message_array.size();i++) {
                        TextView tv_messeage = new TextView(getBaseContext());
                        String message_name = (String) message_array.get(i).get("user");
                        String message_text = (String) message_array.get(i).get("message");

                        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams
                                (LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                        params.setMargins(10,10,10,10);

                        tv_messeage.setTextColor(getResources().getColor(R.color.colorBackground));
                        tv_messeage.setBackgroundColor(getResources().getColor(R.color.colorDark));
                        tv_messeage.setLayoutParams(new LinearLayout.
                                LayoutParams(LinearLayout.
                                LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));
                        tv_messeage.setPadding(40, 40, 40, 40);
                        if (message_name.equals(my_name)) {
                            message_name = "me";
                            params.gravity = Gravity.END;
                            tv_messeage.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
                        }
                        String message_content = message_name + ": " + message_text;
                        tv_messeage.setText(message_content);
                        tv_messeage.setLayoutParams(params);
                        ll_messages.addView(tv_messeage);
                    }
                }
            }
        });

        ImageButton btn_send_message = findViewById(R.id.btn_send_message);
        btn_send_message.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String message_str = String.valueOf(etxt_message.getText());
                etxt_message.getText().clear();
                String ts = new SimpleDateFormat("yyyyMMddHHmmss")
                        .format(new java.sql.Timestamp(new java.util.Date().getTime()));
                Map<String, Object> docData = new HashMap<>();
                //docData.put(ts, Arrays.asList(my_name,message_str));
                docData.put("time",ts);
                docData.put("user",my_name);
                docData.put("message",message_str);
                collection_my_messages.document(message_id_val).update("messages", FieldValue.arrayUnion(docData));

            }
        });


    }

}
