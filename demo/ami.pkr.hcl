variable "aws_region" {
  type    = string
  default = "us-west-2"
}
variable "source_ami" {
  type    = string
  default = "ami-0efcece6bed30fd98" # Ubuntu 22 (64-bit (x86))
}
variable "ssh_username" {
  type    = string
  default = "ubuntu"
}
variable "subnet_id" {
  type    = string
  default = "subnet-04885b32aa1bb325d"
}
variable "profile" {
  type    = string
  default = "devuser"
}
variable "aws_devuser" {
  type    = string
  default = "878402828635"
}

variable "aws_demouser" {
  type    = string
  default = "286957373320"
}

source "amazon-ebs" "my-ami" {
  region          = "${var.aws_region}"
   profile         = "${var.profile}"
  ami_name        = "csye6225Ami_${formatdate("YYYY_MM_DD_hh_mm_ss", timestamp())}"
  ami_description = "AMI for test CSYE 6225"
  ami_regions = [
    "us-west-2",
    "us-west-1",
  ]
    ami_users = [
    "${var.aws_devuser}",
    "${var.aws_demouser}",
  ]

  aws_polling {
    delay_seconds = 120
    max_attempts  = 50
  }

  instance_type = "t2.micro"
  source_ami    = "${var.source_ami}"
  ssh_username  = "${var.ssh_username}"
  subnet_id     = "${var.subnet_id}"

  launch_block_device_mappings {
    delete_on_termination = true
    device_name           = "/dev/xvda"
    volume_size           = 25
    volume_type           = "gp2"
  }
}

build {
  sources = ["source.amazon-ebs.my-ami"]
  
   provisioner "file" {
    source = "demo/target/demo-0.0.1-SNAPSHOT.jar"
    destination = "/home/ubuntu/"
  }

  provisioner "shell" {
    environment_vars = [
      "DEBIAN_FRONTEND=noninteractive",
      "CHECKPOINT_DISABLE=1"
    ]
     inline = [
    "sudo apt-get update",
    "sudo apt-get upgrade -y",
    "sudo apt-get clean",
    "sudo apt update",
    "sudo apt-get install openjdk-17-jdk -y",
    "export TOMCAT_VERSION=10.1.15",
    "sudo groupadd --system tomcat",
    "sudo useradd -d /usr/share/tomcat -r -s /bin/false -g tomcat tomcat",
    "sudo wget https://downloads.apache.org/tomcat/tomcat-10/v$TOMCAT_VERSION/bin/apache-tomcat-$TOMCAT_VERSION.tar.gz",
    "sudo tar xvf apache-tomcat-$TOMCAT_VERSION.tar.gz -C /usr/share/",
    "sudo ln -s /usr/share/apache-tomcat-$TOMCAT_VERSION/ /usr/share/tomcat",
    "sudo chown -R tomcat:tomcat /usr/share/tomcat",
    "sudo chown -R tomcat:tomcat /usr/share/apache-tomcat-$TOMCAT_VERSION/",
    "echo -e '[Unit]\nDescription=Tomcat Server\nAfter=syslog.target network.target\n\n[Service]\nType=forking\nUser=tomcat\nGroup=tomcat\n\nEnvironment=JAVA_HOME=/usr/lib/jvm/java-17-openjdk-amd64\nEnvironment=JAVA_OPTS=-Djava.awt.headless=true\nEnvironment=CATALINA_HOME=/usr/share/tomcat\nEnvironment=CATALINA_BASE=/usr/share/tomcat\nEnvironment=CATALINA_PID=/usr/share/tomcat/temp/tomcat.pid\nEnvironment=CATALINA_OPTS=-Xms512M -Xmx1024M -server -XX:+UseParallelGC\n\nExecStart=/usr/share/tomcat/bin/startup.sh\nExecStop=/usr/share/tomcat/bin/shutdown.sh\n\n[Install]\nWantedBy=multi-user.target\n' | sudo tee /etc/systemd/system/tomcat.service",
    "sudo systemctl daemon-reload",
    "sudo systemctl start tomcat.service",
    "sudo systemctl enable tomcat.service",
    "sudo systemctl status tomcat.service",
    "sudo ufw allow 8080/tcp",
    "sudo apt-get install nginx -y",
    "sudo apt-cache search mysql-server",
    "sudo apt info -a mysql-server-8.0",
    "sudo apt-get install mysql-server-8.0 -y",
    "sudo systemctl is-enabled mysql.service",
    "sudo systemctl start mysql.service",
    "sudo systemctl status mysql.service",
    "export pwd=Test1234",
    "echo $pwd",
   "sudo mysql -uroot -p$pwd --connect-expired-password -e \"ALTER USER 'root'@'localhost' IDENTIFIED WITH 'mysql_native_password' BY 'Test1234'\"",
    "sudo systemctl restart mysql.service",
    "sudo systemctl status mysql.service",
    "sudo mysql -uroot -pTest1234 -e \"CREATE DATABASE IF NOT EXISTS restDemo\"",
    "sudo apt install maven -y",
    "sudo mkdir -p /opt/webapps",
    "sudo chmod 755 /opt/webapps",
    "sudo cp /home/ubuntu/demo-0.0.1-SNAPSHOT.jar /opt/webapps/.",
    "cd /opt/webapps",
    "ls -lrt",
    "sudo chmod 755 demo-0.0.1-SNAPSHOT.jar"
  ]
}


}