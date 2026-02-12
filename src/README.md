# ECSE420_A1
MUST NAVIGATE TO SRC DIRECTORY
To run any one of the files, NAVIGATE to the src directory of the project in the terminal.

then run "javac relative/or/absolute/path/to/file.java" to compile it
then run "java ca.mcgill.ecse420.a1.file" to run it, where "file" is the filename

For Question 1 (MatrixMultiplication.java):
 There is some commented code already present
 "runThreadScalingExperiment(2000, new int[]{1,2,3,4,5,6,7,8,9,10,11,12,13,14,15});" for 1.4
 and "runSizeScalingExperiment(new int[]{100,200,500,1000,2000,3000,4000}, 8);" for 1.5,
 (The rest of the main function is used for answering 1.1, 1.2, 1.3)
 To run the respective part of question 1, comment out the rest of the main function and uncomment the relevant part.

For Question 3 (DiningPhilosophers.java):
 The way the code functions depends on the value of the variable: RUN_DEADLOCK_VERSION 
 (true makes it run for 3.1 and false makes it run for 3.2)

 We additionally have the function calls to sleepALittle, we experimented with different values.