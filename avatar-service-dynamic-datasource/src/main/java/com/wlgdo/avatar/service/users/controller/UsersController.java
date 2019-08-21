package com.wlgdo.avatar.service.users.controller;


import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wlgdo.avatar.common.http.HttpResp;
import com.wlgdo.avatar.service.actors.entity.TActor;
import com.wlgdo.avatar.service.bridge.AuthorUserService;
import com.wlgdo.avatar.service.bridge.BridgeBuilder;
import com.wlgdo.avatar.service.bridge.HidoUserService;
import com.wlgdo.avatar.service.users.entity.TUsers;
import com.wlgdo.avatar.service.users.export.ExcelData;
import com.wlgdo.avatar.service.users.export.ExportExcelUtils;
import com.wlgdo.avatar.service.users.service.IUsersService;
import org.apache.commons.lang3.StringUtils;
import org.dozer.DozerBeanMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;


/**
 * <p>
 * 用户表 前端控制器
 * </p>
 *
 * @author Ligang.Wang[wlgchun@163.com]
 * @since 2019-06-10
 */
@RestController
public class UsersController {

    static Logger logger = LoggerFactory.getLogger(UsersController.class);

    @Autowired
    private IUsersService usersService;

    /**
     * 该方法只是一个示例
     *
     * @param pageIndex
     * @param pageSize
     * @return
     */
    @GetMapping("/users")
    public Object getUserList(@RequestParam Integer pageIndex, @RequestParam Integer pageSize) {
        /**
         * 这是一个桥接器的应用示例
         */
        BridgeBuilder bridgeBuilder = new BridgeBuilder();
        bridgeBuilder.setUserInterface(new AuthorUserService());
        bridgeBuilder.save("作者:李");
        bridgeBuilder.setUserInterface(new HidoUserService());
        bridgeBuilder.save("平台：李");
        IPage<TUsers> page = new Page<>(pageIndex, pageSize);
        Wrapper<TUsers> queryWrapper = new QueryWrapper<>();
        IPage<TUsers> pageData = usersService.page(page, queryWrapper);

        return HttpResp.instance().setData(pageData);
    }

    @GetMapping("/users/list")
    public Object getList(@RequestParam(required = false) String nickName, @RequestParam(required = false) String mobile) {

        QueryWrapper queryWrapper = new QueryWrapper<TUsers>();
        if (StringUtils.isNotBlank(nickName)) {
            queryWrapper.like("nick_name", nickName);
        }
        if (StringUtils.isNotBlank(mobile)) {
            queryWrapper.like("contact_number", mobile);
        }
        List<TUsers> userlist = usersService.list(queryWrapper);

        List<TUsers> list = userlist.stream().filter(e -> e.getSex() == 1).collect(Collectors.toList());

        List list1 = BeanMapper.mapList(list, TActor.class);

        List<String> openIds = list.stream().map(tUsers -> tUsers.getOpenId()).collect(Collectors.toList());

        List<TActor> tacts = list.stream().map(tUsers -> tUsers.build(1)).collect(Collectors.toList());


        DozerBeanMapper mapper = new DozerBeanMapper();

        List<Class<TActor>> aList = list.stream().map(e -> TActor.class).collect(Collectors.toList());

        Optional<TUsers> firstUser = list.stream().findFirst();
        TActor actor = new TActor();
        BeanUtils.copyProperties(actor, firstUser.get());

        return HttpResp.instance().setData(userlist);
    }

    /**
     * 导出excel数据
     *
     * @param response
     * @throws Exception
     */
    @RequestMapping(value = "/excel", method = RequestMethod.GET)
    public void excel(HttpServletResponse response) throws Exception {
        Long startTime = System.currentTimeMillis();
        QueryWrapper queryWrapper = new QueryWrapper<TUsers>();
        List<TUsers> userlist = usersService.list(queryWrapper);
        ExcelData data = new ExcelData();
        data.setName("hello");
        List<String> titles = new ArrayList();
        titles.add("A");
        titles.add("B");
        titles.add("C");
        data.setTitles(titles);
        List<List<Object>> rows = new ArrayList();
        List<Object> row = null;
        for (TUsers u : userlist) {
            row = new ArrayList<>();
            row.add(u.getNickName());
            row.add(u.getOpenId());
            row.add(u.getPhone());
            rows.add(row);
        }
        data.setRows(rows);
        ExportExcelUtils.exportExcel(response, "TEST.xlsx", data);
        logger.info("Total used time {}sm", (System.currentTimeMillis() - startTime) / 1000);
    }

    @RequestMapping(value = "/import", method = RequestMethod.GET)
    public void importExcel(@RequestParam("file") MultipartFile file) {
        try {
            ExportExcelUtils.importDataFromExcel(new TUsers(), file.getInputStream(), file.getOriginalFilename());
        } catch (IOException e) {
            e.printStackTrace();

        }


    }

}
