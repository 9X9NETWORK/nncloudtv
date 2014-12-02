#!/bin/sh
#
# installer.sh - build & deploy for develpement/test site
#

script_path=`readlink -f "$0"`
script_dir=`dirname "$script_path"`

cd "$script_dir"

echo
echo "     *****************************"
echo "   **                          *"
echo " **    9x9 DevOps Installer  **"
echo "   **                          *"
echo "     *****************************"
echo

cd ..

mvn -Dmaven.test.skip=true \
    install:install-file -Dfile=./lib/CcxClientApi.jar -DgroupId=com.clearcommerce -DartifactId=clear-commerce -Dversion=5.10.0.3706 -Dpackaging=jar \
    compile \
    datanucleus:enhance \
    install war:war \
&& cp -v target/root.war $JETTY_HOME/webapps/ && jetty.sh restart

