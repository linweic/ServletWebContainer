#!/bin/bash

echo "VOC::ID=2000"
echo "VOC::2000::REPORT_OUTPUT=1"

echo "VOC::2000::REPORT_STRING='Testing compilation...'"


echo "VOC::2000::REPORT_STRING='Running submission tests'"
echo "VOC::2000::REPORT_STRING=''"

dir=$(pwd)
port=$((($RANDOM % 62535) + 3000))
test_port=$((($RANDOM % 62535) + 3000))

#unzip ./submit-hw1.zip -d .

#Checks if user filled the first line of the README file

if grep -Fxq "Full name:  ______________" README
then
  echo "VOC::2000::REPORT_STRING='Error with submission: README file was not filled'"
fi

#Checks if ant build works
ant build -Dbuild.compiler=javac1.7
rc=$?
if [ $rc != 0 ]
then
  echo "VOC::2000::REPORT_STRING='Error with submission: ant build failed. Make sure `ant build` works and then `ant pack` and try uploading it again'"
fi

#Tries running the HttpServer with invalid parameters. None of these should work, so if the curl connection
#succeeds its because it actually connected when it shouldn't have managed to do that

java -Xmx2048m -Xms256m -classpath lib/*:target/WEB-INF/classes/. edu.upenn.cis.cis455.webserver.HttpServer p $dir/www &
SERVER_PID=$!
sleep 2

if curl -i -s "http://localhost:8080" | grep -q "HTTP/1.[0|1] 200 OK"
then
  echo "VOC::2000::REPORT_STRING='Error with submission: it was possible to run your server with invalid port parameter p. Make sure to do validation of the program arguments.'"
  curl -i -s "http://localhost:8080/shutdown" > /dev/null
  sleep 4
  kill $SERVER_PID 2> /dev/null
fi 

java -Xmx2048m -Xms256m -classpath lib/*:target/WEB-INF/classes/. edu.upenn.cis.cis455.webserver.HttpServer $test_port $dir/wwww &

if curl -i -s "http://localhost:$test_port" | grep -q "HTTP/1.[0|1] 200 OK"
then
  echo "VOC::2000::REPORT_STRING='Error with submission: it was possible to run your server with with an inexisting root directory. Make sure to do validation of the program arguments.'"
  curl -i -s "http://localhost:$test_port/shutdown" > /dev/null
  sleep 4
  kill $SERVER_PID 2> /dev/null
fi 

#Runs the HttpServer with valid parameters
java -Xmx2048m -Xms256m -classpath lib/*:target/WEB-INF/classes/. edu.upenn.cis.cis455.webserver.HttpServer $port $dir/www &
SERVER_PID=$!
sleep 3

#Tries doing a simple connection to the root of the server
if curl -i "http://localhost:$port" | grep -q "HTTP/1.[0|1] 200 OK"
then
  echo "VOC::2000::REPORT_STRING='Webserver was set up on port $port and connects successfully.'"
else
  echo "VOC::2000::REPORT_STRING='Error with submission: couldnt connect to HttpServer root page.'"
  echo "VOC::2000::REPORT_STRING='We are testing your submission with port $port and using your www folder as the root. Make sure your server knows how to deal with those appropriately'"
fi

#Tries shutting down the server
if curl -i -s "http://localhost:$port/shutdown" | grep -q "HTTP/1.[0|1] 200 OK"
then
  echo "VOC::2000::REPORT_STRING='Shutdown page returned 200 OK.'"
else
  echo "VOC::2000::REPORT_STRING='Error with submission: shutdown didn't return a 200 OK."
  
fi

sleep 6

#Checks if shutdown really worked
if curl -i "http://localhost:$port" | grep -q "HTTP/1.[0|1] 200 OK"
then
  echo "VOC::2000::REPORT_STRING='Error with submission: server can still be accessed after shutdown."
  kill $SERVER_PID 2> /dev/null
fi


echo "VOC::2000::REPORT_STRING='If you didnt get any errors, this means your submission is good to go.'"
echo "VOC::2000::REPORT_STRING='Done with script'"
