#!/bin/bash
host=$1
role=$2

curl -vv -i -H "Accept: application/json" -H 'Content-Type: application/json' -X POST -d '{"role":"'$role'"}' http://$host/wm/core/role/json
