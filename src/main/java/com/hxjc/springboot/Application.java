package com.hxjc.springboot;



import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;

import org.springframework.boot.autoconfigure.SpringBootApplication;



@SpringBootApplication
//@EnableDubboConfiguration  //开启dubbo的自动配置  开启dubbo配置支持
@MapperScan("com/hxjc/springboot/mapper")
public class Application {

	public static void main(String[] args) {
		SpringApplication.run(Application.class, args);
	}

}
