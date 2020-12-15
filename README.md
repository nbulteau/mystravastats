# My Strava Stats

A tool to calculates and displays many statistics about Strava activities.

This tool scans through activities and looks for the best effort for distance or time span during the examined year. For
example, it finds for the fastest consecutive 1 km, 5 km, 10 km you've run, or the fastest 2 hours, 3 hours in ride
activities.

Eddington number is the largest number, E, such that you have ridden at least E km on at least E days. MyStravaStats
calculate Eddington number for rides, runs and hikes.
https://en.wikipedia.org/wiki/Arthur_Eddington#Eddington_number_for_cycling

Best Cooper (12 min) : In the original form, the point of the test is to run as far as possible within 12 minutes. MyStravaStats look for with a 'sliding window' the best effort for the given time (12 minutes) on running activities. 
https://fr.wikipedia.org/wiki/Test_de_Cooper

Best vVO2max (6 min) : This is the smallest speed that requires VO2 max in an accelerated speed test. MyStravaStats look for with a 'sliding window' the best effort for the given time (6 minutes) on running activities.
https://en.wikipedia.org/wiki/VVO2max 

## Global Statistics

| Global Statistics ||    
|---|---|
| Nb activities | Total of all commute activities.
| Nb actives days | Number of active days for all activities.
| Max streak | Max streak of activities for consecutive days.
| Most active month. | The most active month of the year.

## Rides (commute)

| Rides (commute) ||    
|---|--- 
| Nb activities | Total of all commute rides.
| Nb actives days | Number of active days for all commute rides.
| Max streak | Max streak of commute rides for consecutive days.
| Total distance | Total elevation accumulated on all commute rides.
| Total elevation | Total elevation accumulated on all commute rides.
| Max distance | Max distance calculated by Strava for commute rides.
| Max elevation | Max elevation calculated by Strava for commute rides.
| Max moving time | Max moving time for commute rides. Moving time, is a measure of how long you were active. Strava attempt to calculate this based on the GPS locations, distance, and speed of your activity.
| Most active month | The most active month of the year for commute rides.
| Eddington number | The Eddington number in the context of cycling is defined as the maximum number E such that the cyclist has cycled E km on E days.

## Rides (sport)

| Rides (sport) || 
| --- | --- 
| Nb activities | Total of all bike rides.
| Nb actives days | Number of active days for all bike rides.| 
| Max streak | Max streak of bike rides for a consecutive days. | 
| Total distance | Total elevation accumulated on all bike rides. | 
| Total elevation | Total elevation accumulated on all bike rides. | 
| Max distance | Max distance calculated by Strava for bike rides.| 
| Max elevation | Max elevation calculated by Strava for bike rides.| 
| Max moving time | Max moving time for bike rides. Moving time, is a measure of how long you were active. Strava attempt to calculate this based on the GPS locations, distance, and speed of your activity.|
| Most active month | The most active month of the year for bike rides. | 
| Eddington number | The Eddington number in the context of cycling is defined as the maximum number E such that the cyclist has cycled E km on E days.|
| Max speed | Max speed calculated by Strava for bike rides.| 
| Max moving time | Max moving time calculated by Strava for bike rides| 
| Best 250 m | Sliding window best effort for a given distance.| 
| Best 500 m | Sliding window best effort for a given distance.| 
| Best 1000 m | Sliding window best effort for a given distance.| 
| Best 5 km | Sliding window best effort for a given distance.| 
| Best 10 km | Sliding window best effort for a given distance.| 
| Best 20 km | Sliding window best effort for a given distance.|  
| Best 50 km | Sliding window best effort for a given distance.|  
| Best 100 km | Sliding window best effort for a given distance.|  
| Best 30 min | Sliding window best effort for a given time.| 
| Best 1 h | Sliding window best effort for a given time.| 
| Best 2 h | Sliding window best effort for a given time.|  
| Best 3 h | Sliding window best effort for a given time.| 
| Best 4 h | Sliding window best effort for a given time.|  
| Best 5 h | Sliding window best effort for a given time.|  
| Max slope for 250 m | Sliding window max slope for a given distance.| 
| Max slope for 500 m | Sliding window max slope for a given distance.| 
| Max slope for 1000 m | Sliding window max slope for a given distance.| 
| Max slope for 5 km | Sliding window max slope for a given distance.| 
| Max slope for 10 km | Sliding window max slope for a given distance.|
| Max slope for 20 km | Sliding window max slope for a given distance.|

