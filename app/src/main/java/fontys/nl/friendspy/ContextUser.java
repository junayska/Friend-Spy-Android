package fontys.nl.friendspy;

import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

/**
 * Created by Àlex on 12/6/2017.
 */

public class ContextUser {

    private String id;
    private String name;
    private String email;
    private ArrayList<String> friends = new ArrayList<>();


    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public ArrayList<String> getFriends() {
     return friends;
    }

    public void setFriends(ArrayList<String> friends) {
        this.friends = friends;
    }



    private static ContextUser ourInstance = new ContextUser();

    public static ContextUser getInstance() {
        return ourInstance;
    }

    private ContextUser() {
    }

    public void addFriend(String email) {
        DatabaseReference ref;
        ref = FirebaseDatabase.getInstance().getReference().child("users");
        friends.add(email);
        ref.child(id).child("friends").setValue(friends);

    }

    public void deleteFriend(String email) {
        DatabaseReference ref;
        ref = FirebaseDatabase.getInstance().getReference().child("users");
        friends.remove(email);
        ref.child(id).child("friends").setValue(friends);
    }

    public boolean isFriend(String email) {
        for (int i = 0; i<friends.size(); ++i)
            if (friends.get(i).equals(email)) return true;
        return false;
    }
}
