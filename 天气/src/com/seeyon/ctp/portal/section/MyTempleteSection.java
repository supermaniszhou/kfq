/**
 * $Author: yans $
 * $Rev:  $
 * $Date:: 2012-12-05 09:05:25#$:
 * <p>
 * Copyright (C) 2012 Seeyon, Inc. All rights reserved.
 * <p>
 * This software is the proprietary information of Seeyon, Inc.
 * Use is subject to license terms.
 */
package com.seeyon.ctp.portal.section;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import com.seeyon.ctp.util.ParamUtil;
import org.apache.log4j.Logger;

import com.seeyon.ctp.common.AppContext;
import com.seeyon.ctp.common.ModuleType;
import com.seeyon.ctp.common.SystemEnvironment;
import com.seeyon.ctp.common.authenticate.domain.User;
import com.seeyon.ctp.common.cache.CacheAccessable;
import com.seeyon.ctp.common.cache.CacheFactory;
import com.seeyon.ctp.common.cache.CacheObject;
import com.seeyon.ctp.common.constants.ApplicationSubCategoryEnum;
import com.seeyon.ctp.common.customize.manager.CustomizeManager;
import com.seeyon.ctp.common.exceptions.BusinessException;
import com.seeyon.ctp.common.i18n.ResourceUtil;
import com.seeyon.ctp.common.po.template.CtpTemplate;
import com.seeyon.ctp.common.taglibs.functions.Functions;
import com.seeyon.ctp.common.template.enums.TemplateCategoryConstant;
import com.seeyon.ctp.common.template.manager.TemplateManager;
import com.seeyon.ctp.organization.bo.V3xOrgAccount;
import com.seeyon.ctp.organization.manager.OrgManager;
import com.seeyon.ctp.portal.section.templete.BaseSectionTemplete;
import com.seeyon.ctp.portal.section.templete.BaseSectionTemplete.OPEN_TYPE;
import com.seeyon.ctp.portal.section.templete.ChessboardTemplete;
import com.seeyon.ctp.portal.section.templete.mobile.MListTemplete;
import com.seeyon.ctp.portal.section.util.SectionUtils;
import com.seeyon.ctp.portal.util.PortletPropertyContants.PropertyName;
import com.seeyon.ctp.util.FlipInfo;
import com.seeyon.ctp.util.StringUtil;
import com.seeyon.ctp.util.Strings;
import com.seeyon.ctp.util.annotation.After;

public class MyTempleteSection extends BaseSectionImpl {
    private static final Logger LOG = Logger.getLogger(MyTempleteSection.class);
    private TemplateManager templateManager;
    private OrgManager orgManager;
    private CustomizeManager customizeManager;

    private CacheObject<Long> changeEtag;
    private Map<Integer, Integer> newLine2Column = new ConcurrentHashMap<Integer, Integer>();

    public void setNewLine2Column(Map<String, String> newLine2Column) {
        Set<Map.Entry<String, String>> en = newLine2Column.entrySet();
        for (Map.Entry<String, String> entry : en) {
            this.newLine2Column.put(Integer.parseInt(entry.getKey()), Integer.parseInt(entry.getValue()));
        }
    }

    public void initialize() {
        CacheAccessable cacheFactory = CacheFactory.getInstance(MyTempleteSection.class);
        changeEtag = cacheFactory.createObject("authChangeEtag");
        changeEtag.set(0l);
    }

    @Override
    public String getIcon() {
        return "templete";
    }

    @Override
    public String getId() {
        return "templeteSection";
    }

    @Override
    public boolean isAllowUsed() {
        if (AppContext.isGroupAdmin()) {
            return false;
        }
        return true;
    }

    @Override
    public String getBaseName() {
        return ResourceUtil.getString("common.my.template");
    }

    @Override
    public String getBaseNameI18nKey() {
        return "common.my.template";
    }

    @Override
    public String getName(Map<String, String> preference) {
        //栏目显示的名字，必须实现国际化，在栏目属性的“columnsName”中存储
        String name = preference.get("columnsName");
        if (Strings.isBlank(name)) {
            return ResourceUtil.getString("common.my.template");
        } else {
            return name;
        }
    }

