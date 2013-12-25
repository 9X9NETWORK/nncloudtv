About this document,

Three sections,

-   Project intro
-   Devel environment and tool: more for Windows
-   EC2: i.e. Ubuntu on EC2

--------------------------------

Project intro
================================

-   This project is based on java spring framework.
-   Installer folder is not part of the main project. It's intended for installation and data migration. 
    1.  ***tools/***, tools for installer script
    2.  ***maintenance/***, maintenance page when system is not available
    3.  ***migration/***, data migration tools to port data from gae to mysql

Devel environment and tool
================================

First time setup procedures
--------------------------------

Detail for each step can be found in the following sections.

-   zh_TW.UTF-8 locale support

        :::bash
        locale-gen --lang zh_TW.UTF-8

-   Download JDK 1.6.x, MySql 5.5.10, RabbitMQ 1.8, Maven 3.0.5  
    Details can be found in the following sections

-   Start MySql server

-   Start RabbitMQ server

-   Create databases and tables  
    Reference *mysql/README.md*

-   Modify datanucleus.properties files,  
    there are four, this is to make sure your db connection is setup correctly

-   Run nnqueue client  
    Reference README in nnqueue project

-   Run memcache server

-   datanucleus jdo enhance:  
    Go to project root folder,

        :::bash
        mvn datanucleus:enhance # (it might not be necessary)

-   Run nncloudtv on Jetty:  
    Go to project root folder,

        mvn jetty:run
        nohup mvn jetty:run > ~/jetty.log 2>&1

        # c:\Java\projects\nncloudtv> mvn -Dslf4j=false -Dlog4j.configuration=file:./target/classes/log4j.properties jetty:run

-   Basic tests,

        :::bash
        # http://localhost:8080/hello/world                      (test your servlet and spring dispatcher)
        #
        # http://localhost:8080/hello/pdr                        (write a db record)
        #
        # http://localhost:8080/hello/cache_set                  (test memcache component)
        #
        # http://localhost:8080/hello/fanout?exchange_name=hello (test rabbitq, nnqueue should output a hello message)

-   ready to go,  
    go to http://localhost:8080/admin/index, click on initialize link

mvn
--------------------------------

-   Download 3.0.5

    > http://maven.apache.org/download.html 

-   ubuntu 

        :::bash
        wget http://linux-files.com/maven/binaries/apache-maven-3.0.5-bin.tar.gz
        tar -xzvf apache-maven-3.0.5-bin.tar.gz
        sudo mkdir /usr/local/apache-maven
        sudo cp -R apache-maven-3.0.5 /usr/local/apache-maven/
        export PATH=/usr/local/apache-maven/apache-maven-3.0.5/bin:$PATH

    ps. add this line to the end of /home/ubuntu/.bashrc

        :::bash
        mvn --version

-   read pom.xml

-   generate javadoc

        :::bash
        mvn javadoc:javadoc
        sudo rm -rf /var/www/9x9/apidocs
        sudo mv ~/files/nncloudtv/doc/apidocs /var/www/9x9/

Datanucleus tool
--------------------------------

datanucleus commands:

-   enhance

        :::bash
        mvn datanucleus:enhance

-   Schema tool

        :::bash
        mvn datanucleus:schema-create

Note:

1.  You might run into command length limitation error if running on Windows. Remove everything under java except model folder if it's the case.
2.  Currently there is four databases need to be initiated. Depending on the db you are going to initiate, change configuration in datanucleus plugin section in pom.xml before you run schema-create
3.  Alternatively, reference *mysql/README.rd* file

Jetty
--------------------------------

-   Run your application on jetty
    go to project root folder,  

        :::bash
        mvn jetty:run

RabbitMQ
--------------------------------

-   Windows download

    > http://www.rabbitmq.com/releases/bundles/v1.7.2/

-   installation quick guide

    1.  unzip the package
    2.  install erlang 5.7.4 using 'otp_win32_R13B03.exe'
    3.  set ERLANG_HOME in environment variable  
        (move folder rabbitmq-server-windows-1.7.2 to program files)

-   commands examples:

        :::bash
        cd "C:\Program Files (x86)\RabbitMQ\rabbitmq_server-1.7.2\sbin"
        # double click "rabbitmq-server.bat" to start the server
        rabbitmqctl list_exchanges
        rabbitmqctl list_queues

MySql
--------------------------------

-   Download MySQL Server 5.5

-   example commands on Windows:

        :::bash
        cd "c:\Program Files\MySQL\MySQL Server 5.5\bin"
        mysqld
        mysql nncloudtv_analytics -h localhost -u root -p
        mysql nncloudtv_content -h localhost -u root -p
        mysql nncloudtv_nnuser1 -h localhost -u root -p
        mysql nncloudtv_nnuser2 -h localhost -u root -p
        mysql --default-character-set=utf8 -u root -p

-   please reference *mysql/README.md* file

-   dump data example

        :::bash
        mysqldump --user=root nncloudtv_analytics > nnanalytics.sql

-   import data example

        :::bash
        cat nnanalytics.sql | mysql nncloudtv_analytics -h localhost -u root -p

Package
--------------------------------

-   packaging nncloudtv war procedures:

    1.  modify datanucleus.properties files, there are four.

    2.  packaging:

            :::bash
            mvn clean:clean
            mvn compile
            mvn datanucleus:enhance
            mvn compile war:war

    3.  test the packaged war with jetty plugin:

            :::bash
            mvn jetty:deploy-war

        packaging and run jetty with jetty plugin:

            :::bash
            mvn jetty:run-war

-   packaging nnqueue jar:

    reference nnqueue.README

Note
--------------------------------

-   javascript and image resources should be placed on amazon s3.  
    use account **nncloudtv@gmail.com** to access

EC2
================================

-   to restart the instance

    > http://channelwatch.9x9.tv/ec2/

Jetty
--------------------------------

-   installation

        :::bash
        apt-get install jetty
        apt-get install libjetty-extra
        apt-get install openjdk-6-jdk

-   configuration

    1.  modify /etc/default/jetty
    2.  change value of ***NO_START*** to ***NO_START=0***
    3.  change value of ***JETTY_HOST*** TO **JETTY_HOST=0.0.0.0**

-   start/stop jetty

        :::bash
        service jetty start
        service jetty stop

-   jetty version

        :::bash
        cd /usr/share/jetty 
        java -jar start.jar --version
        # null 6.1.22

-   if v=6.1.24, bug fix

        :::bash
        vi /etc/init.d/jetty

    replace

        :::bash
        CONFIG_LINES=$(cat /etc/jetty/jetty.conf | grep -v "^[:space:]*#" | tr "\n" " ")

    with this one:

        :::bash
        CONFIG_LINES=$(cat /etc/jetty/jetty.conf | grep -v "^[[:space:]]*#" | tr "\n" " ")

-   deploy app: deploy your war to webapps under jetty 

    > /usr/share/jetty/webapps

-   remove **root/** folder if default has one

MySql
--------------------------------

-   installation

        :::bash
        apt-get install mysql-server-5.1
        apt-get install mysql-server-5.5

-   commands

        :::bash
        service mysql start

-   permission

    Grant permission for remote application access if application server is on a different machine

        :::sql
        CREATE USER 'root'@'%.compute.internal' IDENTIFIED BY 'password';
        GRANT ALL ON *.* TO 'root'@'%.compute.internal';
 
RabbitMQ
--------------------------------

-   installation

        :::bash
        apt-cache search rabbitmq
        apt-get install rabbitmq-server

-   commands:

        :::bash
        rabbitmqctl status

Memcached
--------------------------------

    :::bash
    apt-get install memcached
    service memcached start
    telnet localhost 11211

    #
    # Trying 127.0.0.1...
    # Connected to localhost.
    # Escape character is '^]'.
    #
    # stats
    #

    vi /etc/memcached.conf 

Mail server
--------------------------------

    :::bash
    apt-get install postfix

-   jetty6 is loading wrong gnumail.jar, fixes:

    1.  edit **/etc/jetty/start.config**:  
        comment out the use of gnumail and activation.jar  
        doesn't actually take effect, but just to be sure for future use.

    2.  remove following files(gnumail*.jar) under /usr/share/java/:

        > gnumail.jar, gnumail-1.1.2.jar, gnumail-providers.jar, gnumail-providers-1.1.2.jar

    3.  copy following files to /usr/share/jetty/lib:

        > mail-1.4.jar, activation-1.1.jar

    4. upgrade to jetty 7 should be able to solve the problem as well.

