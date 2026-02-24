package empire.digiprem.chirp.infra.database.repositories

import empire.digiprem.empire.digiprem.chirp.domain.type.UserId
import empire.digiprem.chirp.infra.database.entities.RefreshTokenEntity
import org.springframework.data.jpa.repository.JpaRepository

interface RefreshTokenRepository: JpaRepository<RefreshTokenEntity, Long> {

    fun findByUserIdAndHashedToken(userId: UserId,hashedToken: String): RefreshTokenEntity?
    fun deleteByUserIdAndHashedToken(userId: UserId,hashedToken: String)
    fun deleteByUserId(userId: UserId)
}