    @Override
    public Integer getTotal(Map<String, String> arg0) {
        return null;
    }

    @Override
    public boolean isAllowMobileCustomSet() {
        return true;
    }

    @Override
    public Long getLastModify(Map<String, String> preference) {
        User user = AppContext.getCurrentUser();
        Date date = null;
        try {
            date = orgManager.getModifiedTimeStamp(user.getLoginAccount());
        } catch (BusinessException e) {
            LOG.info("", e);
        }
        long orgChangeDate = 0l;
        if (date != null) {
            orgChangeDate = date.getTime();
        }
        //组织模型的修改时间+授权的修改时间+登陆时间
        return orgChangeDate + changeEtag.get() + user.getLoginTimestamp().getTime();
    }

    //    seeyon/template/template.do?method=saveTemplate2Cache
    @After({"/template/template.do.saveTemplate2Cache",//表单停用授权
            "/collTemplate/collTemplate.do.saveCollaborationTemplate",
            "collaborationTemplateManager.updateTempleteAuth",
            "formListManager.disableForms",//表单停用
            "cap4FormListManager.disableForms",//cap4表单停用
            "/edocTempleteController.do.systemSaveTemplete",
            "/form/formDesign.do.save4design",
            "collaborationTemplateManager.deleteTemplete",
            "/mytemplate.do.deleteTemplate",   //个人模板删除
            "/mytemplate.do.renameTemplate",
            "collaborationTemplateManager.saveTemplate",
            "/collTemplate/collTemplate.do.saveTemplate",//另存为事务模板
            "/govdoc/template.do.saveTemplate",//发文拟文-存为模板
            "/template/template.do.templateChoose", //调用模板，刷新最新使用
            "collaborationTemplateManager.updateTempleteConfigSort" //模板排序后,刷新显示顺序
    })
    public void updateAuthChangeEtag() {
        changeEtag.set(System.currentTimeMillis());
    }

