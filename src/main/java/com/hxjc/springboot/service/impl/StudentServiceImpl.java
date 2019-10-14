package com.hxjc.springboot.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.hxjc.springboot.mapper.StudentMapper;
import com.hxjc.springboot.model.Student;
import com.hxjc.springboot.service.StudentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * creater: 13116
 * create time: 2019/10/11
 * describe:
 */
@Component  //让该service变成spring的bean
@Service(version = "1.0.0") //dubbo注解 相当于 <dubbo:service interface= ref=  version= />
public class StudentServiceImpl implements StudentService {
    @Autowired(required=false)
    StudentMapper studentMapper;
    @Override
    public String sayHi(String name) {
        return "hi,springbbot "+name;
    }

    @Override
    public Student getStudent(int id) {
        return studentMapper.selectByPrimaryKey(id);
    }
}
