package jp.ac.jec.cm0128.recommap;

import androidx.room.TypeConverter;

public class Converters {
    @TypeConverter
    public static String fromCategory(ItemCategory category) {
        return category.name();
    }

    @TypeConverter
    public static ItemCategory toCategory(String name) {
        return ItemCategory.valueOf(name);
    }
}
