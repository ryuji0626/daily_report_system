package jp.example.controllers.employees;

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

import jp.example.config.SecurityData;
import jp.example.forms.EmployeeForm;
import jp.example.models.Employee;
import jp.example.models.validators.EmployeeValidator;
import jp.example.repositories.EmployeeRepository;
import jp.example.utils.EncryptUtil;

@Controller
public class EmployeeController {

	@Autowired
	HttpSession session; // セッション

	@Autowired
	EmployeeRepository employeeRepository; // リポジトリ

	@Autowired
	SecurityData securityData;

	@Autowired
	EmployeeValidator validator;

	/**
	 * employeesIndex.
	 * 
	 * @param page ページ番号
	 * @param mv
	 * @return
	 */
	@RequestMapping(path = "/employees/index", method = RequestMethod.GET)
	public ModelAndView employeesIndex(@RequestParam(name = "page", required = false) Integer page, ModelAndView mv) {

		if (page == null) {
			page = 1;
		}

		// 15件
		Page<Employee> employees = employeeRepository
				.findAll(PageRequest.of(15 * (page - 1), 15, Sort.by("id").descending()));
		// 全件のカウント
		long employees_count = employeeRepository.count();

		mv.addObject("employees", employees);
		mv.addObject("employees_count", employees_count);
		// mv.addObject("page_count", ((employees_count - 1) / 15) + 1);
		mv.addObject("page", page);

		if (session.getAttribute("flush") != null) {
			mv.addObject("flush", session.getAttribute("flush"));
			session.removeAttribute("flush");
		}

		mv.setViewName("views/employees/index");

		return mv;
	}

	/**
	 * employeesNew.
	 * 
	 * @param employeeForm 従業員フォーム
	 * @param mv
	 * @return
	 */
	@RequestMapping(path = "/employees/new", method = RequestMethod.GET)
	public ModelAndView employeesNew(@ModelAttribute EmployeeForm employeeForm, ModelAndView mv) {

		employeeForm.setToken(session.getId());

		mv.addObject("employee", employeeForm);

		mv.setViewName("views/employees/new");

		return mv;
	}

	/**
	 * employeesCreate.
	 * 
	 * @param employeeForm 従業員フォーム
	 * @param mv
	 * @return
	 */
	@RequestMapping(path = "/employees/create", method = RequestMethod.POST)
	@Transactional // メソッド開始時にトランザクションを開始、終了時にコミットする
	public ModelAndView employeesCreate(@ModelAttribute EmployeeForm employeeForm, ModelAndView mv) {

		if (employeeForm.getToken() != null && employeeForm.getToken().equals(session.getId())) {

			Employee e = new Employee();

			e.setCode(employeeForm.getCode());
			e.setName(employeeForm.getName());
			e.setAdminFlag(employeeForm.getAdminFlag());

			e.setPassword(
					EncryptUtil.getPasswordEncrypt(
							employeeForm.getPassword(),
							securityData.getPepper()));

			Timestamp currentTime = new Timestamp(System.currentTimeMillis());
			e.setCreated_at(currentTime);
			e.setUpdated_at(currentTime);
			e.setDeleteFlag(0);

			List<String> errors = validator.validate(e, true, true);

			if (errors.size() > 0) {

				employeeForm.setToken(session.getId());
				mv.addObject("employee", employeeForm);
				mv.addObject("errors", errors);
				mv.setViewName("views/employees/new");
			} else {
				employeeRepository.save(e);
				session.setAttribute("flush", "登録が完了しました。");

				mv = new ModelAndView("redirect:/employees/index"); // リダイレクト
			}
		} else {
			mv.setViewName("views/common/error"); // 真っ白な画面は嫌なので、エラー画面を出す
		}

		return mv;
	}

	@RequestMapping(path = "/employees/show", method = RequestMethod.GET)
	public ModelAndView employeesShow(@ModelAttribute EmployeeForm employeeForm, ModelAndView mv) {

		Optional<Employee> e = employeeRepository.findById(employeeForm.getId());

		// ModelMapperでEntity→Formオブジェクトへマッピング
		ModelMapper modelMapper = new ModelMapper();
		employeeForm = modelMapper.map(e.orElse(new Employee()), EmployeeForm.class);

		mv.addObject("employee", employeeForm);

		mv.setViewName("views/employees/show");

		return mv;
	}

