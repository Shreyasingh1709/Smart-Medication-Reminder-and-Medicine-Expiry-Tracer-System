package com.mediease.app.database;

import android.database.Cursor;
import android.os.CancellationSignal;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.room.CoroutinesRoom;
import androidx.room.EntityDeletionOrUpdateAdapter;
import androidx.room.EntityInsertionAdapter;
import androidx.room.RoomDatabase;
import androidx.room.RoomSQLiteQuery;
import androidx.room.SharedSQLiteStatement;
import androidx.room.util.CursorUtil;
import androidx.room.util.DBUtil;
import androidx.sqlite.db.SupportSQLiteStatement;
import com.mediease.app.models.Reminder;
import java.lang.Class;
import java.lang.Exception;
import java.lang.Long;
import java.lang.Object;
import java.lang.Override;
import java.lang.String;
import java.lang.SuppressWarnings;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import javax.annotation.processing.Generated;
import kotlin.Unit;
import kotlin.coroutines.Continuation;

@Generated("androidx.room.RoomProcessor")
@SuppressWarnings({"unchecked", "deprecation"})
public final class ReminderDao_Impl implements ReminderDao {
  private final RoomDatabase __db;

  private final EntityInsertionAdapter<Reminder> __insertionAdapterOfReminder;

  private final EntityDeletionOrUpdateAdapter<Reminder> __deletionAdapterOfReminder;

  private final EntityDeletionOrUpdateAdapter<Reminder> __updateAdapterOfReminder;

  private final SharedSQLiteStatement __preparedStmtOfDeleteRemindersForMedicine;

