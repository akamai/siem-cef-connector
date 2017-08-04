#!/bin/bash

CEFConnector_start() {
    echo "Starting Akamai CEF Connector"
    java -Dfile.encoding=UTF-8 -Xmx2048m -Xms2048m \
            -DCEFConnector.kill.mark.dummy=CEFCONNECTOR.KILL.MARK \
            -jar ${artifactId}-${version}.jar 1>>/dev/null 2>>/dev/null &
    echo "Akamai CEF Connector started"
}

CEFConnector_stop() {
    [ -z "$PID" ] && { echo "Akamai CEF Connector is not running"; exit 1; }
    echo "Stopping Akamai CEF Connector, pid: $PID..."
    kill -9 $PID 1>/dev/null 2>/dev/null && { echo "Akamai CEF Connector killed"; } || { echo "Could not kill Akamai CEF Connector"; }
}

CEFConnector_pid() {
    PID=`ps ax | grep -e"CEFCONNECTOR.KILL.MARK" | grep -v "grep" | awk '{printf(substr($0,1,6))}'`
    [ -z "$PID" ] && return 1;
    return 0;
}
CEFConnector_reset() {
    [ -e "cefconnector.db" ] && rm "cefconnector.db"
}

SCRIPT_NAME=`basename $0`

case "$1" in
   start)
        CEFConnector_pid
        [ -z "$PID" ] || { echo "Akamai CEF Connector is already running, pid: $PID"; exit 1; }
        CEFConnector_start
        sleep 1
        CEFConnector_pid
        [ -z "$PID" ] && { echo "Akamai CEF Connector is not running"; exit 1; }
        echo "Akamai CEF Connector is running, pid: $PID"
        exit $?
    ;;

    stop)
        CEFConnector_pid
        CEFConnector_stop
        exit $?
    ;;
    
    status)
        CEFConnector_pid
        [ -z "$PID" ] && { echo "Akamai CEF Connector is not running"; exit 1; }
        echo "Akamai CEF Connector is running, pid: $PID"
    ;;

    resetdb)
        CEFConnector_pid
        [ -z "$PID" ] || { echo "Akamai CEF Connector is running, pid: $PID"; echo "Please stop the connector before resetting the db"; exit 1; }
        CEFConnector_reset
        echo "Akamai CEF Connector db has been reset"
    ;;
    *)
        echo "Usage: $SCRIPT_NAME { start | stop | status | resetdb }"
        exit 1;;
esac