    @SuppressWarnings("deprecation")
    @Override
    public BaseSectionTemplete projection(Map<String, String> preference) {

        String subject = "";
        try {
            subject = URLEncoder.encode(this.getName(preference), "UTF-8");
        } catch (UnsupportedEncodingException e) {
            LOG.error("", e);
        }
        ChessboardTemplete c = new ChessboardTemplete();
        //配置模板
        Map<String, String> detailParamer = new HashMap<String, String>();
        detailParamer.put("height", "500");
        detailParamer.put("width", "800");
        detailParamer.put("type", "dialog");
        detailParamer.put("detailParamer", "true");
        c.addBottomButton(ResourceUtil.getString("template.templatePub.configurationTemplates"),
                "/collTemplate/collTemplate.do?method=showTemplateConfig&fragmentId="
                        + preference.get(PropertyName.entityId.name()) + "&ordinal="
                        + preference.get(PropertyName.ordinal.name()), "getCtpTop()", "sectionSetIco", detailParamer);
        //更多
        String viewType = customizeManager.getCustomizeValue(AppContext.currentUserId(), "template_view_type");
        if ("1".equals(viewType)) {
            c.addBottomButton(BaseSectionTemplete.BOTTOM_BUTTON_LABEL_MORE,
                    "/template/template.do?method=listRACITemplate&fragmentId="
                            + preference.get(PropertyName.entityId.name()) + "&ordinal="
                            + preference.get(PropertyName.ordinal.name()), null, "sectionMoreIco");
        } else {
            c.addBottomButton(BaseSectionTemplete.BOTTOM_BUTTON_LABEL_MORE,
                    "/template/template.do?method=moreTreeTemplate&fragmentId="
                            + preference.get(PropertyName.entityId.name()) + "&ordinal="
                            + preference.get(PropertyName.ordinal.name()) + "&columnsName=" + subject + "&recent=" + preference.get("recent"), null, "sectionMoreIco");
        }


        /**
         * 模板栏目走两条线的逻辑：
         * 1、当选择了具体的模板分类的时候，走Auth表的查询 (具体分类的系统模板)  个人模板　+ 最近使用 
         * 2、当没有选择具体的模板分类的时候，走Config表查询（个人模板+系统模板） +　最近使用
         */

        String _configRecent = (null == preference.get("recent")) ? "10" : preference.get("recent");
        //根据配置获取对应的模板分类
        String categoryId = templateManager.getPanelCategory4Portal(preference);

        //查询最近使用模板
        List<CtpTemplate> templatesRecent = new ArrayList<CtpTemplate>();
        List<CtpTemplate> configTemplates = new ArrayList<CtpTemplate>();

        long currentUserId = AppContext.currentUserId();
        try {
            if (Boolean.valueOf(preference.get("showRecentTemplate")) || null == preference.get("showRecentTemplate")) {
                templatesRecent = templateManager.findCtpTemplateRecents(currentUserId, categoryId, 30);
                List<CtpTemplate> afterFilterRecent = new ArrayList<CtpTemplate>();
                Set<Long> recentTemplateIds = new HashSet<Long>();
                if (Strings.isNotEmpty(templatesRecent)) {
                    for (CtpTemplate t : templatesRecent) {
                        if (!recentTemplateIds.contains(t.getId())) {
                            afterFilterRecent.add(t);
                            recentTemplateIds.add(t.getId());
                        }
                    }
                    templatesRecent = afterFilterRecent;
                }
            } else {
                _configRecent = "0";
            }
        } catch (BusinessException e) {
            LOG.error("", e);
        }

        preference.put(PropertyName.cellWidth.name(), c.getTdWidth() + "");
        preference.put(PropertyName.cellHeight.name(), c.getTdHeight() + "");
        int[] chessBoardInfo = c.getPageSize(preference);
        int cou = chessBoardInfo[0];
        int row = chessBoardInfo[1];
        int column = chessBoardInfo[2];
        c.setLayout(row, column);
        c.setDataNum(cou);
        
        /*//显示的总条数
        int total = SectionUtils.getSectionCount(16, preference);*/
        //int total = 50;
        //栏目配置的最近使用数
        int pageCfgRecentCount = 10;
        if (!StringUtil.checkNull(_configRecent)) {
            pageCfgRecentCount = Integer.parseInt(_configRecent);
            if (pageCfgRecentCount > cou) {
                pageCfgRecentCount = cou;
            }
        }

        //查询栏目配置的模板
        int configTempsFetchCount = cou * 2;
        configTemplates = findConfigTemplates(categoryId, configTempsFetchCount);

        try {
            checkAcl(categoryId, templatesRecent, configTemplates);
        } catch (BusinessException e) {
            LOG.error("", e);
        }

        //一般情况下不会进入下面的if,config表中的模板不够显示、比如实际有权限100个、配置了30个，config表中只有10条数据这种极端情况
        if (configTemplates.size() < cou) {
            try {

                List<CtpTemplate> newComputeAllTemplates = templateManager.transMergeCtpTemplateConfig(currentUserId);
                if (newComputeAllTemplates.size() != configTemplates.size()) {
                    //将有权限的模板合并到configTemplates中去
                    mergeTemplates(categoryId, configTemplates, cou, newComputeAllTemplates);
                }
            } catch (BusinessException e) {
                LOG.error("", e);
            }
        }


//        Set<Long> configTemplateIds = new HashSet<Long>();
//        for(CtpTemplate t : configTemplates){
//            configTemplateIds.add(t.getId());
//        }


        //取需要显示的最近使用模板，限制条件（条数/模板配置/授权[授权一级在checkAcl中过滤，此处不再过滤]）
        int _sublistRecentInt = templatesRecent.size() >= pageCfgRecentCount ? pageCfgRecentCount : templatesRecent.size();
        List<CtpTemplate> showRecentTemplates = templatesRecent.subList(0, _sublistRecentInt);
        Set<Long> showRecentTemplateIds = new HashSet<Long>();
        for (CtpTemplate t : showRecentTemplates) {
            showRecentTemplateIds.add(t.getId());
        }

        //取需要显示的普通的模板
        for (Iterator<CtpTemplate> it = configTemplates.iterator(); it.hasNext(); ) {
            CtpTemplate t = it.next();
            if (showRecentTemplateIds.contains(t.getId())) {  //不在最近使用的模板里面
                it.remove();
            }
        }

        int _sublistOtherInt;
        if (cou - showRecentTemplates.size() > 0) {
            if (cou - showRecentTemplates.size() > configTemplates.size()) {
                _sublistOtherInt = configTemplates.size();
            } else {
                _sublistOtherInt = cou - showRecentTemplates.size();
            }
        } else {
            _sublistOtherInt = 0;
        }
        List<CtpTemplate> showOtherTemplates = configTemplates.subList(0, _sublistOtherInt);

        //icon 1表示最近使用 2 表示我配置的模板 3 表示要添加空行
        addItems(c, showRecentTemplates, "1", preference);
        addItems(c, showOtherTemplates, "2", preference);

        return c;
    }

