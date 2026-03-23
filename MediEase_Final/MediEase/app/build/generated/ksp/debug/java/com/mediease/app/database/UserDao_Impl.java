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
import androidx.room.util.CursorUtil;
import androidx.room.util.DBUtil;
import androidx.sqlite.db.SupportSQLiteStatement;
import com.mediease.app.models.User;
import java.lang.Class;
import java.lang.Exception;
import java.lang.Object;
import java.lang.Override;
import java.lang.String;
import java.lang.SuppressWarnings;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import javax.annotation.processing.Generated;
import kotlin.Unit;
import kotlin.coroutines.Continuation;

@Generated("androidx.room.RoomProcessor")
@SuppressWarnings({"unchecked", "deprecation"})
public final class UserDao_Impl implements UserDao {
  private final RoomDatabase __db;

  private final EntityInsertionAdapter<User> __insertionAdapterOfUser;

  private final EntityDeletionOrUpdateAdapter<User> __updateAdapterOfUser;

  public UserDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__insertionAdapterOfUser = new EntityInsertionAdapter<User>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR REPLACE INTO `users` (`id`,`name`,`email`,`password`,`role`,`dob`,`age`,`ageRange`,`wakeUpTime`,`bedTime`,`breakfastTime`,`lunchTime`,`dinnerTime`,`profileCode`,`linkedUserId`,`avatarPath`,`createdAt`) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final User entity) {
        statement.bindString(1, entity.getId());
        statement.bindString(2, entity.getName());
        statement.bindString(3, entity.getEmail());
        statement.bindString(4, entity.getPassword());
        statement.bindString(5, entity.getRole());
        statement.bindLong(6, entity.getDob());
        statement.bindLong(7, entity.getAge());
        statement.bindString(8, entity.getAgeRange());
        statement.bindString(9, entity.getWakeUpTime());
        statement.bindString(10, entity.getBedTime());
        statement.bindString(11, entity.getBreakfastTime());
        statement.bindString(12, entity.getLunchTime());
        statement.bindString(13, entity.getDinnerTime());
        statement.bindString(14, entity.getProfileCode());
        statement.bindString(15, entity.getLinkedUserId());
        if (entity.getAvatarPath() == null) {
          statement.bindNull(16);
        } else {
          statement.bindString(16, entity.getAvatarPath());
        }
        statement.bindLong(17, entity.getCreatedAt());
      }
    };
    this.__updateAdapterOfUser = new EntityDeletionOrUpdateAdapter<User>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "UPDATE OR ABORT `users` SET `id` = ?,`name` = ?,`email` = ?,`password` = ?,`role` = ?,`dob` = ?,`age` = ?,`ageRange` = ?,`wakeUpTime` = ?,`bedTime` = ?,`breakfastTime` = ?,`lunchTime` = ?,`dinnerTime` = ?,`profileCode` = ?,`linkedUserId` = ?,`avatarPath` = ?,`createdAt` = ? WHERE `id` = ?";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final User entity) {
        statement.bindString(1, entity.getId());
        statement.bindString(2, entity.getName());
        statement.bindString(3, entity.getEmail());
        statement.bindString(4, entity.getPassword());
        statement.bindString(5, entity.getRole());
        statement.bindLong(6, entity.getDob());
        statement.bindLong(7, entity.getAge());
        statement.bindString(8, entity.getAgeRange());
        statement.bindString(9, entity.getWakeUpTime());
        statement.bindString(10, entity.getBedTime());
        statement.bindString(11, entity.getBreakfastTime());
        statement.bindString(12, entity.getLunchTime());
        statement.bindString(13, entity.getDinnerTime());
        statement.bindString(14, entity.getProfileCode());
        statement.bindString(15, entity.getLinkedUserId());
        if (entity.getAvatarPath() == null) {
          statement.bindNull(16);
        } else {
          statement.bindString(16, entity.getAvatarPath());
        }
        statement.bindLong(17, entity.getCreatedAt());
        statement.bindString(18, entity.getId());
      }
    };
  }

  @Override
  public Object insertUser(final User user, final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __insertionAdapterOfUser.insert(user);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object updateUser(final User user, final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __updateAdapterOfUser.handle(user);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public LiveData<User> getUserById(final String userId) {
    final String _sql = "SELECT * FROM users WHERE id = ?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindString(_argIndex, userId);
    return __db.getInvalidationTracker().createLiveData(new String[] {"users"}, false, new Callable<User>() {
      @Override
      @Nullable
      public User call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfName = CursorUtil.getColumnIndexOrThrow(_cursor, "name");
          final int _cursorIndexOfEmail = CursorUtil.getColumnIndexOrThrow(_cursor, "email");
          final int _cursorIndexOfPassword = CursorUtil.getColumnIndexOrThrow(_cursor, "password");
          final int _cursorIndexOfRole = CursorUtil.getColumnIndexOrThrow(_cursor, "role");
          final int _cursorIndexOfDob = CursorUtil.getColumnIndexOrThrow(_cursor, "dob");
          final int _cursorIndexOfAge = CursorUtil.getColumnIndexOrThrow(_cursor, "age");
          final int _cursorIndexOfAgeRange = CursorUtil.getColumnIndexOrThrow(_cursor, "ageRange");
          final int _cursorIndexOfWakeUpTime = CursorUtil.getColumnIndexOrThrow(_cursor, "wakeUpTime");
          final int _cursorIndexOfBedTime = CursorUtil.getColumnIndexOrThrow(_cursor, "bedTime");
          final int _cursorIndexOfBreakfastTime = CursorUtil.getColumnIndexOrThrow(_cursor, "breakfastTime");
          final int _cursorIndexOfLunchTime = CursorUtil.getColumnIndexOrThrow(_cursor, "lunchTime");
          final int _cursorIndexOfDinnerTime = CursorUtil.getColumnIndexOrThrow(_cursor, "dinnerTime");
          final int _cursorIndexOfProfileCode = CursorUtil.getColumnIndexOrThrow(_cursor, "profileCode");
          final int _cursorIndexOfLinkedUserId = CursorUtil.getColumnIndexOrThrow(_cursor, "linkedUserId");
          final int _cursorIndexOfAvatarPath = CursorUtil.getColumnIndexOrThrow(_cursor, "avatarPath");
          final int _cursorIndexOfCreatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "createdAt");
          final User _result;
          if (_cursor.moveToFirst()) {
            final String _tmpId;
            _tmpId = _cursor.getString(_cursorIndexOfId);
            final String _tmpName;
            _tmpName = _cursor.getString(_cursorIndexOfName);
            final String _tmpEmail;
            _tmpEmail = _cursor.getString(_cursorIndexOfEmail);
            final String _tmpPassword;
            _tmpPassword = _cursor.getString(_cursorIndexOfPassword);
            final String _tmpRole;
            _tmpRole = _cursor.getString(_cursorIndexOfRole);
            final long _tmpDob;
            _tmpDob = _cursor.getLong(_cursorIndexOfDob);
            final int _tmpAge;
            _tmpAge = _cursor.getInt(_cursorIndexOfAge);
            final String _tmpAgeRange;
            _tmpAgeRange = _cursor.getString(_cursorIndexOfAgeRange);
            final String _tmpWakeUpTime;
            _tmpWakeUpTime = _cursor.getString(_cursorIndexOfWakeUpTime);
            final String _tmpBedTime;
            _tmpBedTime = _cursor.getString(_cursorIndexOfBedTime);
            final String _tmpBreakfastTime;
            _tmpBreakfastTime = _cursor.getString(_cursorIndexOfBreakfastTime);
            final String _tmpLunchTime;
            _tmpLunchTime = _cursor.getString(_cursorIndexOfLunchTime);
            final String _tmpDinnerTime;
            _tmpDinnerTime = _cursor.getString(_cursorIndexOfDinnerTime);
            final String _tmpProfileCode;
            _tmpProfileCode = _cursor.getString(_cursorIndexOfProfileCode);
            final String _tmpLinkedUserId;
            _tmpLinkedUserId = _cursor.getString(_cursorIndexOfLinkedUserId);
            final String _tmpAvatarPath;
            if (_cursor.isNull(_cursorIndexOfAvatarPath)) {
              _tmpAvatarPath = null;
            } else {
              _tmpAvatarPath = _cursor.getString(_cursorIndexOfAvatarPath);
            }
            final long _tmpCreatedAt;
            _tmpCreatedAt = _cursor.getLong(_cursorIndexOfCreatedAt);
            _result = new User(_tmpId,_tmpName,_tmpEmail,_tmpPassword,_tmpRole,_tmpDob,_tmpAge,_tmpAgeRange,_tmpWakeUpTime,_tmpBedTime,_tmpBreakfastTime,_tmpLunchTime,_tmpDinnerTime,_tmpProfileCode,_tmpLinkedUserId,_tmpAvatarPath,_tmpCreatedAt);
          } else {
            _result = null;
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
  public Object getUserByIdSync(final String userId, final Continuation<? super User> $completion) {
    final String _sql = "SELECT * FROM users WHERE id = ?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindString(_argIndex, userId);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<User>() {
      @Override
      @Nullable
      public User call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfName = CursorUtil.getColumnIndexOrThrow(_cursor, "name");
          final int _cursorIndexOfEmail = CursorUtil.getColumnIndexOrThrow(_cursor, "email");
          final int _cursorIndexOfPassword = CursorUtil.getColumnIndexOrThrow(_cursor, "password");
          final int _cursorIndexOfRole = CursorUtil.getColumnIndexOrThrow(_cursor, "role");
          final int _cursorIndexOfDob = CursorUtil.getColumnIndexOrThrow(_cursor, "dob");
          final int _cursorIndexOfAge = CursorUtil.getColumnIndexOrThrow(_cursor, "age");
          final int _cursorIndexOfAgeRange = CursorUtil.getColumnIndexOrThrow(_cursor, "ageRange");
          final int _cursorIndexOfWakeUpTime = CursorUtil.getColumnIndexOrThrow(_cursor, "wakeUpTime");
          final int _cursorIndexOfBedTime = CursorUtil.getColumnIndexOrThrow(_cursor, "bedTime");
          final int _cursorIndexOfBreakfastTime = CursorUtil.getColumnIndexOrThrow(_cursor, "breakfastTime");
          final int _cursorIndexOfLunchTime = CursorUtil.getColumnIndexOrThrow(_cursor, "lunchTime");
          final int _cursorIndexOfDinnerTime = CursorUtil.getColumnIndexOrThrow(_cursor, "dinnerTime");
          final int _cursorIndexOfProfileCode = CursorUtil.getColumnIndexOrThrow(_cursor, "profileCode");
          final int _cursorIndexOfLinkedUserId = CursorUtil.getColumnIndexOrThrow(_cursor, "linkedUserId");
          final int _cursorIndexOfAvatarPath = CursorUtil.getColumnIndexOrThrow(_cursor, "avatarPath");
          final int _cursorIndexOfCreatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "createdAt");
          final User _result;
          if (_cursor.moveToFirst()) {
            final String _tmpId;
            _tmpId = _cursor.getString(_cursorIndexOfId);
            final String _tmpName;
            _tmpName = _cursor.getString(_cursorIndexOfName);
            final String _tmpEmail;
            _tmpEmail = _cursor.getString(_cursorIndexOfEmail);
            final String _tmpPassword;
            _tmpPassword = _cursor.getString(_cursorIndexOfPassword);
            final String _tmpRole;
            _tmpRole = _cursor.getString(_cursorIndexOfRole);
            final long _tmpDob;
            _tmpDob = _cursor.getLong(_cursorIndexOfDob);
            final int _tmpAge;
            _tmpAge = _cursor.getInt(_cursorIndexOfAge);
            final String _tmpAgeRange;
            _tmpAgeRange = _cursor.getString(_cursorIndexOfAgeRange);
            final String _tmpWakeUpTime;
            _tmpWakeUpTime = _cursor.getString(_cursorIndexOfWakeUpTime);
            final String _tmpBedTime;
            _tmpBedTime = _cursor.getString(_cursorIndexOfBedTime);
            final String _tmpBreakfastTime;
            _tmpBreakfastTime = _cursor.getString(_cursorIndexOfBreakfastTime);
            final String _tmpLunchTime;
            _tmpLunchTime = _cursor.getString(_cursorIndexOfLunchTime);
            final String _tmpDinnerTime;
            _tmpDinnerTime = _cursor.getString(_cursorIndexOfDinnerTime);
            final String _tmpProfileCode;
            _tmpProfileCode = _cursor.getString(_cursorIndexOfProfileCode);
            final String _tmpLinkedUserId;
            _tmpLinkedUserId = _cursor.getString(_cursorIndexOfLinkedUserId);
            final String _tmpAvatarPath;
            if (_cursor.isNull(_cursorIndexOfAvatarPath)) {
              _tmpAvatarPath = null;
            } else {
              _tmpAvatarPath = _cursor.getString(_cursorIndexOfAvatarPath);
            }
            final long _tmpCreatedAt;
            _tmpCreatedAt = _cursor.getLong(_cursorIndexOfCreatedAt);
            _result = new User(_tmpId,_tmpName,_tmpEmail,_tmpPassword,_tmpRole,_tmpDob,_tmpAge,_tmpAgeRange,_tmpWakeUpTime,_tmpBedTime,_tmpBreakfastTime,_tmpLunchTime,_tmpDinnerTime,_tmpProfileCode,_tmpLinkedUserId,_tmpAvatarPath,_tmpCreatedAt);
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
  public Object getUserByEmail(final String email, final Continuation<? super User> $completion) {
    final String _sql = "SELECT * FROM users WHERE email = ? LIMIT 1";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindString(_argIndex, email);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<User>() {
      @Override
      @Nullable
      public User call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfName = CursorUtil.getColumnIndexOrThrow(_cursor, "name");
          final int _cursorIndexOfEmail = CursorUtil.getColumnIndexOrThrow(_cursor, "email");
          final int _cursorIndexOfPassword = CursorUtil.getColumnIndexOrThrow(_cursor, "password");
          final int _cursorIndexOfRole = CursorUtil.getColumnIndexOrThrow(_cursor, "role");
          final int _cursorIndexOfDob = CursorUtil.getColumnIndexOrThrow(_cursor, "dob");
          final int _cursorIndexOfAge = CursorUtil.getColumnIndexOrThrow(_cursor, "age");
          final int _cursorIndexOfAgeRange = CursorUtil.getColumnIndexOrThrow(_cursor, "ageRange");
          final int _cursorIndexOfWakeUpTime = CursorUtil.getColumnIndexOrThrow(_cursor, "wakeUpTime");
          final int _cursorIndexOfBedTime = CursorUtil.getColumnIndexOrThrow(_cursor, "bedTime");
          final int _cursorIndexOfBreakfastTime = CursorUtil.getColumnIndexOrThrow(_cursor, "breakfastTime");
          final int _cursorIndexOfLunchTime = CursorUtil.getColumnIndexOrThrow(_cursor, "lunchTime");
          final int _cursorIndexOfDinnerTime = CursorUtil.getColumnIndexOrThrow(_cursor, "dinnerTime");
          final int _cursorIndexOfProfileCode = CursorUtil.getColumnIndexOrThrow(_cursor, "profileCode");
          final int _cursorIndexOfLinkedUserId = CursorUtil.getColumnIndexOrThrow(_cursor, "linkedUserId");
          final int _cursorIndexOfAvatarPath = CursorUtil.getColumnIndexOrThrow(_cursor, "avatarPath");
          final int _cursorIndexOfCreatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "createdAt");
          final User _result;
          if (_cursor.moveToFirst()) {
            final String _tmpId;
            _tmpId = _cursor.getString(_cursorIndexOfId);
            final String _tmpName;
            _tmpName = _cursor.getString(_cursorIndexOfName);
            final String _tmpEmail;
            _tmpEmail = _cursor.getString(_cursorIndexOfEmail);
            final String _tmpPassword;
            _tmpPassword = _cursor.getString(_cursorIndexOfPassword);
            final String _tmpRole;
            _tmpRole = _cursor.getString(_cursorIndexOfRole);
            final long _tmpDob;
            _tmpDob = _cursor.getLong(_cursorIndexOfDob);
            final int _tmpAge;
            _tmpAge = _cursor.getInt(_cursorIndexOfAge);
            final String _tmpAgeRange;
            _tmpAgeRange = _cursor.getString(_cursorIndexOfAgeRange);
            final String _tmpWakeUpTime;
            _tmpWakeUpTime = _cursor.getString(_cursorIndexOfWakeUpTime);
            final String _tmpBedTime;
            _tmpBedTime = _cursor.getString(_cursorIndexOfBedTime);
            final String _tmpBreakfastTime;
            _tmpBreakfastTime = _cursor.getString(_cursorIndexOfBreakfastTime);
            final String _tmpLunchTime;
            _tmpLunchTime = _cursor.getString(_cursorIndexOfLunchTime);
            final String _tmpDinnerTime;
            _tmpDinnerTime = _cursor.getString(_cursorIndexOfDinnerTime);
            final String _tmpProfileCode;
            _tmpProfileCode = _cursor.getString(_cursorIndexOfProfileCode);
            final String _tmpLinkedUserId;
            _tmpLinkedUserId = _cursor.getString(_cursorIndexOfLinkedUserId);
            final String _tmpAvatarPath;
            if (_cursor.isNull(_cursorIndexOfAvatarPath)) {
              _tmpAvatarPath = null;
            } else {
              _tmpAvatarPath = _cursor.getString(_cursorIndexOfAvatarPath);
            }
            final long _tmpCreatedAt;
            _tmpCreatedAt = _cursor.getLong(_cursorIndexOfCreatedAt);
            _result = new User(_tmpId,_tmpName,_tmpEmail,_tmpPassword,_tmpRole,_tmpDob,_tmpAge,_tmpAgeRange,_tmpWakeUpTime,_tmpBedTime,_tmpBreakfastTime,_tmpLunchTime,_tmpDinnerTime,_tmpProfileCode,_tmpLinkedUserId,_tmpAvatarPath,_tmpCreatedAt);
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
  public Object getUserByEmailAndPassword(final String email, final String password,
      final Continuation<? super User> $completion) {
    final String _sql = "SELECT * FROM users WHERE email = ? AND password = ? LIMIT 1";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 2);
    int _argIndex = 1;
    _statement.bindString(_argIndex, email);
    _argIndex = 2;
    _statement.bindString(_argIndex, password);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<User>() {
      @Override
      @Nullable
      public User call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfName = CursorUtil.getColumnIndexOrThrow(_cursor, "name");
          final int _cursorIndexOfEmail = CursorUtil.getColumnIndexOrThrow(_cursor, "email");
          final int _cursorIndexOfPassword = CursorUtil.getColumnIndexOrThrow(_cursor, "password");
          final int _cursorIndexOfRole = CursorUtil.getColumnIndexOrThrow(_cursor, "role");
          final int _cursorIndexOfDob = CursorUtil.getColumnIndexOrThrow(_cursor, "dob");
          final int _cursorIndexOfAge = CursorUtil.getColumnIndexOrThrow(_cursor, "age");
          final int _cursorIndexOfAgeRange = CursorUtil.getColumnIndexOrThrow(_cursor, "ageRange");
          final int _cursorIndexOfWakeUpTime = CursorUtil.getColumnIndexOrThrow(_cursor, "wakeUpTime");
          final int _cursorIndexOfBedTime = CursorUtil.getColumnIndexOrThrow(_cursor, "bedTime");
          final int _cursorIndexOfBreakfastTime = CursorUtil.getColumnIndexOrThrow(_cursor, "breakfastTime");
          final int _cursorIndexOfLunchTime = CursorUtil.getColumnIndexOrThrow(_cursor, "lunchTime");
          final int _cursorIndexOfDinnerTime = CursorUtil.getColumnIndexOrThrow(_cursor, "dinnerTime");
          final int _cursorIndexOfProfileCode = CursorUtil.getColumnIndexOrThrow(_cursor, "profileCode");
          final int _cursorIndexOfLinkedUserId = CursorUtil.getColumnIndexOrThrow(_cursor, "linkedUserId");
          final int _cursorIndexOfAvatarPath = CursorUtil.getColumnIndexOrThrow(_cursor, "avatarPath");
          final int _cursorIndexOfCreatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "createdAt");
          final User _result;
          if (_cursor.moveToFirst()) {
            final String _tmpId;
            _tmpId = _cursor.getString(_cursorIndexOfId);
            final String _tmpName;
            _tmpName = _cursor.getString(_cursorIndexOfName);
            final String _tmpEmail;
            _tmpEmail = _cursor.getString(_cursorIndexOfEmail);
            final String _tmpPassword;
            _tmpPassword = _cursor.getString(_cursorIndexOfPassword);
            final String _tmpRole;
            _tmpRole = _cursor.getString(_cursorIndexOfRole);
            final long _tmpDob;
            _tmpDob = _cursor.getLong(_cursorIndexOfDob);
            final int _tmpAge;
            _tmpAge = _cursor.getInt(_cursorIndexOfAge);
            final String _tmpAgeRange;
            _tmpAgeRange = _cursor.getString(_cursorIndexOfAgeRange);
            final String _tmpWakeUpTime;
            _tmpWakeUpTime = _cursor.getString(_cursorIndexOfWakeUpTime);
            final String _tmpBedTime;
            _tmpBedTime = _cursor.getString(_cursorIndexOfBedTime);
            final String _tmpBreakfastTime;
            _tmpBreakfastTime = _cursor.getString(_cursorIndexOfBreakfastTime);
            final String _tmpLunchTime;
            _tmpLunchTime = _cursor.getString(_cursorIndexOfLunchTime);
            final String _tmpDinnerTime;
            _tmpDinnerTime = _cursor.getString(_cursorIndexOfDinnerTime);
            final String _tmpProfileCode;
            _tmpProfileCode = _cursor.getString(_cursorIndexOfProfileCode);
            final String _tmpLinkedUserId;
            _tmpLinkedUserId = _cursor.getString(_cursorIndexOfLinkedUserId);
            final String _tmpAvatarPath;
            if (_cursor.isNull(_cursorIndexOfAvatarPath)) {
              _tmpAvatarPath = null;
            } else {
              _tmpAvatarPath = _cursor.getString(_cursorIndexOfAvatarPath);
            }
            final long _tmpCreatedAt;
            _tmpCreatedAt = _cursor.getLong(_cursorIndexOfCreatedAt);
            _result = new User(_tmpId,_tmpName,_tmpEmail,_tmpPassword,_tmpRole,_tmpDob,_tmpAge,_tmpAgeRange,_tmpWakeUpTime,_tmpBedTime,_tmpBreakfastTime,_tmpLunchTime,_tmpDinnerTime,_tmpProfileCode,_tmpLinkedUserId,_tmpAvatarPath,_tmpCreatedAt);
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
  public Object getUserByProfileCode(final String code,
      final Continuation<? super User> $completion) {
    final String _sql = "SELECT * FROM users WHERE profileCode = ? LIMIT 1";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindString(_argIndex, code);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<User>() {
      @Override
      @Nullable
      public User call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfName = CursorUtil.getColumnIndexOrThrow(_cursor, "name");
          final int _cursorIndexOfEmail = CursorUtil.getColumnIndexOrThrow(_cursor, "email");
          final int _cursorIndexOfPassword = CursorUtil.getColumnIndexOrThrow(_cursor, "password");
          final int _cursorIndexOfRole = CursorUtil.getColumnIndexOrThrow(_cursor, "role");
          final int _cursorIndexOfDob = CursorUtil.getColumnIndexOrThrow(_cursor, "dob");
          final int _cursorIndexOfAge = CursorUtil.getColumnIndexOrThrow(_cursor, "age");
          final int _cursorIndexOfAgeRange = CursorUtil.getColumnIndexOrThrow(_cursor, "ageRange");
          final int _cursorIndexOfWakeUpTime = CursorUtil.getColumnIndexOrThrow(_cursor, "wakeUpTime");
          final int _cursorIndexOfBedTime = CursorUtil.getColumnIndexOrThrow(_cursor, "bedTime");
          final int _cursorIndexOfBreakfastTime = CursorUtil.getColumnIndexOrThrow(_cursor, "breakfastTime");
          final int _cursorIndexOfLunchTime = CursorUtil.getColumnIndexOrThrow(_cursor, "lunchTime");
          final int _cursorIndexOfDinnerTime = CursorUtil.getColumnIndexOrThrow(_cursor, "dinnerTime");
          final int _cursorIndexOfProfileCode = CursorUtil.getColumnIndexOrThrow(_cursor, "profileCode");
          final int _cursorIndexOfLinkedUserId = CursorUtil.getColumnIndexOrThrow(_cursor, "linkedUserId");
          final int _cursorIndexOfAvatarPath = CursorUtil.getColumnIndexOrThrow(_cursor, "avatarPath");
          final int _cursorIndexOfCreatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "createdAt");
          final User _result;
          if (_cursor.moveToFirst()) {
            final String _tmpId;
            _tmpId = _cursor.getString(_cursorIndexOfId);
            final String _tmpName;
            _tmpName = _cursor.getString(_cursorIndexOfName);
            final String _tmpEmail;
            _tmpEmail = _cursor.getString(_cursorIndexOfEmail);
            final String _tmpPassword;
            _tmpPassword = _cursor.getString(_cursorIndexOfPassword);
            final String _tmpRole;
            _tmpRole = _cursor.getString(_cursorIndexOfRole);
            final long _tmpDob;
            _tmpDob = _cursor.getLong(_cursorIndexOfDob);
            final int _tmpAge;
            _tmpAge = _cursor.getInt(_cursorIndexOfAge);
            final String _tmpAgeRange;
            _tmpAgeRange = _cursor.getString(_cursorIndexOfAgeRange);
            final String _tmpWakeUpTime;
            _tmpWakeUpTime = _cursor.getString(_cursorIndexOfWakeUpTime);
            final String _tmpBedTime;
            _tmpBedTime = _cursor.getString(_cursorIndexOfBedTime);
            final String _tmpBreakfastTime;
            _tmpBreakfastTime = _cursor.getString(_cursorIndexOfBreakfastTime);
            final String _tmpLunchTime;
            _tmpLunchTime = _cursor.getString(_cursorIndexOfLunchTime);
            final String _tmpDinnerTime;
            _tmpDinnerTime = _cursor.getString(_cursorIndexOfDinnerTime);
            final String _tmpProfileCode;
            _tmpProfileCode = _cursor.getString(_cursorIndexOfProfileCode);
            final String _tmpLinkedUserId;
            _tmpLinkedUserId = _cursor.getString(_cursorIndexOfLinkedUserId);
            final String _tmpAvatarPath;
            if (_cursor.isNull(_cursorIndexOfAvatarPath)) {
              _tmpAvatarPath = null;
            } else {
              _tmpAvatarPath = _cursor.getString(_cursorIndexOfAvatarPath);
            }
            final long _tmpCreatedAt;
            _tmpCreatedAt = _cursor.getLong(_cursorIndexOfCreatedAt);
            _result = new User(_tmpId,_tmpName,_tmpEmail,_tmpPassword,_tmpRole,_tmpDob,_tmpAge,_tmpAgeRange,_tmpWakeUpTime,_tmpBedTime,_tmpBreakfastTime,_tmpLunchTime,_tmpDinnerTime,_tmpProfileCode,_tmpLinkedUserId,_tmpAvatarPath,_tmpCreatedAt);
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
