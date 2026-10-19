# MFA sequence diagrams

The MFA use cases from the HTTP entry point down to the stores. The endpoints are described in [mfa.md](mfa.md), the vocabulary (MFA Device, Enrolled, Associated, Default Device, Revoke) in [CONTEXT.md](../CONTEXT.md).

Every adapter appears only as the port it implements (`«interface»`): the JDBC and DynamoDB repositories, the KMS and plain-Java key components, and the email and SMS senders are interchangeable behind them (see [profiles.md](profiles.md)). Domain classes appear by name. Stores are marked `(database)`.

The diagrams are Mermaid, so GitHub renders them in place.

## 1. Enrol an MFA Device

`POST /api/mfa/enrollment`: registers a channel as an enrolled device, sends it a code and returns the ticket to associate it with.

```mermaid
sequenceDiagram
    actor Client
    participant EP as MfaEnrolmentAssociationEndPoint
    participant PV as PermissionValidator
    participant Enrollment as MfaMethodsEnrollment
    participant AccountRepo as «interface»<br/>AccountRepository
    participant MfaRepo as «interface»<br/>MfaAccountMethodsRepository
    participant KeyRepository
    participant KeyGenerator as «interface»<br/>KeyGenerator
    participant KeyStorage as «interface»<br/>KeyStorage
    participant Sender as MfaChallengeSender
    participant TicketCreator
    participant TicketRepo as «interface»<br/>TicketRepository
    participant AccountDB as Account store<br/>(database)
    participant MfaDB as MFA device store<br/>(database)
    participant KeyDB as Key store<br/>(database)
    participant TicketDB as Ticket store<br/>(database)
    Client->>EP: POST /api/mfa/enrollment<br/>{mfaMethod, mfaChannel}<br/>Bearer JWT
    EP->>PV: validate(authentication, mfa:enrollment)
    Note right of PV: JWT only (ADR 0007):<br/>no JWT → 401, missing scope → 403
    EP->>Enrollment: enroll(userName, mfaMethod, mfaChannel, clientAppId)
    Enrollment->>AccountRepo: accountFor(userName)
    AccountRepo->>AccountDB: read account
    Enrollment->>MfaRepo: findBy(userName, mfaMethod, mfaChannel)
    MfaRepo->>MfaDB: read device
    opt channel not registered yet
        Enrollment->>MfaRepo: save(userName, mfaMethod, mfaChannel, associated = false)
        MfaRepo->>KeyRepository: createKeyFrom(masterKid, SYMMETRIC, MFA)
        KeyRepository->>KeyGenerator: dataKeyFor(masterKid)
        KeyRepository->>KeyStorage: store(masterKid, kid, dataKey, SYMMETRIC, MFA)
        KeyStorage->>KeyDB: write key
        MfaRepo->>MfaDB: write device (new mfaDeviceId, key id)
    end
    Enrollment->>Sender: sendMfaChallengeFor(userName, mfaDeviceId)
    Note right of Sender: see 6. Send a challenge
    Enrollment->>TicketCreator: createTicketFor(account, clientAppId,<br/>mfa context: mfaDeviceId, mfaMethod, mfaChannel)
    TicketCreator->>TicketRepo: store(ticket)
    TicketRepo->>TicketDB: write ticket
    EP-->>Client: 201 Created {ticket}
```

## 2. Associate an MFA Device

`POST /api/mfa/associate`: confirms the code sent to an enrolled device and makes it associated. The email-verification link follows the same path without a code (self-association) and always sets the device as default.

