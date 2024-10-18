package com.teamapi.palette.service.infra

import com.teamapi.palette.response.ErrorCode
import com.teamapi.palette.response.exception.CustomException
import jakarta.mail.MessagingException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.slf4j.LoggerFactory
import org.springframework.mail.MailException
import org.springframework.mail.javamail.JavaMailSender
import org.springframework.mail.javamail.MimeMessageHelper
import org.springframework.stereotype.Component
import kotlin.random.Random
import kotlin.random.nextInt

@Component
class MailSendService(private val mailSender: JavaMailSender) {
    companion object {
        const val MAIL_TEMPLATE = """<!DOCTYPE html>
<html lang="ko">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Palette - 이메일 인증</title>
    <style>
        body {
            font-family: Arial, sans-serif;
            background-color: #f6f6f6;
            margin: 0;
            padding: 0;
            display: flex;
            justify-content: center;
            align-items: center;
            height: 100vh;
        }
        .container {
            background-color: #ffffff;
            border-radius: 10px;
            box-shadow: 0 4px 8px rgba(0, 0, 0, 0.1);
            width: 100%;
            max-width: 600px;
            padding: 20px;
            box-sizing: border-box;
        }
        .header {
            text-align: center;
            padding-bottom: 20px;
            border-bottom: 1px solid #dddddd;
        }
        .header h1 {
            margin: 0;
            font-size: 24px;
            color: #333333;
        }
        .content {
            padding: 20px 0;
            text-align: center;
        }
        .content p {
            margin: 0;
            font-size: 16px;
            color: #666666;
        }
        .verification-code {
            display: inline-block;
            margin-top: 20px;
            padding: 10px 20px;
            background-color: #4caf50;
            color: #ffffff;
            border-radius: 5px;
            font-size: 18px;
            font-weight: bold;
            letter-spacing: 2px;
        }
        .footer {
            text-align: center;
            padding-top: 20px;
            border-top: 1px solid #dddddd;
            font-size: 12px;
            color: #aaaaaa;
        }
    </style>
</head>
<body>
    <div class="container">
        <div class="header">
            <h1>Palette에 오신 것을 환영합니다!</h1>
        </div>
        <div class="content">
            <p>회원가입해주셔서 감사합니다. 아래의 코드를 사용하여 이메일 주소를 인증해주세요:</p>
            <div class="verification-code">{{VERIFICATION_CODE}}</div>
        </div>
        <div class="footer">
            <p>&copy; 2024 Palette. 모든 권리 보유.</p>
        </div>
    </div>
</body>
</html>"""
        private val log = LoggerFactory.getLogger(MailSendService::class.java)
    }

    suspend fun sendEmail(toMail: String, code: String) {
        withContext(Dispatchers.IO) {
            val caught = runCatching {
                val mime = mailSender.createMimeMessage()
                MimeMessageHelper(mime, false, "UTF-8")
                    .apply {
                        setTo(toMail)
                        setSubject("[Palette] 이메일 인증 코드")
                        setText(MAIL_TEMPLATE.replace("{{VERIFICATION_CODE}}", code), true)
                    }

                mailSender.send(mime)
            }

            if (caught.isFailure) {
                val e = caught.exceptionOrNull()

                if (e is MessagingException) {
                    log.error("error while sending mail", e)
                    throw Exception(e) // need to be handled by reporter
                }
                if (e is MailException) {
                    log.error("error while sending mail", e)
                    throw CustomException(ErrorCode.MAIL_SEND_FAILED)
                }
            }

        }
    }

    fun createVerifyCode() = List(6) { Random.nextInt(0..9) }.joinToString("")
}
