#!/bin/bash

sudo mke2fs -m 0 /dev/ram0
sudo mkdir -p /mnt/rd
sudo mount /dev/ram0 /mnt/rd
sudo chown jens.jens /mnt/rd
cd /mnt/rd
ls -la
