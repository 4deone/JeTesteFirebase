package cm.deone.jetestefirebase;

import android.content.Intent;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class DashboardActivity extends AppCompatActivity {

    private FirebaseAuth firebaseAuth;

    private ActionBar actionBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        actionBar = getSupportActionBar();
        actionBar.setTitle("DASHBOARD");

        firebaseAuth = firebaseAuth.getInstance();

        BottomNavigationView navigationView = findViewById(R.id.design_navigation_view);
        navigationView.setOnNavigationItemSelectedListener(selectedListener);

        //Home Fragment transaction default start
        actionBar.setTitle("HOME");
        HomeFragment homeFragment = new HomeFragment();
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.content, homeFragment, "");
        fragmentTransaction.commit();

    }

    private BottomNavigationView.OnNavigationItemSelectedListener selectedListener = new BottomNavigationView.OnNavigationItemSelectedListener() {
        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
            switch (menuItem.getItemId()){
                case R.id.nav_home:
                    actionBar.setTitle("HOME");
                    HomeFragment homeFragment = new HomeFragment();
                    FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
                    fragmentTransaction.replace(R.id.content, homeFragment, "");
                    fragmentTransaction.commit();
                    return true;
                case R.id.nav_profile:
                    actionBar.setTitle("PROFILE");
                    ProfileFragment profileFragment = new ProfileFragment();
                    FragmentTransaction fragmentTransactionOne = getSupportFragmentManager().beginTransaction();
                    fragmentTransactionOne.replace(R.id.content, profileFragment, "");
                    fragmentTransactionOne.commit();
                    return true;
                case R.id.nav_users:
                    actionBar.setTitle("USERS");
                    UsersFragment usersFragment = new UsersFragment();
                    FragmentTransaction fragmentTransactionTwo = getSupportFragmentManager().beginTransaction();
                    fragmentTransactionTwo.replace(R.id.content, usersFragment, "");
                    fragmentTransactionTwo.commit();
                    return true;
                case R.id.nav_chats:
                    actionBar.setTitle("CHATS");
                    ChatListFragment chatListFragment = new ChatListFragment();
                    FragmentTransaction fragmentTransactionTrois = getSupportFragmentManager().beginTransaction();
                    fragmentTransactionTrois.replace(R.id.content, chatListFragment, "");
                    fragmentTransactionTrois.commit();
                    return true;
                    default:
            }
            return false;
        }
    };

    private void checkUserStatus(){
        FirebaseUser user = firebaseAuth.getCurrentUser();
        if (user != null){

        }else {
            startActivity(new Intent(DashboardActivity.this, MainActivity.class));
            finish(); return;
        }
    }

    @Override
    protected void onStart() {
        checkUserStatus();
        super.onStart();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish(); return;
    }
}
