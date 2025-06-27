package org.ispw.fastridetrack.controller.applicationcontroller;

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

        mapRequestBean.setEstimatedTimeMinutes(map.getEstimatedTimeMinutes());

        return map;
    }

}



