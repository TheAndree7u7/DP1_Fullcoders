package com.plg.dto;

import java.util.ArrayList;
import java.util.List;

import com.plg.utils.Gen;
import com.plg.utils.Individuo;

import lombok.Data;

@Data
public class IndividuoDto {
    private List<GenDto> cromosoma;
    
    public IndividuoDto(Individuo individuo) {
        this.cromosoma = new ArrayList<>();
        for (Gen gen : individuo.getCromosoma()) {
            cromosoma.add(new GenDto(gen));
        }

    }
}
