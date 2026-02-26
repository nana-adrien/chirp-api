package empire.digiprem.chirp.api.controller

import empire.digiprem.chirp.api.dto.AddParticipantToChatDto
import empire.digiprem.chirp.api.dto.ChatDto
import empire.digiprem.chirp.api.dto.ChatMessageDto
import empire.digiprem.chirp.api.dto.CreateChatRequest
import empire.digiprem.chirp.api.mappers.toChatDto
import empire.digiprem.chirp.api.util.requestUserId
import empire.digiprem.chirp.domain.type.ChatId
import empire.digiprem.chirp.service.ChatMessageService
import empire.digiprem.chirp.service.ChatService
import jakarta.validation.Valid
import org.springframework.beans.support.PagedListHolder.DEFAULT_PAGE_SIZE
import org.springframework.web.bind.annotation.*
import java.time.Instant

@RestController
@RequestMapping("/api/chat")
class ChatController(
    private val chatService: ChatService,
    private val chatMessageService: ChatMessageService

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


    @GetMapping("/{chatId}/messages")
    fun getMessagesForChat(
        @PathVariable chatId: ChatId,
        @RequestParam ("before",required = false) before: Instant?=null,
        @RequestParam ("pageSize") pageSize: Int=DEFAULT_PAGE_SIZE,
    ):List<ChatMessageDto> {
        return chatService.getChatMessages(
            chatId=chatId,
            before=before,
            pageSize=pageSize
        )
    }
}

