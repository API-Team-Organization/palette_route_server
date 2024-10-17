package com.teamapi.palette.service

import com.teamapi.palette.dto.user.UpdateRequest
import com.teamapi.palette.entity.User
import com.teamapi.palette.entity.consts.UserState
import com.teamapi.palette.repository.UserRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test
import java.time.LocalDate

class UserServiceTest {
    private val userRepository = mockk<UserRepository>()
    private val sessionHolder = mockk<SessionHolder>()

    private val user = User(
        email = "test@test.com",
        password = "test",
        username = "test",
        birthDate = LocalDate.now(),
        state = UserState.ACTIVE,
        id = 1
    )

    @Test
    fun me() = runBlocking {
        coEvery {
            sessionHolder.me(userRepository)
        } returns user

        val userService = UserService(userRepository, sessionHolder)
        userService.me()

        coVerify{ sessionHolder.me(userRepository) }
    }

    @Test
    fun update() = runBlocking {
        val updateRequest = UpdateRequest(
            username = "updated_test",
            birthDate = LocalDate.of(2000, 1, 1),
        )

        val updatedUser = User(
            email = "test@test.com",
            password = "test",
            username = "updated_test",
            birthDate = LocalDate.of(2000, 1, 1),
            state = UserState.ACTIVE,
            id = 1
        )

        coEvery {
            sessionHolder.me(userRepository)
        } returns user

        coEvery {
            userRepository.save(any())
        } returns updatedUser

        val userService = UserService(userRepository, sessionHolder)
        userService.update(updateRequest)

        coVerify { sessionHolder.me(userRepository) }
        coVerify { userRepository.save(updatedUser) }
    }
}