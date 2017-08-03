package fontys.nl.friendspy;

/**
 * Created by Sami on 13-Jun-17.
 */


import android.location.Location;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import java.math.RoundingMode;
import java.text.DecimalFormat;

import static org.junit.Assert.assertEquals;


//@RunWith(MockitoJUnitRunner.class)
@RunWith(RobolectricTestRunner.class)
public class FriendSpyTests {

    private MapsActivity mA;
    private double ownLatitude;
    private double ownLongitude;
    private double friendLatitude;
    private double friendLongitude;
    private User user;
    private Location myLocation;


    @Before
    public void makeNewMapsActivity(){
        mA = new MapsActivity();
        ownLatitude = 51.452805;
        ownLongitude = 5.47600;
        friendLatitude = 51.452805;
        friendLongitude = 5.47630;
        user = new User("test@test.com", friendLatitude, friendLongitude, "Ted Tester");
        myLocation = new Location("test user");
        myLocation.setLatitude(ownLatitude);
        myLocation.setLongitude(ownLongitude);
    }


    @Test
    public void distanceAsDoubleTest() throws Exception {
        double distance = mA.distance(user, myLocation);
        System.out.println(distance);
        assertEquals(distance, 21.00, 1);
    }

    @Test
    public void distanceBetweenUsersTest() throws Exception {
        double distance = mA.calculateDistance(user,myLocation);
        // round to 2 decimals
        distance = (double)Math.round(distance * 100d) / 100d;
        System.out.println(distance);
        assertEquals(distance, 20.85, 0.1);
    }

}
