package empire.digiprem.chirp.service

import empire.digiprem.chirp.domain.event.ProfilePictureUploadEvent
import empire.digiprem.chirp.domain.exception.ChatParticipantNotFoundException
import empire.digiprem.chirp.domain.exception.InvalidProfilePictureException
import empire.digiprem.chirp.domain.models.ProfilePictureUploadCredentials
import empire.digiprem.chirp.domain.type.UserId
import empire.digiprem.chirp.infra.database.repositories.ChatParticipantRepository
import empire.digiprem.chirp.infra.storage.SupabaseStorageService
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.ApplicationEventPublisher
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class ProfilePictureService(
    private val supabaseStorageService: SupabaseStorageService,
    private val chatParticipantRepository: ChatParticipantRepository,
    private val applicationEventPublisher: ApplicationEventPublisher,
    @param:Value("\${supabase.url}")
    private val supabaseUrl: String
) {

    private val logger = LoggerFactory.getLogger(ProfilePictureService::class.java)
    fun generateUpdatedCredentials(
        userId: UserId,
        mimeType: String,
    ): ProfilePictureUploadCredentials {
        return supabaseStorageService.generateSignedUrl(
            userId = userId,
            mimeType = mimeType,
        )
    }

    @Transactional
    fun deleteProfilePicture(
        userId: UserId,
    ) {
        val participant = chatParticipantRepository.findByIdOrNull(userId)
            ?: throw ChatParticipantNotFoundException(userId)

        participant.profilePictureUrl?.let { url ->
            chatParticipantRepository.save(
                participant.apply {
                    profilePictureUrl = null
                }
            )
            supabaseStorageService.deleteFile(url)

        }
        applicationEventPublisher.publishEvent(
            ProfilePictureUploadEvent(
                userId = userId,
                newUrl = null
            )
        )
    }

    @Transactional
    fun confirmProfilePictureUpload(userId: UserId,publicUrl: String) {
        if (!publicUrl.startsWith(supabaseUrl)) {
            throw InvalidProfilePictureException("Invalid profile picture URl")
        }
        val participant = chatParticipantRepository.findByIdOrNull(userId)
            ?: throw ChatParticipantNotFoundException(userId)

        val oldUrl=participant.profilePictureUrl
        chatParticipantRepository.save(
            participant.apply {
                profilePictureUrl = publicUrl
            }
        )
        try {
            oldUrl?.let {
                supabaseStorageService.deleteFile(it)
            }

        }catch (e:Exception){
            logger.warn("Delete old profile picture for $userId failed",e)
        }

        applicationEventPublisher.publishEvent(
            ProfilePictureUploadEvent(
                userId = userId,
                newUrl = publicUrl
            )
        )
    }
}

