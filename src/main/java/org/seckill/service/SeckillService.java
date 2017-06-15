package org.seckill.service;

import java.util.List;

import org.seckill.dto.Exposer;
import org.seckill.dto.SeckillExecution;
import org.seckill.entity.Seckill;
import org.seckill.exception.RepeatKillException;
import org.seckill.exception.SeckillCloseException;
import org.seckill.exception.SeckillException;

/**
 *ҵ��ӿڣ�վ�ڡ�ʹ���ߡ��ĽǶ���ƽӿ�
 *	1.��������������ȷ
 *	2.��������
 *	3.���������Ѻã�return/exception��
 */
public interface SeckillService {
	
	/**
	 * ��ѯ������ɱ��¼
	 */
	List<Seckill> getSeckillList();
	
	/**
	 * ��ѯ������ɱ��¼
	 */
	Seckill getById(long seckillId);
	
	/**
	 * ��ɱ����ʱ�����ɱ�ӿڵ�ַ
	 * �������ϵͳʱ�����ɱʱ��
	 */
	Exposer exportSeckillUrl(long seckillId);
	
	/**
	 * ִ����ɱ����
	 */
	SeckillExecution executeSeckill(long seckillId,long userPhone,String md5) 
			throws SeckillException,RepeatKillException,SeckillCloseException;
	
	/**
	 * ִ����ɱ���� by�洢����
	 */
	SeckillExecution executeSeckillProcedure(long seckillId,long userPhone,String md5);
	
}
