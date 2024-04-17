package com.jccv.tuprivadaapp.repository.worker.facade;

import com.jccv.tuprivadaapp.exception.ResourceNotFoundException;
import com.jccv.tuprivadaapp.model.resident.Resident;
import com.jccv.tuprivadaapp.model.worker.Worker;
import com.jccv.tuprivadaapp.repository.resident.ResidentRepository;
import com.jccv.tuprivadaapp.repository.worker.WorkerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class WorkerFacade {
    @Autowired
    private WorkerRepository workerRepository;

    public Worker findWorkerById(Long workerId){
        return workerRepository.findById(workerId).orElseThrow(() -> new ResourceNotFoundException("Trabajador no encontrado con el id: " + workerId));
    }

    public Worker save(Worker worker) {
        return workerRepository.save(worker);
    }
}
