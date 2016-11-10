#OpenRemote Controller 2

### Run tests

`./gradlew clean check`

(Ignore the 44 tests which are currently failing, we are working on fixing them.)

### Build the application WAR

`./gradlew clean war`

The deployable WAR file can be found under `build/libs`. 

### Build the distribution ZIP

`./gradlew clean controller`

The ZIP file can be found under `build/distributions`.

### Build and deploy with Docker

`./gradlew clean buildImage`

Start the container:

`docker run -i -t -p 8688:8688 openremote/controller run`

Access the controller at `http://<Your Docker Host>:8688/controller/` and synchronize its configuration with your [Designer account](http://designer.openremote.com/). 

You can also directly build and push the image to our [Docker Hub Account](https://hub.docker.com/u/openremote/): `/gradlew clean pushImage -PdockerHubUsername=username -PdockerHubPassword=secret`

The Docker image is currently work in progress and some issues remain:

* Configuration and stored data is not persistent (problems with Docker volume mapping)
* Z-Wave and Velbus protocols are not bundled