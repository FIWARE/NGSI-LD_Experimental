#!/bin/bash

# check branch
if [ "${TRAVIS_BRANCH}" != "master" ]; then exit 0; fi

# check PR
if [ "${TRAVIS_PULL_REQUEST}" != "false" ]; then exit 0; fi

# login
echo ${DOCKER_PASSWORD} | docker login -u ${DOCKER_USERNAME} --password-stdin

# push
docker push ${DOCKER_ID}/${DOCKER_REPO}:${TAG}
