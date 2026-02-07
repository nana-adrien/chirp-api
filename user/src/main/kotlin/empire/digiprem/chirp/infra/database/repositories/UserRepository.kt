package empire.digiprem.empire.digiprem.user.infra.database.repositories

import empire.digiprem.empire.digiprem.user.domain.model.UserId
import empire.digiprem.empire.digiprem.user.infra.database.entities.UserEntity
import org.springframework.data.jpa.repository.JpaRepository

interface UserRepository: JpaRepository<UserEntity, UserId> {


}