package com.jccv.tuprivadaapp.repository.worker;

import com.jccv.tuprivadaapp.model.worker.Worker;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface WorkerRepository extends JpaRepository<Worker, Long> {
}
