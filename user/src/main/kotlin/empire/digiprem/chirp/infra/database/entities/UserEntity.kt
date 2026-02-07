package empire.digiprem.empire.digiprem.user.infra.database.entities

import empire.digiprem.empire.digiprem.user.domain.model.UserId
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Index
import jakarta.persistence.Table
import org.hibernate.annotations.CreationTimestamp
import org.hibernate.annotations.UpdateTimestamp
import java.time.Instant

@Entity
@Table(
    name="users",
    schema="user_service",
    indexes=[
        Index(name = "idx_users_email", columnList = "email",),
        Index(name = "idx_users_username", columnList = "username",)
    ]
)

class UserEntity {
    @Id
   @GeneratedValue(strategy = GenerationType.UUID) var id: UserId?=null
    @Column(nullable = false, unique = true)
    var email: String?=null
    @Column(nullable = false, unique = true)
    var username: String?=null
    @Column(nullable = false, unique = true)
    var hashedPassword:String?=null
    var hasVerifiedEmail:Boolean=false
    @CreationTimestamp
    var createdAt:Instant= Instant.now()
    @UpdateTimestamp
    var updatedAt:Instant=Instant.now()
}