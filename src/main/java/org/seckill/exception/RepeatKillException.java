package org.seckill.exception;

/**
 *重复秒杀异常（运行期异常）
 *不需要手动try……catch
 *spring声明式事务只接收运行期异常回滚策略
 */
@SuppressWarnings("serial")
public class RepeatKillException extends SeckillException {


	public RepeatKillException(String message) {
		super(message);
	}
	
	public RepeatKillException(String message, Throwable cause) {
		super(message, cause);
	}
}
