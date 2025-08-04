package jp.ac.jec.cm0128.recommap;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

@Dao
public interface ItemDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void upsert(Item item);

    @Query("SELECT * FROM items")
    List<Item> getAll();

    @Query("DELETE FROM items")
    void deleteAll();
}
