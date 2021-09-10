# odutils-web
Orbit Determination Utilities Web Services

## cart2tle
Convert a Cartesian position and velocity vector into a TLE.  Three different TLEs are returned: SGP4 based on open source, USSF SGP4, USSF SGP4-XP.

## tle2cart
Specify line1, line2, start date, end date, and time step.  Method will return a set of Cartesian position and velocity vectors.

## sgp4od
Specify a set of Cartesian position vectors and receive a job id.  Call sgp4odres to see if the TLE is ready.

## sgp4xpod
Specify a set of Cartesian position vectors and receive a job id.  Call sgp4xpodres to see if the SGP4-XP TLE is ready.

## orekitod
Coming soon.

# Web Server
The web server used here is [JLHTTP](https://www.freeutils.net/source/jlhttp/) - the Java Lightweight HTTP Server.  It is very very lightweight.

If you want more features in a Web Server, you can reimplement the handlers in a Servlet for a webapp to be deployed in Tomcat.  Otherwise, you can add a proxy supporting HTTPS and authentication with NGINX or Apache.

# Installation Instructions


# Container Recipes
