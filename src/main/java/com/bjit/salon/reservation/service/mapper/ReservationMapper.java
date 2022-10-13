package com.bjit.salon.reservation.service.mapper;



import com.bjit.salon.reservation.service.dto.request.CatalogRequest;

import com.bjit.salon.reservation.service.dto.response.CatalogResponse;
import com.bjit.salon.reservation.service.dto.response.ReservationResponseDto;
import com.bjit.salon.reservation.service.entity.Catalog;
import com.bjit.salon.reservation.service.entity.Reservation;
import org.mapstruct.Mapper;

import java.util.ArrayList;
import java.util.List;

@Mapper(componentModel = "spring")
public interface ReservationMapper {

    ReservationResponseDto toReservationResponse(Reservation reservation);
    //List<ReservationResponseDto> toReservationResponseList(List<Reservation> reservations);

    List<Catalog> toCatalogs(List<CatalogRequest> CatalogRequests);

    default List<ReservationResponseDto> reservationsToReservationResponses(List<Reservation> reservations){
        List<ReservationResponseDto> listOfReservationResponse = new ArrayList<>();
        if (reservations == null){
            return null;
        }
        reservations.forEach(reservation -> {
            listOfReservationResponse.add(reservationToReservationResponse(reservation));
        });
        return listOfReservationResponse;
    }

    default List<CatalogResponse> catalogsToCatalogsResponse(List<Catalog> catalogs){
        List<CatalogResponse> response = new ArrayList<>();
        if (catalogs == null){
            return null;
        }
        catalogs.forEach(catalog ->{
           response.add(catalogToCatalogResponse(catalog));
        });
        return response;
    }

    default ReservationResponseDto reservationToReservationResponse(Reservation reservation){
        ReservationResponseDto response = new ReservationResponseDto();
        response.setId(reservation.getId());
        response.setStaffId(reservation.getStaffId());
        response.setConsumerId(reservation.getConsumerId());
        response.setStartTime(reservation.getStartTime());
        response.setEndTime(reservation.getEndTime());
        response.setPaymentMethod(reservation.getPaymentMethod());
        response.setWorkingStatus(reservation.getWorkingStatus());
        response.setReservationDate(reservation.getReservationDate());
        response.setTotalPayableAmount(reservation.getTotalPayableAmount());
        response.setServices(catalogsToCatalogsResponse(reservation.getServices()));
        return response;
    }

    default CatalogResponse catalogToCatalogResponse(Catalog catalog){
        CatalogResponse response = new CatalogResponse();
        response.setName(catalog.getName());
        response.setDescription(catalog.getDescription());
        response.setApproximateTimeForCompletion(catalog.getApproximateTimeForCompletion());
        response.setPayableAmount(catalog.getPayableAmount());
        return response;
    }



}