    private void mergeTemplates(String categoryIds, List<CtpTemplate> configTemplates, int cou, List<CtpTemplate> newComputeAllTemplates) {
        Set<Long> newComputeAllTemplateIds = new HashSet<Long>();
        for (CtpTemplate t : newComputeAllTemplates) {
            newComputeAllTemplateIds.add(t.getId());
        }

        Set<Long> configIds = new HashSet<Long>();
        for (CtpTemplate t1 : configTemplates) {
            configIds.add(t1.getId());
        }

        for (Iterator<CtpTemplate> it = configTemplates.iterator(); it.hasNext(); ) {
            CtpTemplate t = it.next();
            if (!newComputeAllTemplateIds.contains(t.getId())) {
                it.remove();
            }
        }

        List<Integer> _moduType = new ArrayList<Integer>();
        List<Long> list = new ArrayList<Long>();
        boolean isPersonTemplate = false;
        if (!StringUtil.checkNull(categoryIds)) {
            String[] ids = categoryIds.split(",");
            for (String s : ids) {
                if (!"".equals(s)) {
                    Long idL = Long.valueOf(s);
                    if (!list.contains(idL)) {
                        list.add(idL);
                    }
                }
            }
            isPersonTemplate = getModuleTypeAndCategoryId(list, _moduType);
        }

        for (Iterator<CtpTemplate> it1 = newComputeAllTemplates.iterator(); it1.hasNext(); ) {
            CtpTemplate t = it1.next();
            //判断栏目是否配置
            if ((isPersonTemplate && !t.isSystem())
                    || (!_moduType.isEmpty() && _moduType.contains(t.getModuleType()))
                    || (!list.isEmpty() && list.contains(t.getCategoryId()))) {
                if (!configIds.contains(t.getId())) {
                    configTemplates.add(t);
                }
            }
            if (configTemplates.size() == cou) {
                break;
            }
        }
    }

    private boolean getModuleTypeAndCategoryId(List<Long> list, List<Integer> _moduType) {
        if (list.contains(Long.valueOf(TemplateCategoryConstant.edocRoot.key()))) {
            list.remove(TemplateCategoryConstant.edocRoot.key());
            _moduType.add(ModuleType.edocSend.getKey());
            _moduType.add(ModuleType.edocRec.getKey());
            _moduType.add(ModuleType.edocSign.getKey());
        }

        if (list.contains(Long.valueOf(TemplateCategoryConstant.edocSendRoot.key()))) {
            list.remove(TemplateCategoryConstant.edocSendRoot.key());
            _moduType.add(ModuleType.edocSend.getKey());
        }
        if (list.contains(Long.valueOf(TemplateCategoryConstant.edocRecRoot.key()))) {
            list.remove(TemplateCategoryConstant.edocRecRoot.key());
            _moduType.add(ModuleType.edocRec.getKey());
        }
        if (list.contains(Long.valueOf(TemplateCategoryConstant.edocSignRoot.key()))) {
            list.remove(TemplateCategoryConstant.edocSignRoot.key());
            _moduType.add(ModuleType.edocSign.getKey());
        }

        if (list.contains(Long.valueOf(TemplateCategoryConstant.publicRoot.key()))) {
            list.remove(TemplateCategoryConstant.publicRoot.key());
            _moduType.add(ModuleType.collaboration.getKey());
        }

        boolean isPersonTemplate = false;
        if (list.contains(Long.valueOf(TemplateCategoryConstant.personRoot.key()))) {
            list.remove(TemplateCategoryConstant.personRoot.key());
            isPersonTemplate = true;
        }

        return isPersonTemplate;
    }

