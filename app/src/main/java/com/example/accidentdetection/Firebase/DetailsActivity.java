package com.example.accidentdetection.Firebase;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.media.Image;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.accidentdetection.MainActivity;
import com.example.accidentdetection.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class DetailsActivity extends AppCompatActivity {
    private static final int REQUEST_CONTACT = 1;
    private static final int REQUEST_READ_CONTACTS_PERMISSION = 0;
    private static final int SELECT_PICTURE = 200;
    private FirebaseFirestore firebaseFirestore ;
    private FirebaseAuth firebaseAuth;
    private StorageReference storageRef;

    private Button contact_btn  ;
    private TextView count_text;
    private ImageView profile_img;
    private EditText text_name;
    private EditText text_age;
    private TextView text_contactCounter;
    private EditText text_sex;
    private List<String> contacts;
    static  int count =0;
    private Uri uri;

    private HashMap<String,String>map;
    @Override

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details);
        requestContactsPermission();

        Button addImage_btn = findViewById(R.id.btn_addImage);
        Button saveDetails_btn = findViewById(R.id.btn_saveDetails);

        firebaseFirestore  =  FirebaseFirestore.getInstance();
        firebaseAuth =  FirebaseAuth.getInstance();
        storageRef = FirebaseStorage.getInstance().getReference("profilePicUploads");

        contact_btn =  findViewById(R.id.btn_Contact);
        count_text =  findViewById(R.id.Contact_counter_text);
        profile_img =  findViewById(R.id.profile_pic);
        text_name =   findViewById(R.id.Name_text);
        text_age =  findViewById(R.id.Age_text);
        text_sex = findViewById(R.id.Sex_text);
        text_contactCounter  = findViewById(R.id.Contact_counter_text);
        contacts =  new ArrayList<>();

        //--------------------------------------------------------------------------------
        contact_btn.setOnClickListener(view -> getContacts());
        //---------------------------------------------------------------------------------
        addImage_btn.setOnClickListener(view -> imageChooser());
        //------------------------------------------------------------------------------------
        saveDetails_btn.setOnClickListener(view -> {
            saveDetails();
        });
    }

    private  void getContacts(){
        requestContactsPermission();

        final Intent pickContact = new Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI);
        updateButton(hasContactsPermission());
        map =  new HashMap<String,String>();
        Uri uri =  ContactsContract.CommonDataKinds.Phone.CONTENT_URI;
        Cursor cursor =  getContentResolver().query(uri,null,null,null,null);
        while(cursor.moveToNext()){
            int phoneIndex = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER);
            int nameIndex = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME);

            String phoneNo ="";
            phoneNo = cursor.getString(phoneIndex);
            String name = "";
            name = cursor.getString(nameIndex);
            if(!name.equals(null) && !phoneNo.equals(null)){
                //Log.e("names",  "phone" +" "+phoneNo+" "+ nameIndex + " " + name);
                map.put(name,phoneNo);
            }
        }
        startActivityForResult(pickContact,REQUEST_CONTACT);
    }

    private void saveDetails() {
        ProgressBar progressBar = findViewById(R.id.p_bar_2);
        progressBar.setVisibility(View.VISIBLE);

        String name =  text_name.getText().toString().trim();
        String age =   text_age.getText().toString().trim();
        String sex =   text_sex.getText().toString().trim();
        String imgPath ="";
        String imgUrl = "";
        if(name.length() == 0){
            text_name.setError("This can't be empty");
            return;
        }
        if(sex.length() != 1 ){
            text_sex.setError("Enter M or F");
            return;
        }
        else if(sex.charAt(0) !='M' && sex.charAt(0) !='F'){
            text_sex.setError("Enter M or F");
            return;
        }
        try {
            int a = Integer.parseInt(age);
        }catch (Exception e){
            text_age.setError("enter an integer");
            return;
        }
        if(contacts.size()==0){
            text_contactCounter.setError("minimum 1 number needed");
            return;
        }



        String UID =  firebaseAuth.getUid().toString();
        //Log.d("UID", "saveDetails: "+UID);
        Map<String,Object> map =  new HashMap<>();
        storageRef.child("images/" + UUID.randomUUID().toString());
        if(uri !=null){
            StorageReference fileRef = storageRef.child(firebaseAuth.getUid().toString()).child(firebaseAuth.getUid().toString()+"."+getFileExt());
            fileRef.putFile(uri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                    if(task.isSuccessful()){
                        //                Toast.makeText(MainActivity2.this,fileRef.getPath().toString(),Toast.LENGTH_SHORT).show();
                        Log.d("hello",fileRef.getPath().toString());
                        progressBar.setVisibility(View.INVISIBLE);
                        startActivity(new Intent(getApplicationContext(), MainActivity.class));
                        finish();
                    }
                }
            });
        }

        map.put("Name",name);
        map.put("Age",age);
        map.put("Sex",sex);
        map.put("contacts",contacts);

        firebaseFirestore.collection("User").document(UID).set(map).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()){
                    Toast.makeText(DetailsActivity.this,"Updated",Toast.LENGTH_SHORT).show();
                }else{
                    Toast.makeText(DetailsActivity.this,"SomeThing went wrong, Try again",Toast.LENGTH_SHORT).show();
                }

            }
        });
    }
    private String getFileExt(){
        ContentResolver cr =  getContentResolver();
        MimeTypeMap mime = MimeTypeMap.getSingleton();
        return mime.getExtensionFromMimeType(cr.getType(uri));
    }
    void imageChooser() {

        Intent i = new Intent();
        i.setType("image/*");
        i.setAction(Intent.ACTION_GET_CONTENT);

        startActivityForResult(Intent.createChooser(i, "Select Picture"), SELECT_PICTURE);
    }



    public void updateButton(boolean enable)
    {
        contact_btn.setEnabled(enable);

    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {

        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK && requestCode == REQUEST_CONTACT) {
            String phoneNo = null;
            String name = null;

            Uri uri = data.getData();
            Cursor cursor = this.getContentResolver().query(uri, null, null, null, null);

            if (cursor.moveToFirst()) {
                int nameIndex = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME);
                name = cursor.getString(nameIndex);
                phoneNo = map.get(name);
                Log.e("yep",  "phone" +" "+phoneNo+" "+ nameIndex + " " + name);
                if(!phoneNo.equals(null)){
                    count++;
                    String counter = Integer.toString(count);
                    counter = counter + "number added";
                    count_text.setText(counter);
                    contacts.add(phoneNo);
                    Toast.makeText(this,name+" "+phoneNo+"Added",Toast.LENGTH_SHORT).show();
                }

            }
            cursor.close();
        }

        if (resultCode == RESULT_OK && requestCode == SELECT_PICTURE) {
            Uri selectedImageUri = data.getData();

            if (null != selectedImageUri) {
                uri = selectedImageUri;
                profile_img.setImageURI(uri);
            }
        }
    }

    private boolean hasContactsPermission()
    {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS) ==
                PackageManager.PERMISSION_GRANTED;
    }
    private void requestContactsPermission()
    {
        if (!hasContactsPermission())
        {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.READ_CONTACTS}, REQUEST_READ_CONTACTS_PERMISSION);
        }
    }
}