cd "${0%/*}"

cd ..

export GPG_TTY=$(tty)

read -p "==== please input version: " version

if [[ -z "${version}" ]]; then
    echo "==== version is incorrect ===="
    exit
fi

echo "==== starting to deploy ===="

mvn versions:set -DnewVersion=${version}
#mvn versions:update-child-modules
mvn versions:commit

rm -rf ~/.m2/repository/net/dloud/

echo "==== starting to deploy maven-plugin ===="

mvn deploy -N

mvn clean deploy -DskipTests -pl gateway-info,platform-maven-plugin

echo "==== deploying maven-plugin ===="