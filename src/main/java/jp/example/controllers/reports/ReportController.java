package jp.example.controllers.reports;

import java.sql.Date;
import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;

import javax.servlet.http.HttpSession;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import jp.example.forms.ReportForm;
import jp.example.models.Employee;
import jp.example.models.Report;
import jp.example.models.validators.ReportValidator;
import jp.example.repositories.ReportRepository;

@Controller
public class ReportController {

	@Autowired
	HttpSession session; // セッション

	@Autowired
	ReportRepository reportRepository; // リポジトリ

	@Autowired
	ReportValidator validator; // バリデーター

	@RequestMapping(path = "/reports/index", method = RequestMethod.GET)
	public ModelAndView reportsIndex(@RequestParam(name = "page", required = false) Integer page, ModelAndView mv) {

		if (page == null) {
			page = 1;
		}

		// 15件
		Page<Report> reports = reportRepository.findAll(PageRequest.of(15 * (page - 1), 15, Sort.by("id").descending()));
		// 全件のカウント
		long reports_count = reportRepository.count();

		mv.addObject("reports", reports);
		mv.addObject("reports_count", reports_count);
		mv.addObject("page", page);

		if (session.getAttribute("flush") != null) {
			mv.addObject("flush", session.getAttribute("flush"));
			session.removeAttribute("flush");
		}

		mv.setViewName("views/reports/index");

		return mv;
	}

	@RequestMapping(path = "/reports/new", method = RequestMethod.GET)
	public ModelAndView reportsNew(@ModelAttribute ReportForm reportForm, ModelAndView mv) {

		reportForm.setToken(session.getId());
		reportForm.setReportDate(new Date(System.currentTimeMillis()));

		mv.addObject("report", reportForm);

		mv.setViewName("views/reports/new");

		return mv;
	}

	/**
	 *
	 * @param reportForm
	 * @param mv
	 * @return
	 */
	@RequestMapping(path = "/reports/create", method = RequestMethod.POST)
	@Transactional // メソッド開始時にトランザクションを開始、終了時にコミットする
	public ModelAndView reportsCreate(@ModelAttribute ReportForm reportForm, ModelAndView mv) {

		if (reportForm.getToken() != null && reportForm.getToken().equals(session.getId())) {

			Report r = new Report();
            r.setEmployee((Employee)session.getAttribute("login_employee"));

            Date report_date = new Date(System.currentTimeMillis());
            if(reportForm.getReportDate() != null) {
                report_date =reportForm.getReportDate();
            }
            r.setReportDate(report_date);

            r.setTitle(reportForm.getTitle());
            r.setContent(reportForm.getContent());

			Timestamp currentTime = new Timestamp(System.currentTimeMillis());
			r.setCreated_at(currentTime);
			r.setUpdated_at(currentTime);

			List<String> errors = validator.validate(r);

			if (errors.size() > 0) {
				reportForm.setToken(session.getId());
				mv.addObject("report", reportForm);
				mv.addObject("errors", errors);
				mv.setViewName("views/reports/new");
			} else {
				reportRepository.save(r);
				session.setAttribute("flush", "登録が完了しました。");

				mv = new ModelAndView("redirect:/reports/index"); // リダイレクト
			}
		} else {
			mv.setViewName("views/common/error"); // 真っ白な画面は嫌なので、エラー画面を出す
		}

		return mv;
	}

	@RequestMapping(path = "/reports/show", method = RequestMethod.GET)
	public ModelAndView reportsShow(@ModelAttribute ReportForm reportForm, ModelAndView mv) {

		Optional<Report> r = reportRepository.findById(reportForm.getId());

		// ModelMapperでEntity→Formオブジェクトへマッピング
		ModelMapper modelMapper = new ModelMapper();
		reportForm = modelMapper.map(r.orElse(new Report()), ReportForm.class);

		mv.addObject("report", reportForm);

		mv.setViewName("views/reports/show");

		return mv;
	}

	@RequestMapping(path = "/reports/edit", method = RequestMethod.GET)
	public ModelAndView reportsEdit(@ModelAttribute ReportForm reportForm, ModelAndView mv) {

		Optional<Report> e = reportRepository.findById(reportForm.getId());
		// ModelMapperでEntity→Formオブジェクトへマッピング
		ModelMapper modelMapper = new ModelMapper();
		reportForm = modelMapper.map(e.orElse(new Report()), ReportForm.class);

		reportForm.setToken(session.getId());

		mv.addObject("report", reportForm);

		session.setAttribute("report_id", reportForm.getId());

		mv.setViewName("views/reports/edit");

		return mv;
	}

	@RequestMapping(path = "/reports/update", method = RequestMethod.POST)
	@Transactional // メソッド開始時にトランザクションを開始、終了時にコミットする
	public ModelAndView reportsUpdate(@ModelAttribute ReportForm reportForm, ModelAndView mv) {

		if (reportForm.getToken() != null && reportForm.getToken().equals(session.getId())) {

			Optional<Report> opr = reportRepository.findById((Integer) session.getAttribute("report_id"));
			Report r = opr.orElse(null);
			r.setReportDate(reportForm.getReportDate());
            r.setTitle(reportForm.getTitle());
            r.setContent(reportForm.getContent());
            r.setUpdated_at(new Timestamp(System.currentTimeMillis()));

			List<String> errors = validator.validate(r);
			if (errors.size() > 0) {
				reportForm.setToken(session.getId());
				mv.addObject("report", reportForm);
				mv.addObject("errors", errors);
				mv.setViewName("views/reports/edit");
			} else {

				reportRepository.save(r);
				session.setAttribute("flush", "更新が完了しました。");
				session.removeAttribute("report_id");
				mv = new ModelAndView("redirect:/reports/index"); // リダイレクト
			}
		} else {
			mv.setViewName("views/common/error"); // 真っ白な画面は嫌なので、エラー画面を出す
		}

		return mv;
	}

}