Running the app on EC2
--------------------------------

-   build war

    1.  change datanucleus property files if necessary
    2.  check port setting on server if ncessary

            :::bash
            netstat -vatn

-   upload the war and the jar: use SCP with your private key, and upload to the server

        :::bash
        pscp -i awsdev.ppk nncloudtv-0.0.1-SNAPSHOT.war "ubuntu@ec2-174-129-141-179.compute-1.amazonaws.com:/home/ubuntu/files"
        pscp -i prod-west2.ppk nncloudtv-0.0.1-SNAPSHOT.war "ubuntu@ec2-50-112-111-245.us-west-2.compute.amazonaws.com:/home/ubuntu/files"

-   login name: *** ubuntu ***

-   change hostname if necessary

        :::bash
        sudo hostname devel # /etc/hostname

-   commands:

        :::bash
        # EX1
        sudo su
        cd /usr/share/jetty/webapps
        service jetty stop
        rm root.war
        cp /home/ubuntu/files/nncloudtv-0.0.1-SNAPSHOT.war .
        mv nncloudtv-0.0.1-SNAPSHOT.war root.war
        service jetty start

        # EX2
        sudo su
        cd /usr/share/jetty/webapps && service jetty stop && rm root.war && cp /home/ubuntu/files/nncloudtv-0.0.1-SNAPSHOT.war . && mv nncloudtv-0.0.1-SNAPSHOT.war root.war && service jetty start

        # EX3
        bash -x ./installer/installer.sh

    EX1, EX2, and EX3 are equavalent, but EX3 is recommended.

        :::bash
        # test RabbitMQ
        java -jar nnqueue.jar hello
        java -jar nnqueue.jar brand_counter
        java -jar nnqueue.jar category_create
        java -jar nnqueue.jar channel_create_related

