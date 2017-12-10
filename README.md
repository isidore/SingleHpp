# SingleHpp
Combine a directory of C++ files into a single .hpp for deployment ease


Check the test as it's the main runner at the moment.


## Unalterable

If you need something in a particular place (usually a header after a DEFINE) Try wrapping it in
``` cpp
// <SingleHpp unalterable>
 ... your code here ...
// </SingleHpp>

```

## Commandline

Currently must be run from the directory you want to search(recursively) from.
```
  java -jar SingleHpp.jar my_output_file.hpp
```
