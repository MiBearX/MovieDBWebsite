## CS 122B Project 2

## Team Number
- 110 on Spreadsheet

## Demo Link
https://youtu.be/uFAYYt_f5Uk

## Deployment
- Deploy normally, make sure to change movie table title column to full text.

## Contribution
- Michael Xiong - Everything for P4

## Connection Pooling
- Connection Pooling is set up in WebContent/META-INF/context.xml
- Prepared Statements and Pooling are used in every Java servlet that connects to db.

## Master/Slave
- I used two resources in WebContent/META-INF/context.xml
- Reads are done on localhost (slave) db.
- Writes are done on the second resource db (jdbc/master), linked to master instance.
- Servlets that write to jdbc/master are AddMovieServlet, InsertStarServlet, PlaceOrderServlet
