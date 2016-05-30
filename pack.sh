#!/usr/bin/env bash

set -e
OUTPUT=$(./gradlew info)
declare -a lines=()
IFS=$'\r\n' mapfile -t lines <<< "$OUTPUT"


VERSION=${lines[2]}
PROJECT_NAME=${lines[1]}
PACKAGE_CATALOG=${PROJECT_NAME}-${VERSION}
JAR_NAME="${PACKAGE_CATALOG}.jar"


# build project
./gradlew clean assemble

# create tmp catalog
mkdir ${PACKAGE_CATALOG}

# files to package
cp manifest.yml ${PACKAGE_CATALOG}
cp --parents build/libs/${JAR_NAME} ${PACKAGE_CATALOG}

# prepare build manifest
echo "commit_sha=$(git rev-parse HEAD)" > ${PACKAGE_CATALOG}/build_info.ini

# create zip package
cd ${PACKAGE_CATALOG}
zip -r ../${PROJECT_NAME}-${VERSION}.zip *
cd ..

# remove tmp catalog
rm -r ${PACKAGE_CATALOG}

echo "Zip package for $PROJECT_NAME project in version $VERSION has been prepared."