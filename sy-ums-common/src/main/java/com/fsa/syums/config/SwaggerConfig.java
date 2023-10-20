/*
 *  Copyright 2019-2020 Zheng Jie
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package com.fsa.syums.config;

import cn.hutool.core.collection.CollUtil;
import io.swagger.v3.oas.models.ExternalDocumentation;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityScheme;
import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.data.domain.Pageable;

import java.util.ArrayList;
import java.util.List;

/**
 * api页面 /doc.html
 *
 * @author 全栈架构师
 * @date 2023-09-23
 */
@Configuration
public class SwaggerConfig {

    @Value("${jwt.header}")
    private String tokenHeader;

    @Value("${swagger.enabled}")
    private Boolean enabled;

    @Bean
    @SuppressWarnings("all")
    public OpenAPI openAPI() {
        return new OpenAPI()
                .info(apiInfo())
                .externalDocs(new ExternalDocumentation()
                        .description("SpringDoc Wiki Documentation")
                        .url("https://springdoc.org/v2"));
    }

    private Info apiInfo() {
        return new Info()
                .title("sy-ums接口文档")
                .description("springfox swagger 3.0 demo")
                .version("2.0.0")
                .contact(new Contact()
                        .name("<Your name>")
                        .url("<Your url>")
                        .email("<Your email>")
                );
//                .license(new License()
//                        .name("Apache 2.0")
//                        .url("http://www.apache.org/licenses/LICENSE-2.0.txt")
//                );
    }


}

