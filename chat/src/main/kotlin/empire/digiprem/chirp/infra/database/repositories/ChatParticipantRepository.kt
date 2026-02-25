package empire.digiprem.empire.digiprem.chirp.infra.database.repositories

import empire.digiprem.chirp.domain.type.UserId
import empire.digiprem.chirp.infra.database.entities.ChatParticipantEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query

interface ChatParticipantRepository : JpaRepository<ChatParticipantEntity, UserId>{
    fun findByUserIdIn(userIds: List<UserId>): Set<ChatParticipantEntity>
    @Query(
        """
            SELECT P
            FROM ChatParticipantEntity P
            WHERE LOWER(P.username) = :query OR LOWER(P.email) = :query
        """
    )
    fun findByEmailOrUsername(query:String):ChatParticipantEntity?
}