    @Override
    public BaseSectionTemplete mProjection(Map<String, String> preference) {
        // 全部种类 (移动端只支持表单模板)
        String category = String.valueOf(TemplateCategoryConstant.publicRoot.key());
        Integer count = SectionUtils.getSectionCount(3, preference);
        //查询最近使用模板
        List<CtpTemplate> templatesRecent = new ArrayList<CtpTemplate>();
        long currentUserId = AppContext.currentUserId();
        try {
            templatesRecent = templateManager.findCtpTemplateRecents(currentUserId, category, 30);
        } catch (BusinessException e) {
            LOG.error("移动端栏目获取最近使用的模板异常！", e);
        }
        List<CtpTemplate> afterFilterRecent = new ArrayList<CtpTemplate>();
        Set<Long> recentTemplateIds = new HashSet<Long>();
        if (Strings.isNotEmpty(templatesRecent)) {
            for (CtpTemplate t : templatesRecent) {
                if (!"20".equals(t.getBodyType())) {
                    continue;
                }
                if (!recentTemplateIds.contains(t.getId())) {
                    afterFilterRecent.add(t);
                    recentTemplateIds.add(t.getId());
                }
            }
            templatesRecent = afterFilterRecent;
        }
        //查询栏目配置的模板
        int configTempsFetchCount = count * 2;
        List<CtpTemplate> configTemplates = findConfigTemplates(category, configTempsFetchCount);

        //一般情况下不会进入下面的if,config表中的模板不够显示、比如实际有权限100个、配置了30个，config表中只有10条数据这种极端情况
        if (configTemplates.size() < count) {
            try {
                templateManager.transMergeCtpTemplateConfig(currentUserId);
                configTemplates = findConfigTemplates(category, configTempsFetchCount);
            } catch (BusinessException e) {
                LOG.error("", e);
            }
        }


//        Set<Long> configTemplateIds = new HashSet<Long>();
//        for(CtpTemplate t : configTemplates){
//            configTemplateIds.add(t.getId());
//        }

        //判断是否有模板的调用权限
        try {
            checkAcl(category, templatesRecent, configTemplates);
        } catch (BusinessException e) {
            LOG.error("", e);
        }

        List<CtpTemplate> showRecentTemplates;
        //取需要显示的最近使用模板，限制条件（条数/模板配置/授权[授权一级在checkAcl中过滤，此处不再过滤]）

        if (Strings.isNotEmpty(templatesRecent) && templatesRecent.size() > count) {
            showRecentTemplates = templatesRecent.subList(0, count);
        } else {
            showRecentTemplates = templatesRecent;
        }
        Set<Long> showRecentTemplateIds = new HashSet<Long>();
        for (CtpTemplate t : showRecentTemplates) {
            showRecentTemplateIds.add(t.getId());
        }

        //取需要显示的普通的模板
        for (Iterator<CtpTemplate> it = configTemplates.iterator(); it.hasNext(); ) {
            CtpTemplate t = it.next();
            if (!"20".equals(t.getBodyType()) || showRecentTemplateIds.contains(t.getId())) {  //不在最近使用的模板里面
                it.remove();
            }
        }

        int _sublistOtherInt;
        if (count - showRecentTemplates.size() > 0) {
            if (count - showRecentTemplates.size() > configTemplates.size()) {
                _sublistOtherInt = configTemplates.size();
            } else {
                _sublistOtherInt = count - showRecentTemplates.size();
            }
        } else {
            _sublistOtherInt = 0;
        }
        List<CtpTemplate> showOtherTemplates = configTemplates.subList(0, _sublistOtherInt);
        MListTemplete t = new MListTemplete();
        for (CtpTemplate recentTemplate : showRecentTemplates) {
            MListTemplete.Row row = t.addRow();
            row.setSubject(recentTemplate.getSubject());
            row.setLink("/seeyon/m3/apps/v5/collaboration/html/newCollaboration.html?VJoinOpen=VJoin&templateId="
                    + recentTemplate.getId() + "&r=" + System.currentTimeMillis());
            //最近使用的图标
            int type = recentTemplate.getModuleType();
            if (type == ModuleType.edocSend.ordinal() || type == ModuleType.edocRec.ordinal()
                    || type == ModuleType.edocSign.ordinal() || type == ModuleType.edoc.ordinal()) {
                row.setIcon("vportal vp-lately_red_text_template_16");
            } else if (type == ModuleType.info.ordinal()) {
                // 添加信息报送图标
                row.setIcon("vportal vp-infoTemplate_16");
            } else {

                if (recentTemplate.isSystem() == null || recentTemplate.isSystem() == false) {
                    row.setIcon("vportal vp-person_template_16");
                } else {
                    if ("workflow".equals(recentTemplate.getType())) {
                        row.setIcon("vportal vp-lately_flow_template_16");
                    } else if ("text".equals(recentTemplate.getType())) {
                        row.setIcon("vportal vp-lately_format_template_16");
                    } else if ("template".equals(recentTemplate.getType()) && "20".equals(recentTemplate.getBodyType())) {
                        row.setIcon("vportal vp-lately_text_type_template_16");
                    } else {
                        row.setIcon("vportal vp-lately_text_type_template_16");
                    }
                }
            }
        }
        for (CtpTemplate showOthershowOther : showOtherTemplates) {
            MListTemplete.Row row = t.addRow();
            row.setSubject(showOthershowOther.getSubject());
            row.setLink("/seeyon/m3/apps/v5/collaboration/html/newCollaboration.html?VJoinOpen=VJoin&templateId="
                    + showOthershowOther.getId() + "&r=" + System.currentTimeMillis());
            // 设置图标
            int type = showOthershowOther.getModuleType();
            if (type == ModuleType.edocSend.ordinal() || type == ModuleType.edocRec.ordinal()
                    || type == ModuleType.edocSign.ordinal() || type == ModuleType.edoc.ordinal()) {
                row.setIcon("vportal vp-red_text_template_16");
            } else if (type == ModuleType.info.ordinal()) {
                // 添加信息报送图标
                row.setIcon("vportal vp-infoTemplate_16");
            } else {
                if ("workflow".equals(showOthershowOther.getType())) {
                    row.setIcon("vportal vp-flow_template_16");
                } else if ("text".equals(showOthershowOther.getType())) {
                    row.setIcon("vportal vp-format_template_16");
                } else if ("template".equals(showOthershowOther.getType()) && "20".equals(showOthershowOther.getBodyType())) {
                    row.setIcon("vportal vp-form_temp_16");
                } else {
                    row.setIcon("vportal vp-collaboration_16");
                }
            }
        }
        t.setMoreLink("/seeyon/m3/apps/v5/collaboration/html/templateIndex.html?VJoinOpen=VJoin&r="
                + System.currentTimeMillis());
        return t;
    }

