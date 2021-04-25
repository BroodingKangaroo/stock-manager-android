package com.android.stockmanager.firebase

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.map
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import timber.log.Timber

class FirebaseUserLiveData : LiveData<FirebaseUser?>() {
    val firebaseAuth = FirebaseAuth.getInstance()

    private val authStateListener = FirebaseAuth.AuthStateListener { firebaseAuth ->
        value = firebaseAuth.currentUser
    }

    // When this object has an active observer, start observing the FirebaseAuth state to see if
    // there is currently a logged in user.
    override fun onActive() {
        firebaseAuth.addAuthStateListener(authStateListener)
    }

    // When this object no longer has an active observer, stop observing the FirebaseAuth state to
    // prevent memory leaks.
    override fun onInactive() {
        firebaseAuth.removeAuthStateListener(authStateListener)
    }

    fun getUserId(): String {
        return firebaseAuth.uid!!
    }
}

//TODO("make `userAuthStateLiveData` an object")
enum class AuthenticationState {
    AUTHENTICATED, UNAUTHENTICATED, INVALID_AUTHENTICATION
}

val userAuthStateLiveData = FirebaseUserLiveData()

var authenticationState: LiveData<AuthenticationState> = userAuthStateLiveData.map { user ->
    if (user != null) {
        AuthenticationState.AUTHENTICATED
    } else {
        AuthenticationState.UNAUTHENTICATED
    }
}


object UserData {

    val favoriteTickers: MutableLiveData<MutableList<String>> = MutableLiveData(mutableListOf())
    private val userId: MutableLiveData<String> = MutableLiveData("")

    fun init(_userId: String, _favoriteTickers: MutableList<String>) {
        favoriteTickers.value = _favoriteTickers
        userId.value = _userId
    }

    suspend fun addTicker(symbol: String) {
        favoriteTickers.value!!.add(symbol)

        writeTickersToFirebase()
    }

    suspend fun removeTicker(symbol: String) {
        favoriteTickers.value!!.remove(symbol)

        writeTickersToFirebase()
    }

    private suspend fun writeTickersToFirebase() {
        withContext(Dispatchers.IO) {
            val db = Firebase.firestore
            db
                .collection("users")
                .document(userId.value!!)
                .set(hashMapOf("tickers" to favoriteTickers.value))
                .addOnSuccessListener {
                    Timber.i("Document of $userId was successfully written")
                }
                .addOnFailureListener { e ->
                    Timber.e("Error writing document of $userId: $e")
                }
        }
    }

    fun tickersToString(): String {
        return favoriteTickers.value!!.joinToString(",")
    }

    suspend fun fetchUser() {
        val db = Firebase.firestore
        val docRef = db.collection("users").document(userId.value!!)

        docRef.get()
            .addOnSuccessListener { document ->
                try {
                    if (document != null && document["tickers"] != null) {
                        favoriteTickers.value = document["tickers"] as MutableList<String>
                        Timber.i("Document of ${userId.value} was successfully read")
                    }
                } catch (e: Exception) {
                    Timber.e("Error reading document of ${userId.value}: $e")
                }
            }
            .addOnFailureListener { e ->
                Timber.e("Error reading document of ${userId.value}: $e")
            }.await()
    }
}