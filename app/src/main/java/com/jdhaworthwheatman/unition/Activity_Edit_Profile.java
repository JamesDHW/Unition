package com.jdhaworthwheatman.unition;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class Activity_Edit_Profile extends AppCompatActivity {

    FirebaseAuth mAuth;
    boolean drawn = false;

    Uri targetUri;
    Bitmap bitmap = null;

    FirebaseStorage storage;
    StorageReference storageReference;

    StorageReference mStorageRef;
    Uri mImageUri;


    private String saveToInternalStorage(Bitmap bitmapImage){
        ContextWrapper cw = new ContextWrapper(getApplicationContext());
        // path to /data/data/yourapp/app_data/imageDir
        File directory = cw.getDir("imageDir", Context.MODE_PRIVATE);
        // Create imageDir
        File mypath = new File(directory,"my_profile.jpg");

        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(mypath);
            // Use the compress method on the BitMap object to write image to the OutputStream
            bitmapImage.compress(Bitmap.CompressFormat.PNG, 100, fos);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                fos.close();
            } catch (IOException e) {e.printStackTrace();}
        }
        return directory.getAbsolutePath();
    }

    private Uri loadImageFromStorage(String path) {
        try {
            File f=new File(path, "my_profile.jpg");
            Bitmap b = BitmapFactory.decodeStream(new FileInputStream(f));
            ImageButton upload_prof_pic = findViewById(R.id.ib_edit_my_profile_pic);
            upload_prof_pic.setImageBitmap(b);
            Uri myUri = Uri.fromFile(f);
            return myUri;
        }
        catch (FileNotFoundException e){e.printStackTrace();}
        return null;
    }

    private void uploadImage() {

        if(targetUri != null)
        {
            final ProgressDialog progressDialog = new ProgressDialog(this);
            progressDialog.setTitle("Uploading...");
            progressDialog.show();

            StorageReference ref = storageReference.child("profile_pic");
            ref.putFile(targetUri)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            progressDialog.dismiss();
                            Toast.makeText(getBaseContext(), "Uploaded", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            progressDialog.dismiss();
                            Toast.makeText(getBaseContext(), "Failed "+e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                            double progress = (100.0*taskSnapshot.getBytesTransferred()/taskSnapshot
                                    .getTotalByteCount());
                            progressDialog.setMessage("Uploaded "+(int)progress+"%");
                        }
                    });
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity__edit__profile);
        //hide keyboard
        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        ImageButton btn_save_profile = findViewById(R.id.ib_save_my_profile);

        //find edit texts
        final EditText etxt_name = findViewById(R.id.etxt_edit_my_name);
        final EditText etxt_degree = findViewById(R.id.etxt_edit_my_degree);
        final TextView txt_skills = findViewById(R.id.tv_edit_my_skills);
        final EditText etxt_bio = findViewById(R.id.etxt_edit_my_bio);
        final EditText etxt_cost = findViewById(R.id.etxt_edit_my_cost);

        //find the two frames for registering and logging in
        final FrameLayout fl_skills = findViewById(R.id.frame_layout_skills_chooser);
        //get fragment manager
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        //add fragment to frame
        ft.add(R.id.frame_layout_skills_chooser, new Frag_Update_Skills());
        ft.commit();
        //set initial visibility of views
        fl_skills.setVisibility(View.GONE);


        //initialise shared preferences
        final SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        final SharedPreferences.Editor editor = sharedPreferences.edit();

        final String uni_val = sharedPreferences.getString("user_uni",null);

        String name_val = sharedPreferences.getString("user_name", "YourName");
        String degree_val = sharedPreferences.getString("user_degree", "YourDegree");
        String bio_val = sharedPreferences.getString("user_bio", "-");
        final long cost_val = sharedPreferences.getLong("user_cost", 0);
        final int skills_size_val = sharedPreferences.getInt("user_skills_size", 0);
        final String User_ID = sharedPreferences.getString("ID","No Name");

        String skills_string = "" ;
        for(int i=0;i<skills_size_val;i++ ){
            skills_string +=  " - " + sharedPreferences.getString("skill_val_"+i,null) +
                    System.getProperty("line.separator");
        }

        etxt_name.setText(name_val, TextView.BufferType.EDITABLE);
        etxt_degree.setText(degree_val, TextView.BufferType.EDITABLE);
        txt_skills.setText(skills_string);
        etxt_bio.setText(bio_val, TextView.BufferType.EDITABLE);
        etxt_cost.setText(String.valueOf(cost_val/100), TextView.BufferType.EDITABLE);

        mStorageRef = FirebaseStorage.getInstance().getReference(User_ID);

        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference(User_ID);

        //SKILLS CHECKBOXES
        final ImageButton btn_begin_update_skills = findViewById(R.id.ib_begin_update_skills);
        btn_begin_update_skills.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //sets frame visible
                fl_skills.setVisibility(View.VISIBLE);

                //put the keyboard away
                try{
                    InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(btn_begin_update_skills.getWindowToken(), 0);
                } catch(Exception e){}

                //get list of possible skills
                final String[] all_skills_list = getResources().getStringArray(R.array.skills_list);
                //find the linear layout
                final LinearLayout sv_skills = findViewById(R.id.ll_skills);
                if(!drawn){
                    for(int i=0;i<all_skills_list.length;i++){
                        CheckBox cb = new CheckBox(getBaseContext());
                        cb.setText(all_skills_list[i]);
                        cb.setTextColor(getResources().getColor(R.color.white));
                        sv_skills.addView(cb);
                    }
                    drawn = true;
                }

                //button to update and exit the view
                Button btn_update_skills = findViewById(R.id.btn_update_skills);
                btn_update_skills.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        //define a variable to find how many items are selected
                        int checked_size=0;
                        for(int i=0;i<all_skills_list.length;i++){
                            View nextChild = sv_skills.getChildAt(i);
                            if(nextChild instanceof CheckBox){
                                if(((CheckBox) nextChild).isChecked()){
                                    String skill_val = ((CheckBox) nextChild).getText().toString();
                                    editor.putString("skill_val_"+checked_size,skill_val).apply();
                                    checked_size ++;
                                }
                            }
                        }

                        //save how many items are checked
                        editor.putInt("user_skills_size",checked_size).apply();
                        //remove the frame
                        fl_skills.setVisibility(View.GONE);

                        //Redraw the skills TV
                        String skills_string = "My Skills: ";
                        for(int i=0;i<sharedPreferences.getInt("user_skills_size", 0);i++ ){
                            skills_string += System.getProperty("line.separator") + " - " +
                                    sharedPreferences.getString("skill_val_"+i,null);
                        }
                        txt_skills.setText(skills_string);

                    }
                });
            }
        });

        btn_save_profile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String name_val = etxt_name.getText().toString();
                String degree_val = etxt_degree.getText().toString();
                String bio_val = etxt_bio.getText().toString();
                long cost_val = 100*Long.valueOf(etxt_cost.getText().toString());

                if(bitmap!=null){
                    saveToInternalStorage(bitmap);
                }

                //update shared preferences
                editor.putString("user_name", name_val).
                        putString("user_degree", degree_val).
                        putString("user_bio", bio_val).
                        putLong("user_cost", cost_val).
                        apply();

                //save data to FireBase
                mAuth = FirebaseAuth.getInstance();
                FirebaseUser currentUser = mAuth.getCurrentUser();
                String ID = currentUser.getUid();
                final DocumentReference mDocRef = FirebaseFirestore.getInstance().
                        document("Unition/"+uni_val+"/Users/"+ID);
                final Map<String,Object> dataToSave = new HashMap<String, Object>();
                dataToSave.put("name",name_val);
                dataToSave.put("degree",degree_val);
                dataToSave.put("bio",bio_val);
                dataToSave.put("cost",cost_val);
                dataToSave.put("no_of_skills",sharedPreferences.getInt("user_skills_size",0));
                ArrayList<String> skills_array_list= new ArrayList<String>();
                for(int i=0;i<sharedPreferences.getInt("user_skills_size",0);i++){
                    skills_array_list.add(sharedPreferences.getString("skill_val_"+i,null));
                }

                //add my degree as a skill to search
                skills_array_list.add(degree_val.toLowerCase().trim());
                dataToSave.put("skill_list",skills_array_list);
                mDocRef.update(dataToSave).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Intent intent = new Intent(getBaseContext(), Activity_Unition_Hub.class);
                        Toast.makeText(getBaseContext(), "Profile saved.", Toast.LENGTH_SHORT).show();
                        finish();
                        startActivity(intent);
                    }
                });
            }
        });

        ImageButton choose_prof_pic = findViewById(R.id.ib_edit_my_profile_pic);
        mImageUri = loadImageFromStorage("/data/user/0/com.jdhaworthwheatman.unition/app_imageDir");
        choose_prof_pic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_PICK,
                        android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(intent, 0);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // TODO Auto-generated method stub
        super.onActivityResult(requestCode, resultCode, data);
        ImageButton upload_prof_pic = findViewById(R.id.ib_edit_my_profile_pic);

        if (resultCode == RESULT_OK) {
            targetUri = data.getData();
            try {
                bitmap = BitmapFactory.decodeStream(getContentResolver().openInputStream(targetUri));
                upload_prof_pic.setImageBitmap(bitmap);
                uploadImage();
            } catch (FileNotFoundException e) {e.printStackTrace();}
        }
    }

    @Override
    public void onBackPressed(){
        FrameLayout fl_skills = findViewById(R.id.frame_layout_skills_chooser);
        if(fl_skills.getVisibility()==View.VISIBLE){
            fl_skills.setVisibility(View.GONE);

        } else{
            Intent intent = new Intent(getBaseContext(), Activity_Unition_Hub.class);
            finish();
            startActivity(intent);
        }
    }
}
