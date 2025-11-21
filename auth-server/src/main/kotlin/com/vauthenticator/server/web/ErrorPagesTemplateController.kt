package com.vauthenticator.server.web

import com.fasterxml.jackson.databind.ObjectMapper
import jakarta.servlet.RequestDispatcher
import jakarta.servlet.http.HttpServletRequest
import org.springframework.boot.web.servlet.error.ErrorController
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.RequestMapping

@Controller
class ErrorPagesTemplateController(objectMapper: ObjectMapper) : ErrorController {
    private val errorMessages = objectMapper.writeValueAsString(mapOf("defaultMessage" to "Oops........ something went wrong in VAuthenticator"))

    @RequestMapping("/error")
    fun handleError(request: HttpServletRequest, model: Model): String {
        model.addAttribute("errorPageTitle", "VAuthenticator")
        model.addAttribute("errors", errorMessages)
        when (request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE) as Int) {
            400 -> model.addAttribute("assetBundle", "400_error_bundle.js")
            404 -> model.addAttribute("assetBundle", "404_error_bundle.js")
            500 -> model.addAttribute("assetBundle", "500_error_bundle.js")
            else -> model.addAttribute("assetBundle", "default_error_bundle.js")
        }

        return "error-template"
    }

}