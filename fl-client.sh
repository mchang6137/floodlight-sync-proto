#!/bin/bash

my_dir=$(dirname $(readlink -f "$0"))
# sleep for 1 second to give the server a chance to come up
sleep 1
# forgot to build a back-off mechanism into distmemstoragesource
exec java -jar $my_dir/target/floodlight.jar -cf $my_dir/src/main/resources/fl-client.properties
