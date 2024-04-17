package com.jccv.tuprivadaapp.service.worker.implementation;

import com.jccv.tuprivadaapp.dto.worker.WorkerResponseDto;
import com.jccv.tuprivadaapp.repository.auth.UserRepository;
import com.jccv.tuprivadaapp.repository.worker.WorkerRepository;
import com.jccv.tuprivadaapp.service.worker.WorkerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class WorkerServiceImp implements WorkerService {

    @Autowired
    private WorkerRepository workerRepository;
    @Autowired
    private UserRepository userRepository;



    @Override
    public List<WorkerResponseDto> findAllWorkers() {
        List<WorkerResponseDto> workers = workerRepository.findAll().stream()
                .map(worker ->WorkerResponseDto.builder().id(worker.getId()).build()).collect(Collectors.toList());
        return workers;
    }
}
