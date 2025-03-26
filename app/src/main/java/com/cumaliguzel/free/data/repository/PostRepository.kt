package com.cumaliguzel.free.data.repository

import com.cumaliguzel.free.data.model.Post
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import javax.inject.Inject
import java.util.*

class PostRepository @Inject constructor(
    private val db: FirebaseFirestore
) {
    private val postsCollection = db.collection("posts")

    /**
     * Yeni bir ilan (post) Firestore'a ekler.
     */
    fun addPost(
        post: Post,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        val documentRef = postsCollection.document()
        val postToSave = post.copy(
            postId = documentRef.id,
            deleteAt = Timestamp(Date(System.currentTimeMillis() + 30 * 60 * 1000)),
            distanceKm = post.distanceKm,  // ✅ Firebase'e mesafe kaydediyoruz
            estimatedDuration = post.estimatedDuration  // ✅ Firebase'e süre kaydediyoruz
        )

        documentRef.set(postToSave)
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { onFailure(it) }
    }

    /**
     * İlanları gerçek zamanlı olarak dinler.
     */
    fun listenForPosts(
        onPostsUpdated: (List<Post>) -> Unit,
        onFailure: (Exception) -> Unit
    ): ListenerRegistration {
        return postsCollection
            .orderBy("timestamp") // Zaman sırasına göre sıralar
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    onFailure(error)
                    return@addSnapshotListener
                }
                val postList = snapshot?.documents?.mapNotNull { it.toObject(Post::class.java) } ?: emptyList()
                onPostsUpdated(postList)
            }
    }

    /**
     * Belirli bir post'u Firestore'dan siler.
     */
    fun deletePost(postId: String, onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {
        postsCollection.document(postId)
            .delete()
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { onFailure(it) }
    }
}