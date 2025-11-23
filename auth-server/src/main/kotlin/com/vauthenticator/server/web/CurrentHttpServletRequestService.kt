package com.vauthenticator.server.web

import jakarta.servlet.http.HttpServletRequest
import org.springframework.stereotype.Service
import org.springframework.web.context.request.RequestContextHolder
import org.springframework.web.context.request.ServletRequestAttributes

interface CurrentHttpServletRequestService {
    fun getServletRequest(): HttpServletRequest
}

class SpringCurrentHttpServletRequestService : CurrentHttpServletRequestService {
    override fun getServletRequest(): HttpServletRequest =
        (RequestContextHolder.getRequestAttributes() as ServletRequestAttributes).request
}