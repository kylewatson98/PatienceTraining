package com.example.kyle.patiencetraining.MainUI;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import com.example.kyle.patiencetraining.Reward.LockedReward.LockedFragment;
import com.example.kyle.patiencetraining.R;
import com.example.kyle.patiencetraining.Reward.Reward;
import com.example.kyle.patiencetraining.Reward.RewardAsyncTask;
import com.example.kyle.patiencetraining.Util.NotificationService;
import com.example.kyle.patiencetraining.Util.User;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.ViewPager;

import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;

import java.util.List;

public class MainActivity extends AppCompatActivity{

    /**
     * Todo: make leaderboard layout
     *
     * Todo: update adapter to show hours,days,weeks?
     * Todo: show 'no rewards added' page if empty
     * Todo: pass arguments to fragments
     *
     * Todo: make settings menu get all options needed
     * Todo: Make activities/layouts for each option
     * Todo: Start theming/dimens sorting
     */

    private static final int ADD_REQUEST = 0;
    public static final String REWARD_EXTRA = "PatienceTrainingReward";
    private User user;

    private SectionsPagerAdapter mSectionsPagerAdapter;

    private Menu menu;
    private RewardAsyncTask.OnPostExecuteListener listener = new RewardAsyncTask.OnPostExecuteListener() {
        @Override
        public void onPostExecute(List<Reward> list) {
            mSectionsPagerAdapter.notifyDataSetChanged();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);


        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager(), 3);
        // Set up the ViewPager with the sections adapter.
        ViewPager mViewPager = findViewById(R.id.container);
        mViewPager.setAdapter(mSectionsPagerAdapter);
        Log.d("MainActivity", getSupportFragmentManager().getFragments().size() + "fragments");
        Intent intent = getIntent();
        long rewardId = intent.getLongExtra(NotificationService.REWARD_ID_EXTRA,-1);
        if(rewardId != -1)
            mViewPager.setCurrentItem(1);
        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivityForResult(new Intent(MainActivity.this, ModifyRewardActivity.class), ADD_REQUEST);
            }
        });
    }

    private void firestoreDB(final String uID){
        // Access a Cloud Firestore instance from your Activity
        final FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("users")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful() && task.getResult() != null) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                if(document.getId().equals(uID))
                                    user = document.toObject(User.class);
                                Log.d("Success", document.getId() + " => " + document.getData());
                            }
                        } else {
                            Log.w("Unsuccess", "Error getting documents.", task.getException());
                        }

                        user.totalTime += 22;
                        db.collection("users").document(uID)
                                .set(user);
                    }
                });

    }

    private void signOut(){
        Intent signInIntent = new Intent(this, LoginActivity.class);
        signInIntent.putExtra(LoginActivity.LOGIN_TASK_EXTRA, LoginActivity.LOGOUT_TASK);
        startActivityForResult(signInIntent, LoginActivity.LOGOUT_TASK);
    }

    private void signIn() {
        Intent signInIntent = new Intent(this, LoginActivity.class);
        signInIntent.putExtra(LoginActivity.LOGIN_TASK_EXTRA, LoginActivity.LOGIN_TASK);
        startActivityForResult(signInIntent, LoginActivity.LOGIN_TASK);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data){
        List<Fragment> frags = getSupportFragmentManager().getFragments();
        //Unmask fragment request code
        requestCode &= 0x0000ffff;
        for (Fragment frag:frags) {
            frag.onActivityResult(requestCode, resultCode, data);
        }
        Log.d("Request", ""+requestCode);
        switch (requestCode){
            case ADD_REQUEST:
                if(resultCode == RESULT_OK)
                    new RewardAsyncTask(this,RewardAsyncTask.TASK_INSERT_REWARDS,listener).execute((Reward)data.getParcelableExtra(REWARD_EXTRA));
                break;
            case LockedFragment.MOD_REQUEST:
                if(resultCode == Activity.RESULT_OK)
                    new RewardAsyncTask(this,RewardAsyncTask.TASK_UPDATE_REWARDS,listener).execute((Reward)data.getParcelableExtra(MainActivity.REWARD_EXTRA));
                break;
            case LoginActivity.LOGIN_TASK:
                if(resultCode == Activity.RESULT_OK){
                    String uID = data.getStringExtra(LoginActivity.UID_EXTRA);
                    firestoreDB(uID);
                    menu.findItem(R.id.action_signout).setVisible(true);
                    menu.findItem(R.id.action_signin).setVisible(false);
                    Snackbar snackbar = Snackbar.make(findViewById(R.id.mainLayout), R.string.success, Snackbar.LENGTH_LONG);
                    snackbar.setAction(R.string.signout, new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            signOut();
                        }
                    });
                    snackbar.show();
                }else{
                    Snackbar.make(findViewById(R.id.mainLayout), R.string.goog_error, Snackbar.LENGTH_LONG).show();
                }
                break;
            case LoginActivity.LOGOUT_TASK:
                if(resultCode == Activity.RESULT_OK){
                    menu.findItem(R.id.action_signout).setVisible(false);
                    menu.findItem(R.id.action_signin).setVisible(true);
                    Snackbar snackbar = Snackbar.make(findViewById(R.id.mainLayout), R.string.success_signout, Snackbar.LENGTH_LONG);
                    snackbar.setAction(R.string.common_signin_button_text, new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            signIn();
                        }
                    });
                    snackbar.show();
                }else{
                    Snackbar.make(findViewById(R.id.mainLayout), R.string.serv_error, Snackbar.LENGTH_LONG).show();
                }
                break;
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        this.menu = menu;
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        if(mAuth.getCurrentUser() != null){
            menu.findItem(R.id.action_signout).setVisible(true);
            menu.findItem(R.id.action_signin).setVisible(false);
        }else{
            menu.findItem(R.id.action_signout).setVisible(false);
            menu.findItem(R.id.action_signin).setVisible(true);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        switch(id){
            case R.id.action_settings:
                return true;
            case R.id.action_signin:
                // Configure Google Sign In
                signIn();
                return true;
            case R.id.action_signout:
                signOut();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
