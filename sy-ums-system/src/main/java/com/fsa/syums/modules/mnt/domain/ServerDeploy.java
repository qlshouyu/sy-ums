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
package com.fsa.syums.modules.mnt.domain;

import com.fsa.syums.base.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.bean.copier.CopyOptions;
import lombok.Getter;
import lombok.Setter;
import jakarta.persistence.*;
import java.io.Serializable;
import java.util.Objects;

/**
* @author 全栈架构师
* @date 2023-08-24
*/
@Entity
@Getter
@Setter
@Table(name="mnt_server")
public class ServerDeploy extends BaseEntity implements Serializable {

    @Id
    @Column(name = "server_id")
    @Schema(title = "ID", hidden = true)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Schema(title = "服务器名称")
    private String name;

    @Schema(title = "IP")
    private String ip;

    @Schema(title = "端口")
    private Integer port;

    @Schema(title = "账号")
    private String account;

    @Schema(title = "密码")
    private String password;

    public void copy(ServerDeploy source){
        BeanUtil.copyProperties(source,this, CopyOptions.create().setIgnoreNullValue(true));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ServerDeploy that = (ServerDeploy) o;
        return Objects.equals(id, that.id) &&
                Objects.equals(name, that.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name);
    }
}
