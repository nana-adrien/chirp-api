package empire.digiprem.chirp.api.websocket

import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.databind.JsonMappingException
import empire.digiprem.chirp.api.dto.ws.*
import empire.digiprem.chirp.api.mappers.toChatMessageDto
import empire.digiprem.chirp.domain.type.ChatId
import empire.digiprem.chirp.domain.type.UserId
import empire.digiprem.chirp.service.ChatMessageService
import empire.digiprem.chirp.service.ChatService
import empire.digiprem.chirp.service.JWTService
import org.slf4j.LoggerFactory
import org.springframework.http.HttpHeaders
import org.springframework.stereotype.Component
import org.springframework.web.socket.CloseStatus
import org.springframework.web.socket.TextMessage
import org.springframework.web.socket.WebSocketSession
import org.springframework.web.socket.handler.TextWebSocketHandler
import tools.jackson.databind.ObjectMapper
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.locks.ReentrantReadWriteLock
import kotlin.concurrent.read
import kotlin.concurrent.write

@Component
class ChatWebSocketHandler(
    private val chatMessageService: ChatMessageService,
    private val objectMapper: ObjectMapper,
    private val chatService: ChatService,
    private val jWTService: JWTService,
) : TextWebSocketHandler() {
    private val logger = LoggerFactory.getLogger(javaClass)

    private val connectionLock = ReentrantReadWriteLock()


    private val sessions = ConcurrentHashMap<String, UserSession>()
    private val userToSessions = ConcurrentHashMap<UserId, MutableSet<String>>()
    private val userChatIds = ConcurrentHashMap<UserId, MutableSet<ChatId>>()
    private val chatToSessions = ConcurrentHashMap<ChatId, MutableSet<String>>()

    private val connectionLockRead = ReentrantReadWriteLock()

    override fun afterConnectionClosed(session: WebSocketSession?, status: CloseStatus?) {
        super.afterConnectionClosed(session, status)
    }


    override fun afterConnectionEstablished(session: WebSocketSession) {
        val authHeader = session
            .handshakeHeaders
            .getFirst(HttpHeaders.AUTHORIZATION)
            ?: run {
                logger.warn("Session ${session.id} was closed due to missing Authorization hearder")
                session.close(CloseStatus.SERVER_ERROR.withReason("Authentication faild"))
                return
            }

        val userId = jWTService.getUserIdFromToken(authHeader)
        val userSession = UserSession(
            userId = userId,
            session = session
        )

        connectionLockRead.write {
            sessions[session.id] = userSession
            userToSessions.compute(userId) { _, existingSession ->
                (existingSession ?: mutableSetOf()).apply {
                    add(session.id)
                }
            }
            val chatIds = userChatIds.computeIfAbsent(userId) {
                val chatIds = chatService.findChatsByUser(userId).map { it.id }
                ConcurrentHashMap.newKeySet<ChatId>().apply {
                    addAll(chatIds)
                }
            }
            chatIds.forEach { chatId ->
                chatToSessions.compute(chatId) { _, sessions ->
                    (sessions ?: mutableSetOf()).apply {
                        add(session.id)
                    }
                }
            }

        }
        logger.info("Websocket connection establish for user $userId")
    }

    override fun handleTextMessage(session: WebSocketSession, message: TextMessage) {
        logger.debug("Received message for session ${message.payload}")
        val userSession = connectionLockRead.read {
            sessions[session.id] ?: return
        }
        try {
            val webSocketMessage = objectMapper.readValue(
                message.payload,
                IncomingWebSocketMessage::class.java
            )

            when (webSocketMessage.type) {
                IncomingWebSocketMessageType.NEW_MESSAGE -> {
                    val dto = objectMapper.readValue(
                        webSocketMessage.payload,
                        SendMessageDto::class.java
                    )
                    handlerSendMessage(
                        dto = dto,
                        senderId = userSession.userId,
                    )
                }
            }
        } catch (e: JsonMappingException) {
            sendError(
                session = userSession.session,
                error=ErrorDto(
                    code = "INVALID_JSON",
                    message="Incoming JSON or UUID is invalid"
                )
            )
        }
    }

    private fun sendError(
        session: WebSocketSession,
        error: ErrorDto
    ) {
        val webSocketMessage = objectMapper.writeValueAsString(
            OutgoingWebSocketMessage(
                type = OutgoingWebSocketMessageType.ERROR,
                payload = objectMapper.writeValueAsString(error),
            )
        )
        try {
            session.sendMessage(TextMessage(webSocketMessage))
        }catch (e: JsonProcessingException) {
            logger.warn("Couldn't send ", e)
        }
    }

    private fun broadcastToChat(
        chatId: ChatId,
        message: OutgoingWebSocketMessage,
    ) {
        val chatSessions = connectionLockRead.read {
            chatToSessions[chatId]?.toList() ?: emptyList()
        }

        chatSessions.forEach { sessionId ->
            val userSession = connectionLockRead.read {
                sessions[sessionId]
            }

            sendToUser(
                userId = userSession?.userId!!,
                message = message,
            )
        }

    }

    private fun handlerSendMessage(
        dto: SendMessageDto,
        senderId: UserId,
    ) {
        val userChatIds = connectionLockRead.read { this@ChatWebSocketHandler.userChatIds[senderId] } ?: return
        if (dto.chatId !in userChatIds) {
            return

        }

        val savedMessage = chatMessageService.sendMessage(
            dto.chatId,
            senderId = senderId,
            content = dto.content,
            messageId = dto.messageId
        )

        broadcastToChat(
            chatId = dto.chatId,
            message = OutgoingWebSocketMessage(
                type = OutgoingWebSocketMessageType.NEW_MESSAGE,
                payload = objectMapper.writeValueAsString(savedMessage.toChatMessageDto()),
            )
        )
    }

    private fun sendToUser(userId: UserId, message: OutgoingWebSocketMessage) {
        val userSessions = connectionLockRead.read {
            userToSessions[userId] ?: emptySet()
        }
        userSessions.forEach { sessionId ->
            val userSession = connectionLockRead.read {
                sessions[sessionId] ?: return@forEach
            }
            if (userSession.session.isOpen) {
                try {
                    val messageJson = objectMapper.writeValueAsString(message)
                    userSession.session.sendMessage(TextMessage(messageJson))
                    logger.info("Sent message to user {}:{}", userId, messageJson)

                } catch (e: Exception) {
                    logger.warn("Error while sending message to $userId", e)
                }
            }
        }

    }

    private data class UserSession(
        val userId: UserId,
        val session: WebSocketSession,
    )

}