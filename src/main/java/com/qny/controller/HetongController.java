package com.qny.controller;

import java.util.List;

import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.qny.dao.ItemMapper;
import com.qny.pojo.Apply;
import com.qny.pojo.Checkout;
import com.qny.pojo.Hetong;
import com.qny.pojo.Item;
import com.qny.pojo.User;
import com.qny.pojo.Userlist;
import com.qny.service.ApplyService;
import com.qny.service.CheckoutService;
import com.qny.service.HetongService;
import com.qny.service.ItemService;
import com.qny.service.UserlistService;

@Controller
@RequestMapping("/hetong")
public class HetongController {
	@Autowired
	private HetongService hetongService;
	@Autowired
	private ItemService itemService;
	@Autowired
	private ApplyService applyService;
	@Autowired
	private CheckoutService checkoutService;
	@Autowired
	private UserlistService userlistService;
	@Autowired
	private ItemMapper itemMapper;
	
	/**
	* @author:  qny
	* @methodsName: insertHetong
	* @description: 新增租赁合同信息
	* @param:  itemId: 被租赁的物品id; hetong: 即将插入的合同信息
	* @return: String: 重定向到admin查看所有租赁申请的action
	* @throws: 
	*/
	@RequestMapping("/inserthetong")
	public String insertHetong(Model model, Integer itemId,Hetong hetong,
			                   HttpSession httpSession) {
				
		hetong = hetongService.getApplyByItemId(itemId);
		//1.拿到itemId,查询masterId
		Item item = itemService.findItemId(hetong.getItemId());
		int masterId = item.getMasterId();
		//2.根据masterId查询出租人身份证
		Userlist master = userlistService.findhasuserlist(masterId);
		//3.将出租人身份证写入合同
		hetong.setChuZuIdcard(master.getIdcard());
		//4.填充合同信息并插入合同数据表
		hetong.setZuKeId(hetong.getZuKeId());
		hetong.setChuZuId(item.getMasterId().toString());
		hetongService.inserthetong(hetong);
		Hetong hetong1 = hetongService.findhetong(hetong.getItemId());
		//5.更新物品状态
		Item item2 = itemService.findItemId(hetong1.getItemId());
		item2.setStatus("已租赁");
		itemService.updateItemStatus(item2);
		//7.更改申请状态
		Apply apply = new Apply();
		apply.setItemId(hetong.getItemId());;
		apply.setStatus("已同意");
		applyService.updateApplyStatus(apply);
		//8.重定向到admin查看所有租赁申请的action
		model.addAttribute("info", "zusuccess");
		return "redirect:/findapplylist.action";
	}
	
	//qny
	//用户同意别人的租赁申请
	@RequestMapping("/insertHetongByZuke")
	public String insertHetongByZuke(Model model, Integer itemId,Hetong hetong,
			                         HttpSession httpSession) {
				
		hetong = hetongService.getApplyByItemId(itemId);
		//1.拿到itemId,查询masterId
		Item item = itemService.findItemId(hetong.getItemId());
		int masterId = item.getMasterId();
		//2.根据masterId查询出租人身份证
		Userlist master = userlistService.findhasuserlist(masterId);
		//3.将出租人身份证写入合同
		hetong.setChuZuIdcard(master.getIdcard());
		//4.写入租客UserIistId、出租人UserIistId,新增合同信息
		hetong.setZuKeId(hetong.getZuKeId());
		hetong.setChuZuId(item.getMasterId().toString());
		hetongService.inserthetong(hetong);
		Hetong hetong1 = hetongService.findhetong(hetong.getItemId());
		//5.修改物品状态
		Item item2 = itemService.findItemId(hetong1.getItemId());
		item2.setStatus("已租赁");
		itemService.updateItemStatus(item2);
		//7.更改申请状态
		Apply apply = new Apply();
		apply.setItemId(hetong.getItemId());;
		apply.setStatus("已同意");
		applyService.updateApplyStatus(apply);
		//8.查看所有在租商品
		model.addAttribute("info", "zusuccess");
		return "redirect:/getMyApply.action";
	}	

	@RequestMapping("/seehetong")
	public String seehetong(String itemId, Model model) {
		Hetong hetong = hetongService.findhetong(itemId);
		model.addAttribute("hetong", hetong);
		model.addAttribute("mainPage", "hetong.jsp");
		return "admin/main1";
	}

	@RequestMapping("/updatehetong")
	public String updatehetong(String house_id, Model model) {
		Hetong hetong = hetongService.findhetong(house_id);
		model.addAttribute("hetong", hetong);
		model.addAttribute("mainPage", "updatehetong.jsp");
		return "admin/main1";
	}


