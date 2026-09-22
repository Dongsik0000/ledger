# ledger WAS — Spring 6 / MyBatis / Java 17 / Tomcat 10.1
#
#   docker compose up -d --build
#
# 빌드 단계에서 Maven 으로 war 를 만들고 실행 단계의 Tomcat 에 p3.war 로 넣는다.

# ── 빌드 ──────────────────────────────────────────────
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /build

# 의존성 먼저 받아 레이어로 굳힌다 — 소스만 바뀌면 이 단계는 캐시된다.
COPY pom.xml .
RUN mvn -B -q dependency:go-offline

COPY src ./src
RUN mvn -B clean package -DskipTests

# ── 실행 ──────────────────────────────────────────────
FROM tomcat:10.1-jdk17-temurin

# 포트 8081, 서버에서는 loopback 에만 바인딩 (docker-compose 의 CATALINA_OPTS 로 지정)
ENV CATALINA_OPTS="-Dtomcat.address=0.0.0.0"
RUN sed -i 's/<Connector port="8080"/<Connector address="${tomcat.address}" port="8081"/' /usr/local/tomcat/conf/server.xml

# 기본 웹앱(manager, examples) 제거 — 운영에 불필요하고 공격면만 넓힌다
RUN rm -rf /usr/local/tomcat/webapps/*

# SameSite 쿠키 설정을 /p3 컨텍스트에 적용 (nginx 가 /p3/ 로 프록시하므로 ROOT 가 아닌 p3 로 배포)
COPY src/main/webapp/META-INF/context.xml /usr/local/tomcat/conf/Catalina/localhost/p3.xml
COPY --from=build /build/target/ledger.war /usr/local/tomcat/webapps/p3.war

# 스케줄러(매일 00:05)가 한국 시간 기준으로 돌도록 타임존 고정
ENV TZ=Asia/Seoul \
    JAVA_OPTS="-Xms256m -Xmx512m -Duser.timezone=Asia/Seoul -Djava.security.egd=file:/dev/./urandom"

EXPOSE 8081

HEALTHCHECK --interval=30s --timeout=5s --start-period=90s --retries=3 \
    CMD curl -fsS http://localhost:8081/p3/login > /dev/null || exit 1

CMD ["catalina.sh", "run"]
