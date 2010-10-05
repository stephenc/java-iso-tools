#!/bin/bash

./makerd.sh

function perf {
  ./clear.sh
  ./perf.sh $CONFIG $WHAT
  read
}

export CONFIG=ijr
export WHAT=minimal

perf

perf
perf
perf
perf
perf
