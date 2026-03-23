package com.mediease.app.database;

import android.database.Cursor;
import android.os.CancellationSignal;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.room.CoroutinesRoom;
import androidx.room.EntityInsertionAdapter;
import androidx.room.RoomDatabase;
import androidx.room.RoomSQLiteQuery;
import androidx.room.util.CursorUtil;
import androidx.room.util.DBUtil;
import androidx.sqlite.db.SupportSQLiteStatement;
import com.mediease.app.models.Patient;
import java.lang.Class;
import java.lang.Exception;
import java.lang.Long;
import java.lang.Object;
import java.lang.Override;
import java.lang.String;
import java.lang.SuppressWarnings;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import javax.annotation.processing.Generated;
import kotlin.coroutines.Continuation;

@Generated("androidx.room.RoomProcessor")
@SuppressWarnings({"unchecked", "deprecation"})
public final class PatientDao_Impl implements PatientDao {
  private final RoomDatabase __db;

  private final EntityInsertionAdapter<Patient> __insertionAdapterOfPatient;

  public PatientDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__insertionAdapterOfPatient = new EntityInsertionAdapter<Patient>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR REPLACE INTO `patients` (`id`,`name`,`age`,`email`,`password`,`bedTime`,`wakeTime`,`breakfastTime`,`lunchTime`,`dinnerTime`,`profileCode`) VALUES (nullif(?, 0),?,?,?,?,?,?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final Patient entity) {
        statement.bindLong(1, entity.getId());
        statement.bindString(2, entity.getName());
        statement.bindLong(3, entity.getAge());
        statement.bindString(4, entity.getEmail());
        statement.bindString(5, entity.getPassword());
        statement.bindString(6, entity.getBedTime());
        statement.bindString(7, entity.getWakeTime());
        statement.bindString(8, entity.getBreakfastTime());
        statement.bindString(9, entity.getLunchTime());
        statement.bindString(10, entity.getDinnerTime());
        statement.bindString(11, entity.getProfileCode());
      }
    };
  }

  @Override
  public Object insert(final Patient patient, final Continuation<? super Long> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Long>() {
      @Override
      @NonNull
      public Long call() throws Exception {
        __db.beginTransaction();
        try {
          final Long _result = __insertionAdapterOfPatient.insertAndReturnId(patient);
          __db.setTransactionSuccessful();
          return _result;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object getPatientById(final long id, final Continuation<? super Patient> $completion) {
    final String _sql = "SELECT * FROM patients WHERE id = ?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, id);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<Patient>() {
      @Override
      @Nullable
      public Patient call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfName = CursorUtil.getColumnIndexOrThrow(_cursor, "name");
          final int _cursorIndexOfAge = CursorUtil.getColumnIndexOrThrow(_cursor, "age");
          final int _cursorIndexOfEmail = CursorUtil.getColumnIndexOrThrow(_cursor, "email");
          final int _cursorIndexOfPassword = CursorUtil.getColumnIndexOrThrow(_cursor, "password");
          final int _cursorIndexOfBedTime = CursorUtil.getColumnIndexOrThrow(_cursor, "bedTime");
          final int _cursorIndexOfWakeTime = CursorUtil.getColumnIndexOrThrow(_cursor, "wakeTime");
          final int _cursorIndexOfBreakfastTime = CursorUtil.getColumnIndexOrThrow(_cursor, "breakfastTime");
          final int _cursorIndexOfLunchTime = CursorUtil.getColumnIndexOrThrow(_cursor, "lunchTime");
          final int _cursorIndexOfDinnerTime = CursorUtil.getColumnIndexOrThrow(_cursor, "dinnerTime");
          final int _cursorIndexOfProfileCode = CursorUtil.getColumnIndexOrThrow(_cursor, "profileCode");
          final Patient _result;
          if (_cursor.moveToFirst()) {
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final String _tmpName;
            _tmpName = _cursor.getString(_cursorIndexOfName);
            final int _tmpAge;
            _tmpAge = _cursor.getInt(_cursorIndexOfAge);
            final String _tmpEmail;
            _tmpEmail = _cursor.getString(_cursorIndexOfEmail);
            final String _tmpPassword;
            _tmpPassword = _cursor.getString(_cursorIndexOfPassword);
            final String _tmpBedTime;
            _tmpBedTime = _cursor.getString(_cursorIndexOfBedTime);
            final String _tmpWakeTime;
            _tmpWakeTime = _cursor.getString(_cursorIndexOfWakeTime);
            final String _tmpBreakfastTime;
            _tmpBreakfastTime = _cursor.getString(_cursorIndexOfBreakfastTime);
            final String _tmpLunchTime;
            _tmpLunchTime = _cursor.getString(_cursorIndexOfLunchTime);
            final String _tmpDinnerTime;
            _tmpDinnerTime = _cursor.getString(_cursorIndexOfDinnerTime);
            final String _tmpProfileCode;
            _tmpProfileCode = _cursor.getString(_cursorIndexOfProfileCode);
            _result = new Patient(_tmpId,_tmpName,_tmpAge,_tmpEmail,_tmpPassword,_tmpBedTime,_tmpWakeTime,_tmpBreakfastTime,_tmpLunchTime,_tmpDinnerTime,_tmpProfileCode);
          } else {
            _result = null;
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
  public Object getPatientByProfileCode(final String profileCode,
      final Continuation<? super Patient> $completion) {
    final String _sql = "SELECT * FROM patients WHERE profileCode = ?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindString(_argIndex, profileCode);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<Patient>() {
      @Override
      @Nullable
      public Patient call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfName = CursorUtil.getColumnIndexOrThrow(_cursor, "name");
          final int _cursorIndexOfAge = CursorUtil.getColumnIndexOrThrow(_cursor, "age");
          final int _cursorIndexOfEmail = CursorUtil.getColumnIndexOrThrow(_cursor, "email");
          final int _cursorIndexOfPassword = CursorUtil.getColumnIndexOrThrow(_cursor, "password");
          final int _cursorIndexOfBedTime = CursorUtil.getColumnIndexOrThrow(_cursor, "bedTime");
          final int _cursorIndexOfWakeTime = CursorUtil.getColumnIndexOrThrow(_cursor, "wakeTime");
          final int _cursorIndexOfBreakfastTime = CursorUtil.getColumnIndexOrThrow(_cursor, "breakfastTime");
          final int _cursorIndexOfLunchTime = CursorUtil.getColumnIndexOrThrow(_cursor, "lunchTime");
          final int _cursorIndexOfDinnerTime = CursorUtil.getColumnIndexOrThrow(_cursor, "dinnerTime");
          final int _cursorIndexOfProfileCode = CursorUtil.getColumnIndexOrThrow(_cursor, "profileCode");
          final Patient _result;
          if (_cursor.moveToFirst()) {
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final String _tmpName;
            _tmpName = _cursor.getString(_cursorIndexOfName);
            final int _tmpAge;
            _tmpAge = _cursor.getInt(_cursorIndexOfAge);
            final String _tmpEmail;
            _tmpEmail = _cursor.getString(_cursorIndexOfEmail);
            final String _tmpPassword;
            _tmpPassword = _cursor.getString(_cursorIndexOfPassword);
            final String _tmpBedTime;
            _tmpBedTime = _cursor.getString(_cursorIndexOfBedTime);
            final String _tmpWakeTime;
            _tmpWakeTime = _cursor.getString(_cursorIndexOfWakeTime);
            final String _tmpBreakfastTime;
            _tmpBreakfastTime = _cursor.getString(_cursorIndexOfBreakfastTime);
            final String _tmpLunchTime;
            _tmpLunchTime = _cursor.getString(_cursorIndexOfLunchTime);
            final String _tmpDinnerTime;
            _tmpDinnerTime = _cursor.getString(_cursorIndexOfDinnerTime);
            final String _tmpProfileCode;
            _tmpProfileCode = _cursor.getString(_cursorIndexOfProfileCode);
            _result = new Patient(_tmpId,_tmpName,_tmpAge,_tmpEmail,_tmpPassword,_tmpBedTime,_tmpWakeTime,_tmpBreakfastTime,_tmpLunchTime,_tmpDinnerTime,_tmpProfileCode);
          } else {
            _result = null;
          }
          return _result;
        } finally {
          _cursor.close();
          _statement.release();
        }
      }
    }, $completion);
  }

  @NonNull
  public static List<Class<?>> getRequiredConverters() {
    return Collections.emptyList();
  }
}
