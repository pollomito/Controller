#OpenRemote Controller 2

### Run tests

`./gradlew clean check`

### Build the application WAR

`./gradlew clean war`

The deployable WAR file can be found under `build/libs`. 

### Build the distribution ZIP

`./gradlew clean controller`

The ZIP file can be found under `controller/build/distributions/`.

### Build and deploy with Docker

`./gradlew clean buildImage`

Start the container:

`docker run -i -t -p 8688:8688 openremote/controller run`

Access the controller at `http://<Your Docker Host>:8688/controller/` and synchronize its configuration with your [Designer account](http://designer.openremote.com/). 

The controller will by default try to to reach the [OpenRemote controller proxy and command service (CCS)](https://github.com/openremote/CCS). You'll see a message in the controller log reminding you to provide account credentials. This service is provided by [OpenRemote Inc](http://openremote.com), it allows secure remote access to your - usually locally installed - controller. If you want to completely disable this functionality set the environment variable of the container `BEEHIVE_REMOTE_SERVICE_URI=urn:disabled`. You can set a URL if you are deploying the [CCS](https://github.com/openremote/CCS) on your own host.

You can also directly build and push the image to our [Docker Hub Account](https://hub.docker.com/u/openremote/): `/gradlew clean pushImage -PdockerHubUsername=username -PdockerHubPassword=secret`

The Docker image is currently work in progress and some issues remain:

* Configuration and stored data is not persistent (problems with Docker volume mapping)
* Z-Wave and Velbus protocols are not bundled
