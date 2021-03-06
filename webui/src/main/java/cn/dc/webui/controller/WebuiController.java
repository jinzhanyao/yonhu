package cn.dc.webui.controller;

import cn.dc.webui.bean.ClickTrendBean;
import cn.dc.webui.bean.ClickTrendOption;
import cn.dc.webui.bean.Line;
import cn.dc.webui.dao.ClickTrendDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@RestController
public class WebuiController {
    @Autowired
    private ClickTrendDao clickTrendDao;

    @RequestMapping("ui")
    public ClickTrendOption catelogAdd( String date,  String hour, @RequestParam(defaultValue = "10000000") int size) {


        Specification<ClickTrendBean> specification = null;
        if (StringUtils.isEmpty(date)){
            final String date1 = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            specification =  new Specification<ClickTrendBean>() {
                @Override
                public Predicate toPredicate(Root<ClickTrendBean> root, CriteriaQuery<?> criteriaQuery, CriteriaBuilder criteriaBuilder) {
                    return criteriaBuilder.and(criteriaBuilder.equal(root.get("date"), date1), criteriaBuilder.equal(root.get("hour"), String.format("%02d",LocalDateTime.now().getHour())));
                }
            };
        }else {
            final String date1 = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            specification =  new Specification<ClickTrendBean>() {
                @Override
                public Predicate toPredicate(Root<ClickTrendBean> root, CriteriaQuery<?> criteriaQuery, CriteriaBuilder criteriaBuilder) {

                    return criteriaBuilder.and(criteriaBuilder.equal(root.get("date"), date), criteriaBuilder.equal(root.get("hour"), String.format("%02d",LocalDateTime.now().getHour())));
                }
            };
        }

         /*
            ????????????????????? + ???????????? ???size???????????????????????????ClickTrendBean
     */
        Page<ClickTrendBean> all = clickTrendDao.findAll(specification, new PageRequest(0, size));

        /*
        ??????????????????????????????

         */

        List<ClickTrendBean> beans = all.getContent();

        Set<Integer> setAdid = new HashSet();
        for (ClickTrendBean clickTrendBean : beans) {
            //???adid?????????????????????????????????????????????????????????????????? ?????????
            //??????????????????adid
            setAdid.add(clickTrendBean.getAdid());
        }
        //???adid?????????????????????????????????????????????????????????????????? ?????????
        Map<Integer, List<ClickTrendBean>> map1 = new HashMap<>();

        List<ClickTrendBean> list1 = null;
        for (int i : setAdid) {
            list1 = new LinkedList<>();
            for (ClickTrendBean clickTrendBean : beans) {
                if (i == clickTrendBean.getAdid()) {
                    list1.add(clickTrendBean);
                }
            }
            map1.put(i, list1);
        }
        /*
        ?????????map<adid,ClickTrendBean?????????>????????????map??????line
         */
        ClickTrendOption clickTrendOption = new ClickTrendOption();
        List<Line> lineList = new ArrayList<>();
        Line line = null;

        for (int i : map1.keySet()) {
            line = new Line();
            line.setName("??????" + i);
            //??????y???????????????????????????????????????
            Map<Integer,Integer> mapMC = clickTrendBeanToInt(map1.get(i));
            List<Integer> list = new ArrayList<>();
            for (int j = 0; j <= 59; j++){
                if (mapMC.keySet().contains(j)) {
                    list.add(mapMC.get(j));
                }else {
                    list.add(0);
                }
            }
            line.setData(list);

            lineList.add(line);
        }

        clickTrendOption.setLineList(lineList);

        //??????linename?????????
        List<String> list2 = new ArrayList<>();
        for (Line line2 : lineList) {
            list2.add(line2.getName());
        }
        clickTrendOption.setLegend_data(list2);

        List<Integer> list3 = new ArrayList<>();
        for (int i = 0; i <= 59; i++) {
            list3.add(i);
        }
        clickTrendOption.setData(list3);

        return clickTrendOption;
    }
    /*
    ???list<ClickTrendBean> ??????map<?????????????????????>
     */
    private Map<Integer,Integer> clickTrendBeanToInt(List<ClickTrendBean> clickTrendBeans) {
        Map<Integer, Integer> map = new HashMap<>();

        for (ClickTrendBean clickTrendBean : clickTrendBeans) {
            map.put(Integer.valueOf(clickTrendBean.getMinute()), clickTrendBean.getClickcount());
        }

        return map;
    }
}