    private void checkAcl(String category, List<CtpTemplate> templatesRecent, List<CtpTemplate> configTemplates) throws BusinessException {

        List<CtpTemplate> allTemplates = new ArrayList<CtpTemplate>();
        allTemplates.addAll(configTemplates);
        allTemplates.addAll(templatesRecent);

        Map<Long, Boolean> isEnabled = templateManager.isTemplateEnabled(allTemplates, AppContext.currentUserId());
        if (templatesRecent != null) {
            for (Iterator<CtpTemplate> it = templatesRecent.iterator(); it.hasNext(); ) {
                CtpTemplate t = it.next();
                if (isEnabled.get(t.getId()) != null && !isEnabled.get(t.getId())) {
                    it.remove();
                }
            }
        }
        //LOG.error("首页栏目，username:"+AppContext.currentUserName()+",最近使用模板查询总数："+recentSize+",权限判断以后，剩下："+templatesRecent.size());
        if (configTemplates != null) {
            for (Iterator<CtpTemplate> it = configTemplates.iterator(); it.hasNext(); ) {
                CtpTemplate t = it.next();
                if (isEnabled.get(t.getId()) != null && !isEnabled.get(t.getId())) {
                    it.remove();
                }
            }
        }
    }

    private void addItems(ChessboardTemplete c, List<CtpTemplate> templates, String icon, Map<String, String> preference) {
        if (Strings.isNotEmpty(templates)) {
            boolean isPluginEdoc = SystemEnvironment.hasPlugin("edoc");
            boolean isEdoc = Functions.isEnableEdoc();
            for (CtpTemplate ctpTemplate : templates) {
                int type = ctpTemplate.getModuleType();
                // 没有公文模块不显示发文模板和收文模板
                if (!isPluginEdoc || !isEdoc) {
                    if (type == ModuleType.edocSend.ordinal() || type == ModuleType.edocRec.ordinal()
                            || type == ModuleType.edocSign.ordinal() || type == ModuleType.edoc.ordinal()) {
                        continue;
                    }
                }
                ChessboardTemplete.Item item = c.addItem();
                long templeteId = ctpTemplate.getId();
                // 设置图标
                if ("2".equals(icon)) {
                    if (type == ModuleType.edocSend.ordinal() || type == ModuleType.edocRec.ordinal()
                            || type == ModuleType.edocSign.ordinal() || type == ModuleType.edoc.ordinal()) {
                        item.addExtIcon("red_text_template_16");
                    } else if (type == ModuleType.info.ordinal()) {
                        // 添加信息报送图标
                        item.addExtIcon("infoTemplate_16");
                    } else {
                        if ("workflow".equals(ctpTemplate.getType())) {
                            item.addExtIcon("flow_template_16");
                        } else if ("text".equals(ctpTemplate.getType())) {
                            item.addExtIcon("format_template_16");
                        } else if ("template".equals(ctpTemplate.getType()) && "20".equals(ctpTemplate.getBodyType())) {
                            item.addExtIcon("form_temp_16");
                        } else {
                            item.addExtIcon("collaboration_16");
                        }
                    }
                } else {
                    //最近使用的图标
                    if (type == ModuleType.edocSend.ordinal() || type == ModuleType.edocRec.ordinal()
                            || type == ModuleType.edocSign.ordinal() || type == ModuleType.edoc.ordinal()) {
                        item.addExtIcon("lately_red_text_template_16");
                    } else if (type == ModuleType.info.ordinal()) {
                        // 添加信息报送图标
                        item.addExtIcon("infoTemplate_16");
                    } else {

                        if (ctpTemplate.isSystem() == null || ctpTemplate.isSystem() == false) {
                            item.addExtIcon("person_template_16");
                        } else {
                            if ("workflow".equals(ctpTemplate.getType())) {
                                item.addExtIcon("lately_flow_template_16");
                            } else if ("text".equals(ctpTemplate.getType())) {
                                item.addExtIcon("lately_format_template_16");
                            } else if ("template".equals(ctpTemplate.getType()) && "20".equals(ctpTemplate.getBodyType())) {
                                item.addExtIcon("lately_text_type_template_16");
                            } else {
                                item.addExtIcon("lately_text_type_template_16");
                            }
                        }
                    }
                }

                // 设置链接
                // 协同和表单模板
                if (type == -1 || type == ModuleType.collaboration.ordinal() || type == ModuleType.form.ordinal()) {
                    // TODO 在这里不去校验模板是否存在
                    item.setLink("/collaboration/collaboration.do?method=newColl&from=templateNewColl&templateId=" + templeteId);
                    //打开方式，协同为多窗口弹出信息
                    item.setOpenType(OPEN_TYPE.multiWindow);
                }// 公文模板
                else if (type == ModuleType.edoc.ordinal()) {
                    //TODO 在这里不去校验模板是否存在
                    item.setLink("/edocController.do?method=entryManager&entry=newEdoc&templeteId=" + templeteId);
                } else if (type == ModuleType.edocSend.ordinal()) {
                    item.setLink("/edocController.do?method=entryManager&entry=sendManager&edocType=0&toFrom=newEdoc&templeteId="
                            + templeteId);
                } else if (type == ModuleType.edocRec.ordinal()) {
                    item.setLink("/edocController.do?method=entryManager&entry=recManager&edocType=1&toFrom=newEdoc&templeteId=" + templeteId + "&listType=newEdoc");
                } else if (type == ModuleType.edocSign.ordinal()) {
                    item.setLink("/edocController.do?method=entryManager&entry=signReport&edocType=2&toFrom=newEdoc&templeteId="
                            + templeteId);
                } else if (type == ModuleType.info.ordinal()) {
                    item.setLink("/info/infomain.do?method=infoReport&listType=listCreateInfo&templateId=" + templeteId);
                    //item.setLink("/info/infocreate.do?method=createInfo&action=template&templateId="+templeteId);
                }
                //G6 6.1新表单公文---start
                else if (type == ModuleType.govdocSend.getKey() || type == ModuleType.govdocRec.getKey()) {
                    String subApp = type == ModuleType.govdocSend.getKey() ? "1" : "2";
                    item.setLink("/govdoc/govdoc.do?method=newGovdoc&from=template&templateId=" + templeteId + "&app=4&sub_app=" + subApp);
                    item.setOpenType(OPEN_TYPE.multiWindow);
                } else if (type == ModuleType.govdocSign.getKey()) {
                    item.setLink("/govdoc/govdoc.do?method=newGovdoc&from=template&templateId=" + templeteId + "&app=4&sub_app=" + ApplicationSubCategoryEnum.edoc_qianbao.getKey());
                    item.setOpenType(OPEN_TYPE.multiWindow);
                }
                //G6 6.1新表单公文---end
                // 設置名稱
                String templeteSubject = Strings.toHTML(Strings.escapeNULL(ctpTemplate.getSubject(), ""), false);
                // 非当前登录单位的模板，标示单位名称
                if (!ctpTemplate.getOrgAccountId().equals(AppContext.getCurrentUser().getLoginAccount())) {
                    V3xOrgAccount account = null;
                    try {
                        account = orgManager.getAccountById(ctpTemplate.getOrgAccountId());
                    } catch (BusinessException e) {
                        LOG.error("", e);
                    }
                    StringBuilder sb = new StringBuilder(templeteSubject);
                    //[开发区] zhou：注释掉  2021-02-23

//                    sb.append("(");
//                    sb.append(account != null ? account.getShortName() : "");
//                    sb.append(")");
                    templeteSubject = sb.toString();
                }
                item.setName(templeteSubject.toString());
                item.setTitle(Strings.escapeNULL(ctpTemplate.getSubject(), ""));
            }
        }
    }

