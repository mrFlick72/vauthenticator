package it.valeriovaudi.vauthenticator.account

import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.test.context.junit4.SpringRunner

@DataMongoTest
@RunWith(SpringRunner::class)
class MongoAccountRepositoryTest {

    @Autowired
    lateinit var mongoTemplate: MongoTemplate

    private val sub = "A_SUB"
    private val account = AccountTestFixture.anAccount()

    lateinit var mongoAccountRepository: MongoAccountRepository

    @Before
    fun setUp() {
        mongoAccountRepository = MongoAccountRepository(mongoTemplate)
    }

    @Test
    fun `find an account by email`() {
        mongoAccountRepository.save(account)

        val findByUsername: Account = mongoAccountRepository.accountFor(account.username).orElseThrow()
        assertThat(findByUsername, equalTo(account))
    }

    @Test
    fun `save an account by email`() {
        mongoAccountRepository.save(account)

        val findByUsername: Account = mongoAccountRepository.accountFor(account.username).orElseThrow()
        assertThat(findByUsername, equalTo(account))

        val accountUpdated = account.copy(firstName = "A_NEW_FIRSTNAME", lastName = "A_NEW_LASTNAME")
        mongoAccountRepository.save(accountUpdated)

        val updatedFindByUsername = mongoAccountRepository.accountFor(account.username).orElseThrow()
        assertThat(updatedFindByUsername, equalTo(accountUpdated))
    }

}