#!/bin/sh

# JAVA_HOME variable must be set!!!

export JAVA_HOME=/usr/java/jdk1.6.0_10/

echo "Starting grabber"

$JAVA_HOME/bin/java -Xmx256M -jar lib/JavaXmlTvCore-1.2.7.jar

# Uncoment this line if you want to run mythfill database after
#mythfilldatabase --file 1 0 out.xml

echo "All done!"

