package empire.digiprem.chirp.api.controller

import empire.digiprem.chirp.api.dto.ChatParticipantDto
import empire.digiprem.chirp.api.dto.ConfirmProfilePictureRequest
import empire.digiprem.chirp.api.dto.PictureUploadResponse
import empire.digiprem.chirp.api.dto.toResponse
import empire.digiprem.chirp.api.mappers.toChatParticipantDto
import empire.digiprem.chirp.api.util.requestUserId
import empire.digiprem.chirp.service.ChatParticipantService
import empire.digiprem.chirp.service.ProfilePictureService
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*
import org.springframework.web.server.ResponseStatusException

@RestController
@RequestMapping("/api/participants")
class ChatParticipantController(
    private val chatParticipantService: ChatParticipantService,
    private val profilePictureService: ProfilePictureService
) {
    @GetMapping()
    fun getChatParticipantByUsernameOrEmail(
        @RequestParam (required = false) query: String?,
    ): ChatParticipantDto {
        val participant=if (query==null){
            chatParticipantService.findChatParticipantById(requestUserId)
        } else  {
            chatParticipantService.findChatParticipantsByEmailOrUsername(query)
        }
        return  participant?.toChatParticipantDto()
            ?:throw ResponseStatusException(HttpStatus.NOT_FOUND)
    }

    @PostMapping("/profile-picture-upload")
    fun getProfilePictureUploadUrl(
        @RequestParam mimeType: String,
    ):PictureUploadResponse{
        return profilePictureService.generateUpdatedCredentials(
            userId = requestUserId,
            mimeType=mimeType
        ).toResponse()
    }
    @PostMapping("/confirm-profile-picture")
    fun confirmProfilePictureUpload(
        @Valid @RequestBody body: ConfirmProfilePictureRequest,
    ){
         profilePictureService.confirmProfilePictureUpload(
            userId = requestUserId,
             publicUrl = body.publicUrl,
        )
    }

    @DeleteMapping("profile-picture")
    fun deleteProfilePicture(){
        profilePictureService.deleteProfilePicture(
            userId = requestUserId,
        )

    }


}