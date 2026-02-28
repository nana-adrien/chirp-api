package empire.digiprem.chirp.infra.storage

import empire.digiprem.chirp.domain.exception.InvalidProfilePictureException
import empire.digiprem.chirp.domain.exception.StorageException
import empire.digiprem.chirp.domain.models.ProfilePictureUploadCredentials
import empire.digiprem.chirp.domain.type.UserId
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.web.client.RestClient
import java.time.Instant
import java.util.*

@Service
class SupabaseStorageService (
    @param:Value("\${supabase.url}")
    private val supabaseUrl: String,
    private val supabaseRestClient: RestClient
){

    companion object {
        private val allowedMimeTypes=mapOf(
            "image/jpeg" to "jpg",
            "image/jpg" to "jpg",
            "image/png" to "png",
            "image/webp" to "webp",
        )




    }



    fun generateSignedUrl(userId: UserId, mimeType:String): ProfilePictureUploadCredentials{
        val extension= allowedMimeTypes[mimeType]
            ?:throw InvalidProfilePictureException("Invalid mime type $mimeType")

        val filename ="user_${userId}_${UUID.randomUUID()}.$extension"
        val path="profile-pictures/$filename"

        val publishUrl="$supabaseUrl/storage/v1/object/public/$path"
        return ProfilePictureUploadCredentials(
            uploadUrl = createSigneUrl(
               path= path,
                expiresInSeconds = 300),
            publishUrl = publishUrl,
            headers = mapOf(
                "Content-Type" to mimeType
            ),
            expiresAt = Instant.now().plusSeconds(300)
        )


    }

    private fun createSigneUrl(
        path:String,
        expiresInSeconds:Int
    ): String{
        val json="""
            {"expiresIn":$expiresInSeconds}
        """.trimIndent()

        val response=supabaseRestClient
            .post()
            .uri("/storage/v1/object/upload/sign/$path")
            .header("Content-Type","Application/json")
            .body (json)
            .retrieve()
            .body(SingedUploadResponse::class.java)
            ?:throw StorageException("Failed to create signed URL")

        return "$supabaseUrl/storage/v1${response.url}"
    }

    private data class SingedUploadResponse (
        val url: String,
    )


    fun deleteFile(url:String){
        val path=if (url.contains("/object/public/")){
            url.substringAfter("/object/public")
        } else throw StorageException("Invalid file URL format")

        val deleteUrl="/storage/v1/object/$path"

        val response=supabaseRestClient
            .delete()
            .uri(deleteUrl)
            .retrieve()
            .toBodilessEntity()

        if(response.statusCode.isError) {
            throw StorageException("Unable to delete file:${response.statusCode}")
        }
    }

}





