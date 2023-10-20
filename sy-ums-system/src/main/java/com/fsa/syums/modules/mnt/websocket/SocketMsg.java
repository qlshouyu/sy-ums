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
package com.fsa.syums.modules.mnt.websocket;

import lombok.Data;

/**
 * @author 全栈架构师
 * @date 2023-08-10 9:55
 */
@Data
public class SocketMsg {
	private String msg;
	private MsgType msgType;

	public SocketMsg(String msg, MsgType msgType) {
		this.msg = msg;
		this.msgType = msgType;
	}
}
