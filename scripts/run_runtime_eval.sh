#!/bin/bash

DATASET_DIR=$1
OUTPUT_DIR=$2
LOG_DIR=$OUTPUT_DIR/logs
JAR_DIRECTORY=target/auto-oas-1.2.0-jar-with-dependencies.jar

# Ensure dataset folder exists
if [ ! -d "$DATASET_DIR" ]; then
  echo "Error: dataset folder not found."
  exit 1
fi

RUN_SPRING=false
RUN_JERSEY=false

# Parse flags indicating which frameworks to evaluate on
for arg in "$@"; do
  case $arg in
    -spring) RUN_SPRING=true ;;
    -jersey) RUN_JERSEY=true ;;
    *) : ;;
  esac
done

# Create generated folder if it doesn't exist
echo "Creating output folders..."
mkdir -p $OUTPUT_DIR
mkdir -p $LOG_DIR

# Remove all spoon temp files
find "$DATASET_DIR" -type f -name "spoon*" -exec rm {} \;


# Loop through projects in dataset folder and print the command
# for project in $(ls "$DATASET_DIR"); do
#   if [[ -d "$DATASET_DIR/$project" && ! "$project" =~ ^\..* ]] ; then
#     command="java -jar target/auto-oas-1.2.0-jar-with-dependencies.jar \"$DATASET_DIR/$project\" \"$OUTPUT_DIR/${project}_oas\""
#     echo "$command"
#   fi
# done



### Spring projects ###
if $RUN_SPRING; then

echo "Running for Spring projects..."

echo "1/7 Running catwatch"
{ time java -jar $JAR_DIRECTORY "$DATASET_DIR/catwatch" "$OUTPUT_DIR/catwatch_oas" ; } >> $LOG_DIR/catwatch.log 2>&1

echo "2/7 Running cwa"
{ time java -jar $JAR_DIRECTORY "$DATASET_DIR/cwa-verification-server" "$OUTPUT_DIR/cwa-verification-server_oas" ; } >> $LOG_DIR/cwa.log 2>&1

echo "3/7 Running ocvn"
{ time java -jar $JAR_DIRECTORY "$DATASET_DIR/ocvn/web" "$OUTPUT_DIR/ocvn_oas" ; } >> $LOG_DIR/ocvn.log 2>&1

echo "4/7 Running ohsome"
{ time java -jar $JAR_DIRECTORY "$DATASET_DIR/ohsome-api" "$OUTPUT_DIR/ohsome-api_oas" ; } >> $LOG_DIR/ohsome-api.log 2>&1

echo "5/7 Running proxyprint"
{ time java -jar $JAR_DIRECTORY "$DATASET_DIR/proxyprint-kitchen" "$OUTPUT_DIR/proxyprint-kitchen_oas" ; } >> $LOG_DIR/proxyprint.log 2>&1

echo "6/7 Running quartz-manager"
{ time java -jar $JAR_DIRECTORY "$DATASET_DIR/quartz-manager/quartz-manager-parent" "$OUTPUT_DIR/quartz-manager_oas" ; } >> $LOG_DIR/quartz-manager.log 2>&1

echo "7/7 Running ur-codebin"
{ time java -jar $JAR_DIRECTORY "$DATASET_DIR/Ur-Codebin-API" "$OUTPUT_DIR/Ur-Codebin-API_oas" ; } >> $LOG_DIR/ur-codebin.log 2>&1

fi



### Jersey projects ###
if $RUN_JERSEY; then

echo "Running for Jersey projects..."

echo "1/8 Running digdag"
{ time java -jar $JAR_DIRECTORY "$DATASET_DIR/digdag" "$OUTPUT_DIR/digdag_oas" ; } >> "$LOG_DIR/digdag.log" 2>&1

echo "2/8 Running enviroCar-server"
{ time java -jar $JAR_DIRECTORY "$DATASET_DIR/enviroCar-server" "$OUTPUT_DIR/enviroCar-server_oas" ; } >> "$LOG_DIR/enviroCar-server.log" 2>&1

echo "3/8 Running features-service"
{ time java -jar $JAR_DIRECTORY "$DATASET_DIR/features-service" "$OUTPUT_DIR/features-service_oas" ; } >> "$LOG_DIR/features-service.log" 2>&1

echo "4/8 Running gravitee-api-management"
{ time java -jar $JAR_DIRECTORY "$DATASET_DIR/gravitee-api-management" "$DATASET_DIR/gravitee-api-management/gravitee-apim-rest-api/gravitee-apim-rest-api-management" "$OUTPUT_DIR/gravitee-api-management_oas" ; } >> "$LOG_DIR/gravitee-api-management.log" 2>&1
echo "4/8 Running gravitee-api-management v4"
{ time java -jar $JAR_DIRECTORY "$DATASET_DIR/gravitee-api-management" "$DATASET_DIR/gravitee-api-management/gravitee-apim-rest-api/gravitee-apim-rest-api-management-v4" "$OUTPUT_DIR/gravitee-api-management-v4_oas" ; } >> "$LOG_DIR/gravitee-api-management.log" 2>&1

echo "5/8 Running kafka-rest"
{ time java -jar $JAR_DIRECTORY "$DATASET_DIR/kafka-rest" "$OUTPUT_DIR/kafka-rest_oas" ; } >> "$LOG_DIR/kafka-rest.log" 2>&1

echo "6/8 Running management-api-for-apache-cassandra"
{ time java -jar $JAR_DIRECTORY "$DATASET_DIR/management-api-for-apache-cassandra/management-api-server" "$OUTPUT_DIR/management-api-for-apache-cassandra_oas" ; } >> "$LOG_DIR/management-api-for-apache-cassandra.log" 2>&1

echo "7/8 Running restcountries"
{ time java -jar $JAR_DIRECTORY "$DATASET_DIR/restcountries" "$OUTPUT_DIR/restcountries_oas" ; } >> "$LOG_DIR/restcountries.log" 2>&1

echo "8/8 Running senzing-api-server"
{ time java -jar $JAR_DIRECTORY "$DATASET_DIR/senzing-api-server" "$OUTPUT_DIR/senzing-api-server_oas" ; } >> "$LOG_DIR/senzing-api-server.log" 2>&1

fi
