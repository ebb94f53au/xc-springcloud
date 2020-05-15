package com.xuecheng.filesystem.dao;

import com.xuecheng.framework.domain.filesystem.FileSystem;
import org.springframework.data.mongodb.repository.MongoRepository;

/**
 * @author siyang
 * @create 2020-03-16 16:59
 */
public interface FileSystemRepository extends MongoRepository<FileSystem,String> {
}
