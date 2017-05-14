package ads.check.rate.exchange_rate_check.sqlite;


import android.provider.BaseColumns;

public final class ConversionResultContract {

    private ConversionResultContract() {}

    /* Inner class that defines the table contents */
    public static class ConversionEntry implements BaseColumns {
        public static final String TABLE_NAME = "results";
        public static final String COLUMN_NAME_BASE = "base";
        public static final String COLUMN_NAME_BASE_DESC = "base_desc";
        public static final String COLUMN_NAME_TARGET = "target";
        public static final String COLUMN_NAME_TARGET_DESC = "target_desc";
        public static final String COLUMN_NAME_PRICE = "price";
        public static final String COLUMN_NAME_VOLUME = "volume";
        public static final String COLUMN_NAME_CHANGE = "change";
        public static final String COLUMN_NAME_DATETIME = "datetime";
    }
}
