#OpenRemote Controller 2

### Build

Build is done using gradle.

To build the controller war, run *./gradlew clean build*.
This also runs the tests, currently 44 tests are failing, this is "normal" and will eventually get fixed.
war file can be found under build/libs

To build controller zip distribution, run *./gradlew clean controller*.
zip file can be found under controller/build/distributions

Samsung protocol jar that was built by previous ant build mechanism is not yet integrated into new gradle build.

### Docker image

Build Docker images with `./gradlew clean buildImage`. Remove old images before if you don't want to use the Docker build cache.

You can start and test these images with the `docker run -i -t -p 8688:8688 openremote/controller run`.

You can also directly build and push the image to our [Docker Hub Account](https://hub.docker.com/u/openremote/): `/gradlew clean pushImage -PdockerHubUsername=username -PdockerHubPassword=secret`

This is work in progress and there are still pending issues on the Docker image:
* Z-Wave and Velbus protocols are not bundled
* configuration is not exposed outside of the docker image