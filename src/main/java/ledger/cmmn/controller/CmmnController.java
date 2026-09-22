package ledger.cmmn.controller;

import jakarta.servlet.http.HttpSession;
import ledger.cmmn.util.SessionUtil;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class CmmnController {

    @GetMapping("/login")
    public String login(HttpSession session) {
        if (SessionUtil.getUserId(session) != null) {
            return "redirect:/ledger/dashboard";
        }
        return "login/loginMain";
    }

    @GetMapping("/signup")
    public String signup() {
        return "login/signupMain";
    }

    @GetMapping("/logout")
    public String logout(HttpSession session) {
        SessionUtil.logout(session);
        return "redirect:/login";
    }
}
