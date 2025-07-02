package org.ispw.fastridetrack.adapter;

import org.ispw.fastridetrack.bean.CoordinateBean;
import org.ispw.fastridetrack.bean.MapRequestBean;
import org.ispw.fastridetrack.exception.MapServiceException;
import org.ispw.fastridetrack.model.Map;

public interface MapService {
    Map calculateRoute(MapRequestBean requestBean) throws MapServiceException;
    Map calculateRouteDriver(CoordinateBean startPoint, CoordinateBean endPoint) throws MapServiceException;
    String getAddressFromCoordinates(double latitude, double longitude) throws MapServiceException;
    CoordinateBean geocodeAddress(String address) throws MapServiceException;
    String getAddressFromCoordinatesString(String address) throws MapServiceException;

}





