package rockets.mining;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rockets.dataaccess.DAO;
import rockets.model.Launch;
import rockets.model.LaunchServiceProvider;
import rockets.model.Rocket;

//import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;


public class RocketMiner {
    private static Logger logger = LoggerFactory.getLogger(RocketMiner.class);

    private DAO dao;

    public RocketMiner(DAO dao) {
        this.dao = dao;
    }

    /**
     * TODO: to be implemented & tested!
     * Returns the top-k most active rockets, as measured by number of completed launches.
     *
     * @param k the number of rockets to be returned.
     * @return the list of k most active rockets.
     */
    public List<Rocket> mostLaunchedRockets(int k) {

        logger.info("find the " + k + "most active rockets");
        List<Rocket> topkList;
        Collection<Launch> launches = dao.loadAll(Launch.class);
        Map<Rocket, Integer> launchMap = frequencyOfListElements((List) launches);

        Map<Rocket, Integer> sortedMap = new LinkedHashMap<Rocket, Integer>();
        List<Map.Entry<Rocket, Integer>> entryList = new ArrayList<Map.Entry<Rocket, Integer>>(launchMap.entrySet());
        Collections.sort(entryList, new MapValueComparator());
        Iterator<Map.Entry<Rocket, Integer>> iter = entryList.iterator();
        Map.Entry<Rocket, Integer> tmpEntry = null;
        while (iter.hasNext()) {
            tmpEntry = iter.next();
            sortedMap.put(tmpEntry.getKey(), tmpEntry.getValue());
        }
        List<Rocket> RocketList = (List<Rocket>) sortedMap.keySet();
        topkList =RocketList.subList(0,k);

//        Comparator<Launch> launchDateComparator = (a, b) -> -a.getLaunchDate().compareTo(b.getLaunchDate());
//        return launches.stream().sorted(launchDateComparator).limit(k).collect(Collectors.toList());


        return topkList;
    }

    /**
     * java统计List集合中每个元素出现的次数
     */
    public static Map<Rocket, Integer> frequencyOfListElements(List<Launch> items) {
        if (items == null || items.size() == 0) return null;
        Map<Rocket, Integer> map = new HashMap<Rocket, Integer>();
        for (Launch temp : items) {
            Integer count = map.get(temp);
            map.put(temp.getLaunchVehicle(), (count == null) ? 1 : count + 1);
        }
        return map;
    }

    public static Map<String, Integer> sortMapByValue(Map<String, Integer> oriMap) {
        if (oriMap == null || oriMap.isEmpty()) {
            return null;
        }

        Map<String, Integer> sortedMap = new LinkedHashMap<String, Integer>();
        List<Map.Entry<String, Integer>> entryList = new ArrayList<Map.Entry<String, Integer>>(oriMap.entrySet());
        Collections.sort(entryList, (Comparator<? super Map.Entry<String, Integer>>) new MapValueComparator());
        Iterator<Map.Entry<String, Integer>> iter = entryList.iterator();
        Map.Entry<String, Integer> tmpEntry = null;
        while (iter.hasNext()) {
            tmpEntry = iter.next();
            sortedMap.put(tmpEntry.getKey(), tmpEntry.getValue());
        }
        return sortedMap;

    }


    /**
     * TODO: to be implemented & tested!
     * <p>
     * Returns the top-k most reliable launch service providers as measured
     * by percentage of successful launches.
     *
     * @param k the number of launch service providers to be returned.
     * @return the list of k most reliable ones.
     */
    public List<LaunchServiceProvider> mostReliableLaunchServiceProviders(int k) {
        return Collections.emptyList();
    }

    /**
     * <p>
     * Returns the top-k most recent launches.
     *
     * @param k the number of launches to be returned.
     * @return the list of k most recent launches.
     */
    public List<Launch> mostRecentLaunches(int k) {
        logger.info("find most recent " + k + " launches");
        Collection<Launch> launches = dao.loadAll(Launch.class);
        Comparator<Launch> launchDateComparator = (a, b) -> -a.getLaunchDate().compareTo(b.getLaunchDate());
        return launches.stream().sorted(launchDateComparator).limit(k).collect(Collectors.toList());
    }

    /**
     * TODO: to be implemented & tested!
     * <p>
     * Returns the dominant country who has the most launched rockets in an orbit.
     *
     * @param orbit the orbit
     * @return the country who sends the most payload to the orbit
     */
    public String dominantCountry(String orbit) {
        return null;
    }

    /**
     * TODO: to be implemented & tested!
     * <p>
     * Returns the top-k most expensive launches.
     *
     * @param k the number of launches to be returned.
     * @return the list of k most expensive launches.
     */
    public List<Launch> mostExpensiveLaunches(int k) {
        return Collections.emptyList();
    }

    /**
     * TODO: to be implemented & tested!
     * <p>
     * Returns a list of launch service provider that has the top-k highest
     * sales revenue in a year.
     *
     * @param k    the number of launch service provider.
     * @param year the year in request
     * @return the list of k launch service providers who has the highest sales revenue.
     */
    public List<LaunchServiceProvider> highestRevenueLaunchServiceProviders(int k, int year) {
        return Collections.emptyList();



    }
}


class MapValueComparator implements Comparator<Map.Entry<Rocket, Integer>> {
    @Override
    public int compare(Map.Entry<String, String> me1, Map.Entry<String, String> me2) {
        return me1.getValue().compareTo(me2.getValue());
    }
}
