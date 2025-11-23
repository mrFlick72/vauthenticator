package com.vauthenticator.server.config

import org.springframework.boot.autoconfigure.EnableAutoConfiguration
import org.springframework.boot.jdbc.autoconfigure.DataSourceAutoConfiguration
import org.springframework.boot.jdbc.autoconfigure.DataSourceTransactionManagerAutoConfiguration
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile


@Configuration
@EnableAutoConfiguration(exclude = [DataSourceAutoConfiguration::class, DataSourceTransactionManagerAutoConfiguration::class])
@Profile("!database")
class ExcludeDatabaseConfig

