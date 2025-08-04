package jp.ac.jec.cm0128.recommap;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "items")
public class Item {

    @PrimaryKey(autoGenerate = true)
    public int id;

    @NonNull public String name;
    @NonNull public String comment;
    public float rating;
    public double latitude;
    public double longitude;
    @NonNull public String category;

    public Item(@NonNull String name, @NonNull String comment, float rating,
                double latitude, double longitude, @NonNull String category) {
        this.name = name;
        this.comment = comment;
        this.rating = rating;
        this.latitude = latitude;
        this.longitude = longitude;
        this.category = category;
    }
}
