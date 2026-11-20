package ledger.auth.dao;

import jakarta.annotation.Resource;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

// 인증 SQL 호출. 매퍼: sqlmap/mappers/ledger/auth/auth.xml
@Repository("authDAO")
public class AuthDAO {

    @Resource(name = "sqlSession-ledger-postgre")
    private SqlSessionTemplate sqlSession;

    public static final String SQL_PATH = "ledger.auth.dao.AuthDAO";

    // 아이디로 사용자(id·username·password_hash). 없으면 null
    public Map<String, Object> selectUserByUsername(String username) {
        return sqlSession.selectOne(SQL_PATH + ".selectUserByUsername", username);
    }

    // 사용자 기록 삭제·초기화 문장(매퍼 id)을 순서대로 실행. 호출한 서비스의 트랜잭션 안에서 돈다
    public void deleteUserData(List<String> statements, long userId) {
        for (String id : statements) {
            sqlSession.update(SQL_PATH + "." + id, userId);
        }
    }

    // 비밀번호 해시
    public String selectPasswordHash(long userId) {
        return sqlSession.selectOne(SQL_PATH + ".selectPasswordHash", userId);
    }

    // param: userId, passwordHash
    public int updatePassword(Map<String, Object> param) {
        return sqlSession.update(SQL_PATH + ".updatePassword", param);
    }

    // param: username, passwordHash. 실행 후 param.id 에 생성된 키가 들어간다
    public int insertUser(Map<String, Object> param) {
        return sqlSession.insert(SQL_PATH + ".insertUser", param);
    }

    // param.id 사용자에게 기본 카테고리 10개
    public int insertDefaultCategories(Map<String, Object> param) {
        return sqlSession.insert(SQL_PATH + ".insertDefaultCategories", param);
    }

    // param.id 사용자에게 기본 결제수단 4개
    public int insertDefaultPaymentMethods(Map<String, Object> param) {
        return sqlSession.insert(SQL_PATH + ".insertDefaultPaymentMethods", param);
    }
}
