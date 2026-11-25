package com.vauthenticator.platform.extensions

import java.time.Clock
import java.time.Duration
import java.time.Instant


fun Duration.expirationTimeStampInSecondFromNow(clock: Clock) =
    this.plus(Duration.ofSeconds(Instant.now(clock).epochSecond)).seconds