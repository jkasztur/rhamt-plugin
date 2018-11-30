# Red hat Application Migration Toolkit
Red Hat Application Migration Toolkit is a collection of open 
source tools that enables large-scale application migrations and modernizations.
The tools provide support for each step of the migration process. 
The main supported migrations consist of:
* Application platform upgrade
* Migration to a cloud-native deployment environment
* Migration from commercial products to Red Hat Jboss Enterprise Application Platform

RHAMT is available from: https://developers.redhat.com/products/rhamt/download/

## Build steps
The plugin introduces two new steps:
* build step: Execute RHAMT
* post build step: Display RHAMT metric
  * creates graph metric from previous builds
  * show analysis of last build on main project page
  
Available configuration options:
* Input
* Output
* Source technology
* Target technology
* User rules directory
* Packages
* Excluded packages
* Online
* Source mode
* Exploded app
* Mavenize
* Mavenize groupId
* Tattletale
* Export CSV
* Keep work directories
* Compatible files report
* ClassNotFound analysis
* Included tags
* Excluded tags
* Additional classpath
* User ignore path

## Initial configuration
To properly use the plugin, we need to specify RHAMT home directory in Manage Jenkins -> Configure System -> RHAMT Plugin.
This value is then used in RHAMT execution.

## Integration tests
Prerequisites:
* downloaded and unzipped RHAMT
* `rhamt.home` specified in `src/test/resources/test.properties`

To run the provided tests use the following command:  

```mvn clean install -Pit-tests```
