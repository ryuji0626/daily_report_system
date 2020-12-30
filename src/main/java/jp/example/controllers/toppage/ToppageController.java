package jp.example.controllers.toppage;

import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import jp.example.models.Employee;
import jp.example.models.Report;
import jp.example.repositories.ReportRepository;

@Controller
public class ToppageController {

	@Autowired
	HttpSession session; // セッション

	@Autowired
	ReportRepository reportRepository; // リポジトリ

	/**
	 *
	 * @param page
	 * @param mv
	 * @return
	 */
	@RequestMapping(path = "/", method = RequestMethod.GET)
	public ModelAndView index(@RequestParam(name = "page", required = false) Integer page, ModelAndView mv) {
		Employee login_employee = (Employee)session.getAttribute("login_employee");
		if (page == null) {
			page = 1;
		}
		// ログインユーザーの日報
		Page<Report> reports = reportRepository.findByEmployee(login_employee, PageRequest.of(15 * (page - 1), 15, Sort.by("id").descending()));
		// ログインユーザー全件のカウント
		long reports_count = reportRepository.countByEmployee(login_employee);

		mv.addObject("reports", reports);
		mv.addObject("reports_count", reports_count);
		mv.addObject("page", page);

		if(session.getAttribute("flush") != null) {
	        mv.addObject("flush", session.getAttribute("flush"));
	        session.removeAttribute("flush");
	    }

		mv.setViewName("views/toppage/index");

		return mv;
	}


}
