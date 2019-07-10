#!/bin/bash

export env=$1

if [[ ${env} = "dev" ]] || [[ ${env} = "staging" ]] || [[ ${env} = "prod" ]]; then
    echo "copy jar from target to S3 location"
else
    echo "Invalid Environment"
    exit 1
fi

