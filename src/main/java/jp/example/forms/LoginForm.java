package jp.example.forms;

public class LoginForm {
	private String _token;
	private String code;
	private String password;

	public String get_token() {
		return _token;
	}

	public void set_token(String _token) {
		this._token = _token;
	}

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}
}