```mermaid
sequenceDiagram
    actor Client
    participant EP as MfaEnrolmentAssociationEndPoint
    participant PV as PermissionValidator
    participant Association as MfaMethodsEnrollmentAssociation
    participant TicketRepo as «interface»<br/>TicketRepository
    participant MfaRepo as «interface»<br/>MfaAccountMethodsRepository
    participant Verifier as OtpMfaVerifier
    participant AccountRepo as «interface»<br/>AccountRepository
    participant OtpMfa as «interface»<br/>OtpMfa
    participant TicketDB as Ticket store<br/>(database)
    participant AccountDB as Account store<br/>(database)
    participant MfaDB as MFA device store<br/>(database)
    Client->>EP: POST /api/mfa/associate<br/>{ticket, code}<br/>Bearer JWT
    EP->>PV: validate(authentication, mfa:enrollment)
    EP->>Association: associate(ticket, code)
    Association->>TicketRepo: loadFor(ticketId)
    TicketRepo->>TicketDB: read ticket
    alt ticket expired or unknown
        Association-->>EP: InvalidTicketException
    end
    Association->>MfaRepo: findBy(userName, mfaMethod, mfaChannel) from the ticket
    MfaRepo->>MfaDB: read device
    alt device gone, or not the device the ticket names
        Association->>TicketRepo: delete(ticketId)
        Association-->>EP: InvalidTicketException
    else device already associated
        Association->>TicketRepo: delete(ticketId)
        Association-->>EP: InvalidTicketException(ALREADY_ASSOCIATED_MFA)
    end
    Association->>Verifier: verifyMfaChallengeToBeAssociatedFor(userName, mfaDeviceId, code)
    Verifier->>MfaRepo: findBy(mfaDeviceId)
    Note right of Verifier: missing or another account's device<br/>→ MfaDeviceNotFoundException
    Verifier->>AccountRepo: accountFor(userName)
    AccountRepo->>AccountDB: read account
    Verifier->>OtpMfa: verify(account, mfaMethod, mfaChannel, code)
    Note right of OtpMfa: wrong code → MfaException,<br/>the ticket is kept, so the code can be retried<br/>(secret lookup: see 6. Send a challenge)
    Association->>MfaRepo: save(userName, mfaMethod, mfaChannel, associated = true)
    MfaRepo->>MfaDB: write device (same id and key)
    Association->>MfaRepo: getDefaultDevice(userName)
    opt account has no default device
        Association->>MfaRepo: setAsDefault(userName, mfaDeviceId)
        MfaRepo->>MfaDB: write default
    end
    Association->>TicketRepo: delete(ticketId)
    TicketRepo->>TicketDB: delete ticket
    EP-->>Client: 204 No Content
```

## 3. List the MFA Devices

`GET /api/mfa/enrollment`: also called by the login MFA page through the browser session.

```mermaid
sequenceDiagram
    actor Client as Client or login MFA page
    participant EP as MfaEnrolmentAssociationEndPoint
    participant PV as PermissionValidator
    participant ClientAppRepo as «interface»<br/>ClientApplicationRepository
    participant Enrollment as MfaMethodsEnrollment
    participant MfaRepo as «interface»<br/>MfaAccountMethodsRepository
    participant Masker as SensitiveDataMaskerResolver
    participant ClientAppDB as Client application store<br/>(database)
    participant MfaDB as MFA device store<br/>(database)
    Client->>EP: GET /api/mfa/enrollment<br/>Bearer JWT or browser session
    EP->>PV: validate(authentication, session, mfa:enrollment)
    alt bearer JWT
        PV->>PV: check the token scopes
    else browser session
        PV->>ClientAppRepo: findOne(session client app id)
        ClientAppRepo->>ClientAppDB: read client application
        Note right of PV: no resolvable client app → 403 (ADR 0008)
    end
    EP->>Enrollment: getEnrollmentsFor(userName, masked = true)
    Enrollment->>MfaRepo: getDefaultDevice(userName)
    MfaRepo->>MfaDB: read default
    alt no default device
        EP-->>Client: 200 OK []
    end
    Enrollment->>MfaRepo: findAll(userName)
    MfaRepo->>MfaDB: read the account's devices
    Enrollment->>Masker: mask userName and channel per mfaMethod
    EP-->>Client: 200 OK [devices, default flagged]
```

## 4. Set the Default Device

`PUT /api/mfa/device`.

