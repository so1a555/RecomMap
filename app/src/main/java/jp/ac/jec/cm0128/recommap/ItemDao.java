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

    @Query("SELECT * FROM items WHERE id = :id LIMIT 1")
    Item findById(int id);

    @Query("DELETE FROM items")
    void deleteAll();

    @Query("DELETE FROM items WHERE id = :id")
    void deleteById(int id);
}
