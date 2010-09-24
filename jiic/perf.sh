#!/bin/bash

DEST=/mnt/rd

case $1 in
  "mjr")
    time mkisofs -J -R -o $DEST/$2-mkisofs.iso $2 2>/dev/null ;;
  "mr")
    time mkisofs -R -o $DEST/$2-mkisofs.iso $2 2>/dev/null ;;
  "mj")
    time mkisofs -J -o $DEST/$2-mkisofs.iso $2 2>/dev/null ;;
  "m")
    time mkisofs -o $DEST/$2-mkisofs.iso $2 2>/dev/null ;;
  "ijr")
    time java -cp sabre.jar:iso9660.jar:. ISOtest $DEST/$2.iso --disable-eltorito $2 2>/dev/null ;;
  "ir")
    time java -cp sabre.jar:iso9660.jar:. ISOtest $DEST/$2.iso --disable-eltorito --disable-joliet $2 2>/dev/null ;;
  "ij")
    time java -cp sabre.jar:iso9660.jar:. ISOtest $DEST/$2.iso --disable-eltorito --disable-rockridge $2 2>/dev/null ;;
  "i")
    time java -cp sabre.jar:iso9660.jar:. ISOtest $DEST/$2.iso --disable-eltorito --disable-rockridge --disable-joliet $2 2>/dev/null ;;
  *)
    echo "Usage: $0 mjr|mr|mj|m|ijr|ir|ij|i <file or directory>"
    echo "Example: $0 mjr eldream -> mkisofs; Joliet, Rock Ridge; eldream"
    ;;
esac
