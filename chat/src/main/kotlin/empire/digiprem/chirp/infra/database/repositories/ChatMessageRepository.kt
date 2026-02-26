package empire.digiprem.chirp.infra.database.repositories

import empire.digiprem.chirp.domain.type.ChatId
import empire.digiprem.chirp.domain.type.ChatMessageId
import empire.digiprem.chirp.infra.database.entities.ChatMessageEntity
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Slice
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import java.time.Instant


interface ChatMessageRepository : JpaRepository<ChatMessageEntity, ChatMessageId> {

    @Query(
        """
            SELECT M
            FROM ChatMessageEntity M
            WHERE M.chatId = :chatId
            AND M.createdAt < :before
            ORDER BY M.createdAt DESC
        """
    )
    fun findByChatIdBefore(
        chatId: ChatId,
        before: Instant,
        pegeable: Pageable
    ): Slice<ChatMessageEntity>


    @Query(
        """
            SELECT M
            FROM ChatMessageEntity M
            LEFT JOIN FETCH M.sender
            WHERE M.chatId IN :chatIds
            AND (M.createdAt,M.id)=(
                SELECT M2.createdAt, M2.id
                FROM ChatMessageEntity M2
                WHERE M2.chatId = :chatId
                ORDER BY M2.createdAt DESC
                LIMIT 1
            )
        """
    )
    fun findLatestMessagesByChatId(
        chatIds: Set<ChatId>
    ):Slice<ChatMessageEntity>
}