= java-iso-tools

This project provides a number of tools for manipulating iso9660 filesystems natively within Java.

image:https://img.shields.io/badge/project%20status-unmaintained-red[Project Status]
image:https://img.shields.io/maven-central/v/com.github.stephenc.java-iso-tools/java-iso-tools-parent[Maven Central Latest Version]
image:https://github.com/stephenc/java-iso-tools/workflows/Java%20CI%20with%20Maven/badge.svg[Java CI with Maven]

== Project status

I (link:https://github.com/stephenc[@stephenc]) am not actively maintaining this repository.
Because the Maven Central publishing of this is tied to my personal account, I cannot transfer the project to somebody else without changing the GroupId.

If somebody wants to take responsibility for vetting pull requests, etc I am happy to give them commit permissions and I can set up something to allow periodic releases.

NOTE: If somebody wants to take over as an offical fork at the a new GroupId, create a pull request against this README with a pointer to the fork. 
If, after 3 months there are more thumbs up from users than thumbs down, I will merge the pull request for the fork with the most "votes".

If nobody steps forward, do not expect any new releases or any work on the open issues.

== Getting started quickly

Visit link:https://mvnrepository.com/artifact/com.github.stephenc.java-iso-tools[mvnrepository.com], select the parts you need for your project, and then add that to your build system via Gradle, Maven, or other.

== Building from source

These instructions will get you a copy of the project up and running on your local machine for development and testing purposes.
See deployment for notes on how to deploy the project on a live system.

=== Prerequisites

* Maven
* JDK

=== Installing

Assuming you already have a working JDK and maven install.

Compiling

[source,bash]
----
mvn compile
----

== Running the tests

NOTE: There are some default tests

Use maven to run existing unit tests

[source,bash]
----
mvn test
----

To run all tests including the Maven plugin integration tests:

[source,bash]
----
mvn verify
----
