#OpenRemote Controller 2

Build is done using gradle.

To build the controller war, run *gradlew clean build*.
This also runs the tests, currently 44 tests are failing, this is "normal" and will eventually get fixed.
war file can be found under build/libs

To build controller zip distribution, run *gradlew clean controller*.
zip file can be found under build/distributions

Samsung protocol jar that was built by previous ant build mechanism is not yet integrated into new gradle build.
