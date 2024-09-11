package com.teamapi.palette.annotations

import io.swagger.v3.oas.annotations.security.SecurityRequirement

@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.CLASS, AnnotationTarget.FUNCTION)
@SecurityRequirement(name = "X-AUTH-Token")
annotation class SwaggerRequireAuthorize