	/**
	* @author:  qny
	* @methodsName: deleteHetong
	* @description: admin停止一件在租物品的租赁
	* @param:  itemId: 要停止租赁的物品id
	* @return: String: 重定向到admin查看所有在租物品的action
	* @throws: 
	*/
	@RequestMapping("/deletehetong")
	public String deleteHetong(String itemId, Model model) {
		//1.插入退租数据表
		Hetong hetong = hetongService.getHetongByItemId(Integer.parseInt(itemId));
		Checkout checkout = new Checkout();
		//填充退租信息
		checkout.setItemId(itemId);
		checkout.setItemName(hetong.getItemName());
		checkout.setStatus("已退租");
		checkout.setUserId(Integer.parseInt(hetong.getZuKeId()));
		checkout.setMasterId(Integer.parseInt(hetong.getChuZuId()));
		checkout.setItemNumber(hetong.getItemNumber());
		//正式插入退租数据表
		checkoutService.insertcheckout(checkout);
		//2.更改物品状态
		//这里更新商品Id，通过删除再插入的形式实现
		Item item = new Item();
	    item = itemMapper.findItemId(hetong.getItemId());
		itemMapper.deleteItem(Integer.parseInt(itemId));
		item.setStatus("未租赁");
		item.setItemId(0);
		itemMapper.insertItem(item);
		//3.删除合同
		hetongService.deletehetong(itemId);
		//4.重定向到admin查看所有在租物品的action
		model.addAttribute("info", "checkoutsuccess");
		return "redirect:/hetong/findAllHetong.action";
	}
	
	//qny
	//用户停止租赁自己的物品
	@RequestMapping("/deleteHetongByUser")
	public String deleteHetongByUser(String itemId, Model model) {
		//1.插入已退租列表
		Hetong hetong = hetongService.getHetongByItemId(Integer.parseInt(itemId));
		Checkout checkout = new Checkout();
		checkout.setItemId(itemId);
		checkout.setItemName(hetong.getItemName());
		checkout.setStatus("已退租");
		checkout.setUserId(Integer.parseInt(hetong.getZuKeId()));
		checkout.setMasterId(Integer.parseInt(hetong.getChuZuId()));
		checkout.setItemNumber(hetong.getItemNumber());
		checkoutService.insertcheckout(checkout);
		//2.更改物品状态
		//这里更新商品Id，通过删除再插入的形式实现
		Item item = new Item();
	    item = itemMapper.findItemId(hetong.getItemId());
		itemMapper.deleteItem(Integer.parseInt(itemId));
		item.setStatus("未租赁");
		item.setItemId(0);
		itemMapper.insertItem(item);
		//3.删除合同
		hetongService.deletehetong(itemId);
		//4.跳转到“在租列表”
		model.addAttribute("error", "checkoutsuccess");
		return "redirect:/hetong/findAllHetongByZuke.action";
		
	}
	
	

	@RequestMapping("/zukeseehetong")
	public String zukeseehetong(String itemId, Model model) {
		Hetong hetong = hetongService.findhetong(itemId);
		model.addAttribute("hetong", hetong);
		model.addAttribute("mainPage", "showhetong.jsp");
		return "user/main";
	}

	
	/**
	* @author:  qny
	* @methodsName: findZuList
	* @description: admin查看所有在租物品列表
	* @param:  page: 显示第几页的数据 ；pageSize: 显示几条数据
	* @return: String: 前往admin模块下的在租列表的jsp界面
	* @throws: 
	*/
	@RequestMapping("/findAllHetong")
	public String findZuList(Model model, 
			@RequestParam(required = false, defaultValue = "1") Integer page,
			@RequestParam(required = false, defaultValue = "8") Integer pageSize) throws Exception {
		PageHelper.startPage(page, pageSize);
		//得到所有在租列表的信息
		List<Hetong> hetongs = hetongService.getAllHetong();
		PageInfo<Hetong> p = new PageInfo<Hetong>(hetongs);
		model.addAttribute("p", p);
		model.addAttribute("hetongs", hetongs);
		return "admin/zulist";
	}
	
	
	//qny
	//用户查看自己的所有出租列表	
	@RequestMapping("/findAllHetongByZuke")
	public String findAllHetongByZuke(HttpSession httpSession,Model model, 
			@RequestParam(required = false, defaultValue = "1") Integer page,
			@RequestParam(required = false, defaultValue = "8") Integer pageSize) throws Exception {
		User zuke = (User) httpSession.getAttribute("user");
		PageHelper.startPage(page, pageSize);
		List<Hetong> hetongs = hetongService.getAllHetongByzuke(zuke.getId());
		PageInfo<Hetong> p = new PageInfo<Hetong>(hetongs);
		model.addAttribute("p", p);
		model.addAttribute("hetongs", hetongs);
		return "user/mychuzulist";
	}
}
