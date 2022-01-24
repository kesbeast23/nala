package com.kesego.nala;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

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
import com.theartofdev.edmodo.cropper.CropImage;

import java.util.HashMap;

public class PostActivity extends AppCompatActivity {
    Uri imageUri;
    String imageUrl;
    StorageTask uploadTask;
    StorageReference storageReference;
    EditText description,title;
    ImageView close,image_added;
    static final int CAMERA_REQUEST_CODE = 101;
    private static final  int GALLERY_REQUEST =1;

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
        CropImage.activity().setAspectRatio(1,1).start(PostActivity.this);

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

        if (requestCode==CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE && resultCode == RESULT_OK){
            CropImage.ActivityResult result = CropImage.getActivityResult(data);

            imageUri = result.getUri();
            image_added.setImageURI(imageUri);
        }
        else{
            Toast.makeText(this,"Something is wrong!",Toast.LENGTH_SHORT).show();
            startActivity(new Intent(PostActivity.this,MainActivity.class));
            finish();
        }
    }
}