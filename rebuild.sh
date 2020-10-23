#!/bin/bash

docker stop objectdetection
docker rm objectdetection
docker image rm objectdetection
docker build -t objectdetection .
docker run -v /etc/ssl/certs-for-docker:/etc/ssl/certs-for-docker -it --name objectdetection --network host -d objectdetection
docker logs -f objectdetection
