package com.nova.server.handler;

import com.nova.server.domain.GameRequest;
import com.nova.server.domain.GameResponse;

/** 
* @project:		WSK
* @Title:		GameHandler.java
* @Package:		com.nova.server.handler
  @author: 		zhangxx
* @email: 		395295759@qq.com
* @date:		2015年8月20日 下午2:25:51 
* @description:
* @version:
*/

public abstract interface GameInitHandler
{
  public abstract void execute(GameRequest paramGameRequest, GameResponse paramGameResponse);
}
