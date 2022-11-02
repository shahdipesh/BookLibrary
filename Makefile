.PHONY := build


CS3380A2Q5.class: CS3380A2Q5.java 
	javac CS3380A2Q5.java

build: CS3380A2Q5.class 

run: build
	java -cp .:sqlite-jdbc-3.39.3.0.jar CS3380A2Q5
