# Update and upgrade the system
sudo apt-get update
sudo apt-get upgrade -y
sudo apt-get clean
sudo apt update

# Install OpenJDK 17
sudo apt-get install openjdk-17-jdk -y

# Tomcat
export TOMCAT_VERSION=10.0.27
sudo groupadd --system tomcat
sudo useradd -d /usr/share/tomcat -r -s /bin/false -g tomcat tomcat
sudo wget https://downloads.apache.org/tomcat/tomcat-10/v$TOMCAT_VERSION/bin/apache-tomcat-$TOMCAT_VERSION.tar.gz
sudo tar xvf apache-tomcat-$TOMCAT_VERSION.tar.gz -C /usr/share/
sudo ln -s /usr/share/apache-tomcat-$TOMCAT_VERSION/ /usr/share/tomcat
sudo chown -R tomcat:tomcat /usr/share/tomcat
sudo chown -R tomcat:tomcat /usr/share/apache-tomcat-$TOMCAT_VERSION/

# Create a systemd service for Tomcat
echo -e "[Unit]\nDescription=Tomcat Server\nAfter=syslog.target network.target\n\n[Service]\nType=forking\nUser=tomcat\nGroup=tomcat\n\nEnvironment='JAVA_HOME=/usr/lib/jvm/java-17-openjdk-amd64'\nEnvironment='JAVA_OPTS=-Djava.awt.headless=true'\nEnvironment='CATALINA_HOME=/usr/share/tomcat'\nEnvironment='CATALINA_BASE=/usr/share/tomcat'\nEnvironment='CATALINA_PID=/usr/share/tomcat/temp/tomcat.pid'\nEnvironment='CATALINA_OPTS=-Xms512M -Xmx1024M -server -XX:+UseParallelGC'\n\nExecStart=/usr/share/tomcat/bin/startup.sh\nExecStop=/usr/share/tomcat/bin/shutdown.sh\n\n[Install]\nWantedBy=multi-user.target\n" | sudo tee /etc/systemd/system/tomcat.service

# Reload systemd configuration and start Tomcat service
sudo systemctl daemon-reload
sudo systemctl start tomcat.service

# Enable Tomcat service to start on boot
sudo systemctl enable tomcat.service

# Check the status of the Tomcat service
sudo systemctl status tomcat.service

# Open port 8080 for Tomcat in the firewall
sudo ufw allow 8080/tcp

# Install Nginx
sudo apt-get install nginx -y



# Install MySQL Server
sudo apt-get install mysql-server-8.0 -y
sudo systemctl is-enabled mysql.service
sudo systemctl start mysql.service
sudo systemctl status mysql.service

# Configure MySQL and create the database
export mysql_user=root
export mysql_pwd=Test@1234
echo $mysql_pwd

sudo mysql -uroot -p$mysql_pwd -e "ALTER USER 'root'@'localhost' IDENTIFIED WITH mysql_native_password BY '$mysql_pwd'"
sudo systemctl restart mysql.service
sudo systemctl status mysql.service

# Create the database
sudo mysql -uroot -p$mysql_pwd -e "CREATE DATABASE IF NOT EXISTS restDemo"

sudo apt install maven -y


# Create directories for your application
sudo cd
sudo pwd
sudo ls -lrt
sudo mkdir -p /opt/webapps
sudo chmod 755 /opt/webapps

sudo pwd
sudo ls -lrt

sudo pwd
sudo cp /home/ubuntu/demo1-0.0.1-SNAPSHOT.jar /opt/webapps/.

cd /opt/webapps
sudo pwd
ls -lrt

sudo chmod 755 demo1-0.0.1-SNAPSHOT.jar

