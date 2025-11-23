package com.vauthenticator.management.admin

import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.ModelAttribute

@Controller
class AdminApplicationController {
    @ModelAttribute("assetBundle")
    fun assetBundle() = "admin_bundle.js"

    @GetMapping("/secure/admin/index")
    fun view(model: Model) = "template"

}