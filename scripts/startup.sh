#!/bin/bash

# Run jar
DEPLOY_PATH="/home/ec2-user"
DEPLOY_LOG_PATH="/home/ec2-user/deploy.log"
DEPLOY_ERROR_LOG_PATH="/home/ec2-user/deploy_err.log"
APPLICATION_LOG_PATH="/home/ec2-user/application.log"

ALL_JAR="/home/ec2-user/build/*.jar" # Expects one jar file exists.
JAR_PATH=$(ls $ALL_JAR)
JAR_NAME=$(basename $JAR_PATH)

echo "> Build filename: $JAR_NAME" >> $DEPLOY_LOG_PATH

echo "> Build file copy" >> $DEPLOY_LOG_PATH
cp $JAR_PATH $DEPLOY_PATH

echo "> Get application PID if there is one running" >> $DEPLOY_LOG_PATH
CURRENT_PID=$(pgrep -f $JAR_NAME)

if [ -z "$CURRENT_PID" ]; then
    echo "> No application running" >> $DEPLOY_LOG_PATH
else
    echo "> kill -9 $CURRENT_PID" >> $DEPLOY_LOG_PATH
    kill -9 $CURRENT_PID
    sleep 5
fi

JAVA_OPTS="-Dspring.jpa.hibernate.ddl-auto=update"

JAR_PATH=$DEPLOY_PATH/$JAR_NAME
echo "> JAR_PATH $JAR_PATH" >> $DEPLOY_LOG_PATH
echo "> JAVA_OPTS $JAVA_OPTS" >> $DEPLOY_LOG_PATH
nohup java $JAVA_OPTS -jar $JAR_PATH >> $APPLICATION_LOG_PATH 2>> $DEPLOY_ERROR_LOG_PATH &