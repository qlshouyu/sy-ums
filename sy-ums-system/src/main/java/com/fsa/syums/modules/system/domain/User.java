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
package com.fsa.syums.modules.system.domain;

import com.fsa.syums.base.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import jakarta.persistence.*;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.Date;
import java.util.Objects;
import java.util.Set;

/**
 * @author 全栈架构师
 * @date 2023-09-22
 */
@Entity
@Getter
@Setter
@Table(name="sys_user")
public class User extends BaseEntity implements Serializable {

    @Id
    @Column(name = "user_id")
    @NotNull(groups = Update.class)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(title = "ID", hidden = true)
    private Long id;

    @ManyToMany(fetch = FetchType.EAGER)
    @Schema(title = "用户角色")
    @JoinTable(name = "sys_users_roles",
            joinColumns = {@JoinColumn(name = "user_id",referencedColumnName = "user_id")},
            inverseJoinColumns = {@JoinColumn(name = "role_id",referencedColumnName = "role_id")})
    private Set<Role> roles;

    @ManyToMany(fetch = FetchType.EAGER)
    @Schema(title = "用户岗位")
    @JoinTable(name = "sys_users_jobs",
            joinColumns = {@JoinColumn(name = "user_id",referencedColumnName = "user_id")},
            inverseJoinColumns = {@JoinColumn(name = "job_id",referencedColumnName = "job_id")})
    private Set<Job> jobs;

    @OneToOne
    @JoinColumn(name = "dept_id")
    @Schema(title = "用户部门")
    private Dept dept;

    @NotBlank
    @Column(unique = true)
    @Schema(title = "用户名称")
    private String username;

    @NotBlank
    @Schema(title = "用户昵称")
    private String nickName;

    @Email
    @NotBlank
    @Schema(title = "邮箱")
    private String email;

    @NotBlank
    @Schema(title = "电话号码")
    private String phone;

    @Schema(title = "用户性别")
    private String gender;

    @Schema(title = "头像真实名称",hidden = true)
    private String avatarName;

    @Schema(title = "头像存储的路径", hidden = true)
    private String avatarPath;

    @Schema(title = "密码")
    private String password;

    @NotNull
    @Schema(title = "是否启用")
    private Boolean enabled;

    @Schema(title = "是否为admin账号", hidden = true)
    private Boolean isAdmin = false;

    @Column(name = "pwd_reset_time")
    @Schema(title = "最后修改密码的时间", hidden = true)
    private Date pwdResetTime;

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        User user = (User) o;
        return Objects.equals(id, user.id) &&
                Objects.equals(username, user.username);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, username);
    }
}