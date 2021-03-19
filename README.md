# fort_j
Generic Java Utility Classes / Methods


### Change Log

5/14/18 - 0.0.3
 * Updated Lang.empty() to support varargs

5/09/18 - 0.0.2
 
 * Changed method signature on J.explode()

### Info
* Version is currently set to 1.0.0
* Group is currently com.github.wellsb1 
* Requires Gradle 5+

### Helpful Commands
###### (in no particular order)
* Importing the project  ~~``gradle eclipse``~~ for Eclipse, right-click in the Package Explorer and select Gradle -> Existing Gradle Project convert this project into an eclipse project 
* ``gradle publishToMavenLocal`` ~~``gradle install``~~ install this project's jar into a local maven repository for use by other projects
* ``gradle publish`` ~~``gradle uploadArchives``~~ start the process of pushing a build into the Maven Repositiory.  Takes a few minutes to complete.  See more below...
* ``gradle publishToSonatype closeAndReleaseStagingRepository`` ~~``gradle closeAndReleaseRepository``~~ closes and releases the uploaded project into the Maven Repository.  This can be run after the 'publish' ~~'uploadArchives'~~ task to bypass manually releasing the artifact from the [Nexus OSSR site](https://oss.sonatype.org/).  Takes a few minutes to complete. Yes, **both commands are required to be run together**.

### Maven Central
In order to push a build to the Maven Central repository, the following must occur: [(official steps here)](http://central.sonatype.org/pages/gradle.html)
1. You must have a [Sonatype JIRA account](https://issues.sonatype.org/secure/Signup!default.jspa).  This acct will be used to access both 'Sonatype Jira' AND Sonatype's 'OSS' site.  The acct name will need to be added to any/all projects that you plan to deploy in the future, do so via the JIRA ticket pertaining to that groupId
	- *example:* groupId: [com.github.wellsb1](https://issues.sonatype.org/browse/OSSRH-34727)
1. Local PGP key must exist - see creating a key below.
1. A 'gradle.properties' file must exist in either the project's root directory or ~/.gradle/.  See a template at the bottom of this page. 
1. ``gradle publish`` publishes the project to Maven Central.  Once completed, log into [Sonatype's OSS site](https://oss.sonatype.org/#stagingRepositories) to release the artifact to Maven Central.  Select, then 'close' the staged project.  After a minute, refresh the list.  Select and release the artifact.  You can now search the Repositories to verify the project has been released.  Up to 2hrs may pass before the artifact become available on Maven Central.
	- *optionally:* ``gradle closeAndReleaseStagingRepository`` bypasses logging into Sonatype to release the artifact.  CAUTION! Once a project has been released it cannot be removed from Maven Central.


### [Creating a PGP key](http://central.sonatype.org/pages/working-with-pgp-signatures.html)
If you have git installed, use it's 'bin/gpg' to create a key, otherwise you'll need to download the app from [gnupg.org](https://www.gnupg.org/download/) 
1. ``gpg --gen-key`` Select the default value when asked for the kind (RSA) and the size (2048bit) of the key.  Provide your name, email, and a comment for the key. These identifiers are essential as they will be seen by anyone downloading a software artifact and validating a signature.  *NOTE* If you do not get the an opportunity to select the bit size, use the following command to generate your key `gpg --full-generate-key`
1. ``gpg --list-keys --keyid-format short`` Once key pair is generated, list them along with any other keys installed
1.  Make note of your keyId and path to your key file as that information will be added to the gradle.properties file.  In the following example, 'C6EED57A' is the keyId
	```
	/home/juven/.gnupg/pubring.gpg
	------------------------------
	pub   1024D/C6EED57A 2010-01-13
	uid                  Juven Xu (Juven Xu works at Sonatype) <juven@sonatype.com>
	sub   2048g/D704745C 2010-01-13
	```
1. Finally, upload your public key so that others have access to it.  Be sure to update the keyId in the example 
	```
	gpg --keyserver hkp://pool.sks-keyservers.net --send-keys C6EED57A
	```

### gradle.properties
Use the following template to create a 'gradle.properties' file

```
signing.keyId=YourKeyId ex. X000000X
signing.password=YourPublicKeyPassword - password used when creating the pgp
signing.secretKeyRingFile=PathToYourKeyRingFile/secring.gpg

sonatypeUsername=your-jira-id
sonatypePassword=your-jira-password
```

 * Replace the above values with your PGP key and your OSSR (Sonatype JIRA) login information
 * Place the gradle.properties into the project's root directory OR your 'USER/.gradle' directory 