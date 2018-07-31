package com.infoshare.kodziaki.web.dao;

import com.infoshare.kodziaki.Place;
import com.infoshare.kodziaki.UserPreferences;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.util.*;

@Stateless
public class PlaceDao {

    private Logger LOG = LoggerFactory.getLogger(PlaceDao.class);

    @PersistenceContext
    private EntityManager entityManager;

    public void saveAd(Place place) {
        entityManager.persist(place);
    }

    public List<Place> getAll() {
        final Query query = entityManager.createQuery("SELECT p FROM Place p");
        return (List<Place>) query.getResultList();
    }

    public Place findById(int id) {
        return entityManager.find(Place.class, id);
    }


    public Long getLastId() {
        Query queryLastId = entityManager.createQuery("SELECT COUNT(*) FROM Place p");
        return (Long) queryLastId.getSingleResult();
    }

    public void delete(int id) {
        final Place place = entityManager.find(Place.class, id);
        if (place != null) {
            entityManager.remove(place);
        }
    }

    public Place update(Place place) {
        return entityManager.merge(place);
    }

    public void updateAdVisits(int id) {
        final Place place = entityManager.find(Place.class, id);
        if (place != null) {
            place.setVisits(place.getVisits() + 1);
            entityManager.merge(place);
        }
    }

    public List<Object[]> getDistrictsStatistics() {
        final Query query = entityManager
                .createQuery("SELECT p.district,SUM(visits) FROM Place p GROUP BY p.district order by sum(visits) desc");
        return (List<Object[]>) query.getResultList();
    }

    public List<Object[]> getCitiesStatistics() {
        final Query query = entityManager
                .createQuery("SELECT p.city,SUM(visits) FROM Place p GROUP BY p.city order by sum(visits) desc");
        return (List<Object[]>) query.getResultList();
    }

    public List<Place> getAdsStatistics() {
        final Query query = entityManager.createQuery("SELECT p FROM Place p ORDER BY visits desc");
        return (List<Place>) query.getResultList();
    }

    public List<Place> getXRandomAds(int num) {

        Query queryIds = entityManager.createQuery("SELECT p.id FROM Place p");
        List<Integer> ids = queryIds.getResultList();

        if (num <= ids.size()) {

            final Set<Integer> randomIds = new HashSet<>();
            Random r = new Random();

            while (randomIds.size() != num) {
                randomIds.add(r.nextInt(ids.size()));
            }

            Query query = entityManager.createQuery("SELECT P FROM Place p WHERE p.id IN :ids");
            query.setParameter("ids", randomIds);
            return query.getResultList();

        } else {
            return new ArrayList<>();
        }
    }

    public Place getRandomAd() {
        Query queryIds = entityManager.createQuery("SELECT p.id FROM Place p");
        List<Integer> ids = queryIds.getResultList();

        Random r = new Random();
        int random = r.nextInt(ids.size()-1);
        return findById(random);
    }

    public List<Place> getXMostPopularAds() {
        final Query query = entityManager.createQuery("SELECT p FROM Place p ORDER BY visits desc");
        return (List<Place>) query.setMaxResults(4).getResultList();
    }

    public List<Place> getXPromotedAds() {
        final Query query = entityManager.createQuery("SELECT p FROM Place p WHERE p.isPromoted = true");
        List<Place> allPromoted = (List<Place>) query.getResultList();

        final Set<Integer> randomIndexes = new HashSet<>();
        Random r = new Random();

        while (randomIndexes.size() != 4) {
            randomIndexes.add(r.nextInt(allPromoted.size()));
        }

        List<Place> randomPromoted = new ArrayList<>();
        for (Integer num : randomIndexes) {
            randomPromoted.add(allPromoted.get(num));
        }

        return randomPromoted;
    }

    public List<Place> getAdsByUserPreferences(UserPreferences pref) {
        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        CriteriaQuery<Place> criteriaQuery = criteriaBuilder.createQuery(Place.class);
        Root<Place> root = criteriaQuery.from(Place.class);
        List<Predicate> predicates = new ArrayList<>();

        if (pref.getPlaceType() != null) {
            predicates.add(criteriaBuilder.equal(root.get("placeType"), pref.getPlaceType()));
            LOG.info("sorted by parameter: placeType");
        }

        if (pref.getCity() != null && !pref.getCity().isEmpty()) {
            predicates.add(criteriaBuilder.equal(root.get("city"), pref.getCity()));
            LOG.info("sorted by parameter: city");
        }

        if (pref.getDistrict() != null && !pref.getDistrict().isEmpty()) {
            predicates.add(criteriaBuilder.equal(root.get("district"), pref.getDistrict()));
            LOG.info("sorted by parameter: district");
        }

        if (pref.getMinPrice() != null) {
            predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("price"), pref.getMinPrice()));
            LOG.info("sorted by parameter: minPrice");
        }

        if (pref.getMaxPrice() != null) {
            predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("price"), pref.getMaxPrice()));
            LOG.info("sorted by parameter: maxPrice");
        }

        if (pref.getMinArea() != null) {
            predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("area"), pref.getMinArea()));
            LOG.info("sorted by parameter: minArea");
        }

        if (pref.getMaxArea() != null) {
            predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("area"), pref.getMaxArea()));
            LOG.info("sorted by parameter: maxArea");
        }

        if (pref.getMinFloor() != null) {
            predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("floor"), pref.getMinFloor()));
            LOG.info("sorted by parameter: minFloor");
        }

        if (pref.getMaxFloor() != null) {
            predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("floor"), pref.getMaxFloor()));
            LOG.info("sorted by parameter: maxFloor");
        }

        if (pref.getMinRooms() != null) {
            predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("rooms"), pref.getMinRooms()));
            LOG.info("sorted by parameter: minRooms");
        }

        if (pref.getMaxRooms() != null) {
            predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("rooms"), pref.getMaxRooms()));
            LOG.info("sorted by parameter: maxRooms");
        }

        if (pref.isAnimalAllowed() != null) {
            predicates.add(criteriaBuilder.equal(root.get("animalAllowed"), pref.isAnimalAllowed()));
            LOG.info("sorted by parameter: isAnimalAllowed");
        }

        if (pref.isSmokingAllowed() != null) {
            predicates.add(criteriaBuilder.equal(root.get("smokingAllowed"), pref.isSmokingAllowed()));
            LOG.info("sorted by parameter: isSmokingAllowed");
        }

        if (pref.isHasElevator() != null) {
            predicates.add(criteriaBuilder.equal(root.get("hasElevator"), pref.isHasElevator()));
            LOG.info("sorted by parameter: hasElevator");
        }

        if (pref.isOnlyLongTerm() != null) {
            predicates.add(criteriaBuilder.equal(root.get("onlyLongTerm"), pref.isOnlyLongTerm()));
            LOG.info("sorted by parameter: isOnlyLongTerm");
        }

        criteriaQuery.where(predicates.toArray(new Predicate[]{}));
        Query query = entityManager.createQuery(criteriaQuery);
        return (List<Place>) query.getResultList();
    }

}
