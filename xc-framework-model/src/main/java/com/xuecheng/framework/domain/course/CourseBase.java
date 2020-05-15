package com.xuecheng.framework.domain.course;

import lombok.Data;
import lombok.ToString;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import java.io.Serializable;

/**
 * Created by admin on 2018/2/10.
 */
@Data
@ToString
@Entity
@Table(name="course_base")
//@GenericGenerator(name = "jpa-assigned", strategy = "assigned")
@GenericGenerator(name = "jpa-uuid", strategy = "uuid")
public class CourseBase implements Serializable {
    private static final long serialVersionUID = -916357110051689486L;
    @Id
    @GeneratedValue(generator = "jpa-uuid")
    @Column(length = 32)
    private String id;
    @NotBlank
    private String name;
    private String users;
    @NotBlank
    private String mt;
    @NotBlank
    private String st;
    private String grade;
    @NotBlank
    private String studymodel;
    private String teachmode;
    private String description;
    private String status;
    @Column(name="company_id")
    private String companyId;
    @Column(name="user_id")
    private String userId;

}
