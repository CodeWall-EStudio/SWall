#!/bin/sh

args=("$@")

NODEJS="node-v0.10.32"
MONGODB="mongodb-linux-x86_64-2.6.4"
REDIS="redis-2.8.17"

if [ $1 == "-h" ] || [ $1 == "--help" ]; then
    echo "Usage: ./setup.sh <Arguments>"
    echo "Arguments:"
    echo "-n : don't install nodejs"
    echo "-m : don't install mongodb"
    echo "-r : don't install redis"
    exit
fi

#TODO how to check linux distribution name?

#get your machine ready for work
#sudo apt-get update
#sudo apt-get -y install build-essential
yum install -y gcc-c++ make
yum install -y wget

#install nodejs
if [[ "${args[*]}" != *"-n"* ]]; then
    echo "[NODEJS] installing ..."
    wget http://nodejs.org/dist/v0.10.32/${NODEJS}.tar.gz
    tar xzf ${NODEJS}.tar.gz
    cd ${NODEJS}
    ./configure
    make
    make install
    cd ..

    npm install -g forever
fi

#install mongodb
if [[ "${args[*]}" != *"-m"* ]]; then
    echo "[MONGODB] installing ..."
    wget https://fastdl.mongodb.org/linux/${MONGODB}.tgz
    tar -zxvf ${MONGODB}.tgz
    mkdir -p mongodb
    cp -R -n ${MONGODB}/ mongodb
    #export PATH=<mongodb-install-directory>/bin:$PATH
    mkdir -p /data/db
fi

#install redis and start as daemon
if [[ "${args[*]}" != *"-r"* ]]; then
    echo "[REDIS] installing ..."
    wget http://download.redis.io/releases/${REDIS}.tar.gz
    tar xzf ${REDIS}.tar.gz
    cd ${REDIS}
    make
    sed -i.bak s/"daemonize no"/"daemonize yes"/ redis.conf
    ./src/redis-server redis.conf
    cd ..
fi

#TODO config redis as daemon
#TODO test node and mongodb and redis