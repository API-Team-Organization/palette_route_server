package com.teamapi.palette.dto.user

import java.time.LocalDate

data class UpdateRequest (
    val username: String?,
    val birthDate: LocalDate?
)