package empire.digiprem.empire.digiprem.chirp.infra.database.entities

import empire.digiprem.chirp.domain.models.ChatParticipant
import empire.digiprem.chirp.domain.type.ChatId
import empire.digiprem.chirp.domain.type.ChatMessageId
import empire.digiprem.chirp.infra.database.entities.ChatParticipantEntity
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Index
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import org.hibernate.annotations.CreationTimestamp
import org.springframework.data.annotation.CreatedDate
import java.time.Instant

@Entity
@Table(
    name = "chat_messages",
    schema = "chat_service",
    indexes=[
        Index(
            name = "idx_chat_message_chat_id_created_at",
            columnList = "chat_id,created_at DESC",
        )
    ]
)
class ChatMessageEntity (
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    var id: ChatMessageId?=null,
    @Column(nullable = false)
    var content: String,
    @JoinColumn(
        name = "chat_id",
        nullable = false,
        insertable = false,
        updatable = false,
    )
    var chatId: ChatId,
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
        name = "chat_id",
        nullable = false,
        insertable = false,
        updatable = false,
    )
    var chat: ChatEntity?=null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
        name = "sender_id",
        nullable = false,
        insertable = false,
        updatable = false,
    )
    var sender: ChatParticipantEntity,
    @CreationTimestamp
    var createdAt: Instant=Instant.now(),
)