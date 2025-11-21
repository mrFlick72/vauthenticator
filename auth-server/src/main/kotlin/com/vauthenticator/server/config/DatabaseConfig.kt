package com.vauthenticator.server.config

import org.springframework.boot.autoconfigure.EnableAutoConfiguration
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration
import org.springframework.boot.autoconfigure.jdbc.DataSourceTransactionManagerAutoConfiguration
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile


@Configuration
@EnableAutoConfiguration(exclude = [DataSourceAutoConfiguration::class, DataSourceTransactionManagerAutoConfiguration::class, HibernateJpaAutoConfiguration::class])
@Profile("!database")
class ExcludeDatabaseConfig

