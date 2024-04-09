package com.jccv.tuprivadaapp.repository.worker;

import com.jccv.tuprivadaapp.model.worker.Worker;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WorkerRepository extends JpaRepository<Worker, Long> {
}
