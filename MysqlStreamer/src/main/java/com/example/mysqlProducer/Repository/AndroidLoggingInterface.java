package com.example.mysqlProducer.Repository;

import com.example.mysqlProducer.Model.AndroidLogging;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AndroidLoggingInterface extends JpaRepository<AndroidLogging, Long> {

    @Query(value = "SELECT * FROM android_logging  where id > :id ORDER BY id ASC Limit 100",nativeQuery = true)
    List<AndroidLogging> getListOfLogs(@Param("id")Long id);
}