## Runs

| Runs || 
|---|---| 
| Nb activities | Total of all bike rides.
| Nb actives days | Number of active days for all running.
| Max streak | Max streak of bike rides for a running days.
| Total distance | Total elevation accumulated on all running.
| Total elevation | Total elevation accumulated on all running.
| Max distance | Max distance calculated by Strava for running.
| Max elevation | Max elevation calculated by Strava for running.
| Max moving time | Max moving time for running. Moving time, is a measure of how long you were active. Strava attempt to calculate this based on the GPS locations, distance, and speed of your activity.|
| Most active month | The most active month of the year for running.
| Eddington number | The Eddington number in the context of running is defined as the maximum number E such that the runner has run E km on E days.|
| Best Cooper (12 min) | best effort for the given time (12 minutes) on running activities
| Best vVO2max (6 min) | best effort for the given time (6 minutes) on running activities
| Best 200 m | Sliding window best effort for a given distance.
| Best 400 m | Sliding window best effort for a given distance.
| Best 1000 m | Sliding window best effort for a given distance.
| Best 10000 m | Sliding window best effort for a given distance.
| Best half Marathon | Sliding window best effort for a given distance.
| Best Marathon | Sliding window best effort for a given distance.
| Best 1 h | Sliding window best effort for a given time.
| Best 2 h | Sliding window best effort for a given time.
| Best 3 h | Sliding window best effort for a given time.
| Best 4 h | Sliding window best effort for a given time.
| Best 5 h | Sliding window best effort for a given time.
| Best 6 h | Sliding window best effort for a given time.

## Hikes

| Hikes ||
|---|---| 
| Nb activities | Total of all hikes.
| Nb actives days | Number of active days for all hikes.
| Max streak | Max streak of hikes for consecutive days.
| Total distance | Total elevation accumulated on all hikes.
| Total elevation | Total elevation accumulated on all hikes.
| Max distance | Max distance calculated by Strava for hikes.
| Max elevation | Max elevation calculated by Strava for hikes.
| Max moving time | Max moving time for hikes. Moving time, is a measure of how long you were active. Strava attempt to calculate this based on the GPS locations, distance, and speed of your activity.
| Most active month | The most active month of the year for hikes.
| Eddington number | The Eddington number in the context of cycling is defined as the maximum number E such that the cyclist has cycled E km on E days.
| Max distance in a day | Max walked distance in a day for hikes.
| Max elevation in a day | Max elevation in a day for hikes.

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

Build the jar :
```
./gradlew jar
```

*Mandatory parameters*

* -clientId: your application’s ID. You can your find your client id on this page : https://www.strava.com/settings/api
  with :
* -code: the authorization code you get above. This code can be used only once.
* -clientSecret: your client secret. You can your find client secret on this page : https://www.strava.com/settings/api
  or
* -accessToken: Your access token is print when you use -code.

*Optional parameters*

* -year: the year you request (default value is the current year).
* -csv : to export all activities in a CSV file.
* -filter: to filter exported activities on a specific distance in meters. For example : -csv -filter 10000 will display
  all the activities around 10000 m (+/- 5 %)

Activities are download in a local directory, in that way only new and missing ones are downloaded from Strava. For
people with a huge amount of long activities, I recommend to increase memory for example : -Xmx2048m (Set the maximum
memory size to 2048 megabytes).

```
 java -Xmx2048m -jar ./build/libs/mystravastats.jar -clientId [clientId] -clientSecret [clientSecret] -code d4ebd5ee7f512523d49fcb66d6eda207e46fcb8c
```

```
 java -Xmx2048m -jar ./build/libs/mystravastats.jar -clientId [clientId] -accessToken [accessToken]
```

Some examples :

You can use locally download activities (no -code and no -accessToken) :

```
 java -Xmx2048m -jar ./build/libs/mystravastats.jar -clientId [clientId] -year 2019
```

Export activities in a CSV file using locally download activities (current year) :

```
 java -Xmx2048m -jar ./build/libs/mystravastats.jar -clientId [clientId] -csv 
```

Display export activities in a CSV file with a filter using locally download activities (current year) :

```
 java -Xmx2048m -jar ./build/libs/mystravastats.jar -clientId [clientId] -csv -filter 10000
```