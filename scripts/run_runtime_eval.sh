#!/bin/bash

DATASET_DIR=$1
OUTPUT_DIR=$2
LOG_DIR=$OUTPUT_DIR/logs

# Ensure dataset folder exists
if [ ! -d "$DATASET_DIR" ]; then
  echo "Error: dataset folder not found."
  exit 1
fi

# Create generated folder if it doesn't exist
echo "Creating output folders..."
mkdir -p $OUTPUT_DIR
mkdir -p $LOG_DIR

# Remove all spoon temp files
find "$DATASET_DIR" -type f -name "spoon*" -exec rm {} \;

# Loop through projects in dataset folder and print the command
# for project in $(ls "$DATASET_DIR"); do
#   if [[ -d "$DATASET_DIR/$project" && ! "$project" =~ ^\..* ]] ; then
#     command="java -jar target/auto-oas-1.1.0-jar-with-dependencies.jar \"$DATASET_DIR/$project\" \"$OUTPUT_DIR/${project}_oas\""
#     echo "$command"
#   fi
# done

echo "1/7 Running catwatch"
{ time java -jar target/auto-oas-1.1.0-jar-with-dependencies.jar "$DATASET_DIR/catwatch" "$OUTPUT_DIR/catwatch_oas" ; } >> $LOG_DIR/catwatch.log 2>&1

echo "2/7 Running cwa"
{ time java -jar target/auto-oas-1.1.0-jar-with-dependencies.jar "$DATASET_DIR/cwa-verification-server" "$OUTPUT_DIR/cwa-verification-server_oas" ; } >> $LOG_DIR/cwa.log 2>&1

echo "3/7 Running ocvn"
{ time java -jar target/auto-oas-1.1.0-jar-with-dependencies.jar "$DATASET_DIR/ocvn/web" "$OUTPUT_DIR/ocvn_oas" ; } >> $LOG_DIR/ocvn.log 2>&1

echo "4/7 Running ohsome"
{ time java -jar target/auto-oas-1.1.0-jar-with-dependencies.jar "$DATASET_DIR/ohsome-api" "$OUTPUT_DIR/ohsome-api_oas" ; } >> $LOG_DIR/ohsome-api.log 2>&1

echo "5/7 Running proxyprint"
{ time java -jar target/auto-oas-1.1.0-jar-with-dependencies.jar "$DATASET_DIR/proxyprint-kitchen" "$OUTPUT_DIR/proxyprint-kitchen_oas" ; } >> $LOG_DIR/proxyprint.log 2>&1

echo "6/7 Running quartz-manager"
{ time java -jar target/auto-oas-1.1.0-jar-with-dependencies.jar "$DATASET_DIR/quartz-manager/quartz-manager-parent" "$OUTPUT_DIR/quartz-manager_oas" ; } >> $LOG_DIR/quartz-manager.log 2>&1

echo "7/7 Running ur-codebin"
{ time java -jar target/auto-oas-1.1.0-jar-with-dependencies.jar "$DATASET_DIR/Ur-Codebin-API" "$OUTPUT_DIR/Ur-Codebin-API_oas" ; } >> $LOG_DIR/ur-codebin.log 2>&1

