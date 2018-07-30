package com.infoshare.kodziaki.web.servlets;

import com.infoshare.kodziaki.Place;
import com.infoshare.kodziaki.PlaceType;
import com.infoshare.kodziaki.UserPreferences;
import com.infoshare.kodziaki.web.dao.LocationDao;
import com.infoshare.kodziaki.web.dao.PlaceDao;
import com.infoshare.kodziaki.web.freemarker.TemplateProvider;
import com.infoshare.kodziaki.web.model.Location;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@WebServlet("/search")
public class SearchAdsServlet extends HttpServlet {

    java.util.logging.Logger logger = java.util.logging.Logger.getLogger(getClass().getName());

    @Inject
    private TemplateProvider templateProvider;

    @Inject
    private PlaceDao placeDao;

    @Inject
    private LocationDao locationDao;

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

        resp.setContentType("text/html;charset=UTF-8");

        Template template = templateProvider.getTemplate(getServletContext(), "SearchAds.ftlh");
        Map<String, Object> dataModel = new HashMap<>();
        dataModel.put("locations", sortDistrictsByCities());

        try {
            template.process(dataModel, resp.getWriter());
        } catch (TemplateException e) {
            logger.info("Template not found ");
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        req.setCharacterEncoding("UTF-8");

        UserPreferences userPreferences = null;
        try {
            userPreferences = getUserPreferences(req);
        } catch (Exception e) {
            resp.getWriter().println("Wystapił błąd: " + e.getMessage());
            logger.info("Error ");
        }

        Template template = templateProvider.getTemplate(getServletContext(), "FilteredAds.ftlh");
        Map<String, Object> filteredAds = new HashMap<>();

        List<Place> adsList = placeDao.getAdsByUserPreferences(userPreferences);
        filteredAds.put("filteredAds", adsList);

        resp.setContentType("text/html;charset=UTF-8");

        try {
            template.process(filteredAds, resp.getWriter());
        } catch (TemplateException e) {
            logger.info("Template not found ");
        }
    }

    private Map<String, List<Location>> sortDistrictsByCities() {
        return locationDao
                .findAll()
                .stream()
                .collect(Collectors.groupingBy(Location::getCity));
    }

    private UserPreferences getUserPreferences(HttpServletRequest req) {
        return new UserPreferences(
                    parseToPlaceType(req.getParameter("placeType")),
                    getCityParam(req.getParameter("localization")),
                    getDistrictParam(req.getParameter("localization")),
                    parseToBigDecimal(req.getParameter("minPrice")),
                    parseToBigDecimal(req.getParameter("maxPrice")),
                    parseToDouble(req.getParameter("minArea")),
                    parseToDouble(req.getParameter("maxArea")),
                    parseToInteger(req.getParameter("minRooms")),
                    parseToInteger(req.getParameter("maxRooms")),
                    parseToInteger(req.getParameter("minFloor")),
                    parseToInteger(req.getParameter("maxFloor")),
                    parseToBoolean(req.getParameter("isElevator")),
                    parseToBoolean(req.getParameter("smokingAllowed")),
                    parseToBoolean(req.getParameter("animalsAllowed")),
                    parseToBoolean(req.getParameter("onlyLongTerm")));

    }

    private String getCityParam(String parameter) {
        String[] localizationParams = parameter.split(",");
        return localizationParams[0];

    }

    private String getDistrictParam(String parameter) {
        String[] localizationParams = parameter.split(",");
        if (localizationParams.length < 2) {
            return null;
        }
        return localizationParams[1];
    }

    private Boolean parseToBoolean(String parameter) {
        if (parameter == null) {
            return null;
        }
        return Boolean.valueOf(parameter);
    }

    private Integer parseToInteger(String parameter) {
        if (parameter.isEmpty()) {
            return null;
        }
        return Integer.parseInt(parameter);
    }

    private Double parseToDouble(String parameter) {
        if (parameter.isEmpty()) {
            return null;
        }
        return Double.parseDouble(parameter);
    }

    private BigDecimal parseToBigDecimal(String parameter) {
        if (parameter.isEmpty()) {
            return null;
        }
        return BigDecimal.valueOf(Double.parseDouble(parameter));
    }

    private PlaceType parseToPlaceType(String parameter) {
        if (parameter.equals("all")) {
            return null;
        }
        return PlaceType.valueOf(parameter.toUpperCase());
    }

}
