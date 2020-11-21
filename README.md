= Strava Stats

== Get a Strava API access token with read permission

=== Get authorization code

==== Create a request URL for Strava authorization, where the base URL is https://www.strava.com/api/v3/oauth/authorize and parameters are:

* client_id: your application’s ID, obtained during registration. You can your find client_id on this page : https://www.strava.com/settings/api
* redirect_uri:	URL to which the user will be redirected with the authorization code. A random but unique one on localhost should be fine.
* response_type: must be 'code'
* scope : 'read', 'read_all', 'profile:read_all', 'profile:write', 'profile:write', 'activity:read', 'activity:read_all', 'activity:write'

http://www.strava.com/api/v3/oauth/authorize?client_id=[YOUR_CLIENT_ID]&response_type=code&redirect_uri=http://localhost/exchange_token&approval_prompt=force&scope=read_all,profile:read_all,activity:read_all

==== Go to above URL in a browser. (HTTP GET)
Login to Strava then click 'Authorize' and tick the required permissions if needed.
Browser should go to 404 as http://localhost/exchange_token doesn't exist.
Copy the authorization code from URL. 

For example : http://localhost/exchange_token?state=&code=d4ebd5ee7f512523d49fcb66d6eda207e46fcb8c&scope=read_all,profile:read_all,activity:read_all
The authorization code for next step is d4ebd5ee7f512523d49fcb66d6eda207e46fcb8c.


....
 ./gradlew jar
 java -jar ./build/libs/stravastats.jar -clientId [clientId] -clientSecret [clientSecret] -code d4ebd5ee7f512523d49fcb66d6eda207e46fcb8c
....
