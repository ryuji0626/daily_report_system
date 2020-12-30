package jp.example.controllers.login;

import javax.persistence.NoResultException;
import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

import jp.example.config.SecurityData;
import jp.example.forms.LoginForm;
import jp.example.models.Employee;
import jp.example.repositories.EmployeeRepository;
import jp.example.utils.EncryptUtil;

@Controller
public class LoginController {

	@Autowired
	HttpSession session; // セッション

	@Autowired
	SecurityData securityData;

	@Autowired
	EmployeeRepository employeeRepository; // リポジトリ

	@RequestMapping(path = "/login", method = RequestMethod.GET)
	public ModelAndView loginPage(@ModelAttribute LoginForm loginForm, ModelAndView mv) {

		loginForm.set_token(session.getId());
		mv.addObject("hasError", false);
		if (session.getAttribute("flush") != null) {
			mv.addObject("flush", session.getAttribute("flush"));
			session.removeAttribute("flush");
		}

		mv.addObject("login", loginForm);

		mv.setViewName("views/login/login");

		return mv;
	}

	@RequestMapping(path = "/login", method = RequestMethod.POST)
	@Transactional // メソッド開始時にトランザクションを開始、終了時にコミットする
	public ModelAndView loginExec(@ModelAttribute LoginForm loginForm, ModelAndView mv) {
		// 認証結果を格納する変数
		Boolean check_result = false;

		String code = loginForm.getCode();
		String plain_pass = loginForm.getPassword();

		Employee e = null;

		if (code != null && !code.equals("") && plain_pass != null && !plain_pass.equals("")) {
			String password = EncryptUtil.getPasswordEncrypt(
					plain_pass,
					securityData.getPepper());

			// 社員番号とパスワードが正しいかチェックする
			try {
				e = employeeRepository.findByDeleteFlagAndCodeAndPassword(0, code, password);
			} catch (NoResultException ex) {
			}

			if (e != null) {
				check_result = true;
			}
		}

		if (!check_result) {
			// 認証できなかったらログイン画面に戻る
			loginForm.set_token(session.getId());
			loginForm.setPassword("");
			mv.addObject("hasError", true);

			mv.addObject("login", loginForm);
			mv.setViewName("views/login/login");
		} else {
			// 認証できたらログイン状態にしてトップページへリダイレクト
			session.setAttribute("login_employee", e);

			session.setAttribute("flush", "ログインしました。");
			mv = new ModelAndView("redirect:/"); // リダイレクト
		}

		return mv;
	}

	@RequestMapping(path = "/logout", method = RequestMethod.GET)
	public ModelAndView logout(ModelAndView mv) {
		session.removeAttribute("login_employee");
	    session.setAttribute("flush", "ログアウトしました。");

	    mv = new ModelAndView("redirect:/login"); // リダイレクト

		return mv;
	}

}
