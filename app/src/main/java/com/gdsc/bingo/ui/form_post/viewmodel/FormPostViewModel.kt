package com.gdsc.bingo.ui.form_post.viewmodel

import android.net.Uri
import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.firebase.geofire.GeoFireUtils
import com.firebase.geofire.GeoLocation
import com.gdsc.bingo.model.BinLocation
import com.gdsc.bingo.model.FireModel
import com.gdsc.bingo.model.Forums
import com.gdsc.bingo.model.KomentarHub
import com.gdsc.bingo.model.Likes
import com.gdsc.bingo.model.PostImage
import com.gdsc.bingo.services.api.firebase.MapsDataSyncFirebase
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.GeoPoint
import com.google.firebase.firestore.Source
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

class FormPostViewModel : ViewModel() {
    val vicinity : MutableLiveData<GeoPoint?> = MutableLiveData()
    var address : String? = null
    val description : MutableLiveData<String?> = MutableLiveData()

    fun setVicinity(vicinity: GeoPoint?) {
        viewModelScope.launch {
            this@FormPostViewModel.vicinity.value = vicinity
        }
    }

    fun setDescription(description: String?) {
        viewModelScope.launch {
            this@FormPostViewModel.description.value = description
        }
    }

    fun clear() {
        viewModelScope.launch {
            vicinity.value = null
            address = null
            description.value = null
        }
    }

    private suspend fun uploadForumsReferences(
        firestore: FirebaseFirestore,
        forums : Forums,
        firebaseStorage: FirebaseStorage,
        auth: FirebaseAuth,
        caption: String
    ) : DocumentReference? = suspendCoroutine { continuation ->
        firestore.collection(forums.table).add(forums.toFirebaseModel())
            .addOnSuccessListener { forumPostRef ->
                val documentId = forumPostRef.id

                viewModelScope.launch(Dispatchers.IO) successLaunch@{
                    // Upload text file
                    if (caption.length > forums.textLimit) {

                        try {
                            forums.isUsingTextFile = true
                            forums.textFilePath =
                                uploadFile(firebaseStorage, auth, caption, documentId)
                        } catch (e: Exception) {
                            Log.e("FormPostViewModel", "submitForm: ${e.message}")

                            Log.w("FormPostViewModel", "submitForm: Deleting forums")
                            forumPostRef.delete()

                            continuation.resume(null)
                        }

                    } else {
                        forums.text = caption
                    }

                    forums.apply {
                        referencePath = forumPostRef
                        likesReference = firestore.collection(Likes().table).document(documentId)
                        komentarHub = firestore.collection(KomentarHub().table).document(documentId)
                    }
                    forumPostRef.update(forums.toFirebaseModel())

                    continuation.resume(forumPostRef)
                }
            }
            .addOnFailureListener {
                Log.e("FormPostViewModel", "uploadForums: ${it.message}")
                continuation.resume(null)
            }
    }

