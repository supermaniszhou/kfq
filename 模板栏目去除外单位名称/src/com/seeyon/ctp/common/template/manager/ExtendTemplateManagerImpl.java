package com.seeyon.ctp.common.template.manager;

import com.seeyon.ctp.common.AppContext;
import com.seeyon.ctp.common.authenticate.domain.User;
import com.seeyon.ctp.common.exceptions.BusinessException;
import com.seeyon.ctp.common.po.template.CtpTemplate;
import com.seeyon.ctp.common.template.vo.TemplateVO;
import com.seeyon.ctp.organization.bo.V3xOrgAccount;
import com.seeyon.ctp.util.annotation.AjaxAccess;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ExtendTemplateManagerImpl extends TemplateManagerImpl {

    @AjaxAccess
    public List<TemplateVO> getRecentUseTemplate(String category, int count) throws BusinessException {
        User user = AppContext.getCurrentUser();
        List<CtpTemplate> ctpTemplates = this.getPersonalRencentTemplete(category, count);
        List<TemplateVO> templateVOs = new ArrayList<TemplateVO>();
        for (CtpTemplate template : ctpTemplates) {
            TemplateVO templateVO = new TemplateVO();
            templateVO.setSubject(template.getSubject());

            if (!template.getOrgAccountId().equals(user.getLoginAccount())) {
//                V3xOrgAccount outOrgAccount = orgManager.getAccountById(template.getOrgAccountId());
//                templateVO.setSubject(template.getSubject()+"("+outOrgAccount.getShortName()+")");
                //zhou
                templateVO.setSubject(template.getSubject());
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
}
