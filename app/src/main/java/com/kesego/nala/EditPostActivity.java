package com.kesego.nala;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.ContentResolver;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.kesego.nala.Model.Post;
import com.rengwuxian.materialedittext.MaterialEditText;
import com.theartofdev.edmodo.cropper.CropImage;

import java.util.HashMap;

public class EditPostActivity extends AppCompatActivity {

    ImageView close,image_added;
    MaterialEditText description;
    TextView save,tv_change;
    FirebaseUser firebaseUser;
    private Uri mImageUri;
    private StorageTask uploadtask;
    StorageReference storageRef;
    Post post;
    String postE;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_post);
        close = findViewById(R.id.close);
        image_added = findViewById(R.id.image_added);
        description = findViewById(R.id.description);
        save = findViewById(R.id.save);
        tv_change = findViewById(R.id.tv_change);
        Intent intent = getIntent();
        postE = intent.getStringExtra("postE");

        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Posts").child(postE);

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Post post= snapshot.getValue(Post.class);
                description.setText(post.getDescription());
                Glide.with(getApplicationContext()).load(post.getPostimage()).into(image_added);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        tv_change.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CropImage.activity().setAspectRatio(1,1).start(EditPostActivity.this);
            }
        });
        image_added.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CropImage.activity().setAspectRatio(1,1).start(EditPostActivity.this);
            }
        });
        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updatePost(description.getText().toString());
            }


        });

    }
    private void updatePost(String description) {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Posts").child(postE);
        HashMap<String,Object> hashMap = new HashMap<>();

        hashMap.put("description",description);
        reference.updateChildren(hashMap);
    }
    private String getFileExtension(Uri uri){
        ContentResolver contentResolver = getContentResolver();
        MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton();
        return mimeTypeMap.getExtensionFromMimeType(contentResolver.getType(uri));
    }

}