  public ReminderDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__insertionAdapterOfReminder = new EntityInsertionAdapter<Reminder>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR REPLACE INTO `reminders` (`id`,`medicineId`,`medicineName`,`time`,`repeatDays`,`isEnabled`,`alarmRequestCode`,`createdAt`,`frequency`,`status`,`medicineImagePath`) VALUES (nullif(?, 0),?,?,?,?,?,?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final Reminder entity) {
        statement.bindLong(1, entity.getId());
        statement.bindLong(2, entity.getMedicineId());
        statement.bindString(3, entity.getMedicineName());
        statement.bindString(4, entity.getTime());
        statement.bindString(5, entity.getRepeatDays());
        final int _tmp = entity.isEnabled() ? 1 : 0;
        statement.bindLong(6, _tmp);
        statement.bindLong(7, entity.getAlarmRequestCode());
        statement.bindLong(8, entity.getCreatedAt());
        statement.bindString(9, entity.getFrequency());
        statement.bindString(10, entity.getStatus());
        if (entity.getMedicineImagePath() == null) {
          statement.bindNull(11);
        } else {
          statement.bindString(11, entity.getMedicineImagePath());
        }
      }
    };
    this.__deletionAdapterOfReminder = new EntityDeletionOrUpdateAdapter<Reminder>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "DELETE FROM `reminders` WHERE `id` = ?";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final Reminder entity) {
        statement.bindLong(1, entity.getId());
      }
    };
    this.__updateAdapterOfReminder = new EntityDeletionOrUpdateAdapter<Reminder>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "UPDATE OR ABORT `reminders` SET `id` = ?,`medicineId` = ?,`medicineName` = ?,`time` = ?,`repeatDays` = ?,`isEnabled` = ?,`alarmRequestCode` = ?,`createdAt` = ?,`frequency` = ?,`status` = ?,`medicineImagePath` = ? WHERE `id` = ?";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final Reminder entity) {
        statement.bindLong(1, entity.getId());
        statement.bindLong(2, entity.getMedicineId());
        statement.bindString(3, entity.getMedicineName());
        statement.bindString(4, entity.getTime());
        statement.bindString(5, entity.getRepeatDays());
        final int _tmp = entity.isEnabled() ? 1 : 0;
        statement.bindLong(6, _tmp);
        statement.bindLong(7, entity.getAlarmRequestCode());
        statement.bindLong(8, entity.getCreatedAt());
        statement.bindString(9, entity.getFrequency());
        statement.bindString(10, entity.getStatus());
        if (entity.getMedicineImagePath() == null) {
          statement.bindNull(11);
        } else {
          statement.bindString(11, entity.getMedicineImagePath());
        }
        statement.bindLong(12, entity.getId());
      }
    };
    this.__preparedStmtOfDeleteRemindersForMedicine = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "DELETE FROM reminders WHERE medicineId = ?";
        return _query;
      }
    };
  }

  @Override
  public Object insertReminder(final Reminder reminder,
      final Continuation<? super Long> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Long>() {
      @Override
      @NonNull
      public Long call() throws Exception {
        __db.beginTransaction();
        try {
          final Long _result = __insertionAdapterOfReminder.insertAndReturnId(reminder);
          __db.setTransactionSuccessful();
          return _result;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object deleteReminder(final Reminder reminder,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __deletionAdapterOfReminder.handle(reminder);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object updateReminder(final Reminder reminder,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __updateAdapterOfReminder.handle(reminder);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object deleteRemindersForMedicine(final long medicineId,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfDeleteRemindersForMedicine.acquire();
        int _argIndex = 1;
        _stmt.bindLong(_argIndex, medicineId);
        try {
          __db.beginTransaction();
          try {
            _stmt.executeUpdateDelete();
            __db.setTransactionSuccessful();
            return Unit.INSTANCE;
          } finally {
            __db.endTransaction();
          }
        } finally {
          __preparedStmtOfDeleteRemindersForMedicine.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public LiveData<List<Reminder>> getRemindersForMedicineLive(final long medicineId) {
    final String _sql = "SELECT * FROM reminders WHERE medicineId = ? ORDER BY time ASC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, medicineId);
    return __db.getInvalidationTracker().createLiveData(new String[] {"reminders"}, false, new Callable<List<Reminder>>() {
      @Override
      @Nullable
      public List<Reminder> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfMedicineId = CursorUtil.getColumnIndexOrThrow(_cursor, "medicineId");
          final int _cursorIndexOfMedicineName = CursorUtil.getColumnIndexOrThrow(_cursor, "medicineName");
          final int _cursorIndexOfTime = CursorUtil.getColumnIndexOrThrow(_cursor, "time");
          final int _cursorIndexOfRepeatDays = CursorUtil.getColumnIndexOrThrow(_cursor, "repeatDays");
          final int _cursorIndexOfIsEnabled = CursorUtil.getColumnIndexOrThrow(_cursor, "isEnabled");
          final int _cursorIndexOfAlarmRequestCode = CursorUtil.getColumnIndexOrThrow(_cursor, "alarmRequestCode");
          final int _cursorIndexOfCreatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "createdAt");
          final int _cursorIndexOfFrequency = CursorUtil.getColumnIndexOrThrow(_cursor, "frequency");
          final int _cursorIndexOfStatus = CursorUtil.getColumnIndexOrThrow(_cursor, "status");
          final int _cursorIndexOfMedicineImagePath = CursorUtil.getColumnIndexOrThrow(_cursor, "medicineImagePath");
          final List<Reminder> _result = new ArrayList<Reminder>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final Reminder _item;
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final long _tmpMedicineId;
            _tmpMedicineId = _cursor.getLong(_cursorIndexOfMedicineId);
            final String _tmpMedicineName;
            _tmpMedicineName = _cursor.getString(_cursorIndexOfMedicineName);
            final String _tmpTime;
            _tmpTime = _cursor.getString(_cursorIndexOfTime);
            final String _tmpRepeatDays;
            _tmpRepeatDays = _cursor.getString(_cursorIndexOfRepeatDays);
            final boolean _tmpIsEnabled;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfIsEnabled);
            _tmpIsEnabled = _tmp != 0;
            final int _tmpAlarmRequestCode;
            _tmpAlarmRequestCode = _cursor.getInt(_cursorIndexOfAlarmRequestCode);
            final long _tmpCreatedAt;
            _tmpCreatedAt = _cursor.getLong(_cursorIndexOfCreatedAt);
            final String _tmpFrequency;
            _tmpFrequency = _cursor.getString(_cursorIndexOfFrequency);
            final String _tmpStatus;
            _tmpStatus = _cursor.getString(_cursorIndexOfStatus);
            final String _tmpMedicineImagePath;
            if (_cursor.isNull(_cursorIndexOfMedicineImagePath)) {
              _tmpMedicineImagePath = null;
            } else {
              _tmpMedicineImagePath = _cursor.getString(_cursorIndexOfMedicineImagePath);
            }
            _item = new Reminder(_tmpId,_tmpMedicineId,_tmpMedicineName,_tmpTime,_tmpRepeatDays,_tmpIsEnabled,_tmpAlarmRequestCode,_tmpCreatedAt,_tmpFrequency,_tmpStatus,_tmpMedicineImagePath);
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
        }
      }

      @Override
      protected void finalize() {
        _statement.release();
      }
    });
  }

  @Override
  public Object getRemindersForMedicine(final long medicineId,
      final Continuation<? super List<Reminder>> $completion) {
    final String _sql = "SELECT * FROM reminders WHERE medicineId = ?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, medicineId);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<List<Reminder>>() {
      @Override
      @NonNull
      public List<Reminder> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfMedicineId = CursorUtil.getColumnIndexOrThrow(_cursor, "medicineId");
          final int _cursorIndexOfMedicineName = CursorUtil.getColumnIndexOrThrow(_cursor, "medicineName");
          final int _cursorIndexOfTime = CursorUtil.getColumnIndexOrThrow(_cursor, "time");
          final int _cursorIndexOfRepeatDays = CursorUtil.getColumnIndexOrThrow(_cursor, "repeatDays");
          final int _cursorIndexOfIsEnabled = CursorUtil.getColumnIndexOrThrow(_cursor, "isEnabled");
          final int _cursorIndexOfAlarmRequestCode = CursorUtil.getColumnIndexOrThrow(_cursor, "alarmRequestCode");
          final int _cursorIndexOfCreatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "createdAt");
          final int _cursorIndexOfFrequency = CursorUtil.getColumnIndexOrThrow(_cursor, "frequency");
          final int _cursorIndexOfStatus = CursorUtil.getColumnIndexOrThrow(_cursor, "status");
          final int _cursorIndexOfMedicineImagePath = CursorUtil.getColumnIndexOrThrow(_cursor, "medicineImagePath");
          final List<Reminder> _result = new ArrayList<Reminder>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final Reminder _item;
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final long _tmpMedicineId;
            _tmpMedicineId = _cursor.getLong(_cursorIndexOfMedicineId);
            final String _tmpMedicineName;
            _tmpMedicineName = _cursor.getString(_cursorIndexOfMedicineName);
            final String _tmpTime;
            _tmpTime = _cursor.getString(_cursorIndexOfTime);
            final String _tmpRepeatDays;
            _tmpRepeatDays = _cursor.getString(_cursorIndexOfRepeatDays);
            final boolean _tmpIsEnabled;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfIsEnabled);
            _tmpIsEnabled = _tmp != 0;
            final int _tmpAlarmRequestCode;
            _tmpAlarmRequestCode = _cursor.getInt(_cursorIndexOfAlarmRequestCode);
            final long _tmpCreatedAt;
            _tmpCreatedAt = _cursor.getLong(_cursorIndexOfCreatedAt);
            final String _tmpFrequency;
            _tmpFrequency = _cursor.getString(_cursorIndexOfFrequency);
            final String _tmpStatus;
            _tmpStatus = _cursor.getString(_cursorIndexOfStatus);
            final String _tmpMedicineImagePath;
            if (_cursor.isNull(_cursorIndexOfMedicineImagePath)) {
              _tmpMedicineImagePath = null;
            } else {
              _tmpMedicineImagePath = _cursor.getString(_cursorIndexOfMedicineImagePath);
            }
            _item = new Reminder(_tmpId,_tmpMedicineId,_tmpMedicineName,_tmpTime,_tmpRepeatDays,_tmpIsEnabled,_tmpAlarmRequestCode,_tmpCreatedAt,_tmpFrequency,_tmpStatus,_tmpMedicineImagePath);
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
          _statement.release();
        }
      }
    }, $completion);
  }

  @Override
  public LiveData<List<Reminder>> getAllRemindersLive() {
    final String _sql = "SELECT * FROM reminders ORDER BY time ASC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    return __db.getInvalidationTracker().createLiveData(new String[] {"reminders"}, false, new Callable<List<Reminder>>() {
      @Override
      @Nullable
      public List<Reminder> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfMedicineId = CursorUtil.getColumnIndexOrThrow(_cursor, "medicineId");
          final int _cursorIndexOfMedicineName = CursorUtil.getColumnIndexOrThrow(_cursor, "medicineName");
          final int _cursorIndexOfTime = CursorUtil.getColumnIndexOrThrow(_cursor, "time");
          final int _cursorIndexOfRepeatDays = CursorUtil.getColumnIndexOrThrow(_cursor, "repeatDays");
          final int _cursorIndexOfIsEnabled = CursorUtil.getColumnIndexOrThrow(_cursor, "isEnabled");
          final int _cursorIndexOfAlarmRequestCode = CursorUtil.getColumnIndexOrThrow(_cursor, "alarmRequestCode");
          final int _cursorIndexOfCreatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "createdAt");
          final int _cursorIndexOfFrequency = CursorUtil.getColumnIndexOrThrow(_cursor, "frequency");
          final int _cursorIndexOfStatus = CursorUtil.getColumnIndexOrThrow(_cursor, "status");
          final int _cursorIndexOfMedicineImagePath = CursorUtil.getColumnIndexOrThrow(_cursor, "medicineImagePath");
          final List<Reminder> _result = new ArrayList<Reminder>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final Reminder _item;
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final long _tmpMedicineId;
            _tmpMedicineId = _cursor.getLong(_cursorIndexOfMedicineId);
            final String _tmpMedicineName;
            _tmpMedicineName = _cursor.getString(_cursorIndexOfMedicineName);
            final String _tmpTime;
            _tmpTime = _cursor.getString(_cursorIndexOfTime);
            final String _tmpRepeatDays;
            _tmpRepeatDays = _cursor.getString(_cursorIndexOfRepeatDays);
            final boolean _tmpIsEnabled;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfIsEnabled);
            _tmpIsEnabled = _tmp != 0;
            final int _tmpAlarmRequestCode;
            _tmpAlarmRequestCode = _cursor.getInt(_cursorIndexOfAlarmRequestCode);
            final long _tmpCreatedAt;
            _tmpCreatedAt = _cursor.getLong(_cursorIndexOfCreatedAt);
            final String _tmpFrequency;
            _tmpFrequency = _cursor.getString(_cursorIndexOfFrequency);
            final String _tmpStatus;
            _tmpStatus = _cursor.getString(_cursorIndexOfStatus);
            final String _tmpMedicineImagePath;
            if (_cursor.isNull(_cursorIndexOfMedicineImagePath)) {
              _tmpMedicineImagePath = null;
            } else {
              _tmpMedicineImagePath = _cursor.getString(_cursorIndexOfMedicineImagePath);
            }
            _item = new Reminder(_tmpId,_tmpMedicineId,_tmpMedicineName,_tmpTime,_tmpRepeatDays,_tmpIsEnabled,_tmpAlarmRequestCode,_tmpCreatedAt,_tmpFrequency,_tmpStatus,_tmpMedicineImagePath);
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
        }
      }

      @Override
      protected void finalize() {
        _statement.release();
      }
    });
  }

  @Override
  public Object getAllReminders(final Continuation<? super List<Reminder>> $completion) {
    final String _sql = "SELECT * FROM reminders";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<List<Reminder>>() {
      @Override
      @NonNull
      public List<Reminder> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfMedicineId = CursorUtil.getColumnIndexOrThrow(_cursor, "medicineId");
          final int _cursorIndexOfMedicineName = CursorUtil.getColumnIndexOrThrow(_cursor, "medicineName");
          final int _cursorIndexOfTime = CursorUtil.getColumnIndexOrThrow(_cursor, "time");
          final int _cursorIndexOfRepeatDays = CursorUtil.getColumnIndexOrThrow(_cursor, "repeatDays");
          final int _cursorIndexOfIsEnabled = CursorUtil.getColumnIndexOrThrow(_cursor, "isEnabled");
          final int _cursorIndexOfAlarmRequestCode = CursorUtil.getColumnIndexOrThrow(_cursor, "alarmRequestCode");
          final int _cursorIndexOfCreatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "createdAt");
          final int _cursorIndexOfFrequency = CursorUtil.getColumnIndexOrThrow(_cursor, "frequency");
          final int _cursorIndexOfStatus = CursorUtil.getColumnIndexOrThrow(_cursor, "status");
          final int _cursorIndexOfMedicineImagePath = CursorUtil.getColumnIndexOrThrow(_cursor, "medicineImagePath");
          final List<Reminder> _result = new ArrayList<Reminder>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final Reminder _item;
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final long _tmpMedicineId;
            _tmpMedicineId = _cursor.getLong(_cursorIndexOfMedicineId);
            final String _tmpMedicineName;
            _tmpMedicineName = _cursor.getString(_cursorIndexOfMedicineName);
            final String _tmpTime;
            _tmpTime = _cursor.getString(_cursorIndexOfTime);
            final String _tmpRepeatDays;
            _tmpRepeatDays = _cursor.getString(_cursorIndexOfRepeatDays);
            final boolean _tmpIsEnabled;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfIsEnabled);
            _tmpIsEnabled = _tmp != 0;
            final int _tmpAlarmRequestCode;
            _tmpAlarmRequestCode = _cursor.getInt(_cursorIndexOfAlarmRequestCode);
            final long _tmpCreatedAt;
            _tmpCreatedAt = _cursor.getLong(_cursorIndexOfCreatedAt);
            final String _tmpFrequency;
            _tmpFrequency = _cursor.getString(_cursorIndexOfFrequency);
            final String _tmpStatus;
            _tmpStatus = _cursor.getString(_cursorIndexOfStatus);
            final String _tmpMedicineImagePath;
            if (_cursor.isNull(_cursorIndexOfMedicineImagePath)) {
              _tmpMedicineImagePath = null;
            } else {
              _tmpMedicineImagePath = _cursor.getString(_cursorIndexOfMedicineImagePath);
            }
            _item = new Reminder(_tmpId,_tmpMedicineId,_tmpMedicineName,_tmpTime,_tmpRepeatDays,_tmpIsEnabled,_tmpAlarmRequestCode,_tmpCreatedAt,_tmpFrequency,_tmpStatus,_tmpMedicineImagePath);
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
          _statement.release();
        }
      }
    }, $completion);
  }

  @Override
  public LiveData<List<Reminder>> getActiveReminders() {
    final String _sql = "SELECT * FROM reminders WHERE isEnabled = 1 ORDER BY time ASC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    return __db.getInvalidationTracker().createLiveData(new String[] {"reminders"}, false, new Callable<List<Reminder>>() {
      @Override
      @Nullable
      public List<Reminder> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfMedicineId = CursorUtil.getColumnIndexOrThrow(_cursor, "medicineId");
          final int _cursorIndexOfMedicineName = CursorUtil.getColumnIndexOrThrow(_cursor, "medicineName");
          final int _cursorIndexOfTime = CursorUtil.getColumnIndexOrThrow(_cursor, "time");
          final int _cursorIndexOfRepeatDays = CursorUtil.getColumnIndexOrThrow(_cursor, "repeatDays");
          final int _cursorIndexOfIsEnabled = CursorUtil.getColumnIndexOrThrow(_cursor, "isEnabled");
          final int _cursorIndexOfAlarmRequestCode = CursorUtil.getColumnIndexOrThrow(_cursor, "alarmRequestCode");
          final int _cursorIndexOfCreatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "createdAt");
          final int _cursorIndexOfFrequency = CursorUtil.getColumnIndexOrThrow(_cursor, "frequency");
          final int _cursorIndexOfStatus = CursorUtil.getColumnIndexOrThrow(_cursor, "status");
          final int _cursorIndexOfMedicineImagePath = CursorUtil.getColumnIndexOrThrow(_cursor, "medicineImagePath");
          final List<Reminder> _result = new ArrayList<Reminder>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final Reminder _item;
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final long _tmpMedicineId;
            _tmpMedicineId = _cursor.getLong(_cursorIndexOfMedicineId);
            final String _tmpMedicineName;
            _tmpMedicineName = _cursor.getString(_cursorIndexOfMedicineName);
            final String _tmpTime;
            _tmpTime = _cursor.getString(_cursorIndexOfTime);
            final String _tmpRepeatDays;
            _tmpRepeatDays = _cursor.getString(_cursorIndexOfRepeatDays);
            final boolean _tmpIsEnabled;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfIsEnabled);
            _tmpIsEnabled = _tmp != 0;
            final int _tmpAlarmRequestCode;
            _tmpAlarmRequestCode = _cursor.getInt(_cursorIndexOfAlarmRequestCode);
            final long _tmpCreatedAt;
            _tmpCreatedAt = _cursor.getLong(_cursorIndexOfCreatedAt);
            final String _tmpFrequency;
            _tmpFrequency = _cursor.getString(_cursorIndexOfFrequency);
            final String _tmpStatus;
            _tmpStatus = _cursor.getString(_cursorIndexOfStatus);
            final String _tmpMedicineImagePath;
            if (_cursor.isNull(_cursorIndexOfMedicineImagePath)) {
              _tmpMedicineImagePath = null;
            } else {
              _tmpMedicineImagePath = _cursor.getString(_cursorIndexOfMedicineImagePath);
            }
            _item = new Reminder(_tmpId,_tmpMedicineId,_tmpMedicineName,_tmpTime,_tmpRepeatDays,_tmpIsEnabled,_tmpAlarmRequestCode,_tmpCreatedAt,_tmpFrequency,_tmpStatus,_tmpMedicineImagePath);
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
        }
      }

      @Override
      protected void finalize() {
        _statement.release();
      }
    });
  }

  @NonNull
  public static List<Class<?>> getRequiredConverters() {
    return Collections.emptyList();
  }
}