```mermaid
sequenceDiagram
    actor Client
    participant EP as MfaEnrolmentAssociationEndPoint
    participant PV as PermissionValidator
    participant MfaDevices
    participant MfaRepo as «interface»<br/>MfaAccountMethodsRepository
    participant MfaDB as MFA device store<br/>(database)
    Client->>EP: PUT /api/mfa/device {mfaDeviceId}<br/>Bearer JWT
    EP->>PV: validate(authentication, mfa:enrollment)
    EP->>MfaDevices: setAsDefault(userName, mfaDeviceId)
    MfaDevices->>MfaRepo: findBy(mfaDeviceId)
    MfaRepo->>MfaDB: read device
    alt missing, another account's, or only enrolled
        MfaDevices-->>EP: MfaDeviceNotFoundException
        EP-->>Client: 404 Not Found
    end
    MfaDevices->>MfaRepo: setAsDefault(userName, mfaDeviceId)
    MfaRepo->>MfaDB: write default
    EP-->>Client: 204 No Content
```

## 5. Revoke an MFA Device

`DELETE /api/mfa/enrollment/{mfaDeviceId}`: the rules are in ADR 0009. The repository runs the domain's check between reading and deleting, so an adapter that can (JDBC) holds the account's devices locked for the whole step.

```mermaid
sequenceDiagram
    actor Client
    participant EP as MfaEnrolmentAssociationEndPoint
    participant PV as PermissionValidator
    participant MfaDevices
    participant MfaRepo as «interface»<br/>MfaAccountMethodsRepository
    participant KeyRepository
    participant KeyStorage as «interface»<br/>KeyStorage
    participant Events as VAuthenticatorEventsDispatcher
    participant MfaDB as MFA device store<br/>(database)
    participant KeyDB as Key store<br/>(database)
    Client->>EP: DELETE /api/mfa/enrollment/{mfaDeviceId}<br/>Bearer JWT
    EP->>PV: validate(authentication, mfa:enrollment)
    EP->>MfaDevices: revoke(userName, mfaDeviceId, clientAppId)
    MfaDevices->>MfaRepo: revoke(userName, mfaDeviceId, check)
    MfaRepo->>MfaDB: read the account's devices and default<br/>(JDBC: SELECT … FOR UPDATE)
    alt no device of the account with this id
        MfaRepo-->>MfaDevices: MfaDeviceNotFoundException
        EP-->>Client: 404 Not Found
    end
    MfaRepo->>MfaDevices: check(candidate: device, accountDevices, defaultDevice)
    alt the account's last associated device
        MfaDevices-->>EP: MfaDeviceRevocationException(LAST_ASSOCIATED_DEVICE)
        EP-->>Client: 409 Conflict {reason}
    else the Default Device
        MfaDevices-->>EP: MfaDeviceRevocationException(DEFAULT_DEVICE)
        EP-->>Client: 409 Conflict {reason}
    end
    MfaRepo->>MfaDB: delete device
    MfaRepo-->>MfaDevices: revoked device
    MfaDevices->>KeyRepository: deleteKeyFor(device key, MFA)
    KeyRepository->>KeyStorage: justDeleteKey(kid, MFA)
    KeyStorage->>KeyDB: delete key
    Note right of MfaDevices: a failed key delete is logged,<br/>the device is already gone
    MfaDevices->>Events: dispatch(MfaDeviceRevokedEvent(mfaDeviceId, mfaMethod))
    EP-->>Client: 204 No Content
```

## 6. Send a challenge

`PUT /api/mfa/challenge` (API, optional `mfa-device-id`) and `GET /mfa-challenge/send` (login MFA page, Default Device). Enrolment (1) uses the same sender for the new device.

