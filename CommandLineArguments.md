
```
Option (* = required)                   Description                            
---------------------                   -----------                   
-i, --ignore [Long]                     Set ignore categories. Ex: -i 1,2,3 (default: -1)                        
-r, --result <File>                     Sets the result file. (default: results.csv)                         
* -s, * --source <File>                 Sets the source file.

Examples:

# Specify a source file with default result file being ./result.csv
java -jar -Xmx2048m water-category-identifier.jar -s ./source.csv

# Specify both a source and result file
java -jar -Xmx2048m water-category-identifier.jar -s ./source.csv -r ./result.csv

# Ignore category -1
java -jar -Xmx2048m water-category-identifier.jar -s ./source.csv
java -jar -Xmx2048m water-category-identifier.jar -s ./source.csv -i -1

# Ignore a list of categories (note: overrides -1 default)
java -jar -Xmx2048m water-category-identifier.jar -s ./source.csv -i 1,2,3

# Use all categories
java -jar -Xmx2048m water-category-identifier.jar -s ./source.csv -i

# Set the memory (should increase if encountered an OutOfMemoryException)
java -jar -Xmx4096m water-category-identifier.jar -s ./source.csv

# Note: options can be in any order after the jar so long as an option's arguments appear next to the option.
```