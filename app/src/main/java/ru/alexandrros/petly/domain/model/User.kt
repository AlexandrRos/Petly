package ru.alexandrros.petly.domain.model

data class User(
    val uid: String,
    val email: String,
    val name: String,
    val specialist: String? = null,
    val photoBytes: ByteArray? = null
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is User) return false
        if (uid != other.uid) return false
        if (email != other.email) return false
        if (name != other.name) return false
        if (specialist != other.specialist) return false
        if (photoBytes != null) {
            if (other.photoBytes == null || !photoBytes.contentEquals(other.photoBytes)) return false
        } else if (other.photoBytes != null) return false
        return true
    }

    override fun hashCode(): Int {
        var result = uid.hashCode()
        result = 31 * result + email.hashCode()
        result = 31 * result + name.hashCode()
        result = 31 * result + (specialist?.hashCode() ?: 0)
        result = 31 * result + (photoBytes?.contentHashCode() ?: 0)
        return result
    }
}