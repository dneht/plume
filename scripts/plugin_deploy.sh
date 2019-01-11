cd "${0%/*}"

cd ..

export GPG_TTY=$(tty)

rm -rf ~/.m2/repository/net/dloud/

echo "==== starting to deploy maven-plugin ===="

mvn deploy -N

mvn clean deploy -DskipTests -pl gateway-info,platform-maven-plugin

echo "==== deploying maven-plugin ===="