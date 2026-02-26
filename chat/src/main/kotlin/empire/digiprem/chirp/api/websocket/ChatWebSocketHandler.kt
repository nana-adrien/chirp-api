package empire.digiprem.chirp.api.websocket

import empire.digiprem.chirp.domain.type.ChatId
import empire.digiprem.chirp.domain.type.UserId
import empire.digiprem.chirp.service.ChatMessageService
import empire.digiprem.chirp.service.ChatService
import empire.digiprem.chirp.service.JWTService
import org.slf4j.LoggerFactory
import org.springframework.http.HttpHeaders
import org.springframework.stereotype.Component
import org.springframework.web.socket.CloseStatus
import org.springframework.web.socket.WebSocketSession
import org.springframework.web.socket.handler.TextWebSocketHandler
import tools.jackson.databind.ObjectMapper
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.locks.ReentrantReadWriteLock
import kotlin.concurrent.write

@Component
class ChatWebSocketHandler(
    private val chatMessageService: ChatMessageService,
    private val objectMapper: ObjectMapper,
    private val chatService: ChatService,
    private val jWTService: JWTService,
) : TextWebSocketHandler(){
    private val logger = LoggerFactory.getLogger(javaClass)

    private val connectionLock= ReentrantReadWriteLock()


    private val sessions = ConcurrentHashMap<String, UserSession>()
    private val userToSessions = ConcurrentHashMap<UserId, MutableSet<String>>()
    private val userChatIds = ConcurrentHashMap<UserId, MutableSet<ChatId>>()
    private val chatToSessions = ConcurrentHashMap<ChatId, MutableSet<String>>()

    private val connectionLockRead = ReentrantReadWriteLock()

    override fun afterConnectionClosed(session: WebSocketSession?, status: CloseStatus?) {
        super.afterConnectionClosed(session, status)
    }




    override fun afterConnectionEstablished(session: WebSocketSession) {
        val authHeader=session
            .handshakeHeaders
            .getFirst(HttpHeaders.AUTHORIZATION)
            ?:run {
                logger.warn("Session ${session.id} was closed due to missing Authorization hearder")
                session.close(CloseStatus.SERVER_ERROR.withReason("Authentication faild"))
                return
            }

        val userId=jWTService.getUserIdFromToken(authHeader)
        val userSession= UserSession(
            userId=userId,
            session=session
        )

        connectionLockRead.write {
            sessions[session.id] = userSession
            userToSessions.compute(userId){_,existingSession->
                (existingSession?:mutableSetOf()).apply {
                    add(session.id)
                }
            }
            val chatIds=userChatIds.computeIfAbsent(userId) {
                val chatIds=chatService.findChatsByUser(userId).map { it.id }
                ConcurrentHashMap.newKeySet<ChatId>().apply {
                    addAll(chatIds)
                }
            }
            chatIds.forEach { chatId->
                chatToSessions.compute(chatId){_,sessions->
                    (sessions?:mutableSetOf()).apply {
                        add(session.id)
                    }
                }
            }

        }
        logger.info("Websocket connection establish for user $userId")
    }


    private data class UserSession(
        val userId: UserId,
        val session: WebSocketSession,
    )

}