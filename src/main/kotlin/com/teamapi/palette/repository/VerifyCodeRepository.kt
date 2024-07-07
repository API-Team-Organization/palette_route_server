package com.teamapi.palette.repository

import com.teamapi.palette.entity.VerifyCode
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository

@Repository
interface VerifyCodeRepository : CrudRepository<VerifyCode, Long>
