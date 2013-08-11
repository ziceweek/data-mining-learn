We've provided the basic requirements for building and running with a variety of tools.  The most complicated part of developing with java is properly including the json-simple-1.1.1.jar at compile time and submit time.  This is a external library which we use to properly encode your submission before sending it to our servers.  We've set things up so that it just works out of the box with Eclipse and Ant, but please keep this in mind if you run into any compile errors involving JSON-related classes.


ECLIPSE:
We've provided .project and .classpath files, which you can directly import into eclipse. To do this, select File -> Import.  In the dialog box which pops up, expand the folder called "General" and choose the option "Existing Projects into Workspace".  Finally, set the root directory to be the java directory within the original project directory: pa1-spamlord-vn/java.  The project is already set up to pass in the arguments "../data/dev ../data/devGOLD" under the run configuration called "Develop", so make sure you do not change the directory structure.  To submit your assignment you can use "Submit" run configuration and enter your e-mail and password in the Eclipse Console.  Also, the .classpath file should already include the submit dependency json-simple-1.1.1.jar.


ANT:
we've provided a basic build.xml file for use with ant.  Just call:

$ ant compile
$ ant run
$ ant submit


COMMAND LINE:
If you want to develop on the command line, use the following commands to build and run your code from the pa1-spamlord/java directory:

$ mkdir classes
$ javac -cp "json-simple-1.1.1.jar" -d classes *.java
#  test on dev set
$ java -cp "classes" SpamLord
#  submit (LINUX / MAC OS)
$ java -cp "json-simple-1.1.1.jar:classes" Submit
#  submit (WINDOWS)
$ java -cp "json-simple-1.1.1.jar;classes" Submit
