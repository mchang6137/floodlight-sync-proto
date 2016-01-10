#!/bin/bash

#Get the current directory
my_dir=$(dirname $(readlink -f "$0"))

exec > $my_dir/bashlog.log 2>&1
set -x

echo 'testingtesting'

#Break the Master Controller
pkill -9 -f 'java -jar /home/chachang/floodlight-sync-proto/target/floodlight.jar -cf /home/chachang/floodlight-sync-proto/src/main/resources/fl-server.properties'

#Break the link
ip link del S2-eth3

