package empire.digiprem.chirp.api.controller

import empire.digiprem.chirp.api.util.requestUserId
import empire.digiprem.chirp.domain.type.ChatMessageId
import empire.digiprem.chirp.service.ChatMessageService
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/message")
class ChatMessageController(private val chatMessageService: ChatMessageService) {


    @DeleteMapping("/{message}")
    fun deleteMessage(
        @PathVariable messageId: ChatMessageId
    ){
        chatMessageService.deleteMessage(
            requestUserId=requestUserId,
            messageId=messageId
            )
    }


}