    /**
     * 查询配置表中的模板，包含新建的
     *
     * @param category
     * @param count
     * @return
     */
    private List<CtpTemplate> findConfigTemplates(String categoryIds, int count) {
        List<CtpTemplate> templateList = new ArrayList<CtpTemplate>();
        User user = AppContext.getCurrentUser();
        if (count <= 0) {
            return templateList;
        }

        FlipInfo flipInfo = new FlipInfo();
        flipInfo.setPage(1);
        //当前查询的count为，剩余显示的模版数量 加上最近使用的模版数量（查询出的模版因为包含最近使用的，所以先查询出来再筛选）
        flipInfo.setSize(count);
        flipInfo.setNeedTotal(false);
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("userId", user.getId());
        params.put("accountId", user.getLoginAccount());
        params.put("categoryIds", categoryIds);
        try {
            templateList = templateManager.getMyConfigCollTemplate(flipInfo, params);
        } catch (BusinessException e) {
            LOG.error("", e);
        }
        return templateList;
    }

    @Override
    public String getResolveFunction(Map<String, String> preference) {
        String requestFrom = ParamUtil.getString(preference, "requestFrom", "");
        if ("mobile".equals(requestFrom)) {
            return MListTemplete.RESOLVE_FUNCTION;
        } else {
            return ChessboardTemplete.RESOLVE_FUNCTION;
        }
    }


    /**
     * @param orgManager the orgManager to set
     */
    public void setOrgManager(OrgManager orgManager) {
        this.orgManager = orgManager;
    }

    public void setTemplateManager(TemplateManager templateManager) {
        this.templateManager = templateManager;
    }

    public void setCustomizeManager(CustomizeManager customizeManager) {
        this.customizeManager = customizeManager;
    }
}
