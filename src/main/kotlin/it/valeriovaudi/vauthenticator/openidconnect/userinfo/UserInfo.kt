package it.valeriovaudi.vauthenticator.openidconnect.userinfo

data class UserInfo(var sub: String = "",var userName: String = "", var authorities: List<String> = listOf())