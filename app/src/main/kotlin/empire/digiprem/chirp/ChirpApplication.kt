package empire.digiprem.chirp

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.scheduling.annotation.EnableScheduling


@SpringBootApplication
@EnableScheduling
class ChirpApplication

fun main(args: Array<String>) {
    runApplication<ChirpApplication>(*args)
}

/*@Component
class Demo(
    private val repository: UserRepository,
){
    @PostConstruct
    fun init(){
        repository.save(
            UserEntity(
                email = "kako@Gmail.com",
                hashedPassword = "kako55123",
                username = "kako123",
            )
        )
    }
}*/
