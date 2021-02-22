# Device-Management

This project consists of management and administration of Devices. The core service provides a REST interface for querying the current state of Device registry as well as for changing it.

 The Registry developed exposes server that provides access to all information and operations needed to set up, run and manage the Device configurations. By default, the REST interface is exposed at *0.0.0.0:9000*, and contains endpoints for:
 
* Creating a new Device with a new set of configurations. This is achieved by exposing the end point POST /devices/create
* Updating an Existing device with new set of configurations. This is achieved by exposing the end point PUT /devices/update
* Getting details of all the created devices. This is achieved by exposing the end point GET /devices
* Getting details of a particular device by using its id. This is achieved by exposing the end point GET /devices/{id}
* Finally, deleting the device. This is achieved by exposing the end point DELETE /devices/{id}/delete
* Please refer to *OpenAPISpecification.yaml* to know more information about the endpoints

## Adapt the configuration file

The project makes use of two types memory to save the device configuration data. It uses the value of ```useInMemoryDB``` present in the file *src/main/scala/flix/deviceregistry/Configuration.scala* to find out whether the data has has to be stored internally or in MySql Database. If the value of ```useInMemoryDB``` is set to true, then the data is saved internally, and if the value is false, then the data is stored in Mysql Database. Please make appropriate changes to the parameters present in the *Configuration.scala* if you wish the data to be saved in MySql DB. 

## Docker Configurations

The project already contains a docker file inside the folder *device-management/target/docker/dockerfile*. Please run the appropriate docker command at the desired port to run the project through a container.

## Running the Application

The project can be started by running the command ```sbt run``` from inside the project folder. The project then starts at *localhost:9000* server. The endpoints mentioned above can be manually tested by using ```curl``` commands for respective endpoints.

The project can be tested by running the command ```sbt test```. This process initialzes the server and various operations for the devices are tested for both internal memory as well as external database. 
