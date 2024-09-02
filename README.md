## Demo Link
- https://youtu.be/lr0H0Nv98Bs

## Jmeter tests
- 1 Control Plane + 3 Worker nodes + 1 master MySQL pod + 1 slave MySQL pod + 2 Fabflix pods: 3,525.057/minute
- 1 Control Plane + 4 Worker nodes + 1 master MySQL pod + 1 slave MySQL pod + 3 Fabflix pods: 522.43/minute

## Deployment
- Deploy mvn package normally, make sure to change movie table title column to full text and unencrypt passwords.

## Connection Pooling
- Connection Pooling is set up in WebContent/META-INF/context.xml
- Prepared Statements and Pooling are used in every Java servlet that connects to db.

## Master/Slave
- Used two resources in WebContent/META-INF/context.xml
- Reads are done on localhost (slave) db.
- Writes are done on the second resource db (jdbc/master), linked to master instance.
- Servlets that write to jdbc/master are AddMovieServlet, InsertStarServlet, PlaceOrderServlet
