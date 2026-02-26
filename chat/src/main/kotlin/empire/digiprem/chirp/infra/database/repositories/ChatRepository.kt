package empire.digiprem.empire.digiprem.chirp.infra.database.repositories

import empire.digiprem.chirp.domain.models.Chat
import empire.digiprem.chirp.domain.type.ChatId
import empire.digiprem.chirp.domain.type.UserId
import empire.digiprem.empire.digiprem.chirp.infra.database.entities.ChatEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query

interface ChatRepository : JpaRepository<ChatEntity, ChatId> {
    @Query(
        """
            SELECT C
            FROM ChatEntity C
            LEFT JOIN FETCH C.participants
            LEFT JOIN FETCH C.creator
            WHERE C.id = :id
            AND EXISTS (
                SELECT 1
                FROM C.participants P
                WHERE P.userId = :userId
            )
        """
    )
    fun findChatById(id: ChatId,userId: UserId): ChatEntity?


    // Query1: Query all chats by user
    // Query2: Query all the participates of chat A
    // query3: Query the creator of chat A
    // Query4: Query all the participates of chat B
    // Query5: Query the creator of chat B
    // Query6: Query all the participates of chat C
    // Query7: Query the creator of chat C

    @Query("""
        SELECT C
        FROM ChatEntity C
        LEFT JOIN FETCH C.participants
        LEFT JOIN FETCH C.creator
        WHERE EXISTS (
            SELECT 1
            FROM C.participants P
            WHERE P.userId = :userId
        )
    """)
    fun findAllByUserId(userId: UserId):List<ChatEntity>

}