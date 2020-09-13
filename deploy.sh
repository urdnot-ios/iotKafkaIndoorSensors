#!/bin/zsh

# did you change the version number?
docker image tag indoorsensors:latest intel-server-03:5000/indoorsensors
docker image push intel-server-03:5000/indoorsensors

# Server side:
# kubectl apply -f /home/appuser/deployments/indoorSensors.yaml
# If needed:
# kubectl delete deployment iot-kafka-indoor-sensors
# For troubleshooting
# kubectl exec --stdin --tty iot-kafka-indoor-sensors -- /bin/bash