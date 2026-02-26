package empire.digiprem.chirp.api.controller

import empire.digiprem.chirp.api.dto.ChatParticipantDto
import empire.digiprem.chirp.api.mappers.toChatParticipantDto
import empire.digiprem.chirp.api.util.requestUserId
import empire.digiprem.chirp.service.ChatParticipantService
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.server.ResponseStatusException

@RestController
@RequestMapping("/api/chat/participants")
class ChatParticipantController(
    private val chatParticipantService: ChatParticipantService
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
}