#!/bin/sh

#get your machine ready for work
sudo apt-get update
sudo apt-get -y install build-essential

#install nodejs
sudo apt-get -y install nodejs nodejs-dev npm

#install mongodb
sudo apt-key adv --keyserver hkp://keyserver.ubuntu.com:80 --recv 7F0CEB10
echo 'deb http://downloads-distro.mongodb.org/repo/ubuntu-upstart dist 10gen' | sudo tee /etc/apt/sources.list.d/mongodb.list
sudo apt-get update
sudo apt-get install -y mongodb-org

#install redis
wget http://download.redis.io/releases/redis-2.8.17.tar.gz
tar xzf redis-2.8.17.tar.gz
cd redis-2.8.17
make

#TODO config redis as daemon
#TODO test node and mongodb and redis