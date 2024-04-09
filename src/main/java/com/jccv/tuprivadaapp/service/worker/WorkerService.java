package com.jccv.tuprivadaapp.service.worker;


import com.jccv.tuprivadaapp.dto.worker.WorkerRequestDto;
import com.jccv.tuprivadaapp.dto.worker.WorkerResponseDto;

import java.util.List;
import java.util.Optional;

public interface WorkerService {


    public WorkerResponseDto createWorker(WorkerRequestDto worker);
    public List<WorkerResponseDto> findAllWorkers();
}
