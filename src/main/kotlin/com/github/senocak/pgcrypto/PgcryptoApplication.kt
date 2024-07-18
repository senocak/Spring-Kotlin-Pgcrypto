package com.github.senocak.pgcrypto

import jakarta.persistence.criteria.CriteriaBuilder
import jakarta.persistence.criteria.CriteriaQuery
import jakarta.persistence.criteria.Join
import jakarta.persistence.criteria.JoinType
import jakarta.persistence.criteria.Predicate
import jakarta.persistence.criteria.Root
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.boot.runApplication
import org.springframework.context.event.EventListener
import org.springframework.data.jpa.domain.Specification
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@SpringBootApplication
@RestController
class PgcryptoApplication(
    private val userRepository: UserRepository,
    private val messageRepository: MessageRepository,
    private val studentRepository: StudentRepository,
    private val courseRepository: CourseRepository,
) {

    @GetMapping("/users")
    fun getAllUsers(): MutableList<User> = userRepository.findAll()

    @GetMapping("/messages")
    fun getAllMessages(
        @RequestParam firstname: String?,
        @RequestParam lastname: String?,
    ): MutableList<Message> {
        val specification = Specification { root: Root<Message>, query: CriteriaQuery<*>, builder: CriteriaBuilder ->
            val predicates: MutableList<Predicate> = ArrayList()

            val fromUser: Join<User, Message> = root.join("from", JoinType.LEFT)
            val toUser: Join<User, Message> = root.join("to", JoinType.LEFT)
            if (!firstname.isNullOrEmpty()){
                val predicateNameFrom: Predicate = builder.like(builder.lower(fromUser.get("firstname")), "%${firstname.lowercase()}%")
                val predicateNameTo: Predicate = builder.like(builder.lower(toUser.get("firstname")), "%${firstname.lowercase()}%")
                predicates.add(builder.or(predicateNameFrom, predicateNameTo))
            }
            if (!lastname.isNullOrEmpty()){
                val predicateLastNameFrom: Predicate = builder.like(builder.lower(fromUser.get("lastname")), "%${lastname.lowercase()}%")
                val predicateLastNameTo: Predicate = builder.like(builder.lower(toUser.get("lastname")), "%${lastname.lowercase()}%")
                predicates.add(builder.or(predicateLastNameFrom, predicateLastNameTo))
            }

            query.where(*predicates.toTypedArray()).distinct(true).restriction
        }
        return messageRepository.findAll(specification)
    }

    @GetMapping("/students")
    fun getAllStudents(
        @RequestParam name: String?
    ): List<Student> {
        val specification = Specification { root: Root<Student>, query: CriteriaQuery<*>, builder: CriteriaBuilder ->
            val predicates: MutableList<Predicate> = ArrayList()

            val likedCourses: Join<User, Student> = root.join("likedCourses", JoinType.LEFT)
            val hatedCourses: Join<User, Student> = root.join("hatedCourses", JoinType.LEFT)
            if (!name.isNullOrEmpty()){
                val predicateNameFrom: Predicate = builder.like(builder.lower(likedCourses.get("name")), "%${name.lowercase()}%")
                val predicateNameTo: Predicate = builder.like(builder.lower(hatedCourses.get("name")), "%${name.lowercase()}%")
                predicates.add(builder.or(predicateNameFrom, predicateNameTo))
            }
            query.where(*predicates.toTypedArray()).distinct(true).restriction
        }
        return studentRepository.findAll(specification)
    }

    @EventListener(value = [ApplicationReadyEvent::class])
    fun init(event: ApplicationReadyEvent) {
        userRepository.deleteAll()
        messageRepository.deleteAll()

        val user1: User = userRepository.save(User(firstname = "Anıl1", lastname = "Senocak1"))
        val user2: User = userRepository.save(User(firstname = "Anıl2", lastname = "Senocak2"))
        val user3: User = userRepository.save(User(firstname = "Anıl3", lastname = "Senocak3"))

        val message1: Message = messageRepository.save(Message(from = user1, to = user2, content = "1to2-1"))
        val message2: Message = messageRepository.save(Message(from = user1, to = user2, content = "1to2-2"))
        val message3: Message = messageRepository.save(Message(from = user1, to = user2, content = "1to2-3"))

        val message4: Message = messageRepository.save(Message(from = user2, to = user3, content = "2to3-1"))

        courseRepository.deleteAll()
        studentRepository.deleteAll()
        val course1: Course = courseRepository.save(Course(name = "course1"))
        val course2: Course = courseRepository.save(Course(name = "course2"))
        val course3: Course = courseRepository.save(Course(name = "course3"))
        val course4: Course = courseRepository.save(Course(name = "course4"))
        val student1: Student = studentRepository.save(Student(name = "student1").also { it.likedCourses = setOf(course1); it.hatedCourses = setOf(course1); })
        val student2: Student = studentRepository.save(Student(name = "student2").also { it.likedCourses = setOf(course1, course2); it.hatedCourses = setOf(course2, course3); })
    }
}

fun main(args: Array<String>) {
    runApplication<PgcryptoApplication>(*args)
}


//    @Bean
//    public SecurityFilterChain configure(AuthenticationManagerBuilder auth) throws Exception {
//        auth.
//                jdbcAuthentication()
//                .usersByUsernameQuery("select email as principal, password as credentials, true from user where email=?")
//                .authoritiesByUsernameQuery("select u.email as principal, r.role as role from user u inner join user_role ur on(u.user_id=ur.user_id) inner join role r on(ur.role_id=r.role_id) where u.email=?")
//                .dataSource(dataSource)
//                .passwordEncoder(bCryptPasswordEncoder)
//                .rolePrefix("ROLE_");
//    }