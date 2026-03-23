package com.mediease.app.viewmodels

import android.app.Application
import androidx.lifecycle.*
import com.mediease.app.database.AppDatabase
import com.mediease.app.models.User
import com.mediease.app.repository.UserRepository
import com.mediease.app.utils.PrefsManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class UserViewModel(application: Application) : AndroidViewModel(application) {
    private val repo = UserRepository()
    private val db = AppDatabase.getDatabase(application)
    private val userDao = db.userDao()
    private val prefs = PrefsManager(application)

    val currentUser = MutableLiveData<User?>()
    val isLoading = MutableLiveData<Boolean>()
    val error = MutableLiveData<String?>()
    
    val saveResult = MutableLiveData<Boolean>()
    val loginResult = MutableLiveData<User?>()
    val connectResult = MutableLiveData<String?>()
    val resetPasswordResult = MutableLiveData<Boolean>()

    fun loadCurrentUser() {
        viewModelScope.launch {
            isLoading.value = true
            try {
                val user = repo.getMe()
                currentUser.value = user
                error.value = null
            } catch (e: Exception) {
                error.value = e.message
            } finally {
                isLoading.value = false
            }
        }
    }

    fun loadCurrentUserById(id: String) {
        viewModelScope.launch {
            isLoading.postValue(true)
            try {
                val user = withContext(Dispatchers.IO) {
                    userDao.getUserByIdSync(id)
                }
                currentUser.postValue(user)
            } catch (e: Exception) {
                error.postValue(e.message)
            } finally {
                isLoading.postValue(false)
            }
        }
    }

    fun saveUser(user: User) {
        viewModelScope.launch {
            isLoading.value = true
            try {
                withContext(Dispatchers.IO) {
                    // If we are updating an existing user (during setup), 
                    // we should preserve the password if the passed object has it empty
                    val existing = userDao.getUserByIdSync(user.id)
                    val userToSave = if (existing != null && user.password.isEmpty()) {
                        user.copy(password = existing.password)
                    } else {
                        user
                    }
                    userDao.insertUser(userToSave)
                }
                saveResult.value = true
                error.value = null
            } catch (e: Exception) {
                error.value = e.message
                saveResult.value = false
            } finally {
                isLoading.value = false
            }
        }
    }

    fun loginWithEmailAndPassword(email: String, pass: String) {
        viewModelScope.launch {
            isLoading.value = true
            try {
                val user = withContext(Dispatchers.IO) {
                    userDao.getUserByEmailAndPassword(email, pass)
                }
                loginResult.value = user
                error.value = null
            } catch (e: Exception) {
                error.value = e.message
                loginResult.value = null
            } finally {
                isLoading.value = false
            }
        }
    }

    fun resetPassword(email: String, newPass: String) {
        viewModelScope.launch {
            isLoading.value = true
            try {
                val success = withContext(Dispatchers.IO) {
                    val user = userDao.getUserByEmail(email)
                    if (user != null) {
                        userDao.updateUser(user.copy(password = newPass))
                        true
                    } else {
                        false
                    }
                }
                resetPasswordResult.value = success
                if (!success) error.value = "Email not found"
                else error.value = null
            } catch (e: Exception) {
                error.value = e.message
                resetPasswordResult.value = false
            } finally {
                isLoading.value = false
            }
        }
    }

    fun connectToPatient(code: String) {
        viewModelScope.launch {
            isLoading.value = true
            try {
                // Simulated connection logic
                if (code.startsWith("MED-")) {
                    connectResult.value = "Patient Verified"
                } else {
                    connectResult.value = null
                }
                error.value = null
            } catch (e: Exception) {
                error.value = e.message
                connectResult.value = null
            } finally {
                isLoading.value = false
            }
        }
    }
}
