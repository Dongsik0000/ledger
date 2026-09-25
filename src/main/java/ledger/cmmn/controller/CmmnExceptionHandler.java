package ledger.cmmn.controller;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import ledger.cmmn.util.Constants;
import ledger.cmmn.util.RequestUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.ErrorResponse;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.ModelAndView;

// 컨트롤러 밖으로 나온 예외를 한곳에서 처리한다. Ajax 는 {"code":"99"} JSON, 화면은 error.jsp.
// 컨트롤러는 try/catch 를 쓰지 않는다 — 예외가 메서드 밖으로 나가야 @Transactional 이 롤백한다.
@ControllerAdvice
public class CmmnExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(CmmnExceptionHandler.class);

    @ExceptionHandler(Exception.class)
    public ModelAndView handle(Exception e, HttpServletRequest request, HttpServletResponse response) {
        int status = statusOf(e);
        if (status >= 500) {
            logger.error("요청 처리 오류: {} {}", request.getMethod(), request.getRequestURI(), e);
        } else {
            logger.warn("잘못된 요청: {} {} ({})", request.getMethod(), request.getRequestURI(), e.getClass().getSimpleName());
        }
        response.setStatus(status);

        if (RequestUtil.isAjax(request)) {
            ModelAndView mav = new ModelAndView("jsonView");
            mav.addObject("code", Constants.FAIL);
            mav.addObject("message", status >= 500 ? "처리 중 오류가 발생했습니다." : "잘못된 요청입니다.");
            return mav;
        }
        request.setAttribute(RequestDispatcher.ERROR_STATUS_CODE, status); // error.jsp 가 상태 코드를 표시
        return new ModelAndView("error");
    }

    // Spring MVC 표준 예외(경로 없음 404, 메서드 불일치 405 …)는 그 상태, 읽을 수 없는 본문은 400, 나머지 500
    private int statusOf(Exception e) {
        if (e instanceof ErrorResponse errorResponse) {
            return errorResponse.getStatusCode().value();
        }
        if (e instanceof HttpMessageNotReadableException) {
            return HttpStatus.BAD_REQUEST.value();
        }
        return HttpStatus.INTERNAL_SERVER_ERROR.value();
    }
}
