package com.example.chat.config;

import io.swagger.annotations.ApiOperation;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.Contact;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;


/**
 * Swagger文档的配置
 */
@Configuration
public class SwaggerConfig {

    @Bean
    public Docket createRestApi() {
        return new Docket(DocumentationType.OAS_30)
                .apiInfo(apiInfo())
                .select()
                .apis(RequestHandlerSelectors.basePackage("com.example.chat.controller"))
                .paths(PathSelectors.any())
                .build()
                .groupName("chat");
    }

    private ApiInfo apiInfo() {
        return new ApiInfoBuilder()
                .title("校园论坛接口文档")
                .description("一个成熟的接口文档会自己写接口了")
                .contact(new Contact("unicorn","保密","1437121910@qq.com"))
                .version("1.0")
                .build();
    }
}
