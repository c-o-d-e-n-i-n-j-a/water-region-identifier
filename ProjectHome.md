This program is designed to uniquely identify separate regions of water based on a category identifier.

This project does not make use of a database, and therefor can be very memory intensive depending on the data set (memory usage can increase exponentially for the dataset in the worst case).  The goal is to decrease the processing time as much as possible.  As such, if you get any OutOfMemoryExceptions, increase the -Xmx switch value (ex: -Xmx2048m).

This program makes use of log4j and JOpt Simple libraries.