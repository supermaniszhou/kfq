/**
 * $Author: muj $
 * $Rev: 4543 $
 * $Date:: 2013-02-26 14:19:29#$:
 *
 * Copyright (C) 2012 Seeyon, Inc. All rights reserved.
 *
 * This software is the proprietary information of Seeyon, Inc.
 * Use is subject to license terms.
 */

package com.seeyon.ctp.common.template.manager;


import com.seeyon.apps.collaboration.po.ColSummary;
import com.seeyon.cap4.form.api.FormApi4Cap4;
import com.seeyon.cap4.form.modules.component.ComparatorCtpTemplate;
import com.seeyon.ctp.cap.api.bean.CAPFormBean;
import com.seeyon.ctp.cap.api.manager.CAPFormManager;
import com.seeyon.ctp.common.AppContext;
import com.seeyon.ctp.common.ModuleType;
import com.seeyon.ctp.common.appLog.manager.AppLogManager;
import com.seeyon.ctp.common.authenticate.domain.User;
import com.seeyon.ctp.common.constants.ApplicationCategoryEnum;
import com.seeyon.ctp.common.content.affair.constants.StateEnum;
import com.seeyon.ctp.common.content.mainbody.MainbodyManager;
import com.seeyon.ctp.common.content.mainbody.MainbodyService;
import com.seeyon.ctp.common.content.mainbody.MainbodyType;
import com.seeyon.ctp.common.ctpenumnew.EnumNameEnum;
import com.seeyon.ctp.common.ctpenumnew.manager.EnumManager;
import com.seeyon.ctp.common.exceptions.BusinessException;
import com.seeyon.ctp.common.filemanager.manager.AttachmentManager;
import com.seeyon.ctp.common.i18n.ResourceUtil;
import com.seeyon.ctp.common.permission.manager.PermissionManager;
import com.seeyon.ctp.common.po.content.CtpContentAll;
import com.seeyon.ctp.common.po.ctpenumnew.CtpEnumBean;
import com.seeyon.ctp.common.po.template.CtpTemplate;
import com.seeyon.ctp.common.po.template.CtpTemplateAuth;
import com.seeyon.ctp.common.po.template.CtpTemplateCategory;
import com.seeyon.ctp.common.po.template.CtpTemplateConfig;
import com.seeyon.ctp.common.po.template.CtpTemplateHistory;
import com.seeyon.ctp.common.po.template.CtpTemplateOrg;
import com.seeyon.ctp.common.po.template.TemplateApprovePO;
import com.seeyon.ctp.common.processassets.manager.ProcessAssetsManager;
import com.seeyon.ctp.common.quartz.MutiQuartzJobNameException;
import com.seeyon.ctp.common.quartz.NoSuchQuartzJobBeanException;
import com.seeyon.ctp.common.quartz.QuartzHolder;
import com.seeyon.ctp.common.supervise.manager.SuperviseManager;
import com.seeyon.ctp.common.supervise.vo.SuperviseSetVO;
import com.seeyon.ctp.common.taglibs.functions.Functions;
import com.seeyon.ctp.common.template.dao.TemplateApproveDao;
import com.seeyon.ctp.common.template.dao.TemplateDao;
import com.seeyon.ctp.common.template.enums.Approve;
import com.seeyon.ctp.common.template.enums.TemplateCategoryConstant;
import com.seeyon.ctp.common.template.enums.TemplateChooseScope;
import com.seeyon.ctp.common.template.enums.TemplateEnum;
import com.seeyon.ctp.common.template.enums.TemplateEnum.DataType;
import com.seeyon.ctp.common.template.enums.TemplateEnum.searchCondtion;
import com.seeyon.ctp.common.template.enums.TemplateTypeEnums;
import com.seeyon.ctp.common.template.event.TemplateDeleteEvent;
import com.seeyon.ctp.common.template.po.CtpTemplateRecent;
import com.seeyon.ctp.common.template.quartz.TemplatePublishQuartz;
import com.seeyon.ctp.common.template.util.CtpTemplateUtil;
import com.seeyon.ctp.common.template.util.TemplateUtil;
import com.seeyon.ctp.common.template.vo.SimpleTemplate;
import com.seeyon.ctp.common.template.vo.TemplateBO;
import com.seeyon.ctp.common.template.vo.TemplateCategoryTreeVO;
import com.seeyon.ctp.common.template.vo.TemplateDetailVO;
import com.seeyon.ctp.common.template.vo.TemplateTreeVo;
import com.seeyon.ctp.common.template.vo.TemplateVO;
import com.seeyon.ctp.common.template.vo.TempleteCategorysWebModel;
import com.seeyon.ctp.cycle.enums.CycleEnum;
import com.seeyon.ctp.cycle.enums.HourEnum;
import com.seeyon.ctp.datasource.CtpDynamicDataSource;
import com.seeyon.ctp.datasource.annotation.DataSourceName;
import com.seeyon.ctp.datasource.annotation.ProcessInDataSource;
import com.seeyon.ctp.event.EventDispatcher;
import com.seeyon.ctp.form.api.FormApi4Cap3;
import com.seeyon.ctp.form.bean.FormBean;
import com.seeyon.ctp.form.bean.FormBindAuthBean;
import com.seeyon.ctp.form.bean.FormBindBean;
import com.seeyon.ctp.form.util.Enums.FormType;
import com.seeyon.ctp.form.util.FormConstant;
import com.seeyon.ctp.organization.OrgConstants;
import com.seeyon.ctp.organization.OrgConstants.ORGENT_TYPE;
import com.seeyon.ctp.organization.OrgConstants.Role_NAME;
import com.seeyon.ctp.organization.bo.MemberPost;
import com.seeyon.ctp.organization.bo.V3xOrgAccount;
import com.seeyon.ctp.organization.bo.V3xOrgDepartment;
import com.seeyon.ctp.organization.bo.V3xOrgEntity;
import com.seeyon.ctp.organization.bo.V3xOrgLevel;
import com.seeyon.ctp.organization.bo.V3xOrgMember;
import com.seeyon.ctp.organization.bo.V3xOrgPost;
import com.seeyon.ctp.organization.bo.V3xOrgRole;
import com.seeyon.ctp.organization.bo.V3xOrgTeam;
import com.seeyon.ctp.organization.dao.OrgHelper;
import com.seeyon.ctp.organization.event.DeleteAccountEvent;
import com.seeyon.ctp.organization.manager.OrgManager;
import com.seeyon.ctp.organization.manager.OrgManagerDirect;
import com.seeyon.ctp.organization.manager.RoleManager;
import com.seeyon.ctp.portal.section.util.SectionUtils;
import com.seeyon.ctp.util.BeanUtils;
import com.seeyon.ctp.util.CommonTools;
import com.seeyon.ctp.util.DBAgent;
import com.seeyon.ctp.util.DateUtil;
import com.seeyon.ctp.util.Datetimes;
import com.seeyon.ctp.util.FlipInfo;
import com.seeyon.ctp.util.ParamUtil;
import com.seeyon.ctp.util.SQLWildcardUtil;
import com.seeyon.ctp.util.StringUtil;
import com.seeyon.ctp.util.Strings;
import com.seeyon.ctp.util.UUIDLong;
import com.seeyon.ctp.util.UniqueList;
import com.seeyon.ctp.util.XMLCoder;
import com.seeyon.ctp.util.annotation.AjaxAccess;
import com.seeyon.ctp.util.annotation.CheckRoleAccess;
import com.seeyon.ctp.util.annotation.ListenEvent;
import com.seeyon.ctp.workflow.exception.BPMException;
import com.seeyon.ctp.workflow.wapi.WorkflowApiManager;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Restrictions;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.io.UnsupportedEncodingException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.EnumMap;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

public class TemplateManagerImpl implements TemplateManager{
    private static final Log LOG = LogFactory.getLog(TemplateManagerImpl.class);
    private TemplateDao templateDao;
    private OrgManager orgManager;
    private WorkflowApiManager wapi;
    private PermissionManager permissionManager;
    private TemplateQuery4ReportHandler templateQuery4ReportHandler;
    private MainbodyManager ctpMainbodyManager;
	private CAPFormManager capFormManager;
	private FormApi4Cap3  formApi4Cap3;
	private FormApi4Cap4  formApi4Cap4;
	private AttachmentManager attachmentManager;
	private AppLogManager appLogManager;
	private TemplateCategoryManager  templateCategoryManager;
	private ProcessAssetsManager processAssetsManager;
	private SuperviseManager superviseManager;
	private EnumManager em = (EnumManager) AppContext.getBean("enumManagerNew");
	private TemplateApproveDao templateApproveDao;
	private OrgManagerDirect orgManagerDirect;
	private RoleManager roleManager;
	private TemplateCacheManager templateCacheManager;
	
	
	public OrgManagerDirect getOrgManagerDirect() {
		return orgManagerDirect;
	}

	public void setOrgManagerDirect(OrgManagerDirect orgManagerDirect) {
		this.orgManagerDirect = orgManagerDirect;
	}

	public RoleManager getRoleManager() {
		return roleManager;
	}

	public void setRoleManager(RoleManager roleManager) {
		this.roleManager = roleManager;
	}

	public TemplateCacheManager getTemplateCacheManager() {
		return templateCacheManager;
	}

	public void setTemplateCacheManager(TemplateCacheManager templateCacheManager) {
		this.templateCacheManager = templateCacheManager;
	}

	public TemplateApproveDao getTemplateApproveDao() {
		return templateApproveDao;
	}

	public void setTemplateApproveDao(TemplateApproveDao templateApproveDao) {
		this.templateApproveDao = templateApproveDao;
	}

	public SuperviseManager getSuperviseManager() {
		return superviseManager;
	}

	public void setSuperviseManager(SuperviseManager superviseManager) {
		this.superviseManager = superviseManager;
	}

	public void setProcessAssetsManager(ProcessAssetsManager processAssetsManager) {
		this.processAssetsManager = processAssetsManager;
	}

	public AppLogManager getAppLogManager() {
		return appLogManager;
	}

	public void setAppLogManager(AppLogManager appLogManager) {
		this.appLogManager = appLogManager;
	}

	public AttachmentManager getAttachmentManager() {
		return attachmentManager;
	}

	public void setAttachmentManager(AttachmentManager attachmentManager) {
		this.attachmentManager = attachmentManager;
	}

	private TemplateRecentBatchTaskManager templateRecentBatchTaskManager;
	
	
    
    public TemplateCategoryManager getTemplateCategoryManager() {
        return templateCategoryManager;
    }

    public void setTemplateCategoryManager(TemplateCategoryManager templateCategoryManager) {
        this.templateCategoryManager = templateCategoryManager;
    }

    public TemplateRecentBatchTaskManager getTemplateRecentBatchTaskManager() {
		return templateRecentBatchTaskManager;
	}

	public void setTemplateRecentBatchTaskManager(TemplateRecentBatchTaskManager templateRecentBatchTaskManager) {
		this.templateRecentBatchTaskManager = templateRecentBatchTaskManager;
	}	
	

	public void setCapFormManager(CAPFormManager capFormManager) {
        this.capFormManager = capFormManager;
    }
	
	public void setCtpMainbodyManager(MainbodyManager ctpMainbodyManager) {
		this.ctpMainbodyManager = ctpMainbodyManager;
	}
	
	public void setPermissionManager(PermissionManager permissionManager) {
		this.permissionManager = permissionManager;
	}

	public TemplateQuery4ReportHandler getTemplateQuery4ReportHandler() {
		if (templateQuery4ReportHandler == null) {
			this.templateQuery4ReportHandler = (TemplateQuery4ReportHandler) AppContext
					.getBean("templateQuery4ReportHandler");
		}
		return this.templateQuery4ReportHandler;
	}

    
	public TemplateDao getTemplateDao() {
        return templateDao;
    }
    public OrgManager getOrgManager() {
        return orgManager;
    }
    public void setOrgManager(OrgManager orgManager) {
        this.orgManager = orgManager;
    }
    public void setTemplateDao(TemplateDao templateDao) {
        this.templateDao = templateDao;
    }
    
 
    @Override
    @ProcessInDataSource(name = DataSourceName.BASE)
    public void updateTempleteOfCategory(Long[] templateIds, int categoryType, Long categoryId)throws BusinessException{
    	templateDao.updateTempleteOfCategory(templateIds, categoryType, categoryId);
    	
    	//更新缓存
    	if(templateIds != null && templateIds.length != 0 ) {
    		for(Long id : templateIds) {
    			CtpTemplate t =   templateCacheManager.getCtpTemplate(id);
    			t.setCategoryId(categoryId);
    			templateCacheManager.addCacheTemplate(t);
    		}
    	}
    }
    
    
    @Override
    @ProcessInDataSource(name = DataSourceName.BASE)
    public void saveCtpTemplate(CtpTemplate ctpTemplate) throws BusinessException {

        templateDao.saveTemplete(ctpTemplate);
        
        templateCacheManager.addCacheTemplate(ctpTemplate);
    }
    
    @Override
    @ProcessInDataSource(name = DataSourceName.BASE)
    public void updateCtpTemplate(CtpTemplate ctpTemplate) throws BusinessException {

    	  templateDao.updateTemplete(ctpTemplate);
    	
    	  if(ctpTemplate.isDelete()){
    		  templateCacheManager.deleteCacheTemplate(ctpTemplate);

    		  TemplateDeleteEvent templateDeleteEvent = new TemplateDeleteEvent(this);
              templateDeleteEvent.setTemplateId(ctpTemplate.getId());
              EventDispatcher.fireEventAfterCommit(templateDeleteEvent);

    	  }
    	  else{
    		  templateCacheManager.addCacheTemplate(ctpTemplate);
    	  }
    }
    
    //保存历史记录
    @Override
    public void saveCtpTemplateHistory(CtpTemplateHistory ctpTemplateHistory) throws BusinessException {
        templateDao.saveCtpTemplateHistory(ctpTemplateHistory);
    }
    public void updateTempleteHistory(CtpTemplateHistory ctpTemplate)throws BusinessException{
        templateDao.updateTempleteHistory(ctpTemplate);
    }
    public void chownTemplete(CtpTemplate ctpTemplate)throws BusinessException{
        try {
            CtpDynamicDataSource.setDataSourceKey(DataSourceName.BASE.getSource());
            templateDao.updateTemplete(ctpTemplate);
        }finally{
            CtpDynamicDataSource.clearDataSourceKey();
        }
        if(ctpTemplate.isDelete()){
            templateCacheManager.deleteCacheTemplate(ctpTemplate);
        }else{//更新模板所属人对某人来说可能是新增权限
            templateCacheManager.addCacheTemplate(ctpTemplate);
        }
        HashMap<String,String> params = new HashMap<String, String>();
        params.put("templateId",String.valueOf(ctpTemplate.getId()));
        //更新历史记录
        List<CtpTemplateHistory> histories =   templateDao.getCtpTemplateHistory(null,params);
        for(CtpTemplateHistory ctpTemplateHistory:histories){
            ctpTemplateHistory.setMemberId(ctpTemplate.getMemberId());
            ctpTemplateHistory.setCategoryId(ctpTemplate.getCategoryId());
            ctpTemplateHistory.setOrgAccountId(ctpTemplate.getOrgAccountId());
            templateDao.updateTempleteHistory(ctpTemplateHistory);
            //草稿,审核中 的数据需要修改创建人
            if(ctpTemplateHistory.getSubstate() !=null && (ctpTemplateHistory.getSubstate() == 0 || ctpTemplateHistory.getSubstate() == 1)){
               TemplateApprovePO templateApprovePO =  templateApproveDao.getByTemplateHistoryId(ctpTemplateHistory.getId());
                templateApprovePO.setCreater(ctpTemplateHistory.getMemberId());
            }
        }

    }
    @Override
    public CtpTemplate getCtpTemplate(Long id) throws BusinessException {
    	if(id == null) {
    	    return null;
    	}
        return templateDao.getTemplete(id);
    }
    
    public CtpTemplate getCtpTemplateFromCache(Long id) throws BusinessException {
    	if(id == null) {
    	    return null;
    	}
        return templateCacheManager.getCtpTemplate(id);
    }
    
    
    @Override
    public CtpTemplateHistory getCtpTemplateHistory(Long id)throws BusinessException{
        if(id == null) {
            return null;
        }
        return templateDao.getCtpTemplateHistoryById(id);
    }
    @Override
    @ProcessInDataSource(name = DataSourceName.BASE)
    public void deleteCtpTemplate(Long id) throws BusinessException {
    	if(id == null){
    	    return ;
    	}
    	CtpTemplate template = templateCacheManager.getCtpTemplate(id);
        templateDao.deleteTemplete(id);
        //分发事件
        TemplateDeleteEvent templateDeleteEvent = new TemplateDeleteEvent(this);
        templateDeleteEvent.setTemplateId(id);
        if(template!=null){
        	templateDeleteEvent.setFormId(template.getFormAppId());
        }
        EventDispatcher.fireEvent(templateDeleteEvent);
        CtpTemplate tt = new CtpTemplate();
        tt.setId(id);
        //因为个人模板最后获取的时候走了权限过滤所以可以只删除模板
        templateCacheManager.deleteCacheTemplate(tt);
    }
    @Override
    public void deleteCtpTempleteHistoryById(Long id)throws BusinessException{
        if(id == null){
            return ;
        }
        templateDao.deleteCtpTempleteHistoryById(id);
        //分发事件
    }
    @Override
    @ProcessInDataSource(name = DataSourceName.BASE)
    public void saveCtpTemplateCategory(CtpTemplateCategory ctpTemplateCategory) throws BusinessException {
    	templateCategoryManager.save(ctpTemplateCategory);
    }
    @Override
    @ProcessInDataSource(name = DataSourceName.BASE)
    public void updateCtpTemplateCategory(CtpTemplateCategory ctpTemplateCategory) throws BusinessException {
    	templateCategoryManager.update(ctpTemplateCategory);
    }
    @Override
    @CheckRoleAccess(roleTypes = { Role_NAME.TtempletManager,Role_NAME.AccountAdministrator})
    public CtpTemplateCategory getCtpTemplateCategory(Long id) throws BusinessException {
        return templateCategoryManager.get(id);
    }
    
    
	public CtpTemplateCategory getCategoryIncludeDirectChildren(Long id) throws BusinessException {
		
		return templateCategoryManager.getIncludeDirectChildren(id);
	}

	public CtpTemplateCategory getCategoryIncludeAllChildren(Long id) throws BusinessException {
		
		return templateCategoryManager.getIncludeAllChildren(id);
	}



 
    public List<CtpTemplateCategory> getCtpTemplateCategoryChildren(CtpTemplateCategory category,boolean isDirect){
        if(category == null){
            return new ArrayList<CtpTemplateCategory>();
        }
        
        if(isDirect){
            return category.getDirectChildrens();
        }
        else{
            return category.getAllCascadeChildrens();
        }
    }

	/*private void getAllChildrenOfCtpTemplateCategory(CtpTemplateCategory category,List<CtpTemplateCategory> childrenList){
	    if(category == null){
	        return ;
	    }
	    childrenList.add(category);
	    
	    try {
	        List<CtpTemplateCategory>  children= category.getChildren();
	        if(Strings.isNotEmpty(children)){
	        	for (CtpTemplateCategory cat : children) {
	        		getAllChildrenOfCtpTemplateCategory(cat,childrenList);
	        	}
	        }
	    } catch (Exception e) {
	        LOG.error("",e);
	    } 
	}*/
    private List<CtpTemplateCategory> getCtpTemplateCategoryAll() {
        return templateDao.getTemplateCategoryAll();
    }

    @Override
    @ProcessInDataSource(name = DataSourceName.BASE)
    public void deleteCtpTemplateCategory(Long id) throws BusinessException {
        templateCategoryManager.delete(id);
    }
    public List<CtpTemplateCategory> getCategorys(Long orgAccountId, List<ModuleType> types) {
       
        boolean hasEdocPlugin = AppContext.hasPlugin("edoc");
        List<CtpTemplateCategory> result = new ArrayList<CtpTemplateCategory>();
        List<CtpTemplateCategory> all = templateCategoryManager.getTemplateCategorys(orgAccountId);
        String categoryName = null;
        if (Strings.isNotEmpty(all)) {
            Integer tempType = 0;
            for (CtpTemplateCategory category : all) {
                tempType = category.getType();
                ModuleType tempModuleType = ModuleType.getEnumByKey(tempType);
                if (types.contains(tempModuleType)
                        || (types.contains(ModuleType.collaboration) && Integer.valueOf(2).equals(tempType))
                        || (types.contains(ModuleType.form) && Integer.valueOf(1).equals(tempType))
                        || (types.contains(ModuleType.edoc) && (tempType == 19 || tempType == 20 || tempType == 21) && hasEdocPlugin )
                    ) {
                    // 判断是否是国际化名称,
                    categoryName = ResourceUtil.getString(category.getName());
                    if (Strings.isNotBlank(categoryName)){
                    	category.setName(categoryName);
                    }
                    result.add(category);
                }
            }
        }
        return result;
    }

    public List<CtpTemplateCategory> getCategorysForCopy(List<Long> orgAccountIds, List<ModuleType> types) {
       
        boolean hasEdocPlugin = AppContext.hasPlugin("edoc");
        List<CtpTemplateCategory> result = new ArrayList<CtpTemplateCategory>();
        List<CtpTemplateCategory> all = new ArrayList<CtpTemplateCategory>();
        for (Long orgAccountId : orgAccountIds) {
        	List<CtpTemplateCategory> myAll =  templateCategoryManager.getTemplateCategorys(orgAccountId);
        	if(Strings.isNotEmpty(myAll)){
        		all.addAll(myAll);
        	}
		}
        
        String categoryName = null;
        if (Strings.isNotEmpty(all)) {
            Integer tempType = 0;
            for (CtpTemplateCategory category : all) {
                tempType = category.getType();
                ModuleType tempModuleType = ModuleType.getEnumByKey(tempType);
                if (types.contains(tempModuleType)
                        || (types.contains(ModuleType.collaboration) && Integer.valueOf(2).equals(tempType))
                        || (types.contains(ModuleType.form) && Integer.valueOf(1).equals(tempType))
                        || (types.contains(ModuleType.edoc) && (tempType == 19 || tempType == 20 || tempType == 21) && hasEdocPlugin )
                    ) {
                    // 判断是否是国际化名称,
                    categoryName = ResourceUtil.getString(category.getName());
                    if (Strings.isNotBlank(categoryName)){
                    	category.setName(categoryName);
                    }
                    result.add(category);
                }
            }
        }
        return result;
    }
    
    @Override
    public List<CtpTemplateCategory> getCategorys(Long orgAccountId, ModuleType type) {
        List<ModuleType> types = new ArrayList<ModuleType>();
        types.add(type);
        return getCategorys(orgAccountId, types);
    }
    
  
   
    
    /**
     * 获取当前登录单位下的全部表单、协同模板分类
     */
    private List<CtpTemplateCategory> getFormAndColCategories(Long accountId) {
    	
        List<CtpTemplateCategory> categories_coll= this.getCategorys(accountId,ModuleType.collaboration);
        List<CtpTemplateCategory> categories_form = this.getCategorys(accountId, ModuleType.form);
        
        return Strings.getSumCollection(categories_coll, categories_form);
    }
    /**
     * 解析出表单模板对应的所属应用类型及其父类型<br>
     * 如果是某个应用类型下的子类型，则最终在前端展现时，父类型也需要加入以便树状展现<br>
     * @param accountId   单位ID
     * @param categoryIds 表单模板所属应用分类ID集合
     * @throws BusinessException 
     */
    private List<CtpTemplateCategory> getCategorys(Long accountId, Set<Long> categoryIds) throws BusinessException {
        List<CtpTemplateCategory> formAndColCategories = this.getFormAndColCategories(accountId);
        
        List<CtpTemplateCategory> result = new UniqueList<CtpTemplateCategory>();
        if(Strings.isNotEmpty(formAndColCategories)) {
            for(CtpTemplateCategory category : formAndColCategories) {
                if(categoryIds.contains(category.getId())) {
                    result.add(category);
                    if(category.getParentId() != 4l && category.getParentId() != 0L) {
                        result.addAll(this.getParentCategorys(category));
                    }
                }
            }
        }
        return result;
    }

    private List<CtpTemplate> getTemplatesByParam(LinkedList<Criterion> list){
        if(Strings.isEmpty(list)){
            return new ArrayList<CtpTemplate>();
        }
        DetachedCriteria dc = DetachedCriteria.forClass(CtpTemplate.class);
        if(Strings.isNotEmpty(list)){
            for (Criterion criterion:list){
                dc.add(criterion);
            }
        }
//        dc.add(Restrictions.eq("orgAccountId", accountId));
        List<CtpTemplate> rs =  DBAgent.findByCriteria(dc);
        return rs;
    }
    @ListenEvent(event = DeleteAccountEvent.class)
    @ProcessInDataSource(name = DataSourceName.BASE)
    public void deleteAllTemplatesByAccountId(DeleteAccountEvent event) throws BusinessException {
        templateCacheManager.deleteCacheTempalteAuthsByOrgAccountId(event.getAccount().getId());
        String hql = "update CtpTemplate c set c.delete=:flag where c.orgAccountId =:orgAccountId";
        Map<String, Object> pMap = new HashMap<String, Object>();
        pMap.put("flag", Boolean.TRUE);
        pMap.put("orgAccountId", event.getAccount().getId());
        DBAgent.bulkUpdate(hql, pMap);
    }
    
    /**
     * 获取某一表单模板应用分类的父级分类，追溯到根节点下为止
     * @throws BusinessException 
     */
    private List<CtpTemplateCategory> getParentCategorys(CtpTemplateCategory category) throws BusinessException {
        List<CtpTemplateCategory> result = new UniqueList<CtpTemplateCategory>();
        
        if(category.getParentId()!=null 
                && category.getParentId() != ModuleType.collaboration.getKey() 
                && category.getParentId() != ModuleType.form.getKey() ) {
            CtpTemplateCategory parentCategory = this.getCtpTemplateCategory(category.getParentId());
            result.add(parentCategory);
            result.addAll(this.getParentCategorys(parentCategory));
        }
        
        return result;
    }
    @Override
    public void saveCtpTemplateAuth(CtpTemplateAuth ctpTemplateAuth) throws BusinessException {
        if(ctpTemplateAuth.getAccountId() == null){
            V3xOrgEntity e = orgManager.getEntity(ctpTemplateAuth.getAuthType(),ctpTemplateAuth.getAuthId());
            if(e!=null){
                ctpTemplateAuth.setAccountId(e.getOrgAccountId());
            }
        }
        templateDao.saveTemplateAuth(ctpTemplateAuth);
    }
    @Override
    public void updateCtpTemplateAuth(CtpTemplateAuth ctpTemplateAuth) throws BusinessException {
        templateDao.updateTemplateAuth(ctpTemplateAuth);
    }
    @Override
    public CtpTemplateAuth getCtpTemplateAuth(Long id) throws BusinessException {
        return templateDao.getTemplateAuth(id);
    }
    @Override
    public void deleteCtpTemplateAuth(CtpTemplateAuth ctpTemplateAuth) throws BusinessException {
        templateDao.deleteTemplateAuth(ctpTemplateAuth);
        templateCacheManager.deleteCacheAclTemplate(ctpTemplateAuth);
    }
    @Override
    public void saveCtpTemplateConfig(CtpTemplateConfig ctpTemplateConfig) throws BusinessException {
        templateDao.saveCtpTemplateConfig(ctpTemplateConfig);
    }
    @Override
    public void updateCtpTemplateConfig(CtpTemplateConfig ctpTemplateConfig) throws BusinessException {
        templateDao.updateCtpTemplateConfig(ctpTemplateConfig);
    }
    @Override
    public CtpTemplateConfig getCtpTemplateConfig(Long id) throws BusinessException {
        return templateDao.getCtpTemplateConfig(id);
    }
    @Override
    public void deleteCtpTemplateConfig(CtpTemplateConfig ctpTemplateConfig) throws BusinessException {
        templateDao.deleteCtpTemplateConfig(ctpTemplateConfig);
    }


	@Override
    public List<CtpTemplate> getPersonalTemplates(Long memberId) throws BusinessException{
		return templateCacheManager.cachePersonTemplates(memberId);
	}
	

   
    @Override
	public List<CtpTemplate> getPersonalTemplates(Long memberId, List<ModuleType> moduleTypes)
			throws BusinessException {

		List<CtpTemplate> templates = getPersonalTemplates(memberId);

		List<CtpTemplate> newTemplates = new ArrayList<CtpTemplate>();

		if (Strings.isNotEmpty(templates)) {
			List<Integer> moduleKeys = new ArrayList<Integer>();
			if (Strings.isNotEmpty(moduleTypes)) {
				for (ModuleType m : moduleTypes) {
					moduleKeys.add(m.getKey());
				}
			}
			for (CtpTemplate t : templates) {
				boolean isInclude = false;
				if (Strings.isNotEmpty(moduleKeys)) {
					if (moduleKeys.contains(t.getModuleType()) || t.getModuleType() == null) {
						isInclude = true;
					}
				} else {
					isInclude = true;
				}
				if (isInclude) {
					CtpTemplate clone = t.clone();
					newTemplates.add(clone);
				}
			}
		}

		return newTemplates;

	}
    
    //管理员查询本单位所有的模板
    private List<CtpTemplate> getSystemTemplatesByAccount(Long currentAccountId, List<ModuleType> moduleTypes) throws BusinessException {
	    EnumMap<searchCondtion, Object> map = new EnumMap<TemplateEnum.searchCondtion, Object>(TemplateEnum.searchCondtion.class);
	    if (Strings.isNotEmpty(moduleTypes)) {
		    List<Integer> moduleTypeKeys = new ArrayList<Integer>();
		    for (ModuleType moduleType : moduleTypes) {
			    moduleTypeKeys.add(moduleType.getKey());
		    }
		    map.put(searchCondtion.categoryType, moduleTypeKeys);
	    }
	    map.put(searchCondtion.account, currentAccountId);
	    return  systemTempleteObjQueryHelper(map);
    }
   
    public List<CtpTemplate> getSystemTemplatesByAcl(Long memberId, List<ModuleType> moduleTypes) throws BusinessException {
        
    	List<CtpTemplate> cacheTemplates  = getSystemTemplatesByAcl(memberId);
    	Iterator<CtpTemplate> ctpTemplateIterator = cacheTemplates.iterator();
    	while(ctpTemplateIterator.hasNext()){
		    CtpTemplate template = ctpTemplateIterator.next();
		    Integer moduleType = template.getModuleType();
		    if(null != moduleType && (moduleType.intValue() ==19 || moduleType.intValue() ==20 || moduleType.intValue() ==21)) {
			    ctpTemplateIterator.remove();
		    }
	    }
	   
    	List<CtpTemplate> retTemplates = new ArrayList<CtpTemplate>();
    	
    	if(Strings.isNotEmpty(moduleTypes) && Strings.isNotEmpty(cacheTemplates)){
    		boolean isOnlyForm = !moduleTypes.contains(ModuleType.collaboration)
    								&& moduleTypes.contains(ModuleType.form);
    		
    		boolean isOnlyColl = moduleTypes.contains(ModuleType.collaboration)
									&& !moduleTypes.contains(ModuleType.form);
    		
    		boolean isAllColl = moduleTypes.contains(ModuleType.collaboration)
					&& moduleTypes.contains(ModuleType.form);
    		
    		
    		List<Integer> moduleTypeKeys = new ArrayList<Integer>();
    		if(Strings.isNotEmpty(moduleTypes)){
    			for(ModuleType moduleType : moduleTypes){
    				moduleTypeKeys.add(moduleType.getKey());
    			}
    		}
    		for(CtpTemplate cache : cacheTemplates){
    			boolean isInclude = false;
    			if(Integer.valueOf(ApplicationCategoryEnum.collaboration.key()).equals(cache.getModuleType())){
    				if(isAllColl){
    					isInclude = true;
    				}
    				else if(isOnlyColl && !String.valueOf(MainbodyType.FORM.getKey()).equals(cache.getBodyType())){
    					isInclude = true;
    				}
    				else if(isOnlyForm && String.valueOf(MainbodyType.FORM.getKey()).equals(cache.getBodyType())){
    					isInclude = true;
    				}
    			}
    			else if(moduleTypeKeys.contains(cache.getModuleType())){
    				isInclude = true;
    			}
    			if(isInclude){
    				CtpTemplate clone = cache.clone();
    				retTemplates.add(clone);
    			}
    		}
    	}
    	
    	return retTemplates ; 
    }

    public List<CtpTemplate> getSystemTemplatesByAcl(Long memberId) throws BusinessException {
    	return templateCacheManager.cacheSystemTemplatesByAcl(memberId);
    }
    
  
    
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public List<Long> getSystemTemplateIdsByAcl(Long memberId) throws BusinessException {
		List<Long>  templateIdList = new ArrayList<Long>();
		
		List<CtpTemplate> cacheTemplates = getSystemTemplatesByAcl(memberId);
		
		if (Strings.isNotEmpty(cacheTemplates)) {
			for(CtpTemplate t : cacheTemplates){
				templateIdList.add(t.getId());
			}
		} 
		return templateIdList;
	}

    /**
     * 辅助书写Hql语句：所要查询的表单模板字段
     */
    private StringBuilder getSelectFieldsHqlStr() {
        StringBuilder s = new StringBuilder();
        s.append("select DISTINCT")
        .append(" t.id")
        .append(",t.categoryId")
        .append(",t.createDate")
        .append(",t.modifyDate")
        .append(",t.description")
        .append(",t.orgAccountId")
        .append(",t.sort")
        .append(",t.state")
        .append(",t.subject")
        .append(",t.type")
        .append(",t.bodyType")
        .append(",t.moduleType")
        .append(",t.memberId")
        .append(",t.standardDuration")
        .append(",t.workflowId")
        .append(",t.body")
        .append(",t.system")
        .append(",t.delete")
        .append(",t.formAppId ")
        .append(",t.belongOrg ")
        .append(",t.publishTime ");
        return s;
    }
    
    @Override
    public List<CtpTemplate> getSystemTempletes(Long accountId,List<ModuleType> moduleTypes, String queryColumn, String queryVlaue) {
       
        Map<searchCondtion,Object> map = new HashMap<TemplateEnum.searchCondtion, Object>();
       
        if(Strings.isNotBlank(queryColumn) && Strings.isNotBlank(queryVlaue) ){
            if(searchCondtion.subject.name().equals(queryColumn)){
                map.put(searchCondtion.subject, queryVlaue);
            }else if(searchCondtion.categoryId.name().equals(queryColumn)){
                map.put(searchCondtion.categoryId, Long.valueOf(queryVlaue));
            }else if(searchCondtion.memberId.name().equals(queryColumn)){
                map.put(searchCondtion.memberId, Long.valueOf(queryVlaue));
            }else if(searchCondtion.hasAuth.name().equals(queryColumn)){
                map.put(searchCondtion.memberId, Long.valueOf(queryVlaue));
                map.put(searchCondtion.hasAuth, Boolean.TRUE);
            }else if(searchCondtion.types.name().equals(queryColumn)){
            	String[] arrayTypes = queryVlaue.split(",");
            	List<String> resultList= Arrays.asList(arrayTypes);
            	map.put(searchCondtion.types, resultList);
            }
        }
        
        List<CtpTemplate> result = getSystemTempletes(accountId, moduleTypes, map);
        return result;
    }
    
    @Override
    public List<CtpTemplate> getSystemTempletes(Long accountId, List<ModuleType> moduleTypes,
            Map<searchCondtion, Object> conditions) {

        EnumMap<searchCondtion, Object> map = new EnumMap<TemplateEnum.searchCondtion, Object>(
                TemplateEnum.searchCondtion.class);
        
        if(conditions != null && !conditions.isEmpty()){
            map.putAll(conditions);
        }

        List<Integer> moduleTypeKeys = new ArrayList<Integer>();
        if (Strings.isNotEmpty(moduleTypes)) {
            for (ModuleType m : moduleTypes) {
                moduleTypeKeys.add(m.getKey());
            }
        }

        map.put(searchCondtion.categoryType, moduleTypeKeys);
        if(!OrgConstants.GROUPID.equals(accountId)){
        	map.put(searchCondtion.account, accountId);
        }
        List<CtpTemplate> result = systemTempleteObjQueryHelper(map);
        return result;
    }
    
 

    @Override
    public List<CtpTemplate> getSystemFormTempletes(Long accountId, String queryColumn, String queryValue) {
    	
        List<ModuleType> moduleTypes = new ArrayList<ModuleType>();
        moduleTypes.add(ModuleType.form);
        return getSystemTempletes(accountId, moduleTypes, queryColumn, queryValue);
        
    }
    
    @Override
	public List<CtpTemplate> getSystemFormTemplatesByAcl(Long memberId,String queryColumn, String queryVlaue) throws BusinessException {
    	List<CtpTemplate> templates = templateCacheManager.cacheSystemTemplatesByAcl(memberId);
    	if(Strings.isNotEmpty(templates)){
    		boolean removeFlag = false ;
    		for(Iterator<CtpTemplate> it= templates.iterator();it.hasNext();){
    			CtpTemplate t = it.next();
    			if(Strings.isNotBlank(queryColumn) && Strings.isNotBlank(queryVlaue) ){
    				if(searchCondtion.subject.name().equals(queryColumn)){
    					if(t.getSubject() != null && t.getSubject().indexOf(queryVlaue) == -1){
    						it.remove();
                            removeFlag = true ;
                        }
    				}
    				if(searchCondtion.categoryId.name().equals(queryColumn)){
    					if(t.getCategoryId() != null && ! String.valueOf(t.getCategoryId()).equals(queryVlaue)){
    						it.remove();
                            removeFlag = true ;
    					}
    				}
    			}
    			if(!removeFlag){
                    boolean isForm = Integer.valueOf(ApplicationCategoryEnum.collaboration.key()).equals(t.getModuleType())
                            && String.valueOf(MainbodyType.FORM.getKey()).equals(t.getBodyType()) ;
                    if(!isForm ){
                        it.remove();
                    }
                }
    		}
    	}
    	return templates;
    }
    
    /**
     * 只取模板ID
     */
    private List<Long> systemTempleteIDQueryHelper(Map<searchCondtion,Object> search){
        StringBuilder s = new StringBuilder();
        s.append("select DISTINCT t.id");
        
        List<Long> list = (List<Long>)this.systemTempleteQueryHelper(s, "", search);
        
        return list;
    }

    /**
     * 只取需要的字段
     */
    private List<CtpTemplate> systemTempleteSimpleObjQueryHelper(Map<searchCondtion, Object> search) {
        StringBuilder s = new StringBuilder();
        s.append("select DISTINCT t.id,t.categoryId,t.subject,t.type,t.moduleType ");
        List<Object[]> list = (List<Object[]>) systemTempleteQueryHelper(s, "", search);
        List<CtpTemplate> result = new ArrayList<CtpTemplate>();
        if (list != null) {
            CtpTemplate templete = null;
            for (Object[] o : list) {
                templete = new CtpTemplate();
                int n = 0;
                templete.setId((Long) o[n++]);
                templete.setCategoryId((Long) o[n++]);
                templete.setSubject((String) o[n++]);
                templete.setType((String) o[n++]);
                templete.setModuleType((Integer) o[n++]);
                result.add(templete);
            }
        }
        return result;
    }

    private List<CtpTemplate> systemTempleteObjQueryHelper(Map<searchCondtion,Object> search){
        List<Object[]> list = (List<Object[]>)systemTempleteQueryHelper(getSelectFieldsHqlStr(), "order by t.sort,t.createDate", search);
        return parseObjArray2Templetes(list);
    }
    
    private List<?> systemTempleteQueryHelper(StringBuilder selectFieldsHqlStr, String orderBy,
            Map<searchCondtion, Object> search) {
        
        Long categoryId = null;
        List<Integer> categoryType = null;
        String subject = null;
        List<Long> domainIds = null;
        Long accountId = null;
        Integer contentType = null;
        Boolean hasAuth = null;
        List<Long> formIds = null;

        if (search.containsKey(searchCondtion.categoryId))
            categoryId = (Long) search.get(searchCondtion.categoryId);
        if (search.containsKey(searchCondtion.categoryType))
            categoryType = (List<Integer>) search.get(searchCondtion.categoryType);
        
        // 仅查表单 2 
        if (categoryType != null && categoryType.contains(Integer.valueOf(ModuleType.form.getKey()))
                && !categoryType.contains(Integer.valueOf(ModuleType.collaboration.getKey()))){
            contentType = MainbodyType.FORM.getKey();
            if (!categoryType.contains(ModuleType.collaboration.getKey())){
                categoryType.add(ModuleType.collaboration.getKey());
            }
        }
        if (search.containsKey(searchCondtion.domainIds))
            domainIds = (List<Long>) search.get(searchCondtion.domainIds);
        if (search.containsKey(searchCondtion.subject))
            subject = (String) search.get(searchCondtion.subject);
        if (search.containsKey(searchCondtion.account))
            accountId = (Long) search.get(searchCondtion.account);
        if (search.containsKey(searchCondtion.contentType))
            contentType = (Integer) search.get(searchCondtion.contentType);
        if (search.containsKey(searchCondtion.hasAuth))
            hasAuth = (Boolean) search.get(searchCondtion.hasAuth);
        if(search.containsKey(searchCondtion.formIds))
            formIds = (List<Long>) search.get(searchCondtion.formIds);
            

        Map<String, Object> namedParameterMap = new HashMap<String, Object>();
        StringBuilder s = new StringBuilder();
        s.append(selectFieldsHqlStr);
        s.append(" from ");
        s.append(CtpTemplate.class.getCanonicalName()).append(" as t ");
        
        
        if (domainIds != null) {
            s.append(",").append(CtpTemplateAuth.class.getCanonicalName() ).append( " as a ");
        }
        
        s.append(" where 1=1 ");
        if(domainIds!=null){
            s.append(" and t.id=a.moduleId" );
        }
        
        if(Strings.isNotEmpty(categoryType)){
            s.append(" and t.moduleType in (:categoryType)");
            namedParameterMap.put("categoryType", categoryType);
        }
        if(accountId!=null){
            s.append(" and t.orgAccountId in (:orgAccountId)");
            namedParameterMap.put("orgAccountId", accountId);
        }
        if(domainIds!=null){
            s.append(" and a.authId" ).append( " in (:domainIds)");
            namedParameterMap.put("domainIds", domainIds);
        }
        if(Strings.isNotBlank(subject)){
            s.append(" and t.subject like :subject ");
            namedParameterMap.put("subject", "%" + SQLWildcardUtil.escape(subject).trim() + "%");
        }
        if(categoryId!=null){
            s.append(" and t.categoryId = :categoryId ");
            namedParameterMap.put("categoryId", categoryId);
        }
        if(contentType!=null){
            s.append(" and t.bodyType = :bodyType ");
            namedParameterMap.put("bodyType", String.valueOf(contentType));
        }
        if(categoryType != null && categoryType.contains(ModuleType.collaboration.getKey()) 
                && !categoryType.contains(ModuleType.form.getKey())
                && !Integer.valueOf(MainbodyType.FORM.getKey()).equals(contentType) ){
            s.append(" and t.bodyType != :bodyType ");
            namedParameterMap.put("bodyType", String.valueOf(MainbodyType.FORM.getKey()));
        }
        
        if(search.containsKey(searchCondtion.memberId)){
            Long memberId = (Long)search.get(searchCondtion.memberId);
            if(memberId != null){
                s.append(" and t.memberId = :memberId ");
                namedParameterMap.put("memberId", memberId);
            }
        }
        
        if(search.containsKey(searchCondtion.types)){
        	s.append(" and t.type in (:type)");
        	namedParameterMap.put("type", (List<String>)search.get(searchCondtion.types));
        }
        
        s.append(" and (t.system").append( "=" + Boolean.TRUE + ")")
        //排除已经删除过的
         .append(" and (t.delete").append( "=" + Boolean.FALSE + ")")
         .append(" and (t.state" ).append( "=" + TemplateEnum.State.normal.ordinal() + ")");
         
        //s.append(" and t.substate" ).append( "=" + Approve.ApproveType.haveReleased.key() );
        if (hasAuth != null && hasAuth && domainIds == null) {
        	//公文的升级老数据可能存在没有授权的时候authId为0,因此需要排除该种数据
            s.append(" and exists(from CtpTemplateAuth t3 where t3.moduleId=t.id and authId != 0) ");
        }
        
        //表单ID
        if(formIds != null && !formIds.isEmpty()){
            s.append(" and t.formAppId in (:formAppIds)");
            namedParameterMap.put("formAppIds", formIds);
        }
        
        s.append(" ").append(orderBy);

        List<?> list = DBAgent.find(s.toString(), namedParameterMap);
        return list;
    }
 

    @Override
    @ProcessInDataSource(name = DataSourceName.BASE)
    public List<CtpTemplate> getCtpTemplates(long formAppId) throws BusinessException {

        Map<String,Object> map = new HashMap<String,Object>();

        StringBuilder hql = new StringBuilder();
        hql.append("select t from CtpTemplate t,CtpContentAll a where  t.body=a.id and a.contentTemplateId =:contentTemplateId ");
        map.put("contentTemplateId", formAppId);
        return DBAgent.find(hql.toString(),map);
    }

    
    @Override
    @ProcessInDataSource(name = DataSourceName.BASE)
    public List<CtpTemplate> getCtpTemplates(long contentId, boolean isDelete) throws BusinessException {
        
        Map<String,Object> map = new HashMap<String,Object>();

        StringBuilder hql = new StringBuilder();
    	hql.append("select t from CtpTemplate t,CtpContentAll a where  t.body=a.id and a.contentTemplateId =:contentTemplateId ");
	    hql.append(" and t.delete = :delete ");
	    map.put("delete",isDelete);
        map.put("contentTemplateId", contentId);
        return DBAgent.find(hql.toString(),map);
    }

    //应用绑定
    @Override
    public List<CtpTemplateHistory> getCtpTemplateHistorys(long contentId) throws BusinessException {
        
        Map<String,Object> map = new HashMap<String,Object>();

        StringBuilder hql = new StringBuilder();
    	hql.append("select t from CtpTemplateHistory t where  t.formAppId =:contentTemplateId ");
        map.put("contentTemplateId", contentId);
        return DBAgent.find(hql.toString(),map);
    }
    
    @AjaxAccess
    public FlipInfo getformBindList(FlipInfo flipInfo,Map<String,String> query) throws BusinessException{
    	Map<String,Object> map = new HashMap<String,Object>();

    	String hql ="select t from CtpTemplate t,CtpContentAll a where " +
    			" t.body=a.id and a.contentTemplateId =:contentTemplateId and t.delete=:delete and t.system=:system";
    	boolean isBlankQuery = false;
    	if(null != query.get("responsibleShowStr") || null != query.get("auditorShowStr") ){ 
    		hql ="select t from CtpTemplate t where t.id in(select distinct t1.id from CtpTemplate t1,CtpContentAll a";
    		if(Strings.isBlank(query.get("responsibleShowStr")) && Strings.isBlank(query.get("auditorShowStr"))){
    			isBlankQuery = true;
    			hql += " where t1.body=a.id and a.contentTemplateId =:contentTemplateId and t1.delete=:delete and t1.system=:system";
    		}else{
    			hql += ", CtpTemplateOrg org where t1.body=a.id and t1.id=org.templateId and a.contentTemplateId =:contentTemplateId and t1.delete=:delete and t1.system=:system";
    		}
    	}
    	
    	// 创建人
    	if(Strings.isLong(query.get("memberId"))) {
    	    long memberId = Long.valueOf(query.get("memberId"));
    	    hql += " and t.memberId = :memberId ";
    	    map.put("memberId", memberId);
    	}
    	
    	if(null != query.get("subject")){
    		hql += " and t.subject like:subject ";
    		map.put("subject", "%" + SQLWildcardUtil.escape(query.get("subject")) + "%");
    	}else if(null != query.get("belongOrg")){
    		hql ="select t from CtpTemplate t,CtpContentAll a,OrgUnit o where " +
        			" t.body=a.id and t.belongOrg=o.id and a.contentTemplateId =:contentTemplateId and t.delete=:delete and t.system=:system"
        			+ " and o.name like :orgName ";
    		map.put("orgName",  "%" + SQLWildcardUtil.escape(Strings.getSafeLimitLengthString(query.get("belongOrg"),99,"")) + "%");
    	}else if(null != query.get("responsibleShowStr")){
    		if(isBlankQuery){
    			hql +=" ) ";
    		}else{
    			hql += " and org.orgName like :responsibleShowStr and org.dataType=0)";
    			String responsible = "";
    			if(query.get("responsibleShowStr").length() > 99){
    				responsible =  query.get("responsibleShowStr").substring(0,99);
    			}else{
    				responsible =  query.get("responsibleShowStr");
    			}
    			responsible ="%" + SQLWildcardUtil.escape(responsible)  + "%";
    			map.put("responsibleShowStr", responsible);
    			LOG.info("responsibleShowStr="+responsible);
    		}
    	}else if(null != query.get("auditorShowStr")){
    		if(isBlankQuery){
    			hql +=" ) ";
    		}else{
    			hql += " and org.orgName like :auditorShowStr and org.dataType=1)";
    			String auditor = "";
    			if(query.get("auditorShowStr").length() > 99){
    				auditor = query.get("auditorShowStr").substring(0, 99);
    			}else{
    				auditor = query.get("auditorShowStr");
    			}
    			auditor = "%"+ SQLWildcardUtil.escape(auditor) +"%";
    			map.put("auditorShowStr", auditor);
    			LOG.info("auditorShowStr=" + auditor);
    		}
    	}else if(null != query.get("publishTime")){
    		String[] split = query.get("publishTime").split("#");
    		if(split.length > 0){
    			if(Strings.isNotBlank(split[0])){
    				java.util.Date stamp = Datetimes.parseDatetime(Datetimes.getFirstTimeStr(split[0]));
    				hql += " and t.publishTime >= :from_publishTime ";
    				map.put("from_publishTime", stamp);
    			}
    			if(split.length > 1){
    				if(Strings.isNotBlank(split[1])){
    					java.util.Date stamp = Datetimes.parseDatetime(Datetimes.getLastTimeStr(split[1]));
    					hql += " and t.publishTime <= :to_publishTime ";
    					map.put("to_publishTime", stamp);
    				}
    			}
    		}
    	}
        map.put("contentTemplateId", Long.valueOf(query.get("contentTemplateId")));
        map.put("delete",false);
        map.put("system",true);

        //排序
        hql += " order by t.modifyDate desc ";
        List<CtpTemplate> find = null;
        try {
            CtpDynamicDataSource.setDataSourceKey(DataSourceName.BASE.getSource());
            find = DBAgent.find(hql,map,null);
        } finally {
            CtpDynamicDataSource.clearDataSourceKey();
        }
		HashMap<Long, TemplateBO> mapBind = new HashMap<Long, TemplateBO>();
        if(find != null && find.size() >0){
			HashMap<String,String> parmas = new HashMap<String, String>();
			parmas.put("formAppId",String.valueOf(find.get(0).getFormAppId()));
			parmas.put("substate",Approve.ApproveType.inReview.getKey()+","+Approve.ApproveType.toBeReleased.getKey()+","+Approve.ApproveType.haveReleased.getKey());
			List<CtpTemplateHistory> historyList = this.getCtpTemplateHistory(null,parmas);

			if(historyList != null && historyList.size() >0) {

				boolean flag = true;
				TemplateBO vo = null;
				Integer numVersion = -1;
				for (CtpTemplateHistory ctpTemplateHistory : historyList) {

					flag = mapBind.containsKey(ctpTemplateHistory.getTemplateId());//是否为真
					if (flag) {
						vo = mapBind.get(ctpTemplateHistory.getTemplateId());
					} else {
						vo = new TemplateBO();
					}
					//待发布的版本--只获取最新的待发布版本来显示
					numVersion = ctpTemplateHistory.getVersion();
                    vo.setVersion(0);
					if (Approve.ApproveType.toBeReleased.getKey() == ctpTemplateHistory.getSubstate()) {
						if (vo.getToBeReleasedVersion() != null && Strings.isNotEmpty(vo.getToBeReleasedVersion())) {
							if (Integer.parseInt(vo.getToBeReleasedVersion()) < numVersion) {
								vo.setToBeReleasedVersion(String.valueOf(numVersion));
							}
						} else {
							vo.setToBeReleasedVersion(String.valueOf(numVersion));
						}
					}
					//审批中的版本
					if (Approve.ApproveType.inReview.getKey() == ctpTemplateHistory.getSubstate()) {
						vo.setApproveVersion(String.valueOf(numVersion));
					}
                    //正在使用的版本
                    if (Approve.ApproveType.haveReleased.getKey() == ctpTemplateHistory.getSubstate()) {
                        vo.setVersion(numVersion);
                    }
					mapBind.put(ctpTemplateHistory.getTemplateId(), vo);

				}
			}
		}
        if(null != query.get("isFromQuery") && "1".equals(query.get("isFromQuery"))){
			List<TemplateBO> VO = covertTemplatePO2VO(find);
			//提前查询表单的所有待发布数据和审批数据
			if("1".equals(query.get("cap4Flag"))){
				if((find != null && find.size() >0)){
						for(TemplateBO templateBO:VO){
                            HashMap<String,String> versionMap = new HashMap<String, String>();
							if(mapBind.containsKey(templateBO.getId())) {
								TemplateBO bo = mapBind.get(templateBO.getId());
								if (bo.getApproveVersion() != null && Strings.isNotEmpty(bo.getApproveVersion())) {
									templateBO.setApproveVersion("V" + bo.getApproveVersion() + ".0");
                                    versionMap.put("approveVersion","V" + bo.getApproveVersion()+ ".0");
								}
								if (bo.getToBeReleasedVersion() != null && Strings
										.isNotEmpty(bo.getToBeReleasedVersion())) {
									templateBO.setToBeReleasedVersion("V" + bo.getToBeReleasedVersion() + ".0");
                                    versionMap.put("toBeReleasedVersion","V" + bo.getToBeReleasedVersion()+ ".0");
								}
                                if (bo.getVersion() != null && Strings
                                        .isNotEmpty(String.valueOf(bo.getVersion()))) {
                                    if("0".equals(String.valueOf(bo.getVersion()))){
                                        templateBO.setVersion1("");
                                        versionMap.put("version","");
                                    }else{
                                        templateBO.setVersion1("V" + bo.getVersion() + ".0");
                                        versionMap.put("version",String.valueOf(bo.getVersion()));
                                    }

                                }
							}else{
                                templateBO.setVersion1("");
                            }
                            templateBO.getExtraMap().put("versionMap",versionMap);

						}
					}
				}
			flipInfo.setData(VO);
			return flipInfo;
		}

        if(flipInfo == null){
        	flipInfo = new FlipInfo();
        }
		for(CtpTemplate template:find){
		    HashMap<String,String> versionMap = new HashMap<String, String>();
			if(mapBind.containsKey(template.getId())) {
				TemplateBO bo = mapBind.get(template.getId());
				if (bo.getApproveVersion() != null && Strings.isNotEmpty(bo.getApproveVersion())) {
					template.setApproveVersion("V" + bo.getApproveVersion() + ".0");
                    versionMap.put("approveVersion","V" +bo.getApproveVersion()+ ".0");
				}
				if (bo.getToBeReleasedVersion() != null && Strings
						.isNotEmpty(bo.getToBeReleasedVersion())) {
					template.setToBeReleasedVersion("V" + bo.getToBeReleasedVersion() + ".0");
                    versionMap.put("toBeReleasedVersion","V" + bo.getToBeReleasedVersion()+ ".0");
				}
                if (bo.getVersion() != null && Strings
                        .isNotEmpty(String.valueOf(bo.getVersion()))) {
                    template.setVersion(bo.getVersion() );
                    versionMap.put("version",String.valueOf(bo.getVersion()));
                }

			}
            template.getExtraMap().put("versionMap",versionMap);
		}
        flipInfo.setData(find);
        return flipInfo;
     }
    public List covertTemplatePO2VO(List<CtpTemplate> list) throws BusinessException{
    	List<TemplateBO> boList = new ArrayList<TemplateBO>();
    	for(int a=0 ; a < list.size(); a ++){
    		CtpTemplate ctpTemplate = list.get(a);
    		ctpTemplate = addOrgIntoTempalte(ctpTemplate);
    		TemplateBO bo = new TemplateBO(ctpTemplate);
    		if(null != bo.getCategoryId()){
    			CtpTemplateCategory categorybyId = getCategorybyId(bo.getCategoryId());
    			if(null != categorybyId){
    				bo.setCategoryName(categorybyId.getName());
    			}
    		}
    		boList.add(bo);
    	}
    	return boList;
    }
    
    public boolean needRecordAppLog(CtpTemplate newTemplate,CtpTemplate oldTemplate){
    	if((null != newTemplate.getBelongOrg() && !newTemplate.getBelongOrg().equals(oldTemplate.getBelongOrg()) ||
    		(null == newTemplate.getBelongOrg() && oldTemplate.getBelongOrg() != null))){
    		return true;
    	}
    	if((null != newTemplate.getPublishTime() && !newTemplate.getPublishTime().equals(oldTemplate.getPublishTime()) ||
        		(null == newTemplate.getPublishTime() && oldTemplate.getPublishTime() != null))){
        		return true;
        	}
    	if((null != newTemplate.getProcessLevel() && !newTemplate.getProcessLevel().equals(oldTemplate.getProcessLevel()) ||
        		(null == newTemplate.getProcessLevel() && oldTemplate.getProcessLevel() != null))){
        		return true;
        	}
    	if((null != newTemplate.getCoreUseOrg() && !newTemplate.getCoreUseOrg().equals(oldTemplate.getCoreUseOrg()) ||
        		(null == newTemplate.getCoreUseOrg() && oldTemplate.getCoreUseOrg() != null))){
        		return true;
        	}
    	if((null != newTemplate.getResponsible() && !newTemplate.getResponsible().equals(oldTemplate.getResponsible()) ||
        		(null == newTemplate.getResponsible() && oldTemplate.getResponsible() != null))){
        		return true;
        	}
    	if((null != newTemplate.getAuditor() && !newTemplate.getAuditor().equals(oldTemplate.getAuditor()) ||
        		(null == newTemplate.getAuditor() && oldTemplate.getAuditor() != null))){
        		return true;
        	}
    	
	    if((null != newTemplate.getConsultant() && !newTemplate.getConsultant().equals(oldTemplate.getConsultant()) ||
	    		(null == newTemplate.getConsultant() && oldTemplate.getConsultant() != null))){
	    		return true;
	    	}
		if((null != newTemplate.getInform() && !newTemplate.getInform().equals(oldTemplate.getInform()) ||
	    		(null == newTemplate.getInform() && oldTemplate.getInform() != null))){
	    		return true;
	    	}
    	return false;
    }
    
   @Override
   public List<CtpTemplate> getSystemFormTemplates(long memberId)throws BusinessException{
        String hql ="select t from CtpTemplate t where t.moduleType = :moduleType and  t.bodyType = :bodyType  and  t.memberId = :memberId and t.delete = :delete and t.system = :system ";
        Map<String,Object> m = new HashMap<String,Object>();
        m.put("memberId",memberId);
        m.put("delete",Boolean.FALSE);
        m.put("moduleType",ModuleType.collaboration.getKey());
        m.put("bodyType",String.valueOf(MainbodyType.FORM.getKey()));
        m.put("system", Boolean.TRUE);
        return DBAgent.find(hql,m);
    }
    @Override
    @ProcessInDataSource(name = DataSourceName.BASE)
    public void saveCtpTemplates(List<CtpTemplate> ctpTemplates) throws BusinessException {
        DBAgent.saveAll(ctpTemplates);
        if(Strings.isNotEmpty(ctpTemplates)){
            for(CtpTemplate temp:ctpTemplates){
                templateCacheManager.addCacheTemplate(temp);
            }
        }
    }
    @Override
    public void saveCtpTemplateAuths(List<CtpTemplateAuth> ctpTemplateAuth) throws BusinessException {
        List<CtpTemplateAuth> al = new ArrayList<CtpTemplateAuth>();
        for( CtpTemplateAuth auth : ctpTemplateAuth){
            if(auth.getAccountId() == null){
                V3xOrgEntity e = orgManager.getEntity(auth.getAuthType(),auth.getAuthId());
                if(e!=null){
                    auth.setAccountId(e.getOrgAccountId());
                }
            }
            al.add(auth);
        }
        if(Strings.isEmpty(al)){
        	return;
        }
        DBAgent.saveAll(al);
        for (CtpTemplateAuth ctpTemplateAuth2 : al) {
			templateCacheManager.addCacheAclTemplate(ctpTemplateAuth2);
		}
        //跟新模板相关的授权
        CtpTemplate template = new CtpTemplate();
        template.setId(ctpTemplateAuth.get(0).getModuleId());
        this.synchronizeTemplateAuthCache(template,ctpTemplateAuth);
    }
    
    @Override
    public List<CtpTemplate> getSystemTemplates(List<Long> templeteIds) {
    	List<CtpTemplate> resultList=new ArrayList<CtpTemplate>();
        if(templeteIds != null && templeteIds.size() !=0){
        	for (Long id : templeteIds) {
        		CtpTemplate template = templateCacheManager.getCtpTemplate(id);
        		if (template != null) {
        			resultList.add(template);
        		}
        	}
            return resultList;
        }
        return resultList;
    }
    
    @Override
    public List<CtpTemplate> getCtpTemplateListByIdsWithSub(List<Long> ids)throws BusinessException{
        List<CtpTemplate> resultList=new ArrayList<CtpTemplate>();
        if(ids != null && ids.size() !=0){
            List<Long>[] arr=Strings.splitList(ids, 900);
            Map<String,Object> parameter = new HashMap<String,Object>();
            for(int i = 0 ; i < arr.length;i++){
                String hql = "from CtpTemplate where id in (:ids) or formParentid in (:ids)";
                parameter.put("ids", arr[i]);
                List<CtpTemplate> result= (List<CtpTemplate>)DBAgent.find(hql, parameter);
                resultList.addAll(result);
            }
            return resultList;
        }
        return resultList;
    }
    
    
    @Override
    public List<CtpTemplate> getTemplatesByIds(List<Long> ids)throws BusinessException{
        List<CtpTemplate> resultList=new ArrayList<CtpTemplate>();
        if(ids != null && ids.size() !=0){
            List<Long>[] arr=Strings.splitList(ids, 900);
            Map<String,Object> parameter = new HashMap<String,Object>();
            for(int i = 0 ; i < arr.length;i++){
                String hql = "from CtpTemplate where id in (:ids)";
                parameter.put("ids", arr[i]);
                List<CtpTemplate> result= (List<CtpTemplate>)DBAgent.find(hql, parameter);
                resultList.addAll(result);
            }
            return resultList;
        }
        return resultList;
    }

    @Override
    public CtpTemplate getTemplateByWorkflowId(Long workflowId) throws BusinessException {
        String hql = "from CtpTemplate where workflowId =:workflowId";
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("workflowId", workflowId);
        List<CtpTemplate> list = DBAgent.find(hql, params);
        for (CtpTemplate ctpTemplate : list) {//取第一个
            return ctpTemplate;
        }
        return null;
    }
    
    
    @Override
    public CtpTemplateHistory getTemplateHistoryByWorkflowId(Long workflowId) {
        return templateDao.getTemplateHistoryByWorkflowId(workflowId);
    }
    
    public List<CtpTemplate> getTemplateByProjectId(long projectId)throws BusinessException{
        return getTempletesByPropectId(projectId,null);
    }
    public FlipInfo getTemplateByProjectId(FlipInfo flipInfo,Map<String,String> query)throws BusinessException{
         String projectId = query.get("projectId");
         if(Strings.isNotBlank(projectId)){
             List<CtpTemplate> list  = getTempletesByPropectId(Long.valueOf(projectId),flipInfo);
             if(flipInfo!=null){
                 flipInfo.setData(list);
             }
         }
        return flipInfo;
    }
    public List<CtpTemplate>  getTempletesByPropectId(long projectId,FlipInfo flipInfo)throws BusinessException{

        User user = AppContext.getCurrentUser();
        long currentUserId = user.getId();

        // 我能访问的所有Id
        List<Long> domainIds = null;
            domainIds = this.orgManager.getUserDomainIDs(currentUserId,V3xOrgEntity.VIRTUAL_ACCOUNT_ID, 
                    ORGENT_TYPE.Member.name(), 
                    ORGENT_TYPE.Department.name(), 
                    ORGENT_TYPE.Account.name(), 
                    ORGENT_TYPE.Team.name(),
                    ORGENT_TYPE.Post.name(), 
                    ORGENT_TYPE.Level.name());

        StringBuilder sb = new StringBuilder();
        sb.append("select DISTINCT");
        sb.append(" t.id");
        sb.append(",t.categoryId");
        sb.append(",t.createDate");
        sb.append(",t.modifyDate");
        sb.append(",t.description");
        sb.append(",t.orgAccountId");
        sb.append(",t.sort");
        sb.append(",t.state");
        sb.append(",t.subject");
        sb.append(",t.type");
        sb.append(",t.bodyType");

        sb.append(" from CtpTemplate as t, CtpTemplateAuth as a ");
        sb.append(" where");      
        sb.append(" t.id=a.moduleId ");
        sb.append(" and (t.moduleType=:categoryType)");
        sb.append(" and (t.projectId=:projectId)"); 
        sb.append(" and (t.system=:system)"); // 系统模板用true,个人模板为false
        sb.append( " and (t.state=:state)");
        sb.append(" and (a.authId in (:domainIds))");
        sb.append(" order by t.sort,t.modifyDate,t.createDate" );

        Map<String, Object> namedParameterMap = new HashMap<String, Object>();
        namedParameterMap.put("domainIds", domainIds);
        namedParameterMap.put("categoryType", ModuleType.collaboration.ordinal());
        namedParameterMap.put("projectId", projectId);
        namedParameterMap.put("system", Boolean.TRUE);
        namedParameterMap.put("state", TemplateEnum.State.normal.ordinal());
        
        List<CtpTemplate> result = new ArrayList<CtpTemplate>();
        List<Object[]> list = null;
        if(flipInfo!=null){
            list = DBAgent.find(sb.toString(),namedParameterMap,flipInfo);
        }else{
            list = DBAgent.find(sb.toString(),namedParameterMap);
        }
        if (list != null) {
            for (Object[] o : list) {

                CtpTemplate template = new CtpTemplate();
                int n = 0;
                template.setId((Long) o[n++]);
                template.setCategoryId((Long) o[n++]);
                template.setCreateDate((Timestamp) o[n++]);
                template.setModifyDate((Timestamp) o[n++]);
                template.setDescription((String) o[n++]);
                template.setOrgAccountId((Long) o[n++]);
                template.setSort((Integer) o[n++]);
                template.setState((Integer) o[n++]);
                template.setSubject((String) o[n++]);
                template.setType((String) o[n++]);
                template.setBodyType((String) o[n++]);
                template.setMemberId(0L);
                template.setModuleType(ApplicationCategoryEnum.collaboration.getKey());
                result.add(template);
            }
        }

        return result;
    
    }
    private List<List<Long>> splitList(List<Long> list ,int count){
        List<List<Long>> result = new ArrayList<List<Long>>();
        if(list != null ){
            if(list.size() <= count){
                result.add(list);
            }else{
                int size = list.size();
                for(int i = 0 ; ;){
                    if(i+count >= size){
                        result.add(list.subList(i, size));
                        break;
                    }else{
                        result.add(list.subList(i, count));
                    }
                    i+= count;
                }
            }
        }
        return result;
    }
    @Override
    public List<CtpTemplateAuth> getCtpTemplateAuths(Long moduleId, Integer moduleType) throws BusinessException {
       return templateDao.getCtpTemplateAuths(moduleId, moduleType);
    } 
    
    @Override
    public List<CtpTemplateAuth> getCtpTemplateAuths(List<Long> authId)throws BusinessException {
        return templateDao.getCtpTemplateAuths(authId);
    }
    
    @Override
    public void deleteCtpTemplateAuths(Long moduleId, Integer moduleType) throws BusinessException {
        templateDao.deleteCtpTemplateAuths(moduleId, moduleType);
        List<Long> templateIds = new ArrayList<Long>();
        templateIds.add(moduleId);
        templateCacheManager.deleteCacheTemplateAuthByTemplateIds(templateIds);
    }

    

    @Override
    public boolean isTemplateStateEnabled(CtpTemplate template) {

        if(template == null || template.isDelete()){
            return false;
        }
        
        //个人模板
        boolean isSelfTemplate = TemplateUtil.isSelfTemplate(template);
        if(isSelfTemplate){
            return true;
        }
        //停启用
        if(Integer.valueOf(TemplateEnum.State.invalidation.ordinal()).equals(template.getState())){
            return false;
        }
        

        if (template.getFormAppId() != null) {
            boolean templateEnable = capFormManager.isEnabled(template.getFormAppId());
            if (!templateEnable) {
                return false;
            }
        }
        return true;
    }
    
    
    @Override
    public boolean isTemplateEnabled(Long templeteId, Long userId) throws BusinessException {
        
    	CtpTemplate templete = templateCacheManager.getCtpTemplate(templeteId);
        
    	return isTemplateEnabled(templete, userId);
    }
    
	public boolean isTemplateEnabled(CtpTemplate templete, Long userId) throws BusinessException {

		if (templete == null) {
			return false;
		}
		//如果模板已经被停用了或者被删除了那么就是不可用的数据
		if (templete.isDelete()
				|| Integer.valueOf(TemplateEnum.State.invalidation.ordinal()).equals(templete.getState())) {
			return false;
		}
		Long formAppid = (templete.getFormAppId() == null || templete.getFormAppId() == 0
				|| templete.getFormAppId() == -1) ? null : templete.getFormAppId();
		Long formParentId = (templete.getFormParentid() == null || templete.getFormParentid() == 0
				|| templete.getFormParentid() == -1) ? null : templete.getFormParentid();

		boolean isSelfTemplate = TemplateUtil.isSelfTemplate(templete);

		if (isSelfTemplate) {
			return true;
		}
		return isTemplateEnabled(templete.getId(), formAppid, formParentId, userId, templete.isSystem());

	}

	
	
    public Map<Long, Boolean> isTemplateEnabled(List<CtpTemplate> templeteList, Long userId) throws BusinessException {
        
        Map<Long, Boolean> templateId2enabled = new HashMap<Long, Boolean>();
        List<Long> templeteIds = CommonTools.getIds(templeteList);
        Integer invalidation = Integer.valueOf(TemplateEnum.State.invalidation.ordinal());

        
        List<Long> acls  = getSystemTemplateIdsByAcl(userId);
        HashMap<Long, Boolean> aclIdMap  = new HashMap<Long,Boolean>();
        if(Strings.isNotEmpty(acls)){
        	for(Long id : acls){
        		aclIdMap.put(id, true);
        	}
        }
        List<Long> hasAclTempleteIds = new ArrayList<Long>();
        
        if(Strings.isNotEmpty(templeteIds)){
        	for(Long id:templeteIds){
        		boolean isAcl = aclIdMap.get(id) == null ? false : true;
        		if(isAcl){
        			hasAclTempleteIds.add(id);
        		}
        	}
        }

        for (CtpTemplate templete : templeteList) {
            
        	if (templete.isSystem()) {
                if (invalidation.equals(templete.getState())) {
                    templateId2enabled.put(templete.getId(), Boolean.FALSE);
                    continue;
                }

                boolean isEnable = hasAclTempleteIds.contains(templete.getId());
                if(!isEnable){
                	templateId2enabled.put(templete.getId(),Boolean.FALSE );
                	continue;
                }
                else if (TemplateUtil.isForm(templete.getBodyType())) {// 查询表单是否开启
                    try {
                        if (templete.getFormAppId() != null) {
                            boolean templateEnable = capFormManager.isEnabled(templete.getFormAppId());
                            if (!templateEnable) {
                                templateId2enabled.put(templete.getId(), Boolean.FALSE);
                                continue;
                            }
                        }

                    } catch (Exception e) {
                        LOG.error(e.getMessage(), e);
                    }
                }
                templateId2enabled.put(templete.getId(), Boolean.TRUE);
            } 
            
            else { // 个人模板不判断
                Long parentId = templete.getFormParentid();
                if (parentId != null) {// 用系统模版另存为个人模版需要判断
                    Boolean isEnabled = isTemplateEnabled(parentId, userId);
                    templateId2enabled.put(templete.getId(), isEnabled);
                } else {
                    templateId2enabled.put(templete.getId(), Boolean.TRUE);
                }
            }
        }
        return templateId2enabled;
    }
	
    public boolean isTemplateEnabled(Long templateId, Long _formAppid, Long _templateParentId, Long userId,
            Boolean isSystem) throws BusinessException {

        Long formAppid = (_formAppid == null || _formAppid == 0 || _formAppid == -1) ? null : _formAppid;
        Long formParentId = (_templateParentId == null || _templateParentId == 0 || _templateParentId == -1) ? null
                : _templateParentId;

        if (templateId == null && formParentId == null) {
            return false;
        }


        if (formAppid != null) {
        	CtpTemplate templete = getCtpTemplateFromCache(templateId);
            if (templete != null && templete.getModuleType() != ModuleType.info.getKey()) {
				try {
					boolean templateEnable = capFormManager.isEnabled(formAppid);
					if (!templateEnable) {
						return false;
					}
				} catch (Exception e) {
					LOG.error(e.getMessage(), e);
				} 
			}
        }

        if (isSystem != null && !isSystem && (formParentId == null)) {
            return true;
        }

        Long checkTemplateId = formParentId != null ? formParentId : templateId;
        boolean hasAcl = hasAccSystemTempletes(checkTemplateId, userId);
        

        if (!hasAcl) {
            return false;
        }

        return true;
    }
	
	private boolean hasAccSystemTempletes(Long templeteId, Long userId) throws BusinessException {
		
		boolean isAcl = templateCacheManager.isAclCacheSystemTemplate(templeteId, userId);
		return isAcl;
		
	}
	
   /* private boolean hasAccSystemTempletes(Long tempId, Long userId, List<Long> domainIds) {
        if (tempId == null) {
            return true;
        }
        if (domainIds == null || domainIds.isEmpty()) {
            return false;
        }
        // 我能访问的所有Id(change objectId to moduleId)
        String hql = "select t.id from CtpTemplate as t,CtpTemplateAuth as a where (t.id=a.moduleId) and t.system = :isSystem and" + " t.state= :state and t.id= :templateId and (a.authId in(:domainIds))";
        Map<String,Object> map = new HashMap<String,Object>();
        map.put("isSystem", Boolean.TRUE);
        map.put("state", TemplateEnum.State.normal.ordinal());
        map.put("templateId", tempId);
        map.put("domainIds", domainIds);
        return DBAgent.count(hql, map) > 0;
    }*/
    
    private List<Long> hasAccSystemTempletes(List<Long> tempIds, Long userId) throws BusinessException {
        // 我能访问的所有Id(change objectId to moduleId)
        List<Long> domainIds = orgManager.getUserDomainIDs(userId, 1L, ORGENT_TYPE.Member.name(), 
                ORGENT_TYPE.Department.name(), ORGENT_TYPE.Account.name(), ORGENT_TYPE.Team.name(),
                ORGENT_TYPE.Post.name(), ORGENT_TYPE.Level.name());
        String hql = "select distinct t.id from CtpTemplate as t,CtpTemplateAuth as a where (t.id=a.moduleId) and t.system = :isSystem and" + " t.state= :state and t.id in(:tempIds) and (a.authId in(:domainIds))";
        Map<String,Object> map = new HashMap<String,Object>();
        map.put("isSystem", Boolean.TRUE);
        map.put("state", TemplateEnum.State.normal.ordinal());
        map.put("tempIds", tempIds);
        map.put("domainIds", domainIds);
        if(Strings.isEmpty(tempIds)){
            return Collections.emptyList();
        }
        return DBAgent.find(hql, map);
    }
	
	@Override
    public boolean isTemplateCategoryManager(long memberId, long loginAccountId, CtpTemplateCategory c) throws BusinessException {
        if (c == null)
            return false;
        Set<CtpTemplateAuth> a = this.getCategoryAuths(c);
        for (CtpTemplateAuth auth : a) {
            if (auth.getAuthId().longValue() == memberId
                    && (c.getOrgAccountId() != null && c.getOrgAccountId().longValue() == loginAccountId)) {
                return true;
            }
        }
        return false;
    }
	@Override
	public void deleteCacheTemplateAuthByTemplateIds(List<Long> templateIds){
		templateCacheManager.deleteCacheTemplateAuthByTemplateIds(templateIds);
	}

    @ProcessInDataSource(name = DataSourceName.BASE)
	public void deleteCtpTemplateByTemplateIds(List<Long> templateIds) throws BusinessException{
		templateDao.deleteCtpTempleteByTemplateIds(templateIds);
		deleteCacheTemplateAuthByTemplateIds(templateIds);
	}
	public void deleteAuthsByModuleId(Long id) {
		templateDao.deleteAuthsByModuleId(id);
	}
	/**
	 * 获取某个模板分类管理员的所有模板分类列表
	 */
	@Override
    @ProcessInDataSource(name = DataSourceName.BASE)
    public List<CtpTemplateCategory> getTemplateCategoryListByMemberId(long memberId,ModuleType type) throws BusinessException {
    	List<CtpTemplateCategory> result=new ArrayList<CtpTemplateCategory>();
    	V3xOrgMember member=orgManager.getMemberById(memberId);
    	if(null!=member){
    		Long accountId=member.getOrgAccountId();
    		List<CtpTemplateCategory> list=getCategorys(accountId,type);
    		for(CtpTemplateCategory c:list){
    			Set<CtpTemplateAuth> a = this.getCategoryAuths(c);
    			for (CtpTemplateAuth auth : a) {
    				if (auth.getAuthId().longValue() == memberId && (c.getOrgAccountId() != null )) {
    					result.add(c);
    					break;
    				}
    			}
    		}
    	}
    	return result;
    }
	
	/**
	 * 获取某个模板分类管理员的所有模板分类列表(包括所在单位和兼职单位的所有模板)
	 */
	@Override
    public List<CtpTemplateCategory> getAllTemplateCategoryListByMemberId(long memberId,ModuleType type) throws BusinessException {
    	List<CtpTemplateCategory> result=new ArrayList<CtpTemplateCategory>();
    	V3xOrgMember member=orgManager.getMemberById(memberId);
    	if(null!=member){
    		List<Long> accountIds=new ArrayList<Long>();
    		//主岗单位的id
    		Long mainAccountId=member.getOrgAccountId();
    		accountIds.add(mainAccountId);
    		List<MemberPost> memberPostList = member.getConcurrent_post();
    		if(null!=memberPostList && memberPostList.size()>0){
    			for(MemberPost l:memberPostList){
    				//兼职单位的id
    				accountIds.add(l.getOrgAccountId());
    			}
    		}
    		
    		for(Long accountId:accountIds){
    			List<CtpTemplateCategory> list=getCategorys(accountId,type);
    			for(CtpTemplateCategory c:list){
    				Set<CtpTemplateAuth> a = this.getCategoryAuths(c);
    				for (CtpTemplateAuth auth : a) {
    					if (auth.getAuthId().longValue() == memberId && (c.getOrgAccountId() != null )) {
    						result.add(c);
    						break;
    					}
    				}
    			}
    		}
    	}
    	return result;
    }
    public java.util.Set<CtpTemplateAuth> getCategoryAuths(CtpTemplateCategory c) throws BusinessException {
        Set<CtpTemplateAuth> categoryAuths = new HashSet<CtpTemplateAuth>();
        //根据moduleID去查询权限
        List<CtpTemplateAuth> list = templateDao.getCtpTemplateAuths(c.getId(), ModuleType.collaboration.getKey());
        list.addAll(templateDao.getCtpTemplateAuths(c.getId(), -1));
        for (CtpTemplateAuth a : list) {
            categoryAuths.add(a);
        }
        return categoryAuths;
    }
	 
	@CheckRoleAccess(roleTypes = { Role_NAME.TtempletManager,Role_NAME.AccountAdministrator,Role_NAME.InfoManager})
    public List<CtpTemplate> getCtpTemplates(Long categoryId, String name,Long templateId, Boolean isSystem){
	    return templateDao.checkNameRepeat(categoryId,  name, templateId,  isSystem);
	}
	  
	 
	@Override
	public FlipInfo getSystemTempletes(FlipInfo flipInfo,Map<String, String> params) {
	    User user = AppContext.getCurrentUser();
	    templateDao.selectAllSystemTempletes(flipInfo,params, user.getLoginAccount());
		return flipInfo;
	}
	
	@Override
	public StringBuffer categoryHTML(Long accountId){
	    return categoryHTML(accountId,true,"");
	}

	
    public  List<CtpTemplateCategory> getCategory(Long accountId, int type) throws BusinessException {
    	List<ModuleType> _type = new ArrayList<ModuleType>();
    	_type.add(ModuleType.getEnumByKey(type));
        List<CtpTemplateCategory> templateCategorys = this.getCategorys(accountId, _type);
        return templateCategorys;
    }
    
    @Override
    public StringBuffer categoryHTML(Long accountId,boolean isIncludeEdoc,String moduleType) {
    	return categoryHTML(accountId,isIncludeEdoc,moduleType,null);
    }
    @Override
    public StringBuffer categoryHTML(Long accountId,boolean isIncludeEdoc,String moduleType,List<Long> categoryIds) {
        List<String> categorys = new ArrayList<String>();
        boolean isV5Member = AppContext.getCurrentUser().isV5Member();
        categorys.add(String.valueOf(ModuleType.collaboration.getKey()));
//        boolean hasEdocPlugin = AppContext.hasPlugin("edoc");
        boolean hasGovdocdocPlugin = AppContext.hasPlugin("govdoc");
        List<CtpTemplateCategory> templeteCategories = getCategorys(accountId, ModuleType.collaboration);//协同
        
        if (isIncludeEdoc && hasGovdocdocPlugin && isV5Member) {
        	if ("".equals(moduleType)) {
        		moduleType = "4";
        	}
        	List<String> types = Strings.newArrayList(moduleType.split(",")); 
            List<CtpTemplateCategory> govdocRec=null;
            List<CtpTemplateCategory> govdocSend=null;
            List<CtpTemplateCategory> govdocSign=null;
			try {
				//公文下边的子分类
				govdocRec = this.getCategory(accountId,Integer.parseInt(String.valueOf(ModuleType.govdocRec.getKey())));
				govdocSend = this.getCategory(accountId,Integer.parseInt(String.valueOf(ModuleType.govdocSend.getKey())));
				govdocSign = this.getCategory(accountId,Integer.parseInt(String.valueOf(ModuleType.govdocSign.getKey())));
			} catch (Exception e) {
				LOG.error("查询公文模板分类出错",e);
			}
			CtpTemplateCategory ctpTemplateCategory = new CtpTemplateCategory();
			if (types.contains(String.valueOf(ModuleType.govdoc.getKey())) || types.contains(String.valueOf(ModuleType.edoc.getKey())) || types.contains(String.valueOf(ModuleType.govdocSend.getKey()))) {
				ctpTemplateCategory.setId(Long.valueOf(ModuleType.govdocSend.getKey()));
				ctpTemplateCategory.setParentId(TemplateCategoryConstant.edocRoot.key());
				ctpTemplateCategory.setName(ResourceUtil.getString("template.edocsend.label"));
				templeteCategories.add(ctpTemplateCategory);
				templeteCategories.addAll(govdocSend);
			}
			if (types.contains(String.valueOf(ModuleType.govdoc.getKey())) || types.contains(String.valueOf(ModuleType.edoc.getKey())) || types.contains(String.valueOf(ModuleType.govdocRec.getKey()))) {
				ctpTemplateCategory = new CtpTemplateCategory();
				ctpTemplateCategory.setId(Long.valueOf(ModuleType.govdocRec.getKey()));
				ctpTemplateCategory.setParentId(TemplateCategoryConstant.edocRoot.key());
				ctpTemplateCategory.setName(ResourceUtil.getString("template.edocrec.label"));
				templeteCategories.add(ctpTemplateCategory);
				templeteCategories.addAll(govdocRec);
			}
			if (types.contains(String.valueOf(ModuleType.govdoc.getKey())) || types.contains(String.valueOf(ModuleType.edoc.getKey())) || types.contains(String.valueOf(ModuleType.govdocSign.getKey()))) {
				ctpTemplateCategory = new CtpTemplateCategory();
				ctpTemplateCategory.setId(Long.valueOf(ModuleType.govdocSign.getKey()));
				ctpTemplateCategory.setParentId(TemplateCategoryConstant.edocRoot.key());
				ctpTemplateCategory.setName(ResourceUtil.getString("template.edocsign.label"));
				templeteCategories.add(ctpTemplateCategory);
				templeteCategories.addAll(govdocSign);
			}
            
            categorys.add(String.valueOf(TemplateCategoryConstant.edocRoot.key()));
        }
        // 查询协同时已经将表单包括了
        if(null != AppContext.getRequestContext("outerUserForShowCategories")){
        	List<CtpTemplateCategory>  listOuter= (List)AppContext.getRequestContext("outerUserForShowCategories");
        	templeteCategories.addAll(listOuter);
        	AppContext.putRequestContext("outerUserForShowCategories", null);
        }
        categorys.add(String.valueOf(ModuleType.form.getKey()));
        categorys.add(String.valueOf(-1L));//外单位的模板分类
        List<CtpTemplateCategory> newTempleteCategories = new ArrayList<CtpTemplateCategory>();
        if (null != categoryIds && categoryIds.size() > 0) {
        	for (CtpTemplateCategory category : templeteCategories) {
        		if (categoryIds.contains(category.getId())) {
        			newTempleteCategories.add(category);
        		}
        	}
        } else {
        	newTempleteCategories = templeteCategories;
        }
        
        StringBuffer categoryHTML = categoryHTML(newTempleteCategories, categorys, 0);
        return categoryHTML;
    }
    
    @Override
    public StringBuffer categoryHTMLEdoc(String moduleType) {
    	List<CtpTemplateCategory> templeteCategories = new ArrayList<CtpTemplateCategory>();
        if (AppContext.hasPlugin("edoc")) {
        	CtpTemplateCategory ctpTemplateCategory = new CtpTemplateCategory();
            if(String.valueOf(ModuleType.edoc.getKey()).equals(moduleType) || String.valueOf(ModuleType.edocSend.getKey()).equals(moduleType)){
            	ctpTemplateCategory.setId(Long.valueOf(ModuleType.edocSend.getKey()));
            	ctpTemplateCategory.setParentId(TemplateCategoryConstant.edocRoot.key());
            	ctpTemplateCategory.setName(ResourceUtil.getString("template.edocsend.label"));
            	templeteCategories.add(ctpTemplateCategory);
            }
            if(String.valueOf(ModuleType.edoc.getKey()).equals(moduleType) || String.valueOf(ModuleType.edocRec.getKey()).equals(moduleType)){
            	ctpTemplateCategory = new CtpTemplateCategory();
            	ctpTemplateCategory.setId(Long.valueOf(ModuleType.edocRec.getKey()));
            	ctpTemplateCategory.setParentId(TemplateCategoryConstant.edocRoot.key());
            	ctpTemplateCategory.setName(ResourceUtil.getString("template.edocrec.label"));
            	templeteCategories.add(ctpTemplateCategory);
            }
            if(String.valueOf(ModuleType.edoc.getKey()).equals(moduleType) || String.valueOf(ModuleType.edocSign.getKey()).equals(moduleType)){
            	ctpTemplateCategory = new CtpTemplateCategory();
            	ctpTemplateCategory.setId(Long.valueOf(ModuleType.edocSign.getKey()));
            	ctpTemplateCategory.setParentId(TemplateCategoryConstant.edocRoot.key());
            	ctpTemplateCategory.setName(ResourceUtil.getString("template.edocsign.label"));
            	templeteCategories.add(ctpTemplateCategory);
            }
        }
        
        List<String> categorys = new ArrayList<String>();
        categorys.add(String.valueOf(TemplateCategoryConstant.edocRoot.key()));
        
        StringBuffer categoryHTML = categoryHTML(templeteCategories, categorys, 0);
       
        return categoryHTML;
    }

    @Override
    public StringBuffer categoryHTML(List<CtpTemplateCategory> categories, List<String> currentNode, int level) {
        StringBuffer html = new StringBuffer();
        List<TemplateCategoryTreeVO> treeVos = this.coverCategoryToTreeVO(categories);
        categoryHTML2(html, treeVos, currentNode, level);
        return html;
    }
    
    /**
     * 将分类对象转换成用于组装树结构的VO对象
     * 对于名称相同的做合并操作，id合并成id1,id2,id3格式
     * @param categories
     * @return
     */
    private List<TemplateCategoryTreeVO> coverCategoryToTreeVO(List<CtpTemplateCategory> categories){
    	List<TemplateCategoryTreeVO> treeVos = new ArrayList<TemplateCategoryTreeVO>();
    	if(Strings.isEmpty(categories)){
    		return treeVos;
    	}
    	
    	for(CtpTemplateCategory category : categories){
    		if (category.isDelete() != null && category.isDelete()){
    			continue;
    		}
    		treeVos.add(new TemplateCategoryTreeVO(category));
    	}
    	
    	return treeVos;
    }
    
    private void categoryHTML2(StringBuffer html, List<TemplateCategoryTreeVO> treeVos, List<String> currentNode, int level) {
        Map<String, String> name_id = new HashMap<String, String>();
    	for(TemplateCategoryTreeVO treeVo : treeVos){
    		//防护，死循环的时候跳出，记录日志！
            if(level >= 50) {
                StringBuilder sb = new StringBuilder();
                sb.append("递归层次》100，程序自动退出！");
                if(Strings.isNotEmpty(currentNode)){
                    sb.append("currentNode:");
                    for(String id : currentNode){
                        sb.append(id).append(",");
                    }
                    sb.append("categoryid:"+treeVo.getId()+";category.parentId:"+treeVo.getParentId());
                }
                LOG.error(sb);
                break;
            }
            
        	String parentId = treeVo.getParentId();
        	if(currentNode.contains(parentId)){
        		String map_id = name_id.get(treeVo.getName());
        		if(Strings.isNotBlank(map_id)){
        			map_id += "," + treeVo.getId();
        		}else{
        			map_id = treeVo.getId();
        		}
        		name_id.put(treeVo.getName(), map_id);
        	}
        }
    	StringBuffer spaceHtml = new StringBuffer();
    	for (int i = 0; i < level; i++) {
    		spaceHtml.append("&nbsp;&nbsp;&nbsp;&nbsp;");
        }
    	
    	Set<String> map_names = name_id.keySet();
    	for(String name : map_names){
    		String ids = name_id.get(name);
    		if(name.contains("|")){
    			String[] categoryIds = name.split("\\|");
    			name = categoryIds[0];
    		}
    		html.append("<option value='" + ids + "' title='" + Strings.toHTML(name)+ "'>");
    		html.append(spaceHtml.toString());
    		html.append(Strings.toHTML(name) + "</option>\n");
    		
    		List<String> nextNode = new ArrayList<String>();
    		for(String id : ids.split(",")){
    			nextNode.add(id);
    		}
    		categoryHTML2(html,treeVos, nextNode, level + 1);
    	}
    }
	
    @Override
    public List<CtpTemplate> getSystemTemplatesByMaxAcl(Long accountId, List<ModuleType> moduleTypes) throws BusinessException {



        List<Long> accountIds = new ArrayList<Long>();
        if (orgManager.isGroup() && (accountId == null || OrgConstants.GROUPID.equals(accountId))) {
        	List<V3xOrgAccount> allAccount = orgManager.getAllAccounts();
        	for (V3xOrgAccount account : allAccount) {
        		accountIds.add(account.getId());
        	}
        } else {
        	accountIds.add(OrgConstants.GROUPID);
        	if(accountId != null) {
        	    accountIds.add(accountId);
        	}
        }
        List<Object[]> result = templateDao.getAllSystemTempletesByAclAndSpecialAuthID(accountIds, moduleTypes, getSelectFieldsHqlStr().toString());
        List<CtpTemplate> resultList =  parseObjArray2Templetes(result);
        
        //去掉重复的模板 
        List<CtpTemplate> templatesTemp = removeDuplicateTemplate(resultList);
        return templatesTemp;
    }
    
    // 去掉重复的模板
    private List<CtpTemplate> removeDuplicateTemplate(List<CtpTemplate> templates) {
        Set<Long> tempIds = new HashSet<Long>();
        List<CtpTemplate> templatesTemp = new ArrayList<CtpTemplate>();
        for (Iterator<CtpTemplate> it = templates.iterator(); it.hasNext();) {
            CtpTemplate template = it.next();
            if (tempIds.contains(template.getId())) {
                continue;
            } else {
                templatesTemp.add(template);
                tempIds.add(template.getId());
            }
        }
        return templatesTemp;
    }
   
    
	@Override
    public List<CtpTemplate> getAllSystemTemplatesByMaxAcl() throws BusinessException {
	    
	    List<Long> accountIds = new ArrayList<Long>();
	    // 获取集团下所有人
	    List<V3xOrgAccount> allAccount = orgManager.getAllAccounts();
        for (V3xOrgAccount account : allAccount) {
            accountIds.add(account.getId());
        }
	    
	    List<ModuleType> moduleTypes = new ArrayList<ModuleType>();
	    moduleTypes.add(ModuleType.collaboration); 
	    moduleTypes.add(ModuleType.form);
	    
	    List<Object[]> result = templateDao.getAllSystemTempletesByAclAndSpecialAuthID(accountIds, moduleTypes, getSelectFieldsHqlStr().toString());
	    List<CtpTemplate> resultList = parseObjArray2Templetes(result);
	    
	    // 去掉重复的模板
        List<CtpTemplate> templatesTemp = removeDuplicateTemplate(resultList);
        return templatesTemp;
    }

    /**
	 * 将查询所得数组结果组装为表单模板集合
	 */
	private List<CtpTemplate> parseObjArray2Templetes(List<Object[]> list) {
    	List<CtpTemplate> result = new ArrayList<CtpTemplate>();
    	if (list != null) {
    		CtpTemplate templete = null;
            for (Object[] o : list) {
            	templete = new CtpTemplate();
                int n = 0;
                templete.setId((Long) o[n++]);
                templete.setCategoryId((Long) o[n++]);
                templete.setCreateDate((Timestamp) o[n++]);
                templete.setModifyDate((Timestamp) o[n++]);
                templete.setDescription((String) o[n++]);
                templete.setOrgAccountId((Long) o[n++]);
                templete.setSort((Integer) o[n++]);
                templete.setState((Integer) o[n++]);
                templete.setSubject((String) o[n++]);
                templete.setType((String) o[n++]);
                templete.setBodyType((String) o[n++]);
                templete.setModuleType((Integer) o[n++]);
                templete.setMemberId((Long) o[n++]);
                templete.setStandardDuration((Long)o[n++]);
                templete.setWorkflowId((Long) o[n++]);
                templete.setBody((Long) o[n++]);
                templete.setSystem((Boolean) o[n++]);
                templete.setDelete((Boolean) o[n++]);
                templete.setFormAppId((Long) o[n++]);                
                templete.setBelongOrg((Long) o[n++]);
                templete.setPublishTime((Date) o[n++]);
                
                result.add(templete);
            }
        }
        return result;
    }

	/**
	 * 模板左右选择查询模板的方法
	 */
	 private  List<CtpTemplate> getSystemTemplets(Map params, List<ModuleType> moduleTypes, Long memberId , Long currentAccountId , String condition,String textfield)throws BusinessException{
	        
	        String reportId = String.valueOf(params.get("reportId"));
	      
	        	        
	        String scope=params.get("scope").toString();
	       
		    List<CtpTemplate> templates  = null ;
		    if(TemplateChooseScope.MemberUse.name().equalsIgnoreCase(scope)){
		    	//by 单位管理员配置栏目的时候，已发栏目获取全部的模板
		    	if (orgManager.isAdministrator()) {
		    		//单位管理员能取得所有模板
		    		templates = getSystemTemplatesByMaxAcl(currentAccountId, moduleTypes);
		    	} else {
		    		//用户能使用的模板
		    		templates   = getSystemTemplatesByAcl(memberId,moduleTypes);
		    	}
		    }else if(TemplateChooseScope.Done.name().equalsIgnoreCase(scope)){
		    	//设置代理人
		        templates = templateDao.getTemplatesByAffair(memberId, ModuleType.collaboration, StateEnum.col_done);
		    } else if (TemplateChooseScope.EdocDone.name().equalsIgnoreCase(scope)) {
		    	//设置代理人公文
		    	templates = templateDao.getTemplatesByAffair(memberId, ModuleType.edoc, StateEnum.col_done);
		    } else if(TemplateChooseScope.MemberAnalysis.name().equalsIgnoreCase(scope)){
		    	//用户能进行流程效率分析的模板
		    	if(AppContext.isAdministrator()){
		    		templates = getSystemTemplatesByMaxAcl(currentAccountId, moduleTypes);
		    	}else{
		    		if(getTemplateQuery4ReportHandler() == null){
		    		    throw new BusinessException("没有实现模板选择接口TemplateQuery4ReportHandler");
		    		}else{
		    		    Long rid = Strings.isBlank(reportId)?0l:Long.valueOf(reportId);
		    		    //选择全部的时候，F8需要传null
		    		    String m = String.valueOf(params.get("moduleType"));
		    		    templates = getTemplateQuery4ReportHandler().findAuthorizedTemplate(rid,memberId, StringUtil.checkNull(m)? null : Integer.valueOf(m));
		    		}
		    	}
		    }else if(TemplateChooseScope.MaxScope.name().equalsIgnoreCase(scope)){
		    	//能取到最大范围的模板《本单位制作的或者外单位授权给本单位的单位、部门、组、岗位》
			    V3xOrgMember member = orgManager.getMemberById(memberId);
			    //对于外部人员，只寻找他在部门和组范围内的权限
			    if(member.isV5External()){
				    templates = getSystemTemplatesByAcl(memberId,moduleTypes);
			    }else{
				    //内部管理员 直接查询所有的数据
			    	  templates = getSystemTemplatesByAccount(currentAccountId,moduleTypes);
			    }
			    Set<Long> ids = new HashSet<Long>();
			    for(CtpTemplate t : templates){
				    ids.add(t.getId());
			    }
		    	//外单位授权给本单位的部门、组、岗位
		    	List<CtpTemplate> ts = getSystemTemplatesByMaxAcl(currentAccountId, moduleTypes);
	            
		    	for(CtpTemplate t: ts){
		    		if(!ids.contains(t.getId())){
		    			templates.add(t);
		    			ids.add(t.getId());
		    		}
		    	}
		    }else if(TemplateChooseScope.ManageDep.name().equalsIgnoreCase(scope)){
		        //主管各部门
		        
		      //只找到当前发起者单位的
                List<V3xOrgDepartment> temp = orgManager.getDeptsByManager(memberId, currentAccountId);
                templates = findManageTemplats(temp,currentAccountId, moduleTypes);
		        
		    }else if(TemplateChooseScope.LeaderDep.name().equalsIgnoreCase(scope)){
                //分管各部门
		      //只找到当前发起者单位的
                List<V3xOrgDepartment> temp = orgManager.getDeptsByDeptLeader(memberId, currentAccountId);;
                templates = findManageTemplats(temp, currentAccountId, moduleTypes);
                
            }else if(TemplateChooseScope.EdocManagement.name().equalsIgnoreCase(scope)){
                //公文管理员
                templates = findEdocManagerTemplates(memberId, currentAccountId, moduleTypes, true);
                
            }else if(TemplateChooseScope.FormAdmin.name().equalsIgnoreCase(scope)){
                //表单管理员
                templates = findFormManagerTemplates(memberId,currentAccountId, moduleTypes, true);
            }else if(TemplateChooseScope.ColTempManagement.name().equalsIgnoreCase(scope)){
                //协同管理员
                templates = findColManagerTemplates(currentAccountId, memberId,null);
                
            }else if(TemplateChooseScope.FormAdminAndColManagement.name().equalsIgnoreCase(scope)){
                //表单管理和协同管理模板并集
                templates = findFormManagerTemplates(memberId, currentAccountId, moduleTypes, true);
                List<CtpTemplate> colTemplates = findColManagerTemplates(currentAccountId, memberId,null);
                if(templates == null){
                    templates = colTemplates;
                }else if(Strings.isNotEmpty(colTemplates)){
                    templates.addAll(colTemplates);
                }
            }else if(TemplateChooseScope.DrAdmin.name().equalsIgnoreCase(scope)){//数据关联管理员
                boolean isFormAdmin = orgManager.isRole(memberId, currentAccountId, OrgConstants.Role_NAME.FormAdmin.name());
                if (isFormAdmin) {
                    templates = getSystemTempletes(currentAccountId, moduleTypes, searchCondtion.hasAuth.name(), String.valueOf(memberId));
                }
            }else if(TemplateChooseScope.ProcessAssets.name().equalsIgnoreCase(scope)){
            	templates = this.getCtpTemplate(new HashMap<String, String>());
            }else if(TemplateChooseScope.ProcessAssetsSection.name().equalsIgnoreCase(scope)){
            	List<Long> templateIds = processAssetsManager.getAllTemplateIds();
            	
            	Set<CtpTemplate> set = new HashSet<CtpTemplate>();
            	set.addAll(this.getSystemTemplates(templateIds));
            	//普通人员需并入其可调用的模板
            	if(!AppContext.isAdministrator()){
            		//能取到最大范围的模板《本单位制作的或者外单位授权给本单位的单位、部门、组、岗位》
            		set.addAll(getSystemTemplatesByAcl(memberId,moduleTypes));
            	}
            	Iterator<CtpTemplate> it = set.iterator();
            	while(it.hasNext()){
            		CtpTemplate template = it.next();
            		if(!template.getType().equals("template") && !template.getType().equals("templete")){
            			it.remove();
            		}
            	}
            	templates = new ArrayList<CtpTemplate>(set);
            }else if (TemplateChooseScope.CurrentAccount.name().equalsIgnoreCase(scope)){
		    	//得到指定单位的所有模板(单位制作的)
		    	templates = getSystemTempletes(currentAccountId,moduleTypes, condition, textfield);
		    }
		    
	    	return templates;
	 }
	 
	 @Override
	 public List<CtpTemplate> getEdocSystemTemplates(long memberId, long accountId) throws BusinessException{
	     
	     List<ModuleType> moduleTypes = new ArrayList<ModuleType>();
	     moduleTypes.add(ModuleType.edoc);
	     moduleTypes.add(ModuleType.edocSend);
	     moduleTypes.add(ModuleType.edocRec);
	     moduleTypes.add(ModuleType.edocSign);
	     
	     moduleTypes.add(ModuleType.govdoc);
	     moduleTypes.add(ModuleType.govdocSend);
	     moduleTypes.add(ModuleType.govdocRec);
	     moduleTypes.add(ModuleType.govdocSign);
	     
	     
	     return filterTemplate(findEdocManagerTemplates(memberId, accountId, moduleTypes, true), accountId, moduleTypes);
	 }
	   
	 @Override
	 public List<CtpTemplate> getFormManagerTemplates(long memberId, long accountId) throws BusinessException{
	     List<ModuleType> moduleTypes = new ArrayList<ModuleType>();
         moduleTypes.add(ModuleType.form);
         return filterTemplate(findFormManagerTemplates(memberId, accountId, moduleTypes, true), accountId, moduleTypes);
	 }
	 
	 @Override
	 public List<CtpTemplate> getBusinessDesignerTemplates(long memberId, long accountId) throws BusinessException{
		 boolean isBusinessDesigner = orgManager.isRole(memberId, accountId, OrgConstants.Role_NAME.BusinessDesigner.name());
         List<CtpTemplate> templates = null;
         if(isBusinessDesigner){
             templates = filterTemplate(getSystemTempletes(accountId, Arrays.asList(ModuleType.form), "memberId", String.valueOf(memberId)), 
            		 accountId, Arrays.asList(ModuleType.form));
         }
         return templates;
	 }
	   
	 @Override
	 public List<CtpTemplate> getColManagerTemplates(long accountId, long memberId) throws BusinessException{

	     List<ModuleType> moduleList = new ArrayList<ModuleType>();
         moduleList.add(ModuleType.collaboration);
         
	     return filterTemplate(findColManagerTemplates(accountId, memberId,null), accountId, moduleList);
	 }
	   
	 @Override
	 public List<CtpTemplate> getTemplatesByManageDep(long memberId, long accountId, List<ModuleType> moduleTypes) throws BusinessException{
         
         List<V3xOrgDepartment> temp = orgManager.getDeptsByManager(memberId, accountId);
         return filterTemplate(findManageTemplats(temp, accountId, moduleTypes), accountId, moduleTypes);
	 }
	   
	 @Override
	 public List<CtpTemplate> getTemplatesByLeaderDep(long memberId, long accountId, List<ModuleType> moduleTypes) throws BusinessException{
	     
	     List<V3xOrgDepartment> temp = orgManager.getDeptsByDeptLeader(memberId, accountId);
	     return filterTemplate(findManageTemplats(temp, accountId, moduleTypes), accountId, moduleTypes);
	 }
	 
	 /**
	  * 过滤掉
	  * @Author      : xuqw
	  * @Date        : 2016年4月19日上午9:48:22
	  * @param tpls
	  * @return
	 * @throws BusinessException 
	  */
	 private List<CtpTemplate> filterTemplate(List<CtpTemplate> tpls, Long accountId, List<ModuleType> moduleTypes) throws BusinessException{
	     
	     List<CtpTemplate> ret = new ArrayList<CtpTemplate>();
	     
	     
	     if(Strings.isNotEmpty(tpls)){
	         
	         List<Long> tIds = new ArrayList<Long>();
	         Map<Long, CtpTemplate> id2Temps = new HashMap<Long, CtpTemplate>();
	         
	         for(CtpTemplate t : tpls){
	             if(t.getState() != null 
	                     && t.getState() == TemplateEnum.State.normal.ordinal()
	                     && !TemplateEnum.Type.text.name().equals(t.getType())){
	                 //再校验是否进行了授权， 只有做了授权的模板才取出
	                 tIds.add(t.getId());
	                 id2Temps.put(t.getId(), t);
	             }
	         }
	         
	         //授权过滤
	         if(Strings.isNotEmpty(tIds)){
	             
	           //单位下模板授权情况
	             List<CtpTemplateAuth> auths = templateDao.getTemplateAuths(tIds,null, null);
	             List<Long> hasAdd = new ArrayList<Long>(tIds.size());
	             if(Strings.isNotEmpty(auths)){
	                 for(CtpTemplateAuth c : auths){
	                     Long authId = c.getModuleId();
	                     if(hasAdd.contains(authId)){
	                         continue;
	                     }
	                     
	                     hasAdd.add(authId);
	                     ret.add(id2Temps.get(authId));
	                 }
	             }
	         }
	         
	     }
	     return ret;
	 }
	 
	 /**
	  * 获取部门下，人员的管理模板的并集
	  * @Author      : xuqw
	  * @Date        : 2016年2月1日上午1:14:47
	  * @param depts
	  * @param accountId
	  * @return
	  * @throws BusinessException
	  */
	 private List<CtpTemplate> findManageTemplats(List<V3xOrgDepartment> depts, long accountId, List<ModuleType> moduleTypes) throws BusinessException{
	     
	     if(moduleTypes.isEmpty()){
	         moduleTypes = new ArrayList<ModuleType>();
	         
	         moduleTypes.add(ModuleType.edoc);
	         moduleTypes.add(ModuleType.edocSend);
	         moduleTypes.add(ModuleType.edocRec);
	         moduleTypes.add(ModuleType.edocSign);
	         
	         moduleTypes.add(ModuleType.govdoc);
	         moduleTypes.add(ModuleType.govdocSend);
	         moduleTypes.add(ModuleType.govdocRec);
	         moduleTypes.add(ModuleType.govdocSign);
	         
	         moduleTypes.add(ModuleType.collaboration);
	         moduleTypes.add(ModuleType.form);
	         moduleTypes.add(ModuleType.cap4Form);
	     }
         
	     List<CtpTemplate> templateList = new ArrayList<CtpTemplate>();
	     
	     Long loginAccountId = accountId;
	     List<Long> templateIds = new ArrayList<Long>();
         if(Strings.isNotEmpty(depts)){
             
             //模板分类授权情况
            Set<Long> authIds = new HashSet<Long>();
            if (moduleTypes.contains(ModuleType.collaboration)) {
                List<ModuleType> colTypes = new ArrayList<ModuleType>(1);
                colTypes.add(ModuleType.collaboration);
                List<CtpTemplateCategory> templateCategorys = getCategorys(loginAccountId, colTypes);
                if (Strings.isNotEmpty(templateCategorys)) {

                    List<Long> mIds = new ArrayList<Long>();
                    for (CtpTemplateCategory c : templateCategorys) {
                        mIds.add(c.getId());
                    }

                    List<CtpTemplateAuth> auths = templateDao.getTemplateAuths(mIds, null, loginAccountId);
                    if (Strings.isNotEmpty(auths)) {
                        for (CtpTemplateAuth a : auths) {
                            authIds.add(a.getAuthId());
                        }
                    }
                }
            }
            
            //获取表单管理员
            List<Long> formManagerIds = new ArrayList<Long>();
            if(moduleTypes.contains(ModuleType.form)){
                List<V3xOrgMember> formMembers = orgManager.getMembersByRole(accountId, OrgConstants.Role_NAME.FormAdmin.name());
                if(Strings.isNotEmpty(formMembers)){
                    for(V3xOrgMember m : formMembers){
                        formManagerIds.add(m.getId());
                    }
                }
            }
            //cap4模板
            if(moduleTypes.contains(ModuleType.cap4Form)){
            	 List<V3xOrgMember> formMembers = orgManager.getMembersByRole(accountId, OrgConstants.Role_NAME.BusinessDesigner.name());
                 if(Strings.isNotEmpty(formMembers)){
                     for(V3xOrgMember m : formMembers){
                    	 formManagerIds.add(m.getId());
                     }
                 }
            }
            
            //公文管理员
            List<Long> edocManagerIds = new ArrayList<Long>();
            if(moduleTypes.contains(ModuleType.edoc)){
                List<V3xOrgMember> edocMembers = orgManager.getMembersByRole(accountId, OrgConstants.Role_NAME.EdocManagement.name());
                if(Strings.isNotEmpty(edocMembers)){
                    for(V3xOrgMember m : edocMembers){
                        edocManagerIds.add(m.getId());
                    }
                }
            }
             
            if(Strings.isNotEmpty(authIds) 
                    || Strings.isNotEmpty(formManagerIds) 
                    || Strings.isEmpty(edocManagerIds)){
                
                //公文类型
                List<ModuleType> edocTypes = new ArrayList<ModuleType>(1);
                edocTypes.add(ModuleType.edoc);
                if(moduleTypes.contains(ModuleType.edocSend)){
                    //外部人员无权查看
                    edocTypes.add(ModuleType.edocSend);
                    edocTypes.add(ModuleType.edocRec);
                    edocTypes.add(ModuleType.edocSign);
                    
                    
                }
                
                // 政务公文
                edocTypes.add(ModuleType.govdoc);
                if(moduleTypes.contains(ModuleType.govdocSend)){
                	edocTypes.add(ModuleType.govdocSend);
                	edocTypes.add(ModuleType.govdocRec);
                	edocTypes.add(ModuleType.govdocSign);
                }
                
                //表单类型
                List<ModuleType> formTypes = new ArrayList<ModuleType>(2);
                formTypes.add(ModuleType.form);
                formTypes.add(ModuleType.cap4Form);
                
                for(V3xOrgDepartment depart : depts){
                    
                    List<V3xOrgMember> colMembers = orgManager.getMembersByDepartment(depart.getId(), false);
                    if (Strings.isNotEmpty(colMembers)) {
                        for (V3xOrgMember m : colMembers) {
                            
                            //初步过滤
                            boolean fitter = authIds.contains(m.getId()) 
                                    || formManagerIds.contains(m.getId())
                                    || edocManagerIds.contains(m.getId());
                            
                            // 过滤兼职
                            if (fitter
                                    && (!loginAccountId.equals(m.getOrgAccountId()) 
                                            || m.getOrgDepartmentId().equals(depart.getId())
                                            || orgManager.isInDepartmentPathOf(depart.getId(), m.getOrgDepartmentId()))) {
                                
                                //协同管理员
                                if (moduleTypes.contains(ModuleType.collaboration) && authIds.contains(m.getId())) {
                                    List<CtpTemplate> ts = findColManagerTemplates(loginAccountId, m.getId(),null);
                                    if (Strings.isNotEmpty(ts)) {
                                        for(CtpTemplate t : ts){
                                            if(!templateIds.contains(t.getId())){
                                                templateIds.add(t.getId());
                                                templateList.add(t);
                                            }
                                        }
                                    }
                                }
                                
                                if((moduleTypes.contains(ModuleType.form) || moduleTypes.contains(ModuleType.cap4Form))
                                	&& formManagerIds.contains(m.getId())){
                                    //表单管理员
                                    List<CtpTemplate> ts = findFormManagerTemplates(m.getId(), loginAccountId, formTypes, false);
                                    if(Strings.isNotEmpty(ts)){
                                        for(CtpTemplate t : ts){
                                            if(!templateIds.contains(t.getId())){
                                                templateIds.add(t.getId());
                                                templateList.add(t);
                                            }
                                        }
                                    }
                                }
                                

                                if((moduleTypes.contains(ModuleType.edoc)
                                		|| moduleTypes.contains(ModuleType.govdoc)) && edocManagerIds.contains(m.getId())){
                                  //公文管理员
                                    
                                    List<CtpTemplate> ts = findEdocManagerTemplates(m.getId(), loginAccountId, edocTypes, false);
                                    if(Strings.isNotEmpty(ts)){
                                        for(CtpTemplate t : ts){
                                            if(!templateIds.contains(t.getId())){
                                                templateIds.add(t.getId());
                                                templateList.add(t);
                                            }
                                        }
                                    }
                                }
                                
                            }
                        }
                    }
                     
                 }
            }
         }
         return templateList;
	 }
	 
	/**
	  * 查找公文管理员管理的模板
	  * @Author      : xuqw
	  * @Date        : 2016年2月1日上午1:01:08
	  * @param memberId
	  * @param accountId
	  * @param moduletypes
	  * @param checkRole 是否需要检测是否具有公文管理员角色
	  * @return
	  * @throws BusinessException
	  */
	 private List<CtpTemplate> findEdocManagerTemplates(long memberId, long accountId, List<ModuleType> moduletypes, boolean checkRole) throws BusinessException{
	     boolean isEdocManager = !checkRole || (checkRole && orgManager.isRole(memberId, accountId, 
                 OrgConstants.Role_NAME.EdocManagement.name()));
	     List<CtpTemplate> templates = null;
	     if(isEdocManager){
             templates = getSystemTempletes(accountId, moduletypes, null, null);
         }
	     return templates;
	 }
	 
	 /**
	  * 查找表单管理员管理的模板
	  * @Author      : xuqw
	  * @Date        : 2016年2月1日上午12:53:41
	  * @param memberId
	  * @param accountId
	  * @param checkRole 是否需要检测是否具有表单管理员角色
	  * @return
	  * @throws BusinessException 
	  */
	 private List<CtpTemplate> findFormManagerTemplates(long memberId, long accountId, List<ModuleType> moduleTypes, boolean checkRole) throws BusinessException{
         boolean isFormAdmin = !checkRole || (checkRole && orgManager.isRole(memberId, accountId, 
                 OrgConstants.Role_NAME.FormAdmin.name()));
         List<CtpTemplate> templates = null;
         if(isFormAdmin){
             templates = getSystemTempletes(accountId, moduleTypes, "memberId", String.valueOf(memberId));
         }
         return templates;
	 }
	 
	 /**
	  * 获取协同管理员管理模板
	  * @Author      : xuqw
	  * @Date        : 2016年2月1日上午12:31:44
	  * @param accountId
	  * @param memberId
	  * @return
	  * @throws BusinessException
	  */
	 private List<CtpTemplate> findColManagerTemplates(long accountId, long memberId,Map<String,String> params) throws BusinessException{
	     
	     List<CtpTemplate> templates = null;
	     
	     Map<String, String> queryParams = new HashMap<String, String>();
         queryParams.put("delete", "false");
         String bodyType = "10,30,41,42,43,44,45";
         queryParams.put("bodyType", bodyType);
         List<ModuleType> moduleList = new ArrayList<ModuleType>();
         moduleList.add(ModuleType.collaboration);
         List<CtpTemplateCategory> categorys = this.getCategorysByAuth(accountId,moduleList, memberId);
         List<Long> cid = new ArrayList<Long>();
         for(CtpTemplateCategory c : categorys){
             cid.add(c.getId());
         }
         if(Strings.isNotEmpty(cid)){
             queryParams.put("categoryId", Strings.join(cid, ","));
         }
         queryParams.put("categoryType", String.valueOf(ApplicationCategoryEnum.collaboration.getKey()));
        if (params != null) {
             queryParams.putAll(params);
         }
         templates = templateDao.selectAllSystemTempletes(null, queryParams, accountId);
         
         return templates;
	 }
	 
	
    public List<CtpTemplateCategory> getCategorysByAuth(Long accountId, List<ModuleType> types, Long memberId) throws BusinessException{
    	if(accountId == null || types == null){
    		return new ArrayList<CtpTemplateCategory>();
    	}
    	List<ModuleType> _type = new ArrayList<ModuleType>();
    	for(ModuleType t : types){
    		_type.add(t);
    	}
    	
    	List<CtpTemplateCategory> templateCategorys = this.getCategorys(accountId,_type);
       
    	return checkCategoryAuth(accountId, templateCategorys, memberId);
    }
	    
    /**
     * @param accountId
     * @param templateCategorys
     * @return 检查模板类型的是否授权
     * @throws BusinessException
     */
    private List<CtpTemplateCategory> checkCategoryAuth(Long accountId, List<CtpTemplateCategory> templateCategorys, Long memberId)
            throws BusinessException {
        List<CtpTemplateCategory> result = new ArrayList<CtpTemplateCategory>();
        if (templateCategorys != null) {
            CtpTemplateCategory temp = null;
            for (CtpTemplateCategory ctpTemplateCategory : templateCategorys) {
                if (ctpTemplateCategory.isDelete() == null || !ctpTemplateCategory.isDelete()) {
                    // 单位管理员可访问所有
                    if (orgManager.isAdministratorById(memberId, accountId)
                            || this.isTemplateCategoryManager(memberId, accountId,
                                    findRootParent(ctpTemplateCategory))){
                        try {
                            // 返回clone对象
                            temp = (CtpTemplateCategory) ctpTemplateCategory.clone();
                            temp.setId(ctpTemplateCategory.getId());
                            result.add(temp);
                        } catch (CloneNotSupportedException e) {
                            LOG.error("", e);
                        }
                    }

                }
            }
        }
        return result;
    }
    
    
	public Map<Long, SimpleTemplate> getSystemTempleteSimpleInfo() {
	    return templateDao.getSystemTempleteSimpleInfo();
	}
	public List<CtpTemplate> getAllSystemTemplatesByIds(List<Long> templeteIds,Integer categoryType){
	    return templateDao.getAllSystemTemplatesByIds(templeteIds, categoryType);
	}
    public CtpTemplateCategory getCategorybyId(Long id){
        CtpTemplateCategory c =  null;
        try {
           c =  getCtpTemplateCategory(id);
        } catch (BusinessException e) {
          LOG.error("", e);
        }
        return c;
    }

    @ProcessInDataSource(name = DataSourceName.BASE)
	public void saveDefaultCategoryOnAddAccount(long accountId){
        List<CtpTemplateCategory> list = new ArrayList<CtpTemplateCategory>();
        for(int i=0;i<3; i++){
            CtpTemplateCategory templeteCategory = new CtpTemplateCategory();
            if(i==0){
                templeteCategory.setIdIfNew();
                templeteCategory.setName("template.create.finance.label");
            }else if(i==1){
                templeteCategory.setIdIfNew();
                templeteCategory.setName("template.create.administration.label");
            }else if(i==2){
                templeteCategory.setIdIfNew();
                templeteCategory.setName("template.create.manpower.label");
            }       
            templeteCategory.setType(ModuleType.form.ordinal());//分类改存表单
            templeteCategory.setParentId(Long.valueOf(ModuleType.collaboration.ordinal()));       
            templeteCategory.setSort(0);
            templeteCategory.setDelete(false);
            templeteCategory.setOrgAccountId(accountId);
            templeteCategory.setCreateDate(new Date(System.currentTimeMillis()));
            list.add(templeteCategory);
        }
        for(CtpTemplateCategory c : list){
        	try {
				saveCtpTemplateCategory(c);
			} catch (BusinessException e) {
			    LOG.error("", e);
			}
        }
    }
	@Override
	public List<TemplateTreeVo> getTemplateChooseTreeData(@SuppressWarnings("rawtypes") Map params) throws BusinessException{
		User user = AppContext.getCurrentUser();
		Long memberId = user.getId();
		Long currentAccountId = AppContext.currentAccountId();
		boolean IsInternal = user.isInternal();
		String _pMemberId = (String)params.get("memberId");
		if(Strings.isNotBlank(_pMemberId)){
			memberId = Long.valueOf(_pMemberId);
			V3xOrgMember member = orgManager.getMemberById(memberId);
			IsInternal = member.getIsInternal();
		}
		
		List<ModuleType> types = new ArrayList<ModuleType>();
        String _moduleType= (String)params.get("moduleType");
        
        String scope=params.get("scope").toString();
        if(TemplateChooseScope.ManageDep.name().equalsIgnoreCase(scope)
                || TemplateChooseScope.LeaderDep.name().equalsIgnoreCase(scope)){
            //主管各部门, 分管各部门, 查询协同，表单，公文模板, 按前台传递查询
        }else if(TemplateChooseScope.EdocManagement.name().equalsIgnoreCase(scope)){
            _moduleType = String.valueOf(ModuleType.edoc.getKey());
        }else if(TemplateChooseScope.FormAdmin.name().equalsIgnoreCase(scope)){
            _moduleType = String.valueOf(ModuleType.form.getKey());
            
        }else if(TemplateChooseScope.ColTempManagement.name().equalsIgnoreCase(scope)){
            //协同管理员
            _moduleType = String.valueOf(ModuleType.collaboration.getKey());
        }
        
        
        if(Strings.isBlank(_moduleType)){
            types.add(ModuleType.collaboration);
            types.add(ModuleType.form);
            types.add(ModuleType.edoc);
        }else{
            String[] arr = _moduleType.split(",");
            for(String s : arr){
                
                try {
                    ModuleType m = ModuleType.getEnumByKey(Integer.parseInt(s));
                    if(m != null){
                        types.add(m);
                    }
                } catch (IllegalArgumentException e) {
                    LOG.error("获取枚举报错了", e);
                }
            }
        }
        
        //公文拆分分类
        if(types.contains(ModuleType.edoc) && AppContext.hasPlugin("edoc")){
            if(IsInternal){
                //外部人员过滤公文。
                types.add(ModuleType.edocSend);
                types.add(ModuleType.edocRec);
                types.add(ModuleType.edocSign);
                types.add(ModuleType.govdocSend);
                types.add(ModuleType.govdocRec);
                types.add(ModuleType.govdocSign);
            }
        }
        String condition = "";
        String textfield = "";
        try {
            textfield = java.net.URLDecoder.decode(
                Strings.isNotBlank((String) params.get("textfield")) ? (String) params.get("textfield") : "", "UTF-8");
        } catch (UnsupportedEncodingException e) {
            LOG.error("", e);
        }
        if("1".equals(String.valueOf(params.get("searchType")))){
            condition = TempleteCategorysWebModel.SEARCH_BY_SUBJECT;
        }else if("2".equals(String.valueOf(params.get("searchType")))){
            condition = TempleteCategorysWebModel.SEARCH_BY_CATEGORY;
        }
        
        if(params.get("templateTypes") != null){
        	condition = searchCondtion.types.name();
        	textfield = (String)params.get("templateTypes");
        }
        
        boolean isV5Member = user.isV5Member();
        if (!isV5Member) {
            currentAccountId = OrgHelper.getVJoinAllowAccount();
            types.remove(ModuleType.edoc);
        }
        
        //具体查询模板的方法
		List<CtpTemplate> templates = getSystemTemplets(params,types, memberId,currentAccountId,condition,textfield);
		
		List<CtpTemplateCategory> templeteCategorys = new ArrayList<CtpTemplateCategory>();
		if(TemplateChooseScope.ProcessAssets.name().equalsIgnoreCase(scope)){
			Map<String, String> categoryParams = new HashMap<String, String>();
			categoryParams.put("inculdeEdoc", "true");
			templeteCategorys = this.getCtpTemplateListByCategory(categoryParams);
		}else{
			templeteCategorys = getCategorys(currentAccountId,types);
		}
        
        //处理下分类的名字转换下 有可能为特殊字符
        Map<String, CtpTemplateCategory> nameCategoryMul = new HashMap<String, CtpTemplateCategory>();
        Iterator<CtpTemplateCategory> itCategory = templeteCategorys.iterator();
        while(itCategory.hasNext()){
        	CtpTemplateCategory category = itCategory.next();
        	boolean isSameParent = false;
        	if(nameCategoryMul.get(category.getName()) != null) {
        		if (nameCategoryMul.get(category.getName()).getParentId() != null && nameCategoryMul.get(category.getName()).getParentId().equals(category.getParentId())){
        			isSameParent = true;
        		}
        	}
        	
        	if(nameCategoryMul.get(category.getName()) != null && isSameParent){
        		itCategory.remove();
        	}else{
        		nameCategoryMul.put(category.getName(), category);
        	}
        }
        List<CtpTemplateCategory> outerUserForShowCategories =  new ArrayList<CtpTemplateCategory>();
       
        //合并模板分类，外单位授权给本单位的需要合并
		mergeCategory(user, templates, templeteCategorys, nameCategoryMul, outerUserForShowCategories);
        
		String showOldEdocTemplate = (String)params.get("showOldEdocTemplate");
		
        if(Strings.isNotBlank(condition) && Strings.isNotBlank(textfield) 
        	&& (condition.equals(TempleteCategorysWebModel.SEARCH_BY_SUBJECT) || condition.equals(TempleteCategorysWebModel.SEARCH_BY_CATEGORY))) {
    	    if(Strings.isNotEmpty(templates)){
    	    	List<Long> categoryIds = new ArrayList<Long>();
    	        if(TempleteCategorysWebModel.SEARCH_BY_CATEGORY.equalsIgnoreCase(condition)){
    	        	for(String categoryId : textfield.split(",")){
    	        		categoryIds.add(Long.valueOf(categoryId));
    	        		if ("true".equals(showOldEdocTemplate)) {
    	        			if (String.valueOf(ModuleType.govdocSend.getKey()).equals(categoryId)) {//新公文分类,追加老公文
    	        				categoryIds.add(Long.valueOf(ModuleType.edocSend.getKey()));
    	        			} else if (String.valueOf(ModuleType.govdocRec.getKey()).equals(categoryId)) {
    	        				categoryIds.add(Long.valueOf(ModuleType.edocRec.getKey()));
    	        			} else if (String.valueOf(ModuleType.govdocSign.getKey()).equals(categoryId)) {
    	        				categoryIds.add(Long.valueOf(ModuleType.edocSign.getKey()));
    	        			}
    	        		}
    	        			
	            	}
    	        }
    	    	for(Iterator<CtpTemplate> it = templates.iterator();it.hasNext();){
    	            CtpTemplate t = it.next();
    	            if(TempleteCategorysWebModel.SEARCH_BY_SUBJECT.equalsIgnoreCase(condition)) {
    	                if(Strings.isNotBlank(t.getSubject())
    	                        && t.getSubject().indexOf(textfield)!=-1){
    	                    continue;
    	                } 
    	            } else if(TempleteCategorysWebModel.SEARCH_BY_CATEGORY.equalsIgnoreCase(condition)) {
    	            	
    	                if(categoryIds.contains(t.getCategoryId())){
    	                    continue;
    	                } 
    	            }
    	            it.remove();
    	        }
    	    }
    	}
        boolean hasEdoc = AppContext.hasPlugin("govdoc") || AppContext.hasPlugin("edoc");
        
	    Map<String, List<CtpTemplate>> maps = convertToMap(params, hasEdoc, templates);
        
        
        if(!outerUserForShowCategories.isEmpty() && outerUserForShowCategories .size() > 0){
        	AppContext.putRequestContext("outerUserForShowCategories",outerUserForShowCategories);
        }
        boolean isQuery = params.get("searchType")!= null && ("1".equals(String.valueOf(params.get("searchType"))) || "2".equals(String.valueOf(params.get("searchType"))));
        boolean isAlwaysShowTemplateCommon = params.get("isAlwaysShowTemplateCommon")!= null ? Boolean.valueOf(String.valueOf(params.get("isAlwaysShowTemplateCommon"))) : false;
        
        String showNoChildrenCategory = (String)params.get("showNoChildrenCategory");
      
        List<TemplateTreeVo> listTreeVo = getTemplateSelectTree(templeteCategorys, maps , getModuleTypeKeyList(types), isQuery, scope, showNoChildrenCategory,isAlwaysShowTemplateCommon, showOldEdocTemplate);
      
        listTreeVo=removeRedundant(listTreeVo);
    	
        return listTreeVo;
	}

	private void mergeCategory(User user, List<CtpTemplate> templates, List<CtpTemplateCategory> templeteCategorys,
			Map<String, CtpTemplateCategory> nameCategoryMul, List<CtpTemplateCategory> outerUserForShowCategories) {
		if (Strings.isNotEmpty(templates)) {
			for (CtpTemplate template : templates) {
				try {
					boolean isColOrFormCategory = Integer.valueOf(ModuleType.form.getKey())
							.equals(template.getModuleType())
							|| Integer.valueOf(ModuleType.collaboration.getKey()).equals(template.getModuleType());
					boolean isNotCurrentAccount = template.getOrgAccountId().longValue() != user.getLoginAccount()
							.longValue();
					if (isNotCurrentAccount && isColOrFormCategory) {
						CtpTemplateCategory category = templateCategoryManager.get(template.getCategoryId());
						if (category != null) {
							CtpTemplateCategory exsit = nameCategoryMul.get(category.getName());
							if (null != exsit) {// 已经存在同名的分类 不重复添加分类, 把模板对应的目录
												// 设置为 已经存在的分类
								if (!exsit.getName().contains(String.valueOf(exsit.getId()))) {
									exsit.setName(exsit.getName() + "|C_" + exsit.getId());
								}
								if (!exsit.getName().contains(String.valueOf(category.getId()))) {
									exsit.setName(exsit.getName() + "|C_" + category.getId());
								}
								template.setCategoryId(exsit.getId());
							} else {
								category.setParentId(TemplateCategoryConstant.publicRoot.key());
								templeteCategorys.add(category);
								nameCategoryMul.put(category.getName(), category);
								outerUserForShowCategories.add(category);
							}
						}
					}
				} catch (Exception e) {
					LOG.error("", e);
				}
			}
		}
	}

	private Map<String, List<CtpTemplate>> convertToMap(Map params, boolean hasEdoc, List<CtpTemplate> templates) throws BusinessException {
		List<CtpTemplate> colTemp = new ArrayList<CtpTemplate>();
    	List<CtpTemplate> faTemp = new ArrayList<CtpTemplate>();
    	List<CtpTemplate> shouTemp = new ArrayList<CtpTemplate>();
    	List<CtpTemplate> qianTemp = new ArrayList<CtpTemplate>();
    	List<CtpTemplate> recentDealtemplates = new ArrayList<CtpTemplate>();
    	List<CtpTemplate> faNewTemp = new ArrayList<CtpTemplate>();
    	List<CtpTemplate> shouNewTemp = new ArrayList<CtpTemplate>();
    	List<CtpTemplate> qianNewTemp = new ArrayList<CtpTemplate>();
    	
    	User user = AppContext.getCurrentUser();
		Long memberId = user.getId();
		boolean isShowTemplateRecentDeal = Boolean.valueOf((String)params.get("isShowTemplateRecentDeal"));//是否显示最近处理模板
		if (isShowTemplateRecentDeal) {
			String condition = "";
			String textfield = "";
            if ("1".equals(String.valueOf(params.get("searchType")))) {//所属应用不查询最近处理模板
                condition = TempleteCategorysWebModel.SEARCH_BY_SUBJECT;
                try {
                    textfield = java.net.URLDecoder.decode(Strings.isNotBlank((String) params.get("textfield")) ? (String) params.get("textfield") : "", "UTF-8");
                } catch (UnsupportedEncodingException e) {
                    LOG.error("", e);
                }
                
                Map<String, String> queryParams = new HashMap<String, String>();
                queryParams.put(condition, textfield);
                recentDealtemplates = templateDao.getRecentTemplates(memberId, 10, TemplateEnum.RecentUseType.deal, queryParams);
            }
		}
		
    	if(null != templates){
    		String excludeTemplateIds = String.valueOf(params.get("excludeTemplateIds"));
  	        List<Long> excludeTemplateIdl = CommonTools.parseStr2Ids(excludeTemplateIds, ",");
  	        if(excludeTemplateIdl==null){
  	            excludeTemplateIdl = Collections.emptyList();
  	        }
			for (CtpTemplate ctpTemplate : templates) {
				if (excludeTemplateIdl.contains(ctpTemplate.getId()))
					continue;
				CtpTemplate template = ctpTemplate.clone();
				ModuleType m = ModuleType.getEnumByKey(template.getModuleType());
				if (!hasEdoc && (m == ModuleType.edoc || m == ModuleType.edocSend || m == ModuleType.edocRec
						|| m == ModuleType.edocSign)) {
					continue;
				}
				//添加新公文模版
				if(!hasEdoc && (m == ModuleType.edoc ||
						m == ModuleType.govdocSend ||
						m == ModuleType.govdocRec ||
						m == ModuleType.govdocSign)){
					continue;
				}
				switch (m) {
				case collaboration:
				case form:
					colTemp.add(template);
					break;
				case edoc:
				case edocSend:
					faTemp.add(template);
					break;
				case edocRec:
					shouTemp.add(template);
					break;
				case edocSign:
					qianTemp.add(template);
					break;
					//添加新公文模版类型
				case govdocSend:
					faNewTemp.add(template);
					break;
				case govdocRec:
					shouNewTemp.add(template);
					break;
				case govdocSign:
					qianNewTemp.add(template);
					break;
				}
			}
    	}
    	Map<String, List<CtpTemplate>> maps=new HashMap<String, List<CtpTemplate>>();
    	maps.put("colTemp", colTemp);
    	maps.put("faTemp", faTemp);
    	maps.put("shouTemp", shouTemp);
    	maps.put("qianTemp", qianTemp);
    	maps.put("recentDeal", recentDealtemplates);
    	maps.put("faNewTemp", faNewTemp);
    	maps.put("shouNewTemp", shouNewTemp);
    	maps.put("qianNewTemp", qianNewTemp);
		return maps;
	}
	
	private List<Integer> getModuleTypeKeyList(List<ModuleType> moduleTypes){
		
		List<Integer> moduleTypeKeys = new ArrayList<Integer>();
		
		if(Strings.isNotEmpty(moduleTypes)){
			for(ModuleType m : moduleTypes){
				moduleTypeKeys.add(m.getKey());
			}
		}
		return moduleTypeKeys;
	}
	
	private List<TemplateTreeVo> removeRedundant(List<TemplateTreeVo> listTreeVo){
		List<TemplateTreeVo> temp=new ArrayList<TemplateTreeVo>();
		for(TemplateTreeVo templateTreeVo:listTreeVo){
			if(!temp.contains(templateTreeVo)){
				temp.add(templateTreeVo);
			}
		}
		return temp;
	}
	/**
	 * 初始化情况下： 
	 * 1、公文的分类只要有公文的插件都固定显示 
	 * 2、协同的分类如果没有模板就不显示该分类 
	 * 3、查询状态下没有的久不显示，公文协同相同
	 * 4、根据传参添加是否显示最近处理
	 * @throws BusinessException 
    */
	private List<TemplateTreeVo> getTemplateSelectTree(
		List<CtpTemplateCategory> templeteCategorys,
		Map<String, List<CtpTemplate>> maps,
		List<Integer> types ,
		boolean isQuery,
		String scope,
		String showNoChildrenCategory,boolean isAlwaysShowTemplateCommon,String showOldEdocTemplate) throws BusinessException {
		
		
		List<CtpTemplate> showTempletes=new ArrayList<CtpTemplate>();
        for(String key:maps.keySet()){
        	if (!"recentDeal".equals(key)) {
        		for(CtpTemplate ctpTemplate:maps.get(key)){
        			showTempletes.add(ctpTemplate);
        		}
        	}
        }
        
		List<TemplateTreeVo> listTreeVo = new ArrayList<TemplateTreeVo>();
		TemplateTreeVo templateTreeVO = null;
		//模板树需要的分类
		Set<Long> templateNeedCategorys = new HashSet<Long>();
		List<Long> cids = new UniqueList<Long>();
		Long la = AppContext.getCurrentUser().getLoginAccount();
		//是否显示最近处理模板
		List<CtpTemplate> recentDealtemplates = maps.get("recentDeal");
	    if (Strings.isNotEmpty(recentDealtemplates)) {//存在最近处理模板
	    	//添加最近处理模板根目录
	    	templateTreeVO = new TemplateTreeVo();
			templateTreeVO.setId(TemplateCategoryConstant.recentDeal.key());
			templateTreeVO.setCombinId(String.valueOf(TemplateCategoryConstant.recentDeal.key()));
			templateTreeVO.setName(ResourceUtil.getString("template.choose.category.recent.deal.label"));
			templateTreeVO.setType("category");
			templateTreeVO.setpId(null);
			listTreeVo.add(templateTreeVO);
			templateNeedCategorys.add(TemplateCategoryConstant.recentDeal.key());
	    	for (CtpTemplate ctpTemplate : recentDealtemplates) {
	    		templateTreeVO = new TemplateTreeVo();
				templateTreeVO.setId(ctpTemplate.getId());
				
				if(null != ctpTemplate.getOrgAccountId() && !la.equals(ctpTemplate.getOrgAccountId()) && ctpTemplate.isSystem()){
	    			String shortName = orgManager.getAccountById(ctpTemplate.getOrgAccountId()).getShortName();
	    			templateTreeVO.setName(ctpTemplate.getSubject() +"("+shortName+")");
	    		}else{
	    			templateTreeVO.setName(ctpTemplate.getSubject());
	    		}	
				templateTreeVO.setType(ctpTemplate.getType());
				templateTreeVO.setpId(TemplateCategoryConstant.recentDeal.key());//pId：加到最近处理模板中
				templateTreeVO.setCategoryType(ctpTemplate.getModuleType());
				templateTreeVO.setCombinId(String.valueOf(ctpTemplate.getId()));
				//将模板加到树上
				listTreeVo.add(templateTreeVO);
	    	}
	    }
	    if(!AppContext.hasPlugin("govdoc") && AppContext.hasPlugin("edoc")){
    		if(types.contains(ModuleType.edoc.getKey())){
    			templateTreeVO = new TemplateTreeVo();
    			templateTreeVO.setId(TemplateCategoryConstant.edocRoot.key());
    			templateTreeVO.setCombinId(String.valueOf(TemplateCategoryConstant.edocRoot.key()));
    			templateTreeVO.setName(ResourceUtil.getString("template.edoc.label"));
    			templateTreeVO.setType("category");
    			templateTreeVO.setpId(null);
    			listTreeVo.add(templateTreeVO);
    			
    		}
    		if (types.contains(ModuleType.edocSend.getKey())) {
    			listTreeVo.add(buildEdocCategory(String.valueOf(ModuleType.edocSend.getKey())));
    			cids.add(19L);
    		}
    		if (types.contains(ModuleType.edocRec.getKey())) {
    			listTreeVo.add(buildEdocCategory(String.valueOf(ModuleType.edocRec.getKey())));
    			 cids.add(20L);
    		}
    		if (types.contains(ModuleType.edocSign.getKey())) {
    			listTreeVo.add(buildEdocCategory(String.valueOf(ModuleType.edocSign.getKey())));
    			 cids.add(21l);
    		}
		}
		// 新公文模板代码
		if(AppContext.hasPlugin("govdoc")){
    		if(types.contains(ModuleType.edoc.getKey())){
    			templateTreeVO = new TemplateTreeVo();
    			templateTreeVO.setId(TemplateCategoryConstant.edocRoot.key());
    			templateTreeVO.setCombinId(String.valueOf(TemplateCategoryConstant.edocRoot.key()));
    			templateTreeVO.setName(ResourceUtil.getString("template.edoc.label"));
    			templateTreeVO.setType("category");
    			templateTreeVO.setpId(null);
    			listTreeVo.add(templateTreeVO);
    		}
    		if (types.contains(ModuleType.govdocSend.getKey())) {
    			//新发文
    			listTreeVo.add(buildEdocCategory(String.valueOf(ModuleType.govdocSend.getKey())));
    			cids.add(Long.parseLong(String.valueOf(ModuleType.govdocSend.getKey())));
    		}
    		if (types.contains(ModuleType.govdocRec.getKey())) {
    			//新收文
    			listTreeVo.add(buildEdocCategory(String.valueOf(ModuleType.govdocRec.getKey())));
    			cids.add(Long.parseLong(String.valueOf(ModuleType.govdocRec.getKey())));
    		}
    		if (types.contains(ModuleType.govdocSign.getKey())) {
    			//新签报
    			listTreeVo.add(buildEdocCategory(String.valueOf(ModuleType.govdocSign.getKey())));
    			cids.add(Long.parseLong(String.valueOf(ModuleType.govdocSign.getKey())));
    		}	
    		
	    		
		}
		if(types.contains(ModuleType.collaboration.getKey())){
			templateTreeVO = new TemplateTreeVo();
			templateTreeVO.setId(TemplateCategoryConstant.publicRoot.key());
			templateTreeVO.setCombinId(String.valueOf(TemplateCategoryConstant.publicRoot.key()));
			templateTreeVO.setName(ResourceUtil.getString("template.public.label"));
			templateTreeVO.setType("category");
			templateTreeVO.setpId(null);
			listTreeVo.add(templateTreeVO);
		}
		if (AppContext.hasPlugin("govdoc") && "true".equals(showOldEdocTemplate)){
			//构建老公文分类树:公文模板(升级前)
			if(types.contains(ModuleType.edoc.getKey())){
    			templateTreeVO = new TemplateTreeVo();
    			templateTreeVO.setId(4L);//老公文不支持选择模板分类,并且新公文的根目录使用的是TemplateCategoryConstant.edocRoot.暂时任意构造一个id
    			templateTreeVO.setCombinId("4");
    			templateTreeVO.setName(ResourceUtil.getString("template.edoc.label") + ResourceUtil.getString("template.edoc.old.label"));
    			templateTreeVO.setType("category");
    			templateTreeVO.setpId(null);
    			listTreeVo.add(templateTreeVO);
    		}
    		if (types.contains(ModuleType.edocSend.getKey())) {
    			TemplateTreeVo ttpersonlVO = new TemplateTreeVo();
    			ttpersonlVO.setId(TemplateCategoryConstant.edocSendRoot.getKey());
    			ttpersonlVO.setName(ResourceUtil.getString("template.edocsend.label"));
    			ttpersonlVO.setType("category");
    			ttpersonlVO.setpId(4L);
    			ttpersonlVO.setCombinId(String.valueOf(TemplateCategoryConstant.edocSendRoot.getKey()));
    			listTreeVo.add(ttpersonlVO);
    			cids.add(19L);
    		}
    		if (types.contains(ModuleType.edocRec.getKey())) {
    			TemplateTreeVo ttpersonlVO = null;
    			ttpersonlVO = new TemplateTreeVo();
    			ttpersonlVO.setId(TemplateCategoryConstant.edocRecRoot.getKey());
    			ttpersonlVO.setName(ResourceUtil.getString("template.edocrec.label"));
    			ttpersonlVO.setType("category");
    			ttpersonlVO.setpId(4L);
    			ttpersonlVO.setCombinId(String.valueOf(TemplateCategoryConstant.edocRecRoot.getKey()));
    			listTreeVo.add(ttpersonlVO);
    			cids.add(20L);
    		}
    		if (types.contains(ModuleType.edocSign.getKey())) {
    			TemplateTreeVo ttpersonlVO = null;
    			ttpersonlVO = new TemplateTreeVo();
    			ttpersonlVO.setId(TemplateCategoryConstant.edocSignRoot.getKey());
    			ttpersonlVO.setName(ResourceUtil.getString("template.edocsign.label"));
    			ttpersonlVO.setType("category");
    			ttpersonlVO.setpId(4L);
    			ttpersonlVO.setCombinId(String.valueOf(TemplateCategoryConstant.edocSignRoot.getKey()));
    			listTreeVo.add(ttpersonlVO);
    			cids.add(21l);
    		}
		}
		// 分类
		for (CtpTemplateCategory ctpTemplateCategory : templeteCategorys) {
			templateTreeVO = new TemplateTreeVo();
			String categoryName = ctpTemplateCategory.getName();
			templateTreeVO.setId(ctpTemplateCategory.getId());
			templateTreeVO.setName(categoryName);
			templateTreeVO.setType("category");
			String combinIds = "";
			if(categoryName.contains("|")){
				String[] categoryIds = categoryName.split("\\|");
				for(int i=1;i<categoryIds.length;i++){
					if(Strings.isBlank(combinIds)){
						combinIds = categoryIds[i];
					}else{
						combinIds +=","+categoryIds[i];
					}
				}
				templateTreeVO.setName(categoryIds[0]);
			}else{
				combinIds = String.valueOf(ctpTemplateCategory.getId());
			}
			templateTreeVO.setCombinId(combinIds);
			Long parentId=ctpTemplateCategory.getParentId();
			if(null ==parentId && 
					(ctpTemplateCategory.getType()==ModuleType.govdocSend.getKey() || ctpTemplateCategory.getType()==ModuleType.govdocRec.getKey() || ctpTemplateCategory.getType()==ModuleType.govdocSign.getKey())){
				continue;
			}else if (null ==parentId 
			        ||Long.valueOf(ModuleType.collaboration.getKey()).equals(parentId)
			        ||Long.valueOf(ModuleType.form.getKey()).equals(parentId)) {
				templateTreeVO.setpId(TemplateCategoryConstant.publicRoot.key());
			} else {
				templateTreeVO.setpId(ctpTemplateCategory.getParentId());
			}
			cids.add(ctpTemplateCategory.getId());
			listTreeVo.add(templateTreeVO);
		}
		//模板树需要的分类
		boolean hasEdocTemplate = false;
		boolean hasNewEdocTemplate = false;
		boolean hasColTemplate = false;
		for (CtpTemplate ctpTemplate : showTempletes) {
		    //如果所属分类没有找到就不显示该模板，避免找到模板了没有找到分类，模板直接显示在根目录下面了。
		    if(!cids.contains(ctpTemplate.getCategoryId())) continue;
			templateTreeVO = new TemplateTreeVo();
			templateTreeVO.setId(ctpTemplate.getId());
			
			if(null != ctpTemplate.getOrgAccountId() && !la.equals(ctpTemplate.getOrgAccountId()) && ctpTemplate.isSystem()){
    			String shortName = orgManager.getAccountById(ctpTemplate.getOrgAccountId()).getShortName();
    			templateTreeVO.setName(ctpTemplate.getSubject() +"("+shortName+")");
    		}else{
    			templateTreeVO.setName(ctpTemplate.getSubject());
    		}	
			templateTreeVO.setType(ctpTemplate.getType());
			
			templateTreeVO.setpId(ctpTemplate.getCategoryId());//pId
			templateTreeVO.setCategoryType(ctpTemplate.getModuleType());
			boolean isEdoc = isEdoc(String.valueOf(ctpTemplate.getModuleType()));
			boolean isGovdoc = isGovdoc(String.valueOf(ctpTemplate.getModuleType()));
			templateTreeVO.setIsEdoc(isEdoc || isGovdoc);
			templateTreeVO.setCombinId(String.valueOf(ctpTemplate.getId()));
			if(!hasEdocTemplate) hasEdocTemplate = isEdoc;
			if(!hasNewEdocTemplate) hasNewEdocTemplate = isGovdoc;
			if(!hasColTemplate)  hasColTemplate = !isEdoc;
			//将模板加到树上
			listTreeVo.add(templateTreeVO);
			//将模板分类也加到树上 
			addCategoryInCludeParent2Needs(ctpTemplate.getCategoryId(),templeteCategorys,templateNeedCategorys);
			if (isEdoc) {
				//老公文不支持选择模板分类,并且新公文的根目录使用的是TemplateCategoryConstant.edocRoot.暂时任意构造一个id
				templateNeedCategorys.add(4L);
			}
		}
        
		//没有模板就不显示分类了
		if(Strings.isNotEmpty(listTreeVo)){
		    for(Iterator<TemplateTreeVo> it = listTreeVo.iterator();it.hasNext();){
		        TemplateTreeVo treeVO = it.next();
		        if("category".equals(treeVO.getType())){
		            
		            //初始化模板树的时候公文模板全部显示
		            if(!"true".equals(showOldEdocTemplate) && !isQuery && (Long.valueOf(ModuleType.edocSend.getKey()).equals(treeVO.getId()) 
		                    ||Long.valueOf(ModuleType.edocRec.getKey()).equals(treeVO.getId()) 
		                    ||Long.valueOf(ModuleType.edocSign.getKey()).equals(treeVO.getId())
		                    ||treeVO.getId().equals(TemplateCategoryConstant.edocRoot.key()))){
		                continue;
		            }
		            //新公文模板
		            if(!isQuery && (Long.valueOf(ModuleType.govdocSend.getKey()).equals(treeVO.getId()) 
		            		||Long.valueOf(ModuleType.govdocRec.getKey()).equals(treeVO.getId()) 
		            		||Long.valueOf(ModuleType.govdocSign.getKey()).equals(treeVO.getId()) 
		            		||treeVO.getId().equals(TemplateCategoryConstant.personRoot.key()))
		            		||treeVO.getId().equals(TemplateCategoryConstant.edocRoot.key())){
		            	continue;
		            }
		            //始终显示公共模板
		            if (isAlwaysShowTemplateCommon && Long.valueOf(TemplateCategoryConstant.publicRoot.key()).equals(treeVO.getId())) {
		            	continue;
		            }
		            
		            if(hasEdocTemplate && treeVO.getId().equals(TemplateCategoryConstant.edocRoot.key())){
		                continue;
		            }
		            if(hasNewEdocTemplate && treeVO.getId().equals(TemplateCategoryConstant.personRoot.key())){
		            	continue;
		            }
		            if(hasColTemplate && treeVO.getId().equals(TemplateCategoryConstant.publicRoot.key())){
                        continue;
                    }
                    
		            if(Strings.isBlank(showNoChildrenCategory) || !showNoChildrenCategory.equals("true")){
		            	//"4".equals(treeVO.getCombinId())虚构的老公文分类
                        if(!templateNeedCategorys.contains(treeVO.getId())){
                            it.remove();
                        }
                    }
		        }
		    }
		}
		
		return listTreeVo;
	}
	private void addCategoryInCludeParent2Needs(Long categoryId,List<CtpTemplateCategory> templeteCategorys,Set<Long> templateNeedCategorys){
	    templateNeedCategorys.add(categoryId);
	    for(CtpTemplateCategory c:templeteCategorys){
	        if(c.getId().equals(categoryId)){
	            Long pid = c.getParentId();
	            if(pid==null){
	                break;
	            }
	            else{
	                addCategoryInCludeParent2Needs(pid,templeteCategorys,templateNeedCategorys);
	            }
	        }
	    }
	}
	
	@Override
	@SuppressWarnings("unchecked")
	public Map isTemplateDelete(String tid)throws BusinessException{
		HashMap result = new HashMap();
		CtpTemplate templete = templateDao.getTemplete(Long.parseLong(tid));
		if(null == templete){
			result.put("isDel", "0");
		}else{
			if(templete.isDelete()){
				result.put("isDel", "1");
			}else{
				result.put("isDel", "0");
			}
		}
		return  result;
	}
	
	private boolean isCategoryExist(List<TemplateTreeVo> templateCategorysVO,Long id){
		for(TemplateTreeVo templateTreeVo:templateCategorysVO) {
			if(templateTreeVo.getId().equals(id))
				return true;
		}
		return false;
	}
	
	private TemplateTreeVo buildEdocCategory(String s) {
		TemplateTreeVo ttpersonlVO = null;
		ttpersonlVO = new TemplateTreeVo();
		// 表单和协同的构建根节点(pid为空的，则为顶层)
		ttpersonlVO.setId(Long.valueOf(s));
		if (s.equals(String.valueOf(ModuleType.edocSend.getKey())) || s.equals(String.valueOf(ModuleType.govdocSend.getKey()))){
			ttpersonlVO.setName(ResourceUtil.getString("template.edocsend.label"));
		}	
		if (s.equals(String.valueOf(ModuleType.edocRec.getKey())) || s.equals(String.valueOf(ModuleType.govdocRec.getKey()))){
			ttpersonlVO.setName(ResourceUtil.getString("template.edocrec.label"));
		}	
		if (s.equals(String.valueOf(ModuleType.edocSign.getKey())) || s.equals(String.valueOf(ModuleType.govdocSign.getKey()))){
			ttpersonlVO.setName(ResourceUtil.getString("template.edocsign.label"));
		}	
		ttpersonlVO.setType("category");
//		if (s.equals(String.valueOf(ModuleType.govdocSign.getKey()))||s.equals(String.valueOf(ModuleType.govdocSend.getKey())) || s.equals(String.valueOf(ModuleType.govdocRec.getKey()))) {
//			ttpersonlVO.setpId(TemplateCategoryConstant.personRoot.key());
//		}else {
			ttpersonlVO.setpId(TemplateCategoryConstant.edocRoot.key());
//		}
		ttpersonlVO.setCombinId(s);
		return ttpersonlVO;
	}
	private boolean isEdoc(String categoryKey){
        if(Strings.isNotBlank(categoryKey)){
        	//协同表单传入‘0,4’，转换出问题 直接判断。
        	String[] len = categoryKey.split(",");
        	if(len.length>1){
        		return false;
        	}
            Long ordinal = Long.valueOf(categoryKey);
            
            if(Long.valueOf(ModuleType.edoc.ordinal()) == ordinal
                    ||Long.valueOf(ModuleType.edocRec.ordinal()) == ordinal
                    ||Long.valueOf(ModuleType.edocSend.ordinal()) == ordinal
                    ||Long.valueOf(ModuleType.edocSign.ordinal()) == ordinal){
                return true;
            }
        }
        return false;
    }
	private boolean isGovdoc(String categoryKey){
        if(Strings.isNotBlank(categoryKey)){
        	//协同表单传入‘0,4’，转换出问题 直接判断。
        	String[] len = categoryKey.split(",");
        	if(len.length>1){
        		return false;
        	}
            int ordinal = Integer.valueOf(categoryKey);
            
            if (ModuleType.govdocSend.ordinal() == ordinal
                    ||ModuleType.govdocRec.ordinal() == ordinal
                    ||ModuleType.govdocSign.ordinal() == ordinal
                    || ModuleType.govdocSend.getKey() == ordinal
                    || ModuleType.govdocRec.getKey() == ordinal
                    || ModuleType.govdocSign.getKey() == ordinal) {
            	return true;
			}
        }
        return false;
    }
	 public List<CtpTemplate> getTemplateByWorflowUserId(Long userid){
	     return this.templateDao.getTemplateByWorflowUserId(userid);
	 }
	@Override
	public CtpTemplate getTemplateByNameAndCategoryId(String name,Long categoryId, boolean isDelete) throws BusinessException {
		return this.templateDao.getTemplateByNameAndCategoryId( name, categoryId,  isDelete);
	}
	/**
	 * 得到本单位（单位，部门，岗位，组）的ID + 集团ID
	 * @param accountId
	 * @return
	 * @throws BusinessException 
	 */
	private List<Long> getAccountOrgIds(Long accountId) throws BusinessException{
	    List<Long> ids = new UniqueList<Long>();
	    //职务级别
        List<V3xOrgLevel> levels = orgManager.getAllLevels(accountId);
        if(Strings.isNotEmpty(levels)){
            for(V3xOrgLevel level : levels){
                ids.add(level.getId());
            }
        }
        //部门
        List<V3xOrgDepartment> depts = orgManager.getAllDepartments(accountId);
        if(Strings.isNotEmpty(depts)){
            for(V3xOrgDepartment dept : depts){
                ids.add(dept.getId());
            }
        }
        //岗位
        List<V3xOrgPost> posts = orgManager.getAllPosts(accountId);
        if(Strings.isNotEmpty(posts)){
            for(V3xOrgPost post : posts){
                ids.add(post.getId());
            }
        }
        //组
        List<V3xOrgTeam> teams = orgManager.getAllTeams(accountId);
        if(Strings.isNotEmpty(teams)){
            for(V3xOrgTeam team : teams){
                ids.add(team.getId());
            }
        }
        ids.add(accountId);
        
        V3xOrgAccount root = orgManager.getRootAccount();
        if(root != null){
            ids.add(root.getId());
        }
        
        List<V3xOrgPost> standardPost = orgManager.getAllBenchmarkPost(accountId);
        if(Strings.isNotEmpty(standardPost)){
            for(V3xOrgPost post: standardPost){
                ids.add(post.getId());
            }
        } 
        
        //人
        List<V3xOrgMember> members = orgManager.getAllMembers(accountId);
        if(Strings.isNotEmpty(members)){
            for(V3xOrgMember m: members){
                ids.add(m.getId());
            }
        } 
        return ids;
	}

    @Override
    public List<CtpTemplate> getSystemTempletesByOrgEntity(String orgEntityType, Long orgEntityId, List<ModuleType> moduleTypes) throws BusinessException {
        // 当前部门相关的所有Id
        List<Long> domainIds = new ArrayList<Long>();
        //部门
        if (V3xOrgEntity.ORGENT_TYPE_DEPARTMENT.equals(orgEntityType)) {
            domainIds.add(orgEntityId);
            V3xOrgDepartment department = orgManager.getDepartmentById(orgEntityId);
            if (department != null) {
                String path = department.getParentPath();
                if (!StringUtil.checkNull(path)) {
                    domainIds = getDepartmentIds(domainIds, path);
                }
                //OA-50922  公文/协同：本单位或外单位制作的模版，没有显示在部门模版中  
                domainIds.add(OrgConstants.GROUPID);
                domainIds.add(department.getOrgAccountId());
            }
            //人员
        }else if(V3xOrgEntity.ORGENT_TYPE_ACCOUNT.equals(orgEntityType)){//vjoin 表单模版磁帖用
            domainIds.add(orgEntityId);
            domainIds.add(OrgConstants.GROUPID);
        } else if (V3xOrgEntity.ORGENT_TYPE_MEMBER.equals(orgEntityType)) {
            V3xOrgMember member = orgManager.getMemberById(orgEntityId);
            if (member != null) {
                List<Long> sourceIds = orgManager.getUserDomainIDs(orgEntityId, member.getOrgAccountId(),
                        ORGENT_TYPE.Member.name());
                if (!CollectionUtils.isEmpty(sourceIds)) {
                    domainIds.addAll(sourceIds);
                }
            }
            //其他组织实体在此追加
        }
        return templateDao.getTempleteOfDepartment(domainIds, moduleTypes);
    }
    
    /**
     * 递归查找上级部门
     * @param domainIds
     * @param path
     * @return
     * @throws BusinessException
     */
    private List<Long> getDepartmentIds(List<Long> domainIds, String path) throws BusinessException {
        V3xOrgDepartment parentDept = orgManager.getDepartmentByPath(path);
        if (parentDept != null) {
            domainIds.add(parentDept.getId());
            if (!StringUtil.checkNull(parentDept.getParentPath())) {
                getDepartmentIds(domainIds, parentDept.getParentPath());
            }
        }
        return domainIds;
    }
    @Override
    public List getPersonTemplateIds(String subject,String type) {
        User user = AppContext.getCurrentUser();
        List list = getPersonTemplateIds(user.getId(),subject,type, true);
        return list;
    }

    private List getPersonTemplateIds(Long memberId,String subject,String type, boolean hasBody){

        StringBuilder hql = new StringBuilder("select id");
        if(hasBody){
            hql.append(",body ");
        }
        hql.append(" from CtpTemplate c where c.memberId=:memberId and c.system =:system and");
        hql.append(" c.subject=:subject and c.type=:type and c.delete=:delete");

        Map map = new HashMap();
        //map.put("orgAccountId",user.getLoginAccount());
        map.put("memberId", memberId);
        map.put("system", Boolean.FALSE);
        map.put("subject",subject);
        map.put("type", type);
        map.put("delete", Boolean.FALSE);

        List list = DBAgent.find(hql.toString(),map);
        return list;
    }
	
	@AjaxAccess
	public boolean hasRepeatedPersonTemplate(Long memberId, String subject, String type, Long currentTemplateId) {
		
		
		StringBuilder hql = new StringBuilder("select id");
	    hql.append(" from CtpTemplate c where c.memberId=:memberId and c.system =:system and");
	    hql.append(" c.subject=:subject and c.type=:type and c.delete=:delete");
	    
        Map map = new HashMap();
        //map.put("orgAccountId",user.getLoginAccount());
        map.put("memberId", memberId);
        map.put("system", Boolean.FALSE);
        map.put("subject",subject);
        map.put("type", type);
        map.put("delete", Boolean.FALSE);

        List l = DBAgent.find(hql.toString(),map);
        
        
		if (Strings.isNotEmpty(l)) {
			if (l.size() == 1 && l.contains(currentTemplateId)) {
				return false;
			}
			else {
				return true;
			}
		}
		else {
			return false;
		}
	}
	
	/**
	 * 节点权限的修改
	 * @param user
	 * @throws BPMException
	 * @throws BusinessException
	 */
	public void updatePolicy(User user) throws BPMException,BusinessException{
		 Map<String, String> wfdef = ParamUtil.getJsonDomain("workflow_definition");
	     String processXml = wfdef.get("process_xml");
	     List<String> list = wapi.getWorkflowUsedPolicyIds("collaboration",processXml,null,null);
	     for(String strPname:list){
	    	 permissionManager.updatePermissionRef(EnumNameEnum.col_flow_perm_policy.name(),strPname,user.getLoginAccount());
	     }
	     
	}
	

    public String[] copyWorkFlowInfo(Long templateId) throws BPMException, BusinessException {
        String[] result = new String[2];
        String workFlowId = getCtpTemplate(templateId).getWorkflowId().toString();
        if (!StringUtil.checkNull(workFlowId)) {
            EnumManager em = (EnumManager) AppContext.getBean("enumManagerNew");
            Map<String, CtpEnumBean> ems = em.getEnumsMap(ApplicationCategoryEnum.collaboration);
            CtpEnumBean nodePermissionPolicy = ems.get(EnumNameEnum.col_flow_perm_policy.name());
            result[0] = wapi.getWorkflowNodesInfo(workFlowId, ModuleType.collaboration.name(), nodePermissionPolicy);
            result[1] = wapi.selectWrokFlowTemplateXml(workFlowId);
        }
        return result;
    }
    
   
    @Override
    public String getTemplateCreatorAlt(long memberId) {
        V3xOrgMember member;
        try {
            member = orgManager.getMemberById(memberId);
            if (member != null) {
                String memberName = Functions.showMemberName(member);
                StringBuffer sb = new StringBuffer();
                sb.append(ResourceUtil.getString("common.creater.label")).append(" : ");
                String s = Functions.showMemberAlt(member);
                if (Strings.isNotBlank(s)) {
                    sb.append(s);
                } else {
                    sb.append(memberName);
                }
                return sb.toString();
            } else {
                return null;
            }
        } catch (BusinessException e) {
            return null;
        }
    }
    
    /**
     * @param wapi the wapi to set
     */
    public void setWapi(WorkflowApiManager wapi) {
        this.wapi = wapi;
    }
    @Override
    public CtpTemplate getCtpTemplateByWorkFlowId(Long workflowid) throws BusinessException {
      return templateDao.getCtpTemplateByWorkFlowId(workflowid);
    }
    @Override
	public List<CtpTemplate> getCtpTemplateByWorkFlowIds(List<Long> workflowIds)
			throws BusinessException {
		return templateDao.getCtpTemplateByWorkFlowIds(workflowIds);
	}

	@ProcessInDataSource(name = DataSourceName.BASE)
	public void updateTemplates(Long contentId,TemplateEnum.State state) throws BusinessException{
        List<CtpTemplate> ts = new ArrayList<CtpTemplate>();
        try {
            ts = getCtpTemplates(contentId,false);
        } catch (BusinessException e) {
          LOG.error("停用表单更新模板状态出错", e);
            throw e;
        }
        Date nowDate = new Date();
        for(CtpTemplate t : ts){
        	if(state.equals(TemplateEnum.State.normal)){//保存并发布
        		if(!(null != t.getPublishTime() && t.getPublishTime().after(nowDate))){
        			t.setState(state.ordinal());
        		}
        	}else{
        		t.setState(state.ordinal());
        	}
            templateCacheManager.addCacheTemplate(t);
        }

        DBAgent.updateAll(ts);
    }
    
    @Override
    public List<CtpTemplate> getPersonalTemplete(String category, int count, boolean needCheckAthu)
            throws BusinessException {
        List<CtpTemplate> templatesTemps = templateDao.getPersonalTemplete(AppContext.currentUserId(), category, -1);
        // 获取有授权的模板,将有授权的模板也添加到首页栏目
        if (count == -1 || templatesTemps.size() < count){
        	return templatesTemps.subList(0, templatesTemps.size());
        }
        return templatesTemps.subList(0, count);
    }
    
    @Override
    public void updateTempleteConfig(Long[] ids) throws BusinessException {
        updateTempleteConfig(ids, AppContext.currentUserId());
    }
    
    @Override
    public void updateTempleteConfig(Long[] ids, Long memberId) throws BusinessException {
        int maxSort = templateDao.getCtpTemplateConfigMaxSort(memberId);
        updateTempleteConfig(ids, memberId, maxSort);
    }
    
    private void updateTempleteConfig(Long[] ids, Long memberId, int maxSort) throws BusinessException {
        if (ids == null)
            return;
        CtpTemplateConfig ctpTemplateConfig = null;
        CtpTemplate template = null;
        List<CtpTemplateConfig> configsTemp = null;
        List<CtpTemplateConfig> configs = new ArrayList<CtpTemplateConfig>();
        List<CtpTemplateConfig> updateConfigs = new ArrayList<CtpTemplateConfig>();
        for (Long id : ids) {
            template = getCtpTemplate(id);
            if (template == null)
                continue;
            configsTemp = templateDao.getCtpTemplateConfig(memberId, id);
            if (CollectionUtils.isEmpty(configsTemp)) {
                ctpTemplateConfig = new CtpTemplateConfig();
                ctpTemplateConfig.setNewId();
                ctpTemplateConfig.setMemberId(memberId);
                ctpTemplateConfig.setTempleteId(id);
                ctpTemplateConfig.setType(template.getModuleType());
                ctpTemplateConfig.setSort(++maxSort);
                ctpTemplateConfig.setDelete(false);
                configs.add(ctpTemplateConfig);
            } else {
                for (CtpTemplateConfig config : configsTemp) {
                    config.setDelete(false);
                    config.setSort(++maxSort);
                    updateConfigs.add(config);
                }
            }
        }
        templateDao.updateCtpTemplateConfig(updateConfigs);
        templateDao.saveCtpTemplateConfig(configs);
    }
    
    private void updateTempleteConfig_new(Long[] ids, List memberId, int maxSort) throws BusinessException {
        if (ids == null)
            return;
        CtpTemplateConfig ctpTemplateConfig = null;
        CtpTemplate template = null;
        template = getCtpTemplate(ids[0]);
        if(template == null){
        	return;
        }
        List<CtpTemplateConfig> configsTemp = null;
        List<CtpTemplateConfig> configs = new ArrayList<CtpTemplateConfig>();
        int j =memberId.size();
        if(j<1){
        	return;
        }
        for(int a = 0; a<j ; a++){
        	configsTemp = templateDao.getCtpTemplateConfig((Long)memberId.get(a), ids[0]);
        	if (CollectionUtils.isEmpty(configsTemp)) {
        		ctpTemplateConfig = new CtpTemplateConfig();
        		ctpTemplateConfig.setNewId();
        		ctpTemplateConfig.setMemberId((Long)memberId.get(a));
        		ctpTemplateConfig.setTempleteId(ids[0]);
        		ctpTemplateConfig.setType(template.getModuleType());
        		ctpTemplateConfig.setSort(++maxSort);
        		ctpTemplateConfig.setDelete(false);
        		configs.add(ctpTemplateConfig);
        	} else {
        		for (CtpTemplateConfig config : configsTemp) {
        			config.setDelete(false);
        			config.setSort(++maxSort);
        			configs.add(config);
        		}
        	}
        }
        templateDao.saveCtpTemplateConfig(configs);
    }
    
    @Override
    public void updateTempleteConfig(Long templateId, List<Long> memberIds) throws BusinessException {
        if (templateId != null && !CollectionUtils.isEmpty(memberIds)) {
            templateDao.deleteCtpTemplateConfig(templateId);
            Long[] ids = {templateId};
            for (Long memberId : memberIds) {
                updateTempleteConfig(ids, memberId, 0);
            }
        }
    }
    
    
    @Override
    public List<CtpTemplateCategory> getCategoryByName(String name, Long accountId) throws BusinessException {
        List<CtpTemplateCategory> result = new ArrayList<CtpTemplateCategory>();
        if (!StringUtil.checkNull(name)) {
            List<ModuleType> moduleTypes = new ArrayList<ModuleType>();
            moduleTypes.add(ModuleType.collaboration);
            moduleTypes.add(ModuleType.form);
            moduleTypes.add(ModuleType.govdocSend);
            moduleTypes.add(ModuleType.govdocRec);
            moduleTypes.add(ModuleType.govdocExchange);
            moduleTypes.add(ModuleType.govdocSign);
            List<CtpTemplateCategory> categorys = getCategorys(accountId, moduleTypes);
            if (!CollectionUtils.isEmpty(categorys)) {
                String categoryName = null;
                for (CtpTemplateCategory ctpTemplateCategory : categorys) {
                    categoryName = ctpTemplateCategory.getName();
                    if (name.equals(categoryName)) {
                        result.add(ctpTemplateCategory);
                    }
                }
            }
        }
        return result;
    }
    

    public CtpTemplate getTempleteByTemplateNumber(String templeteCode) {
        return templateDao.getTempleteByTemplateNumber(templeteCode);
    }
    
    /**
     * 设置图标,设置浮动显示的模版来源
     * @param showTemplates 模版集合
     * @param templeteIcon  模版的图片
     * @param templeteCreatorAlt 模版的浮动显示来源
     */
	public void floatDisplayTemplateSource(List<TemplateVO> showTemplates, Map<Long, String> templeteIcon,
			Map<Long, String> templeteCreatorAlt) {
		Map<Long, String> cashName = new HashMap<Long, String>();
		for(TemplateVO template : showTemplates) {
    		int type = template.getModuleType().intValue();
    		// 处理模板名包含特殊字符的情况
    		String subject = template.getSubject();
    		template.setSubject(Strings.getSafeLimitLengthString(subject,38,"..."));
    		template.setTapSubject(subject);
    		if(template.getMemberId() != null) {
    		    String creatorAlt = showTempleteCreatorAlt(template.getMemberId(), template.getOrgAccountId());
    		    if (creatorAlt != null) {
    		        if(template.getFormAppId() != null){
    		            if(cashName.get(template.getFormAppId()) == null){
    		                //TODO, 这里有性能问题
    		                CAPFormBean fBean = capFormManager.getForm(template.getFormAppId());
    		                if(fBean != null){
    		                    cashName.put(template.getFormAppId(), fBean.getFormName());
    		                }
    		            }
    		            StringBuffer sb = new StringBuffer();
    		            sb.append(ResourceUtil.getString("template.form.name")).append("：");
    		            sb.append(Strings.getSafeLimitLengthString(cashName.get(template.getFormAppId()),38,"...")).append("\n");
    		            sb.append(creatorAlt);
    		            creatorAlt =  sb.toString();
    		        }
    		        template.setTempleteCreatorAlt(Strings.toHTMLescapeRN(creatorAlt,false));
    		    }
    		}
    		String icon = "collaboration_16";
            if (type == ModuleType.edocSend.ordinal()
            		|| type == ModuleType.edocRec.ordinal()
            		|| type == ModuleType.edocSign.ordinal() 
                    || type == ModuleType.edoc.ordinal()
                    || type == ModuleType.govdocSend.getKey()
            		|| type == ModuleType.govdocRec.getKey() 
                    || type == ModuleType.govdocSign.getKey()) {
                icon = "red_text_template_16";
            }else if (type == ModuleType.info.ordinal()){
                icon = "infoTemplate_16";
            }else {
                if ("text".equals(template.getType())) {
                    icon = "format_template_16";
                } else if ("template".equals(template.getType()) 
                		&& String.valueOf(MainbodyType.FORM.getKey()).equals(template.getBodyType())) {
                    icon = "form_temp_16";
                } else if ("workflow".equals(template.getType())) {
                    icon = "flow_template_16";
                }
            }
            template.setTempleteIcon(icon);
            
    	}
	}
	
	/***
     * 显示模板的创建者等详细信息
     */
    private String showTempleteCreatorAlt(long memberId, long accountId) {
        try {
            V3xOrgMember member = orgManager.getMemberById(memberId);
            if (member == null) {
                return null;
            }
            
            String s = null;
            if(member.getIsAdmin()){
                s = Functions.showMemberName(member);
            }
            else{
                s = Functions.showMemberAlt(member);
            }
            
            StringBuilder sb = new StringBuilder();
            sb.append(ResourceUtil.getString("cannel.display.column.people.label")).append(" : ");
            sb.append(s);
            
            return sb.toString();
        }
        catch (BusinessException e) {
            return null;
        }
    }
    @AjaxAccess
    @Override
    public List<CtpTemplate> getPersonalRencentTemplete(String category, int count) throws BusinessException {
        Set<Long> ids = new HashSet<Long>();
        List<CtpTemplate> temps = new ArrayList<CtpTemplate>();
        if(count == 0) return temps;
        Long userId = AppContext.currentUserId();
        List<CtpTemplate> templates = templateDao.getCtpTemplateHistory(userId, category, 30);
        Map<Long,Boolean> isEnabled = this.isTemplateEnabled(templates, userId);
        if (templates != null) {
            for (CtpTemplate ctpTemplate : templates) {
                if (!ids.contains(ctpTemplate.getId())) {
                    ids.add(ctpTemplate.getId());
                    if (temps.size() < count && isEnabled.get(ctpTemplate.getId())) {
                        temps.add(ctpTemplate);
                    }
                    if(temps.size() >= count){
                    	break;
                    }
                }
            }
        }
        return temps;
    }
    
    @AjaxAccess
    public List<TemplateVO> getRecentUseTemplate(String category, int count) throws BusinessException {
    	User user = AppContext.getCurrentUser();
    	List<CtpTemplate> ctpTemplates = this.getPersonalRencentTemplete(category, count);
    	List<TemplateVO> templateVOs = new ArrayList<TemplateVO>();
    	 for (CtpTemplate template : ctpTemplates) {
         	TemplateVO templateVO = new TemplateVO();
         	templateVO.setSubject(template.getSubject());
            
             if (!template.getOrgAccountId().equals(user.getLoginAccount())) {
             	V3xOrgAccount outOrgAccount = orgManager.getAccountById(template.getOrgAccountId());
             	templateVO.setSubject(template.getSubject()+"("+outOrgAccount.getShortName()+")");
             }
             
             templateVO.setId(template.getId());
             templateVO.setCategoryId(template.getCategoryId());
             templateVO.setMemberId(template.getMemberId());
             templateVO.setModuleType(template.getModuleType());
             templateVO.setSystem(template.isSystem());
             templateVO.setType(template.getType());
             templateVO.setBodyType(template.getBodyType());
             templateVO.setOrgAccountId(template.getOrgAccountId());
             templateVOs.add(templateVO);
         }
    	Map<Long, String> templeteIcon = new HashMap<Long, String>();
     	Map<Long, String> templeteCreatorAlt = new HashMap<Long, String>();
     	//设置图标,设置浮动显示的模版来源
     	this.floatDisplayTemplateSource(templateVOs, templeteIcon, templeteCreatorAlt);
    	return templateVOs;
    	
    }


	
	
	@Override
	@AjaxAccess
    public List<CtpTemplate> getRecentTemplates(String moduleTypes, int count) throws BusinessException {
        Set<Long> ids = new HashSet<Long>();
        List<CtpTemplate> temps = new ArrayList<CtpTemplate>();
        Long currentUserId = AppContext.currentUserId();
        //查询30条，然后剔除没有权限的，确保还能剩下10条。
        Map<String,String> queryParams = new HashMap<String,String>();
        queryParams.put("moduleType", moduleTypes);
        
        List<CtpTemplate> templates = templateCacheManager.cacheRecentCallTemplates(currentUserId);
        
        List<Integer> moduleTypeInt = new ArrayList<Integer>();
        String[] modules = moduleTypes.split("[,]");
        for(String mt : modules){
            moduleTypeInt.add(Integer.valueOf(mt));
        }
        if(Strings.isNotEmpty(moduleTypeInt)){
        	for(Iterator<CtpTemplate> it = templates.iterator();it.hasNext();){
        		CtpTemplate t = it.next();
        		if(!moduleTypeInt.contains(t.getModuleType())){
        			it.remove();
        		}
        	}
        }
        
        
        if (templates != null) {
            Map<Long, Boolean> templateId2enabled = isTemplateEnabled(templates, currentUserId);
            for (CtpTemplate ctpTemplate : templates) {
            	
                Boolean templateEnabled = templateId2enabled.get(ctpTemplate.getId());
                if (templateEnabled == null || !templateEnabled) {
                    continue;
                }

                if (!ids.contains(ctpTemplate.getId())) {
                    if ("32,-1".equals(moduleTypes) && !ctpTemplate.isSystem() && !Integer.valueOf(32).equals(ctpTemplate.getModuleType())) {
                        continue;
                    }
                    ids.add(ctpTemplate.getId());
                    if (temps.size() < count && (templateEnabled != null && templateEnabled)) {
                        temps.add(ctpTemplate);
                    }
                }
				if (temps.size() >= 10) {
					break;
				}
            }
        }
        return temps;
    }
	
	@Override
	@AjaxAccess
    public List<TemplateVO> getRecentTemplateVos(String moduleTypes, int count) throws BusinessException {
		List<TemplateVO> templateVos = new ArrayList<TemplateVO>();
		
		User user = AppContext.getCurrentUser();
		List<CtpTemplate> templates = this.getRecentTemplates(moduleTypes, count);
		
		for (CtpTemplate template : templates) {
			TemplateVO templateVO = new TemplateVO();
			templateVO.setSubject(template.getSubject());
			
			if (!template.getOrgAccountId().equals(user.getLoginAccount())) {
             	V3xOrgAccount outOrgAccount = orgManager.getAccountById(template.getOrgAccountId());
             	templateVO.setSubject(template.getSubject()+"("+outOrgAccount.getShortName()+")");
             }
			
			templateVO.setId(template.getId());
            templateVO.setCategoryId(template.getCategoryId());
            templateVO.setMemberId(template.getMemberId());
            templateVO.setModuleType(template.getModuleType());
            templateVO.setSystem(template.isSystem());
            templateVO.setType(template.getType());
            templateVO.setBodyType(template.getBodyType());
            templateVO.setOrgAccountId(template.getOrgAccountId());
            templateVO.setFormAppId(template.getFormAppId());
			int type = template.getModuleType().intValue();
			String icon = "collaboration_16";
			if (type == ModuleType.edocSend.ordinal()
					|| type == ModuleType.edocRec.ordinal()
					|| type == ModuleType.edocSign.ordinal() 
					|| type == ModuleType.edoc.ordinal()
					|| type == ModuleType.govdocSend.getKey()
            		|| type == ModuleType.govdocRec.getKey()
                    || type == ModuleType.govdocSign.getKey()) {
				icon = "red_text_template_16";
			} else {
				if ("text".equals(template.getType())) {
					icon = "format_template_16";
				} else if ("template".equals(template.getType()) 
						&& String.valueOf(MainbodyType.FORM.getKey()).equals(template.getBodyType())) {
					
					icon = "form_temp_16";
				} else if ("workflow".equals(template.getType())) {
					icon = "flow_template_16";
				}
			}
			if(template.isSystem() == null || template.isSystem() == false){
				icon = "person_template_16";
			}
			templateVO.setTempleteIcon(icon);
			templateVos.add(templateVO);
		}
		
        return templateVos;
    }

	/**     
	 * @see com.seeyon.ctp.common.template.manager.TemplateManager#getRecentTemplateDetailVos(java.lang.String, int)   
	 */  
	@AjaxAccess
	public List<TemplateDetailVO> getRecentTemplateDetailVos(List<ModuleType> moduleTypes, int count) throws BusinessException {
		
		if(Strings.isEmpty(moduleTypes)) {
			return new ArrayList<TemplateDetailVO>();
		}
		
		List<Integer> mTypes = new ArrayList<Integer>();
		for (ModuleType moduleType : moduleTypes) {
			mTypes.add(moduleType.getKey());
		}
		
		List<CtpTemplate> rencentTemplates = this.getRecentTemplates(Strings.join(mTypes, ","), count);
		
		return convert2TemplateDetailVO(rencentTemplates);
	}
	
	
	private List<TemplateDetailVO> convert2TemplateDetailVO(List<CtpTemplate> templates) {
		List<TemplateDetailVO> templateDetailVOs = new ArrayList<TemplateDetailVO>();
		if(!Strings.isEmpty(templates)) {
			for(CtpTemplate ctpTemplate:templates){
				if(String.valueOf(MainbodyType.FORM.getKey()).equals(ctpTemplate.getBodyType()) && (ctpTemplate.isSystem() || "20".equals(ctpTemplate.getBodyType()) )){
					checkTemplateName(ctpTemplate); 
					TemplateDetailVO v = TemplateDetailVO.valueOf(ctpTemplate);
					checkHasTemplate(ctpTemplate, v); 
					templateDetailVOs.add(v);
				}
			}			
		}
		return templateDetailVOs;
	}
	
	/**
	 * 设置表单模板是否有套红模板
	 * 
	 * @param ctpTemplate 模板对象
	 * @param v ： 模板VO
	 *
	 */
	private void checkHasTemplate(CtpTemplate ctpTemplate, TemplateDetailVO v){
	    if(ctpTemplate.getFormAppId() != null){
            FormBean f = formApi4Cap3.getForm(ctpTemplate.getFormAppId());
            if(f != null){
               v.setHasOfficeTemp(f.hasRedTemplete());
            }
        }
	}
	
	/**
	 * 获取外单位简称
	 * @param ctpTemplate
	 */
	private void checkTemplateName(CtpTemplate ctpTemplate) {
		Long caccountId = AppContext.getCurrentUser().getLoginAccount();
	    String shortName="";
        if (null != ctpTemplate.getOrgAccountId() && !ctpTemplate.getOrgAccountId().equals(caccountId)) {
        	V3xOrgAccount orgAccount;
			try {
				orgAccount = orgManager.getAccountById(ctpTemplate.getOrgAccountId());
				if(null!=orgAccount){
            		shortName = orgAccount.getShortName();
            	}
                if (Strings.isNotBlank(shortName)) {
                	ctpTemplate.setSubject(ctpTemplate.getSubject() + "(" + shortName + ")");
                } 
			} catch (BusinessException e) {
				LOG.error("获取外单位出错",e);
			}
        	
        }
	}
	

	/**     
	 * @see com.seeyon.ctp.common.template.manager.TemplateManager#getTemplateDetailVos(java.util.List)   
	 */  
	@AjaxAccess
	public List<TemplateDetailVO> getTemplateDetailVos(List<ModuleType> moduleTypes) throws BusinessException {
		List<TemplateDetailVO> templateDetailVOs = new ArrayList<TemplateDetailVO>();
		Long userId = AppContext.currentUserId();
		// 获取所有的系统模板
		List<CtpTemplate> systemTempletes = this.getSystemTemplatesByAcl(userId, moduleTypes);
		// 获取所有的个人模板
		List<CtpTemplate> personalTempletes = this.getPersonalTemplates(userId, moduleTypes);
		
		templateDetailVOs.addAll(convert2TemplateDetailVO(systemTempletes));
		templateDetailVOs.addAll(convert2TemplateDetailVO(personalTempletes));
		
		return templateDetailVOs;
	}
	
//    @Override
//    public void deleteCtpTemplatetAuths(List<Long> memberIds,Integer moduleType) throws BusinessException {
//        if (Strings.isNotEmpty(memberIds)) {
//            List<Long>[] arr = Strings.splitList(memberIds, 1000);
//            for (List<Long> idl : arr) {
//                 templateDao.deleteCtpTemplatetAuthsByMemberIdsAndModuleType(idl, moduleType);
////                 需要删除人员的模板权限***************
//                templateCacheManager.deleteCacheTempalteAuthsByUserIds(idl,moduleType);
//            }
//        }
//    }
    
    @Override
    public CtpTemplate getCtpTemplateByWorkFlowId(Long workflowId,boolean isSystem){
    	return this.templateDao.getCtpTemplateByWorkFlowId(workflowId, isSystem);
    }
    
    public String[] getCtpTemplateBySummary(String summaryKey){
    	List<CtpTemplate> list = this.templateDao.getCtpTemplateWithoutDeletedBySummary(summaryKey);
    	String[] templateNames = {};
    	if(list != null){
    		templateNames = new String[list.size()];
    		CtpTemplate t;
    		for(int i=0;i<list.size();i++){
    			templateNames[i] = list.get(i).getSubject(); 
    		}
    	}
    	return templateNames;
    }
	
	
	@Override
	public void saveTemplateRecent(Long id,Long memberId) throws BusinessException {
		updateTempleteRecent(id, memberId);
	}

	/**
	 * 最近使用模板更新。需要区分处理与使用，调用this.addRecentTemplete
	 */
	public void updateTempleteRecent(long memberId,long id)throws BusinessException {
		CtpTemplate template = templateDao.getTemplete(id);
		if (template == null) {
			return;
		}
		Map<String, Object> args = new HashMap<String, Object>();
		args.put("memberId", memberId);
		args.put("templeteId", id);
		List<CtpTemplateRecent> historyList = templateDao.getCtpTemplateRecent(args);

		if(Strings.isNotEmpty(historyList)){
            templateDao.deleteCtpTemplateRecent(historyList);
        }

		CtpTemplateRecent history = new CtpTemplateRecent();
		history.setNewId();
		history.setMemberId(memberId);
		history.setTempleteId(id);
		history.setType(template.getModuleType());
		history.setCallDate(new Date());
		history.setUseType(TemplateEnum.RecentUseType.call.getKey());
		templateDao.saveCtpTemplateRecent(history);
		templateCacheManager.addCacheRecentTemplate(id,memberId);
	}

	@Override
	@SuppressWarnings("unchecked")
	public void deleteTemplateRecent(Long templeteId)throws BusinessException{
	   if(null == templeteId){
	     return;
	   }
	   String hql="delete from CtpTemplateRecent c where c.templeteId = :templeteId";
	   Map pMap = new HashMap();
	   pMap.put("templeteId", templeteId);
	   DBAgent.bulkUpdate(hql, pMap);
	}
	
	@Override
	public String getTemplateBodyType(String templateId) throws BusinessException {
		String bodyType="";
		Long tId=0L;
		if(Strings.isNotBlank(templateId)){
			tId=Long.valueOf(templateId);
		}
		CtpTemplate template = templateDao.getTemplete(tId);
		if(template != null){
			bodyType = template.getBodyType();
		}
		return bodyType;
	}
	
	@Override
	public String getAllTemplateCategoryIdsAndTemplateIds(String templateStr) throws BusinessException {
		User user = AppContext.getCurrentUser();
		//选择了的模板分类Id
		Set<Long> selectedTemplateCatageryIdArray = new HashSet<Long>();
		//选择的模板分类下的所有子分类
		Set<Long> selectTemplateCatageryIds = new HashSet<Long>();
		//选择了的模板Id
		Set<Long> selectedTemplateIds = new HashSet<Long>();
		List<Integer> types = new ArrayList<Integer>();
        types.add(ModuleType.collaboration.getKey());
        types.add(ModuleType.form.getKey());
        if (AppContext.hasPlugin("edoc")) {
        	types.add(ModuleType.edoc.getKey());
        	if(types.contains(ModuleType.edoc.getKey())){
        		if(user.isInternal()){
        			//外部人员过滤公文。
        			types.add(ModuleType.edocSend.getKey());
        			types.add(ModuleType.edocRec.getKey());
        			types.add(ModuleType.edocSign.getKey());
        		}
        	}
		}
		if(Strings.isNotBlank(templateStr)){
			String[] selectTemplateIds = templateStr.split(",");
			for(String templateId : selectTemplateIds){
				if(Strings.isNotBlank(templateId)){
					if(templateId.startsWith("C_")){
						selectedTemplateCatageryIdArray.add(Long.valueOf(templateId.substring(2, templateId.length())));
					}else{
						selectedTemplateIds.add(Long.valueOf(templateId));
					}
				}
			}
			//没有模板分类时直接返回原字符串
			if(null==selectedTemplateCatageryIdArray || selectedTemplateCatageryIdArray.size()<=0){
				return templateStr;
			}
			selectTemplateCatageryIds = this.getTemplateCategoryIdAndChildenTemplateCatagoryId(selectedTemplateCatageryIdArray);
		}
		Set<String> templateCategoryId = new HashSet<String>();
		//选择的模板分类下的所有子分类并且包含外单位的模板
        Set<Long> templateCatageryIds = new HashSet<Long>();
        templateCatageryIds.addAll(selectTemplateCatageryIds);
        
        
        //循环遍历缓存中的分类。使用名字相同的添加上
        if(selectTemplateCatageryIds.size() > 0) {
        	Map<String,Long> nameMap = new HashMap<String,Long>();
            for(Long categoryId:selectTemplateCatageryIds){
                CtpTemplateCategory ctpCategory = templateCategoryManager.get(categoryId);
                if(ctpCategory == null) {
                    continue;
                }
                String categoryName = ctpCategory.getName();
                nameMap.put(categoryName, ctpCategory.getOrgAccountId());
            }
            
            List<CtpTemplateCategory> sameNames = templateCategoryManager.getTemplateCategorysInOtherAccountByName(nameMap);
            
            if(Strings.isNotEmpty(sameNames)){
                for (CtpTemplateCategory category : sameNames) {
                    if(!templateCatageryIds.contains(category.getId())) {
                        templateCatageryIds.add(category.getId());
                    }
                }
            }
                
        }
        
        for(Long categoryId:templateCatageryIds){
			templateCategoryId.add("C_"+categoryId);
		}
		String templateAndTemplateCategoryIdStr = Strings.join(selectedTemplateIds, ",")+","+Strings.join(templateCategoryId, ",");
		return templateAndTemplateCategoryIdStr;
	}
	

	/**
	 * 获取一级分类下所有的子分类Id
	 * @param templateCategoryId 选择的
	 * @return
	 * @throws BusinessException
	 */
	private Set<Long> getTemplateCategoryIdAndChildenTemplateCatagoryId(Set<Long> templateCategoryId) throws BusinessException{
		Set<Long> templateCategoryIdArray = new HashSet<Long>();
		if(null!=templateCategoryId && templateCategoryId.size()>0){
			Set<Long> categoryIds = templateCategoryManager.getAllCacheTemplateCategoryIds();
			templateCategoryIdArray = getChildCategorys(categoryIds,templateCategoryId);
		}
		
		return templateCategoryIdArray;
	}
	
	/**
	 * 根据allCategoryIds获取下面的所有模版分类ID
	 * @param allCategoryIds 所有的模板分类Id
	 * @param selectedsCategoryIds 选择的分类
	 * @return
	 * @throws BusinessException
	 * @throws CloneNotSupportedException 
	 */
	private Set<Long> getChildCategorys(Set<Long> allCategoryIds, Set<Long> selectedsCategoryIds)
			throws BusinessException {
		int templateCategoryIdsSize = selectedsCategoryIds.size();
		List<Integer> listEdocTypes = new ArrayList<Integer>();
		List<Integer> listPublicTypes = new ArrayList<Integer>();
		if (AppContext.hasPlugin("edoc")) {
			listEdocTypes.add(ModuleType.edocSend.getKey());
			listEdocTypes.add(ModuleType.edocRec.getKey());
			listEdocTypes.add(ModuleType.edocSign.getKey());
		}
		listPublicTypes.add(ModuleType.collaboration.getKey());
		listPublicTypes.add(ModuleType.form.getKey());
		Set<Long> remainCategoryIds = new HashSet<Long>();
		
		
		if(Strings.isNotEmpty(selectedsCategoryIds)) {
			for(Long categoryId : allCategoryIds) {
				CtpTemplateCategory ctpTemplateCategory = templateCategoryManager.getIncludeAllChildren(categoryId);
				if (null != ctpTemplateCategory.isDelete() && ctpTemplateCategory.isDelete()) {
					continue;
				}
				CtpTemplateCategory category;
				try {
					category = (CtpTemplateCategory) ctpTemplateCategory.clone();
				
					category.setId(ctpTemplateCategory.getId());
					category.setParentId(ctpTemplateCategory.getParentId());
					Integer type = category.getType();
	
					boolean isParentColOrFormRoot = (Long.valueOf(ModuleType.form.getKey()).equals(category.getParentId())
							|| Long.valueOf(ModuleType.collaboration.getKey()).equals(category.getParentId()));
					
					if (null == category.getParentId() && null != type && listEdocTypes.contains(type)) {
						category.setParentId(TemplateCategoryConstant.edocRoot.key());
					}
					else if ((null == category.getParentId() && null != type && listPublicTypes.contains(type))
							|| (null != category.getParentId() && null != type
									&& isParentColOrFormRoot)){
						
						category.setParentId(TemplateCategoryConstant.publicRoot.key());
					}
					
					if (selectedsCategoryIds.contains(category.getParentId())) {

						selectedsCategoryIds.add(category.getId());
						
						List<CtpTemplateCategory> allCascadeChildrens =  category.getAllCascadeChildrens();
						if(Strings.isNotEmpty(allCascadeChildrens)) {
							for(CtpTemplateCategory c : allCascadeChildrens)
								selectedsCategoryIds.add(c.getId());
						}
					}
	
				} catch (CloneNotSupportedException e) {
					LOG.error("", e);
				}
			}
		}
		return selectedsCategoryIds;
	}
	public boolean checkTemplateEnabel(String templateId) throws BusinessException {
		boolean templateEnable = true;
		String bodyType="";
		Long tId=0L;
		if(Strings.isNotBlank(templateId)){
			tId=Long.valueOf(templateId);
		}
		CtpTemplate template = templateDao.getTemplete(tId);
		if(template != null){
			bodyType = template.getBodyType();
		}
		if(!TemplateUtil.isForm(bodyType)){//只检查表单模板
			return templateEnable;
		}
		try{
			List<CtpContentAll> contents = ctpMainbodyManager.getContentListByModuleIdAndModuleType(ModuleType.collaboration, tId);
			CtpContentAll content = contents.get(0);
			if(content != null){
				long tempId = content.getContentTemplateId();
				templateEnable = capFormManager.isEnabled(tempId);
			}
		}catch(Exception e){
			LOG.error(e.getMessage(), e);
		}
		return templateEnable;
	}

	@Override
	public CtpTemplateCategory getRootCategory(CtpTemplateCategory ctpTemplateCategory) throws BusinessException {
	    if (ctpTemplateCategory == null){
			return null;
		}
		 
        if (ctpTemplateCategory.getParentId() == null
                || (ctpTemplateCategory.getParentId() > 0 && ctpTemplateCategory.getParentId() < 100)) {
            return ctpTemplateCategory;
        }
        return getRootCategory(getCategorybyId(ctpTemplateCategory.getParentId()));
	}

	@Override
	public List<CtpTemplate> findCtpTemplateRecents(Long userid, String category, int count) throws BusinessException {
		return templateDao.getCtpTemplateHistory(userid, category, count);
	}

    @ProcessInDataSource(name = DataSourceName.BASE)
	public void updateTemplates(Date modifyDate, Long modifyMemberId, List<Long> ids) throws BusinessException {
		templateDao.updateTemplates(modifyDate, modifyMemberId, ids);
	}

	@Override
	public List<Long> getTemplateIdsByCategoryIds(List<Long> templateCategoryIds) throws BusinessException {
		List<Long> templateIds = new ArrayList<Long>();
		if(!templateCategoryIds.isEmpty()){
			List<CtpTemplate> ctpTemplates = templateDao.getCtpTemplateByCategoryIds(templateCategoryIds);
			for(CtpTemplate template : ctpTemplates){
				templateIds.add(template.getId());
			}
		}
        return templateIds;
	}
	@Override
	public List<CtpTemplateCategory> getTemplateCategoryListByIds(List<Long> ids) throws BusinessException{
		List<CtpTemplateCategory> templateCategory = new ArrayList<CtpTemplateCategory>();
		if(null!=ids && ids.isEmpty()){
			return templateCategory;
		}
		return templateDao.getCtpTemplateCategoryByIds(ids);
	}

	public Map<Long, CtpTemplateCategory>  getAllShowCategorys(Long orgAccountId, String category,Map<String, CtpTemplateCategory> nameCategory) {
		Map<Long, CtpTemplateCategory> idCategorys = new HashMap<Long, CtpTemplateCategory>();
		String[] typestrs = StringUtils.split(category, ",");
        List<ModuleType> moduleTypes = new ArrayList<ModuleType>();
        //判断是否有信息报送插件
        Boolean hasInfoPlugin = AppContext.hasPlugin("infosend");
        Boolean hasEdoc =  AppContext.hasPlugin("edoc");
        for (int i = 0; i < typestrs.length; i++) {
        	String cType = typestrs[i];
        	if("-1".equals(cType)){
        		continue;
        	}
            if(String.valueOf(ApplicationCategoryEnum.info.key()).equals(cType) && !hasInfoPlugin) {
        		continue;
        	}
            if(!hasEdoc && isEdoc(cType)){
                continue;
            }
        	try {
        	    ModuleType m = ModuleType.getEnumByKey(Integer.valueOf(cType));
        	    if(m != null){
        	        moduleTypes.add(m);
        	    }
            } catch (IllegalArgumentException e) {
            	LOG.error("获取枚举报错了", e);
            }
        }
        
        // 取对应单位某模块的模板分类
        List<CtpTemplateCategory> templeteCategory = new ArrayList<CtpTemplateCategory>();
        
        List<CtpTemplateCategory> templeteCategoryTemp = this.getCategorys(orgAccountId, moduleTypes);
        // 移出已经删除的模板分类
        if (!CollectionUtils.isEmpty(templeteCategoryTemp)) {
            for (CtpTemplateCategory ctpTemplateCategory : templeteCategoryTemp) {
                if (ctpTemplateCategory.isDelete() == null || !ctpTemplateCategory.isDelete()) {
                    templeteCategory.add(ctpTemplateCategory);
                }
            }
        }
        for (CtpTemplateCategory c : templeteCategory) {
            nameCategory.put(c.getName(), c);
            idCategorys.put(c.getId(), c);
        }
        
        return idCategorys;
	}


	@Override
	public boolean templateCanUse(Long templateId) throws BusinessException {
		if(templateId==null){
			return false;
		}
		CtpTemplate template = templateDao.getTemplete(templateId);
		if(null==template){
			return false;
		}
		return !template.isDelete();
	}

	@Override
	public FlipInfo getAllSystemTempletesByAccountAndSpecialAuthID(FlipInfo flipInfo, Map<String, Object> params)
			throws BusinessException {
		templateDao.getAllSystemTempletesByAccountAndSpecialAuthID(flipInfo, params);
		return flipInfo;
	}

	@Override
    public void addRecentTemplete(long memberId, int moduleType, long templateId,TemplateEnum.RecentUseType type) throws BusinessException {
        
	       
        Map<String,Object> m = new HashMap<String,Object>();
        m.put("memberId", memberId);
        m.put("moduleType", moduleType);
        m.put("templateId", templateId);
        m.put("useType", type.getKey());
        templateRecentBatchTaskManager.addTask(m);
        
        if(Integer.valueOf(TemplateEnum.RecentUseType.call.getKey()).equals(type.getKey())) {
        	templateCacheManager.addCacheRecentTemplate(Long.valueOf(templateId),memberId);
        }
        
    }
  public int deleteLessThanDate(Date _callDate) throws BusinessException {
	  return templateDao.deleteLessThanDate(_callDate);
  }

	
	/**
	 * 获取所有人员信息列表
	 * @param ctpTemplateAuths
	 * @return
	 * @throws BusinessException
	 */
	private List<V3xOrgMember> getAllCtpTemplateAuthMemberId(List<CtpTemplateAuth> ctpTemplateAuths) throws BusinessException{
		List<V3xOrgMember> list_member = new ArrayList<V3xOrgMember>();
		if(ctpTemplateAuths == null){
			return list_member;
		}
		int size = ctpTemplateAuths.size();
		String authType = "";
		Long authId;
		for(int i = 0 ; i < size ; i++){
			CtpTemplateAuth ctpTemplateAuth = ctpTemplateAuths.get(i);
			authType = ctpTemplateAuth.getAuthType();
			authId = ctpTemplateAuth.getAuthId();
			if(V3xOrgEntity.ORGENT_TYPE_ACCOUNT.equals(authType)){
				List<V3xOrgMember> members = orgManager.getAllMembersByAccountId(authId, 1, true, true, null, null, null);
				list_member.addAll(members);
			}else if(V3xOrgEntity.ORGENT_TYPE_DEPARTMENT.equals(authType)){
				List<V3xOrgMember> members = orgManager.getAllMembersByDepartmentId(authId, true, 1, null, true, null, null, null);
				list_member.addAll(members);
			}else if(V3xOrgEntity.ORGENT_TYPE_MEMBER.equals(authType)){
				V3xOrgMember v3xOrgMember = orgManager.getMemberById(authId);
				list_member.add(v3xOrgMember);
			}else if(V3xOrgEntity.ORGENT_TYPE_TEAM.equals(authType)){
				List<V3xOrgMember> members = orgManager.getMembersByTeam(authId);
				list_member.addAll(members);
			}else if(V3xOrgEntity.ORGENT_TYPE_POST.equals(authType)){
				List<V3xOrgMember> members = orgManager.getMembersByPost(authId);
				list_member.addAll(members);
			}else if(V3xOrgEntity.ORGENT_TYPE_LEVEL.equals(authType)){
				List<V3xOrgMember> members = orgManager.getMembersByLevel(authId);
				list_member.addAll(members);
			}
		}
		HashSet<V3xOrgMember> set = new HashSet<V3xOrgMember>(list_member);
		list_member.clear();
		list_member.addAll(set);
		return list_member;
	}

	private Map<String, Object> ctpTemplate2CtpTemplateConfig(CtpTemplate template, List<V3xOrgMember> users) throws BusinessException{
		Map<String, Object> rMap = new HashMap<String, Object>();
		List<CtpTemplateConfig> addList = new ArrayList<CtpTemplateConfig>();
		List<CtpTemplateConfig> updateList = new ArrayList<CtpTemplateConfig>();
		if(template == null){
			return rMap;
		}
		
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("templeteId", template.getId());
		List<CtpTemplateConfig> ctpTemplateConfigs = templateDao.getCtpTemplateConfig(null, params);
		
		List<Long> list_member_ids = getMemberIds(users);
		
		for(CtpTemplateConfig ctpTemplateConfig : ctpTemplateConfigs){
			if(list_member_ids.size() == 0){
				if(!ctpTemplateConfig.isDelete()){
					ctpTemplateConfig.setDelete(true);
					updateList.add(ctpTemplateConfig);  //更新成无效标志
				}
			}else{
				if(list_member_ids.contains(ctpTemplateConfig.getMemberId())){
					if(ctpTemplateConfig.isDelete()){
						ctpTemplateConfig.setDelete(false);
						updateList.add(ctpTemplateConfig);  //更新成有效标志
					}
				}else{
					if(!ctpTemplateConfig.isDelete()){
						ctpTemplateConfig.setDelete(true);
						updateList.add(ctpTemplateConfig);  //更新成无效标志
					}
				}
			}
		}
		CtpTemplateConfig addCtpTemplateConfig;
		String label;
		for(Long memberId : list_member_ids){
			label = "0";
			for(CtpTemplateConfig ctpTemplateConfig : ctpTemplateConfigs){
				if(ctpTemplateConfig.getMemberId().equals(memberId)){
					label = "1";
					continue;
				}
			}
			if(label == "0"){
				addCtpTemplateConfig = new CtpTemplateConfig();
				addCtpTemplateConfig.setIdIfNew();
				addCtpTemplateConfig.setMemberId(memberId);
				addCtpTemplateConfig.setSort(template.getSort());
				addCtpTemplateConfig.setTempleteId(template.getId());
				addCtpTemplateConfig.setType(template.getModuleType());
				addCtpTemplateConfig.setDelete(false);
				addList.add(addCtpTemplateConfig);
			}
		}
		
		rMap.put("updateList", updateList);
		rMap.put("addList", addList);
		return rMap;
	}
	
	/**
	 * 获取人员ID
	 * @param list
	 * @return
	 */
	public List<Long> getMemberIds(List<V3xOrgMember> list){
		List<Long> rlist = new ArrayList<Long>();
		for(V3xOrgMember member : list){
			rlist.add(member.getId());
		}
		return rlist;
	}

	
	public List<CtpTemplate> getTemplateRecents(Long userid, int count,TemplateEnum.RecentUseType type, Map<String,String> queryParams) throws BusinessException {
	     return templateDao.getRecentTemplates(userid, count, type, queryParams);
	}
	
    @Override
	public List<CtpTemplateConfig> getCtpTemplateConfig(FlipInfo flipInfo,
			Map<String, Object> params) throws BusinessException {
		return this.templateDao.getCtpTemplateConfig(flipInfo, params);
	}
	/**
	 * 此方法仅供首页模板栏目调用，对首页模板栏目进行特殊操作
	 * 向模板配置表中插入授权数据，删除未授权数据
	 * @param userId
	 */
	@SuppressWarnings("unchecked")
	public List<CtpTemplate> transMergeCtpTemplateConfig(Long userId) throws BusinessException {
		if(userId == null){
			return new ArrayList<CtpTemplate>();
		}
		//===================================处理向模板配置表中插入授权数据，删除未授权数据========开始===============================
		//所有的模板集合，包含有权限使用的系统模板和个人模板
		List<CtpTemplate> allTemplateList = new ArrayList<CtpTemplate>();
		//查询出所有的有权限使用的模板，(不包含计划模板)用来判断是否需要往配置表中插入数据
		List<ModuleType> categoryList = new ArrayList<ModuleType>();
		categoryList.add(ModuleType.getEnumByKey(1));
		categoryList.add(ModuleType.getEnumByKey(2));
		categoryList.add(ModuleType.getEnumByKey(4));
//		categoryList.add(ModuleType.getEnumByKey(19));
//		categoryList.add(ModuleType.getEnumByKey(20));
//		categoryList.add(ModuleType.getEnumByKey(21));
		categoryList.add(ModuleType.getEnumByKey(32));
		categoryList.add(ModuleType.getEnumByKey(401));
		categoryList.add(ModuleType.getEnumByKey(402));
		categoryList.add(ModuleType.getEnumByKey(404));
		List<CtpTemplate> allSystemTempletes = getSystemTemplatesByAcl(userId,categoryList);
		//查询个人模板
		List<CtpTemplate> personalTempletes  = getPersonalTemplates(userId);
        //合并系统模板和个人模板
		allTemplateList.addAll(allSystemTempletes);
        allTemplateList.addAll(personalTempletes);

        //查询配置的所有模板，包含删除的
        Map<String, Object> params = new HashMap<String, Object>();

        params.put("userId", userId);
        List<CtpTemplateConfig> templateConfigList = this.getCtpTemplateConfig(null, params);

        //配置表中所有的模板id
        Set<Long> setAll = new HashSet<Long>();
        if(Strings.isNotEmpty(templateConfigList)){
        	for(CtpTemplateConfig templateConfig : templateConfigList){
    			Long templateId = templateConfig.getTempleteId();
    			setAll.add(templateId);
    		}
        }
        //需要往配置表中插入的数据集合
        List<CtpTemplate> insetList = new ArrayList<CtpTemplate>();
        // 需要从配置表中删除一些没有授权的数据
        Set<Long> all = new HashSet<Long>();
        if(Strings.isNotEmpty(allTemplateList)){
        	for(Iterator<CtpTemplate> iter = allTemplateList.iterator();iter.hasNext();){
            	CtpTemplate tem = iter.next();
            	Long temId = tem.getId();
            	all.add(temId);
            	if(!setAll.contains(temId)){
            		insetList.add(tem);
            	}
            }
        }
        //如果配置表中没有，要将CtpTemplate表中的数据添加
        if(Strings.isNotEmpty(insetList)){
        	List<CtpTemplateConfig> ctpTemplateConfig = this.ctpTemplate2CtpTemplateConfig(insetList);
            DBAgent.saveAll(ctpTemplateConfig);
        }
        //删除配置表中没有授权的数据
        List<Long> delIds = new ArrayList<Long>();
        Set<Long>  filter  = new HashSet<Long>();
    	if(Strings.isNotEmpty(templateConfigList)){
        	for(CtpTemplateConfig templateConfig : templateConfigList){
    			Long templateId = templateConfig.getTempleteId();
    			if(!filter.contains(templateId)){
    				filter.add(templateId);
    			}else{
    				delIds.add(templateConfig.getId());
    			}

    			if(!all.contains(templateId)){
    				delIds.add(templateConfig.getId());
    			}
    		}
        }
        if(Strings.isNotEmpty(delIds)){
        	templateDao.deleteCtpTemplateConfig(delIds);
        }
        return allTemplateList;
      //===================================处理向模板配置表中插入数据========结束===============================
	}
	private List<CtpTemplateConfig> ctpTemplate2CtpTemplateConfig(List<CtpTemplate> template){
		List<CtpTemplateConfig> configList = new ArrayList<CtpTemplateConfig>();
		if(Strings.isEmpty(template)){
			return configList;
		}
		for(CtpTemplate tem:template){
			CtpTemplateConfig config = new CtpTemplateConfig();
			config.setIdIfNew();
			config.setMemberId(AppContext.currentUserId());
			config.setSort(tem.getSort());
			config.setTempleteId(tem.getId());
			config.setType(tem.getModuleType());
			config.setDelete(false);
			configList.add(config);
		}
		return configList;
	}

	@Override
	public List<CtpTemplate> getCtpTemplate(Map<String, String> params) throws BusinessException {
		StringBuilder sql = new StringBuilder();
		sql.append(" select DISTINCT a.id, a.categoryId, a.subject, a.memberId, a.orgAccountId, a.moduleType, a.system, ");
		sql.append("        a.moduleType, a.bodyType, a.belongOrg, a.publishTime, a.createDate ");
		sql.append("   from CtpTemplate a ");
//		CtpTemplateCategory category 没有发现查询所以删除
		
		//责任者
		if(Strings.isNotBlank(params.get("responsible")) || Strings.isNotBlank(params.get("c_responsible"))){
			sql.append(" , CtpTemplateOrg b ");
		}
		//审核者
		if(Strings.isNotBlank(params.get("auditor")) || Strings.isNotBlank(params.get("c_auditor"))){
			sql.append(" , CtpTemplateOrg c ");
		}
		//咨询者
		if(Strings.isNotBlank(params.get("consultant")) || Strings.isNotBlank(params.get("c_consultant"))){
			sql.append(" , CtpTemplateOrg d ");
		}
		//知会者
		if(Strings.isNotBlank(params.get("inform")) || Strings.isNotBlank(params.get("c_inform"))){
			sql.append(" , CtpTemplateOrg e ");
		}
//		sql.append("  where a.categoryId = category.id "); 没有发现分类表的查询字段 无需链接
		sql.append("  where  a.state = :state ");
		sql.append("    and a.system = 1 ");
		sql.append("    and a.type in (:type) ");
		sql.append("    and a.delete = 0 ");
		
		Map<String, Object> queryParams = new HashMap<String, Object>();
		queryParams.put("state", TemplateEnum.State.normal.ordinal());
		
		List<String> types = new ArrayList<String>();
		types.add(TemplateEnum.Type.template.name());
		types.add("templete");
		queryParams.put("type", types);
        //模板编号
        if(params.containsKey("templateNumber") ){
            sql.append(" and a.templeteNumber = :templeteNumber");
            queryParams.put("templeteNumber", params.get("templateNumber"));
        }
		//模板分类
		if(Strings.isNotBlank(params.get("categoryId"))){
			sql.append(" and a.categoryId in (:categoryIds) ");
			String categoryIds = String.valueOf(params.get("categoryId"));
			String[] categoryIdarr = categoryIds.split(",");
			List<Long> categoryIdlist = new ArrayList<Long>();
			for(String cateGory : categoryIdarr){
				categoryIdlist.add(Long.valueOf(cateGory));
			}
			queryParams.put("categoryIds", categoryIdlist);
		}
		
		//创建单位
		if(Strings.isNotBlank(params.get("createAccount"))){
			sql.append(" and a.orgAccountId in (:orgAccountId) ");
			String accounts = String.valueOf(params.get("createAccount"));
			String[] arrAccounts = accounts.split(",");
			List<Long> lAccount = new ArrayList<Long>();
			for(String sAccount : arrAccounts){
				lAccount.add(Long.valueOf(sAccount));
			}
			queryParams.put("orgAccountId", lAccount);
		}else if(AppContext.isAdministrator()){
			sql.append(" and a.orgAccountId = :orgAccountId ");
			queryParams.put("orgAccountId", AppContext.currentAccountId());
		}
		
		//模板名称
		if(Strings.isNotBlank(params.get("templateName"))){
			sql.append(" and a.subject like :templateName ");
			queryParams.put("templateName", "%" + SQLWildcardUtil.escape(params.get("templateName")) + "%");
		}
		if(Strings.isNotBlank(params.get("c_templateName"))){
			sql.append(" and a.subject like :c_templateName ");
			queryParams.put("c_templateName", "%" + SQLWildcardUtil.escape(params.get("c_templateName")) + "%");
		}
		
		//所属人
		if(Strings.isNotBlank(params.get("memberId"))){
			sql.append(" and a.memberId = :memberId ");
			queryParams.put("memberId", Long.valueOf(params.get("memberId")));
		}
		
		//流程类型
		if(Strings.isNotBlank(params.get("processType"))){
			String processType = params.get("processType");
			if("1".equals(processType)){ //协同
				//注意MainbodyType 的类型 协同这里需要查询 html 10 OfficeWord 41 OfficeExcel 42 WpsWord 43 WpsExcel 44 pdf 45
				sql.append(" and a.moduleType = 1 and a.bodyType in ('10','41','42','43','44','45') ");
			}else if("2".equals(processType)){ //表单
				sql.append(" and a.moduleType = 1 and a.bodyType = '20' ");
			}else if("4".equals(processType)){ //公文
				sql.append(" and a.moduleType in (:categoryId) ");
						
				List<Integer> categoryId = new ArrayList<Integer>();
				categoryId.add(Integer.valueOf(ModuleType.govdocRec.getKey()));
				categoryId.add(Integer.valueOf(ModuleType.govdocSend.getKey()));
				categoryId.add(Integer.valueOf(ModuleType.govdocSign.getKey()));
				queryParams.put("categoryId", categoryId);
			}
		}else{
			sql.append(" and a.moduleType in (:categoryId) ");
			
			List<Integer> categoryId = new ArrayList<Integer>();
			categoryId.add(Integer.valueOf(ModuleType.govdocRec.getKey()));
			categoryId.add(Integer.valueOf(ModuleType.govdocSend.getKey()));
			categoryId.add(Integer.valueOf(ModuleType.govdocSign.getKey()));
			categoryId.add(Integer.valueOf(ModuleType.collaboration.getKey()));
			queryParams.put("categoryId", categoryId);
		}
		
		//责任者
		if(Strings.isNotBlank(params.get("responsible"))){
			sql.append(" and a.id = b.templateId ");
			sql.append(" and b.orgName like :bOrgName ");
			queryParams.put("bOrgName", "%" + SQLWildcardUtil.escape(params.get("responsible")) + "%");
			sql.append(" and b.dataType = :bDataType ");
			queryParams.put("bDataType", TemplateEnum.DataType.R.ordinal());
		}
		if(Strings.isNotBlank(params.get("c_responsible"))){
			sql.append(" and a.id = b.templateId ");
			sql.append(" and b.orgName like :c_bOrgName ");
			queryParams.put("c_bOrgName", "%" + SQLWildcardUtil.escape(params.get("c_responsible")) + "%");
			sql.append(" and b.dataType = :c_bDataType ");
			queryParams.put("c_bDataType", TemplateEnum.DataType.R.ordinal());
		}
		//审核者
		if(Strings.isNotBlank(params.get("auditor"))){
			sql.append(" and a.id = c.templateId ");
			sql.append(" and c.orgName like :cOrgName ");
			queryParams.put("cOrgName", "%" + SQLWildcardUtil.escape(params.get("auditor")) + "%");
			sql.append(" and c.dataType = :cDataType ");
			queryParams.put("cDataType", TemplateEnum.DataType.A.ordinal());
		}
		if(Strings.isNotBlank(params.get("c_auditor"))){
			sql.append(" and a.id = c.templateId ");
			sql.append(" and c.orgName like :c_cOrgName ");
			queryParams.put("c_cOrgName", "%" + SQLWildcardUtil.escape(params.get("c_auditor")) + "%");
			sql.append(" and c.dataType = :c_cDataType ");
			queryParams.put("c_cDataType", TemplateEnum.DataType.A.ordinal());
		}
		//咨询者
		if(Strings.isNotBlank(params.get("consultant"))){
			sql.append(" and a.id = d.templateId ");
			sql.append(" and d.orgName like :dOrgName ");
			queryParams.put("dOrgName", "%" + SQLWildcardUtil.escape(params.get("consultant")) + "%");
			sql.append(" and d.dataType = :dDataType ");
			queryParams.put("dDataType", TemplateEnum.DataType.C.ordinal());
		}
		if(Strings.isNotBlank(params.get("c_consultant"))){
			sql.append(" and a.id = d.templateId ");
			sql.append(" and d.orgName like :c_dOrgName ");
			queryParams.put("c_dOrgName", "%" + SQLWildcardUtil.escape(params.get("c_consultant")) + "%");
			sql.append(" and d.dataType = :c_dDataType ");
			queryParams.put("c_dDataType", TemplateEnum.DataType.C.ordinal());
		}
		//知会者
		if(Strings.isNotBlank(params.get("inform"))){
			sql.append(" and a.id = e.templateId ");
			sql.append(" and e.orgName like :eOrgName ");
			queryParams.put("eOrgName", "%" + SQLWildcardUtil.escape(params.get("inform")) + "%");
			sql.append(" and e.dataType = :eDataType ");
			queryParams.put("eDataType", TemplateEnum.DataType.I.ordinal());
		}
		if(Strings.isNotBlank(params.get("c_inform"))){
			sql.append(" and a.id = e.templateId ");
			sql.append(" and e.orgName like :c_eOrgName ");
			queryParams.put("c_eOrgName", "%" + SQLWildcardUtil.escape(params.get("c_inform")) + "%");
			sql.append(" and e.dataType = :c_eDataType ");
			queryParams.put("c_eDataType", TemplateEnum.DataType.I.ordinal());
		}
		
		//归属机构
		if(Strings.isNotBlank(params.get("belongUnit"))){
			sql.append(" and a.belongOrg = :belongOrg ");
			queryParams.put("belongOrg", Long.valueOf(params.get("belongUnit")));
		}
		
		//发布时间
		if(Strings.isNotBlank(params.get("beginPublishTime"))){
			Date startDate = Datetimes.parseDatetime(Datetimes.getFirstTimeStr(params.get("beginPublishTime")));
			sql.append(" and a.publishTime >= :startDate ");
			queryParams.put("startDate", startDate);
		}
		if(Strings.isNotBlank(params.get("endPublishTime"))){
			Date endDate = Datetimes.parseDatetime(Datetimes.getLastTimeStr(params.get("endPublishTime")));
			sql.append(" and a.publishTime <= :endDate ");
			queryParams.put("endDate", endDate);
		}

		List<Object[]> list = new ArrayList<Object[]>();
		if(Strings.isNotBlank(params.get("templateIds"))){
			String[] arrTemplateIds = params.get("templateIds").split(",");
			List<Long> templateIds = new ArrayList<Long>();
			for(String id : arrTemplateIds){
				templateIds.add(Long.valueOf(id));
			}
			List<Long>[] ids = Strings.splitList(templateIds, 999);
			for(int i = 0 ; i < ids.length ; i++){
				sql.append(" and a.id in (:templateIds) ");
				queryParams.put("templateIds", ids[i]);
				list.addAll(DBAgent.find(sql.toString(), queryParams));
			}
		}else{
			list.addAll(DBAgent.find(sql.toString(), queryParams));
		}
		
		List<CtpTemplate> result = new ArrayList<CtpTemplate>();

		for (Object[] o : list) {
            CtpTemplate template = new CtpTemplate();
            int n = 0;
            template.setId((Long) o[n++]);
            template.setCategoryId((Long) o[n++]);
            template.setSubject((String) o[n++]);
            template.setMemberId((Long) o[n++]);
            template.setOrgAccountId((Long) o[n++]);
            template.setModuleType((Integer) o[n++]);
            template.setSystem((Boolean) o[n++]);
            template.setModuleType((Integer) o[n++]);
            template.setBodyType((String) o[n++]);
            template.setBelongOrg((Long) o[n++]);
            template.setPublishTime((Date) o[n++]);
            template.setCreateDate((Date) o[n++]);
            
            result.add(template);
        }
        return result;
	}

    @AjaxAccess
    public String delFlowTemplate(List<String> ids,String defId,String cap4Flag) throws BusinessException {
    	if(Strings.isNotBlank(cap4Flag) && "1".equals(cap4Flag)){
    		com.seeyon.cap4.form.bean.FormBean cap4fb = formApi4Cap4.checkAndLoadForm2Session(Long.valueOf(defId), "");
    		for (String id : ids) {
    			CtpTemplate template = cap4fb.getBind().getFlowTemplate(Long.valueOf(id));
    			long oldProcessId = template.getExtraAttr("oldProId") == null ? template.getWorkflowId() : Long.parseLong(template.getExtraAttr("oldProId").toString());
    			long batchId = Long.parseLong(cap4fb.getBind().getExtraAttr("batchId").toString());
    			wapi.deleteWorkflowTemplate(batchId, oldProcessId);
    			cap4fb.getBind().removeFlowTemplate(Long.parseLong(id));
    		}
    		return "success";
    	}else{
    		FormBean fb = capFormManager.getEditingForm();
    		for (String id : ids) {
    			CtpTemplate template = fb.getBind().getFlowTemplate(Long.valueOf(id));
    			long oldProcessId = template.getExtraAttr("oldProId") == null ? template.getWorkflowId() : Long.parseLong(template.getExtraAttr("oldProId").toString());
    			long batchId = Long.parseLong(fb.getBind().getExtraAttr("batchId").toString());
    			wapi.deleteWorkflowTemplate(batchId, oldProcessId);
    			fb.getBind().removeFlowTemplate(Long.parseLong(id));
    		}
    		return "success";
    	}
    }
	
    

	
	@AjaxAccess
	public void recordDownLoadProcessInsApplog(String templateName,String moduleType){
		int category = 109;
		if("4".equals(moduleType) || "19".equals(moduleType) || "20".equals(moduleType) || "21".equals(moduleType)){
			category = 320;
		}
		appLogManager.insertLog(AppContext.getCurrentUser(), category, AppContext.currentUserName(),templateName);
	}
	

	
	/**
     * @param map
     * @param template
     * @param isNew
     * @param sendMessage
     * @throws BusinessException
     */
    private void savSupervise(Map map, CtpTemplate template, boolean isNew, boolean sendMessage) throws BusinessException {
        String supervisorId = (String) map.get("supervisorIds");
        String supervisors = (String) map.get("supervisorNames");
        Long templateDateTerminal = 0L;
        if (Strings.isNotBlank((String) map.get("templateDateTerminal"))) {
            templateDateTerminal = ParamUtil.getLong(map, "templateDateTerminal", 0L);
        }
        String role = (String) map.get("role");
        if (Strings.isBlank(supervisors) && Strings.isBlank(role)) {
            superviseManager.deleteAllInfoByTemplateId(template.getId());
            return;
        }
        String superviseTitle = (String) map.get("title");
        SuperviseSetVO ssvo = new SuperviseSetVO();
        ssvo.setTitle(superviseTitle);
        ssvo.setSupervisorIds(supervisorId);
        ssvo.setSupervisorNames(supervisors);
        ssvo.setTemplateDateTerminal(templateDateTerminal);
        ssvo.setRole(role);
        //detailid没有实际意义，这里只是用来在接口中判断是保存还是更新
        if (isNew) {
            ssvo.setDetailId(null);
        } else {
            ssvo.setDetailId(1L);
        }
        superviseManager.saveOrUpdateSupervise4Template(template.getId(), ssvo);
    }
	
	@AjaxAccess
	public String checkSameTemplateName(String defId,String bindId, String name,String cap4Flag) throws BusinessException {
		String bindName = name.trim();
		Long bindIdLong = Strings.isEmpty(bindId) ? -1 : Long.valueOf(bindId);
		if(Strings.isNotBlank(cap4Flag) && "1".equals(cap4Flag)){
			com.seeyon.cap4.form.bean.FormBean cap4fb = formApi4Cap4.checkAndLoadForm2Session(Long.valueOf(defId),"");
			for (CtpTemplate c : cap4fb.getBind().getFlowTemplateList()) {
				if (!c.getId().equals(bindIdLong) && c.getSubject().equals(bindName)) {
					return "false";
				}
			}
		}else{
			FormBean fb = capFormManager.getEditingForm();
			for (CtpTemplate c : fb.getBind().getFlowTemplateList()) {
				if (!c.getId().equals(bindIdLong) && c.getSubject().equals(bindName)) {
					return "false";
				}
			}
		}
        List<CtpTemplate> cList = getCtpTemplates(null, bindName, null, null);
        if (Strings.isNotEmpty(cList)) {
            List<CtpTemplate> templateList = formApi4Cap3.getFormSystemTemplate(Long.valueOf(defId));
            for (CtpTemplate c : cList) {
                //这里居然能查出什么HTML模版
                if (!c.getId().equals(bindIdLong) && !c.isDelete() && c.isSystem() && String.valueOf(MainbodyType.FORM.getKey()).equals(c.getBodyType())) {
                    boolean isSame = true;
                    for (CtpTemplate ct : templateList) {
                        if (ct.getId().longValue() == c.getId().longValue()) {
                            isSame = false;
                            break;
                        }
                    }
                    if (isSame) {
                        return "false";
                    }
                }

            }
        }
        return "success";
    }
	
	@AjaxAccess
	public String checkSameCode(String defId,String templateId, String number,String cap4Flag) throws BusinessException {
        number = number.trim();
        if(Strings.isNotBlank(cap4Flag) && "1".equals(cap4Flag)){
        	 if (number != null && Strings.isNotBlank(number)) {
        		 com.seeyon.cap4.form.bean.FormBean cap4fb = formApi4Cap4.checkAndLoadForm2Session(Long.valueOf(defId),"");
                 List<CtpTemplate> ctList = cap4fb.getBind().getFlowTemplateList();//只有在新建 修改应用绑定的时候才验证 所以从缓存查询出来
                 Set<Long> temIds = new HashSet<Long>();
                 if (ctList != null) {
                     for (CtpTemplate ct : ctList) {
                         temIds.add(ct.getId());
                         if (number.equals(ct.getTempleteNumber()) && (Strings.isNotBlank(templateId) && !templateId.equals("" + ct.getId()))) {//有相同的编号 并且id不一样（不是同一个模板）
                             return "false";
                         }
                     }
                 }
                 CtpTemplate ct = getTempleteByTemplateNumber(number);
                 com.seeyon.cap4.form.bean.FormBean temp = formApi4Cap4.getFormByFormCode(ct);
                 if (ct != null && !temIds.contains(ct.getId()) && !cap4fb.equals(temp)) {//其他表单模板 有同样编号
                     return "false";
                 }
                 List<com.seeyon.cap4.form.bean.FormBean> fList = formApi4Cap4.getForms();
                 for (com.seeyon.cap4.form.bean.FormBean f : fList) {
                     if (f.getBind().getFormCode().equals(number)) {
                         return "false";
                     }
                 }
             
             }
        }else{
        	if (number != null && Strings.isNotBlank(number)) {
                List<CtpTemplate> ctList = null ;
                FormBean fb = capFormManager.getEditingForm();
                if(fb != null){
                    ctList = fb.getBind().getFlowTemplateList();//只有在新建 修改应用绑定的时候才验证 所以从缓存查询出来
                }else{
                    //查询当前是否存在相同模板编号的对象
                    Map<String, String> templateNumberParam = new HashMap<String, String>();
                    templateNumberParam.put("templateNumber", number);
                    ctList =  getCtpTemplate(templateNumberParam);
                    if(ctList.size() >0){
                        return "false";
                    }
                }
        		Set<Long> temIds = new HashSet<Long>();
        		if (ctList != null) {
        			for (CtpTemplate ct : ctList) {
        				temIds.add(ct.getId());
        				if (number.equals(ct.getTempleteNumber()) && (Strings.isNotBlank(templateId) && !templateId.equals("" + ct.getId()))) {//有相同的编号 并且id不一样（不是同一个模板）
        					return "false";
        				}
        			}
        		}
        		CtpTemplate ct = getTempleteByTemplateNumber(number);
        		FormBean temp = formApi4Cap3.getFormByFormCode(ct);
        		if(fb != null){
                    if (ct != null && !temIds.contains(ct.getId()) && !fb.equals(temp)) {//其他表单模板 有同样编号
                        return "false";
                    }
                }else{
                    if (ct != null && !temIds.contains(ct.getId())) {//其他表单模板 有同样编号
                        return "false";
                    }
                }
        		List<FormBean> fList = formApi4Cap3.getForms();
        		for (FormBean f : fList) {
        			if (f.getBind().getFormCode().equals(number)) {
        				return "false";
        			}
        		}
        		
        	}
        }
        return "success";
    }
	
	
	/**
     * 表单模板自动发起-定时任务生成
     * @throws BusinessException
     */
    private void newFormBindQuartzJob(Long formId, CtpTemplate template) throws BusinessException {
        if (template == null) {
            return;
        }

        String formBindJobName = FormConstant.FORMBIND_JOBNAME + template.getId();
        //if (QuartzHolder.hasQuartzJob(FormBindQuartzJob.FORMBIND_GROUPNAME, formBindJobName)) {
            QuartzHolder.deleteQuartzJobByGroupAndJobName(FormConstant.FORMBIND_GROUPNAME, formBindJobName);
        //}

        ColSummary summary = null;
        if (template.getSummary() != null) {
            summary = XMLCoder.decoder(template.getSummary(),ColSummary.class);
        }

        if (template.isDelete() || summary == null) {
            return;
        }

        String cycleState = String.valueOf(summary.getExtraAttr("cycleState"));
        if (!"1".equals(cycleState)) {//自动发起：无
            return;
        }

        Map<String, String> parameters = new HashMap<String, String>();
        parameters.put("formId", String.valueOf(formId));
        parameters.put("templateId", String.valueOf(template.getId()));
        parameters.put("cycleSender", String.valueOf(summary.getExtraAttr("cycleSender")));

        String cycleStartDate = String.valueOf(summary.getExtraAttr("cycleStartDate"));
        String cycleEndDate = String.valueOf(summary.getExtraAttr("cycleEndDate"));
        String cycleType = String.valueOf(summary.getExtraAttr("cycleType"));
        String cycleMonth = String.valueOf(summary.getExtraAttr("cycleMonth"));
        String cycleOrder = String.valueOf(summary.getExtraAttr("cycleOrder"));
        String cycleDay = String.valueOf(summary.getExtraAttr("cycleDay"));
        String cycleWeek = String.valueOf(summary.getExtraAttr("cycleWeek"));
        String cycleHour = String.valueOf(summary.getExtraAttr("cycleHour"));

        //BUG_紧急_V5_V5.6SP1_宁波江北威信软件有限公司_（恩莱）表单制作保存的时候报错：出现异常...._20160331018605
        int month = Strings.isNotBlank(cycleMonth) ? Integer.parseInt(cycleMonth) : 0;
        int day = Strings.isNotBlank(cycleDay) ? Integer.parseInt(cycleDay) : 0;
        int week = Strings.isNotBlank(cycleWeek) ? Integer.parseInt(cycleWeek) : 0;

        HourEnum hourEnum = HourEnum.valueOf(Strings.isNotBlank(cycleHour) ? Integer.parseInt(cycleHour) : 0);

        Date currentTime = new Date();
        Date beginTime = Datetimes.parseDatetimeWithoutSecond(Datetimes.formatDate(new Date()) + " " + hourEnum.getText());
        if (!StringUtil.checkNull(cycleStartDate)) {
            Date selectTime = Datetimes.parseDatetimeWithoutSecond(cycleStartDate + " " + hourEnum.getText());
            if (selectTime.compareTo(currentTime) >= 0) {//开始时间小于当前时间，从当天生成定时任务
                beginTime = selectTime;
            }
        }
        Date endTime = null;
        if (!StringUtil.checkNull(cycleEndDate)) {
            endTime = Datetimes.parseDatetimeWithoutSecond(cycleEndDate + " " + hourEnum.getText());
            if (endTime.compareTo(currentTime) < 0) {//结束时间小于当前时间，不生成定时任务
                return;
            }
        }

        if (CycleEnum.DAY.getValue().equals(cycleType)) {//按天
            if (beginTime.compareTo(currentTime) < 0) {//开始时间小于当前时间，取下次
                beginTime = Datetimes.addDate(beginTime, 1);
            }

            if (endTime != null && endTime.compareTo(beginTime) < 0) {//结束时间小于开始时间，不生成定时任务
                return;
            }

            QuartzHolder.newQuartzJobPerDay(FormConstant.FORMBIND_GROUPNAME, formBindJobName, beginTime, endTime, FormConstant.FORMBIND_JOBBEANID, parameters);
        } else if (CycleEnum.WEEK.getValue().equals(cycleType)) {//按周
            Date firstDayInWeek = Datetimes.parseDatetimeWithoutSecond(Datetimes.formatDate(Datetimes.getFirstDayInWeek(beginTime)) + " " + hourEnum.getText());
            beginTime = Datetimes.addDate(firstDayInWeek, week);

            if (beginTime.compareTo(currentTime) < 0) {//开始时间小于当前时间，取下次
                beginTime = Datetimes.addDate(beginTime, 7);
            }

            if (endTime != null && endTime.compareTo(beginTime) < 0) {//结束时间小于开始时间，不生成定时任务
                return;
            }

            QuartzHolder.newQuartzJobPerWeek(FormConstant.FORMBIND_GROUPNAME, formBindJobName, beginTime, endTime, FormConstant.FORMBIND_JOBBEANID, parameters);
        } else if (CycleEnum.MONTH.getValue().equals(cycleType)) {//按月
            Date firstDayInMonth = Datetimes.parseDatetimeWithoutSecond(Datetimes.formatDate(Datetimes.getFirstDayInMonth(beginTime)) + " " + hourEnum.getText());
            Date lastDayInMonth = Datetimes.parseDatetimeWithoutSecond(Datetimes.formatDate(Datetimes.getLastDayInMonth(beginTime)) + " " + hourEnum.getText());
            if ("0".equals(cycleOrder)) {//正数
                beginTime = Datetimes.addDate(firstDayInMonth, day - 1);
            } else if ("1".equals(cycleOrder)) {//倒数
                beginTime = Datetimes.addDate(lastDayInMonth, 1 - day);
            }

            if (beginTime.compareTo(currentTime) < 0) {//开始时间小于当前时间，取下次
                beginTime = Datetimes.addMonth(beginTime, 1);
            }

            if (endTime != null && endTime.compareTo(beginTime) < 0) {//结束时间小于开始时间，不生成定时任务
                return;
            }

            //倒数的时候重新组装任务的时间参数
            if("1".equals(cycleOrder)){
                GregorianCalendar gc = new GregorianCalendar();
                gc.setTime(beginTime);
                int SECOND = gc.get(GregorianCalendar.SECOND);
                int MINUTE = gc.get(GregorianCalendar.MINUTE);
                int HOUR_OF_DAY = gc.get(GregorianCalendar.HOUR_OF_DAY);
                String cronExpression = SECOND + " " + MINUTE + " " + HOUR_OF_DAY + " " + day + "L * ?";
                QuartzHolder.newCronQuartzJob(FormConstant.FORMBIND_GROUPNAME, formBindJobName, cronExpression, beginTime, endTime, FormConstant.FORMBIND_JOBBEANID, parameters);
            }else{
                QuartzHolder.newQuartzJobPerMonth(FormConstant.FORMBIND_GROUPNAME, formBindJobName, beginTime, endTime, FormConstant.FORMBIND_JOBBEANID, parameters);
            }
        } else if (CycleEnum.YEAR.getValue().equals(cycleType)) {//按年
            Date firstDayInYear = Datetimes.parseDatetimeWithoutSecond(Datetimes.formatDate(Datetimes.getFirstDayInYear(beginTime)) + " " + hourEnum.getText());
            beginTime = Datetimes.addMonth(firstDayInYear, month - 1);
            Date firstDayInMonth = Datetimes.parseDatetimeWithoutSecond(Datetimes.formatDate(Datetimes.getFirstDayInMonth(beginTime)) + " " + hourEnum.getText());
            Date lastDayInMonth = Datetimes.parseDatetimeWithoutSecond(Datetimes.formatDate(Datetimes.getLastDayInMonth(beginTime)) + " " + hourEnum.getText());
            if ("0".equals(cycleOrder)) {//正数
                beginTime = Datetimes.addDate(firstDayInMonth, day - 1);
            } else if ("1".equals(cycleOrder)) {//倒数
                beginTime = Datetimes.addDate(lastDayInMonth, 1 - day);
            }

            if (beginTime.compareTo(currentTime) < 0) {//开始时间小于当前时间，取下次
                beginTime = Datetimes.addYear(beginTime, 1);
            }

            if (endTime != null && endTime.compareTo(beginTime) < 0) {//结束时间小于开始时间，不生成定时任务
                return;
            }

            //倒数的时候重新组装任务的时间参数
            if("1".equals(cycleOrder)){
                GregorianCalendar gc = new GregorianCalendar();
                gc.setTime(beginTime);
                int SECOND = gc.get(GregorianCalendar.SECOND);
                int MINUTE = gc.get(GregorianCalendar.MINUTE);
                int HOUR_OF_DAY = gc.get(GregorianCalendar.HOUR_OF_DAY);
                String cronExpression = SECOND + " " + MINUTE + " " + HOUR_OF_DAY + " " + day + "L * ?";
                QuartzHolder.newCronQuartzJob(FormConstant.FORMBIND_GROUPNAME, formBindJobName, cronExpression, beginTime, endTime, FormConstant.FORMBIND_JOBBEANID, parameters);
            }else {
                QuartzHolder.newQuartzJobPerYear(FormConstant.FORMBIND_GROUPNAME, formBindJobName, beginTime, endTime, FormConstant.FORMBIND_JOBBEANID, parameters);
            }
        }
       LOG.info("表单模板自动发起-定时任务开始时间：" + cycleType + "，" + Datetimes.formatDatetime(beginTime));
    }
	
	private  void updatePermissinRef(ModuleType type, String processXml, String processId,String processTemplateId,Long accountId) throws BusinessException {
        //更新节点权限引用状态
        String configCategory = EnumNameEnum.col_flow_perm_policy.name();
        List<String> list = wapi.getWorkflowUsedPolicyIds(type.name(), processXml, processId, processTemplateId);
        if (list != null && list.size() > 0) {
            for (int i = 0; i < list.size(); i++) {
                permissionManager.updatePermissionRef(configCategory, list.get(i), accountId);
            }
        }
    }
	
	/**
     * @param template
     * @param isNew
     * @throws BusinessException
     */
    private void saveOrUpdateCptContentAll(CtpTemplate template, boolean isNew, Long formbeanID) throws BusinessException {
        CtpContentAll content = null;
        if (isNew) {
            content = new CtpContentAll();
            content.setId(UUIDLong.longUUID());
            content.setCreateId(AppContext.currentUserId());
            content.setCreateDate(new Date());
            content.setContentTemplateId(formbeanID);
            content.setContentType(MainbodyType.FORM.getKey());//找不到枚举值了。。
            content.setModuleId(template.getId());
            content.setModuleType(template.getModuleType());
            content.setModuleTemplateId(-1L);
            content.setSort(1);
        } else {
            List<CtpContentAll> contentList = MainbodyService.getInstance().getContentList(ModuleType.getEnumByKey(template.getModuleType()), template.getId());
            if (contentList == null || contentList.size() < 1) {
                throw new BusinessException(template.getSubject() + ":修改时找不到正文组件内容!");
            }
            content = contentList.get(0);
        }
        template.setBody(content.getId());
        content.setTitle(template.getSubject());
        content.setModifyDate(new Date());
        content.setModifyId(AppContext.currentUserId());
        MainbodyService.getInstance().saveOrUpdateContentAll(content);
    }
	
	@Override
	public List<CtpTemplateCategory> getCtpTemplateListByCategory(Map<String, String> params) throws BusinessException {
		
		List<CtpTemplateCategory> categories = new ArrayList<CtpTemplateCategory>();
		
		//单位管理员
		if(AppContext.isAdministrator()){
			categories = templateCategoryManager.getTemplateCategorys(AppContext.currentAccountId());
			if(Strings.isBlank(params.get("inculdeEdoc")) || !"true".equals(params.get("inculdeEdoc"))){
				categories.add(templateCategoryManager.get(Long.valueOf(ModuleType.govdocRec.getKey())));
				categories.add(templateCategoryManager.get(Long.valueOf(ModuleType.govdocSend.getKey())));
				categories.add(templateCategoryManager.get(Long.valueOf(ModuleType.govdocSign.getKey())));
			}
		}else{
			List<CtpTemplateCategory> allCategories = templateCategoryManager.getAllCategories();
			List<Long> categoryId = new ArrayList<Long>();
			if(Strings.isBlank(params.get("inculdeEdoc")) || !"true".equals(params.get("inculdeEdoc"))){
				categoryId.add(Long.valueOf(ModuleType.govdocRec.getKey()));
				categoryId.add(Long.valueOf(ModuleType.govdocSend.getKey()));
				categoryId.add(Long.valueOf(ModuleType.govdocSign.getKey()));
			}
			for (CtpTemplateCategory category : allCategories) {
				if (category.getOrgAccountId() != null) {
					categories.add(category);
				} else if (categoryId.contains(category.getId())) {
					categories.add(category);
				}
			}
		}
		
        return categories;
	}

	@Override
	public FlipInfo findTemplateList4Clone(FlipInfo flipInfo, Map<String, String> params) throws BusinessException {
		String appName = ParamUtil.getString(params, "appName", ApplicationCategoryEnum.collaboration.name());
		Long formAppId = ParamUtil.getLong(params, "formAppId");
		int sourceType = ParamUtil.getInt(params, "sourceType");
		

		List<Map<String,String>> resultMap = new ArrayList<Map<String,String>>();
		if(sourceType==0){
			boolean isForm = formAppId!=null && !Long.valueOf(-1).equals(formAppId);
			if(isForm && ApplicationCategoryEnum.collaboration.name().equals(appName)){
				
				findFormTemplateList4Clone(flipInfo, params);	
				
			}else if( ApplicationCategoryEnum.collaboration.name().equals(appName) 
					|| ApplicationCategoryEnum.edoc.name().equals(appName)){
				
				findCollaborationOrEdocTemplateList4Clone(flipInfo, params);
				
			}
		}else if(sourceType==1 ){
			findTemplateHistoryList4Clone(flipInfo, params);
			
		}
		return flipInfo;
	}
	
	
	
	/**
	 * 
	 * @Title: findTemplateHistoryList4Clone   
	 * @Description: 查询复制流程需要的版本数据
	 * @param flipInfo
	 * @param params
	 * @return
	 * @throws BusinessException      
	 * @return: FlipInfo  
	 * @date:   2018年11月24日 下午5:35:17
	 * @author: xusx
	 * @since   V7.1	       
	 * @throws
	 */
	private FlipInfo findTemplateHistoryList4Clone(FlipInfo flipInfo, Map<String, String> params) throws BusinessException{
		Long formAppId = ParamUtil.getLong(params, "formAppId");
		Long currentProcessId = ParamUtil.getLong(params, "currentProcessId");
		String formName = ParamUtil.getString(params, "formname");
		String category = ParamUtil.getString(params, "category");
		//获取当前模板id
		Long currentTemplateId = null;
		com.seeyon.cap4.form.bean.FormBean cap4fb = null;
		if(currentProcessId!=null){
			CtpTemplate  ctpTemplate = null;
			//先从缓存中获取当前模板如果没有获取到再从数据库中获取
			if( formAppId!=null){
				cap4fb = formApi4Cap4.getEditingForm(formAppId);
				List<CtpTemplate> templates = new ArrayList<CtpTemplate>();
				if(cap4fb!=null){
					templates = cap4fb.getBind().getFlowTemplateList();
				}
				if(Strings.isNotEmpty(templates)){
					for (CtpTemplate tempalte:templates){
						if(currentProcessId.equals(tempalte.getWorkflowId())){
							ctpTemplate = tempalte;
							break;
						}
					}
				}
				
			}
			if(ctpTemplate==null){
				ctpTemplate = templateDao.getCtpTemplateByWorkFlowId(currentProcessId);
			}
			if(ctpTemplate!=null){
				currentTemplateId = ctpTemplate.getId();
			}
		}
		if(currentTemplateId==null || cap4fb==null){
			return flipInfo;
		}
		if((Strings.isNotBlank(formName) && !cap4fb.getFormName().contains(formName)) 
			|| (Strings.isNotBlank(category) && !String.valueOf(cap4fb.getCategoryId()).equals(category))){
			return flipInfo;
		}
		
		params.put("templateId", String.valueOf(currentTemplateId));

		params.put("deleteTemp", "0");
		List<CtpTemplateHistory> templateHistorys = templateDao.getCtpTemplateHistory(flipInfo, params);
		if(Strings.isNotEmpty(templateHistorys)){
			CAPFormBean formBean  = capFormManager.getForm(formAppId);
			
			List<Map<String,Object>> templateHistoryList = new ArrayList<Map<String,Object>>();
			for (CtpTemplateHistory history : templateHistorys) {
				if(currentProcessId.equals(history.getWorkflowId())){
					continue;
				}
				Map<String,Object> map = new HashMap<String,Object>();
				map.put("workflowId", history.getWorkflowId());
				map.put("templateName", history.getSubject());
				map.put("modifyTime", history.getModifyDate());
				map.put("type", history.getType());
				map.put("version", "V"+history.getVersion()+".0");
				V3xOrgMember orgMember = orgManager.getMemberById(cap4fb.getOwnerId());
				if(null!=orgMember){
					map.put("owner", orgMember.getName());
				}else{
					map.put("owner", "");
				}
				
				if(formBean!=null){
					map.put("formName", cap4fb.getFormName());
					String categoryStr = "";
					CtpTemplateCategory  ctc = this.getCtpTemplateCategory(cap4fb.getCategoryId());
					if (ctc != null) {
						String categoryName = ctc.getName();
						String i18nName = ResourceUtil.getString(categoryName);
						categoryStr = Strings.isNotBlank(i18nName) ? i18nName : categoryName;
					}
					map.put("category", categoryStr);
				}
				
				templateHistoryList.add(map);
			}
			
			DBAgent.memoryPaging(templateHistoryList, flipInfo);
		}
		return flipInfo;
	}
	
	/**
	 * 
	 * @Title: findCollaborationOrEdocTemplateList4Clone   
	 * @Description: 查询协同和公文复制流程需要的模板数据
	 * @param flipInfo
	 * @param params
	 * @return
	 * @throws BusinessException      
	 * @return: FlipInfo  
	 * @date:   2018年11月24日 下午5:33:16
	 * @author: xusx
	 * @since   V7.1	       
	 * @throws
	 */
	private FlipInfo findCollaborationOrEdocTemplateList4Clone(FlipInfo flipInfo, Map<String, String> params) throws BusinessException{
		String appName = ParamUtil.getString(params, "appName", ApplicationCategoryEnum.collaboration.name());
		Long formAppId = ParamUtil.getLong(params, "formAppId");
		Integer formType = null;
		if(ApplicationCategoryEnum.edoc.name().equals(appName)){
			
			FormBean editFormBean = formApi4Cap3.getEditingAndCacheForm(formAppId);
			if(editFormBean!=null) {
				formType = editFormBean.getGovDocFormType();
			}
			
			if(Integer.valueOf(FormType.govDocSendForm.getKey()).equals(formType)){
				params.put("categoryType", String.valueOf(ApplicationCategoryEnum.govdocSend.getKey()));
				
			}else if(Integer.valueOf(FormType.govDocReceiveForm.getKey()).equals( formType)){
				params.put("categoryType", String.valueOf(ApplicationCategoryEnum.govdocRec.getKey()));
				
			}else if(Integer.valueOf(FormType.govDocSignForm.getKey()).equals( formType)){
				params.put("categoryType", String.valueOf(ApplicationCategoryEnum.govdocSign.getKey()));
				
			}else if(Integer.valueOf(FormType.govDocExchangeForm.getKey()).equals( formType)){
				params.put("categoryType", String.valueOf(ApplicationCategoryEnum.govdocExchange.getKey()));
			}
		}else{
			params.put("categoryType", String.valueOf(ApplicationCategoryEnum.valueOf(appName).getKey()));
		}
		params.put("onlyFlowTemplate","true");
		this.selectTempletesForCopy(flipInfo, params);
		
		List<TemplateBO> templateBos = flipInfo.getData();
		
		if(Strings.isNotEmpty(templateBos)){
			List<Map<String,Object>> maps = new ArrayList<Map<String,Object>>();
			for(TemplateBO templateBO : templateBos) {
				Map<String,Object> map = new HashMap<String,Object>();
				map.put("workflowId", templateBO.getWorkflowId());
				map.put("templateName", templateBO.getSubject());
				map.put("modifyTime", templateBO.getModifyDate());
				
				if(ApplicationCategoryEnum.edoc.name().equals(appName)){
					if(null==formType){
						map.put("type", TemplateTypeEnums.templete.name());
					}else{
						if(FormType.govDocSendForm.getKey() == formType){
							map.put("type", TemplateTypeEnums.edoc_send.name());
						}else if(FormType.govDocReceiveForm.getKey() == formType){
							map.put("type", TemplateTypeEnums.edoc_rec.name());
						}else if(FormType.govDocSignForm.getKey() == formType){
							map.put("type", TemplateTypeEnums.edoc_sign.name());
						}
					}
				}else if(ApplicationCategoryEnum.collaboration.name().equals(appName)){
					map.put("type", TemplateTypeEnums.template.name());
				}else if(ApplicationCategoryEnum.info.name().equals(appName)){
					map.put("type", TemplateTypeEnums.info.name());
				}
				
				map.put("owner", templateBO.getCreaterName());
				maps.add(map);
			}
			
			flipInfo.setData(maps);
		}
		
		return flipInfo;
	}
	private Map<String, String>  getCtpCategoriesByAuthForCopy(Map<String, String> params, List<Long> allAccountIds, Long memberId)
			throws BusinessException {
        List<ModuleType> _treeModuleTypes = new ArrayList<ModuleType>();
        ModuleType _treeModuleType = ModuleType.collaboration;
        if(Strings.isBlank(params.get("categoryId"))){
        	if(params.get("categoryType")!=null){
        		String _c = params.get("categoryType");
        		_treeModuleType = ModuleType.getEnumByKey(Integer.valueOf(_c == null ? "1" : _c));
        		_treeModuleTypes.add(_treeModuleType);
        	}
        	List<CtpTemplateCategory> categorys = getCategorysByAuthForCopy(allAccountIds,_treeModuleTypes, memberId);
        	List<Long> cid = new ArrayList<Long>();
        	for(CtpTemplateCategory c : categorys){
        		cid.add(c.getId());
        	}
        	if(Strings.isNotEmpty(cid)){
        		params.put("categoryId", Strings.join(cid, ","));
        	}
        }
        return params;
	}
	
	
    public List<CtpTemplateCategory> getCategorysByAuthForCopy(List<Long> allAccountIds, List<ModuleType> types, Long memberId) throws BusinessException{
    	if(allAccountIds == null || allAccountIds.isEmpty() || types == null){
    		return new ArrayList<CtpTemplateCategory>();
    	}
    	List<ModuleType> _type = new ArrayList<ModuleType>();
    	for(ModuleType t : types){
    		_type.add(t);
    	}
    	
    	List<CtpTemplateCategory> templateCategorys = this.getCategorysForCopy(allAccountIds,_type);
       
    	return checkCategoryAuthForCopy(allAccountIds, templateCategorys, memberId);
    }
    
    public CtpTemplateCategory findRootParent(CtpTemplateCategory ctpTemplateCategory) throws BusinessException{
        if (ctpTemplateCategory == null)
            return null;
        if (ctpTemplateCategory.getParentId() == null
                || (ctpTemplateCategory.getParentId() > 0 && ctpTemplateCategory.getParentId() < 100)) {
            return ctpTemplateCategory;
        }
        return findRootParent(this.getCtpTemplateCategory(ctpTemplateCategory.getParentId()));
    }

    
    /**
     * @param accountId
     * @param templateCategorys
     * @return 检查模板类型的是否授权
     * @throws BusinessException
     */
    private List<CtpTemplateCategory> checkCategoryAuthForCopy(List<Long> allAccountIds, List<CtpTemplateCategory> templateCategorys, Long memberId)
            throws BusinessException {
        List<CtpTemplateCategory> result = new ArrayList<CtpTemplateCategory>();
        if (templateCategorys != null) {
        	boolean isAdministrator= false;
        	for (Long accountId : allAccountIds) {
        		isAdministrator= orgManager.isAdministratorById(memberId, accountId);
        		if(isAdministrator){
        			break;
        		}
			}
        	Map<String,Boolean> isTemplateCategoryManagerMap= new HashMap<String, Boolean>();
        	if (!isAdministrator){
	        	for (CtpTemplateCategory ctpTemplateCategory : templateCategorys) {
	        		if (ctpTemplateCategory.isDelete() == null || !ctpTemplateCategory.isDelete()) {
	        			for (Long accountId : allAccountIds) {
		        			String key= memberId+"_"+accountId+"_"+ctpTemplateCategory;
		        			if(null==isTemplateCategoryManagerMap.get(key) || !isTemplateCategoryManagerMap.get(key)){
		        				boolean isTemplateCategoryManager= this.isTemplateCategoryManager(memberId, accountId,ctpTemplateCategory);
		        				if(!isTemplateCategoryManager){
		        					isTemplateCategoryManager= this.isTemplateCategoryManager(memberId, accountId,findRootParent(ctpTemplateCategory));
		        				}
		        				isTemplateCategoryManagerMap.put(key, isTemplateCategoryManager);
		        			}
	        			}
	        		}
	        	}
        	}
            CtpTemplateCategory temp = null;
            for (CtpTemplateCategory ctpTemplateCategory : templateCategorys) {
                if (ctpTemplateCategory.isDelete() == null || !ctpTemplateCategory.isDelete()) {
                	for (Long accountId : allAccountIds) {
                		String key= memberId+"_"+accountId+"_"+ctpTemplateCategory;
                		boolean isTemplateCategoryManager= isTemplateCategoryManagerMap.get(key)==null?false:isTemplateCategoryManagerMap.get(key);
                		// 单位管理员可访问所有
                		if (isAdministrator || isTemplateCategoryManager){
                			try {
                				// 返回clone对象
                				temp = (CtpTemplateCategory) ctpTemplateCategory.clone();
                				temp.setId(ctpTemplateCategory.getId());
                				result.add(temp);
                			} catch (CloneNotSupportedException e) {
                				LOG.error("", e);
                			}
                		}
                	}
                }
            }
        }
        return result;
    }
    
	public List<CtpTemplate> selectAllSystemTempletes(FlipInfo flipInfo,
			Map<String, String> params, Long accountId)   {
				return templateDao.selectAllSystemTempletes(flipInfo, params, accountId);
	}
	
	@SuppressWarnings("unchecked")
    private FlipInfo selectTempletesForCopy(FlipInfo flipInfo, Map<String, String> params) throws BusinessException {
        
        User user = AppContext.getCurrentUser();
        
        params.put("delete", "false");
        if (params.get("subject") != null) {
            params.put("subject", CtpTemplateUtil.unescape(params.get("subject")));
        }
        if (params.get("member") != null) {
            List<V3xOrgMember> members = orgManager.getMemberByIndistinctName(params.get("member"));
            StringBuilder sb = new StringBuilder("-1");
            if (!CollectionUtils.isEmpty(members)) {
                for (V3xOrgMember v3xOrgMember : members) {
                    sb.append(",");
                    sb.append(v3xOrgMember.getId());
                }
            }
            params.put("memberId", sb.toString());
        }
        // 默认不查询表单正文的模板
        String bodyType = "10,30,41,42,43,44,45";
        params.put("bodyType", bodyType);
        // 因为后面需要对模板列表检查是否授权，所以查询数据库时需要查询出所有记录然后进行内存分页
      //  FlipInfo flipInfoTemp = new FlipInfo();
      //  flipInfoTemp.setSize(Integer.MAX_VALUE);
        // 协同模板管理默认显示全部模板
        if("1".equals(params.get("categoryId"))){
            params.remove("categoryId");
        }
        boolean needSearchCategory = true;
        // 公文模板管理默认显示全部模板
        if (ApplicationCategoryEnum.edoc.name().equals(params.get("appName"))) {
     	   	needSearchCategory =false;
     	   params.put("categoryId", "");
            bodyType = bodyType+",20";
            params.put("bodyType", bodyType);
        }
        if("32".equals(params.get("categoryType"))){
        	needSearchCategory = false;
        	params.put("categoryType", "32");
            params.put("categoryId", "32");
        }
        List<V3xOrgAccount> concurrentAccounts= orgManager.getConcurrentAccounts(user.getId());
        List<Long> allAccountIds= new ArrayList<Long>();
        allAccountIds.add(user.getAccountId());
        if(null!=concurrentAccounts){
        	for (V3xOrgAccount myAccount : concurrentAccounts) {
        		allAccountIds.add(myAccount.getId()); 
			}
        }
        if(needSearchCategory){
        	params = getCtpCategoriesByAuthForCopy(params, allAccountIds, user.getId());
        }
        
        
        
        templateDao.selectAllSystemTempletesForCopy(flipInfo, params, allAccountIds);
        
        List<CtpTemplate> result = flipInfo.getData();
        List<TemplateBO> resultBO = new ArrayList<TemplateBO>();
        if (result != null) {
            TemplateBO bo = null;
            String[] results = null;
            V3xOrgMember member = null;
            V3xOrgAccount  account = orgManager.getAccountById(AppContext.currentAccountId()) ;
            for (CtpTemplate ctpTemplate : result) {
                // 是否有模板所属分类的权限
                if (ctpTemplate.getModuleType() == ModuleType.govdoc.getKey()
                        || ctpTemplate.getModuleType() == ModuleType.govdocRec.getKey()
                        || ctpTemplate.getModuleType() == ModuleType.govdocSend.getKey()
                        || ctpTemplate.getModuleType() == ModuleType.govdocSign.getKey()
                        || ctpTemplate.getModuleType() == ModuleType.govdocExchange.getKey()
                        || ctpTemplate.getModuleType() == ModuleType.collaboration.getKey()) {
                    bo = new TemplateBO(ctpTemplate);
                    results = getTemplateAuth(ctpTemplate);
                    bo.setAuth(results[0]);
                    bo.setAuthValue(results[1]);
                    bo.setHasAttsFlag(CtpTemplateUtil.isHasAttachments(ctpTemplate));
                    member = orgManager.getMemberById(ctpTemplate.getMemberId());
                    if (member != null) {
                       bo.setCreaterName(Functions.showMemberName(member));
                    }
                    resultBO.add(bo);
                }
                if(ctpTemplate.getModuleType() == ModuleType.info.getKey()){
                	 bo = new TemplateBO(ctpTemplate);
                	 results = getTemplateAuth(ctpTemplate);
                	 bo.setAuth(results[0]);
                     bo.setAuthValue(results[1]);
                     bo.setHasAttsFlag(CtpTemplateUtil.isHasAttachments(ctpTemplate));
                     try{
                    	 bo.setCreateUnit(account.getName());
                     }catch(Exception e){
                         LOG.error("", e);
                     }
                	 resultBO.add(bo);
                }
            }
        }
        
       //DBAgent.memoryPaging(resultBO, flipInfo);
        flipInfo.setData(resultBO);
        return flipInfo;
    }

	
    public String[] getTemplateAuth(CtpTemplate ctpTemplate) throws BusinessException {
        String[] result = new String[2];
        if (ctpTemplate != null) {
            List<CtpTemplateAuth> auths = this.getCtpTemplateAuths(ctpTemplate.getId(), null);

            result[0] = Functions.showOrgEntities(auths, "authId", "authType", null);
            result[1] = Functions.parseElements(auths, "authId", "authType");
        }
        return result;
    }
    
    
	private FlipInfo findFormTemplateList4Clone(FlipInfo flipInfo, Map<String, String> params) throws BusinessException{
		
		Long currentFormId = Long.parseLong(params.get("formAppId") + "");
		List<CtpTemplate> templateList = new ArrayList<CtpTemplate>();
		
		Set<Integer> formType = new HashSet<Integer>();
		formType.add(FormType.processesForm.getKey());
		List<CAPFormBean> fbList = capFormManager.getMyOwnForms(formType);
		
			
		String notInTemplateProcessId = ParamUtil.getString(params, "notInTemplateProcessId");
		templateList = findMyOwnFormTemplate(params,fbList,notInTemplateProcessId);
		
		long currentCategoryId = 0L;
		String currentFormName = "";
		List<CtpTemplate> templateCacheList = null;
		FormBean cap3FormBean = capFormManager.getEditingForm();
		if(null!=cap3FormBean && cap3FormBean.getId().equals(currentFormId)){
			currentCategoryId = cap3FormBean.getCategoryId();
			currentFormName = cap3FormBean.getFormName();
			if(cap3FormBean.getBind()!=null){
				templateCacheList = cap3FormBean.getBind().getFlowTemplateList();
			}
		}else{
			com.seeyon.cap4.form.bean.FormBean cap4FormBean = formApi4Cap4.getEditingForm(currentFormId);
			if(cap4FormBean!=null){
				currentCategoryId = cap4FormBean.getCategoryId();
				currentFormName = cap4FormBean.getFormName();
				if(cap4FormBean.getBind()!=null){
					templateCacheList = cap4FormBean.getBind().getFlowTemplateList();
				}
			}
		}
		//加入当前正在编辑表单对应的模板(缓存中的模板)
		if (Strings.isNotEmpty(templateCacheList)) {
		    //移除从数据库中查出的模板
		    for(int i=templateList.size()-1;i>=0;i--){
		    	if(((Long)templateList.get(i).getExtraAttr("formId")).longValue() == currentFormId.longValue()){
		    		templateList.remove(i);
		    	}
		    }
		    ComparatorCtpTemplate comparatorCtpTemplate = new ComparatorCtpTemplate("desc");
			Collections.sort(templateCacheList,comparatorCtpTemplate);
			//将当前正在编辑表单符合条件的加入模板列表中
			for(int i = templateCacheList.size()-1; i >= 0; i--){
				CtpTemplate c = templateCacheList.get(i);
				c.setCategoryId(currentCategoryId);
				c.putExtraAttr("formId", currentFormId);
                if (Strings.isBlank(notInTemplateProcessId) || !notInTemplateProcessId.equals(String.valueOf(c.getWorkflowId()))) {
                    //前台用查询条件查询时,不进此逻辑
                        if (Strings.isNotBlank(params.get("templateName"))) {
                            if (c.getSubject().contains(params.get("templateName"))) {
                                templateList.add(0, c);
                            }
                        } else if (Strings.isNotBlank(params.get("formname"))) {
                            if (currentFormName.contains(params.get("formname"))) {
                                templateList.add(0, c);
                            }
                        } else if (Strings.isNotBlank(params.get("category"))) {
                            if (params.get("category").equals(c.getCategoryId().toString())) {
                                templateList.add(0, c);
                            }
                        } else if (Strings.isNotBlank(params.get("startdate")) || Strings.isNotBlank(params.get("enddate"))) {
                                String startdate = params.get("startdate");
                                if(Strings.isBlank(startdate)){
                                	startdate = "1979-01-01";
                               }
                                String enddate = params.get("enddate");
                               if(Strings.isBlank(enddate)){
                                   enddate = "2050-01-01";
                               }
                                Date beginTime = Datetimes.parseDatetimeWithoutSecond(startdate + " 00:00");
                                Date endTime = Datetimes.parseDatetimeWithoutSecond(enddate + " 23:59");
                                if (c.getModifyDate().after(beginTime) && c.getModifyDate().before(endTime)) {
                                    templateList.add(0, c);
                                }
                        } else {
                            templateList.add(0, c);
                        }
                    } 
                }
			}
		List<Map<String, Object>> listMap = new ArrayList<Map<String, Object>>();
		Map<String, Object> ctMap;
		String[] auths = new String[2];
		for (CtpTemplate template : templateList) {
			if (!template.isSystem()) {
				continue;
			}
			ctMap = new HashMap<String,Object>();
			String formName = "";
			Long formId = (Long) template.getExtraAttr("formId");
			Long categoryId = null;
			if(formId!=null && !formId.equals(currentFormId)){
				CAPFormBean formBean = capFormManager.getForm(formId);
				formName = formBean.getFormName();
				categoryId = formBean.getCategoryId();
			}else{
				formName = currentFormName;
				categoryId = currentCategoryId;
			}
			//当用表单名称进行查询时,表单名称不匹配则跳过
			if(params.containsKey("formname")&&!formName.contains(params.get("formname").toString())){
				continue;
			}
			String category = "";
			CtpTemplateCategory  ctc = this.getCtpTemplateCategory(categoryId);
			if (ctc != null) {
				String categoryName = ctc.getName();
				String i18nName = ResourceUtil.getString(categoryName);
				category = Strings.isNotBlank(i18nName) ? i18nName : categoryName;
			}
			ctMap.put("workflowId", template.getWorkflowId());
			ctMap.put("templateName", template.getSubject());// 主题
			ctMap.put("formName", formName);//
			ctMap.put("category", category);//
			ctMap.put("formApp", String.valueOf(template.getExtraAttr("formId")));
			// 设置人员
			String ownerName = "";
			if (orgManager != null) {
				try {
					V3xOrgMember member = orgManager.getMemberById(template.getMemberId());
					if (member != null) {
						ownerName = member.getName();
					}
				} catch (Exception e) {
					LOG.error(e.getMessage(), e);
				}
			}
			ctMap.put("owner", ownerName);// s
			ctMap.put("modifyTime", DateUtil.format(template.getModifyDate(), DateUtil.YMDHMS_PATTERN));// s
			// 授权信息
			List<CtpTemplateAuth> authList = this.getCtpTemplateAuths(template.getId(), null);
			auths[0] = Functions.showOrgEntities(authList, "authId", "authType", null);
			auths[1] = Functions.parseElements(authList, "authId", "authType");
			ctMap.put("auths", auths.length > 0 ? auths[0] : "");// 授权人
			listMap.add(ctMap);
		}
		//这里进行内存分页
		DBAgent.memoryPaging(listMap, flipInfo);
		return flipInfo;
	}

    @ProcessInDataSource(name = DataSourceName.BASE)
	private List<CtpTemplate> findMyOwnFormTemplate(Map<String, String> params,List<CAPFormBean> fbList,String notInProcessTemplateId) throws BusinessException{
		List<CtpTemplate> cList = new ArrayList<CtpTemplate>();
		if(Strings.isEmpty(fbList)){
			return cList;
		}
		Map<String, Object> map = new HashMap<String, Object>();
		StringBuffer hql = new StringBuffer();
		hql.append("select new map(t as o,a.contentTemplateId as formId) from CtpTemplate t,CtpContentAll a where "); 
		hql.append(" t.body=a.id and t.delete=:delete");
		if(Strings.isNotBlank(notInProcessTemplateId)){
			hql.append(" and t.workflowId != :notInTemplateId");
			map.put("notInTemplateId", Long.parseLong(notInProcessTemplateId));
		}
		if(Strings.isNotBlank(params.get("templateName"))){
			hql.append(" and t.subject like :templateName");
			map.put("templateName", "%" + SQLWildcardUtil.escape(params.get("templateName")) + "%");
		}
		if(Strings.isNotBlank(params.get("category"))){
			hql.append(" and t.categoryId = :category");
			map.put("category", Long.parseLong(params.get("category")));
		}
		// 根据修改时间查找
		String startdate = params.get("startdate");
		String enddate = params.get("enddate");
		if (Strings.isNotBlank(startdate) || Strings.isNotBlank(enddate)) {
            hql.append(" and t.modifyDate between :startdate and :enddate ");
            if(Strings.isBlank(startdate)){
            	startdate = "1979-01-01";
            }
            if(Strings.isBlank(enddate)){
                enddate = "2050-01-01";
            }
            map.put("startdate", Datetimes.parseDatetimeWithoutSecond(startdate + " 00:00"));
            map.put("enddate",  Datetimes.parseDatetimeWithoutSecond(enddate + " 23:59"));
        }
		List<Long> formIds = new ArrayList<Long>();
		for(CAPFormBean f:fbList){
		    if(params.containsKey("formname")){
		        if(!f.getFormName().contains((String) params.get("formname"))){
		            continue;
		        }
		    }
			formIds.add(f.getId());
		}
		if(Strings.isEmpty(formIds)){
			formIds.add(-1L);
		}
		List<Long>[] ids = Strings.splitList(formIds, 999);
        if (ids.length == 1){
        	hql.append(" and a.contentTemplateId in (:contentTemplateId) ");
        	map.put("contentTemplateId", formIds);
        } else {
        	hql.append(" and (");
            for(int i=0;i<ids.length;i++){
            	hql.append("a.contentTemplateId in (:contentTemplateId"+i+") or ");
            	map.put("contentTemplateId"+i, ids[i]);
            }
            hql.append("a.contentTemplateId = 1)");
        }
        map.put("delete", false);
        hql.append(" order by t.modifyDate desc");
        try {
            CtpDynamicDataSource.setDataSourceKey(DataSourceName.BASE.getSource());
            List<Map<String, Object>> o = DBAgent.find(hql.toString(), map);
            for (Map<String, Object> m : o) {
                CtpTemplate c = (CtpTemplate) m.get("o");
                c.putExtraAttr("formId", (Long) m.get("formId"));
                cList.add(c);
            }
            return cList;
        }finally {
            CtpDynamicDataSource.clearDataSourceKey();
        }
    }
	
	
	
    public List<TemplateTreeVo> getShortCutTemplateTree(List<ModuleType> moduleTypes, Map<String,Object> params, boolean isMobile) throws BusinessException{
		User user = AppContext.getCurrentUser();
		List<TemplateTreeVo> listTreeVo = new ArrayList<TemplateTreeVo>();
		
    	List<CtpTemplate> allTemplates = new ArrayList<CtpTemplate>();
    	if(AppContext.isAdmin()){
    	    FlipInfo flipInfo = new FlipInfo(1, -1);
            Map<String, String> sysParams = new HashMap<String, String>();
            sysParams.put("categoryType", "1,2");
            sysParams.put("delete", "false");
            templateDao.selectAllSystemTempletes(flipInfo, sysParams, user.getLoginAccount());
            allTemplates = flipInfo.getData();
    	}else{
    		// 所有模板，包括协同和表单
    		List<CtpTemplate> systemTempletes = getSystemTemplatesByAcl(user.getId(), moduleTypes);
    		allTemplates.addAll(systemTempletes);
    		
    		if(!isMobile){
    		    // 最近使用模版的分类
    		    TemplateTreeVo recentTemplate = new TemplateTreeVo();
    		    recentTemplate.setId(TemplateCategoryConstant.recentCall.key());
    		    recentTemplate.setName(ResourceUtil.getString("template.choose.category.recent.label"));//最近使用根目录
    		    recentTemplate.setpId(null);
    		    recentTemplate.setType("category");
    		    listTreeVo.add(recentTemplate);
    		    // 个人模板
    		    listTreeVo.add(new TemplateTreeVo(100L, ResourceUtil.getString("template.templatePub.personalTemplates"), "personal", null,""));//"个人模板"
    		    listTreeVo.add(new TemplateTreeVo(101L, ResourceUtil.getString("collaboration.template.category.type.0"), "template_coll", 100L,""));//"协同模板"
    		    listTreeVo.add(new TemplateTreeVo(102L, ResourceUtil.getString("collaboration.saveAsTemplate.formatTemplate"), "text_coll", 100L,""));//"格式模板"
    		    listTreeVo.add(new TemplateTreeVo(103L, ResourceUtil.getString("collaboration.saveAsTemplate.flowTemplate"), "workflow_coll", 100L,""));//"流程模板"
    		    listTreeVo.add(new TemplateTreeVo(104L, ResourceUtil.getString("collaboration.saveAsTemplate.edocPtem"), "category", 100L,""));
    		}
    	}
    	if(isMobile){
    	    // 表单模板
    	    listTreeVo.add(new TemplateTreeVo(TemplateCategoryConstant.publicRoot.key(), ResourceUtil.getString("desk.metro.form.templates"), "category", null,""));
    	    allTemplates.addAll(getUnflowTemplates(user, null, ""));
    	}else{
    	    // 公共模板
    	    listTreeVo.add(new TemplateTreeVo(TemplateCategoryConstant.publicRoot.key(), ResourceUtil.getString("template.public.label"), "category", null,""));
    	}
    	if(moduleTypes.contains(ModuleType.edoc)){
    	    // 公文模板分类
    		listTreeVo.add(setCategory(ModuleType.govdoc.getValue()));
/*    	    listTreeVo.add(setCategory(ModuleType.edoc.getValue()));
    	    listTreeVo.add(setCategory(ModuleType.edocSend.getValue()));
    	    listTreeVo.add(setCategory(ModuleType.edocRec.getValue()));
    	    listTreeVo.add(setCategory(ModuleType.edocSign.getValue()));*/
    	}
    	
    	Map<String, CtpTemplateCategory> nameCategory = new HashMap<String, CtpTemplateCategory>();
    	Map<Long, TemplateTreeVo> idCategory =  new HashMap<Long, TemplateTreeVo>();
    	
    	List<CtpTemplateCategory> templeteCategory = getCategorys(user.getLoginAccount(), moduleTypes);
    	if (!CollectionUtils.isEmpty(templeteCategory)) {
            for (CtpTemplateCategory ctg : templeteCategory) {
                if (ctg.isDelete() == null || !ctg.isDelete()) {
                    nameCategory.put(ctg.getName(), ctg);
                }
            }
    	}
    	
    	// 是否显示外单位模板
        boolean isShowOuter = Boolean.valueOf(Functions.getSysFlag("col_showOtherAccountTemplate").toString());
    	for (CtpTemplate template : allTemplates) {
        	CtpTemplateCategory ctCategory = this.getCtpTemplateCategory(template.getCategoryId());
        	if(ctCategory == null){
                continue;
            }
        	//外单位的模板才进行分类合并
            if (!template.getOrgAccountId().equals(user.getLoginAccount())) {
                if (!isShowOuter || !template.isSystem() || template.getCategoryId() == 0) {
                    continue;
                }
                CtpTemplateCategory n = nameCategory.get(ctCategory.getName());
                if (n != null) {
                    ctCategory = n;
                } else {
                    nameCategory.put(ctCategory.getName(), ctCategory);
                    if (!isGovdoc(ctCategory.getType().toString())) {//新公文分类类型,数据库中存在,因此不需要创建虚拟分类
                    	listTreeVo.add(new TemplateTreeVo(ctCategory.getId(), ctCategory.getName(), "category", TemplateCategoryConstant.publicRoot.key(), ""));
                    }
                    continue;
                }
            }

        	if(idCategory.containsKey(ctCategory.getId())){
        		continue;
        	}
            
            List<CtpTemplateCategory> parentCtgs = getParentCategorys(ctCategory);
            
        	for(CtpTemplateCategory parent : parentCtgs){
            	if(idCategory.containsKey(parent.getId())){
            		continue;
            	}
            	TemplateTreeVo templateTreeVO = new TemplateTreeVo();
                templateTreeVO.setId(parent.getId());
                templateTreeVO.setName(parent.getName());
                templateTreeVO.setType("category");
                if (null == parent.getParentId()) {
                	if(parent.getId()==ModuleType.govdocSend.getKey()||parent.getId()==ModuleType.govdocRec.getKey()||
                			parent.getId()==ModuleType.govdocSign.getKey()){
                		templateTreeVO.setpId(TemplateCategoryConstant.govdocRoot.key());
                	}else{
                		templateTreeVO.setpId(TemplateCategoryConstant.publicRoot.key());
                	}
                } else {
                    templateTreeVO.setpId(parent.getParentId());
                }
                idCategory.put(parent.getId(), templateTreeVO);
            }
            
            TemplateTreeVo templateTreeVO = new TemplateTreeVo();
            templateTreeVO.setId(ctCategory.getId());
            templateTreeVO.setName(ctCategory.getName());
            templateTreeVO.setType("category");
            if (null == ctCategory.getParentId()) {
            	if(ctCategory.getId()==ModuleType.govdocSend.getKey()||ctCategory.getId()==ModuleType.govdocRec.getKey()||
            			ctCategory.getId()==ModuleType.govdocSign.getKey()){
            		templateTreeVO.setpId(TemplateCategoryConstant.govdocRoot.key());
            	}else{
            		templateTreeVO.setpId(TemplateCategoryConstant.publicRoot.key());
            	}
            } else {
                //表单的插入到公共模板下面
                if (ctCategory.getParentId() == 2L || 1L == ctCategory.getParentId()) {
                    templateTreeVO.setpId(TemplateCategoryConstant.publicRoot.key());
                } else {
                    templateTreeVO.setpId(ctCategory.getParentId());
                }
            }
        	idCategory.put(ctCategory.getId(), templateTreeVO);
        }
    	listTreeVo.addAll(idCategory.values());
    	
        return listTreeVo;
	}
	
	/**
	 * 获取无流程模板
	 * @param user
	 * @param categoryId
	 * @param searchKey
	 * @return
	 */
    private List<CtpTemplate> getUnflowTemplates(User user, Long categoryId, String searchKey) {
        List<CtpTemplate> temps = new ArrayList<CtpTemplate>();
        try {
            List<FormBean> formList = formApi4Cap3.getFormsByTypes(new int[] { FormType.manageInfo.getKey(), FormType.baseInfo.getKey() });
            for (FormBean fbean : formList) {
                if (!formApi4Cap3.isEnabled(fbean.getId())) {
                    continue;
                }
                CtpTemplateCategory category = getCtpTemplateCategory(fbean.getCategoryId());
                if (categoryId != null && !category.getId().equals(categoryId)) {
                    continue;
                }
                FormBindBean bindBean = fbean.getBind();
                Map<String, FormBindAuthBean> templates = bindBean.getUnFlowTemplateMap();
                for (Entry<String, FormBindAuthBean> entry : templates.entrySet()) {
                    FormBindAuthBean template = entry.getValue();
                    //判断当前人是否有权限
                    boolean hasRight = false;
                    if (user.isAdmin()) {
                        hasRight = category.getOrgAccountId().equals(user.getAccountId());
                    } else {
                        hasRight = template.checkRight(user.getId());
                    }
                    if(hasRight){
                        CtpTemplate t = new CtpTemplate();
                        t.setId(template.getId());
                        t.setFormAppId(fbean.getId());
                        t.setSubject(template.getName());
                        t.setCategoryId(fbean.getCategoryId());
                        t.setOrgAccountId(user.getLoginAccount());
                        t.setModuleType(fbean.getFormType());
                        t.setType("unflowTemplate");
                        temps.add(t);
                    }
                }
            }
        } catch (BusinessException e) {
            LOG.error("", e);
        }
        return temps;
    }

    private TemplateTreeVo setCategory(String s) {
        TemplateTreeVo ttpersonlVO = null;
        ttpersonlVO = new TemplateTreeVo();
        // 表单和协同的构建根节点(pid为空的，则为顶层)
        ttpersonlVO.setId(Long.valueOf(s));
        if (s.equals(ModuleType.edoc.getValue()))
            ttpersonlVO.setName(ResourceUtil.getString("template.edoc.label"));
        if (s.equals(ModuleType.edocSend.getValue()))
            ttpersonlVO.setName(ResourceUtil.getString("template.edocsend.label"));
        if (s.equals(ModuleType.edocRec.getValue()))
            ttpersonlVO.setName(ResourceUtil.getString("template.edocrec.label"));
        if (s.equals(ModuleType.edocSign.getValue()))
            ttpersonlVO.setName(ResourceUtil.getString("template.edocsign.label"));
        if (s.equals(ModuleType.govdoc.getValue()))
            ttpersonlVO.setName(ResourceUtil.getString("template.edoc.label"));
        if (s.equals(ModuleType.govdocSend.getValue()))
            ttpersonlVO.setName(ResourceUtil.getString("template.edocsend.label"));
        if (s.equals(ModuleType.govdocRec.getValue()))
            ttpersonlVO.setName(ResourceUtil.getString("template.edocrec.label"));
        if (s.equals(ModuleType.govdocSign.getValue()))
            ttpersonlVO.setName(ResourceUtil.getString("template.edocsign.label"));
        ttpersonlVO.setType("category");
        if (s.equals(ModuleType.edoc.getValue()))
            ttpersonlVO.setpId(null);
        else
            ttpersonlVO.setpId(Long.parseLong(ModuleType.edoc.getValue()));
        if (s.equals(ModuleType.govdoc.getValue()))
            ttpersonlVO.setpId(null);
        else
            ttpersonlVO.setpId(Long.parseLong(ModuleType.govdoc.getValue()));
        return ttpersonlVO;
    }
	private TemplateTreeVo setCategory(String s, Integer affairCount) {
        TemplateTreeVo ttpersonlVO = null;
        ttpersonlVO = new TemplateTreeVo();
        // 表单和协同的构建根节点(pid为空的，则为顶层)
        ttpersonlVO.setId(Long.valueOf(s));
        ttpersonlVO.setAffairCount(affairCount);
        if (s.equals(ModuleType.edoc.getValue()))
            ttpersonlVO.setName(ResourceUtil.getString("template.edoc.label"));
        if (s.equals(ModuleType.edocSend.getValue()))
            ttpersonlVO.setName(ResourceUtil.getString("template.edocsend.label"));
        if (s.equals(ModuleType.edocRec.getValue()))
            ttpersonlVO.setName(ResourceUtil.getString("template.edocrec.label"));
        if (s.equals(ModuleType.edocSign.getValue()))
            ttpersonlVO.setName(ResourceUtil.getString("template.edocsign.label"));
        ttpersonlVO.setType("category");
        if (s.equals(ModuleType.edoc.getValue()))
            ttpersonlVO.setpId(null);
        else
            ttpersonlVO.setpId(Long.parseLong(ModuleType.edoc.getValue()));
        return ttpersonlVO;
    }
	private TemplateTreeVo setCategory(Long s, Integer affairCount) {
        TemplateTreeVo ttpersonlVO = null;
        ttpersonlVO = new TemplateTreeVo();
        // 表单和协同的构建根节点(pid为空的，则为顶层)
        ttpersonlVO.setId(s);
        ttpersonlVO.setAffairCount(affairCount);
        if (s.equals(TemplateCategoryConstant.edocRoot.key()))
            ttpersonlVO.setName(ResourceUtil.getString("template.edoc.label"));
        if (s.equals(TemplateCategoryConstant.edocSendRoot.key()))
            ttpersonlVO.setName(ResourceUtil.getString("template.edocsend.label"));
        if (s.equals(TemplateCategoryConstant.edocRecRoot.key()))
            ttpersonlVO.setName(ResourceUtil.getString("template.edocrec.label"));
        if (s.equals(TemplateCategoryConstant.edocSignRoot.key()))
            ttpersonlVO.setName(ResourceUtil.getString("template.edocsign.label"));
        ttpersonlVO.setType("category");
        if (s.equals(TemplateCategoryConstant.edocRoot.key()))
            ttpersonlVO.setpId(null);
        else
            ttpersonlVO.setpId(TemplateCategoryConstant.edocRoot.key());
        return ttpersonlVO;
    }
	
/*	public List<CtpTemplate> getTemplates4PortletByCache(Long categoryId, Map<String, Object> queryMap, boolean isMobile) throws BusinessException {
		return getTemplates4Portlet(categoryId,queryMap,isMobile);
	}*/
	
	public List<CtpTemplate> getTemplates4Portlet(Long categoryId, Map<String, Object> queryMap, boolean isMobile) throws BusinessException {
		User user = AppContext.getCurrentUser();
		
		List<ModuleType> moduleTypes = new ArrayList<ModuleType>();
        if (AppContext.hasPlugin("collaboration")) {
            moduleTypes.add(ModuleType.collaboration);
            moduleTypes.add(ModuleType.form);
        }
        if (AppContext.hasPlugin("edoc")) {
/*            moduleTypes.add(ModuleType.edoc);
            moduleTypes.add(ModuleType.edocSend);
            moduleTypes.add(ModuleType.edocRec);
            moduleTypes.add(ModuleType.edocSign);*/
            moduleTypes.add(ModuleType.govdoc);//新公文
            moduleTypes.add(ModuleType.govdocSend);
            moduleTypes.add(ModuleType.govdocRec);
            moduleTypes.add(ModuleType.govdocSign);
        }
		// 个人模板分类
		List<Long> personalCtg = Arrays.asList(100L, 101L, 102L, 103L, 104L);
		List<CtpTemplate> templetes = new ArrayList<CtpTemplate>();
		if (Long.valueOf(TemplateCategoryConstant.recentCall.key()).equals(categoryId)) {// 最近使用模板
		    String category = "-1";
		    for(ModuleType t : moduleTypes){
		        category += ("," + t.getKey());
		    }
		    List<CtpTemplate> templates = (List<CtpTemplate>) AppContext.getThreadContext("User_Portlet_Templates_Recent");
		    if (templates == null) {
		        templetes = getRecentTemplates(category, 10);
		        AppContext.putThreadContext("User_Portlet_Templates_Recent", templates);
		    }
		} else if (personalCtg.contains(categoryId)) {// 个人模板
		    List<CtpTemplate> personalTempletes = (List<CtpTemplate>) AppContext.getThreadContext("User_Portlet_Templates_Personal");
		    if (personalTempletes == null) {
		        personalTempletes = getPersonalTemplates(user.getId(), moduleTypes);
                AppContext.putThreadContext("User_Portlet_Templates_Personal", personalTempletes);
            }
			if (Long.valueOf(100L).equals(categoryId)) {
				templetes.addAll(personalTempletes);
			} else {
				for (CtpTemplate t : personalTempletes) {
					if ("template".equals(t.getType()) && Long.valueOf(101L).equals(categoryId)
							&& t.getCategoryId() == null) {
						templetes.add(t);
					} else if ("text".equals(t.getType()) && Long.valueOf(102L).equals(categoryId)) {
						templetes.add(t);
					} else if ("workflow".equals(t.getType()) && Long.valueOf(103L).equals(categoryId)) {
						templetes.add(t);
					} else if ("template".equals(t.getType()) && Long.valueOf(104L).equals(categoryId) && t.getCategoryId() != null) {
						templetes.add(t);
					}
				}
			}
		} else {
			
			List<ModuleType> mTypes = new ArrayList<ModuleType>();
			Map<String, String> params = new HashMap<String, String>();
			List<Long> edocCtg = Arrays.asList(400L,401L,402L,404L);
			List<CtpTemplate> systemTemplet = null;
			Long tempCategory = categoryId;
			CtpTemplateCategory category = this.getCategorybyId(categoryId);
			if(category!=null&&(category.getType()==(ModuleType.govdocSend.getKey())||category.getType()==(ModuleType.govdocRec.getKey())
					||category.getType()==(ModuleType.govdocSign.getKey()))){
				
				categoryId = Long.valueOf(category.getType());
			}
			if (edocCtg.contains(categoryId)) {
			    systemTemplet = (List<CtpTemplate>) AppContext.getThreadContext("User_Portlet_Templates_SystemEdoc");
				params.put("categoryType", "401,402,404");
/*				mTypes.add(ModuleType.edoc);
				mTypes.add(ModuleType.edocSend);
				mTypes.add(ModuleType.edocRec);
				mTypes.add(ModuleType.edocSign);*/
				mTypes.add(ModuleType.govdoc);//新公文
				mTypes.add(ModuleType.govdocSend);
				mTypes.add(ModuleType.govdocRec);
				mTypes.add(ModuleType.govdocSign);
				/*if(!categoryId.equals(4L)){
				    params.put("categoryId", categoryId.toString());
				}*/
				categoryId = tempCategory;
			} else {
			    systemTemplet = (List<CtpTemplate>) AppContext.getThreadContext("User_Portlet_Templates_SystemCF");
			    params.put("categoryType", "1,2");
			    mTypes.add(ModuleType.collaboration);
			    mTypes.add(ModuleType.form);
				/*if(!categoryId.equals(-1L)){
				    params.put("categoryId", categoryId.toString());
				}*/
			}
			if (systemTemplet == null) {
			    if(user.isAdmin()){
			        FlipInfo flipInfo = new FlipInfo(1, -1);
			        params.put("delete", "false");
			        templateDao.selectAllSystemTempletes(flipInfo, params, user.getLoginAccount());
			        systemTemplet = flipInfo.getData();
			    }else{
			        systemTemplet = getSystemTemplatesByAcl(user.getId(), mTypes);
			    }
			    // 移动端 将无流程模板合并进来
			    if (isMobile) {
			        systemTemplet.addAll(getUnflowTemplates(user, null, ""));
			    }
			    if (edocCtg.contains(categoryId)) {
	                AppContext.putThreadContext("User_Portlet_Templates_SystemEdoc", systemTemplet);
	            } else {
	                AppContext.putThreadContext("User_Portlet_Templates_SystemCF", systemTemplet);
	            }
            }

			if (Long.valueOf(TemplateCategoryConstant.publicRoot.key()).equals(categoryId) 
					|| Long.valueOf(4L).equals(categoryId)
					||Long.valueOf(400L).equals(categoryId)) {
				templetes.addAll(systemTemplet);
			} else {
				CtpTemplateCategory ctg = getCtpTemplateCategory(categoryId);
				for (CtpTemplate t : systemTemplet) {
					if (t.getCategoryId() != null && categoryId.equals(t.getCategoryId()) || ctg.getName().equals(getCtpTemplateCategory(t.getCategoryId()).getName())) {
						templetes.add(t);
					}
				}
			}
		}

		return templetes;
	}
	
	@Override
	public boolean checkFormIsEnabled(Long formId){
		boolean enabled = capFormManager.isEnabled(formId);
		return enabled;
	}
	/**
	 * 获取模板更多清单数据
	 * @param flipInfo
	 * @param params
	 * @return
	 * @throws BusinessException
	 */
	@AjaxAccess
	public FlipInfo listRACITemplate(FlipInfo flipInfo, Map<String,Object> params) throws BusinessException{
		User user = AppContext.getCurrentUser();
    	Long orgAccountId = user.getLoginAccount();
    	
    	//获取显示授权模版的单位
    	String selectAccountId = (String)params.get("selectAccountId");
    	//是否所有模版。
        String isShowTemplates = "true";
        //传递的单位selectAccountId=1为全部，则查询外单位授权过来的
        if (Strings.isNotBlank(selectAccountId) && "1".equals(selectAccountId)) {
        	isShowTemplates = "true";
        } else if (Strings.isNotBlank(selectAccountId) && !"1".equals(selectAccountId)) {
        	isShowTemplates = "false";
            orgAccountId = Long.parseLong(selectAccountId);
        }
        params.put("isShowTemplates", isShowTemplates);
        params.put("accountId", orgAccountId);
        String searchValue = (String)params.get("searchValue");
        if (Strings.isNotBlank(searchValue)) {
            params.put("subject", searchValue);
        }
    	
    	List<TemplateBO> showTemplates = this.getSectionShowTemplate(params);
    	
    	DBAgent.memoryPaging(showTemplates, flipInfo);
        
		return flipInfo;
	}
	
    @Override
    public List<Long> getAllChildCategoryIds(Long categoryId) throws BusinessException {
        CtpTemplateCategory currentNode = getCategorybyId(categoryId);
        if (currentNode == null) {
            return Collections.emptyList();
        }
        Map<Long, CtpTemplateCategory> map = new HashMap<Long, CtpTemplateCategory>();
        List<CtpTemplateCategory> children = currentNode.getAllCascadeChildrens();
        if (Strings.isNotEmpty(children)) {
            for (CtpTemplateCategory c : children) {
                map.put(c.getId(), c);
            }
        }
        List<Long> ret =new ArrayList<Long>();
        if(Strings.isNotEmpty(map.keySet())){
            for(Long c :map.keySet()){
                ret.add(c);
            }
        }
        return ret;   
    }
    
    @Override
    public List<CtpTemplateCategory> getAllChildCategories(Long categoryId) throws BusinessException {
        CtpTemplateCategory currentNode = getCategorybyId(categoryId);
        if (currentNode == null) {
            return Collections.emptyList();
        }
        Map<Long, CtpTemplateCategory> map = new HashMap<Long, CtpTemplateCategory>();
        List<CtpTemplateCategory> children = currentNode.getAllCascadeChildrens();
        if (Strings.isNotEmpty(children)) {
            for (CtpTemplateCategory c : children) {
                map.put(c.getId(), c);
            }
        }
        List<CtpTemplateCategory> ret =new ArrayList<CtpTemplateCategory>();
        if(Strings.isNotEmpty(map.values())){
            for(CtpTemplateCategory c :map.values()){
                ret.add(c);
            }
        }
        return ret;   
     }
    
    /**
     * <description>递归获取子模板分类</description>
     *
     * @param current
     * @param map
     * @since: Seeyon V6.1SP2 portal
     * @date: 2017年12月12日 上午10:51:32
     */
	/*   private void recursionCategories(CtpTemplateCategory current, Map<Long, CtpTemplateCategory> map) {
	    List<CtpTemplateCategory> children = current.getChildren();
	    if (Strings.isNotEmpty(children)) {
	        return;
	    }
	    for (CtpTemplateCategory c : children) {
	        map.put(c.getId(), c);
	        recursionCategories(c, map);
	    }
	}*/
    
    @Override
    public void createCategoryParentTreeVO (CtpTemplateCategory category, Integer affairCount, Map<Long, TemplateTreeVo> treeMap) {
    	while(true) {
    		if (category != null) {
    			Long categoryId = category.getId();
    			if (isEdoc(String.valueOf(categoryId))) {
//    				if (treeMap.get(TemplateCategoryConstant.edocRoot.key())==null) {
//    					TemplateTreeVo treeVO = setCategory(TemplateCategoryConstant.edocRoot.key(), affairCount);
//    					treeMap.put(treeVO.getId(), treeVO);
//    				} else {
//    					TemplateTreeVo existTree = treeMap.get(TemplateCategoryConstant.edocRoot.key());
//    					existTree.setAffairCount(existTree.getAffairCount() + affairCount);
//    				}
//    				if (categoryId.equals(TemplateCategoryConstant.edocSendRoot.key())) {
//	    				if (treeMap.get(TemplateCategoryConstant.edocSendRoot.key())==null) {
//	    						TemplateTreeVo treeVO = setCategory(TemplateCategoryConstant.edocSendRoot.key(), affairCount);
//	    						treeMap.put(treeVO.getId(), treeVO);
//	    				} else {
//	    					TemplateTreeVo existTree = treeMap.get(TemplateCategoryConstant.edocSendRoot.key());
//	    					existTree.setAffairCount(existTree.getAffairCount() + affairCount);
//	    				}
//    				}
//    				if (categoryId.equals(TemplateCategoryConstant.edocRecRoot.key())) {
//	    				if (treeMap.get(TemplateCategoryConstant.edocRecRoot.key())==null) {
//	    						TemplateTreeVo treeVO = setCategory(TemplateCategoryConstant.edocRecRoot.key(), affairCount);
//	    						treeMap.put(treeVO.getId(), treeVO);
//	    				} else {
//	    					TemplateTreeVo existTree = treeMap.get(TemplateCategoryConstant.edocRecRoot.key());
//	    					existTree.setAffairCount(existTree.getAffairCount() + affairCount);
//	    				}
//    				}
//    				if (categoryId.equals(TemplateCategoryConstant.edocSignRoot.key())) {
//	    				if (treeMap.get(TemplateCategoryConstant.edocSignRoot.key())==null) {
//	    						TemplateTreeVo treeVO = setCategory(TemplateCategoryConstant.edocSignRoot.key(), affairCount);
//	    						treeMap.put(treeVO.getId(), treeVO);
//	    				} else {
//	    					TemplateTreeVo existTree = treeMap.get(TemplateCategoryConstant.edocSignRoot.key());
//	    					existTree.setAffairCount(existTree.getAffairCount() + affairCount);
//	    				}
//    				}
    				break;
    			} else if (category.getParentId()!=null 
    			        &&(Long.valueOf(ModuleType.collaboration.getKey()).equals(category.getParentId())
    			        ||Long.valueOf(ModuleType.form.getKey()).equals(category.getParentId()))){//公共模板
    				TemplateTreeVo treeVO = new TemplateTreeVo(TemplateCategoryConstant.publicRoot.key(), ResourceUtil.getString("template.public.label"), "category", null,"", affairCount);
    				if (treeMap.get(treeVO.getId())==null) {
    					treeMap.put(treeVO.getId(), treeVO);
    				} else {
    					TemplateTreeVo existTree = treeMap.get(treeVO.getId());
    					existTree.setAffairCount(existTree.getAffairCount() + affairCount);
    				}
        			category.setParentId(TemplateCategoryConstant.publicRoot.key());
        			if (treeMap.get(category.getId())==null) {
        				TemplateTreeVo treeVOchrld = new TemplateTreeVo(category.getId(),category.getName(),"category",category.getParentId(),"", affairCount);
        				treeMap.put(treeVOchrld.getId(), treeVOchrld);
        			} else {
    					TemplateTreeVo existTree = treeMap.get(category.getId());
    					existTree.setAffairCount(existTree.getAffairCount() + affairCount);
    				}
        			break;
    			}else if (category.getParentId()!=null 
    			        &&(isGovdoc(category.getType().toString()))) {
					TemplateTreeVo treeVO1 =  new TemplateTreeVo(TemplateCategoryConstant.govdocSendRoot.key(), ResourceUtil.getString("template.templatePub.postingTemplate"), "category", TemplateCategoryConstant.govdocRoot.key(),"", affairCount);
   					TemplateTreeVo treeVO2 =  new TemplateTreeVo(TemplateCategoryConstant.govdocRecRoot.key(), ResourceUtil.getString("template.templatePub.receiptTemplate"), "category", TemplateCategoryConstant.govdocRoot.key(),"", affairCount);
   					TemplateTreeVo treeVO3 =  new TemplateTreeVo(TemplateCategoryConstant.govdocSignRoot.key(), ResourceUtil.getString("template.categorytree.signdoctemplate.label"), "category", TemplateCategoryConstant.govdocRoot.key(),"", affairCount);
   					if(category.getType() == TemplateCategoryConstant.govdocSendRoot.key()){
   						if (treeMap.get(category.getId())==null) {
   	   						treeMap.put(treeVO1.getId(), treeVO1);
   	   					}else{
   	   						TemplateTreeVo existTree = treeMap.get(treeVO1.getId());
   	   						existTree.setAffairCount(existTree.getAffairCount() + affairCount);

   						}
   					} 
   					if(category.getType() == TemplateCategoryConstant.govdocRecRoot.key()){
   						if (treeMap.get(category.getId())==null) {
   	   						treeMap.put(treeVO2.getId(), treeVO2);
   	   					}else{
   	   						TemplateTreeVo existTree = treeMap.get(treeVO2.getId());
   	   						existTree.setAffairCount(existTree.getAffairCount() + affairCount);

   						}
   					} 
   					if(category.getType() == TemplateCategoryConstant.govdocSignRoot.key()){
   						if (treeMap.get(category.getId())==null) {
   	   						treeMap.put(treeVO3.getId(), treeVO3);
   	   					}else {
   	   						TemplateTreeVo existTree = treeMap.get(treeVO3.getId());
   	   						existTree.setAffairCount(existTree.getAffairCount() + affairCount);

   						}
   					}
        			if (treeMap.get(category.getId())==null) {
        				TemplateTreeVo treeVOchrld = new TemplateTreeVo(category.getId(),category.getName(),"category",category.getParentId(),"", affairCount);
        				treeMap.put(treeVOchrld.getId(), treeVOchrld);
        			} else {
    					TemplateTreeVo existTree = treeMap.get(category.getId());
    					existTree.setAffairCount(existTree.getAffairCount() + affairCount);
    				}
        			break;
    			}
    			if (treeMap.get(category.getId())==null) {
    				TemplateTreeVo treeVO = new TemplateTreeVo(category.getId(),category.getName(),"category",category.getParentId(),"", affairCount);
    				treeMap.put(treeVO.getId(), treeVO);
    			} else {
					TemplateTreeVo existTree = treeMap.get(category.getId());
					existTree.setAffairCount(existTree.getAffairCount() + affairCount);
				}
    			category = templateCategoryManager.get(category.getParentId());
    		} else {
    			break;
    		}
    	}
    }
    
    @Override
    public List<CtpTemplate> getAllTemplateByCategoryId(String categoryIds) throws BusinessException {
    	Map<String,Object> params = new HashMap<String,Object>();
        params.put("categoryIds",categoryIds);
        
    	List<CtpTemplate> allTempletes = this.getMyConfigCollTemplate(null, params);
    	
    	return allTempletes;
    }

	@Override
	public List<CtpTemplate> getTemplateByRACI(List<String> domainIds) throws BusinessException {
		List<CtpTemplate> result = new ArrayList<CtpTemplate>();
		if(Strings.isEmpty(domainIds)){
			return result;
		}
		
		StringBuilder sql = new StringBuilder();
		sql.append(" select distinct a.id, a.subject, a.belongOrg ");
		sql.append("   from CtpTemplate a, CtpTemplateOrg b ");
		sql.append("  where a.id = b.templateId ");
		sql.append("    and b.orgId in (:domainIds) ");
		sql.append("    and a.state = :state ");
		sql.append("    and a.system = 1 ");
		sql.append("    and a.delete = 0 ");
		sql.append("    and b.dataType in (:dataType)");
		sql.append(" 	and a.moduleType not in (:categoryId) ");
		
		Map<String, Object> queryParams = new HashMap<String, Object>();
		List<Integer> categoryId = new ArrayList<Integer>();
		categoryId.add(Integer.valueOf(ModuleType.edocRec.getKey()));
		categoryId.add(Integer.valueOf(ModuleType.edocSend.getKey()));
		categoryId.add(Integer.valueOf(ModuleType.edocSign.getKey()));
		queryParams.put("categoryId", categoryId);
		
		queryParams.put("state", TemplateEnum.State.normal.ordinal());
		queryParams.put("domainIds", domainIds);
		
		List<Integer> dataType = new ArrayList<Integer>();
		dataType.add(TemplateEnum.DataType.R.ordinal());
		dataType.add(TemplateEnum.DataType.A.ordinal());
		dataType.add(TemplateEnum.DataType.C.ordinal());
		dataType.add(TemplateEnum.DataType.I.ordinal());
		queryParams.put("dataType", dataType);
		
		List<Object[]> list = DBAgent.find(sql.toString(), queryParams);

		for (Object[] o : list) {
            CtpTemplate template = new CtpTemplate();
            int n = 0;
            template.setId((Long) o[n++]);
            template.setSubject((String) o[n++]);
            template.setBelongOrg((Long) o[n++]);
            
            result.add(template);
        }
        return result;
	}

	@Override
    public Map<Long,String> getSectionShowCounts(Map<String,Object> params) throws BusinessException{
    	//模版所在的单位集合
    	Map<Long,String> accounts = new HashMap<Long,String>();
    	
    	User user = AppContext.getCurrentUser();
    	Long orgAccountId = user.getLoginAccount();
    	
      //根据首页模板栏目编辑页面条件，查询所有配置模板的集合
    	List<CtpTemplate> allTempletes = this.getMyConfigCollTemplate(null, params);
    	
    	V3xOrgAccount OrgAccount = orgManager.getAccountById(orgAccountId);
    	if (!accounts.containsKey(OrgAccount.getId())) {
    		accounts.put(OrgAccount.getId(), OrgAccount.getName());
    	}
    	for (CtpTemplate template : allTempletes) {
    		if(null != template.getFormParentid()){
        		CtpTemplate pTemplate = this.getCtpTemplate(template.getFormParentid());
        		if(null == pTemplate){//个人模板的父模板不存在
        			continue;
        		}else if(null != pTemplate){//父模板不能使用
        			boolean templateEnabled = this.isTemplateEnabled(pTemplate,user.getId());
        			if(!templateEnabled || pTemplate.isDelete() || template.getState().equals(TemplateEnum.State.invalidation.ordinal())){
        				continue;
        			}
        		}
        	}
    		if(!template.getOrgAccountId().equals(orgAccountId)){
            	V3xOrgAccount outOrgAccount = orgManager.getAccountById(template.getOrgAccountId());
                if (!accounts.containsKey(outOrgAccount.getId())) {
                	accounts.put(outOrgAccount.getId(), outOrgAccount.getName());
                }
            }
    		
    	}
    	
    	return accounts;
    }
    
    @Override
    @ProcessInDataSource(name = DataSourceName.BASE)
    public void updateTempleteState(Long[] ids, int state) throws BusinessException{
    	templateDao.updateTempleteState(ids, state);
    }
    
	public List<CtpTemplate> getMyConfigCollTemplate(FlipInfo flipInfo, Map<String, Object> param)
			throws BusinessException {
		String categoryIds = (String)param.get("categoryIds");
		if(Strings.isBlank(categoryIds)){
			return new ArrayList<CtpTemplate>();
		}

        //根据首页模板栏目编辑页面条件，查询所有配置模板的集合
        Map<String, String> args = new HashMap<String, String>();
        if(param.get("categoryIds")!=null){
            args.put("categoryIds", param.get("categoryIds").toString());
        }
        if(param.get("isToMoreTree") != null){
        	args.put("isToMoreTree","true");
        }
        if (param.get("subject") != null) {
            args.put("subject", CtpTemplateUtil.unescape(param.get("subject").toString()));
        }
		/*  FlipInfo fp  = templateDao.findConfigTemplates(AppContext.currentUserId(), flipInfo, args);
		if(fp != null){
			allList = fp.getData();
		}*/
        
        List<CtpTemplate>  ts = this.findTemplatesByConfig(args,flipInfo);
        if(ts == null) {
        	return new ArrayList<CtpTemplate>();
        }
        
        List<CtpTemplate> allList = null;
        if(flipInfo!=null) {
        	allList = flipInfo.getSize()< ts.size() ? ts.subList(0, flipInfo.getSize()) : ts;
        }
        else {
        	allList = ts;
        }
        
        return allList;
	}
	
	
	
	private List<CtpTemplate>  findTemplatesByConfig(Map<String, String> params,FlipInfo flipInfo) throws BusinessException{
		
		Long memberId = AppContext.currentUserId();
		
		List<CtpTemplateConfig> templateConfigs = templateDao.findTemplateConfigs(memberId,flipInfo);
		
		List<CtpTemplate> aclTemplates = getSystemTemplatesByAcl(memberId);
		//添加个人模板
		aclTemplates.addAll(this.getPersonalTemplates(memberId));
		
		Map<Long,CtpTemplate> aclIdToTemplate = new HashMap<Long, CtpTemplate>();
		if(Strings.isNotEmpty(aclTemplates)) {
			for(CtpTemplate t : aclTemplates) {
				aclIdToTemplate.put(t.getId(), t);
			}
		}
		
		List<CtpTemplate> sortAclTemplates = new ArrayList<CtpTemplate>();
		
		if(Strings.isNotEmpty(templateConfigs)) {
			for(CtpTemplateConfig config : templateConfigs) {
				CtpTemplate t = aclIdToTemplate.get(config.getTempleteId());
				aclIdToTemplate.remove(config.getTempleteId());
				if(t != null) {
					sortAclTemplates.add(t);
				}
			}
		}
		sortAclTemplates.addAll(aclIdToTemplate.values());

        //开始过滤
		boolean onlyPersonTemplate = false;
		boolean onlySystemTemplate = false;
        boolean containGovdocSend  = false;
        boolean containGovdocRec  = false;
        boolean containGovdocSign = false;
        boolean contailCollaboration = false;
        Map<Long,Boolean> specificCategoryIds = new HashMap<Long,Boolean>(); //具体的CategoryIds
        String categoryIds = params.get("categoryIds");
        if (!StringUtil.checkNull(categoryIds)){
        	if(categoryIds.equals(String.valueOf(TemplateCategoryConstant.personRoot.key()))){
        		onlyPersonTemplate = true;
        	}
        	else {
                String[] ids = categoryIds.split(",");
                for(String s:ids){
                	if (!"".equals(s)) {
                		Long idL = Long.valueOf(s);
                		specificCategoryIds.put(idL, true);
                	}
                }
                List<Integer> _moduType = new ArrayList<Integer>();
                if(specificCategoryIds.get(Long.valueOf(TemplateCategoryConstant.edocRoot.key()))!=null){
                     containGovdocSend  = true;
                     containGovdocRec  = true;
                     containGovdocSign = true;
                }
                
                if(specificCategoryIds.get(Long.valueOf(TemplateCategoryConstant.edocSendRoot.key()) )!=null
                		||specificCategoryIds.get(Long.valueOf(TemplateCategoryConstant.govdocSendRoot.key())) !=null){
                	  containGovdocSend  = true;   
                }
                if(specificCategoryIds.get(Long.valueOf(TemplateCategoryConstant.edocRecRoot.key()))!=null
                		||specificCategoryIds.get(Long.valueOf(TemplateCategoryConstant.govdocRecRoot.key()))!=null){
                	 containGovdocRec  = true;
                }
                if(specificCategoryIds.get(Long.valueOf(TemplateCategoryConstant.edocSignRoot.key()))!=null
                		||specificCategoryIds.get(Long.valueOf(TemplateCategoryConstant.govdocSignRoot.key()))!=null){
                	 containGovdocSign = true;; 
                }
                
                if(specificCategoryIds.get(Long.valueOf(TemplateCategoryConstant.publicRoot.key()))!=null){
                	contailCollaboration =true;
                }
                
                if(specificCategoryIds.get(Long.valueOf(TemplateCategoryConstant.personRoot.key()))== null){
                	onlySystemTemplate = true;
                }
        	}
        }
		
        if(Strings.isNotEmpty(sortAclTemplates)) {
        	for(Iterator<CtpTemplate> it = sortAclTemplates.iterator();it.hasNext();) {
        		CtpTemplate t  = it.next();
        		if(t == null) {
        			it.remove();
        			continue;
        		}
        		if(!Integer.valueOf(TemplateEnum.State.normal.ordinal()).equals(t.getState())) {
        			it.remove();
        			continue;
        		}
        		String querySubject = params.get("subject");
        		if (querySubject != null && t.getSubject().indexOf(querySubject) == -1) {
                    it.remove();
                    continue;
                }
    			
        		if(t.isSystem() != null && t.isSystem()) {
    				if(onlyPersonTemplate) {
    					it.remove();
    					continue;
    				}
        		}
    			else { //个人模板
    				if(onlySystemTemplate) {
    					it.remove();
    					continue;
    				}
    			}
    			if(Integer.valueOf(ApplicationCategoryEnum.collaboration.key()).equals(t.getModuleType()) 
    					&&( contailCollaboration
    					|| specificCategoryIds.get(t.getCategoryId())!= null)) {
    				//包含
    			}else if(Integer.valueOf(ApplicationCategoryEnum.govdocSend.key()).equals(t.getModuleType()) 
    					&&( containGovdocSend
    					|| specificCategoryIds.get(t.getCategoryId())!= null)) {
    				//包含
    			}else if(Integer.valueOf(ApplicationCategoryEnum.govdocRec.key()).equals(t.getModuleType()) 
    					&&( containGovdocRec
    					|| specificCategoryIds.get(t.getCategoryId())!= null)) {
    				//包含
    			}else if(Integer.valueOf(ApplicationCategoryEnum.govdocSign.key()).equals(t.getModuleType()) 
    					&&( containGovdocSign
    					|| specificCategoryIds.get(t.getCategoryId())!= null)) {
    				//包含
    			}else if(Integer.valueOf(ApplicationCategoryEnum.info.key()).equals(t.getModuleType())
    					&&(specificCategoryIds.get(t.getCategoryId())!= null)) {
    				//包含
    			}else {
        			it.remove();
        		}
        		
				/*if(s)
				    boolean containGovdocSend  = false;
				boolean containGovdocRec  = false;
				boolean containGovdocSign = false;
				boolean contailCollaboration = false;*/
        	}
        }
      return sortAclTemplates;
	}
	
	
    //获取模板清单样式所有模板信息
    private List<TemplateBO> getSectionShowTemplate(Map<String,Object> params) throws BusinessException{
		User user = AppContext.getCurrentUser();
		
		//登陆单位或者选择的单位ID
		Long orgAccountId = (null==params.get("accountId") ? user.getLoginAccount() : (Long)params.get("accountId"));
		String isShowTemplates = (null==params.get("isShowTemplates") ? "true" : (String)params.get("isShowTemplates"));
		
		//合并权限
        this.transMergeCtpTemplateConfig(user.getId());
		//根据首页模板栏目编辑页面条件，查询所有配置模板的集合
    	List<CtpTemplate> allTempletes = this.getMyConfigCollTemplate(null, params);
    	
    	allTempletes = this.addOrgIntoTempalte(allTempletes);
        List<TemplateBO> showTemplates = new ArrayList<TemplateBO>();//返回的模板对象
        for (CtpTemplate template : allTempletes) {
        	if(null != template.getFormParentid()){
        		CtpTemplate pTemplate = this.getCtpTemplate(template.getFormParentid());
        		if(null == pTemplate){//个人模板的父模板不存在
        			continue;
        		}else if(null != pTemplate){//父模板不能使用
        			boolean templateEnabled = this.isTemplateEnabled(pTemplate,user.getId());
        			if(!templateEnabled || pTemplate.isDelete() || template.getState().equals(TemplateEnum.State.invalidation.ordinal())){
        				continue;
        			}
        		}
        	}
        	if(!template.getOrgAccountId().equals(orgAccountId)){
        		if("false".equals(isShowTemplates)){//单位切换的时候，是否显示其他单位模板
        			continue;
        		}
        	}
        	
        	TemplateBO templateBO = new TemplateBO(template);
        	templateBO.setSubject(template.getSubject());
        	
            if (!template.getOrgAccountId().equals(user.getLoginAccount())) {
            	V3xOrgAccount outOrgAccount = orgManager.getAccountById(template.getOrgAccountId());
            	templateBO.setSubject(template.getSubject()+"("+outOrgAccount.getShortName()+")");
            }
            //栏目分类
            CtpTemplateCategory categorybyId = getCategorybyId(template.getCategoryId());
            if(!template.isSystem()){
				templateBO.setCategoryName(ResourceUtil.getString("template.templatePub.personalTemplates"));
			}else if(null != categorybyId){
				templateBO.setCategoryName(categorybyId.getName());
			}
            showTemplates.add(templateBO);
        }
        
        return showTemplates;
	}

	@Override
	public void saveCtpTemplateOrgs(List<CtpTemplateOrg> ctpTemplateOrgs) throws BusinessException {
		DBAgent.saveAll(ctpTemplateOrgs);
	}

	@Override
	public void deleteCtpTemplateOrgByTemplateId(Long templateId) throws BusinessException {
		templateDao.deleteCtpTemplateOrgByTemplateId(templateId);
	}

	@Transactional(propagation = Propagation.SUPPORTS, rollbackFor = com.seeyon.ctp.common.exceptions.BusinessException.class)
	@Override
	public List<CtpTemplate> addOrgIntoTempalte(List<CtpTemplate> templates) throws BusinessException {
		if(Strings.isEmpty(templates)){
			return templates;
		}
		
		List<Long> templateIds = new ArrayList<Long>();
		for(CtpTemplate template : templates){
			templateIds.add(template.getId());
		}
		
		StringBuilder sql = new StringBuilder();
    	sql.append(" from CtpTemplateOrg cto where 1 = 1 ");
    	Map<String, Object> queryParams = new HashMap<String, Object>();
    	
    	List<Long>[] ids = Strings.splitList(templateIds, 999);
		List<CtpTemplateOrg> list = null;
    	if(ids != null){
    		if( ids.length >1){
			    //超过999直接查询所有数据中的前1万条数据
			    FlipInfo fi = new FlipInfo(1,10000);
			    list = DBAgent.find(sql.toString(), queryParams,fi);
		    }else{
    			//不超过999就拼接查询
			    sql.append(" and( ");
			    for(int i = 0 ; i < ids.length ; i++){
				    if(i != 0){
					    sql.append(" or ");
				    }
				    sql.append(" (cto.templateId in (:templateIds_" + i + ")) ");
				    queryParams.put("templateIds_" + i, ids[i]);
			    }
			    sql.append(" ) ");
			    list = DBAgent.find(sql.toString(), queryParams);
		    }
	    }
    	
    	Long teplateId;
    	CtpTemplateOrg org;
    	String intoInfo; //追加的信息
    	for(CtpTemplate template : templates){
    		teplateId = template.getId();
    		
    		for(Iterator<CtpTemplateOrg> it = list.iterator(); it.hasNext();){
    			org = it.next();
    			if(teplateId.equals(org.getTemplateId())){
    				intoInfo = org.getOrgId() + "_" + org.getOrgName() + "_" + org.getOrgType();
    				if(TemplateEnum.DataType.R.ordinal() == org.getDataType()){
    					template.setResponsible((Strings.isNotBlank(template.getResponsible())?template.getResponsible()+"|":"") + intoInfo);
    				}else if(TemplateEnum.DataType.A.ordinal() == org.getDataType()){
    					template.setAuditor((Strings.isNotBlank(template.getAuditor())?template.getAuditor()+"|":"") + intoInfo);
    				}else if(TemplateEnum.DataType.C.ordinal() == org.getDataType()){
    					template.setConsultant((Strings.isNotBlank(template.getConsultant())?template.getConsultant()+"|":"") + intoInfo);
    				}else if(TemplateEnum.DataType.I.ordinal() == org.getDataType()){
    					template.setInform((Strings.isNotBlank(template.getInform())?template.getInform()+"|":"") + intoInfo);
    				}else if(TemplateEnum.DataType.userOrg.ordinal() == org.getDataType()){
    					template.setCoreUseOrg((Strings.isNotBlank(template.getCoreUseOrg())?template.getCoreUseOrg()+"|":"") + intoInfo);
    				}
    				
    				it.remove();
    			}
    		}
    	}
		return templates;
	}
    @Override
    public List<CtpTemplateHistory> getCtpTemplateHistory(FlipInfo flipInfo, Map<String, String> params)
            throws BusinessException{
       return templateDao.getCtpTemplateHistory(flipInfo,params);
    }
	@Override
	public CtpTemplate addOrgIntoTempalte(CtpTemplate template) throws BusinessException {
		
		if(Strings.isNotBlank(template.getResponsible()) || Strings.isNotBlank(template.getAuditor())
				|| Strings.isNotBlank(template.getConsultant()) || Strings.isNotBlank(template.getInform())
				|| Strings.isNotBlank(template.getCoreUseOrg())){
			return template;
		}
		StringBuilder sql = new StringBuilder();
    	sql.append(" from CtpTemplateOrg cto where cto.templateId =:templateId ");
    	Map<String, Object> queryParams = new HashMap<String, Object>();
    	queryParams.put("templateId", template.getId());
    	List<CtpTemplateOrg> list = DBAgent.find(sql.toString(), queryParams);
    	
    	Long teplateId;
    	String intoInfo =""; //追加的信息
    	CtpTemplateOrg org;
    	if(Strings.isNotEmpty(list)){
    		for(int a =0; a < list.size(); a ++) {
				    org = list.get(a);
    			    intoInfo = org.getOrgId() + "_" + org.getOrgName() + "_" + org.getOrgType()+"|";
    				if(TemplateEnum.DataType.R.ordinal() == org.getDataType()){
    					template.setResponsible(template.getResponsible() + intoInfo);
    				}else if(TemplateEnum.DataType.A.ordinal() == org.getDataType()){
    					template.setAuditor(template.getAuditor() + intoInfo);
    				}else if(TemplateEnum.DataType.C.ordinal() == org.getDataType()){
    					template.setConsultant(template.getConsultant() + intoInfo);
    				}else if(TemplateEnum.DataType.I.ordinal() == org.getDataType()){
    					template.setInform(template.getInform() + intoInfo);
    				}else if(TemplateEnum.DataType.userOrg.ordinal() == org.getDataType()){
    					template.setCoreUseOrg(template.getCoreUseOrg() + intoInfo);
    				}
    		}
    	    if(Strings.isNotBlank(template.getResponsible())&&template.getResponsible().endsWith("[|]")){
    	    	template.setResponsible(template.getResponsible().substring(0, template.getResponsible().length()-1));
    	    }
    	    if(Strings.isNotBlank(template.getAuditor())&&template.getAuditor().endsWith("[|]")){
    	    	template.setAuditor(template.getAuditor().substring(0, template.getAuditor().length()-1));
    	    }
    	    if(Strings.isNotBlank(template.getConsultant())&&template.getConsultant().endsWith("[|]")){
    	    	template.setConsultant(template.getConsultant().substring(0, template.getConsultant().length()-1));
    	    }
    	    if(Strings.isNotBlank(template.getInform())&&template.getInform().endsWith("[|]")){
    	    	template.setInform(template.getInform().substring(0, template.getInform().length()-1));
    	    }
    	    if(Strings.isNotBlank(template.getCoreUseOrg())&&template.getCoreUseOrg().endsWith("[|]")){
    	    	template.setCoreUseOrg(template.getCoreUseOrg().substring(0, template.getCoreUseOrg().length()-1));
    	    }
    		
    	}
    	return template;
	}
	
	@Override
	public List<CtpTemplateOrg> bulidCtpTemplateOrgList(CtpTemplate c) throws BusinessException {
		List<CtpTemplateOrg> list = new ArrayList<CtpTemplateOrg>();
		CtpTemplateOrg  org = null;
		if(Strings.isNotBlank(c.getResponsible())){
			list.addAll(bulidCtpTemplateOrgList(c.getId(), c.getResponsible(), DataType.R));
		}
		if(Strings.isNotBlank(c.getAuditor())){
			list.addAll(bulidCtpTemplateOrgList(c.getId(), c.getAuditor(), DataType.A));
		}
		if(Strings.isNotBlank(c.getConsultant())){
			list.addAll(bulidCtpTemplateOrgList(c.getId(), c.getConsultant(), DataType.C));
		}
		if(Strings.isNotBlank(c.getInform())){
			list.addAll(bulidCtpTemplateOrgList(c.getId(), c.getInform(), DataType.I));
		}
		if(Strings.isNotBlank(c.getCoreUseOrg())){
			list.addAll(bulidCtpTemplateOrgList(c.getId(), c.getCoreUseOrg(), DataType.userOrg));
		}
		return list;
	}

	@Override
	public List<CtpTemplateOrg> bulidCtpTemplateOrgListHistory(CtpTemplateHistory c) throws BusinessException {
		List<CtpTemplateOrg> list = new ArrayList<CtpTemplateOrg>();
		CtpTemplateOrg  org = null;
		if(Strings.isNotBlank(c.getResponsible())){
			list.addAll(bulidCtpTemplateOrgList(c.getId(), c.getResponsible(), DataType.R));
		}
		if(Strings.isNotBlank(c.getAuditor())){
			list.addAll(bulidCtpTemplateOrgList(c.getId(), c.getAuditor(), DataType.A));
		}
		if(Strings.isNotBlank(c.getConsultant())){
			list.addAll(bulidCtpTemplateOrgList(c.getId(), c.getConsultant(), DataType.C));
		}
		if(Strings.isNotBlank(c.getInform())){
			list.addAll(bulidCtpTemplateOrgList(c.getId(), c.getInform(), DataType.I));
		}
		if(Strings.isNotBlank(c.getCoreUseOrg())){
			list.addAll(bulidCtpTemplateOrgList(c.getId(), c.getCoreUseOrg(), DataType.userOrg));
		}
		if(list.size() == 0){
            list = templateDao.getCtpTemplateOrgByTempalteId(c.getId());
        }
		return list;
	}


	public List<CtpTemplateOrg> bulidCtpTemplateOrgList(Long templateId,String str,TemplateEnum.DataType type) throws BusinessException {
		List<CtpTemplateOrg> list = new ArrayList<CtpTemplateOrg>();
		if(Strings.isNotBlank(str)){
			String[] split = str.split("[|]");
			CtpTemplateOrg org = null;
    		for(int a= 0; a < split.length ;a ++){
    			org = new CtpTemplateOrg();
    			String sigle = split[a];
    			String[] split2 = sigle.split("_");
    			org.setIdIfNew();
    			org.setTemplateId(templateId);
    			org.setOrgId(split2[0]);
    			org.setOrgName(split2[1]);
    			org.setOrgType(split2[2]);
    			org.setDataType(type.ordinal());
    			list.add(org);
    		}
		}
		return list;
	}
	public String getEnumShowName(String code,String itemValue){
		CtpEnumBean enumByProCode = em.getEnumByProCode(code);
		if(null != enumByProCode){
			String itemLabel = enumByProCode.getItemLabel(itemValue);
			return ResourceUtil.getString(itemLabel);
		}
		return "";
	}

    @Override
    @ProcessInDataSource(name = DataSourceName.BASE)
    public void deleteCtpTemplatesPhysicallyByFormAppId(Long formAppId) throws BusinessException {
        templateDao.deleteCtpTemplatesPhysicallyByFormAppId(formAppId);
        LinkedList<Criterion> list  =new LinkedList<Criterion>();
        list.add(Restrictions.eq("formAppId", formAppId));
        List<CtpTemplate>  templates =  getTemplatesByParam(list);
        if(Strings.isNotEmpty(templates)){
            for(CtpTemplate template:templates){
                templateCacheManager.deleteCacheTemplate(template);
            }
        }
    }
    
    @Override
    public String getPanelCategory4Portal(Map<String, String> preference ) {
    	String panel = SectionUtils.getPanel("all", preference);
    	StringBuilder categoryIds = new StringBuilder();
        List<Long> moduleTypes = new ArrayList<Long>();
    	if ("all".equals(panel)) {
        	this.getModuleTypes4Portal(moduleTypes);
            // 全部
        } else if("template_catagory".equals(panel)){
            String panelCategory = preference.get(panel + "_value");
            //如果为空 ，查询所有能查询的
            if(Strings.isBlank(panelCategory.toString())){
            	this.getModuleTypes4Portal(moduleTypes);
            } else {
            	/****** 解析选择的模板分类，包含子子孙孙 begin******/
            	panelCategory = panelCategory.replaceAll("C_", "");
                //获取所选分类的子分类
                LOG.info("----categoryIds:"+panelCategory);
           	 	List<CtpTemplateCategory> alls = getAllCategorys(panelCategory);
                StringBuilder allIds = new StringBuilder();
                allIds.append(panelCategory);
                //所有模板分类Id
                List<Long> allCategoryList = new ArrayList<Long>();
                if(Strings.isNotEmpty(alls)){
                    for(CtpTemplateCategory cate : alls){
                        Long cateId = cate.getId();
                        if (!allCategoryList.contains(cateId)) {
                        	if(Strings.isNotBlank(allIds.toString())){
                                allIds.append(",");
                            }
                        	allCategoryList.add(cateId);
                        	allIds.append(cateId);
                        }
                    }
                }
                categoryIds = allIds;
                /****** 解析选择的模板分类，包含子子孙孙 end******/
            }
        } else if("track_catagory".equals(panel)){
            String tempStr = preference.get(panel + "_value");
            //如果为空 ，查询所有能查询的
            if(Strings.isBlank(tempStr)){
            	this.getModuleTypes4Portal(moduleTypes);
            }else{
                String[] temList = tempStr.split(",");
                for (String s : temList) {
                    if ("catagory_coll".equals(s)) {
                        //个人模板
                    	moduleTypes.add(Long.valueOf(TemplateCategoryConstant.personRoot.key()));
                    } else if ("catagory_collOrFormTemplete".equals(s)) {
                    	if(AppContext.hasPlugin("collaboration")){
                    		//协同和表单模板
                        	moduleTypes.add(Long.valueOf(TemplateCategoryConstant.publicRoot.key()));
                    	}
                    } else if ("catagory_edoc".equals(s)) {
                    	if(AppContext.hasPlugin("edoc")){
                    		//公文模板
                    		moduleTypes.add(Long.valueOf(TemplateCategoryConstant.edocRoot.key()));
                    	}
                    }
                }
            }
        }
        
        int i = 0;
        for (Long moduleType : moduleTypes) {
        	if (i != 0) {
        		categoryIds.append(",");
        	}
        	categoryIds.append(String.valueOf(moduleType));
        	i++;
        }
        String categoryId = categoryIds.toString();
        
        return categoryId;
    }

    @Override
    public void getModuleTypes4Portal(List<Long> moduleTypes) {
    	moduleTypes.add(Long.valueOf(TemplateCategoryConstant.personRoot.key()));
    	moduleTypes.add(Long.valueOf(ModuleType.info.getKey()));
    	if(AppContext.hasPlugin("collaboration")){
    		moduleTypes.add(Long.valueOf(TemplateCategoryConstant.publicRoot.key()));
    	}
    	if(AppContext.hasPlugin("edoc")){
    		moduleTypes.add(TemplateCategoryConstant.edocRoot.key());
    	}
    	if(AppContext.hasPlugin("govdoc")){
    		moduleTypes.add(TemplateCategoryConstant.govdocSendRoot.key());
    		moduleTypes.add(TemplateCategoryConstant.govdocRecRoot.key());
    		moduleTypes.add(TemplateCategoryConstant.govdocSignRoot.key());
    	}
    }
    
    @Override
	public List<CtpTemplateCategory> getAllCategorys(String s){
        if(Strings.isBlank(s)){
            return new ArrayList<CtpTemplateCategory>();
        }
        String[] arr = s.split(",");
        List<CtpTemplateCategory> resultList = new ArrayList<CtpTemplateCategory>();
        for(String id : arr){
        	if (String.valueOf(TemplateCategoryConstant.publicRoot.key()).equals(id)
        			|| String.valueOf(TemplateCategoryConstant.edocRoot.key()).equals(id)
        			|| String.valueOf(TemplateCategoryConstant.personRoot.key()).equals(id) || "32".equals(id)) {
        		continue;
        	}
            CtpTemplateCategory category = null;
            try {
                category = this.getCategoryIncludeAllChildren(Long.valueOf(id));
                //查询所有子分类，直至末级分类
                List<CtpTemplateCategory>  children = this.getCtpTemplateCategoryChildren(category, false);
                if (category!=null) {
                    resultList.add(category);
                    resultList.addAll(children);
                }
            } catch (Exception e) {
            	LOG.error("",e);
            } 
        }
        return resultList;
    }

	@Override
	@AjaxAccess
	public boolean isCap4Template(Long templateId) throws BusinessException {
		boolean isCap4Template = false;
		if(null==templateId){
			return isCap4Template;
		}
		CtpTemplate template = getCtpTemplate(templateId);
		if(null != template && String.valueOf(MainbodyType.FORM.getKey()).equals(template.getBodyType())){
			isCap4Template = capFormManager.isCAP4Form(template.getFormAppId());
		}
		return isCap4Template;
	}

	@Override
	public void deleteCtpTemplateOrgById(Long id) throws BusinessException {
		templateDao.deleteCtpTemplateOrgById(id);
	}

	@Override
	public void deleteCtpTemplateOrgByTemplateIds(List<Long> templateIds) throws BusinessException {
		if(Strings.isEmpty(templateIds)){
			return;
		}
		
		templateDao.deleteCtpTemplateOrgByTemplateIds(templateIds);
	}

	@Override
	public void deleteCtpTemplateAuthByTemplateIds(List<Long> templateIds) throws BusinessException {
		if(Strings.isEmpty(templateIds)){
			return;
		}
		
		Long[] ids = new Long[templateIds.size()];
		templateIds.toArray(ids);
		
		templateDao.deleteCtpTemplateAuths(ids, null);

        templateCacheManager.deleteCacheTemplateAuthByTemplateIds(templateIds);

	}

	@Override
	public TemplateApprovePO getTemplateApproveByHistoryId(Long templateHistoryId) throws BusinessException {
		// TODO Auto-generated method stub
		return templateApproveDao.getByTemplateHistoryId(templateHistoryId);
	}

	@Override
	public void updateTempleteApprove(TemplateApprovePO approve) throws BusinessException {
		// TODO Auto-generated method stub
		templateApproveDao.updateTemplateApprove(approve);
	}
	public List<CtpTemplateAuth> getCtpTemplateAuths(Long moduleId, Integer moduleType,Long accountId) throws BusinessException {
		return templateDao.getCtpTemplateAuths(moduleId, moduleType, accountId);
	}
	@Override
	public void saveOrUpdateTempleteHistory(CtpTemplateHistory ctpTemplate) throws BusinessException {
		templateDao.saveOrUpdateCtpTemplateHistory(ctpTemplate);
	}

	@Override
	public void synchronizeTemplateAuthCache(CtpTemplate template, List<CtpTemplateAuth> auths) {
		templateCacheManager.synchronizeTemplateAuthCache(template, auths);
	}

 

	/* (non-Javadoc)
	 * @see com.seeyon.ctp.common.template.manager.TemplateManager#deleteCtpTempleteHistoryByTemplateId(java.lang.Long)
	 */
	@Override
	public void deleteCtpTempleteHistoryByTemplateId(Long templateId) throws BusinessException {

		templateDao.deleteCtpTempleteHistoryByTemplateId(templateId);
		
	}
    @ProcessInDataSource(name = DataSourceName.BASE)
    @Override
    public void updateTemplete(CtpTemplate ctpTemplate) throws BusinessException {
        templateDao.updateTemplete(ctpTemplate);
    }
    public void deleteCtpTemplateAuths(Long[] moduleIds, Integer moduleType) throws BusinessException{
    	templateDao.deleteCtpTemplateAuths(moduleIds, moduleType);
    }
    
    public List<CtpTemplateCategory> getCategoryByAuth(Long accountId, int type) throws BusinessException {
        User user = AppContext.getCurrentUser();
    	List<ModuleType> _type = new ArrayList<ModuleType>();
    	_type.add(ModuleType.getEnumByKey(type));
        List<CtpTemplateCategory> templateCategorys = getCategorys(accountId, _type);
        return checkCategoryAuth(accountId, templateCategorys, user.getId());
    }
    
    public List<CtpTemplateCategory> getCategoryByAuth(Long accountId) throws BusinessException {
        User user = AppContext.getCurrentUser();
        List<CtpTemplateCategory> templateCategorys = getCategorys(accountId,ModuleType.collaboration);
        
        if(Strings.isNotEmpty(templateCategorys)){
        	LOG.info("分类数量1="+templateCategorys.size());
        }
        return checkCategoryAuth(accountId, templateCategorys, user.getId());
    }
    
    @Override
    public CtpTemplateHistory createTemplatePublishJob(CtpTemplateHistory c) throws MutiQuartzJobNameException, NoSuchQuartzJobBeanException {
        return TemplatePublishQuartz.createTemplatePublishJob(c);
    }
	public Map<String, String>  getCtpCategoriesByAuth(Map<String, String> params, Long accountId, Long memberId)
			throws BusinessException {
		long orgAccountId = accountId;
        List<ModuleType> _treeModuleTypes = new ArrayList<ModuleType>();
        ModuleType _treeModuleType = ModuleType.collaboration;
        if(Strings.isBlank(params.get("categoryId"))){
        	if(params.get("categoryType")!=null){
        		String _c = params.get("categoryType");
        		_treeModuleType = ModuleType.getEnumByKey(Integer.valueOf(_c == null ? "1" : _c));
        		_treeModuleTypes.add(_treeModuleType);
        	}
        	List<CtpTemplateCategory> categorys = this.getCategorysByAuth(orgAccountId,_treeModuleTypes, memberId);
        	List<Long> cid = new ArrayList<Long>();
        	for(CtpTemplateCategory c : categorys){
        		cid.add(c.getId());
        	}
        	if(Strings.isNotEmpty(cid)){
        		params.put("categoryId", Strings.join(cid, ","));
        	}
        }
        return params;
	}
	
    public FlipInfo findAccountAdminTemplates(FlipInfo flipInfo, Map<String, String> params) throws BusinessException {
        
        User user = AppContext.getCurrentUser();
        
        params.put("delete", "false");
        if (params.get("subject") != null) {
            params.put("subject", CtpTemplateUtil.unescape(params.get("subject")));
        }
        if (params.get("member") != null) {
            List<V3xOrgMember> members = orgManager.getMemberByIndistinctName(params.get("member"));
            StringBuilder sb = new StringBuilder("-1");
            if (!CollectionUtils.isEmpty(members)) {
                for (V3xOrgMember v3xOrgMember : members) {
                    sb.append(",");
                    sb.append(v3xOrgMember.getId());
                }
            }
            params.put("memberId", sb.toString());
        }
        // 默认不查询表单正文的模板
        String bodyType = "10,30,41,42,43,44,45";
        params.put("bodyType", bodyType);
       
        
        // 协同模板管理默认显示全部模板
        if("1".equals(params.get("categoryId"))){
            params.remove("categoryId");
        }
        boolean needSearchCategory = true;
        // 公文模板管理默认显示全部模板
        if("4".equals(params.get("categoryType"))){
        	needSearchCategory =false;
            params.put("categoryType", "401,402,404");
            params.put("categoryId", "401,402,404");
            
        }
        if("32".equals(params.get("categoryType"))){
        	needSearchCategory = false;
        	params.put("categoryType", "32");
            params.put("categoryId", "32");
        }
        if(needSearchCategory){
        	params = getCtpCategoriesByAuth(params, user.getLoginAccount(), user.getId());
        }
        
        
        templateDao.selectAllSystemTempletes(flipInfo, params, user.getLoginAccount());
        
        List<CtpTemplate> result = flipInfo.getData();
        List<TemplateBO> resultBO = new ArrayList<TemplateBO>();
        if (result != null) {
            TemplateBO bo = null;
            String[] results = null;
            V3xOrgMember member = null;
            V3xOrgAccount  account = orgManager.getAccountById(AppContext.currentAccountId()) ;
            for (CtpTemplate ctpTemplate : result) {
                // 是否有模板所属分类的权限
                if (ctpTemplate.getModuleType() == ModuleType.edocRec.getKey()
                        || ctpTemplate.getModuleType() == ModuleType.edocSend.getKey()
                        || ctpTemplate.getModuleType() == ModuleType.edocSign.getKey()
                        || ctpTemplate.getModuleType() == ModuleType.collaboration.getKey()) {
                    bo = new TemplateBO(ctpTemplate);
                    results = getTemplateAuth(ctpTemplate);
                    bo.setAuth(results[0]);
                    bo.setAuthValue(results[1]);
                    bo.setHasAttsFlag(CtpTemplateUtil.isHasAttachments(ctpTemplate));
                    member = orgManager.getMemberById(ctpTemplate.getMemberId());
                    if (member != null) {
                        if (orgManager.isAdministratorById(member.getId(),account)) {
                            bo.setCreaterName(ResourceUtil.getString("template.unit.manager"));
                        } else {
                            bo.setCreaterName(member.getName());
                        }
                    }
                    resultBO.add(bo);
                }
                if(ctpTemplate.getModuleType() == ModuleType.info.getKey()){
                	 bo = new TemplateBO(ctpTemplate);
                	 results = getTemplateAuth(ctpTemplate);
                	 bo.setAuth(results[0]);
                     bo.setAuthValue(results[1]);
                     bo.setHasAttsFlag(CtpTemplateUtil.isHasAttachments(ctpTemplate));
                     try{
                    	 bo.setCreateUnit(account.getName());
                     }catch(Exception e){
                         LOG.error("", e);
                     }
                	 resultBO.add(bo);
                }
            }
        }
        
        flipInfo.setData(resultBO);
        return flipInfo;
    }
    /* (non-Javadoc)
     * @see com.seeyon.apps.template.manager.CollaborationTemplateManager#saveCategory(com.seeyon.apps.template.vo.TemplateCategory)
     */
   
    @Override
    public void updatePrivAuth(String memberIds,List<String> delarrayList) throws BusinessException {
        List<String> authAllList = new ArrayList<String>();
        List<CtpTemplateAuth> ctpTemplateAuths = getCtpTemplateAuths(null, -1,AppContext.currentAccountId());
        if(Strings.isNotEmpty(ctpTemplateAuths)){
        	for(int count = 0 ; count <ctpTemplateAuths.size(); count ++){
        		CtpTemplateAuth ctAuth = ctpTemplateAuths.get(count);
        		authAllList.add("Member|"+ctAuth.getAuthId());
        	}
        }
        String delStr = "";
        if(null != delarrayList){
        	for(int a = 0 ; a < delarrayList.size(); a++){
        		if(!authAllList.contains(delarrayList.get(a))){
        			delStr += delarrayList.get(a)+",";
        		}
        	}
        }
        if(delStr.length() > 0){
        	delStr = delStr.substring(0,delStr.length()-1);
        }
        // 模板管理员角色
        V3xOrgRole role = orgManager.getRoleByName(OrgConstants.Role_NAME.TtempletManager.name(),AppContext.currentAccountId());

        // 如果当前单位不存在此角色
        if(role == null){
            role = orgManager.getRoleByName(OrgConstants.Role_NAME.TtempletManager.name(),null);
            role.setOrgAccountId(AppContext.currentAccountId());
            role.setId(UUIDLong.longUUID());
            role.setCode(String.valueOf(role.getId()));
            orgManagerDirect.addRole(role);
        }
        //增加
        if(Strings.isNotBlank(memberIds)){
        	if(memberIds.length() > 0){
        		String[] memberAdd = memberIds.split(",");
        		for(int a = 0 ; a < memberAdd.length; a ++){
        			if(!orgManager.isRole(Long.valueOf(memberAdd[a].split("[|]")[1]), AppContext.getCurrentUser().getLoginAccount(),
        			    OrgConstants.Role_NAME.TtempletManager.name(),OrgConstants.MemberPostType.Main)){
        				V3xOrgEntity entity = orgManager.getEntity(memberAdd[a]);
        				orgManagerDirect.addRole2Entity(role.getId(),  AppContext.currentAccountId(),entity);
        			}
        		}
        	}
        }
        //删除
        if(delStr.length() > 1){
        	roleManager.delRole2Entity(role.getName(), AppContext.currentAccountId(), delStr);
        }
    }
    public List<CtpTemplateCategory> getSubCategorys(Long accountId, Long id) throws BusinessException {
        List<CtpTemplateCategory> result = new UniqueList<CtpTemplateCategory>();
        List<ModuleType> intList = new ArrayList<ModuleType>();
        intList.add(ModuleType.collaboration);
        intList.add(ModuleType.form);
        List<CtpTemplateCategory> templeteCategories = getCategorys(accountId, intList);
        if (templeteCategories != null) {
            for (CtpTemplateCategory ctpTemplateCategory : templeteCategories) {
                if (ctpTemplateCategory.getParentId() != null && ctpTemplateCategory.getParentId().equals(id)) {
                    result.add(ctpTemplateCategory);
                }
            }
        }
        return result;
    }

    @AjaxAccess
    @Override
    public FlipInfo findConfigTemplates(FlipInfo flipInfo, Map<String, String> params) throws BusinessException {
        if (params.get("subject") != null) {
            params.put("subject", CtpTemplateUtil.unescape(params.get("subject")));
        }
        return templateDao.findConfigTemplates(AppContext.currentUserId(), flipInfo, params);
    }
    @Override
	public void replaceTemplateAuth(List<Long> categoryIds, Long oldUserId, Long newUserId) throws BusinessException {
    	List<CtpTemplateAuth> auths = templateDao.getTemplateAuths(categoryIds, null, null);
    	if(Strings.isEmpty(auths)|| oldUserId==null || newUserId==null){
    		return ;
    	}
    	V3xOrgMember member = orgManager.getMemberById(newUserId);
    	if(member==null){
    		return;
    	}
    	Map<String,CtpTemplateAuth> authMap = new HashMap<String,CtpTemplateAuth>();
    	
    	for (CtpTemplateAuth ctpTemplateAuth : auths) {
			String key = ctpTemplateAuth.getAuthType()+"_"+ctpTemplateAuth.getModuleId()+"_"+ctpTemplateAuth.getAuthId();
			authMap.put(key, ctpTemplateAuth);
		}
    	
    	List<CtpTemplateAuth> needUpdateAuth = new ArrayList<CtpTemplateAuth>();
    	List<CtpTemplateAuth> needDeleteAuth = new ArrayList<CtpTemplateAuth>();
		for (CtpTemplateAuth ctpTemplateAuth : auths) {
			if(ORGENT_TYPE.Member.name().equals(ctpTemplateAuth.getAuthType()) && oldUserId.equals(oldUserId)){
				String key = ORGENT_TYPE.Member.name()+"_"+ctpTemplateAuth.getModuleId()+"_"+newUserId;
				//此分类中包含了新的这个人的授权无需添加，删除旧的分类授权即可
				if(authMap.get(key)!=null){
					needDeleteAuth.add(ctpTemplateAuth);
				}else{
					//更新之前人员的授权
					ctpTemplateAuth.setAuthId(newUserId);
					ctpTemplateAuth.setAccountId(member.getOrgAccountId());
					needUpdateAuth.add(ctpTemplateAuth);
				}
			}
		}
		
		if(Strings.isNotEmpty(needUpdateAuth)){
			templateDao.updateCtpTemplateAuth(needUpdateAuth);
		}
		if(Strings.isNotEmpty(needDeleteAuth)){
			templateDao.deleteCtpTemplateAuth(auths);
		}
		
		//新的人员所在单位是否包含模板管理员角色
		V3xOrgRole role = orgManager.getRoleByName(OrgConstants.Role_NAME.TtempletManager.name(),member.getOrgAccountId());
		
		//如果当前单位不存在此角色
		if(role == null){
			role = orgManager.getRoleByName(OrgConstants.Role_NAME.TtempletManager.name(),null);
			role.setOrgAccountId(member.getOrgAccountId());
			role.setId(UUIDLong.longUUID());
			role.setCode(String.valueOf(role.getId()));
			orgManagerDirect.addRole(role);
		}
		
		//替换人员在所在单位是否是模板管理员，如果不是新增为模板管理员的角色
		if(!orgManager.isRole(newUserId, member.getOrgAccountId(),
				OrgConstants.Role_NAME.TtempletManager.name(),OrgConstants.MemberPostType.Main)){
			orgManagerDirect.addRole2Entity(role.getId(),  member.getOrgAccountId(),member);
		}
		
		V3xOrgMember oldMember = orgManager.getMemberById(oldUserId);
        if(oldMember==null){
        	return;
        }
        roleManager.delRole2Entity(OrgConstants.Role_NAME.TtempletManager.name(), oldMember.getOrgAccountId(), oldUserId.toString());

	}


    /**
     * 获取所有的模板数量
     * @param accountId
     * @return
     */
    @Override
    public Integer getAllTemplateCountByAccountId(Long accountId) {
        return templateDao.getAllTemplateCountByAccountId(accountId);
    }
    
    @Override
    @ProcessInDataSource(name = DataSourceName.BASE)
	public void updateTemplateSubStateByFormId(Long fromId, int subState) throws BusinessException {
		templateDao.updateTemplateSubStateByFormId(fromId, subState);
	}
    
    @Override
	public List<Long> getMaxVersionWorkflowIdByTemplateWorkflowIds(List<Long> workflowIds) {
		List<CtpTemplate> templates = new ArrayList<CtpTemplate>();
		try {
			templates = this.getCtpTemplateByWorkFlowIds(workflowIds);
		} catch (BusinessException e) {
			LOG.error("", e);
		}
		List<Long> templateIds = new ArrayList<Long>();
		
		if(Strings.isEmpty(templates)){
			return Collections.emptyList();
		}
		
		for (CtpTemplate ctpTemplate : templates) {
			templateIds.add(ctpTemplate.getId());
		}
		List<CtpTemplateHistory> histrorys = templateDao.getTemplateHistoryByTemplateIds(templateIds);
		if(Strings.isEmpty(histrorys)){
			return Collections.emptyList();
		}
		//history中的流程id
		List<Long> hisWorkflowIds = new ArrayList<Long>();
		//用于过滤非最新的版本
		Set<Long> addMaxVersionTemplateIds= new HashSet<Long>();
		for (CtpTemplateHistory ctpTemplateHistory : histrorys) {
			if(!addMaxVersionTemplateIds.contains(ctpTemplateHistory.getTemplateId())){
				hisWorkflowIds.add(ctpTemplateHistory.getWorkflowId());
				addMaxVersionTemplateIds.add(ctpTemplateHistory.getTemplateId());
			}
		}
		return hisWorkflowIds;
	}
    
    @Override
    public CtpTemplateHistory getCtpTemplateHistoryByTemplateIdAndVison(Long templateId, Integer version) {
    	
    	return templateDao.getCtpTemplateHistoryByTemplateIdAndVison(templateId, version);
    }

	public FormApi4Cap3 getFormApi4Cap3() {
		return formApi4Cap3;
	}

	public void setFormApi4Cap3(FormApi4Cap3 formApi4Cap3) {
		this.formApi4Cap3 = formApi4Cap3;
	}

	public FormApi4Cap4 getFormApi4Cap4() {
		return formApi4Cap4;
	}

	public void setFormApi4Cap4(FormApi4Cap4 formApi4Cap4) {
		this.formApi4Cap4 = formApi4Cap4;
	}

    public  CtpTemplateHistory cloneAndSaveTemplateToHistory(CtpTemplate template)
			throws BPMException, BusinessException {
    	
		CtpTemplateHistory history;
		Long newProcessId = wapi.cloneAndSaveProcessTemplate(String.valueOf(template.getWorkflowId()));
		
		history = new CtpTemplateHistory();
		BeanUtils.convert(history,template);//拷贝不上的属性需要手动设置
		history.setDelete(template.isDelete());
		history.setSystem(template.isSystem());
		history.setNewId();
		history.setTemplateId(template.getId());
		history.setWorkflowId(newProcessId);
		
		saveCtpTemplateHistory(history);
		
		return history;
	}
    
    @Override
    public List<Long> findTempleteIdByIds(List<Long> ids) throws BusinessException {
    	return templateDao.findTempleteIdByIds(ids);
    }

	@Override
	public void deletePhysicalCtpTemplateById(Long id) throws BusinessException {
		if(id==null) {
			return;
		}
		String subject = "";
		Long formId = null;
		CtpTemplate ctpTemplate = templateCacheManager.getCtpTemplate(id);
		if(ctpTemplate!=null) {
			subject = ctpTemplate.getSubject();
			formId = ctpTemplate.getFormAppId();
		}

		templateDao.deletePhysicalCtpTemplateById(id);
		//分发事件
        TemplateDeleteEvent templateDeleteEvent = new TemplateDeleteEvent(this);
        templateDeleteEvent.setTemplateId(id);
        templateDeleteEvent.setFormId(formId);
        EventDispatcher.fireEvent(templateDeleteEvent);
        
        CtpTemplate tt = new CtpTemplate();
        tt.setId(id);
        //因为个人模板最后获取的时候走了权限过滤所以可以只删除模板
        templateCacheManager.deleteCacheTemplate(tt);
        LOG.info("物理删除模板："+subject+",tempalteId:"+id+",formId:"+formId);
	}

	@Override
	@AjaxAccess
	public String getStartRightIdAndAddAccessByTemplateId(Long workflowId) throws BusinessException {
		if(workflowId==null ) {
			return "";
		}
		String rightId = wapi.getProcessTemplateStartRightId(workflowId.toString());
		capFormManager.addRight(rightId);
		return rightId;
	}

	
}
