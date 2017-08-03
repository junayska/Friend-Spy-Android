package fontys.nl.friendspy;

import android.location.Location;

/**
 * Created by Oletus on 6/3/2017.
 */

public class User {
    private Double longitude;
    private Double latitude;
    private String email;
    private String name;

    public User() {
    }

    public User(String email, Double latitude, Double longitude, String name) {
        this.email = email;
        this.name = name;
        this.latitude = latitude;
        this.longitude = longitude;
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

    public void setEmail(String eemail) {
        this.email = eemail;
    }

    public Double getLatitude() {
        return latitude;
    }

    public Double getLongitude() {
        return longitude;
    }
}

