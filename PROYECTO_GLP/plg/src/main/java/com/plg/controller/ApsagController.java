package com.plg.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.plg.service.ApsagService;
import com.plg.service.ApsagService.ApsagResult;

@RestController
@RequestMapping("/api/apsag")
public class ApsagController {

    @Autowired
    private ApsagService apsagService;

    /**
     * Lanza el flujo AffinityPropagation → inicialización genética.
     * Ejemplo: GET /api/apsag/run?alpha=1&beta=1&damping=0.9&maxIter=100&nInd=50
     */
    @GetMapping("/run")
    public ApsagResult run(
        @RequestParam double alpha,
        @RequestParam double beta,
        @RequestParam double damping,
        @RequestParam int maxIter,
        @RequestParam int nInd
    ) {
        return apsagService.run(alpha, beta, damping, maxIter, nInd);
    }
}
