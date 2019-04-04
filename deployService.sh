#!/usr/bin/env bash
# cleanup old versions
sudo docker container stop indoor_sensors
sudo docker prune
sudo docker rmi iotkafkaairlighttemp

# start the new version
sudo docker load -i /home/appuser/iotkafkaairlighttemp.tar
sudo docker run --name indoor_sensors -m 500m --network=host -e TOPIC_START=latest -d iotkafkaairlighttemp:latest