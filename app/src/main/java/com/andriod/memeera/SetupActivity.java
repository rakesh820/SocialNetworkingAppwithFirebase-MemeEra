package com.andriod.memeera;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.andriod.memeera.Model.User;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.rengwuxian.materialedittext.MaterialEditText;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

public class SetupActivity extends AppCompatActivity {
      private ImageView close;
      private CircleImageView imageProfile;
      private Button save;
      private TextView changePhoto;
      private TextView skip;
       private MaterialEditText fullname;
      private MaterialEditText username;
        private MaterialEditText bio;

         private FirebaseUser fUser;

        private Uri mImageUri;
        private StorageTask uploadTask;
         private StorageReference storageRef;
      @Override
      protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_setup);

            close = findViewById(R.id.close);
            imageProfile = findViewById(R.id.image_profile);
            save = findViewById(R.id.save);
            changePhoto = findViewById(R.id.change_photo);
            fullname = findViewById(R.id.fullname);
            username = findViewById(R.id.username);
            bio = findViewById(R.id.bio);
            skip=findViewById(R.id.skip);

            fUser = FirebaseAuth.getInstance().getCurrentUser();
            storageRef = FirebaseStorage.getInstance().getReference().child("Uploads");

            FirebaseDatabase.getInstance().getReference().child("Users").child(fUser.getUid()).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    User user = dataSnapshot.getValue(User.class);
                    username.setText(user.getUsername());
                    fullname.setText(user.getFullname());
                    bio.setText(user.getBio());
                    Picasso.get().load(user.getImageurl()).into(imageProfile);
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

            changePhoto.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    CropImage.activity().setCropShape(CropImageView.CropShape.OVAL).start(SetupActivity.this);
                }
            });

            imageProfile.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    CropImage.activity().setCropShape(CropImageView.CropShape.OVAL).start(SetupActivity.this);
                }
            });

            save.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    updateProfile();
                    startActivity(new Intent(SetupActivity.this,MainActivity.class));
                }
            });
            skip.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    startActivity(new Intent(SetupActivity.this,MainActivity.class));
                }
            });
        }

        private void updateProfile() {
            HashMap<String, Object> map = new HashMap<>();
            map.put("fullname", fullname.getText().toString());
            map.put("username", username.getText().toString());
            map.put("bio", bio.getText().toString());

            FirebaseDatabase.getInstance().getReference().child("Users").child(fUser.getUid()).updateChildren(map);
            Toast.makeText(SetupActivity.this, "Profile is updated Successfully!", Toast.LENGTH_SHORT).show();
        }

        private void uploadImage() {
            final ProgressDialog pd = new ProgressDialog(this);
            pd.setMessage("Uploading");
            pd.show();

            if (mImageUri != null) {
                final StorageReference fileRef = storageRef.child(System.currentTimeMillis() + ".jpeg");

                uploadTask = fileRef.putFile(mImageUri);
                uploadTask.continueWithTask(new Continuation() {
                    @Override
                    public Object then(@NonNull Task task) throws Exception {
                        if (!task.isSuccessful()) {
                            throw task.getException();
                        }

                        return  fileRef.getDownloadUrl();
                    }
                }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                    @Override
                    public void onComplete(@NonNull Task<Uri> task) {
                        if (task.isSuccessful()) {
                            Uri downloadUri = task.getResult();
                            String url = downloadUri.toString();

                            FirebaseDatabase.getInstance().getReference().child("Users").child(fUser.getUid()).child("imageurl").setValue(url);
                            pd.dismiss();
                        } else {
                            Toast.makeText(SetupActivity.this, "Upload failed!", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            } else {
                Toast.makeText(this, "No image selected", Toast.LENGTH_SHORT).show();
            }
        }

        @Override
        protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
            super.onActivityResult(requestCode, resultCode, data);

            if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE && resultCode == RESULT_OK) {
                CropImage.ActivityResult result = CropImage.getActivityResult(data);
                mImageUri = result.getUri();

                uploadImage();
            } else {
                Toast.makeText(this, "Something went wrong!", Toast.LENGTH_SHORT).show();
            }
        }
}