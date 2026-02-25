package empire.digiprem.chirp.infra.database.entities

import empire.digiprem.chirp.domain.type.UserId
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Index
import jakarta.persistence.Table
import java.time.Instant

@Entity
@Table(
    name = "refresh_token",
    schema = "user_service",
    indexes=[
        Index(name="idx_refresh_token_user_id", columnList = "user_id"),
        Index(name="idx_refresh_token_user_token", columnList = "user_id,hashed_token")
    ]
)
class RefreshTokenEntity(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long=0,
    @Column(nullable = false)
    var userId: UserId,
    @Column(nullable = false)
    var expiresAt: Instant,
    @Column(nullable = false)
    var hashedToken: String,
    @Column(nullable = false)
    var createdAt: Instant=Instant.now(),
)