# Version 0.2 #
## Refactoring/renaming ##
A new class has been introduced, called the WaterMatrix, which represents the 'grid' of water categories within the input file.  Underneath it is just a 2 dimensional array.  Once loaded, the an instance of this class's state is immutable.  The pixels cannot be changed unless a new matrix is created.  This is useful for concurrency as it guarantees that it will never change no matter what accesses it.

A Main class was created to contain the code for starting up, reading the input arguments, and generating the output.  This is so that this code can change without having to modify the rest of the program significantly.

The class named WaterCategory was renamed to WaterPixel to better reflect its purpose.  It was then modified to be completely immutable.  Its category and x,y location would never change.  However, for performance reasons a Region was added for quick and easy access to the current pixel's current region.  Access to this region is synchronized, and all other properties still remain immutable.

CategorySorter was renamed to WaterPixelSorter.

RegionIdentifier was removed and its sole function is now a static method on region.


## Concurrency Safety ##
Since all regions have the _potential_ to be accessed by multiple threads, all access to this classes methods have been synchronized.  This is to ensure that when a region on a pixel(s) changes or is merged with another region, all the changes take place before another thread can access the region.  This more than likely will decrease performance, but is necessary to ensure correctness of the output.  There is overhead of making the synchronization calls and this forces threads trying to access the resource to wait indefinitely until the resource is available.  However, that said threads will probably will not have to wait on each other as the program first checks the (immutable) category, and if they are the same, then goes on to access the region.  There is only one thread running per category and each thread should not over step its boundary of it's set of pixels and regions.  This synchronization is in place as more of a precaution in case the way the threads process the pixels and regions is changed in a later version.


## Speed ##
The speed of the program has greatly increased.  To ensure correctness, the program was changed to have completely a immutable WaterMatrix and WaterPixels.  This added a huge overhead in that it required more iterations to complete the task.  Thus, I added a reference to a region on the WaterPixel and now only a few iterations are required.  There is some overhead with synchronizing on the WaterPixel's region, but the gains far outweigh this overhead.

After looking into various data structures for keeping track of the regions and pixels, I decided to utilize hash sets because the methods the program uses most are theoretically more performant on this class.  Direct access, removal, and checking if an object exists in the hash set all should be O(1), or constant time access no matter the size of the data set.  In the real world though, this is not the case.  There is an enormous overhead using this class and it uses up a ton of memory.  I've since replaced all collections to use ArrayList instead, which improved performance by about 15 seconds.


## Formatting ##
The output file used to print just the ID's of each region directly.  This was hard to read at a glance, especially when there are many regions (hundreds to thousands).  To enhance readability, each number may be '0' padded so all the columns in the file line up properly.  The program checks the ID with the most digits and pads all other IDs to match its length.


## Bugs/fixes ##
  * Fixed a bug where the program couldn't find the input file.
  * Fixed a bug where the program would assign a new region to each pixel or would mark a pixel as being in an incorrect region.