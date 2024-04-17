package com.jccv.tuprivadaapp.controller.worker;

import com.jccv.tuprivadaapp.dto.worker.WorkerRequestDto;
import com.jccv.tuprivadaapp.dto.worker.WorkerResponseDto;
import com.jccv.tuprivadaapp.model.worker.Worker;
import com.jccv.tuprivadaapp.service.worker.WorkerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static com.jccv.tuprivadaapp.utils.AuthorizationsLevel.*;

@RestController
@RequestMapping("workers")
@PreAuthorize(WORKER_LEVEL)
public class WorkerController {

    @Autowired
    private WorkerService workerService;

    /*
    *
    * Crear una funcion que ayude a dar la jerarquia de autoridades (osea que si un worker puede usarlo, el admin y super admin que estan arriba de el tambien)
    * */
//    @PostMapping
//    @PreAuthorize(CONDOMINIUM_LEVEL)
//    public ResponseEntity<WorkerResponseDto> createWorker(@RequestBody WorkerRequestDto request){
//        WorkerResponseDto workedSaved = workerService.createWorker(request);
//        return ResponseEntity.ok(workedSaved);
//    }



    @GetMapping
    public ResponseEntity<List<WorkerResponseDto>> findAllWorkers(){
        List<WorkerResponseDto> workers = workerService.findAllWorkers();
        return ResponseEntity.ok(workers);
    }
}
