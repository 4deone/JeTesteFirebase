package cm.deone.jetestefirebase;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.format.DateFormat;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

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

import cm.deone.jetestefirebase.adapter.ChatAdapter;
import cm.deone.jetestefirebase.model.Chat;

public class ChatActivity extends AppCompatActivity {

    private FirebaseAuth firebaseAuth;
    private FirebaseDatabase database;
    private DatabaseReference reference;

    private EditText mMessage;
    private ImageButton mSendMessage;
    private ImageView mUserAvatar;
    private TextView mUserName;
    private TextView mStattus;
    private RecyclerView mRecycler;
    private Toolbar mToolbar;

    String hisUid;
    String myUid;
    String hisImage;

    ValueEventListener seenListener;
    DatabaseReference referenceForSeen;


    List<Chat> chatList;
    ChatAdapter chatAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setTitle("");

        firebaseAuth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();

        mRecycler = findViewById(R.id.rv_user_chat);

        mMessage = findViewById(R.id.edt_message);
        mSendMessage = findViewById(R.id.imgb_send);
        mUserAvatar = findViewById(R.id.im_avatar);
        mUserName = findViewById(R.id.tv_user_name);
        mStattus = findViewById(R.id.tv_user_status);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setStackFromEnd(true);
        mRecycler.setHasFixedSize(true);
        mRecycler.setLayoutManager(linearLayoutManager);

        Intent intent = getIntent();
        hisUid = intent.getStringExtra("hisUid");

        reference = database.getReference("users");
        Query userQuery = reference.orderByChild("uid").equalTo(hisUid);
        userQuery.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot ds: dataSnapshot.getChildren()){
                    String name = ""+ ds.child("name").getValue();
                    hisImage = ""+ ds.child("image").getValue();
                    String  typingtatus = ""+ ds.child("typingTo").getValue();

                    if (typingtatus.equals(myUid)){
                        mStattus.setText("typing ...");
                    }else {
                        String  onlineStatus = ""+ ds.child("onlineStatus").getValue();
                        if (onlineStatus.equals("online")){
                            mStattus.setText(onlineStatus);
                        }else {
                            Calendar calendar = Calendar.getInstance(Locale.ENGLISH);
                            calendar.setTimeInMillis(Long.parseLong(onlineStatus));
                            String dateTime = DateFormat.format("dd/MM/yyyy hh:mm aa", calendar).toString();

                            mStattus.setText("Last seen at: "+ dateTime);
                        }
                    }

                    mUserName.setText(name);
                    try{
                        Picasso.get().load(hisImage).into(mUserAvatar);
                    }catch (Exception e){
                        Picasso.get().load(R.drawable.ic_default_img_white).into(mUserAvatar);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        mSendMessage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String message = mMessage.getText().toString().trim();
                if (!TextUtils.isEmpty(message)){
                    sendMessage(message);
                }else {
                    Toast.makeText(ChatActivity.this, "Cannot send the empty message...",
                            Toast.LENGTH_SHORT).show();
                }
            }
        });

        mMessage.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.toString().trim().length() == 0){
                    checkTypingStatus("noOne");
                }else {
                    checkTypingStatus(hisUid);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        readMessage();

        seenMeassge();

    }

    private void seenMeassge() {

        referenceForSeen = FirebaseDatabase.getInstance().getReference("chats");
        seenListener = referenceForSeen.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot ds: dataSnapshot.getChildren()){
                    Chat chat = ds.getValue(Chat.class);
                    if (chat.getReceiver().equals(myUid) && chat.getSender().equals(hisUid)){
                        HashMap<String, Object> hashMap = new HashMap<>();
                        hashMap.put("isSeen", true);
                        ds.getRef().updateChildren(hashMap);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void readMessage() {
        chatList = new ArrayList<>();
        DatabaseReference dr = FirebaseDatabase.getInstance().getReference("chats");
        dr.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                chatList.clear();
                for (DataSnapshot ds: dataSnapshot.getChildren()){
                    Chat chat = ds.getValue(Chat.class);
                    if (chat.getSender().equals(myUid) && chat.getReceiver().equals(hisUid) ||
                            chat.getSender().equals(hisUid) && chat.getReceiver().equals(myUid)){
                        chatList.add(chat);
                    }
                    chatAdapter = new ChatAdapter(ChatActivity.this, chatList, hisImage);
                    chatAdapter.notifyDataSetChanged();
                    mRecycler.setAdapter(chatAdapter);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void sendMessage(String message) {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();

        String timestamp = String.valueOf(System.currentTimeMillis());

        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("sender", myUid);
        hashMap.put("receiver", hisUid);
        hashMap.put("message", message);
        hashMap.put("timestamp", timestamp);
        hashMap.put("isSeen", false);

        databaseReference.child("chats").push().setValue(hashMap);

        mMessage.setText("");
    }

    private void checkUserStatus(){
        FirebaseUser user = firebaseAuth.getCurrentUser();
        if (user != null){
            myUid = user.getUid();
        }else {
            startActivity(new Intent(this, MainActivity.class));
            finish(); return;
        }
    }

    private void checkOnlineStatus(String status){
        DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference("users").child(myUid);

        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("onlineStatus", status);
        dbRef.updateChildren(hashMap);
    }

    private void checkTypingStatus(String typing){
        DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference("users").child(myUid);

        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("typingTo", typing);
        dbRef.updateChildren(hashMap);
    }

    @Override
    protected void onStart() {
        checkUserStatus();
        checkOnlineStatus("online");
        super.onStart();
    }

    @Override
    protected void onPause() {
        super.onPause();

        String timestammp = String.valueOf(System.currentTimeMillis());
        checkOnlineStatus(timestammp);
        checkTypingStatus("noOne");
        referenceForSeen.removeEventListener(seenListener);
    }

    @Override
    protected void onResume() {
        checkOnlineStatus("online");
        super.onResume();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);

        menu.findItem(R.id.menu_search).setVisible(false);
        menu.findItem(R.id.menu_add_post).setVisible(false);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.menu_logout){
            firebaseAuth.signOut();
            checkUserStatus();
        }
        return super.onOptionsItemSelected(item);
    }

}
