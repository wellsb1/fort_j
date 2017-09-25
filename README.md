# fort_j
Generic Java Utility Classes / Methods

### Info
* Version is currently set to 0.0.1
* Group is currently wellsb1 

### Helpful Commands
###### (in no particular order)
* 'gradle eclipse' to convert this project into an eclipse project
* 'gradle install' to install this project's jar into a local maven repository for use by other projects
* 'gradle uploadArchives' to start the process of pushing a build into the Maven Repositiory.  Takes a few minutes to complete.  See more below...
* 'gradle closeAndReleaseRepository' closes and releases the uploaded project into the Maven Repository.  This should be run after the 'uploadArchives' task.  Takes a few minutes to complete.

### Maven Central
In order to push a build to the Maven Central repository, the following must occur:
1. PGP key must exist - if you have git installed, you should have access to the pgp executable, otherwise you'll need to download it from [gnupg.org](https://www.gnupg.org/download/) 
1. 'gradle uploadArchives'
1. 'gradle closeAndReleaseRepository' CAUTION! once a project has been released it cannot be removed from Maven Central.


### Creating a PGP key
Run the following commands
1. create a PGP key
1. create a gradle.properties using the following template

```
signing.keyId=YourKeyId ex. X000000X
signing.password=YourPublicKeyPassword - password used when creating the pgp
signing.secretKeyRingFile=PathToYourKeyRingFile/secring.gpg

ossrhUsername=your-jira-id
ossrhPassword=your-jira-password
```

Replace the above values with your PGP key and your ossr login information 