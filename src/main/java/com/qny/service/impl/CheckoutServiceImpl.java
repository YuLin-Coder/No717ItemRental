package com.qny.service.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.qny.dao.CheckoutMapper;
import com.qny.pojo.Checkout;
import com.qny.service.CheckoutService;

@Service
@Transactional
public class CheckoutServiceImpl implements CheckoutService {

	@Autowired
	private CheckoutMapper checkoutMapper;

	@Override
	public void insertcheckout(Checkout checkout) {
		checkoutMapper.insertcheckout(checkout);

	}

	//qny
	//查询所有退租申请
	@Override
	public List<Checkout> getallcheckout() {
		List<Checkout> checkout = checkoutMapper.getallcheckout();
		return checkout;
	}

	//qny
	//根据已退租数据表id删除已退租数据
	@Override
	public void deletecheckout(Integer cId) {
		checkoutMapper.deletecheckout(cId);
	}

	//qny
	//用户查看自己物品的已退租列表
	@Override
	public List<Checkout> getAllCheckoutByUser(Integer userId) {
		return checkoutMapper.getAllCheckoutByUser(userId);
	}

	//qny
	//根据已退租的信息编号更新评价状态为“已评价”
	@Override
	public void updateEvaStatusById(Integer cId) {
		checkoutMapper.updateEvaStatusById(cId);
		
	}

	/**   
	 * @title: deleteCheckOutByIds
	 * @description: 用户批量删除已退租列表
	 * @param ids   列表id数组
	 * @see com.qny.service.CheckoutService#deleteCheckOutByIds(java.lang.Integer[])     
	 */ 
	@Override
	public void deleteCheckOutByIds(Integer[] ids) {
		checkoutMapper.deleteCheckOutByIds(ids);
	}
	


}
