WHAT IT IS
==========

These files and directories represent the test cases I used to do some
performance tests with JIIC (Java ISO Image Creator) and mkisofs. The
tests were made on Kubuntu 6.06.1 using kernel parameter
ramdisk_size=524288 (512 MB ramdisk).


PREREQUISITES
=============

To complete the test suite, get the following files from the respective
web sites:

tomsrtbt-2.0.103.ElTorito.288.img (for El Torito tests)
  http://www.toms.net/rb/download.html

linux-2.6.20.tar.bz2 (use "ant linux-extract" to extract)
  http://kernel.org/pub/linux/kernel/v2.6/linux-2.6.20.tar.bz2

eldream/ed_1024.avi (Elephants Dream)
  http://orange.blender.org/download


FILES
=====

build.xml
  Ant build script with various test targets

clear.sh
  Shell script that removes *.iso from ramdisk

Copyright.txt
  Copyright information (test case setup; Tux image)

iso9660.jar
  JIIC classes (the actual program)

ISOtest.class
  ISOtest class (test program)

makerd.sh
  Shell script to setup a ramdisk. Adapt user (jens) as needed.

perf.sh
  Shell script for easy execution of tests.
  1st parameter: program configuration to run
  2nd parameter: file or directory to record

run.sh
  Example performance test script. Adapt CONFIG, WHAT and calls as needed.

sabre.jar
  SABRE classes (requirement for the actual program)


ISOtest USAGE
=============

If run without parameters, ISOtest uses some default tests, including
the test directory. If at least one parameter is specified, it is
interpreted as the output filename. If more than one parameter is
specified, the default tests are disabled and each additional parameter
is interpreted as a file or directory to be recorded. The only
exceptions are the strings --disable-eltorito, --disable-rockridge and
--disable-joliet which disable one of the extensions, respectively.