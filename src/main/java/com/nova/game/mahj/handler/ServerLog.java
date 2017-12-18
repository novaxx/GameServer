package com.nova.game.mahj.handler;

import org.slf4j.LoggerFactory;

import nova.common.game.mahjong.handler.GameLogger.LoggerHandler;

public class ServerLog implements LoggerHandler {

	private String mTag = "game_server";
	private static final boolean DEBUG = true;
	
	public ServerLog(String logTag) {
		mTag = logTag;
	}
	
	public void i(String tag, String msg) {
		if (DEBUG) {
			LoggerFactory.getLogger(getClass()).info(mTag + " : " + tag + "-" + msg);
		}
	}
	
	public void d(String tag, String msg) {
		if (DEBUG) {
			LoggerFactory.getLogger(getClass()).debug(mTag + " : " + tag + "-" + msg);
		}
	}

	public void e(String tag, String msg) {
		LoggerFactory.getLogger(getClass()).error(mTag + " : " + tag + "-" + msg);
	}

	@Override
	public void debug(String tag, String msg) {
		d(tag, msg);
	}

	@Override
	public void error(String tag, String msg) {
		e(tag, msg);
	}

	@Override
	public void info(String tag, String msg) {
		i(tag, msg);
	}
}