```mermaid
sequenceDiagram
    actor Client as Client or login MFA page
    participant EP as MfaChallengeEndPoint /<br/>MfaController
    participant PV as PermissionValidator
    participant Sender as MfaChallengeSender
    participant MfaRepo as «interface»<br/>MfaAccountMethodsRepository
    participant AccountRepo as «interface»<br/>AccountRepository
    participant OtpMfa as «interface»<br/>OtpMfa
    participant KeyRepository
    participant KeyStorage as «interface»<br/>KeyStorage
    participant KeyDecrypter as «interface»<br/>KeyDecrypter
    participant Mail as «interface»<br/>EMailSenderService
    participant Sms as «interface»<br/>SmsSenderService
    participant AccountDB as Account store<br/>(database)
    participant MfaDB as MFA device store<br/>(database)
    participant KeyDB as Key store<br/>(database)
    Client->>EP: PUT /api/mfa/challenge[?mfa-device-id]<br/>or GET /mfa-challenge/send
    opt PUT /api/mfa/challenge
        EP->>PV: validate(authentication, session, mfa:always)
    end
    alt mfa-device-id given
        EP->>Sender: sendMfaChallengeFor(userName, mfaDeviceId)
        Sender->>MfaRepo: findBy(mfaDeviceId)
        MfaRepo->>MfaDB: read device
    else Default Device
        EP->>Sender: sendMfaChallengeFor(userName)
        Sender->>MfaRepo: getDefaultDevice(userName)
        MfaRepo->>MfaDB: read default
        Note right of Sender: no default device → nothing is sent
        Sender->>MfaRepo: findBy(defaultDeviceId)
    end
    Sender->>AccountRepo: accountFor(userName)
    AccountRepo->>AccountDB: read account
    Sender->>OtpMfa: generateSecretKeyFor(account, mfaMethod, mfaChannel)
    OtpMfa->>MfaRepo: findBy(account email, mfaMethod, mfaChannel)
    Note right of OtpMfa: no such device on the account → MfaException
    OtpMfa->>KeyRepository: keyFor(device key, MFA)
    KeyRepository->>KeyStorage: findOne(kid, MFA)
    KeyStorage->>KeyDB: read key
    OtpMfa->>KeyDecrypter: decryptKey(encrypted data key)
    Sender->>OtpMfa: getTOTPCode(secret)
    alt EMAIL_MFA_METHOD
        Sender->>Mail: sendFor(account, {email, mfaCode})
    else SMS_MFA_METHOD
        Sender->>Sms: sendFor(account, {phone, mfaCode})
    end
    EP-->>Client: 200 OK, or redirect to /mfa-challenge
```

## 7. Verify the second factor at login

`POST /mfa-challenge`: the login MFA page submits the code, optionally with the device it was sent to. Verification fails closed when the device cannot be resolved (VA-SEC-19). Passing it makes MFA a passed step for the rest of the session, then resumes the authorization request that asked for it; the login workflow is checked again on every authorization request (ADR 0010).

```mermaid
sequenceDiagram
    actor Browser as Login MFA page
    participant Controller as MfaController
    participant Verifier as OtpMfaVerifier
    participant MfaRepo as «interface»<br/>MfaAccountMethodsRepository
    participant AccountRepo as «interface»<br/>AccountRepository
    participant OtpMfa as «interface»<br/>OtpMfa
    participant Publisher as «interface»<br/>ApplicationEventPublisher
    participant Handlers as «interface»<br/>AuthenticationSuccessHandler /<br/>AuthenticationFailureHandler
    participant AccountDB as Account store<br/>(database)
    participant MfaDB as MFA device store<br/>(database)
    Browser->>Controller: POST /mfa-challenge<br/>mfa-code[, mfa-device-id]<br/>browser session
    alt mfa-device-id given
        Controller->>Verifier: verifyAssociatedMfaChallengeFor(userName, mfaDeviceId, code)
    else Default Device
        Controller->>Verifier: verifyAssociatedMfaChallengeFor(userName, code)
        Verifier->>MfaRepo: getDefaultDevice(userName)
        MfaRepo->>MfaDB: read default
        Note right of Verifier: no default device → MfaDeviceNotFoundException
    end
    Verifier->>MfaRepo: findBy(mfaDeviceId)
    MfaRepo->>MfaDB: read device
    Note right of Verifier: missing or another account's device → MfaDeviceNotFoundException<br/>only enrolled → UnAssociatedMfaVerificationException
    Verifier->>AccountRepo: accountFor(userName)
    AccountRepo->>AccountDB: read account
    Verifier->>OtpMfa: verify(account, mfaMethod, mfaChannel, code)
    Note right of OtpMfa: secret lookup: see 6. Send a challenge,<br/>wrong code → MfaException
    alt verified
        Controller->>Publisher: publishEvent(MfaSuccessEvent)
        Controller->>Handlers: onAuthenticationSuccess → MFA step passed in the session,<br/>resume the saved authorization request
    else any exception
        Controller->>Publisher: publishEvent(MfaFailureEvent)
        Controller->>Handlers: onAuthenticationFailure
    end
```
