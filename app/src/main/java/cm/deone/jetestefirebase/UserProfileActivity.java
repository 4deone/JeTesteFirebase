package cm.deone.jetestefirebase;

import android.content.Intent;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class UserProfileActivity extends AppCompatActivity {

    FirebaseAuth firebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle("User Profile");

        firebaseAuth = firebaseAuth.getInstance();
    }

    private void checkUserStatus(){
        FirebaseUser user = firebaseAuth.getCurrentUser();
        if (user != null){

        }else {
            startActivity(new Intent(UserProfileActivity.this, MainActivity.class));
            finish(); return;
        }
    }

    @Override
    protected void onStart() {
        checkUserStatus();
        super.onStart();
    }
}
