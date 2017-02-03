# Seed-REST-Server-JEE7
This is a sample REST service based on JEE7.

## Deployment
The sample has three very simple concepts - "account", "customer" and "location" 
- which is assembled into one deployable war file.
Databases are separate to illustrate three different services. 
Normally they would be deployed as three different deployable units.

Useful Commands
---------------

To build the whole project:

    mvn package

To build project including running integration tests:

    mvn verify

To run in Wildfly:

    mvn -N -Pwildfly cargo:run

To run in WebLogic:

    mvn -N -Pweblogic cargo:run

To redeploy in Wildfly

    mvn -N -Pwildfly cargo:redeploy

To redeploy in WebLogic

    mvn -N -Pweblogic cargo:redeploy

Admin console
-------------
The WebLogic console may be accessed from http://localhost:7001/console using username "weblogic" and password "weblogic1".
The JBoss console may be accessed from http://localhost:9990 using username "advisor1" and password "passw0rd".

Inspiration
-------------
The code here is inspired heavily by work done by Nykredit on REST services. 