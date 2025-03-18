package com.jccv.tuprivadaapp.dto.visit.mapper;

import com.jccv.tuprivadaapp.dto.visit.VisitDto;
import com.jccv.tuprivadaapp.model.User;
import com.jccv.tuprivadaapp.model.visit.Visit;
import org.springframework.stereotype.Component;

@Component
public class VisitMapper {

    public Visit toEntity(VisitDto visitDto, User user) {
        return Visit.builder()
                .id(visitDto.getId())
                .visitDate(visitDto.getVisitDate())
                .expirationDate(visitDto.getExpirationDate())
                .qrToken(visitDto.getQrToken())
                .visitorName(visitDto.getVisitorName())
                .visitorDocument(visitDto.getVisitorDocument())
                .status(visitDto.getStatus())
                .user(user)
                .build();

    }

    public VisitDto toDto(Visit visit) {
        return VisitDto.builder()
                .id(visit.getId())
                .visitDate(visit.getVisitDate())
                .expirationDate(visit.getExpirationDate())
                .qrToken(visit.getQrToken())
                .visitorName(visit.getVisitorName())
                .visitorDocument(visit.getVisitorDocument())
                .status(visit.getStatus())
                .userId(visit.getUser().getId())
                .build();

    }


}
