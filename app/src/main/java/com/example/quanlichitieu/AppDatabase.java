package com.example.quanlichitieu;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.sqlite.db.SupportSQLiteDatabase;

import java.util.concurrent.Executors;

@Database(
        entities = {TransactionEntity.class, CategoryEntity.class},
        version = 2,
        exportSchema = false
)
public abstract class AppDatabase extends RoomDatabase {

    public abstract TransactionDao transactionDao();
    public abstract CategoryDao categoryDao();

    private static volatile AppDatabase INSTANCE;

    public static AppDatabase getDatabase(Context context) {

        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {

                if (INSTANCE == null) {
                    final Context appContext = context.getApplicationContext();
                    INSTANCE = Room.databaseBuilder(
                            appContext,
                            AppDatabase.class,
                            "moneynote_database"
                    )
                    .fallbackToDestructiveMigration()
                    .addCallback(new RoomDatabase.Callback() {
                        @Override
                        public void onCreate(@NonNull SupportSQLiteDatabase db) {
                            super.onCreate(db);
                            Executors.newSingleThreadExecutor().execute(() -> {
                                CategoryDao dao = getDatabase(appContext).categoryDao();
                                dao.insert(new CategoryEntity("Ăn uống", "Chi tiêu"));
                                dao.insert(new CategoryEntity("Học tập", "Chi tiêu"));
                                dao.insert(new CategoryEntity("Đi lại", "Chi tiêu"));
                                dao.insert(new CategoryEntity("Lương", "Thu nhập"));
                                dao.insert(new CategoryEntity("Thưởng", "Thu nhập"));
                                dao.insert(new CategoryEntity("Khác", "Chi tiêu"));
                            });
                        }
                    })
                    .build();
                }
            }
        }

        return INSTANCE;
    }
}
