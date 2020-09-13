#!/bin/zsh

# did you change the version number?
sbt clean
sbt assembly
sbt docker:publishLocal
docker image tag iotkafkaindoorsensors:latest intel-server-03:5000/iotkafkaindoorsensors
docker image push intel-server-03:5000/iotkafkaindoorsensors

# Server side:
# kubectl apply -f /home/appuser/deployments/indoorSensors.yaml
# If needed:
# kubectl delete deployment iot-kafka-indoor-sensors
# For troubleshooting
# kubectl exec --stdin --tty iot-kafka-indoor-sensors -- /bin/bash