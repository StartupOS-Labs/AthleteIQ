// In FitnessViewModel.kt

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class `FitnessViewModel` : ViewModel() {
    private val _totalReps = MutableLiveData(0)
    val totalReps: LiveData<Int> = _totalReps

    private val _goodReps = MutableLiveData(0)
    val goodReps: LiveData<Int> = _goodReps

    // ⭐ New: LiveData for user profile data
    private val _userName = MutableLiveData<String>()
    val userName: LiveData<String> = _userName

    private val _userAge = MutableLiveData<Int>()
    val userAge: LiveData<Int> = _userAge

    fun setReps(total: Int, good: Int) {
        _totalReps.postValue(total)
        _goodReps.postValue(good)
    }

    // ⭐ New: Function to set user profile data
    fun setUserData(name: String, age: Int) {
        _userName.postValue(name)
        _userAge.postValue(age)
    }
}