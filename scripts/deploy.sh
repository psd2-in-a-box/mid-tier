#!/bin/bash

export CLOUDSDK_COMPUTE_ZONE=europe-west1-b
export CLOUDSDK_CORE_PROJECT=future-finance-157420
export CLOUDSDK_CONTAINER_USE_CLIENT_CERTIFICATE=True
export NAME=mid-tier

gcloud docker -- push $1
gcloud container clusters get-credentials hackathon

# Test if deployed??
kubectl get deployments $NAME
if [ $? -eq 1 ]; then
  kubectl run $NAME --image=$1 --hostport=80 --port=80;
  kubectl expose deployment $NAME --type="LoadBalancer";
else
  kubectl set image deployment/$NAME $NAME=$1
fi
