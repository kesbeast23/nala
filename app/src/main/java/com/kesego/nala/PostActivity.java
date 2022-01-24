package com.kesego.nala;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.github.dhaval2404.imagepicker.ImagePicker;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;

import java.util.HashMap;

public class PostActivity extends AppCompatActivity {
    Uri imageUri;
    String imageUrl;
    StorageTask uploadTask;
    StorageReference storageReference;
    EditText description,title;
    ImageView close,image_added;
    private static final int GalleryPick = 1;
    private static final int CAMERA_REQUEST = 1888;
    private static final int STORAGE_REQUEST = 200;
    private static final int REQUEST_CODE_IMAGE = 101;

    String cameraPermission[];
    String storagePermission[];


    TextView post;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post);

        close = findViewById(R.id.close);
        description = findViewById(R.id.description);
        title = findViewById(R.id.title);
        image_added = findViewById(R.id.image_added);
        post = findViewById(R.id.post);

        storageReference = FirebaseStorage.getInstance().getReference("posts");
        // allowing permissions of gallery and camera
        cameraPermission = new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE};
        storagePermission = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE};



        close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(PostActivity.this,MainActivity.class));
                finish();
            }
        });

        post.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                uploadImage();
            }
        });
        ImagePicker.Companion.with(PostActivity.this)
                .crop()	    			//Crop image(Optional), Check Customization for more option
                .compress(1024)			//Final image size will be less than 1 MB(Optional)
                .maxResultSize(1080, 1080)	//Final image resolution will be less than 1080 x 1080(Optional)
                .start(REQUEST_CODE_IMAGE);


    }


    private String getFileExtensions(Uri uri) {

        ContentResolver contentResolver = getContentResolver();
        MimeTypeMap mime = MimeTypeMap.getSingleton();
        return mime.getExtensionFromMimeType(contentResolver.getType(uri));

    }

    private void uploadImage() {

        final ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("adding new entry");
        progressDialog.show();
        if(imageUri != null){

            final StorageReference referencefile = storageReference.child(System.currentTimeMillis()
                    + "."+ getFileExtensions(imageUri));

            uploadTask = referencefile.putFile(imageUri);
            uploadTask.continueWithTask(new Continuation() {
                @Override
                public Object then(@NonNull Task task) throws Exception {
                    if (!task.isSuccessful()){
                        throw  task.getException();
                    }
                    return referencefile.getDownloadUrl();
                }
            }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                @Override
                public void onComplete(@NonNull Task <Uri>task) {
                    if(task.isSuccessful()){
                        Uri downloadUri= task.getResult();
                        imageUrl = downloadUri.toString();

                        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Posts");
                        String postid=reference.push().getKey();
                        HashMap<String,Object> hashMap = new HashMap<>();

                        hashMap.put("postid",postid);
                        hashMap.put("postimage",imageUrl);
                        hashMap.put("description",description.getText().toString());
                        hashMap.put("publisher", FirebaseAuth.getInstance().getCurrentUser().getUid());
                        hashMap.put("title",title.getText().toString().toLowerCase());

                        reference.child(postid).setValue(hashMap);
                        progressDialog.dismiss();

                        startActivity(new Intent(PostActivity.this,MainActivity.class));
                        finish();

                    }else{
                        Toast.makeText(PostActivity.this,"Failed!",Toast.LENGTH_SHORT).show();
                    }
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {

                    Toast.makeText(PostActivity.this, ""+ e.getMessage() , Toast.LENGTH_SHORT).show();

                }
            });
        }
        else {
            Toast.makeText(this, "no Image Selected!", Toast.LENGTH_SHORT).show();
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
     if (requestCode == REQUEST_CODE_IMAGE) {
            if (resultCode == Activity.RESULT_OK) {
                 imageUri = data.getData();
                Picasso.with(this).load(imageUri).into(image_added);
            }
        }
        else{
            Toast.makeText(this,"Something is wrong!",Toast.LENGTH_SHORT).show();
            startActivity(new Intent(PostActivity.this,MainActivity.class));
            finish();
        }
    }
}