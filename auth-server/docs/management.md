# Management

Custom Actuator Endpoint

## Clean Database Entry with TTL

Actuator Clean Database

*URI:* ```Post /actuator/database-clean-up```

*Request:* Empty request body

*Response Status:* ```204 NoContent```

An expired ticket can already not be redeemed (ADR ```docs/adr/0014-tickets-expire-on-read-with-a-lifetime-per-purpose.md```); this endpoint only removes the rows.

## Clean Database Entry with TTL

Actuator to create a local tenant configuration setup it is the successor of the py script in the local-environment folder 

*URI:* ```Post /actuator/tenant-setup```

*Request:* Empty request body

*Response Status:* ```204 NoContent```
