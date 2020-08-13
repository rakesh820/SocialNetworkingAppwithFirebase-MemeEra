package com.andriod.memeera;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.hendraanggrian.appcompat.socialview.Hashtag;
import com.hendraanggrian.appcompat.widget.HashtagArrayAdapter;
import com.hendraanggrian.appcompat.widget.SocialAutoCompleteTextView;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;

public class EditPostActivity extends AppCompatActivity {
    private Uri imageUri;
    private String imageUrl,etag;
    private ImageView close;
    private ImageView imageAdded;
    private TextView update;
    SocialAutoCompleteTextView description;
    private TextView changepic;
    private String saveCurrentDate, saveCurrentTime,editdesc,editImage;
    String isUpdateKey;
    String editPostId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_post);

        close = findViewById(R.id.close);
        imageAdded = findViewById(R.id.image_added);
        update = findViewById(R.id.update);
        changepic = findViewById(R.id.change_pic);
        description = findViewById(R.id.description);
        Intent intent = getIntent();
        isUpdateKey = "" + intent.getStringExtra("key");
        editPostId = "" + intent.getStringExtra("editPostId");

        if (isUpdateKey.equals("editPost")) {
            FirebaseDatabase.getInstance().getReference("Posts").orderByChild("postid").equalTo(editPostId).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot  snapshot) {
                    for (DataSnapshot ds:snapshot.getChildren()) {
                        editdesc=""+ds.child("description").getValue();
                        editImage=""+ds.child("imageurl").getValue();

                        description.setText(editdesc);
                        Picasso.get().load(editImage).into(imageAdded);
                        List<String> hashTags = description.getHashtags();
                        if (!hashTags.isEmpty()){
                            for (String tag : hashTags){
                                etag=tag.toLowerCase();
                                etag=etag.replace("#","");
                                FirebaseDatabase.getInstance().getReference("HashTags").child(etag).child(editPostId).removeValue();
                            }
                        }
                    }
                }
                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                }
            });
            close.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    finish();
                }
            });
            imageAdded.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    CropImage.activity().start(EditPostActivity.this);
                }
            });
            changepic.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    CropImage.activity().start(EditPostActivity.this);
                }
            });

            update.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    updateProfile();
                }
            });
        }
    }
    private void updateProfile() {
        final ProgressDialog pd = new ProgressDialog(this);
        pd.setMessage("Uploading");
        pd.show();
        StorageReference pref = FirebaseStorage.getInstance().getReferenceFromUrl(editImage);
        pref.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                if (imageUri != null) {
                    Calendar calFordDate = Calendar.getInstance();
                    SimpleDateFormat currentDate = new SimpleDateFormat("dd-MMMM-yyyy");
                    saveCurrentDate = currentDate.format(calFordDate.getTime());

                    Calendar calFordTime = Calendar.getInstance();
                    SimpleDateFormat currentTime = new SimpleDateFormat("HH:mm");
                    saveCurrentTime = currentTime.format(calFordTime.getTime());

                    final StorageReference filePath = FirebaseStorage.getInstance().getReference("Posts").child(System.currentTimeMillis() + "." + getFileExtension(imageUri));
                    StorageTask uploadtask = filePath.putFile(imageUri);
                    uploadtask.continueWithTask(new Continuation() {
                        @Override
                        public Object then(@NonNull Task task) throws Exception {
                            if (!task.isSuccessful()) {
                                throw task.getException();
                            }

                            return filePath.getDownloadUrl();
                        }
                    }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                        @Override
                        public void onComplete(@NonNull Task<Uri> task) {
                            Uri downloadUri = task.getResult();
                            imageUrl = downloadUri.toString();

                            DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Posts");

                            HashMap<String, Object> map = new HashMap<>();
                            map.put("imageurl", imageUrl);
                            map.put("date", saveCurrentDate);
                            map.put("time", saveCurrentTime);
                            map.put("description", description.getText().toString());
                            map.put("publisher", FirebaseAuth.getInstance().getCurrentUser().getUid());

                            ref.child(editPostId).updateChildren(map);

                            DatabaseReference mHashTagRef = FirebaseDatabase.getInstance().getReference().child("HashTags");
                            List<String> hashTags = description.getHashtags();
                            if (!hashTags.isEmpty()) {
                                for (String tag : hashTags) {
                                    map.clear();

                                    map.put("tag", tag.toLowerCase());
                                    map.put("postid", editPostId);

                                    mHashTagRef.child(tag.toLowerCase()).child(editPostId).setValue(map);
                                }
                            }

                            pd.dismiss();
                            startActivity(new Intent(EditPostActivity.this, MainActivity.class));
                            finish();
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(EditPostActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
                } else {
                    Toast.makeText(EditPostActivity.this, "No image was selected!", Toast.LENGTH_SHORT).show();
                }

            }
        });
    }
    private String getFileExtension(Uri uri) {

        return MimeTypeMap.getSingleton().getExtensionFromMimeType(this.getContentResolver().getType(uri));

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE && resultCode == RESULT_OK){
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            imageUri = result.getUri();

            imageAdded.setImageURI(imageUri);
        } else {
            Toast.makeText(this, "Try again!", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(EditPostActivity.this , MainActivity.class));
            finish();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();

        final ArrayAdapter<Hashtag> hashtagAdapter = new HashtagArrayAdapter<>(getApplicationContext());

        FirebaseDatabase.getInstance().getReference().child("HashTags").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    hashtagAdapter.add(new Hashtag(snapshot.getKey() , (int) snapshot.getChildrenCount()));
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        description.setHashtagAdapter(hashtagAdapter);
    }
}