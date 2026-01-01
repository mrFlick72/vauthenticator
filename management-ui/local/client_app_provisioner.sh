# gain a new access token for M2M interaction
ACCESS_TOKEN=$(curl -v -X POST -H "Content-Type: application/x-www-form-urlencoded" -d client_id=admin -d client_secret=secret -d grant_type=client_credentials -d scope=admin:full-access http://local.api.vauthenticator.com:9090/oauth2/token  | jq -r .access_token)
    
echo $ACCESS_TOKEN


# sign up a new application

curl -v -X PUT -d @client_app.json -H "Content-Type: application/json" -H "Authorization: Bearer $ACCESS_TOKEN"  http://local.api.vauthenticator.com:9090/api/client-applications/local.vauthenticatror.management.ui
