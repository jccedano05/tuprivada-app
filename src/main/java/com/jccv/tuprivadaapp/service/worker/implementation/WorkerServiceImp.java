package com.jccv.tuprivadaapp.service.worker.implementation;

import com.jccv.tuprivadaapp.dto.worker.WorkerRequestDto;
import com.jccv.tuprivadaapp.dto.worker.WorkerResponseDto;
import com.jccv.tuprivadaapp.model.User;
import com.jccv.tuprivadaapp.model.worker.Worker;
import com.jccv.tuprivadaapp.repository.UserRepository;
import com.jccv.tuprivadaapp.repository.worker.WorkerRepository;
import com.jccv.tuprivadaapp.service.worker.WorkerService;
import org.hibernate.jdbc.Work;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class WorkerServiceImp implements WorkerService {

    @Autowired
    private WorkerRepository workerRepository;
    @Autowired
    private UserRepository userRepository;

    @Override
    public WorkerResponseDto createWorker(WorkerRequestDto requestDto) {
        User user = userRepository.findById(requestDto.getUserId()).orElseThrow(() -> new IllegalArgumentException("User not found with id: " + requestDto.getUserId()));
        Worker worker = Worker.builder()
                .user(user)
                .build();
        worker = workerRepository.save(worker);
        return  WorkerResponseDto.builder()
                .id(worker.getId())
                .build();
    }

    @Override
    public List<WorkerResponseDto> findAllWorkers() {
        List<WorkerResponseDto> workers = workerRepository.findAll().stream()
                .map(worker ->WorkerResponseDto.builder().id(worker.getId()).build()).collect(Collectors.toList());
        return workers;
    }
}
