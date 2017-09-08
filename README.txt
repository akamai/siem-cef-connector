CEF Connector ${project.version} Configuration for Akamai SIEM

System RequirementsAkamai’s CEF Connector requires Sun JRE 1.8+ to be installed. The latest JRE can be downloaded from the Sun Java site (Java Platform, Standard Edition) or installed from a software distribution package on Linux. Verify Java version (Linux): Use the command "java -version" to verify that your JRE is installed and available.

Hardware Requirements
This application is designed to run on a Linux server with at least:•	2 CPU cores•	6GB RAM•	2GB Free Disk Space•	Run a Linux Kernel greater than 2.6 

Installation Instructions

Retrieve the latest CEFConnector distribution package from the Akamai Support Page and transfer the package using either Linux command “wget http://server/CEFConnector-1.0.zip” or using SFTP (SSH File Transfer Protocol).
Unzip the distribution package anywhere on the file system. You can install Unzip from a software distribution package on Linux (for example “yum install unzip”).
To install a service, create a symbolic link to bin/AkamaiCEFConnector.sh shell script in /etc/init.d. 

You can execute the shell script with the following commands (start | stop | status | resetdb). Resetdb will delete cefconnector.db which contains the last successful offset. Removing the file will cause the connector to process offset=NULL as long as timebased is false. If timebased is true and to parameter is null, a new offset will be saved after the first successful pull.