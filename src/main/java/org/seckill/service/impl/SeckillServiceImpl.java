package org.seckill.service.impl;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.MapUtils;
import org.seckill.dao.SeckillDao;
import org.seckill.dao.SuccessKilledDao;
import org.seckill.dao.cache.RedisDao;
import org.seckill.dto.Exposer;
import org.seckill.dto.SeckillExecution;
import org.seckill.entity.Seckill;
import org.seckill.entity.SuccessKilled;
import org.seckill.enums.SeckillStatEnum;
import org.seckill.exception.RepeatKillException;
import org.seckill.exception.SeckillCloseException;
import org.seckill.exception.SeckillException;
import org.seckill.service.SeckillService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.DigestUtils;

@Service
public class SeckillServiceImpl implements SeckillService {

	private Logger logger = LoggerFactory.getLogger(this.getClass());

	// ע��Service����
	@Autowired // @Resource,@Inject
	private SeckillDao seckillDao;

	@Autowired
	private SuccessKilledDao successKilledDao;

	@Autowired
	private RedisDao redisDao;

	// md5��ֵ�ַ��������ڻ���md5
	private final String salt = "#@djkaHdj&@#Ujksdhajkdsad&*#@^$&dsfjhIYAdaa";

	@Override
	public List<Seckill> getSeckillList() {
		return seckillDao.queryAll(0, 4);
	}

	@Override
	public Seckill getById(long seckillId) {
		return seckillDao.queryById(seckillId);
	}

	@Override
	public Exposer exportSeckillUrl(long seckillId) {
		// �Ż��㣺�����Ż�����ʱ�Ļ�����ά��һ����
		// 1.����redis
		Seckill seckill = redisDao.getSeckill(seckillId);
		if (seckill == null) {
			// 2.�������ݿ�
			seckill = seckillDao.queryById(seckillId);
			if (seckill == null) {
				return new Exposer(false, seckillId);
			} else {
				// 3.����redis
				redisDao.putSeckill(seckill);
			}
		}
		Date startTime = seckill.getStartTime();
		Date endTime = seckill.getEndTime();
		// ϵͳ��ǰʱ��
		Date nowTime = new Date();
		if (nowTime.getTime() < startTime.getTime() || nowTime.getTime() > endTime.getTime()) {
			return new Exposer(false, seckillId, nowTime.getTime(), startTime.getTime(), endTime.getTime());
		}
		// ת���ض��ַ����Ĺ��̣�������
		String md5 = getMD5(seckillId);
		return new Exposer(true, md5, seckillId);
	}

	private String getMD5(long seckillId) {
		String base = seckillId + "/" + salt;
		String md5 = DigestUtils.md5DigestAsHex(base.getBytes());
		return md5;
	}

	@Override
	@Transactional
	/**
	 * ʹ��ע��������񷽷����ŵ㣺 
	 * 1.�����ŶӴ��һ�µ�Լ������ȷ��ע���񷽷��ı�̷��
	 * 2.��֤���񷽷���ִ��ʱ�価���̣ܶ���Ҫ���������������RPC/HTTP��������޷����⣬����뵽���񷽷��ⲿ����
	 * 3.�������еķ�������Ҫ������ֻ��һ���޸Ĳ�����ֻ����������Ҫ������ơ����ο��м��������ĵ���
	 */
	public SeckillExecution executeSeckill(long seckillId, long userPhone, String md5)
			throws SeckillException, RepeatKillException, SeckillCloseException {
		// ��ַ�۸���֤
		if (md5 == null || !md5.equals(getMD5(seckillId))) {
			throw new SeckillException("seckill data rewrite");
		}
		// ִ����ɱ�߼�������� + ��¼������Ϊ
		try {
			// ��¼������Ϊ
			int insertCount = successKilledDao.insertSuccessKilled(seckillId, userPhone);
			// Ψһ��֤��(seckillId,userPhone)
			if (insertCount <= 0) {
				// �ظ���ɱ
				throw new RepeatKillException("seckill repeated");
			} else {
				// ����棬�ȵ���Ʒ����
				int updateCount = seckillDao.reduceNumber(seckillId, new Date());
				if (updateCount <= 0) {
					// û�и��µ���¼����ɱ������rollback
					throw new SeckillCloseException("seckill is closed");
				} else {
					logger.info("��ɱ�ɹ���insertCount", insertCount);
					// ��ɱ�ɹ���commit
					SuccessKilled successKilled = successKilledDao.queryByIdWithSeckill(seckillId, userPhone);
					return new SeckillExecution(seckillId, SeckillStatEnum.SUCCESS, successKilled);
				}
			}
		} catch (SeckillCloseException e1) {
			//ֱ���׳�����controler�д�����ͬ
			throw e1;
		} catch (RepeatKillException e2) {
			throw e2;
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			// ���б������쳣ת��Ϊ�������쳣��spring������ʽ�������rollback
			throw new SeckillException("seckill inner error:" + e.getMessage());
		}
	}

	@Override
	public SeckillExecution executeSeckillProcedure(long seckillId, long userPhone, String md5) {
		// ��ַ�۸���֤
		if (md5 == null || !md5.equals(getMD5(seckillId))) {
			return new SeckillExecution(seckillId, SeckillStatEnum.DATA_REWRITE);
		}
		Date killTime = new Date();
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("seckillId", seckillId);
		map.put("phone", userPhone);
		map.put("killTime", killTime);
		map.put("result", null);
		System.out.println(map);
		// ִ�д洢���̣�result����ֵ
		try {
			seckillDao.killByProcedure(map);
			// ��ȡresult
			int result = MapUtils.getInteger(map, "result", -2);
			if (result == 1) {
				SuccessKilled sk = successKilledDao.queryByIdWithSeckill(seckillId, userPhone);
				return new SeckillExecution(seckillId, SeckillStatEnum.SUCCESS, sk);
			} else {
				return new SeckillExecution(seckillId, SeckillStatEnum.stateOf(result));
			}
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			return new SeckillExecution(seckillId, SeckillStatEnum.INNER_ERROR);
		}
	}
}
