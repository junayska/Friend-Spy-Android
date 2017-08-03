package fontys.nl.friendspy;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class FriendsActivity extends AppCompatActivity
{
    private String mFireBaseUser;
    private ListView mListView;
    private ArrayList<Friend> friendArrayList;
    private FriendAdapter friendAdapter;
    private DatabaseReference ref;
    private TextView email;
    private List friendsList;
    private Friend f;
    private ToggleButton mButton;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friends);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // enabling action bar app icon and behaving it as toggle button
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);


        email = (TextView) findViewById(R.id.txtEmail);

        mListView=(ListView) findViewById(R.id.listView);
        friendArrayList =new ArrayList<>();
        /*LOGIN AUTOR NECESITEM QUE JUSSI FAGI PUSH*/
        FirebaseAuth mfirebaseAuth = FirebaseAuth.getInstance();
        mFireBaseUser = mfirebaseAuth.getCurrentUser().getUid();
        if(mFireBaseUser == null){
            //Not signed in, launch the sign In activity
            try{
                //startActivity(new Intent(this, singInActivity.class));
                finish();
                return;
            }
            catch (Exception e){}
        }
        /*friendAdapter = new FriendAdapter(FriendsActivity.this, friendArrayList);
        mListView.setAdapter(friendAdapter);*/

        ref = FirebaseDatabase.getInstance().getReference().child("users");
        ref.addListenerForSingleValueEvent(
                new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        for (DataSnapshot iterator: dataSnapshot.getChildren()) {
                            if(!dataSnapshot.child(mFireBaseUser).child("email").getValue().toString().equals(iterator.child("email").getValue().toString())){
                                friendArrayList.add(new Friend(iterator.child("name").getValue().toString(), iterator.child("email").getValue().toString()));
                            }
                        }
                        friendAdapter = new FriendAdapter(FriendsActivity.this, friendArrayList);
                        mListView.setAdapter(friendAdapter);
                        mListView.setTextFilterEnabled(true);

                        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener(){
                            @Override
                            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                                //whatever we do when we click one user
                            }
                        });
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        //handle databaseError
                    }
                });

        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            }
        });
    }


    @Override
    public boolean onCreateOptionsMenu( Menu menu) {
        getMenuInflater().inflate( R.menu.menu, menu);

        MenuItem myActionMenuItem = menu.findItem( R.id.action_search);
        final SearchView searchView = (SearchView) myActionMenuItem.getActionView();
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if (TextUtils.isEmpty(newText)) {
                    friendAdapter.getFilter().filter("");
                } else {
                    friendAdapter.getFilter().filter(newText);
                }
                mListView.setAdapter(friendAdapter);
                return true;
            }
        });

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_search) {
            return true;
        }
        else if(id == android.R.id.home){
            Intent i= new Intent(this, MapsActivity.class);
            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(i);
            finish();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent i= new Intent(this, MapsActivity.class);
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(i);
        finish();
    }
}
