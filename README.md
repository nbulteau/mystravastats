= My Strava Stats

A tool to calculates and displays many statistics about your Strava activities.

This tool scans through activities and looks for the best effort for distance or time span during the examined year. 
For example, it find for the fastest consecutive 1 km, 5 km, 10 km you've run, or the fastest 2 hours, 3 hours you rided.

== Get authorization code

=== Create a request URL for Strava authorization, where the base URL is https://www.strava.com/api/v3/oauth/authorize and parameters are:

* client_id: your application’s ID. You can your find client_id on this page : https://www.strava.com/settings/api
* redirect_uri:	URL to which the user will be redirected with the authorization code.
* response_type: must be 'code'
* scope : 'read', 'read_all', 'profile:read_all', 'profile:write', 'profile:write', 'activity:read', 'activity:read_all', 'activity:write'

http://www.strava.com/api/v3/oauth/authorize?client_id=[YOUR_CLIENT_ID]&response_type=code&redirect_uri=http://localhost/exchange_token&approval_prompt=force&scope=read_all,profile:read_all,activity:read_all

=== Go to above URL in a browser.

Login to Strava then click 'Authorize' and tick the required permissions if needed.
Browser should go to 404 as http://localhost/exchange_token doesn't exist.
Copy the authorization code from URL. 

For example : http://localhost/exchange_token?state=&code=d4ebd5ee7f512523d49fcb66d6eda207e46fcb8c&scope=read_all,profile:read_all,activity:read_all

The authorization code for next step is d4ebd5ee7f512523d49fcb66d6eda207e46fcb8c.

== Launch mystravastats.

My Strava Stats needs several parameters : 

[Mandatory parameters]
* -clientId: your application’s ID. You can your find your client id on this page : https://www.strava.com/settings/api
* -clientSecret: your client secret. You can your find client secret on this page : https://www.strava.com/settings/api
* -code: the authorization code you get above.

[Optional parameters]
* -year: the year you request (default value is 2020).

For people with a huge amount of long activities, I recommend to increase memory for example : Xmx2048m (Set the maximum memory size to 1024 megabytes).

....
 ./gradlew jar
 java -Xmx2048m -jar ./build/libs/mystravastats.jar -clientId [clientId] -clientSecret [clientSecret] -code d4ebd5ee7f512523d49fcb66d6eda207e46fcb8c
....
