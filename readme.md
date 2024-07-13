# Encrypting a particular column(s)

We'll be using a postgres extension called [pgcrypto](https://www.postgresql.org/docs/8.3/pgcrypto.html). The pgcrypto module provides cryptographic functions for PostgreSQL.

## Install pgcrypto extension in postgres

```sql
CREATE EXTENSION IF NOT EXISTS "pgcrypto";
```

will now be updated to:

```kotlin
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
```

Postgres invoked `pgp_sym_decrypt(...)` function to decrypt data while reading from the database into the application and called    `pgp_sym_encrypt(...)` function to encrypt data while persisting the data.

`"pswd"` is the key used to encrypt and decrypt. It could be anything.

The more appropriate place to store the key would be `postgresql.conf` file.

Then the key can be fetched using the `current_setting(...)` function. Something like this:

```kotlin
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
        read = "pgp_sym_decrypt(lastname, current_setting('key'))",
        write = "pgp_sym_encrypt(?, current_setting('key'))"
    )
    var lastname: String,
): BaseDomain()
```
