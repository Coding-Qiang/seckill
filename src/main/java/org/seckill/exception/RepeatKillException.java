package org.seckill.exception;

/**
 *�ظ���ɱ�쳣���������쳣��
 *����Ҫ�ֶ�try����catch
 *spring����ʽ����ֻ�����������쳣�ع�����
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
