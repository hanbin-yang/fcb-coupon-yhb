FROM java:8
VOLUME /tmp
ADD fcb-coupon-backend.jar app.jar
ENTRYPOINT ["java","-Djava.security.egd=file:/dev/./urandom","-Duser.timezone=Asia/Shanghai","-jar","/app.jar"]
