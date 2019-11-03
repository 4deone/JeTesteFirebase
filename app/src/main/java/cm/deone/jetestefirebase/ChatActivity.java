package cm.deone.jetestefirebase;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.blogspot.atifsoftwares.circularimageview.CircularImageView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.HashMap;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setTitle("");

        firebaseAuth = firebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();

        mRecycler = findViewById(R.id.rv_user_chat);

        mMessage = findViewById(R.id.edt_message);
        mSendMessage = findViewById(R.id.imgb_send);
        mUserAvatar = findViewById(R.id.im_avatar);
        mUserName = findViewById(R.id.tv_user_name);
        mStattus = findViewById(R.id.tv_user_status);

        Intent intent = getIntent();
        hisUid = intent.getStringExtra("hisUid");

        reference = database.getReference("users");
        Query userQuery = reference.orderByChild("uid").equalTo(hisUid);
        userQuery.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot ds: dataSnapshot.getChildren()){
                    String name = ""+ ds.child("name").getValue();
                    String image = ""+ ds.child("image").getValue();

                    mUserName.setText(name);
                    try{
                        Picasso.get().load(image).into(mUserAvatar);
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

    }

    private void sendMessage(String message) {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();

        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("sender", myUid);
        hashMap.put("receiver", hisUid);
        hashMap.put("message", message);

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

    @Override
    protected void onStart() {
        checkUserStatus();
        super.onStart();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);

        menu.findItem(R.id.app_bar_search).setVisible(false);

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
