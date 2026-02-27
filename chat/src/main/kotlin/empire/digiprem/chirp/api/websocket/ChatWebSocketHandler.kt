package empire.digiprem.chirp.api.websocket

import com.fasterxml.jackson.databind.JsonMappingException
import empire.digiprem.chirp.api.dto.ws.*
import empire.digiprem.chirp.api.mappers.toChatMessageDto
import empire.digiprem.chirp.domain.event.ChatParticipantJoinedEvent
import empire.digiprem.chirp.domain.event.ChatParticipantLeftEvent
import empire.digiprem.chirp.domain.event.MessageDeletedEvent
import empire.digiprem.chirp.domain.type.ChatId
import empire.digiprem.chirp.domain.type.UserId
import empire.digiprem.chirp.service.ChatMessageService
import empire.digiprem.chirp.service.ChatService
import empire.digiprem.chirp.service.JWTService
import org.slf4j.LoggerFactory
import org.springframework.http.HttpHeaders
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import org.springframework.transaction.event.TransactionPhase
import org.springframework.transaction.event.TransactionalEventListener
import org.springframework.web.socket.*
import org.springframework.web.socket.handler.TextWebSocketHandler
import tools.jackson.databind.ObjectMapper
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.locks.ReentrantReadWriteLock
import kotlin.concurrent.read
import kotlin.concurrent.write

/*
@Component
class ChatWebSocketHandler(
    private val chatMessageService: ChatMessageService,
    private val objectMapper: ObjectMapper,
    private val chatService: ChatService,
    private val jWTService: JWTService,
) : TextWebSocketHandler()
{
    private val logger = LoggerFactory.getLogger(javaClass)

    private val connectionLock = ReentrantReadWriteLock()


    private val sessions = ConcurrentHashMap<String, UserSession>()
    private val userToSessions = ConcurrentHashMap<UserId, MutableSet<String>>()
    private val userChatIds = ConcurrentHashMap<UserId, MutableSet<ChatId>>()
    private val chatToSessions = ConcurrentHashMap<ChatId, MutableSet<String>>()

    private val connectionLockRead = ReentrantReadWriteLock()



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
*/

