package org.ispw.fastridetrack.controller.applicationcontroller;

import org.ispw.fastridetrack.bean.CoordinateBean;
import org.ispw.fastridetrack.bean.LocationBean;
import org.ispw.fastridetrack.bean.MapRequestBean;
import org.ispw.fastridetrack.adapter.MapService;
import org.ispw.fastridetrack.exception.MapServiceException;
import org.ispw.fastridetrack.model.Map;
import org.ispw.fastridetrack.session.SessionManager;

public class MapApplicationController {

    private final MapService mapService;

    public MapApplicationController() {
        this.mapService = SessionManager.getInstance().getMapService();
    }

    // Calcolo il percorso e aggiorno il MapRequestBean con il tempo stimato.
    public Map showMap(MapRequestBean mapRequestBean) throws MapServiceException {
        if (mapRequestBean == null) {
            throw new IllegalArgumentException("MapRequestBean non può essere nullo");
        }

        if (mapRequestBean.getOrigin() == null || mapRequestBean.getDestination() == null) {
            throw new IllegalArgumentException("Origin o destination nulli!");
        }

        Map map = mapService.calculateRoute(mapRequestBean);

        // Aggiorna tempo stimato nel bean
        mapRequestBean.setEstimatedTimeMinutes(map.getEstimatedTimeMinutes());

        return map;
    }

    public Map displayMapRoute(LocationBean startPointLocation, LocationBean endPointLocation) throws MapServiceException {
        if (startPointLocation == null || endPointLocation == null) {
            throw new IllegalArgumentException("I valori delle posizioni non possono essere nulli");
        }

        CoordinateBean startPoint;
        CoordinateBean endPoint;

        if (!startPointLocation.hasCoordinates()) {
            startPoint = mapService.geocodeAddress(startPointLocation.getAddress());
        } else {
            startPoint = startPointLocation.getCoordinate();
        }

        if (!endPointLocation.hasCoordinates()) {
            endPoint = mapService.geocodeAddress(endPointLocation.getAddress());
        } else {
            endPoint = endPointLocation.getCoordinate();
        }

        return mapService.calculateRouteDriver(startPoint, endPoint);
    }
}



