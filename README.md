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
To run the web server clone or download this repository and the the one for [odutils](https://github.com/aholinch/odutils).  Clone them or unzip the archives into the same directory so that odutils and odutils-web are at the same level.

## Ant
odutils-web and odutils require Ant to compile from the command line or can be imported into an IDE like Eclipse to be compiled.  Instructions for installing Ant can be found [here](https://ant.apache.org/manual/install.html).

## SGP4 Binaries
You need to have the binaries from USSF.  If you have an account, you can download them from [Space-Track.org](https://www.space-track.org/documentation#/sgp4).  As of this writing the available file is Sgp4Prop_small_v8.1.zip.  Download the zip if you agree to the terms in the user agreement, and extract it somewhere on your system.  
From the extracted folders copy the platform folder for your OS to the lib directory in this project.  For Windows, copy \<SGP4 Dir\>\Lib\Win64 to lib\Win64.  For Linux copy \<SGP4 Dir\>/Lib/Linux64 to lib/Linux64. 

# Container Recipes

Regardless of which platform you are running on, the containers target Linux64, so make sure you have the Linux64 binaries for SGP4 copied to lib/Linux64.

## Docker

The docker image runs the web server on port 9000.  To compile the project in the proper manner for docker call "ant docker" from the project home directory.  It will make a dist jar file and copy the necessary files to the containers/docker/target directory.

From the containers/docker directory you can run "docker-compose build" to build the docker image.  You can also run it with "docker-compose run".

The web server should be available from http://localhost:9000.

