package com.apdplat.module.info.service;

import com.apdplat.module.info.model.InfoType;
import com.apdplat.module.info.model.InfoTypeContent;
import com.apdplat.platform.criteria.Criteria;
import com.apdplat.platform.criteria.Operator;
import com.apdplat.platform.criteria.PropertyCriteria;
import com.apdplat.platform.criteria.PropertyEditor;
import com.apdplat.platform.result.Page;
import com.apdplat.platform.service.ServiceFacade;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 *
 * @author ysc
 */
@Service
public class InfoTypeService {
    protected static final Logger log = LoggerFactory.getLogger(InfoTypeService.class);

    public static List<Integer> getChildIds(InfoType obj) {
        List<Integer> ids=new ArrayList<>();
        List<InfoType> child=obj.getChild();
        for(InfoType item : child){
            ids.add(item.getId());
            ids.addAll(getChildIds(item));
        }
        return ids;
    }
    @Resource(name="serviceFacade")
    private ServiceFacade serviceFacade;

    
    public String toRootJson(String lang){
        InfoType infoType=getRootInfoType();
        infoType.setLang(lang);
        
        if(infoType==null){
            log.error("获取根新闻类别失败！");
            return "";
        }
        StringBuilder json=new StringBuilder();
        json.append("[");

        json.append("{'text':'")
            .append(infoType.getInfoTypeName())
            .append("','id':'")
            .append(infoType.getId());
            if(infoType.getChild().isEmpty()){
                json.append("','leaf':true,'cls':'file'");
            }else{
                json.append("','leaf':false,'cls':'folder'");
            }
        json.append("}");
        json.append("]");
        
        return json.toString();
    }
    public String toJson(int infoTypeId, String lang){
        InfoType infoType=serviceFacade.retrieve(InfoType.class, infoTypeId);
        if(infoType==null){
            log.error("获取ID为 "+infoType+" 的新闻类别失败！");
            return "";
        }
        List<InfoType> child=infoType.getChild();
        if(child.isEmpty()){
            return "";
        }
        StringBuilder json=new StringBuilder();
        json.append("[");

        
        for(InfoType item : child){
            item.setLang(lang);
            json.append("{'text':'")
                .append(item.getInfoTypeName())
                .append("','id':'")
                .append(item.getId());
                if(item.getChild().isEmpty()){
                    json.append("','leaf':true,'cls':'file'");
                }else{
                    json.append("','leaf':false,'cls':'folder'");
                }
           json .append("},");
        }
        //删除最后一个,号，添加一个]号
        json=json.deleteCharAt(json.length()-1);
        json.append("]");

        return json.toString();
    }
    public InfoType getRootInfoType(){
        try{
            PropertyCriteria propertyCriteria = new PropertyCriteria(Criteria.or);
            propertyCriteria.addPropertyEditor(new PropertyEditor("infoTypeName", Operator.eq, "String","新闻类别"));
            Page<InfoTypeContent> page = serviceFacade.query(InfoTypeContent.class, null, propertyCriteria);
            if (page.getTotalRecords() == 1) {
                return page.getModels().get(0).getInfoType();
            }
        }catch(Exception e){
            log.error("获取ROOT失败",e);
        }
        return null;
    }
}
