#!/usr/bin/env bash

function prop {
    grep "${1}" config/environment.properties |cut -d'=' -f2
}
