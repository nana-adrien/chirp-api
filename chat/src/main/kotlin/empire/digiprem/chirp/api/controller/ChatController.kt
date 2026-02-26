package empire.digiprem.chirp.api.controller

import empire.digiprem.chirp.api.dto.AddParticipantToChatDto
import empire.digiprem.chirp.api.dto.ChatDto
import empire.digiprem.chirp.api.dto.CreateChatRequest
import empire.digiprem.chirp.api.mappers.toChatDto
import empire.digiprem.chirp.api.util.requestUserId
import empire.digiprem.chirp.domain.type.ChatId
import empire.digiprem.chirp.service.ChatService
import jakarta.validation.Valid
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/chat")
class ChatController(
    private val chatService: ChatService

) {

    @PostMapping()
    fun createChat(
        @Valid @RequestBody body: CreateChatRequest
    ): ChatDto {
        return chatService.createChat(
            creatorId = requestUserId,
            otherUserIds = body.otherUserIds.toSet()
        ).toChatDto()
    }

    @PostMapping("/{chatId}/add")
    fun addChatParticipant(
        @PathVariable chatId: ChatId,
        @Valid @RequestBody body: AddParticipantToChatDto

    ):ChatDto {
        return chatService.addParticipantToChat(
            requestUserId=requestUserId,
            chatId=chatId,
            userIds=body.userIds.toSet()
        ).toChatDto()
    }
    @DeleteMapping("/{chatId}/leave")
    fun removeParticipantFromChat(
        @PathVariable chatId: ChatId,
    ) {
         chatService.removeParticipantFromChat(
            userId = requestUserId,
            chatId=chatId,
        )
    }
}

