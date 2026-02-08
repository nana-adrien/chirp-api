package empire.digiprem.chirp.infra.database.repositories

import empire.digiprem.chirp.domain.model.UserId
import empire.digiprem.chirp.infra.database.entities.UserEntity
import org.springframework.data.jpa.repository.JpaRepository

interface UserRepository: JpaRepository<UserEntity, UserId> {

    fun findByEmail(email: String): UserEntity?
    fun findByEmailOrUsername(email: String,username: String): UserEntity?

}
