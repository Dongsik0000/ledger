package ledger.cmmn.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

// 헬스체크(Dockerfile HEALTHCHECK·deploy.sh): DB 연결까지 되면 200. 세션을 만들지 않는다.
// 연결 실패(SQLException)는 CmmnExceptionHandler 가 500 으로 응답한다
@Controller
public class HealthController {

    @Autowired
    private DataSource dataSource;

    @GetMapping("/health")
    public ResponseEntity<String> health() throws SQLException {
        try (Connection connection = dataSource.getConnection()) {
            return connection.isValid(2)
                    ? ResponseEntity.ok("ok")
                    : ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body("db");
        }
    }
}
