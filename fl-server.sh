#!/bin/bash

my_dir=$(dirname $(readlink -f "$0"))
exec java -jar $my_dir/target/floodlight.jar -cf $my_dir/src/main/resources/fl-server.properties
