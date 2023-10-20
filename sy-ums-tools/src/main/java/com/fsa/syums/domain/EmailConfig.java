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
package com.fsa.syums.domain;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import jakarta.persistence.*;
import javax.validation.constraints.NotBlank;
import java.io.Serializable;

/**
 * 邮件配置类，数据存覆盖式存入数据存
 * @author 全栈架构师
 * @date 2023-10-20
 */
@Entity
@Data
@Table(name = "tool_email_config")
public class EmailConfig implements Serializable {

    @Id
    @Column(name = "config_id")
    @Schema(title = "ID", hidden = true)
    private Long id;

    @NotBlank
    @Schema(title = "邮件服务器SMTP地址")
    private String host;

    @NotBlank
    @Schema(title = "邮件服务器 SMTP 端口")
    private String port;

    @NotBlank
    @Schema(title = "发件者用户名")
    private String user;

    @NotBlank
    @Schema(title = "密码")
    private String pass;

    @NotBlank
    @Schema(title = "收件人")
    private String fromUser;
}
