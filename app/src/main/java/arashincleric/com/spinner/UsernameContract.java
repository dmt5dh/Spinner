package arashincleric.com.spinner;

import android.provider.BaseColumns;

public class UsernameContract {
    public UsernameContract(){}

    /* Inner class that defines the table contents */
    public static abstract class Usernames implements BaseColumns {
        public static final String TABLE_NAME = "UserID";
        public static final String COLUMN_NAME_ENTRY_ID = "id";
    }
}
