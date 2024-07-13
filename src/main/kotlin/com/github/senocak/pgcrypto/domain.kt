package com.github.senocak.pgcrypto

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.ForeignKey
import jakarta.persistence.GeneratedValue
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.MappedSuperclass
import jakarta.persistence.Table
import java.io.Serializable
import java.util.Date
import org.hibernate.annotations.ColumnTransformer
import org.hibernate.annotations.GenericGenerator
import org.hibernate.annotations.OnDelete
import org.hibernate.annotations.OnDeleteAction
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.JpaSpecificationExecutor

@MappedSuperclass
open class BaseDomain(
    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(name = "id", updatable = false, nullable = false)
    var id: String? = null,
    @Column var createdAt: Date = Date(),
    @Column var updatedAt: Date = Date(),
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