package jp.example;

import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import jp.example.filters.EncodingFilter;
import jp.example.filters.LoginFilter;

@Configuration
public class DailyReportSystemFilter {

    @SuppressWarnings({ "rawtypes", "unchecked" })
	@Bean
    public FilterRegistrationBean encodingFilter() {
        // FilterRegistrationBeanに格納される
        FilterRegistrationBean bean = new FilterRegistrationBean(new EncodingFilter());
        // <url-pattern/>
        bean.addUrlPatterns("/*");

        bean.setOrder(1);

        return bean;
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
	@Bean
    public FilterRegistrationBean loginFilter() {
        // FilterRegistrationBeanに格納される
        FilterRegistrationBean bean = new FilterRegistrationBean(new LoginFilter());
        // <url-pattern/>
        bean.addUrlPatterns("/*");

        bean.setOrder(2);

        return bean;
    }
}
