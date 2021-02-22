package flix.deviceregistry

class Configuration( ) {
  //Where to host the http server
  val bindHost: String = "0.0.0.0"
  val bindPort: Int = 9000


  //Auth database configuration
  val useInMemoryDB = true
  val databaseHost = "jdbc:mysql://localhost/"
  val databaseName = ""
  val databaseDriver = "com.mysql.jdbc.Driver"
  val databaseUserName = ""
  val databasePassword= ""
}


