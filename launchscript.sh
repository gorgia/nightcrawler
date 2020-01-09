#!/bin/bash


docker network create grid
docker run -d -p 4444:4444 --net grid --name selenium-hub -e GRID_TIMEOUT=3000 -e GRID_BROWSER_TIMEOUT=3000 selenium/hub:3.14.0-arsenic
docker run -d --net grid -e HUB_HOST=selenium-hub -v /dev/shm:/dev/shm selenium/node-chrome:3.14.0-arsenic
docker run -d --net grid -e HUB_HOST=selenium-hub -v /dev/shm:/dev/shm selenium/node-chrome:3.14.0-arsenic
docker run -d --net grid -e HUB_HOST=selenium-hub -v /dev/shm:/dev/shm selenium/node-chrome:3.14.0-arsenic
docker run -d --net grid -e HUB_HOST=selenium-hub -v /dev/shm:/dev/shm selenium/node-chrome:3.14.0-arsenic
docker run -d --net grid -e HUB_HOST=selenium-hub -v /dev/shm:/dev/shm selenium/node-chrome:3.14.0-arsenic
docker run -d --net grid -e HUB_HOST=selenium-hub -v /dev/shm:/dev/shm selenium/node-chrome:3.14.0-arsenic
docker run -d --net grid -e HUB_HOST=selenium-hub -v /dev/shm:/dev/shm selenium/node-chrome:3.14.0-arsenic
docker run -d --net grid -e HUB_HOST=selenium-hub -v /dev/shm:/dev/shm selenium/node-chrome:3.14.0-arsenic

docker run -p 61616:61616 -p 8161:8161 rmohr/activemq:5.15.6
docker run --name nightcrawler-mongo -p 27017:27017 -v /home/andrea/nightcrawler/docker/mongo/db:/data/db -d mongo
docker run --name nightcrawler-neo4j -p7474:7474 -p7687:7687 -v  /home/andrea/nightcrawler/docker/neo4j/data:/data --env NEO4J_AUTH=none -d neo4j:3.4.7
