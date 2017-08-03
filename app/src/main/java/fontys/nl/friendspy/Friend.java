package fontys.nl.friendspy;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;

/**
 * Created by oriol on 6/3/2017.
 */

public class Friend {

    private String name;
    private String email;
    private ArrayList<Friend> friends;
    private String mFireBaseUser;

    public Friend(String name, String email){
        this.name = name;
        this.email = email;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setFriends(ArrayList<Friend> friends) {
        this.friends = friends;
    }

    public void setmFireBaseUser(String mFireBaseUser) {
        this.mFireBaseUser = mFireBaseUser;
    }

    public String getName(){
        return name;
    }

    public String getEmail(){
        return email;
    }

    public ArrayList<Friend > getFriends() {
        return friends;
    }

    public void addFriend(Friend f) {
        DatabaseReference ref;
        ref = FirebaseDatabase.getInstance().getReference().child("users");
        FirebaseAuth mfirebaseAuth = FirebaseAuth.getInstance();
        mFireBaseUser = mfirebaseAuth.getCurrentUser().getUid();
        friends.add(f);
        ref.child(mFireBaseUser).child("friends").setValue(friends);
    }

}
