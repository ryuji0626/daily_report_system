package jp.example.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class SecurityData {

	/** ペッパー番号 */
	@Value("${pepper}")
	private String pepper;

	public String getPepper() {
		return pepper;
	}

	public void setPepper(String pepper) {
		this.pepper = pepper;
	}

}
