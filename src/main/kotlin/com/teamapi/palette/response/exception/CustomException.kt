package com.teamapi.palette.response.exception

import com.teamapi.palette.response.ResponseCode

class CustomException(val responseCode: ResponseCode, vararg val formats: Any?) : RuntimeException(responseCode.message)
