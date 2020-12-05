# My Strava Stats

A tool to calculates and displays many statistics about your Strava activities.

This tool scans through activities and looks for the best effort for distance or time span during the examined year. For
example, it finds for the fastest consecutive 1 km, 5 km, 10 km you've run, or the fastest 2 hours, 3 hours you ride.
Eddington number etc ..

## Global Statistics

| Global Statistics      |     |    
| ---------------------- | --- |
| Nb activities          |     | 
| Nb actives days        |     | 
| Max streak             |     |
| Most active month.     | The most active month of the year. |

## Rides (commute)

| Rides (commute)        |     |    
| ---------------------- | --- | 
| Nb activities | | 
| Nb actives days | | 
| Max streak | | 
| Total distance | | 
| Total elevation | | 
| Max distance | | 
| Max elevation | | 
| Max moving time | |
| Most active month | | 
| Eddington number | |

## Rides (sport)

| Rides (sport)          |     | 
| ---------------------- | --- | 
| Nb activities | | 
| Nb actives days | | 
| Max streak | | 
| Total distance | | 
| Total elevation | | 
| Max distance | | 
| Max elevation | | 
| Max moving time | | 
| Most active month | | 
| Eddington number | | 
| Max speed | | 
| Max moving time | | 
| Best 250 m | | 
| Best 500 m | | 
| Best 1000 m || 
| Best 5 km | | 
| Best 10 km | | 
| Best 20 km | | 
| Best 50 km | | 
| Best 100 km | | 
| Best 30 min | | 
| Best 1 h | |
| Best 2 h | | 
| Best 3 h | | 
| Best 4 h | | 
| Best 5 h | | 
| Max slope for 250 m | | 
| Max slope for 500 m | | 
| Max slope for 1000 m | | 
| Max slope for 5 km | | 
| Max slope for 10 km | | 
| Max slope for 20 km | |

## Runs

| Runs | | 
| ---------------------- | --- | 
| Nb activities | | 
| Nb actives days | | 
| Max streak | | 
| Total distance | |
| Total elevation | | 
| Max distance | | 
| Max elevation | | 
| Max moving time | | 
| Most active month | | 
| Eddington number | | 
| Best Cooper (12 min)   | | 
| Best vVO2max (6 min)   | | 
| Best 200 m | | 
| Best 400 m | | 
| Best 1000 m | |
| Best 10000 m | | 
| Best half Marathon | | 
| Best Marathon | | 
| Best 1 h | | 
| Best 2 h | | 
| Best 3 h | | 
| Best 4 h | | 
| Best 5 h | | 
| Best 6 h | |

## Hikes

| Hikes |-| 
| ---------------------- | --- | 
| Nb activities | | 
| Nb actives days | | 
| Max streak | | 
| Total distance | |
| Total elevation | | 
| Max distance | | 
| Max elevation | | 
| Max moving time | | 
| Most active month | | 
| Eddington number | | 
| Max distance in a day | | 
| Max elevation in a day | |

## Get authorization code

### Create a request URL for Strava authorization, where the base URL is https://www.strava.com/api/v3/oauth/authorize and parameters are:

* client_id: your application’s ID. You can your find client_id on this page : https://www.strava.com/settings/api
* redirect_uri:    URL to which the user will be redirected with the authorization code.
* response_type: must be 'code'
* scope : 'read', 'read_all', 'profile:read_all', 'profile:write', 'profile:write', 'activity:read', 'activity:
  read_all', 'activity:write'

http://www.strava.com/api/v3/oauth/authorize?client_id=[YOUR_CLIENT_ID]&response_type=code&redirect_uri=http://localhost/exchange_token&approval_prompt=force&scope=read_all,profile:read_all,activity:read_all

### Go to above URL in a browser

Login to Strava then click 'Authorize' and tick the required permissions if needed.
Browser should go to 404 as http://localhost/exchange_token doesn't exist.
Copy the authorization code from URL. 

For example : http://localhost/exchange_token?state=&code=d4ebd5ee7f512523d49fcb66d6eda207e46fcb8c&scope=read_all,profile:read_all,activity:read_all

The authorization code for next step is d4ebd5ee7f512523d49fcb66d6eda207e46fcb8c.

## Launch mystravastats

My Strava Stats needs several parameters : 

*Mandatory parameters*
* -clientId: your application’s ID. You can your find your client id on this page : https://www.strava.com/settings/api
* -clientSecret: your client secret. You can your find client secret on this page : https://www.strava.com/settings/api
with :
* -code: the authorization code you get above. This code can be used only once.
or
* -accessToken: Your access token is print when you use -code.

*Optional parameters*
* -year: the year you request (default value is 2020).
* -file: use locally download activities.
* -displayActivities : to display all activities. 
* -filter: to filter activities on a specific distance in meters. For example : -displayActivities -filter 10000 will display all the activities around 10000 m (+/- 5 %)

Activities are download in a local directory, in that way only new and missing ones are downloaded from Strava.
For people with a huge amount of long activities, I recommend to increase memory for example : -Xmx2048m (Set the maximum memory size to 2048 megabytes).

```
 ./gradlew jar
 java -Xmx2048m -jar ./build/libs/mystravastats.jar -clientId [clientId] -clientSecret [clientSecret] -code d4ebd5ee7f512523d49fcb66d6eda207e46fcb8c
```

You can use locally download activities :

```
 ./gradlew jar
 java -Xmx2048m -jar ./build/libs/mystravastats.jar -file strava-41902-2020/activities-41902-2020-with-stream.json
```

Display activities :

```
 ./gradlew jar
 java -Xmx2048m -jar ./build/libs/mystravastats.jar -file strava-41902-2020/activities-41902-2020-with-stream.json --displayActivities 
```

Display activities with a filter :

```
 ./gradlew jar
 java -Xmx2048m -jar ./build/libs/mystravastats.jar -file strava-41902-2020/activities-41902-2020-with-stream.json --displayActivities -filter 10000
```