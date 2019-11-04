package com.hxjc.springboot.mapper;




import com.hxjc.springboot.model.Student;

import com.hxjc.springboot.paginator.Page;
import org.apache.ibatis.annotations.Mapper;


import java.util.List;

@Mapper
public interface StudentDao {

   public List<Student>  selectAllStudentByCon(Student student,Page page);
}