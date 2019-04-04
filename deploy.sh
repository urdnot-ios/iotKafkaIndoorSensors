#!/usr/bin/env bash

# did you change the version number?

sbt clean
sbt assembly
sbt docker:publishLocal
docker save -o iotkafkaairlighttemp.tar iotkafkaairlighttemp:latest
scp deployService.sh appuser@intel-server-02:/home/appuser/
ssh -t appuser@intel-server-02 chmod u+x /home/appuser/deployService.sh
scp iotkafkaairlighttemp.tar appuser@intel-server-02:/home/appuser/
