# crypto-notifications
## Task definition
> Create application to notify when Bitcoin price has changed above some limit.
> 
> Use one of bitcoin exchange libraties like https://github.com/knowm/XChange to fetch market data.
> 
> User can set limits to be notified when price goes above X amount.
> 
> This task should be split on Client and Server components. 
> But it does not mean you need to create nice Human user interface.
> It is fine to create command line client for this task as well as it is fine to have it in Web UI without any styling.
> 
> Interface between client and server:
> - HTTP PUT /alert?pair=BTC-USD&limit=500 - sets alert to happen when price goes above the limit
> - HTTP DELETE /alert?pair=BTC-USD&limit=500 - remove alert
> - WEB SOCKET /alerts - receive stream of events (when price goes above the limit), including currenca pair, limit, and timestamp in message
> 
> Yes, it is strange to do WebSockets from CLI but we want to focus on checking distributed programming skills rather than doing UI.
> 
> Solution should have enough unit tests to judge how you usually deal with it and at least one integration test.
> 
> Integration test should simulate access to your bitcoin exchange (XChange).. It should be possible to run all tests without external connectivity.
> 
> In case of any uncertainty please make a decision by yourself and explain rationale to us.
 
## Solution
1. Clone source of this repository
2. Build
3. Run
### Building with maven
Build with maven:
`mvn clean install`

Build in offline mode:
`mvn clean install -Poffline`

### Running the application
Run the application:
`mvn -pl :crypto-notifications-rest spring-boot:run`

Then, open in your web browser the following address: [http://localhost:8080/index.html](http://localhost:8080/index.html)

### Explanations
- Alert are processed every second. Price for given currency is polled once every run. I'm not familiar with xchange API to say whether it's possible to set up push notification from the server to avoid the polling.
- Alert data is not stored anywhere except the memory.
- Web socket clients do not get alert notifications that are generated when whey are not connected. 
- After adding an alert, a notification is generated only once, when price goes above the specified limit. It can be generated again after the price goes below limit and again above. 
- Notification can be generated again also when alert is removed and added again.
- Only one alert for given pair and limit can be added.
- Offline mode is implemented by mocking the xchange API. Static price is returned with every call.