	@RequestMapping(path = "/employees/edit", method = RequestMethod.GET)
	public ModelAndView employeesEdit(@ModelAttribute EmployeeForm employeeForm, ModelAndView mv) {

		Optional<Employee> e = employeeRepository.findById(employeeForm.getId());
		// ModelMapperでEntity→Formオブジェクトへマッピング
		ModelMapper modelMapper = new ModelMapper();
		employeeForm = modelMapper.map(e.orElse(new Employee()), EmployeeForm.class);

		employeeForm.setToken(session.getId());

		mv.addObject("employee", employeeForm);

		session.setAttribute("employee_id", employeeForm.getId());

		mv.setViewName("views/employees/edit");

		return mv;
	}

	@RequestMapping(path = "/employees/update", method = RequestMethod.POST)
	@Transactional // メソッド開始時にトランザクションを開始、終了時にコミットする
	public ModelAndView employeesUpdate(@ModelAttribute EmployeeForm employeeForm, ModelAndView mv) {

		if (employeeForm.getToken() != null && employeeForm.getToken().equals(session.getId())) {

			Optional<Employee> ope = employeeRepository.findById((Integer) session.getAttribute("employee_id"));
			Employee e = ope.orElse(null);

			// 現在の値と異なる社員番号が入力されていたら
			// 重複チェックを行う指定をする
			Boolean codeDuplicateCheckFlag = true;
			if (e.getCode().equals(employeeForm.getCode())) {
				codeDuplicateCheckFlag = false;
			} else {
				e.setCode(employeeForm.getCode());
			}

			// パスワード欄に入力があったら
			// パスワードの入力値チェックを行う指定をする
			Boolean passwordCheckFlag = true;
			String password = employeeForm.getPassword();
			if (password == null || password.equals("")) {
				passwordCheckFlag = false;
			} else {
				e.setPassword(
						EncryptUtil.getPasswordEncrypt(
								password,
								securityData.getPepper()));
			}

			e.setName(employeeForm.getName());
			e.setAdminFlag(employeeForm.getAdminFlag());
			e.setUpdated_at(new Timestamp(System.currentTimeMillis()));
			e.setDeleteFlag(0);

			List<String> errors = validator.validate(e, codeDuplicateCheckFlag, passwordCheckFlag);
			if (errors.size() > 0) {

				employeeForm.setToken(session.getId());
				mv.addObject("employee", employeeForm);
				mv.addObject("errors", errors);
				mv.setViewName("views/employees/edit");
			} else {

				employeeRepository.save(e);
				session.setAttribute("flush", "更新が完了しました。");
				session.removeAttribute("employee_id");
				mv = new ModelAndView("redirect:/employees/index"); // リダイレクト
			}
		} else {
			mv.setViewName("views/common/error"); // 真っ白な画面は嫌なので、エラー画面を出す
		}

		return mv;
	}

	@RequestMapping(path = "/employees/destroy", method = RequestMethod.POST)
	@Transactional // メソッド開始時にトランザクションを開始、終了時にコミットする
	public ModelAndView employeesDestroy(@ModelAttribute EmployeeForm employeeForm, ModelAndView mv) {

		if (employeeForm.getToken() != null && employeeForm.getToken().equals(session.getId())) {

			Optional<Employee> ope = employeeRepository.findById((Integer) session.getAttribute("employee_id"));
			Employee e = ope.orElse(null);

			e.setDeleteFlag(1);

			employeeRepository.save(e);
			session.setAttribute("flush", "削除が完了しました。");
			session.removeAttribute("employee_id");
			mv = new ModelAndView("redirect:/employees/index"); // リダイレクト

		} else {
			mv.setViewName("views/common/error"); // 真っ白な画面は嫌なので、エラー画面を出す
		}

		return mv;
	}
}
