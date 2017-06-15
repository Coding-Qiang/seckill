package org.seckill.service;

import java.util.List;

import org.seckill.dto.Exposer;
import org.seckill.dto.SeckillExecution;
import org.seckill.entity.Seckill;
import org.seckill.exception.RepeatKillException;
import org.seckill.exception.SeckillCloseException;
import org.seckill.exception.SeckillException;

/**
 *业务接口：站在“使用者”的角度设计接口
 *	1.方法定义粒度明确
 *	2.参数简练
 *	3.返回类型友好（return/exception）
 */
public interface SeckillService {
	
	/**
	 * 查询所有秒杀记录
	 */
	List<Seckill> getSeckillList();
	
	/**
	 * 查询单个秒杀记录
	 */
	Seckill getById(long seckillId);
	
	/**
	 * 秒杀开启时输出秒杀接口地址
	 * 否则输出系统时间和秒杀时间
	 */
	Exposer exportSeckillUrl(long seckillId);
	
	/**
	 * 执行秒杀操作
	 */
	SeckillExecution executeSeckill(long seckillId,long userPhone,String md5) 
			throws SeckillException,RepeatKillException,SeckillCloseException;
	
	/**
	 * 执行秒杀操作 by存储过程
	 */
	SeckillExecution executeSeckillProcedure(long seckillId,long userPhone,String md5);
	
}
