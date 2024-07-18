package com.github.senocak.pgcrypto

import com.fasterxml.jackson.annotation.JsonIgnore
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.ForeignKey
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.JoinTable
import jakarta.persistence.ManyToMany
import jakarta.persistence.ManyToOne
import jakarta.persistence.MappedSuperclass
import jakarta.persistence.Table
import java.io.Serializable
import java.util.Date
import org.hibernate.annotations.ColumnTransformer
import org.hibernate.annotations.OnDelete
import org.hibernate.annotations.OnDeleteAction
import org.hibernate.annotations.UuidGenerator
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.JpaSpecificationExecutor

@MappedSuperclass
open class BaseDomain(
    @Id
    @UuidGenerator(style = UuidGenerator.Style.RANDOM)
    @Column(name = "id", updatable = false, nullable = false)
    var id: String? = null,
    @Column var createdAt: Date = Date(),
): Serializable

@Entity
@Table(name = "users")
data class User(
    @Column(name = "firstname", nullable = false, columnDefinition = "bytea")
    @ColumnTransformer(
        forColumn = "firstname",
        read = "pgp_sym_decrypt(firstname, 'pswd')",
        write = "pgp_sym_encrypt(?, 'pswd')"
    )
    var firstname: String,

    @Column(name = "lastname", nullable = false, columnDefinition = "bytea")
    @ColumnTransformer(
        forColumn = "lastname",
        read = "pgp_sym_decrypt(lastname, 'pswd')",
        write = "pgp_sym_encrypt(?, 'pswd')"
    )
    var lastname: String,
): BaseDomain()


@Entity
@Table(name = "messages")
data class Message (
    @ManyToOne(fetch = FetchType.LAZY)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(
        name = "user_from_id",
        referencedColumnName = "id",
        nullable = false,
        foreignKey = ForeignKey(name = "fk_user_message_user_from_id")
    )
    val from: User,

    @ManyToOne(fetch = FetchType.LAZY)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(
        name = "user_to_id",
        referencedColumnName = "id",
        nullable = false,
        foreignKey = ForeignKey(name = "fk_user_message_user_to_id")
    )
    val to: User,

    @Column(name = "content", nullable = false, columnDefinition = "bytea")
    @ColumnTransformer(
        forColumn = "content",
        read = "pgp_sym_decrypt(content, 'pswd')",
        write = "pgp_sym_encrypt(?, 'pswd')"
    )
    var content: String
) : BaseDomain()

interface UserRepository: JpaRepository<User, String>, JpaSpecificationExecutor<User>
interface MessageRepository: JpaRepository<Message, String>, JpaSpecificationExecutor<Message>


@Entity
@Table(name = "student")
class Student(
    @Column(name = "name", nullable = false, columnDefinition = "bytea")
    @ColumnTransformer(
        forColumn = "name",
        read = "pgp_sym_decrypt(name, 'pswd')",
        write = "pgp_sym_encrypt(?, 'pswd')"
    )
    var name: String
): BaseDomain() {
    @ManyToMany
    @JoinTable(
        name = "course_like",
        joinColumns = [JoinColumn(name = "student_id")],
        inverseJoinColumns = [JoinColumn(name = "course_id")]
    )
    @JsonIgnore
    var likedCourses: Set<Course>? = null

    @ManyToMany
    @JoinTable(
        name = "course_hate",
        joinColumns = [JoinColumn(name = "student_id")],
        inverseJoinColumns = [JoinColumn(name = "course_id")]
    )
    @JsonIgnore
    var hatedCourses: Set<Course>? = null
}

@Entity
@Table(name = "course")
class Course(
    @Column(name = "name", nullable = false, columnDefinition = "bytea")
    @ColumnTransformer(
        forColumn = "name",
        read = "pgp_sym_decrypt(name, 'pswd')",
        write = "pgp_sym_encrypt(?, 'pswd')"
    )
    var name: String
): BaseDomain() {
    @ManyToMany(mappedBy = "likedCourses")
    var likes: Set<Student>? = null
}
interface StudentRepository: JpaRepository<Student, String>, JpaSpecificationExecutor<Student>
interface CourseRepository: JpaRepository<Course, String>, JpaSpecificationExecutor<Course>