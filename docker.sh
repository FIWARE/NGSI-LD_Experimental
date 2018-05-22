#!/bin/bash

# login
echo ${DOCKER_PASSWORD} | docker login -u ${DOCKER_USERNAME} --password-stdin

# push
docker push ${DOCKER_ID}/${DOCKER_REPO}:${TAG}