-   testing on java02,

        :::bash
        wget http://localhost:8080/hello/world # (local test)

        # http://ec2-174-129-141-179.compute-1.amazonaws.com:8080/hello/world                      (basic web server test)
        # http://ec2-174-129-141-179.compute-1.amazonaws.com:8080/hello/pdr                        (write to db test)
        # http://ec2-174-129-141-179.compute-1.amazonaws.com:8080/hello/cache_set                  (test memcache component)
        # http://ec2-174-129-141-179.compute-1.amazonaws.com:8080/hello/cache_get                  (test memcache component)
        # http://ec2-174-129-141-179.compute-1.amazonaws.com:8080/hello/fanout?exchange_name=hello (rabbit queue client test)

-   run on port 80 (port 80 might be closed, check ec2 console setting)

    +   test

            :::bash
            sudo /sbin/iptables -t nat -I PREROUTING -p tcp --dport 80 -j REDIRECT --to-port 8080

    +   permanent

            :::bash
            # (create a new file iptables.rules)
            vi /etc/iptables.rules

            # (add one line to interfaces file)
            vi /etc/network/interfaces

            pre-up iptables-restore < /etc/iptables.rules

-   stop apache2 (if u ever need it)

        :::bash
        sudo /etc/init.d/apache2 stop

-   for server with monit, stop monit before manually restart your server

        :::bash
        service monit stop

SSH
--------------------------------

1.  combine key and cert file into PKCS12 format

        :::bash
        openssl pkcs12 -inkey alpha.9x9.tv_private.key -in alpha.9x9.tv_ssl.cert -export -out jetty.pkcs12

2.  Generate keystore file (Java Keytool stores the keys and certificates in what is called a keystore)

        :::bash
        keytool -importkeystore -srckeystore jetty.pkcs12 -srcstoretype PKCS12 -destkeystore keystore

3.  copy all the key files to the directory

    > /usr/share/jetty/keys

3.  edit **jetty-ssl.xml**

        :::bash
        vi /etc/jetty/jetty-ssl.xml

    edit keystore location and password, you can reference the file on alpha or it's checked in here,

4.  edit **jetty.conf**

        :::bash
        vi /etc/jetty/jetty.conf

    add line,

    > /etc/jetty/jetty-ssl.xml

5.  modify iptables

        :::bash
        sudo /sbin/iptables -t nat -I PREROUTING -p tcp --dport 443 -j REDIRECT --to-port 8443

    make sure there are only two ruls in iptables

        :::bash
        sudo iptables -t nat -n -L

        #
        # Chain PREROUTING (policy ACCEPT)
        # target prot opt source destination
        # REDIRECT tcp -- 0.0.0.0/0 0.0.0.0/0 tcp dpt:443 redir ports 8443
        # REDIRECT tcp -- 0.0.0.0/0 0.0.0.0/0 tcp dpt:80 redir ports 8080
        #

5.  modify **/etc/iptables.rules**

ps. jetty configuration files are also checked in here,  
https://bitbucket.org/9x9group/nncloudtv/src/master/installer/jetty/

Others
--------------------------------

*(no contents here)*

