package com.andriod.memeera;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.format.DateFormat;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.andriod.memeera.Adapter.AdapterChat;
import com.andriod.memeera.Model.Chat;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

public class ChatActivity extends AppCompatActivity {
    Toolbar toolbar;
    RecyclerView recyclerView;
    TextView nameTv, userStatusTv;
    EditText messageEt;
    ImageButton sendBtn;
    ImageView profileIv;
    String id, hisid,hisimage;
    FirebaseAuth firebaseAuth;
    FirebaseDatabase database;
    DatabaseReference reference;

    ValueEventListener seenListener;
    DatabaseReference ur;
    List<Chat> chatList;
    AdapterChat adapterChat;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setTitle("");

        recyclerView = findViewById(R.id.chat_recyclerView);
        nameTv = findViewById(R.id.nameTv);
        userStatusTv = findViewById(R.id.userStatusTv);
        messageEt = findViewById(R.id.messageEt);
        sendBtn = findViewById(R.id.sendBtn);
        profileIv = findViewById(R.id.profileIv);

        LinearLayoutManager linearLayoutManager=new LinearLayoutManager(this);
        linearLayoutManager.setStackFromEnd(true);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(linearLayoutManager);


        firebaseAuth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();

        FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
        id = firebaseUser.getUid();

        Intent intent = getIntent();
        hisid = intent.getStringExtra("hisid");

        reference = database.getReference("Users");

        Query query = reference.orderByChild("id").equalTo(hisid);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot ds : snapshot.getChildren()) {
                    String name = "" + ds.child("username").getValue();
                    hisimage = "" + ds.child("imageurl").getValue();
                    String typingstatus=""+ds.child("typingTo").getValue();
                    if(typingstatus.equals(id)){
                        userStatusTv.setText(("typing..."));
                    }
                    else {
                        String onlinestatus = "" + ds.child("onlineStatus").getValue();
                        if (onlinestatus.equals("online")) {
                            userStatusTv.setText(onlinestatus);
                        } else {
                            Calendar calendar = Calendar.getInstance(Locale.ENGLISH);
                            calendar.setTimeInMillis(Long.parseLong(onlinestatus));
                            String dateTime = DateFormat.format("dd/MM/yyyy hh:mm aa", calendar).toString();
                            userStatusTv.setText("Last seen at: " + dateTime);
                        }
                    }

                    nameTv.setText(name);
                    Picasso.get().load(hisimage).placeholder(R.drawable.ic_user).into(profileIv);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        sendBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String message = messageEt.getText().toString().trim();
                if (TextUtils.isEmpty(message)) {
                    Toast.makeText(ChatActivity.this, "Cannot send Empty Message!", Toast.LENGTH_LONG).show();
                } else {
                    sendMessage(message);
                }
            }
        });
        messageEt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                    if(charSequence.toString().trim().length()==0){
                        checktypingstatus("noOne");
                    }
                    else {
                        checktypingstatus(hisid);
                    }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
        readmessages();
        seenmessages();
    }

    private void seenmessages() {
        ur=FirebaseDatabase.getInstance().getReference("Chats");
        seenListener=ur.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot ds:snapshot.getChildren()) {
                    Chat chat=ds.getValue(Chat.class);
                    if(chat.getReceiver().equals(id) && chat.getSender().equals(hisid)){
                        HashMap<String,Object> hashMap=new HashMap<>();
                        hashMap.put("isSeen",true);
                        ds.getRef().updateChildren(hashMap);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
    private void checkonlinestatus(String status){
        DatabaseReference reference=FirebaseDatabase.getInstance().getReference("Users").child(id);
        HashMap<String,Object> hashMap=new HashMap<>();
        hashMap.put("onlineStatus",status);
        reference.updateChildren(hashMap);
    }
    private void checktypingstatus(String typing){
        DatabaseReference reference=FirebaseDatabase.getInstance().getReference("Users").child(id);
        HashMap<String,Object> hashMap=new HashMap<>();
        hashMap.put("typingTo",typing);
        reference.updateChildren(hashMap);
    }
    private void readmessages() {
       chatList=new ArrayList<>();
       DatabaseReference reference=FirebaseDatabase.getInstance().getReference("Chats");
       reference.addValueEventListener(new ValueEventListener() {
           @Override
           public void onDataChange(@NonNull DataSnapshot snapshot) {
               chatList.clear();
               for (DataSnapshot ds:snapshot.getChildren()) {
                   Chat chat=ds.getValue(Chat.class);
                   if(chat.getReceiver().equals(id) && chat.getSender().equals(hisid) ||chat.getReceiver().equals(hisid) && chat.getSender().equals(id)){
                       chatList.add(chat);
                   }
                   adapterChat=new AdapterChat(ChatActivity.this,chatList,hisimage);
                   adapterChat.notifyDataSetChanged();
                   recyclerView.setAdapter(adapterChat);
               }
           }

           @Override
           public void onCancelled(@NonNull DatabaseError error) {

           }
       });
    }

    private void sendMessage(String message) {
        DatabaseReference reference=FirebaseDatabase.getInstance().getReference();
        String timestamp=String.valueOf(System.currentTimeMillis());
        HashMap<String,Object> hashMap=new HashMap<>();
        hashMap.put("sender",id);
        hashMap.put("receiver",hisid);
        hashMap.put("message",message);
        hashMap.put("timestamp",timestamp);
        hashMap.put("isSeen",false);
        reference.child("Chats").push().setValue(hashMap);
        messageEt.setText("");
    }
    @Override
    public void onStart(){
        super.onStart();
        checkonlinestatus("online");
    }
    @Override
    public void onPause(){
        super.onPause();
        String timeStamp=String.valueOf(System.currentTimeMillis());
        checktypingstatus("noOne");
        checkonlinestatus(timeStamp);
        ur.removeEventListener(seenListener);
    }
    @Override
    public void onResume(){
        super.onResume();
        checkonlinestatus("online");
    }
}