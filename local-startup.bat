mvn clean install -DskipTests
docker pull fhirfactory/pegacorn-base-docker-wildfly:1.0.0
docker build --rm --build-arg IMAGE_BUILD_TIMESTAMP="%date% %time%" -t pegacorn-fhirplace-bigdata-api:1.0.0-snapshot --file Dockerfile .
helm upgrade pegacorn-fhirplace-bigdata-api-site-a --install --namespace site-a --set serviceName=pegacorn-fhirplace-bigdata-api,imagePullPolicy=Never,basePort=32410,basePortInsidePod=8443,hostPathCerts=/data/certificates,imageTag=1.0.0-snapshot,jvmMaxHeapSizeMB=768,wildflyLogLevel=INFO,javaxNetDebug=none,wildflyAdminUser=admin,wildflyAdminPwd=Pega@dm1n,numOfPods=1 helm
