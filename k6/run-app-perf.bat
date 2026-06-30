@echo off
REM agora_perf(MySQL 5만건)로 앱 기동 - 데이터 유지(ddl-auto=none)
cd /d %~dp0\..
set SPRING_APPLICATION_JSON={"spring.datasource.url":"jdbc:mysql://localhost:3306/agora_perf","spring.datasource.username":"root","spring.datasource.password":"12345678","spring.datasource.driver-class-name":"com.mysql.cj.jdbc.Driver","spring.jpa.hibernate.ddl-auto":"none","spring.jpa.properties.hibernate.dialect":"org.hibernate.dialect.MySQLDialect","spring.flyway.enabled":false,"spring.sql.init.mode":"never","agora.redisson.enabled":false,"agora.chat.redis-listener.enabled":false}
echo [agora] 앱 기동 중... "Started AgoraApplication" 뜨면 준비 완료. 이 창은 켜둔 채 두세요.
call gradlew.bat bootRun
