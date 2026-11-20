package com.vauthenticator.server.web

import jakarta.servlet.RequestDispatcher
import jakarta.servlet.http.HttpServletRequest
import org.springframework.boot.webmvc.error.ErrorController
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.RequestMapping
import tools.jackson.databind.ObjectMapper
import com.vauthenticator.server.page.domain.Page

@Controller
class ErrorPagesTemplateController(objectMapper: ObjectMapper) : ErrorController {
    private val errorMessages = objectMapper.writeValueAsString(mapOf("defaultMessage" to "Oops........ something went wrong in VAuthenticator"))

    @RequestMapping("/error")
    fun handleError(request: HttpServletRequest, model: Model): String {
        model.addAttribute("errorPageTitle", "VAuthenticator")
        model.addAttribute("errors", errorMessages)
        when (request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE) as Int) {
            400 -> model.addAttribute("assetBundle", Page.BAD_REQUEST_ERROR.bundle)
            404 -> model.addAttribute("assetBundle", Page.NOT_FOUND_ERROR.bundle)
            500 -> model.addAttribute("assetBundle", Page.SERVER_ERROR.bundle)
            else -> model.addAttribute("assetBundle", Page.DEFAULT_ERROR.bundle)
        }

        return "error-template"
    }

}