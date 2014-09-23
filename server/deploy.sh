#!/bin/sh

# What am I doing?
# 1. Stop existing server
# 2. Backup old server version
# 3. Unzip new server version
# 4. Restart server

if [ $# -ne 2 ]; then
    echo "Usage: ./deploy.sh <ServerPath> <ZipPath>"
    exit
fi

SERVER=$1
ZIP=$2

cd "$SERVER"
echo "- Updating server ..."

#check if server is running
RUNNING=$(forever list | grep app/server.js)
if [ "$RUNNING" ]; then
    echo "- Stopping server ..."
    forever stop "$SERVER/server/app/server.js"
fi

#if we already have a server folder there, move it into backups
if [ -d "server" ]; then
    echo "- Backing up existing server version ..."
    if [ ! -d "backups" ]; then
        mkdir backups
    fi
    cp -r server "backups/server.$(date +"%Y%m%d%H%M%S")"
fi

echo "- Unzipping new server version ..."
unzip -q -o "$ZIP" -d server

echo "- Updating nodejs packages ..."
cd server/app
npm install
cd ../..

echo "- Restarting server ..."
forever -a -m 10 start "$SERVER/server/app/server.js"
echo "- All done :)"