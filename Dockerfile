FROM tomcat:9.0-jdk17 
RUN rm -rf /usr/local/tomcat/webapps/* 
COPY dist/SeilatsatsiSaRona.war /usr/local/tomcat/webapps/SeilatsatsiSaRona.war 
EXPOSE 8080 
CMD ["catalina.sh", "run"] 
