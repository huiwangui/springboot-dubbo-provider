package com.hxjc.springboot.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.hxjc.springboot.mapper.StudentDao;
import com.hxjc.springboot.model.Student;
import com.hxjc.springboot.paginator.Page;
import com.hxjc.springboot.paginator.PageUtil;
import com.hxjc.springboot.service.StudentService;
import com.hxyc.common.page.PageResult;
import com.hxyc.common.page.Paginator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * creater: 13116
 * create time: 2019/10/11
 * describe:
 */
@Component  //让该service变成spring的bean
@Service(version = "1.0.0") //dubbo注解 相当于 <dubbo:service interface= ref=  version= />
public class StudentServiceImpl implements StudentService {


    @Autowired(required=false)
    StudentDao studentDao;



    @Override
    public PageResult<Student> listStudentByCon(Student student,Paginator paginator) {

        Page page = PageUtil.transformToPage(paginator);
        List<Student> list =studentDao.selectAllStudentByCon(student,page);
        return new PageResult(list, PageUtil.transformToPaginator(page));


    }
}