    private suspend fun uploadImages(
        forumPostRef: DocumentReference,
        forums : Forums,
        firebaseStorage: FirebaseStorage,
        imageList : MutableList<PostImage>
    ) : Boolean = suspendCoroutine { continuation ->

        val imageRef = forumPostRef.collection(PostImage().table)
        imageList.forEachIndexed { index, image ->
            val photosRef = firebaseStorage.reference
                .child("public/${forums.table}/${forumPostRef.id}/${image.table}/${image.filename}_${image.createAt?.seconds}")

            val file = Uri.parse(image.path)

            val uploadTask = photosRef.putFile(file)


            uploadTask.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    image.path = photosRef.path

                    imageRef.add(image.toFirebaseModel())
                        .addOnSuccessListener { onSuccessReference ->
                            image.referencePath = onSuccessReference

                            onSuccessReference.update(FireModel.Keys.referencePath, onSuccessReference)
                                .addOnSuccessListener {
                                    Log.i("FormPostViewModel", "uploadForums: Image uploaded")

                                    if (index == 0) {
                                        forumPostRef.update(Forums.Keys.thumbnailPhotosUrl, image.path)
                                            .addOnSuccessListener {
                                                Log.i("FormPostViewModel", "uploadForums: Thumbnail updated")
                                            }
                                            .addOnFailureListener { e ->
                                                Log.e("FormPostViewModel", "uploadForums: failed when update thumbnail \n ${e.message}")
                                                continuation.resume(false)
                                                executeFailure(e)
                                            }
                                    }


                                }
                                .addOnFailureListener { e ->
                                    Log.e("FormPostViewModel", "uploadForums: failed when update image reference path \n ${e.message}")
                                    executeFailure(e)
                                    continuation.resume(false)
                                }

                        }
                        .addOnFailureListener { e ->
                            Log.e("FormPostViewModel", "uploadForums: ${e.message}")
                            executeFailure(e)
                        }
                } else {
                    Log.e("FormPostViewModel", "uploadForums: ${task.exception}")
                    executeFailure(Exception("upload image failed"))
                    continuation.resume(false)
                }

            }
        }
        continuation.resume(true)
    }

    private suspend fun createKomentarHub(
        forums : Forums
    ) : Boolean = suspendCoroutine { continuation ->
        val komentarHub = KomentarHub(
            referencePath = forums.komentarHub,
            createdAt = Timestamp.now()
        )
        forums.komentarHub?.set(komentarHub.toFirebaseModel())
            ?.addOnFailureListener {
                Log.e("FormPostViewModel", "uploadForums: ${it.message}")
                continuation.resume(false)
            }
            ?.addOnSuccessListener {
                Log.i("FormPostViewModel", "uploadForums: KomentarHub created")
                continuation.resume(true)
            }
    }

    private suspend fun createBinLocation(
        forumPostRef: DocumentReference,
        caption: String,
        firestore: FirebaseFirestore,
        forums : Forums,
        type : String,
        vicinity: GeoPoint?,
        addressReport: String
    ) : Boolean = suspendCoroutine { continuation ->
        if (type == Forums.ForumType.REPORT.fieldName) {
            val placesId = MapsDataSyncFirebase.generateIDforPlaces(
                vicinity!!.latitude,
                vicinity.longitude
            )
            firestore.collection(BinLocation().table).document(placesId)
                .get(Source.SERVER)
                .addOnSuccessListener { documentSnapshot ->
                    if (documentSnapshot.exists().not()) {
                        val binLocation = BinLocation(
                            referencePath = documentSnapshot.reference,
                            name = forums.title,
                            address = addressReport,
                            location = vicinity,
                            geohash = GeoFireUtils.getGeoHashForLocation(GeoLocation(vicinity.latitude, vicinity.longitude)),
                            type = BinLocation.BinTypeCategory.REPORT.fieldName,
                            additionalInfo = mapOf(
                                BinLocation.BinAdditionalInfo.FORUM_ID.fieldName to forumPostRef,
                                BinLocation.BinAdditionalInfo.REPORT_DESCRIPTION.fieldName to caption,
                                BinLocation.BinAdditionalInfo.REPORT_DATE.fieldName to Timestamp.now()
                            )
                        )

                        documentSnapshot.reference.set(binLocation.toFirebaseModel())
                            .addOnSuccessListener {
                                Log.i("FormPostViewModel", "uploadForums: BinLocation created")
                                continuation.resume(true)
                            }
                            .addOnFailureListener {
                                Log.e("FormPostViewModel", "uploadForums: ${it.message}")
                                continuation.resume(false)
                            }
                    }
                }
                .addOnFailureListener {
                    Log.e("FormPostViewModel", "uploadForums: ${it.message}")
                    continuation.resume(false)
                }
        } else {
            continuation.resume(true)
        }
    }

    suspend fun uploadForums(
        firestore: FirebaseFirestore,
        firebaseStorage: FirebaseStorage,
        auth : FirebaseAuth,
        type : String,
        forums: Forums,
        caption: String,
        vicinity: GeoPoint?,
        addressReport: String,
        imageList : MutableList<PostImage>
    ) : Boolean {
        return suspendCoroutine<Boolean> { continuation ->
            viewModelScope.launch(Dispatchers.IO) {

                val documentReference = uploadForumsReferences(
                    firestore,
                    forums,
                    firebaseStorage,
                    auth,
                    caption
                ) ?: return@launch continuation.resume(false)

                val isImageUploaded = if (imageList.isEmpty()) true else uploadImages(
                    documentReference,
                    forums,
                    firebaseStorage,
                    imageList
                )

                val isKomentarHubCreated = createKomentarHub(forums)

                val isBinLocationCreated = if (type == Forums.ForumType.REPORT.fieldName) {
                    createBinLocation(
                        documentReference,
                        caption,
                        firestore,
                        forums,
                        type,
                        vicinity,
                        addressReport
                    )
                } else true


                if (isImageUploaded && isKomentarHubCreated && isBinLocationCreated) {
                    continuation.resume(true)
                } else {
                    continuation.resume(false)
                }

            }
        }
    }

    private suspend fun uploadFile(
        storage: FirebaseStorage,
        auth: FirebaseAuth,
        caption: String,
        documentId: String
    ): String = suspendCoroutine { continuation ->
        val storageRef = storage.reference
        val fileForumsReference = storageRef.child("public/${Forums().table}/${documentId}/${auth.uid}_${Timestamp.now().nanoseconds}.txt")


        val data = caption.toByteArray(Charsets.UTF_8)

        val uploadTask = fileForumsReference.putBytes(data)
        uploadTask.addOnCompleteListener {
            if (it.isSuccessful) {
                continuation.resume(fileForumsReference.path)
            } else {
                Log.e("FormPostViewModel", "uploadFile: ${it.exception}")
                continuation.resumeWithException(it.exception!!)
            }
        }

    }

    private fun executeFailure(e: Exception) {
        Log.e("FormPostViewModel", "\n\n" +
                "Form Error, Unable To Upload" +
                "uploadForums: ${e.message}" +
                "\n\n")
    }
}