@Component
class ChatWebSocketHandler(
    private val chatMessageService: ChatMessageService, // Service pour sauvegarder les messages en DB
    private val objectMapper: ObjectMapper,             // Pour transformer le JSON en objets Kotlin
    private val chatService: ChatService,               // Pour vérifier à quels chats appartient l'user
    private val jWTService: JWTService,                 // Pour identifier l'user via son token
) : TextWebSocketHandler() { // TextWebSocketHandler = spécialisé dans l'envoi/réception de texte (JSON)

    companion object {
        private const val PING_INTERVAL_MS = 30_000L
        private val PONG_TIMEOUT_MS = 30_000L
    }


    private val logger = LoggerFactory.getLogger(javaClass)

    // Verrou pour éviter que deux fils (threads) ne modifient les listes en même temps
    private val connectionLockRead = ReentrantReadWriteLock()

    // --- NOS REGISTRES (STOCKED EN RAM) ---
    // Associe un ID de session (ex: "abc-123") à une session complète
    private val sessions = ConcurrentHashMap<String, UserSession>()

    // Associe un User à ses sessions (un user peut avoir plusieurs téléphones connectés)
    private val userToSessions = ConcurrentHashMap<UserId, MutableSet<String>>()

    // Cache : Liste des IDs de chats auxquels appartient un utilisateur
    private val userChatIds = ConcurrentHashMap<UserId, MutableSet<ChatId>>()

    // La liste des sessions actives pour CHAQUE chat (Crucial pour le broadcast)
    private val chatToSessions = ConcurrentHashMap<ChatId, MutableSet<String>>()

    // ÉTAPE 1 : QUAND UN UTILISATEUR SE CONNECTE
    override fun afterConnectionEstablished(session: WebSocketSession) {
        // 1. On cherche le Token dans les headers de la poignée de main (handshake)
        val authHeader = session.handshakeHeaders.getFirst(HttpHeaders.AUTHORIZATION)
            ?: run {
                logger.warn("Session ${session.id} fermée : Pas de Token")
                session.close(CloseStatus.SERVER_ERROR.withReason("Authentication failed"))
                return
            }

        // 2. On récupère le UserId via le Token
        val userId = jWTService.getUserIdFromToken(authHeader)
        val userSession = UserSession(
            userId = userId,
            session = session,
        )

        // 3. MISE À JOUR DU REGISTRE (On verrouille l'écriture pour être tranquille)
        connectionLockRead.write {
            // On enregistre la session globale
            sessions[session.id] = userSession

            // On lie l'utilisateur à cette nouvelle session
            userToSessions.compute(userId) { _, existingSessions ->
                (existingSessions ?: mutableSetOf()).apply { add(session.id) }
            }

            // On récupère ses chats (en DB) s'ils ne sont pas déjà en cache
            val chatIds = userChatIds.computeIfAbsent(userId) {
                val idsFromDb = chatService.findChatsByUser(userId).map { it.id }
                ConcurrentHashMap.newKeySet<ChatId>().apply { addAll(idsFromDb) }
            }

            // Pour chaque chat, on dit : "Cette session écoute ce qui se passe ici"
            chatIds.forEach { chatId ->
                chatToSessions.compute(chatId) { _, sessions ->
                    (sessions ?: mutableSetOf()).apply { add(session.id) }
                }
            }
        }
        logger.info("Connexion WebSocket établie pour l'user $userId")
    }

    override fun afterConnectionClosed(session: WebSocketSession, status: CloseStatus) {
        connectionLockRead.write {
            sessions.remove(session.id )?.let {userSession ->
                val userId=userSession.userId

                userToSessions.compute(userId){_,sessions->
                    sessions
                        ?.apply { remove(session.id) }
                        ?.takeIf { it.isNotEmpty() }
                }

                userChatIds[userId]?.forEach { chatId ->
                    chatToSessions.compute(userId){_,sessions->
                        sessions
                            ?.apply { remove(session.id) }
                            ?.takeIf { it.isNotEmpty() }
                    }

                }

                logger.info("Websocket session closed for user $userId")
            }
        }
    }


    override fun handleTransportError(session: WebSocketSession, exception: Throwable) {
        logger.error("Transport error for session ${session.id}", exception)
        session.close(CloseStatus.SERVER_ERROR.withReason("Transport error"))
    }

    // ÉTAPE 2 : QUAND LE SERVEUR REÇOIT UN MESSAGE DE L'UTILISATEUR
    override fun handleTextMessage(session: WebSocketSession, message: TextMessage) {
        // On récupère l'identité de celui qui parle
        val userSession = connectionLockRead.read { sessions[session.id] ?: return }

        try {
            // On décode le "paquet" reçu
            val webSocketMessage = objectMapper.readValue(message.payload, IncomingWebSocketMessage::class.java)

            // Si le type est "NOUVEAU_MESSAGE"
            when (webSocketMessage.type) {
                IncomingWebSocketMessageType.NEW_MESSAGE -> {
                    val dto = objectMapper.readValue(webSocketMessage.payload, SendMessageDto::class.java)
                    handlerSendMessage(dto = dto, senderId = userSession.userId)
                }
            }
        } catch (e: JsonMappingException) {
            sendError(userSession.session, ErrorDto("INVALID_JSON", "Format JSON invalide"))
        }
    }

    // ÉTAPE 3 : TRAITEMENT DE L'ENVOI D'UN MESSAGE
    private fun handlerSendMessage(dto: SendMessageDto, senderId: UserId) {
        // Sécurité : On vérifie que l'user fait bien partie du chat
        val userChats = connectionLockRead.read { userChatIds[senderId] } ?: return
        if (dto.chatId !in userChats) return

        // On sauvegarde le message en Base de Données (Postgres)
        val savedMessage = chatMessageService.sendMessage(
            chatId = dto.chatId,
            senderId = senderId,
            content = dto.content,
            messageId = dto.messageId
        )

        // On envoie le message à TOUS les gens connectés dans ce chat
        broadcastToChat(
            chatId = dto.chatId,
            message = OutgoingWebSocketMessage(
                type = OutgoingWebSocketMessageType.NEW_MESSAGE,
                payload = objectMapper.writeValueAsString(savedMessage.toChatMessageDto()),
            )
        )
    }

    // ÉTAPE 4 : LE MÉGAPHONE (BROADCAST)
    private fun broadcastToChat(chatId: ChatId, message: OutgoingWebSocketMessage) {
        // On récupère la liste de tous les "tuyaux" connectés à ce chat
        val targetSessions = connectionLockRead.read { chatToSessions[chatId]?.toList() ?: emptyList() }

        targetSessions.forEach { sessionId ->
            val userSession = connectionLockRead.read { sessions[sessionId] }
            userSession?.let {
                // On envoie à l'utilisateur spécifique
                sendToUser(userId = it.userId, message = message)
            }
        }
    }

    // ÉTAPE 5 : L'ENVOI FINAL SUR LE TUYAU
    private fun sendToUser(userId: UserId, message: OutgoingWebSocketMessage) {
        val userSessions = connectionLockRead.read { userToSessions[userId] ?: emptySet() }

        userSessions.forEach { sessionId ->
            val userSession = connectionLockRead.read { sessions[sessionId] ?: return@forEach }

            // Si le tuyau est toujours ouvert, on pousse le JSON
            if (userSession.session.isOpen) {
                try {
                    val messageJson = objectMapper.writeValueAsString(message)
                    userSession.session.sendMessage(TextMessage(messageJson))
                } catch (e: Exception) {
                    logger.warn("Erreur d'envoi à $userId", e)
                }
            }
        }
    }


    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    fun onDeleteMessage(event: MessageDeletedEvent) {
        broadcastToChat(
            chatId = event.chatId,
            message = OutgoingWebSocketMessage(
                type = OutgoingWebSocketMessageType.MESSAGE_DELETED,
                payload = objectMapper.writeValueAsString(
                    DeleteMessageDto(
                        chatId = event.chatId,
                        messageId = event.messageId
                    )
                )
            )
        )
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    fun onJoinChat(event: ChatParticipantJoinedEvent) {
        connectionLockRead.write {
            event.userIds.forEach { userId ->
                userChatIds.compute(userId) { _, chatIds ->
                    (chatIds ?: mutableSetOf()).apply { add(event.chatId) }
                }
                userToSessions[userId]?.forEach { sessionId ->
                    chatToSessions.compute(event.chatId) { _, sessions ->
                        (sessions ?: mutableSetOf()).apply { add(sessionId) }
                    }
                }
            }
        }

        broadcastToChat(
            chatId = event.chatId,
            message = OutgoingWebSocketMessage(
                type = OutgoingWebSocketMessageType.CHAT_PARTICIPANT_CHANGED,
                payload = objectMapper.writeValueAsString(
                    ChatParticipantsChangedDto(
                        chatId = event.chatId
                    )
                )
            )
        )
    }


    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    fun onLeftChat(event: ChatParticipantLeftEvent) {
        connectionLockRead.write {
            userChatIds.compute(event.userId) { _, chatIds ->
                chatIds
                    ?.apply { remove(event.chatId) }
                    ?.takeIf { it.isNotEmpty() }
            }

        }

        userToSessions[event.userId]?.forEach { sessionId ->
            chatToSessions.compute(event.chatId) { _, sessions ->
                sessions
                    ?.apply { remove(sessionId) }
                    ?.takeIf { it.isNotEmpty() }
            }
        }

    }

    override fun handlePongMessage(session: WebSocketSession, message: PongMessage) {
        connectionLockRead.write {
            sessions.compute(session.id) { _, userSession ->
                userSession?.copy(
                    lastPongTimestamp = System.currentTimeMillis(),
                )
            }
            logger.debug("Received pong from ${session.id}")
        }
    }

    @Scheduled(fixedDelay = PING_INTERVAL_MS)
    fun pingClients() {
        val currentTime = System.currentTimeMillis()
        val sessionsToClose = mutableListOf<String>()

        val sessionsSnapshot = connectionLockRead.read { sessions.toMap() }

        sessionsSnapshot.forEach { (sessionId, userSession) ->
            try {
                if (userSession.session.isOpen) {
                    val lastPong = userSession.lastPongTimestamp
                    if (currentTime - lastPong > PONG_TIMEOUT_MS) {
                        logger.warn("Session $sessionId has timed out, closing connection.")
                        sessionsToClose.add(sessionId)
                        return@forEach
                    }

                    userSession.session.sendMessage(PingMessage())
                    logger.debug("Send ping to {}", userSession.userId)
                }
            } catch (e: Exception) {
                logger.error("Could not ping session $sessionId", e)
                sessionsToClose.add(sessionId)
            }
        }

        sessionsToClose.forEach { sessionId ->
            connectionLockRead.read {
                sessions[sessionId]?.session?.let { session ->
                    try {
                        session.close(CloseStatus.GOING_AWAY.withReason("Ping timeout"))
                    }catch (e: Exception) {
                        logger.error("Could not close sessions  for session ${session.id}")
                    }
                }
            }
        }
    }

    // Petit outil pour renvoyer une erreur au client en cas de problème
    private fun sendError(session: WebSocketSession, error: ErrorDto) {
        val msg = OutgoingWebSocketMessage(OutgoingWebSocketMessageType.ERROR, objectMapper.writeValueAsString(error))
        session.sendMessage(TextMessage(objectMapper.writeValueAsString(msg)))
    }

    // Structure interne pour stocker les infos d'une connexion
    private data class UserSession(
        val userId: UserId,
        val session: WebSocketSession,
        val lastPongTimestamp: Long = System.currentTimeMillis(),
    )
}