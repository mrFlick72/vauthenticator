package com.vauthenticator.server.i18n

import com.fasterxml.jackson.databind.ObjectMapper
import com.vauthenticator.server.web.CurrentHttpServletRequestService
import jakarta.servlet.http.HttpServletRequest
import org.springframework.ui.Model
import org.springframework.web.servlet.LocaleResolver
import java.util.*


class I18nMessageInjector(
    private val localeResolver: LocaleResolver,
    private val objectMapper: ObjectMapper,
    private val i18nMessageRepository: I18nMessageRepository,
    private val currentHttpServletRequestService: CurrentHttpServletRequestService
) {
    fun setMessagedFor(
        i18nScope: I18nScope,
        model: Model
    ) {
        val httpServletRequest = currentHttpServletRequestService.getServletRequest()
        val userLang = localeResolver.resolveLocale(httpServletRequest).toLanguageTag()
        val i18nMessages = i18nMessageRepository.getMessagedFor(i18nScope, userLang)
        model.addAttribute("i18nMessages", objectMapper.writeValueAsString(i18nMessages.messages))
        model.addAttribute("errors", objectMapper.writeValueAsString(i18nMessages.error))
        model.addAttribute("hasServerSideErrors", hasBadLoginFrom(httpServletRequest))
    }


    companion object {

        fun hasBadLoginFrom(httpServletRequest: HttpServletRequest) =
            !Optional.ofNullable(httpServletRequest.session.getAttribute("SPRING_SECURITY_LAST_EXCEPTION")).isEmpty
                    && httpServletRequest.parameterMap.contains("error")

    }


}