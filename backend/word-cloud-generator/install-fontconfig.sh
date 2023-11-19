#!/bin/bash

is_apt_available=`command -v apt-get`
is_yum_available=`command -v yum`

if [ -n "$is_apt_available" ]; then
    if dpkg -s fontconfig; then
        echo "fontconfig already installed"
        exit 0
    fi

    # Install fontconfig
    sudo apt-get update
    sudo apt-get -y install fontconfig

elif [ -n "$is_yum_available" ]; then
    if rpm -q fontconfig; then
        echo "fontconfig already installed"
        exit 0
    fi

    # Install fontconfig
    sudo yum -y install fontconfig
else
    echo "Warning: no path to apt-get or yum"
fi
